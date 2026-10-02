import 'package:flutter/material.dart';
import 'repository.dart';
import 'app_view_model.dart';
import 'screens.dart';

void main() {
  final vm=AppViewModel(DemoRepository.seed());
  runApp(AppScope(vm:vm,child:MaterialApp(debugShowCheckedModeBanner:false,title:'DLC El Salvador',theme:dlcTheme(),home:const LoginScreen())));
}
