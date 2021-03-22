// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:mockito/mockito.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'package:kakao_maps_flutter_platform_interface/src/method_channel/method_channel_kakao_maps_flutter.dart';
import 'package:kakao_maps_flutter_platform_interface/kakao_maps_flutter_platform_interface.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('$KakaoMapsFlutterPlatform', () {
    test('$MethodChannelKakaoMapsFlutter() is the default instance', () {
      expect(KakaoMapsFlutterPlatform.instance,
          isInstanceOf<MethodChannelKakaoMapsFlutter>());
    });

    test('Cannot be implemented with `implements`', () {
      expect(() {
        KakaoMapsFlutterPlatform.instance =
            ImplementsKakaoMapsFlutterPlatform();
      }, throwsA(isInstanceOf<AssertionError>()));
    });

    test('Can be mocked with `implements`', () {
      final KakaoMapsFlutterPlatformMock mock =
      KakaoMapsFlutterPlatformMock();
      KakaoMapsFlutterPlatform.instance = mock;
    });

    test('Can be extended', () {
      KakaoMapsFlutterPlatform.instance = ExtendsKakaoMapsFlutterPlatform();
    });
  });

  group('$MethodChannelKakaoMapsFlutter', () {
    const MethodChannel channel =
    MethodChannel('plugins.clearmaps.io/kakao_maps_flutter');
    final List<MethodCall> log = <MethodCall>[];
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      log.add(methodCall);
    });

//    final MethodChannelKakaoMapsFlutter map = MethodChannelKakaoMapsFlutter(0);

    tearDown(() {
      log.clear();
    });

    test('foo', () async {
//      await map.foo();
      expect(
        log,
        <Matcher>[],
      );
    });
  });
}

class KakaoMapsFlutterPlatformMock extends Mock
    with MockPlatformInterfaceMixin
    implements KakaoMapsFlutterPlatform {}

class ImplementsKakaoMapsFlutterPlatform extends Mock
    implements KakaoMapsFlutterPlatform {}

class ExtendsKakaoMapsFlutterPlatform extends KakaoMapsFlutterPlatform {}