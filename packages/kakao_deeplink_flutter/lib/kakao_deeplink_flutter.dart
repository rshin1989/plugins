import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

class KakaoDeeplinkFlutter {
  static const COMMAND_CHANNEL = 'io.flutter.plugins.kakao.deeplink/command';

  static const MethodChannel _methodChannel =
      const MethodChannel(COMMAND_CHANNEL);

  static Future<String?> get platformVersion async {
    final String? version =
        await _methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> isKaKaoNaviInstalled() async {
    return await _methodChannel.invokeMethod('isKaKaoNaviInstalled');
  }

  static Future<void> navigateTo({
    required String placeName,
    required String latitude,
    required String longitude,
  }) async {
    String argumentAsJson = jsonEncode({
      "placeName": placeName,
      "latitude": latitude,
      "longitude": longitude,
    });
    await _methodChannel.invokeMethod('navigateTo', argumentAsJson);
  }
}
