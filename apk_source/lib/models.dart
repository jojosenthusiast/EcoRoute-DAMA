enum UserRole { generalManager, branchManager }
enum FuelType { superFuel, regular, diesel }
enum FuelStatus { optimal, medium, critical }
enum CutStatus { pending, inProgress, completed }

String fuelLabel(FuelType f) {
  switch (f) {
    case FuelType.superFuel: return 'Súper';
    case FuelType.regular: return 'Regular';
    case FuelType.diesel: return 'Diésel';
  }
}

class AppUser {
  AppUser({required this.id, required this.name, required this.email, required this.password, required this.role, this.stationId});
  final String id, name, email, password;
  final UserRole role;
  final String? stationId;
}

class FuelTank {
  FuelTank({required this.type, required this.currentGallons, required this.capacityGallons});
  final FuelType type;
  double currentGallons;
  final double capacityGallons;
  double get percent => capacityGallons <= 0 ? 0 : currentGallons / capacityGallons;
  FuelStatus get status => percent < .25 ? FuelStatus.critical : (percent <= .50 ? FuelStatus.medium : FuelStatus.optimal);
}

class PumpFuelReading {
  PumpFuelReading({required this.fuelType, required this.initialReading, required this.finalReading});
  final FuelType fuelType;
  final double initialReading, finalReading;
  double get soldGallons => (finalReading - initialReading).clamp(0.0, double.infinity).toDouble();
}

class PumpRecord {
  PumpRecord({required this.pumpNumber, Map<FuelType, PumpFuelReading>? readings}) : readings = readings ?? {};
  final int pumpNumber;
  final Map<FuelType, PumpFuelReading> readings;
  bool get complete => readings.length == 3 && FuelType.values.every(readings.containsKey);
}

class DailyCut {
  DailyCut({required this.id, required this.label, required this.pumps, this.status = CutStatus.pending});
  final String id, label;
  final List<PumpRecord> pumps;
  CutStatus status;
  final Map<FuelType, double> purchases = {for (final f in FuelType.values) f: 0};
  final Map<FuelType, double> losses = {for (final f in FuelType.values) f: 0};
  bool get canClose => pumps.length == 6 && pumps.every((p) => p.complete);
  Map<FuelType, double> get salesByFuel {
    final out = {for (final f in FuelType.values) f: 0.0};
    for (final pump in pumps) {
      for (final f in FuelType.values) {
        out[f] = (out[f] ?? 0) + (pump.readings[f]?.soldGallons ?? 0);
      }
    }
    return out;
  }
}

class Station {
  Station({
    required this.id, required this.name, required this.code, required this.department, required this.address,
    required this.tanks, required this.todaySalesUsd, required this.last7DaysSales,
  });
  final String id, name, code, department, address;
  final Map<FuelType, FuelTank> tanks;
  double todaySalesUsd;
  final List<double> last7DaysSales;
  FuelStatus get overallStatus {
    if (tanks.values.any((t) => t.status == FuelStatus.critical)) return FuelStatus.critical;
    if (tanks.values.any((t) => t.status == FuelStatus.medium)) return FuelStatus.medium;
    return FuelStatus.optimal;
  }
}


extension FirstOrNullExtension<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
