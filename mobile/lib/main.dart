import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smartjarvis_mobile/providers/app_state.dart';
import 'package:smartjarvis_mobile/screens/home_screen.dart';

void main() {
  runApp(const SmartJarvisApp());
}

class SmartJarvisApp extends StatelessWidget {
  const SmartJarvisApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) => AppState(),
      child: MaterialApp(
        title: 'SmartJARVIS',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(
            seedColor: Colors.deepPurple,
            brightness: Brightness.light,
          ),
          useMaterial3: true,
          fontFamily: 'Roboto',
        ),
        darkTheme: ThemeData(
          colorScheme: ColorScheme.fromSeed(
            seedColor: Colors.deepPurple,
            brightness: Brightness.dark,
          ),
          useMaterial3: true,
          fontFamily: 'Roboto',
        ),
        home: const HomeScreen(),
        debugShowCheckedModeBanner: false,
      ),
    );
  }
}
