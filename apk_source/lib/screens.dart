import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'models.dart';
import 'app_view_model.dart';

const orange=Color(0xFFF36A21), ink=Color(0xFF171717), bg=Color(0xFFF7F5F1), line=Color(0xFFE5E1DA);
const green=Color(0xFF1B9E66), yellow=Color(0xFFD99A12), red=Color(0xFFD94A4A);

class DlcLogo extends StatelessWidget {
  const DlcLogo({super.key,this.big=false});
  final bool big;
  @override Widget build(BuildContext context){
    return Row(mainAxisSize:MainAxisSize.min,children:[
      CustomPaint(size:Size(big?36:22,big?48:30),painter:_Drop()),
      const SizedBox(width:7),
      Text('DLC',style:TextStyle(fontSize:big?34:22,fontWeight:FontWeight.w900,letterSpacing:-1.5,color:ink)),
      if(big)...[const SizedBox(width:7),const Text('El Salvador',style:TextStyle(fontWeight:FontWeight.w700,color:Color(0xFF555555)))]
    ]);
  }
}
class _Drop extends CustomPainter{
  @override void paint(Canvas c,Size s){
    final p=Path()..moveTo(s.width/2,0)
      ..cubicTo(s.width*.36,s.height*.22,s.width*.07,s.height*.58,s.width*.07,s.height*.73)
      ..cubicTo(s.width*.07,s.height*.93,s.width*.25,s.height,s.width/2,s.height)
      ..cubicTo(s.width*.75,s.height,s.width*.93,s.height*.93,s.width*.93,s.height*.73)
      ..cubicTo(s.width*.93,s.height*.58,s.width*.64,s.height*.22,s.width/2,0)..close();
    c.drawPath(p,Paint()..color=orange);
  }
  @override bool shouldRepaint(covariant CustomPainter oldDelegate)=>false;
}
ThemeData dlcTheme()=>ThemeData(
  useMaterial3:true,scaffoldBackgroundColor:bg,colorScheme:ColorScheme.fromSeed(seedColor:orange,surface:Colors.white),
  inputDecorationTheme:InputDecorationTheme(filled:true,fillColor:Colors.white,border:OutlineInputBorder(borderRadius:BorderRadius.circular(14),borderSide:const BorderSide(color:line)),enabledBorder:OutlineInputBorder(borderRadius:BorderRadius.circular(14),borderSide:const BorderSide(color:line))),
  cardTheme:CardThemeData(elevation:0,color:Colors.white,shape:RoundedRectangleBorder(borderRadius:BorderRadius.circular(18),side:const BorderSide(color:line))),
  filledButtonTheme:FilledButtonThemeData(style:FilledButton.styleFrom(backgroundColor:orange,foregroundColor:Colors.white,minimumSize:const Size.fromHeight(48),shape:RoundedRectangleBorder(borderRadius:BorderRadius.circular(14)),textStyle:const TextStyle(fontWeight:FontWeight.w800))),
);

class LoginScreen extends StatefulWidget { const LoginScreen({super.key}); @override State<LoginScreen> createState()=>_LoginState(); }
class _LoginState extends State<LoginScreen>{
  final email=TextEditingController(text:'general@dlc.com.sv'), pass=TextEditingController(text:'1234');
  String? error;
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context);
    return Scaffold(body:SafeArea(child:Center(child:SingleChildScrollView(padding:const EdgeInsets.all(24),child:ConstrainedBox(constraints:const BoxConstraints(maxWidth:430),child:Column(children:[
      const DlcLogo(big:true),const SizedBox(height:18),
      const Text('Gestión de estaciones',style:TextStyle(fontSize:28,fontWeight:FontWeight.w900)),const SizedBox(height:6),
      const Text('DLC El Salvador',style:TextStyle(color:Color(0xFF666666))),const SizedBox(height:28),
      Card(child:Padding(padding:const EdgeInsets.all(20),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
        const Text('Iniciar sesión',style:TextStyle(fontSize:20,fontWeight:FontWeight.w800)),const SizedBox(height:18),
        TextField(controller:email,keyboardType:TextInputType.emailAddress,decoration:const InputDecoration(labelText:'Usuario')),const SizedBox(height:12),
        TextField(controller:pass,obscureText:true,decoration:const InputDecoration(labelText:'Contraseña')),const SizedBox(height:10),
        if(error!=null) Padding(padding:const EdgeInsets.only(bottom:8),child:Text(error!,style:const TextStyle(color:red,fontWeight:FontWeight.w700))),
        FilledButton(onPressed:(){
          if(!vm.login(email.text,pass.text)){setState(()=>error='Credenciales incorrectas.');return;}
          final next=vm.currentUser!.role==UserRole.generalManager?const GeneralShell():const BranchShell();
          Navigator.pushReplacement(context,MaterialPageRoute(builder:(_)=>next));
        },child:const Text('Entrar')),
      ]))),
      const SizedBox(height:18),
      const Text('General: general@dlc.com.sv / 1234\nSucursal: santaana@dlc.com.sv / 1234',textAlign:TextAlign.center,style:TextStyle(fontSize:12,color:Color(0xFF777777))),
    ]))))));
  }
}

