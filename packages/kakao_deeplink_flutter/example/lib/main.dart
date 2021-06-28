import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:kakao_deeplink_flutter/kakao_deeplink_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _isKakaoNaviInstalled = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await KakaoDeeplinkFlutter.platformVersion ??
          'Unknown platform version';
      _isKakaoNaviInstalled = await KakaoDeeplinkFlutter.isKaKaoNaviInstalled();
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            Center(
              child: Text('Running on: $_isKakaoNaviInstalled\n'),
            ),
            ElevatedButton.icon(
              onPressed: !_isKakaoNaviInstalled
                  ? null
                  : () {
                      KakaoDeeplinkFlutter.navigateTo(
                        placeName: '고객님',
                        longitude: '37.5371097',
                        latitude: '127.1798046',
                      );
                    },
              icon: Icon(Icons.map_outlined),
              label: Text('Open Kakao Navi'),
            ),
          ],
        ),
      ),
    );
  }
}
