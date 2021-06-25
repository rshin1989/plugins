// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:naver_maps_flutter/naver_maps_flutter.dart';

import 'fake_maps_controllers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final FakePlatformViewsController fakePlatformViewsController =
      FakePlatformViewsController();

  setUpAll(() {
    SystemChannels.platform_views.setMockMethodCallHandler(
        fakePlatformViewsController.fakePlatformViewsMethodHandler);
  });

  setUp(() {
    fakePlatformViewsController.reset();
  });

  testWidgets('Initial camera position', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.cameraPosition,
        const CameraPosition(target: LatLng(10.0, 15.0)));
  });

  testWidgets('Initial camera position change is a no-op',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
        ),
      ),
    );

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 16.0)),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.cameraPosition,
        const CameraPosition(target: LatLng(10.0, 15.0)));
  });

  testWidgets('Can update compassEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          compassEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.compassEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          compassEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.compassEnabled, true);
  });

  testWidgets('Can update mapToolbarEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          mapToolbarEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.mapToolbarEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          mapToolbarEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.mapToolbarEnabled, true);
  });

  testWidgets('Can update cameraTargetBounds', (WidgetTester tester) async {
    await tester.pumpWidget(
      Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition:
              const CameraPosition(target: LatLng(10.0, 15.0)),
          cameraTargetBounds: CameraTargetBounds(
            LatLngBounds(
              southwest: const LatLng(10.0, 20.0),
              northeast: const LatLng(30.0, 40.0),
            ),
          ),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(
        platformNaverMap.cameraTargetBounds,
        CameraTargetBounds(
          LatLngBounds(
            southwest: const LatLng(10.0, 20.0),
            northeast: const LatLng(30.0, 40.0),
          ),
        ));

    await tester.pumpWidget(
      Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition:
              const CameraPosition(target: LatLng(10.0, 15.0)),
          cameraTargetBounds: CameraTargetBounds(
            LatLngBounds(
              southwest: const LatLng(16.0, 20.0),
              northeast: const LatLng(30.0, 40.0),
            ),
          ),
        ),
      ),
    );

    expect(
        platformNaverMap.cameraTargetBounds,
        CameraTargetBounds(
          LatLngBounds(
            southwest: const LatLng(16.0, 20.0),
            northeast: const LatLng(30.0, 40.0),
          ),
        ));
  });

  testWidgets('Can update mapType', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          mapType: MapType.hybrid,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.mapType, MapType.hybrid);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          mapType: MapType.satellite,
        ),
      ),
    );

    expect(platformNaverMap.mapType, MapType.satellite);
  });

  testWidgets('Can update minMaxZoom', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          minMaxZoomPreference: MinMaxZoomPreference(1.0, 3.0),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.minMaxZoomPreference,
        const MinMaxZoomPreference(1.0, 3.0));

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          minMaxZoomPreference: MinMaxZoomPreference.unbounded,
        ),
      ),
    );

    expect(
        platformNaverMap.minMaxZoomPreference, MinMaxZoomPreference.unbounded);
  });

  testWidgets('Can update rotateGesturesEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          rotateGesturesEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.rotateGesturesEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          rotateGesturesEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.rotateGesturesEnabled, true);
  });

  testWidgets('Can update scrollGesturesEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          scrollGesturesEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.scrollGesturesEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          scrollGesturesEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.scrollGesturesEnabled, true);
  });

  testWidgets('Can update tiltGesturesEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          tiltGesturesEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.tiltGesturesEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          tiltGesturesEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.tiltGesturesEnabled, true);
  });

  testWidgets('Can update trackCameraPosition', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.trackCameraPosition, false);

    await tester.pumpWidget(
      Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition:
              const CameraPosition(target: LatLng(10.0, 15.0)),
          onCameraMove: (CameraPosition position) {},
        ),
      ),
    );

    expect(platformNaverMap.trackCameraPosition, true);
  });

  testWidgets('Can update zoomGesturesEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          zoomGesturesEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.zoomGesturesEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          zoomGesturesEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.zoomGesturesEnabled, true);
  });

  testWidgets('Can update zoomControlsEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          zoomControlsEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.zoomControlsEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          zoomControlsEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.zoomControlsEnabled, true);
  });

  testWidgets('Can update myLocationEnabled', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          myLocationEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.myLocationEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          myLocationEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.myLocationEnabled, true);
  });

  testWidgets('Can update myLocationButtonEnabled',
      (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          myLocationEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.myLocationButtonEnabled, true);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          myLocationButtonEnabled: false,
        ),
      ),
    );

    expect(platformNaverMap.myLocationButtonEnabled, false);
  });

  testWidgets('Is default padding 0', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.padding, <double>[0, 0, 0, 0]);
  });

  testWidgets('Can update padding', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.padding, <double>[0, 0, 0, 0]);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          padding: EdgeInsets.fromLTRB(10, 20, 30, 40),
        ),
      ),
    );

    expect(platformNaverMap.padding, <double>[20, 10, 40, 30]);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          padding: EdgeInsets.fromLTRB(50, 60, 70, 80),
        ),
      ),
    );

    expect(platformNaverMap.padding, <double>[60, 50, 80, 70]);
  });

  testWidgets('Can update traffic', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          trafficEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.trafficEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          trafficEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.trafficEnabled, true);
  });

  testWidgets('Can update buildings', (WidgetTester tester) async {
    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          buildingsEnabled: false,
        ),
      ),
    );

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.buildingsEnabled, false);

    await tester.pumpWidget(
      const Directionality(
        textDirection: TextDirection.ltr,
        child: NaverMap(
          initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          buildingsEnabled: true,
        ),
      ),
    );

    expect(platformNaverMap.buildingsEnabled, true);
  });

  testWidgets(
    'Default Android widget is AndroidView',
    (WidgetTester tester) async {
      await tester.pumpWidget(
        const Directionality(
          textDirection: TextDirection.ltr,
          child: NaverMap(
            initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
          ),
        ),
      );

      expect(find.byType(AndroidView), findsOneWidget);
    },
  );

  // TODO(bparrishMines): Uncomment once https://github.com/flutter/plugins/pull/4017 has landed.
  // testWidgets('Use AndroidViewSurface on Android', (WidgetTester tester) async {
  //   await tester.pumpWidget(
  //     const Directionality(
  //       textDirection: TextDirection.ltr,
  //       child: NaverMap(
  //         initialCameraPosition: CameraPosition(target: LatLng(10.0, 15.0)),
  //       ),
  //     ),
  //   );
  //
  //   expect(find.byType(AndroidViewSurface), findsOneWidget);
  // });
}