Widget pageHeader(String title,{String? subtitle,VoidCallback? logout}){
  return Padding(padding:const EdgeInsets.fromLTRB(20,18,20,10),child:Row(children:[
    const DlcLogo(),const Spacer(),
    if(subtitle!=null) Flexible(child:Text(subtitle,overflow:TextOverflow.ellipsis,style:const TextStyle(color:Color(0xFF666666),fontWeight:FontWeight.w600))),
    if(logout!=null) IconButton(onPressed:logout,icon:const Icon(Icons.logout_rounded))
  ]));
}
Widget titleBlock(String title,String subtitle)=>Padding(padding:const EdgeInsets.fromLTRB(20,6,20,14),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
  Text(title,style:const TextStyle(fontSize:29,fontWeight:FontWeight.w900,color:ink)),
  const SizedBox(height:4),Text(subtitle,style:const TextStyle(color:Color(0xFF666666)))
]));

class StatusBadge extends StatelessWidget{
  const StatusBadge(this.status,{super.key}); final FuelStatus status;
  @override Widget build(BuildContext c){
    final t=status==FuelStatus.critical?'Crítico':status==FuelStatus.medium?'Medio':'Óptimo';
    final col=status==FuelStatus.critical?red:status==FuelStatus.medium?yellow:green;
    return Container(padding:const EdgeInsets.symmetric(horizontal:9,vertical:5),decoration:BoxDecoration(color:col.withValues(alpha:.12),borderRadius:BorderRadius.circular(99)),child:Text(t,style:TextStyle(fontSize:12,fontWeight:FontWeight.w800,color:col)));
  }
}
class TankTile extends StatelessWidget{
  const TankTile(this.tank,{super.key}); final FuelTank tank;
  @override Widget build(BuildContext c){
    final col=tank.status==FuelStatus.critical?red:tank.status==FuelStatus.medium?yellow:green;
    final pct=tank.percent.clamp(0.0,1.0);
    return Padding(padding:const EdgeInsets.symmetric(vertical:10),child:Column(children:[
      Row(children:[Expanded(child:Text(fuelLabel(tank.type),style:const TextStyle(fontWeight:FontWeight.w800))),Text(tank.currentGallons.toStringAsFixed(0)+' / '+tank.capacityGallons.toStringAsFixed(0)+' gal',style:const TextStyle(fontWeight:FontWeight.w700)),const SizedBox(width:8),StatusBadge(tank.status)]),
      const SizedBox(height:8),Row(children:[Expanded(child:ClipRRect(borderRadius:BorderRadius.circular(99),child:LinearProgressIndicator(value:pct,minHeight:9,backgroundColor:const Color(0xFFE9E6E1),valueColor:AlwaysStoppedAnimation(col)))),const SizedBox(width:10),SizedBox(width:38,child:Text((pct*100).round().toString()+'%',textAlign:TextAlign.end))])
    ]));
  }
}
class BarChart extends StatelessWidget{
  const BarChart(this.values,{super.key}); final List<double> values;
  @override Widget build(BuildContext c)=>SizedBox(height:130,width:double.infinity,child:CustomPaint(painter:_Bars(values)));
}
class _Bars extends CustomPainter{
  _Bars(this.v); final List<double> v;
  @override void paint(Canvas c,Size s){
    final maxV=v.isEmpty?1.0:v.reduce(math.max); final grid=Paint()..color=line; final bar=Paint()..color=orange;
    for(int i=0;i<4;i++){final y=s.height*(.15+i*.22);c.drawLine(Offset(0,y),Offset(s.width,y),grid);}
    if(v.isEmpty)return; final slot=s.width/v.length;
    for(int i=0;i<v.length;i++){final h=(v[i]/maxV)*s.height*.72;c.drawRRect(RRect.fromRectAndRadius(Rect.fromLTWH(i*slot+slot*.22,s.height-h-6,slot*.56,h),const Radius.circular(5)),bar);}
  }
  @override bool shouldRepaint(covariant _Bars old)=>old.v!=v;
}

