
import 'dart:async';

import 'package:flutter/services.dart';

class KakaoDeeplinkFlutter {
  static const MethodChannel _channel =
      const MethodChannel('kakao_deeplink_flutter');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
