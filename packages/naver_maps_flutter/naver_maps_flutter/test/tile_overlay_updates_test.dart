// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:naver_maps_flutter/naver_maps_flutter.dart';

import 'fake_maps_controllers.dart';

Widget _mapWithTileOverlays(Set<TileOverlay> tileOverlays) {
  return Directionality(
    textDirection: TextDirection.ltr,
    child: NaverMap(
      initialCameraPosition: const CameraPosition(target: LatLng(10.0, 15.0)),
      tileOverlays: tileOverlays,
    ),
  );
}

void main() {
  final FakePlatformViewsController fakePlatformViewsController =
      FakePlatformViewsController();

  setUpAll(() {
    SystemChannels.platform_views.setMockMethodCallHandler(
        fakePlatformViewsController.fakePlatformViewsMethodHandler);
  });

  setUp(() {
    fakePlatformViewsController.reset();
  });

  testWidgets('Initializing a tile overlay', (WidgetTester tester) async {
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t1}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.tileOverlaysToAdd.length, 1);

    final TileOverlay initializedTileOverlay =
        platformNaverMap.tileOverlaysToAdd.first;
    expect(initializedTileOverlay, equals(t1));
    expect(platformNaverMap.tileOverlayIdsToRemove.isEmpty, true);
    expect(platformNaverMap.tileOverlaysToChange.isEmpty, true);
  });

  testWidgets("Adding a tile overlay", (WidgetTester tester) async {
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    final TileOverlay t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_2"));

    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t1}));
    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t1, t2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.tileOverlaysToAdd.length, 1);

    final TileOverlay addedTileOverlay =
        platformNaverMap.tileOverlaysToAdd.first;
    expect(addedTileOverlay, equals(t2));
    expect(platformNaverMap.tileOverlayIdsToRemove.isEmpty, true);

    expect(platformNaverMap.tileOverlaysToChange.isEmpty, true);
  });

  testWidgets("Removing a tile overlay", (WidgetTester tester) async {
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));

    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t1}));
    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.tileOverlayIdsToRemove.length, 1);
    expect(platformNaverMap.tileOverlayIdsToRemove.first,
        equals(t1.tileOverlayId));

    expect(platformNaverMap.tileOverlaysToChange.isEmpty, true);
    expect(platformNaverMap.tileOverlaysToAdd.isEmpty, true);
  });

  testWidgets("Updating a tile overlay", (WidgetTester tester) async {
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    final TileOverlay t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"), zIndex: 10);

    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t1}));
    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.tileOverlaysToChange.length, 1);
    expect(platformNaverMap.tileOverlaysToChange.first, equals(t2));

    expect(platformNaverMap.tileOverlayIdsToRemove.isEmpty, true);
    expect(platformNaverMap.tileOverlaysToAdd.isEmpty, true);
  });

  testWidgets("Updating a tile overlay", (WidgetTester tester) async {
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    final TileOverlay t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"), zIndex: 10);

    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t1}));
    await tester.pumpWidget(_mapWithTileOverlays(<TileOverlay>{t2}));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;
    expect(platformNaverMap.tileOverlaysToChange.length, 1);

    final TileOverlay update = platformNaverMap.tileOverlaysToChange.first;
    expect(update, equals(t2));
    expect(update.zIndex, 10);
  });

  testWidgets("Multi Update", (WidgetTester tester) async {
    TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    TileOverlay t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_2"));
    final Set<TileOverlay> prev = <TileOverlay>{t1, t2};
    t1 = TileOverlay(
        tileOverlayId: TileOverlayId("tile_overlay_1"), visible: false);
    t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_2"), zIndex: 10);
    final Set<TileOverlay> cur = <TileOverlay>{t1, t2};

    await tester.pumpWidget(_mapWithTileOverlays(prev));
    await tester.pumpWidget(_mapWithTileOverlays(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.tileOverlaysToChange, cur);
    expect(platformNaverMap.tileOverlayIdsToRemove.isEmpty, true);
    expect(platformNaverMap.tileOverlaysToAdd.isEmpty, true);
  });

  testWidgets("Multi Update", (WidgetTester tester) async {
    TileOverlay t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_2"));
    final TileOverlay t3 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_3"));
    final Set<TileOverlay> prev = <TileOverlay>{t2, t3};

    // t1 is added, t2 is updated, t3 is removed.
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_2"), zIndex: 10);
    final Set<TileOverlay> cur = <TileOverlay>{t1, t2};

    await tester.pumpWidget(_mapWithTileOverlays(prev));
    await tester.pumpWidget(_mapWithTileOverlays(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.tileOverlaysToChange.length, 1);
    expect(platformNaverMap.tileOverlaysToAdd.length, 1);
    expect(platformNaverMap.tileOverlayIdsToRemove.length, 1);

    expect(platformNaverMap.tileOverlaysToChange.first, equals(t2));
    expect(platformNaverMap.tileOverlaysToAdd.first, equals(t1));
    expect(platformNaverMap.tileOverlayIdsToRemove.first,
        equals(t3.tileOverlayId));
  });

  testWidgets("Partial Update", (WidgetTester tester) async {
    final TileOverlay t1 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_1"));
    final TileOverlay t2 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_2"));
    TileOverlay t3 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_3"));
    final Set<TileOverlay> prev = <TileOverlay>{t1, t2, t3};
    t3 =
        TileOverlay(tileOverlayId: TileOverlayId("tile_overlay_3"), zIndex: 10);
    final Set<TileOverlay> cur = <TileOverlay>{t1, t2, t3};

    await tester.pumpWidget(_mapWithTileOverlays(prev));
    await tester.pumpWidget(_mapWithTileOverlays(cur));

    final FakePlatformNaverMap platformNaverMap =
        fakePlatformViewsController.lastCreatedView!;

    expect(platformNaverMap.tileOverlaysToChange, <TileOverlay>{t3});
    expect(platformNaverMap.tileOverlayIdsToRemove.isEmpty, true);
    expect(platformNaverMap.tileOverlaysToAdd.isEmpty, true);
  });
}