class GeneralShell extends StatefulWidget{const GeneralShell({super.key});@override State<GeneralShell> createState()=>_GeneralShellState();}
class _GeneralShellState extends State<GeneralShell>{
  int index=0;
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context);
    final pages=[const GeneralDashboard(),const BranchesScreen(),const AdminScreen()];
    return Scaffold(body:SafeArea(child:pages[index]),bottomNavigationBar:NavigationBar(selectedIndex:index,onDestinationSelected:(v)=>setState(()=>index=v),destinations:const [
      NavigationDestination(icon:Icon(Icons.space_dashboard_outlined),selectedIcon:Icon(Icons.space_dashboard),label:'Inicio'),
      NavigationDestination(icon:Icon(Icons.local_gas_station_outlined),selectedIcon:Icon(Icons.local_gas_station),label:'Sucursales'),
      NavigationDestination(icon:Icon(Icons.manage_accounts_outlined),selectedIcon:Icon(Icons.manage_accounts),label:'Gerentes'),
    ]),floatingActionButton:index==1?FloatingActionButton.extended(onPressed:()=>showStationDialog(context,vm),icon:const Icon(Icons.add),label:const Text('Sucursal')):index==2?FloatingActionButton.extended(onPressed:()=>showManagerDialog(context,vm),icon:const Icon(Icons.person_add),label:const Text('Gerente')):null);
  }
}

class GeneralDashboard extends StatelessWidget{
  const GeneralDashboard({super.key});
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context), list=vm.filteredStations;
    final sales=list.fold<double>(0,(a,s)=>a+s.todaySalesUsd);
    final chart=List.generate(7,(i)=>list.fold<double>(0,(a,s)=>a+s.last7DaysSales[i]));
    final agg={for(final f in FuelType.values) f:FuelTank(type:f,currentGallons:list.fold<double>(0,(a,s)=>a+s.tanks[f]!.currentGallons),capacityGallons:list.fold<double>(0,(a,s)=>a+s.tanks[f]!.capacityGallons))};
    final alerts=<MapEntry<Station,FuelTank>>[];
    for(final s in list){for(final t in s.tanks.values){if(t.status!=FuelStatus.optimal)alerts.add(MapEntry(s,t));}}
    return CustomScrollView(slivers:[
      SliverToBoxAdapter(child:pageHeader('Resumen',subtitle:'Gerente General',logout:(){vm.logout();Navigator.pushReplacement(context,MaterialPageRoute(builder:(_)=>const LoginScreen()));})),
      SliverToBoxAdapter(child:titleBlock('Resumen de la red','Ventas, inventario y alertas de combustible')),
      SliverPadding(padding:const EdgeInsets.symmetric(horizontal:20),sliver:SliverList(delegate:SliverChildListDelegate([
        DropdownButtonFormField<String>(value:vm.selectedStationId,items:[const DropdownMenuItem(value:'all',child:Text('Toda la red')),...vm.stations.map((s)=>DropdownMenuItem(value:s.id,child:Text(s.name)))],onChanged:(v){if(v!=null)vm.selectStation(v);},decoration:const InputDecoration(labelText:'Vista')),
        const SizedBox(height:14),
        Card(color:ink,child:Padding(padding:const EdgeInsets.all(18),child:Row(children:[
          Expanded(child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('VENTAS HOY',style:TextStyle(color:Colors.white70,fontSize:12,fontWeight:FontWeight.w800)),const SizedBox(height:6),Text('\$'+sales.toStringAsFixed(2),style:const TextStyle(color:Colors.white,fontSize:30,fontWeight:FontWeight.w900))])),
          const Icon(Icons.trending_up_rounded,color:orange,size:48)
        ]))),
        const SizedBox(height:14),
        Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('Ventas · últimos 7 días',style:TextStyle(fontWeight:FontWeight.w900,fontSize:17)),const SizedBox(height:12),BarChart(chart)]))),
        const SizedBox(height:14),
        Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('Inventario actual',style:TextStyle(fontWeight:FontWeight.w900,fontSize:17)),const Text('Cantidad disponible frente a capacidad',style:TextStyle(color:Color(0xFF777777))),const SizedBox(height:4),...FuelType.values.map((f)=>TankTile(agg[f]!))]))),
        const SizedBox(height:14),
        const Text('Requieren atención',style:TextStyle(fontWeight:FontWeight.w900,fontSize:18)),const SizedBox(height:8),
        if(alerts.isEmpty) const Card(child:Padding(padding:EdgeInsets.all(16),child:Text('No hay alertas activas.')))
        else ...alerts.take(5).map((e)=>Card(child:ListTile(title:Text(e.key.name,style:const TextStyle(fontWeight:FontWeight.w800)),subtitle:Text(fuelLabel(e.value.type)+' · '+e.value.currentGallons.toStringAsFixed(0)+' / '+e.value.capacityGallons.toStringAsFixed(0)+' gal'),trailing:StatusBadge(e.value.status),onTap:()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>StationDetailScreen(e.key)))))),
        const SizedBox(height:24),
      ]))),
    ]);
  }
}

