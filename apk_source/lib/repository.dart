import 'models.dart';

class DemoRepository {
  DemoRepository.seed() {
    stations.addAll([
      _station('sa','DLC Santa Ana','SA-001','Santa Ana',18460,4200,3800,1200,6000,[12000,16000,15000,19000,21000,24000,18460]),
      _station('ss','DLC San Salvador Centro','SS-001','San Salvador',22850,6800,5900,4200,10000,[17000,21000,19000,23000,22000,26000,22850]),
      _station('sm','DLC San Miguel','SM-001','San Miguel',20150,8500,7900,8200,10000,[15000,18000,17000,20500,22500,21000,20150]),
      _station('so','DLC Sonsonate','SO-001','Sonsonate',19880,9000,8100,7600,10000,[14000,16500,18500,18000,21000,20000,19880]),
    ]);
    users.addAll([
      AppUser(id:'u1',name:'Gerente General',email:'general@dlc.com.sv',password:'1234',role:UserRole.generalManager),
      AppUser(id:'u2',name:'Gerente Santa Ana',email:'santaana@dlc.com.sv',password:'1234',role:UserRole.branchManager,stationId:'sa'),
    ]);
    cuts['sa'] = [_morning(), _evening()];
  }

  final List<AppUser> users = [];
  final List<Station> stations = [];
  final Map<String,List<DailyCut>> cuts = {};

  Station _station(String id,String name,String code,String dep,double sales,double sup,double reg,double diesel,double cap,List<double> chart) {
    return Station(
      id:id,name:name,code:code,department:dep,address:dep + ', El Salvador',todaySalesUsd:sales,last7DaysSales:chart,
      tanks:{
        FuelType.superFuel:FuelTank(type:FuelType.superFuel,currentGallons:sup,capacityGallons:cap),
        FuelType.regular:FuelTank(type:FuelType.regular,currentGallons:reg,capacityGallons:cap),
        FuelType.diesel:FuelTank(type:FuelType.diesel,currentGallons:diesel,capacityGallons:cap),
      },
    );
  }

  PumpRecord _pump(int n,double base) {
    return PumpRecord(pumpNumber:n,readings:{
      FuelType.superFuel:PumpFuelReading(fuelType:FuelType.superFuel,initialReading:base,finalReading:base+100+n*2),
      FuelType.regular:PumpFuelReading(fuelType:FuelType.regular,initialReading:base+1000,finalReading:base+1070+n),
      FuelType.diesel:PumpFuelReading(fuelType:FuelType.diesel,initialReading:base+2000,finalReading:base+2040+n),
    });
  }

  DailyCut _morning() {
    final c = DailyCut(id:'morning',label:'Matutino',pumps:List.generate(6,(i)=>_pump(i+1,1000+i*200)),status:CutStatus.completed);
    return c;
  }

  DailyCut _evening() {
    final c = DailyCut(id:'evening',label:'Vespertino / Nocturno',pumps:List.generate(6,(i)=>i<4?_pump(i+1,3000+i*200):PumpRecord(pumpNumber:i+1)),status:CutStatus.inProgress);
    c.purchases[FuelType.diesel] = 500;
    c.losses[FuelType.diesel] = 10;
    return c;
  }
}
