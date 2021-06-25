// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:naver_maps_flutter/naver_maps_flutter.dart';

import 'fake_maps_controllers.dart';

Widget _mapWithMarkers(Set<Marker> markers) {
  return Directionality(
    textDirection: TextDirection.ltr,
    child: NaverMap(
      initialCameraPosition: const CameraPosition(target: LatLng(10.0, 15.0)),
      markers: markers,
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

  testWidgets('Initializing a marker', (WidgetTester tester) async {
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));
    await tester.pumpWidget(_mapWithMarkers(<Marker>{m1}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.markersToAdd.length, 1);

    final Marker initializedMarker = platformNaverMap.markersToAdd.first;
    expect(initializedMarker, equals(m1));
    expect(platformNaverMap.markerIdsToRemove.isEmpty, true);
    expect(platformNaverMap.markersToChange.isEmpty, true);
  });

  testWidgets("Adding a marker", (WidgetTester tester) async {
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));
    final Marker m2 = Marker(markerId: MarkerId("marker_2"));

    await tester.pumpWidget(_mapWithMarkers(<Marker>{m1}));
    await tester.pumpWidget(_mapWithMarkers(<Marker>{m1, m2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.markersToAdd.length, 1);

    final Marker addedMarker = platformNaverMap.markersToAdd.first;
    expect(addedMarker, equals(m2));

    expect(platformNaverMap.markerIdsToRemove.isEmpty, true);

    expect(platformNaverMap.markersToChange.isEmpty, true);
  });

  testWidgets("Removing a marker", (WidgetTester tester) async {
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));

    await tester.pumpWidget(_mapWithMarkers(<Marker>{m1}));
    await tester.pumpWidget(_mapWithMarkers(<Marker>{}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.markerIdsToRemove.length, 1);
    expect(platformNaverMap.markerIdsToRemove.first, equals(m1.markerId));

    expect(platformNaverMap.markersToChange.isEmpty, true);
    expect(platformNaverMap.markersToAdd.isEmpty, true);
  });

  testWidgets("Updating a marker", (WidgetTester tester) async {
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));
    final Marker m2 = Marker(markerId: MarkerId("marker_1"), alpha: 0.5);

    await tester.pumpWidget(_mapWithMarkers(<Marker>{m1}));
    await tester.pumpWidget(_mapWithMarkers(<Marker>{m2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.markersToChange.length, 1);
    expect(platformNaverMap.markersToChange.first, equals(m2));

    expect(platformNaverMap.markerIdsToRemove.isEmpty, true);
    expect(platformNaverMap.markersToAdd.isEmpty, true);
  });

  testWidgets("Updating a marker", (WidgetTester tester) async {
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));
    final Marker m2 = Marker(
      markerId: MarkerId("marker_1"),
      infoWindow: const InfoWindow(snippet: 'changed'),
    );

    await tester.pumpWidget(_mapWithMarkers(<Marker>{m1}));
    await tester.pumpWidget(_mapWithMarkers(<Marker>{m2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.markersToChange.length, 1);

    final Marker update = platformNaverMap.markersToChange.first;
    expect(update, equals(m2));
    expect(update.infoWindow.snippet, 'changed');
  });

  testWidgets("Multi Update", (WidgetTester tester) async {
    Marker m1 = Marker(markerId: MarkerId("marker_1"));
    Marker m2 = Marker(markerId: MarkerId("marker_2"));
    final Set<Marker> prev = <Marker>{m1, m2};
    m1 = Marker(markerId: MarkerId("marker_1"), visible: false);
    m2 = Marker(markerId: MarkerId("marker_2"), draggable: true);
    final Set<Marker> cur = <Marker>{m1, m2};

    await tester.pumpWidget(_mapWithMarkers(prev));
    await tester.pumpWidget(_mapWithMarkers(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.markersToChange, cur);
    expect(platformNaverMap.markerIdsToRemove.isEmpty, true);
    expect(platformNaverMap.markersToAdd.isEmpty, true);
  });

  testWidgets("Multi Update", (WidgetTester tester) async {
    Marker m2 = Marker(markerId: MarkerId("marker_2"));
    final Marker m3 = Marker(markerId: MarkerId("marker_3"));
    final Set<Marker> prev = <Marker>{m2, m3};

    // m1 is added, m2 is updated, m3 is removed.
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));
    m2 = Marker(markerId: MarkerId("marker_2"), draggable: true);
    final Set<Marker> cur = <Marker>{m1, m2};

    await tester.pumpWidget(_mapWithMarkers(prev));
    await tester.pumpWidget(_mapWithMarkers(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.markersToChange.length, 1);
    expect(platformNaverMap.markersToAdd.length, 1);
    expect(platformNaverMap.markerIdsToRemove.length, 1);

    expect(platformNaverMap.markersToChange.first, equals(m2));
    expect(platformNaverMap.markersToAdd.first, equals(m1));
    expect(platformNaverMap.markerIdsToRemove.first, equals(m3.markerId));
  });

  testWidgets("Partial Update", (WidgetTester tester) async {
    final Marker m1 = Marker(markerId: MarkerId("marker_1"));
    final Marker m2 = Marker(markerId: MarkerId("marker_2"));
    Marker m3 = Marker(markerId: MarkerId("marker_3"));
    final Set<Marker> prev = <Marker>{m1, m2, m3};
    m3 = Marker(markerId: MarkerId("marker_3"), draggable: true);
    final Set<Marker> cur = <Marker>{m1, m2, m3};

    await tester.pumpWidget(_mapWithMarkers(prev));
    await tester.pumpWidget(_mapWithMarkers(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.markersToChange, <Marker>{m3});
    expect(platformNaverMap.markerIdsToRemove.isEmpty, true);
    expect(platformNaverMap.markersToAdd.isEmpty, true);
  });

  testWidgets("Update non platform related attr", (WidgetTester tester) async {
    Marker m1 = Marker(markerId: MarkerId("marker_1"));
    final Set<Marker> prev = <Marker>{m1};
    m1 = Marker(
        markerId: MarkerId("marker_1"),
        onTap: () => print("hello"),
        onDragEnd: (LatLng latLng) => print(latLng));
    final Set<Marker> cur = <Marker>{m1};

    await tester.pumpWidget(_mapWithMarkers(prev));
    await tester.pumpWidget(_mapWithMarkers(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.markersToChange.isEmpty, true);
    expect(platformNaverMap.markerIdsToRemove.isEmpty, true);
    expect(platformNaverMap.markersToAdd.isEmpty, true);
  });
}