class BranchesScreen extends StatelessWidget{
  const BranchesScreen({super.key});
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context);
    return Column(children:[pageHeader('Sucursales',subtitle:'Gerente General'),titleBlock('Sucursales','Estado actual de la red'),Expanded(child:ListView.separated(padding:const EdgeInsets.fromLTRB(20,0,20,100),itemCount:vm.stations.length,separatorBuilder:(_,__)=>const SizedBox(height:10),itemBuilder:(_,i){
      final s=vm.stations[i];return Card(child:ListTile(contentPadding:const EdgeInsets.all(14),leading:const CircleAvatar(backgroundColor:Color(0xFFFFEEE4),child:Icon(Icons.local_gas_station,color:orange)),title:Text(s.name,style:const TextStyle(fontWeight:FontWeight.w800)),subtitle:Text(s.code+' · '+s.department),trailing:StatusBadge(s.overallStatus),onTap:()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>StationDetailScreen(s))));
    }))]);
  }
}
class StationDetailScreen extends StatelessWidget{
  const StationDetailScreen(this.station,{super.key});final Station station;
  @override Widget build(BuildContext context)=>Scaffold(appBar:AppBar(title:Text(station.name)),body:ListView(padding:const EdgeInsets.all(20),children:[
    Text(station.code+' · '+station.department,style:const TextStyle(color:Color(0xFF666666))),const SizedBox(height:12),
    Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('Tanques',style:TextStyle(fontSize:18,fontWeight:FontWeight.w900)),...FuelType.values.map((f)=>TankTile(station.tanks[f]!))]))),
    const SizedBox(height:14),Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('Ventas · 7 días',style:TextStyle(fontSize:18,fontWeight:FontWeight.w900)),const SizedBox(height:12),BarChart(station.last7DaysSales)]))),
  ]);
}

class AdminScreen extends StatelessWidget{
  const AdminScreen({super.key});
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context);
    final managers=vm.repo.users.where((u)=>u.role==UserRole.branchManager).toList();
    return Column(children:[pageHeader('Gerentes',subtitle:'Gerente General'),titleBlock('Administración','Perfiles y asignación de sucursales'),Expanded(child:ListView(padding:const EdgeInsets.fromLTRB(20,0,20,100),children:[
      ...managers.map((u){final s=vm.stations.where((x)=>x.id==u.stationId).firstOrNull;return Card(child:ListTile(leading:const CircleAvatar(child:Icon(Icons.person)),title:Text(u.name,style:const TextStyle(fontWeight:FontWeight.w800)),subtitle:Text(u.email+'\n'+(s?.name??'Sin sucursal')),isThreeLine:true));})
    ]))]);
  }
}

