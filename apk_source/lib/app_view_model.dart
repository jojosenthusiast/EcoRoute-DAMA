import 'package:flutter/material.dart';
import 'models.dart';
import 'repository.dart';

class AppViewModel extends ChangeNotifier {
  AppViewModel(this.repo);
  final DemoRepository repo;
  AppUser? currentUser;
  String selectedStationId = 'all';

  List<Station> get stations => repo.stations;
  Station? get currentStation {
    final id = currentUser?.stationId;
    if (id == null) return null;
    for (final s in stations) { if (s.id == id) return s; }
    return null;
  }

  bool login(String email,String password) {
    final e = email.trim().toLowerCase();
    currentUser = null;
    for (final u in repo.users) {
      if (u.email.toLowerCase() == e && u.password == password) { currentUser = u; break; }
    }
    notifyListeners();
    return currentUser != null;
  }

  void logout() { currentUser = null; notifyListeners(); }

  List<DailyCut> cutsFor(String stationId) {
    return repo.cuts.putIfAbsent(stationId, () => [
      DailyCut(id:'morning',label:'Matutino',pumps:List.generate(6,(i)=>PumpRecord(pumpNumber:i+1))),
      DailyCut(id:'evening',label:'Vespertino / Nocturno',pumps:List.generate(6,(i)=>PumpRecord(pumpNumber:i+1))),
    ]);
  }

  void selectStation(String id) { selectedStationId = id; notifyListeners(); }

  List<Station> get filteredStations => selectedStationId == 'all' ? stations : stations.where((s)=>s.id==selectedStationId).toList();

  String? addStation(String name,String code,String department,String address,double sup,double reg,double diesel) {
    if ([name,code,department,address].any((e)=>e.trim().isEmpty)) return 'Complete todos los campos.';
    if (stations.any((s)=>s.code.toLowerCase()==code.trim().toLowerCase())) return 'El código ya existe.';
    if (sup<=0 || reg<=0 || diesel<=0) return 'Las capacidades deben ser mayores que cero.';
    stations.add(Station(
      id:'s'+DateTime.now().millisecondsSinceEpoch.toString(),name:name.trim(),code:code.trim(),department:department.trim(),address:address.trim(),
      todaySalesUsd:0,last7DaysSales:List.filled(7,0),
      tanks:{
        FuelType.superFuel:FuelTank(type:FuelType.superFuel,currentGallons:0,capacityGallons:sup),
        FuelType.regular:FuelTank(type:FuelType.regular,currentGallons:0,capacityGallons:reg),
        FuelType.diesel:FuelTank(type:FuelType.diesel,currentGallons:0,capacityGallons:diesel),
      },
    ));
    notifyListeners(); return null;
  }

  String? addManager(String name,String email,String password,String stationId) {
    if (name.trim().isEmpty || !email.contains('@') || password.isEmpty || stationId.isEmpty) return 'Complete datos válidos.';
    if (repo.users.any((u)=>u.email.toLowerCase()==email.trim().toLowerCase())) return 'El correo ya existe.';
    if (!stations.any((s)=>s.id==stationId)) return 'Sucursal inválida.';
    repo.users.add(AppUser(id:'u'+DateTime.now().millisecondsSinceEpoch.toString(),name:name.trim(),email:email.trim(),password:password,role:UserRole.branchManager,stationId:stationId));
    notifyListeners(); return null;
  }

  String? saveMovements(DailyCut cut,Map<FuelType,double> purchases,Map<FuelType,double> losses) {
    final st=currentStation;
    if (st==null) return 'No hay sucursal asignada.';
    if (cut.status==CutStatus.completed) return 'El corte está cerrado.';
    for (final f in FuelType.values) {
      final p=purchases[f]??0, l=losses[f]??0, tank=st.tanks[f]!;
      if (p<0 || l<0) return 'Compras y pérdidas no pueden ser negativas.';
      if (tank.currentGallons+p>tank.capacityGallons) return 'La recepción de '+fuelLabel(f)+' excede la capacidad disponible.';
    }
    cut.purchases..clear()..addAll(purchases);
    cut.losses..clear()..addAll(losses);
    cut.status=CutStatus.inProgress; notifyListeners(); return null;
  }

  String? savePump(DailyCut cut,int pumpNumber,Map<FuelType,PumpFuelReading> readings) {
    if (cut.status==CutStatus.completed) return 'El corte está cerrado.';
    if (pumpNumber<1 || pumpNumber>6) return 'Bomba inválida.';
    for (final f in FuelType.values) {
      final r=readings[f];
      if (r==null) return 'Falta '+fuelLabel(f)+'.';
      if (r.initialReading<0 || r.finalReading<0) return 'Las lecturas no pueden ser negativas.';
      if (r.finalReading<r.initialReading) return 'La lectura final no puede ser menor que la inicial.';
    }
    final p=cut.pumps.firstWhere((p)=>p.pumpNumber==pumpNumber);
    p.readings..clear()..addAll(readings);
    cut.status=CutStatus.inProgress; notifyListeners(); return null;
  }

  String? closeCut(DailyCut cut) {
    if (cut.status==CutStatus.completed) return 'El corte ya está cerrado.';
    final nums=cut.pumps.map((p)=>p.pumpNumber).toSet();
    if (cut.pumps.length!=6 || nums.length!=6 || !nums.containsAll({1,2,3,4,5,6})) return 'Deben existir exactamente las bombas 1 a 6.';
    if (!cut.canClose) return 'Debe registrar las 6 bombas antes de cerrar.';
    final st=currentStation!;
    final sales=cut.salesByFuel;
    for (final f in FuelType.values) {
      final tank=st.tanks[f]!;
      final next=tank.currentGallons+(cut.purchases[f]??0)-(sales[f]??0)-(cut.losses[f]??0);
      if (next<0) return 'El inventario de '+fuelLabel(f)+' quedaría negativo.';
      if (next>tank.capacityGallons) return 'El inventario de '+fuelLabel(f)+' excedería la capacidad.';
    }
    for (final f in FuelType.values) {
      final tank=st.tanks[f]!;
      tank.currentGallons += (cut.purchases[f]??0)-(sales[f]??0)-(cut.losses[f]??0);
    }
    cut.status=CutStatus.completed; notifyListeners(); return null;
  }
}

class AppScope extends InheritedNotifier<AppViewModel> {
  const AppScope({super.key,required AppViewModel vm,required super.child}):super(notifier:vm);
  static AppViewModel of(BuildContext context) => context.dependOnInheritedWidgetOfExactType<AppScope>()!.notifier!;
}
