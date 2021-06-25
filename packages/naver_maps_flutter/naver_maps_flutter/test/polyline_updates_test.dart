// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:naver_maps_flutter/naver_maps_flutter.dart';

import 'fake_maps_controllers.dart';

Widget _mapWithPolylines(Set<Polyline> polylines) {
  return Directionality(
    textDirection: TextDirection.ltr,
    child: NaverMap(
      initialCameraPosition: const CameraPosition(target: LatLng(10.0, 15.0)),
      polylines: polylines,
    ),
  );
}

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

  testWidgets('Initializing a polyline', (WidgetTester tester) async {
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.polylinesToAdd.length, 1);

    final Polyline initializedPolyline = platformNaverMap.polylinesToAdd.first;
    expect(initializedPolyline, equals(p1));
    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);
    expect(platformNaverMap.polylinesToChange.isEmpty, true);
  });

  testWidgets("Adding a polyline", (WidgetTester tester) async {
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    final Polyline p2 = Polyline(polylineId: PolylineId("polyline_2"));

    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1, p2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.polylinesToAdd.length, 1);

    final Polyline addedPolyline = platformNaverMap.polylinesToAdd.first;
    expect(addedPolyline, equals(p2));

    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);

    expect(platformNaverMap.polylinesToChange.isEmpty, true);
  });

  testWidgets("Removing a polyline", (WidgetTester tester) async {
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));

    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.polylineIdsToRemove.length, 1);
    expect(platformNaverMap.polylineIdsToRemove.first, equals(p1.polylineId));

    expect(platformNaverMap.polylinesToChange.isEmpty, true);
    expect(platformNaverMap.polylinesToAdd.isEmpty, true);
  });

  testWidgets("Updating a polyline", (WidgetTester tester) async {
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    final Polyline p2 =
        Polyline(polylineId: PolylineId("polyline_1"), geodesic: true);

    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.polylinesToChange.length, 1);
    expect(platformNaverMap.polylinesToChange.first, equals(p2));

    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);
    expect(platformNaverMap.polylinesToAdd.isEmpty, true);
  });

  testWidgets("Updating a polyline", (WidgetTester tester) async {
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    final Polyline p2 =
        Polyline(polylineId: PolylineId("polyline_1"), geodesic: true);

    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.polylinesToChange.length, 1);

    final Polyline update = platformNaverMap.polylinesToChange.first;
    expect(update, equals(p2));
    expect(update.geodesic, true);
  });

  testWidgets("Mutate a polyline", (WidgetTester tester) async {
    final Polyline p1 = Polyline(
      polylineId: PolylineId("polyline_1"),
      points: <LatLng>[const LatLng(0.0, 0.0)],
    );
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));

    p1.points.add(const LatLng(1.0, 1.0));
    await tester.pumpWidget(_mapWithPolylines(<Polyline>{p1}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.polylinesToChange.length, 1);
    expect(platformNaverMap.polylinesToChange.first, equals(p1));

    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);
    expect(platformNaverMap.polylinesToAdd.isEmpty, true);
  });

  testWidgets("Multi Update", (WidgetTester tester) async {
    Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    Polyline p2 = Polyline(polylineId: PolylineId("polyline_2"));
    final Set<Polyline> prev = <Polyline>{p1, p2};
    p1 = Polyline(polylineId: PolylineId("polyline_1"), visible: false);
    p2 = Polyline(polylineId: PolylineId("polyline_2"), geodesic: true);
    final Set<Polyline> cur = <Polyline>{p1, p2};

    await tester.pumpWidget(_mapWithPolylines(prev));
    await tester.pumpWidget(_mapWithPolylines(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.polylinesToChange, cur);
    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);
    expect(platformNaverMap.polylinesToAdd.isEmpty, true);
  });

  testWidgets("Multi Update", (WidgetTester tester) async {
    Polyline p2 = Polyline(polylineId: PolylineId("polyline_2"));
    final Polyline p3 = Polyline(polylineId: PolylineId("polyline_3"));
    final Set<Polyline> prev = <Polyline>{p2, p3};

    // p1 is added, p2 is updated, p3 is removed.
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    p2 = Polyline(polylineId: PolylineId("polyline_2"), geodesic: true);
    final Set<Polyline> cur = <Polyline>{p1, p2};

    await tester.pumpWidget(_mapWithPolylines(prev));
    await tester.pumpWidget(_mapWithPolylines(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.polylinesToChange.length, 1);
    expect(platformNaverMap.polylinesToAdd.length, 1);
    expect(platformNaverMap.polylineIdsToRemove.length, 1);

    expect(platformNaverMap.polylinesToChange.first, equals(p2));
    expect(platformNaverMap.polylinesToAdd.first, equals(p1));
    expect(platformNaverMap.polylineIdsToRemove.first, equals(p3.polylineId));
  });

  testWidgets("Partial Update", (WidgetTester tester) async {
    final Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"));
    final Polyline p2 = Polyline(polylineId: PolylineId("polyline_2"));
    Polyline p3 = Polyline(polylineId: PolylineId("polyline_3"));
    final Set<Polyline> prev = <Polyline>{p1, p2, p3};
    p3 = Polyline(polylineId: PolylineId("polyline_3"), geodesic: true);
    final Set<Polyline> cur = <Polyline>{p1, p2, p3};

    await tester.pumpWidget(_mapWithPolylines(prev));
    await tester.pumpWidget(_mapWithPolylines(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.polylinesToChange, <Polyline>{p3});
    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);
    expect(platformNaverMap.polylinesToAdd.isEmpty, true);
  });

  testWidgets("Update non platform related attr", (WidgetTester tester) async {
    Polyline p1 = Polyline(polylineId: PolylineId("polyline_1"), onTap: null);
    final Set<Polyline> prev = <Polyline>{p1};
    p1 = Polyline(
        polylineId: PolylineId("polyline_1"), onTap: () => print(2 + 2));
    final Set<Polyline> cur = <Polyline>{p1};

    await tester.pumpWidget(_mapWithPolylines(prev));
    await tester.pumpWidget(_mapWithPolylines(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.polylinesToChange.isEmpty, true);
    expect(platformNaverMap.polylineIdsToRemove.isEmpty, true);
    expect(platformNaverMap.polylinesToAdd.isEmpty, true);
  });
}