Future<void> showStationDialog(BuildContext context,AppViewModel vm) async{
  final n=TextEditingController(),c=TextEditingController(),d=TextEditingController(),a=TextEditingController(),s=TextEditingController(text:'6000'),r=TextEditingController(text:'6000'),di=TextEditingController(text:'6000');
  await showDialog(context:context,builder:(ctx)=>AlertDialog(title:const Text('Nueva sucursal'),content:SizedBox(width:420,child:SingleChildScrollView(child:Column(children:[
    TextField(controller:n,decoration:const InputDecoration(labelText:'Nombre')),const SizedBox(height:8),TextField(controller:c,decoration:const InputDecoration(labelText:'Código')),const SizedBox(height:8),TextField(controller:d,decoration:const InputDecoration(labelText:'Departamento')),const SizedBox(height:8),TextField(controller:a,decoration:const InputDecoration(labelText:'Dirección')),const SizedBox(height:8),
    TextField(controller:s,keyboardType:TextInputType.number,decoration:const InputDecoration(labelText:'Capacidad Súper')),const SizedBox(height:8),TextField(controller:r,keyboardType:TextInputType.number,decoration:const InputDecoration(labelText:'Capacidad Regular')),const SizedBox(height:8),TextField(controller:di,keyboardType:TextInputType.number,decoration:const InputDecoration(labelText:'Capacidad Diésel')),
  ]))),actions:[TextButton(onPressed:()=>Navigator.pop(ctx),child:const Text('Cancelar')),FilledButton(onPressed:(){final err=vm.addStation(n.text,c.text,d.text,a.text,double.tryParse(s.text)??0,double.tryParse(r.text)??0,double.tryParse(di.text)??0);if(err!=null){ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(err)));}else{Navigator.pop(ctx);}},child:const Text('Guardar'))]));
}
Future<void> showManagerDialog(BuildContext context,AppViewModel vm) async{
  final n=TextEditingController(),e=TextEditingController(),p=TextEditingController(text:'1234');String sid=vm.stations.first.id;
  await showDialog(context:context,builder:(ctx)=>StatefulBuilder(builder:(ctx,set)=>AlertDialog(title:const Text('Nuevo gerente'),content:SizedBox(width:420,child:Column(mainAxisSize:MainAxisSize.min,children:[
    TextField(controller:n,decoration:const InputDecoration(labelText:'Nombre')),const SizedBox(height:8),TextField(controller:e,decoration:const InputDecoration(labelText:'Correo')),const SizedBox(height:8),TextField(controller:p,decoration:const InputDecoration(labelText:'Contraseña temporal')),const SizedBox(height:8),
    DropdownButtonFormField<String>(value:sid,items:vm.stations.map((s)=>DropdownMenuItem(value:s.id,child:Text(s.name))).toList(),onChanged:(v){if(v!=null)set(()=>sid=v);},decoration:const InputDecoration(labelText:'Sucursal')),
  ])),actions:[TextButton(onPressed:()=>Navigator.pop(ctx),child:const Text('Cancelar')),FilledButton(onPressed:(){final err=vm.addManager(n.text,e.text,p.text,sid);if(err!=null){ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(err)));}else{Navigator.pop(ctx);}},child:const Text('Crear'))])));
}

