import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:kakao_maps_flutter/kakao_maps_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel('kakao_maps_flutter');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await KakaoMapsFlutter.platformVersion, '42');
  });
}