class BranchShell extends StatefulWidget{const BranchShell({super.key});@override State<BranchShell> createState()=>_BranchShellState();}
class _BranchShellState extends State<BranchShell>{
  int index=0;
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context),st=vm.currentStation!;
    return Scaffold(body:SafeArea(child:[BranchDashboard(st),CutsScreen(st),PumpsOverview(st)][index]),bottomNavigationBar:NavigationBar(selectedIndex:index,onDestinationSelected:(v)=>setState(()=>index=v),destinations:const [
      NavigationDestination(icon:Icon(Icons.space_dashboard_outlined),selectedIcon:Icon(Icons.space_dashboard),label:'Inicio'),
      NavigationDestination(icon:Icon(Icons.receipt_long_outlined),selectedIcon:Icon(Icons.receipt_long),label:'Cortes'),
      NavigationDestination(icon:Icon(Icons.local_gas_station_outlined),selectedIcon:Icon(Icons.local_gas_station),label:'Bombas'),
    ]));
  }
}
class BranchDashboard extends StatelessWidget{
  const BranchDashboard(this.station,{super.key});final Station station;
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context),cuts=vm.cutsFor(station.id);
    return ListView(padding:EdgeInsets.zero,children:[
      pageHeader('Sucursal',subtitle:station.name,logout:(){vm.logout();Navigator.pushReplacement(context,MaterialPageRoute(builder:(_)=>const LoginScreen()));}),
      titleBlock(station.name,'Inventario y operación del día'),
      Padding(padding:const EdgeInsets.symmetric(horizontal:20),child:Column(children:[
        Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('Inventario de tanques',style:TextStyle(fontSize:18,fontWeight:FontWeight.w900)),...FuelType.values.map((f)=>TankTile(station.tanks[f]!))]))),
        const SizedBox(height:14),Align(alignment:Alignment.centerLeft,child:Text('Cortes de hoy',style:Theme.of(context).textTheme.titleLarge)),const SizedBox(height:8),
        ...cuts.map((cut)=>Card(child:ListTile(title:Text(cut.label,style:const TextStyle(fontWeight:FontWeight.w800)),subtitle:Text((cut.status==CutStatus.completed?'Cerrado':cut.status==CutStatus.inProgress?'En proceso':'Pendiente')+' · '+cut.pumps.where((p)=>p.complete).length.toString()+'/6 bombas'),trailing:Icon(cut.status==CutStatus.completed?Icons.check_circle:Icons.chevron_right,color:cut.status==CutStatus.completed?green:orange),onTap:()=>openCut(context,cut)))),
        const SizedBox(height:14),Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[const Text('Ventas · 7 días',style:TextStyle(fontSize:18,fontWeight:FontWeight.w900)),const SizedBox(height:10),BarChart(station.last7DaysSales)]))),
        const SizedBox(height:24)
      ]))
    ]);
  }
}
class CutsScreen extends StatelessWidget{
  const CutsScreen(this.station,{super.key});final Station station;
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context),cuts=vm.cutsFor(station.id);
    return Column(children:[pageHeader('Cortes',subtitle:station.name),titleBlock('Cortes del día','Exactamente dos cortes por jornada'),Expanded(child:ListView(padding:const EdgeInsets.symmetric(horizontal:20),children:[
      ...cuts.map((c)=>Card(child:ListTile(contentPadding:const EdgeInsets.all(16),title:Text(c.label,style:const TextStyle(fontWeight:FontWeight.w900)),subtitle:Text((c.status==CutStatus.completed?'Cerrado':c.status==CutStatus.inProgress?'En proceso':'Pendiente')+' · '+c.pumps.where((p)=>p.complete).length.toString()+'/6 bombas'),trailing:const Icon(Icons.chevron_right),onTap:()=>openCut(context,c)))),
      const SizedBox(height:12),const Card(child:Padding(padding:EdgeInsets.all(16),child:Text('Para cerrar un corte deben estar registradas las bombas 1, 2, 3, 4, 5 y 6. Las ventas se consolidan desde sus lecturas.')))
    ]))]);
  }
}
class PumpsOverview extends StatelessWidget{
  const PumpsOverview(this.station,{super.key});final Station station;
  @override Widget build(BuildContext context){
    final vm=AppScope.of(context),cuts=vm.cutsFor(station.id),cut=cuts.firstWhere((c)=>c.status!=CutStatus.completed,orElse:()=>cuts.last);
    return Column(children:[pageHeader('Bombas',subtitle:station.name),titleBlock('6 bombas','Estado del corte '+cut.label),Expanded(child:PumpGrid(cut:cut))]);
  }
}

void openCut(BuildContext context,DailyCut cut){
  if(cut.status==CutStatus.completed){Navigator.push(context,MaterialPageRoute(builder:(_)=>CutSummaryScreen(cut)));return;}
  Navigator.push(context,MaterialPageRoute(builder:(_)=>CutMovementScreen(cut)));
}

class CutMovementScreen extends StatefulWidget{const CutMovementScreen(this.cut,{super.key});final DailyCut cut;@override State<CutMovementScreen> createState()=>_CutMovementState();}
class _CutMovementState extends State<CutMovementScreen>{
  late final Map<FuelType,TextEditingController> purchases,losses;
  @override void initState(){super.initState();purchases={for(final f in FuelType.values)f:TextEditingController(text:(widget.cut.purchases[f]??0).toStringAsFixed(0))};losses={for(final f in FuelType.values)f:TextEditingController(text:(widget.cut.losses[f]??0).toStringAsFixed(0))};}
  @override Widget build(BuildContext context){final vm=AppScope.of(context);return Scaffold(appBar:AppBar(title:Text('Corte '+widget.cut.label)),body:ListView(padding:const EdgeInsets.all(20),children:[
    const Text('Movimientos del corte',style:TextStyle(fontSize:24,fontWeight:FontWeight.w900)),const SizedBox(height:8),const Text('Ventas se calculan desde las seis bombas.',style:TextStyle(color:Color(0xFF666666))),const SizedBox(height:16),
    _movementCard('Compras / Recepción',purchases),const SizedBox(height:12),_movementCard('Pérdidas / Daños',losses),const SizedBox(height:18),
    FilledButton(onPressed:(){final p={for(final f in FuelType.values)f:double.tryParse(purchases[f]!.text)??0};final l={for(final f in FuelType.values)f:double.tryParse(losses[f]!.text)??0};final err=vm.saveMovements(widget.cut,p,l);if(err!=null){ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(err)));return;}Navigator.push(context,MaterialPageRoute(builder:(_)=>PumpListScreen(widget.cut)));},child:const Text('Continuar a bombas'))
  ]));}
  Widget _movementCard(String title,Map<FuelType,TextEditingController> ctrls)=>Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text(title,style:const TextStyle(fontWeight:FontWeight.w900,fontSize:17)),const SizedBox(height:12),...FuelType.values.map((f)=>Padding(padding:const EdgeInsets.only(bottom:8),child:TextField(controller:ctrls[f],keyboardType:const TextInputType.numberWithOptions(decimal:true),decoration:InputDecoration(labelText:fuelLabel(f)+' (gal)'))))])));
}
class PumpListScreen extends StatelessWidget{
  const PumpListScreen(this.cut,{super.key});final DailyCut cut;
  @override Widget build(BuildContext context)=>Scaffold(appBar:AppBar(title:Text('Bombas · '+cut.label)),body:PumpGrid(cut:cut));
}
class PumpGrid extends StatelessWidget{
  const PumpGrid({super.key,required this.cut});final DailyCut cut;
  @override Widget build(BuildContext context){
    final done=cut.pumps.where((p)=>p.complete).length;
    return ListView(padding:const EdgeInsets.all(20),children:[
      Text(done.toString()+' de 6 registradas',style:const TextStyle(fontSize:23,fontWeight:FontWeight.w900)),const SizedBox(height:8),LinearProgressIndicator(value:done/6,minHeight:9,borderRadius:BorderRadius.circular(99),color:orange),const SizedBox(height:16),
      GridView.count(crossAxisCount:2,shrinkWrap:true,physics:const NeverScrollableScrollPhysics(),mainAxisSpacing:10,crossAxisSpacing:10,childAspectRatio:1.25,children:cut.pumps.map((p)=>Card(color:p.complete?const Color(0xFFEDF8F2):Colors.white,child:InkWell(borderRadius:BorderRadius.circular(18),onTap:cut.status==CutStatus.completed?null:()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>PumpEntryScreen(cut,p))),child:Padding(padding:const EdgeInsets.all(14),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text('Bomba '+p.pumpNumber.toString(),style:const TextStyle(fontWeight:FontWeight.w900,fontSize:17)),const Spacer(),Icon(p.complete?Icons.check_circle:Icons.edit_note,color:p.complete?green:orange),Text(p.complete?'Registrada':'Pendiente',style:TextStyle(fontWeight:FontWeight.w700,color:p.complete?green:orange))]))))).toList()),
      const SizedBox(height:16),FilledButton(onPressed:cut.canClose?()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>CutSummaryScreen(cut))):null,child:Text(cut.canClose?'Revisar y cerrar corte':'Faltan '+(6-done).toString()+' bombas'))
    ]);
  }
}
class PumpEntryScreen extends StatefulWidget{const PumpEntryScreen(this.cut,this.pump,{super.key});final DailyCut cut;final PumpRecord pump;@override State<PumpEntryScreen> createState()=>_PumpEntryState();}
class _PumpEntryState extends State<PumpEntryScreen>{
  late final Map<FuelType,TextEditingController> initial,finalC;
  @override void initState(){super.initState();initial={for(final f in FuelType.values)f:TextEditingController(text:widget.pump.readings[f]?.initialReading.toStringAsFixed(2)??'')};finalC={for(final f in FuelType.values)f:TextEditingController(text:widget.pump.readings[f]?.finalReading.toStringAsFixed(2)??'')};}
  @override Widget build(BuildContext context){final vm=AppScope.of(context);return Scaffold(appBar:AppBar(title:Text('Bomba '+widget.pump.pumpNumber.toString())),body:ListView(padding:const EdgeInsets.all(20),children:[
    const Text('Lecturas por combustible',style:TextStyle(fontSize:23,fontWeight:FontWeight.w900)),const SizedBox(height:12),
    ...FuelType.values.map((f)=>Card(child:Padding(padding:const EdgeInsets.all(14),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text(fuelLabel(f),style:const TextStyle(fontWeight:FontWeight.w900,fontSize:17)),const SizedBox(height:10),TextField(controller:initial[f],keyboardType:const TextInputType.numberWithOptions(decimal:true),decoration:const InputDecoration(labelText:'Lectura inicial')),const SizedBox(height:8),TextField(controller:finalC[f],keyboardType:const TextInputType.numberWithOptions(decimal:true),decoration:const InputDecoration(labelText:'Lectura final'))]))),
    const SizedBox(height:14),FilledButton(onPressed:(){final readings=<FuelType,PumpFuelReading>{};for(final f in FuelType.values){readings[f]=PumpFuelReading(fuelType:f,initialReading:double.tryParse(initial[f]!.text)??-1,finalReading:double.tryParse(finalC[f]!.text)??-1);}final err=vm.savePump(widget.cut,widget.pump.pumpNumber,readings);if(err!=null){ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(err)));return;}Navigator.pop(context);},child:const Text('Guardar bomba'))
  ]));}
}
class CutSummaryScreen extends StatelessWidget{
  const CutSummaryScreen(this.cut,{super.key});final DailyCut cut;
  @override Widget build(BuildContext context){final vm=AppScope.of(context),sales=cut.salesByFuel;return Scaffold(appBar:AppBar(title:Text('Resumen · '+cut.label)),body:ListView(padding:const EdgeInsets.all(20),children:[
    Card(color:cut.canClose?const Color(0xFFEDF8F2):const Color(0xFFFFF4D8),child:Padding(padding:const EdgeInsets.all(16),child:Text(cut.canClose?'6 de 6 bombas registradas':'Corte incompleto',style:TextStyle(fontWeight:FontWeight.w900,color:cut.canClose?green:yellow)))),
    const SizedBox(height:14),_summary('Ventas consolidadas',sales),const SizedBox(height:10),_summary('Compras / Recepción',cut.purchases),const SizedBox(height:10),_summary('Pérdidas / Daños',cut.losses),const SizedBox(height:18),
    if(cut.status==CutStatus.completed) const Card(child:Padding(padding:EdgeInsets.all(16),child:Text('Corte cerrado. Los registros quedan en solo lectura.',style:TextStyle(fontWeight:FontWeight.w800))))
    else FilledButton(onPressed:cut.canClose?(){final err=vm.closeCut(cut);if(err!=null){ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(err)));return;}ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content:Text('Corte cerrado e inventario actualizado.')));Navigator.popUntil(context,(r)=>r.isFirst);}:null,child:const Text('Cerrar corte'))
  ]));}
  Widget _summary(String title,Map<FuelType,double> values)=>Card(child:Padding(padding:const EdgeInsets.all(16),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text(title,style:const TextStyle(fontSize:17,fontWeight:FontWeight.w900)),const SizedBox(height:8),...FuelType.values.map((f)=>Padding(padding:const EdgeInsets.symmetric(vertical:5),child:Row(children:[Expanded(child:Text(fuelLabel(f))),Text((values[f]??0).toStringAsFixed(2)+' gal',style:const TextStyle(fontWeight:FontWeight.w800))])))])));
}
