// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

part of naver_maps_flutter;

/// Controller for a single NaverMap instance running on the host platform.
class NaverMapController {
  /// The mapId for this controller
  final int mapId;

  NaverMapController._(
    CameraPosition initialCameraPosition,
    this._naverMapState, {
    required this.mapId,
  }) {
    _connectStreams(mapId);
  }

  /// Initialize control of a [NaverMap] with [id].
  ///
  /// Mainly for internal use when instantiating a [NaverMapController] passed
  /// in [NaverMap.onMapCreated] callback.
  static Future<NaverMapController> init(
    int id,
    CameraPosition initialCameraPosition,
    _NaverMapState NaverMapState,
  ) async {
    assert(id != null);
    await NaverMapsFlutterPlatform.instance.init(id);
    return NaverMapController._(
      initialCameraPosition,
      NaverMapState,
      mapId: id,
    );
  }

  /// Used to communicate with the native platform.
  ///
  /// Accessible only for testing.
  // TODO(dit) https://github.com/flutter/flutter/issues/55504 Remove this getter.
  @visibleForTesting
  MethodChannel? get channel {
    if (NaverMapsFlutterPlatform.instance is MethodChannelNaverMapsFlutter) {
      return (NaverMapsFlutterPlatform.instance
              as MethodChannelNaverMapsFlutter)
          .channel(mapId);
    }
    return null;
  }

  final _NaverMapState _naverMapState;

  void _connectStreams(int mapId) {
    if (_naverMapState.widget.onCameraMoveStarted != null) {
      NaverMapsFlutterPlatform.instance
          .onCameraMoveStarted(mapId: mapId)
          .listen((_) => _naverMapState.widget.onCameraMoveStarted!());
    }
    if (_naverMapState.widget.onCameraMove != null) {
      NaverMapsFlutterPlatform.instance.onCameraMove(mapId: mapId).listen(
          (CameraMoveEvent e) => _naverMapState.widget.onCameraMove!(e.value));
    }
    if (_naverMapState.widget.onCameraIdle != null) {
      NaverMapsFlutterPlatform.instance
          .onCameraIdle(mapId: mapId)
          .listen((_) => _naverMapState.widget.onCameraIdle!());
    }
    NaverMapsFlutterPlatform.instance
        .onMarkerTap(mapId: mapId)
        .listen((MarkerTapEvent e) => _naverMapState.onMarkerTap(e.value));
    NaverMapsFlutterPlatform.instance.onMarkerDragEnd(mapId: mapId).listen(
        (MarkerDragEndEvent e) =>
            _naverMapState.onMarkerDragEnd(e.value, e.position));
    NaverMapsFlutterPlatform.instance.onInfoWindowTap(mapId: mapId).listen(
        (InfoWindowTapEvent e) => _naverMapState.onInfoWindowTap(e.value));
    NaverMapsFlutterPlatform.instance
        .onPolylineTap(mapId: mapId)
        .listen((PolylineTapEvent e) => _naverMapState.onPolylineTap(e.value));
    NaverMapsFlutterPlatform.instance
        .onPolygonTap(mapId: mapId)
        .listen((PolygonTapEvent e) => _naverMapState.onPolygonTap(e.value));
    NaverMapsFlutterPlatform.instance
        .onCircleTap(mapId: mapId)
        .listen((CircleTapEvent e) => _naverMapState.onCircleTap(e.value));
    NaverMapsFlutterPlatform.instance
        .onTap(mapId: mapId)
        .listen((MapTapEvent e) => _naverMapState.onTap(e.position));
    NaverMapsFlutterPlatform.instance.onLongPress(mapId: mapId).listen(
        (MapLongPressEvent e) => _naverMapState.onLongPress(e.position));
  }

  /// Updates configuration options of the map user interface.
  ///
  /// Change listeners are notified once the update has been made on the
  /// platform side.
  ///
  /// The returned [Future] completes after listeners have been notified.
  Future<void> _updateMapOptions(Map<String, dynamic> optionsUpdate) {
    assert(optionsUpdate != null);
    return NaverMapsFlutterPlatform.instance
        .updateMapOptions(optionsUpdate, mapId: mapId);
  }

  /// Updates marker configuration.
  ///
  /// Change listeners are notified once the update has been made on the
  /// platform side.
  ///
  /// The returned [Future] completes after listeners have been notified.
  Future<void> _updateMarkers(MarkerUpdates markerUpdates) {
    assert(markerUpdates != null);
    return NaverMapsFlutterPlatform.instance
        .updateMarkers(markerUpdates, mapId: mapId);
  }

  /// Updates polygon configuration.
  ///
  /// Change listeners are notified once the update has been made on the
  /// platform side.
  ///
  /// The returned [Future] completes after listeners have been notified.
  Future<void> _updatePolygons(PolygonUpdates polygonUpdates) {
    assert(polygonUpdates != null);
    return NaverMapsFlutterPlatform.instance
        .updatePolygons(polygonUpdates, mapId: mapId);
  }

  /// Updates polyline configuration.
  ///
  /// Change listeners are notified once the update has been made on the
  /// platform side.
  ///
  /// The returned [Future] completes after listeners have been notified.
  Future<void> _updatePolylines(PolylineUpdates polylineUpdates) {
    assert(polylineUpdates != null);
    return NaverMapsFlutterPlatform.instance
        .updatePolylines(polylineUpdates, mapId: mapId);
  }

  /// Updates circle configuration.
  ///
  /// Change listeners are notified once the update has been made on the
  /// platform side.
  ///
  /// The returned [Future] completes after listeners have been notified.
  Future<void> _updateCircles(CircleUpdates circleUpdates) {
    assert(circleUpdates != null);
    return NaverMapsFlutterPlatform.instance
        .updateCircles(circleUpdates, mapId: mapId);
  }

  /// Updates tile overlays configuration.
  ///
  /// Change listeners are notified once the update has been made on the
  /// platform side.
  ///
  /// The returned [Future] completes after listeners have been notified.
  Future<void> _updateTileOverlays(Set<TileOverlay> newTileOverlays) {
    return NaverMapsFlutterPlatform.instance
        .updateTileOverlays(newTileOverlays: newTileOverlays, mapId: mapId);
  }

  /// Clears the tile cache so that all tiles will be requested again from the
  /// [TileProvider].
  ///
  /// The current tiles from this tile overlay will also be
  /// cleared from the map after calling this method. The API maintains a small
  /// in-memory cache of tiles. If you want to cache tiles for longer, you
  /// should implement an on-disk cache.
  Future<void> clearTileCache(TileOverlayId tileOverlayId) async {
    assert(tileOverlayId != null);
    return NaverMapsFlutterPlatform.instance
        .clearTileCache(tileOverlayId, mapId: mapId);
  }

  /// Starts an animated change of the map camera position.
  ///
  /// The returned [Future] completes after the change has been started on the
  /// platform side.
  Future<void> animateCamera(CameraUpdate cameraUpdate) {
    return NaverMapsFlutterPlatform.instance
        .animateCamera(cameraUpdate, mapId: mapId);
  }

  /// Changes the map camera position.
  ///
  /// The returned [Future] completes after the change has been made on the
  /// platform side.
  Future<void> moveCamera(CameraUpdate cameraUpdate) {
    return NaverMapsFlutterPlatform.instance
        .moveCamera(cameraUpdate, mapId: mapId);
  }

  /// Sets the styling of the base map.
  ///
  /// Set to `null` to clear any previous custom styling.
  ///
  /// If problems were detected with the [mapStyle], including un-parsable
  /// styling JSON, unrecognized feature type, unrecognized element type, or
  /// invalid styler keys: [MapStyleException] is thrown and the current
  /// style is left unchanged.
  ///
  /// The style string can be generated using [map style tool](https://mapstyle.withgoogle.com/).
  /// Also, refer [iOS](https://developers.google.com/maps/documentation/ios-sdk/style-reference)
  /// and [Android](https://developers.google.com/maps/documentation/android-sdk/style-reference)
  /// style reference for more information regarding the supported styles.
  Future<void> setMapStyle(String? mapStyle) {
    return NaverMapsFlutterPlatform.instance
        .setMapStyle(mapStyle, mapId: mapId);
  }

  /// Return [LatLngBounds] defining the region that is visible in a map.
  Future<LatLngBounds> getVisibleRegion() {
    return NaverMapsFlutterPlatform.instance.getVisibleRegion(mapId: mapId);
  }

  /// Return [ScreenCoordinate] of the [LatLng] in the current map view.
  ///
  /// A projection is used to translate between on screen location and geographic coordinates.
  /// Screen location is in screen pixels (not display pixels) with respect to the top left corner
  /// of the map, not necessarily of the whole screen.
  Future<ScreenCoordinate> getScreenCoordinate(LatLng latLng) {
    return NaverMapsFlutterPlatform.instance
        .getScreenCoordinate(latLng, mapId: mapId);
  }

  /// Returns [LatLng] corresponding to the [ScreenCoordinate] in the current map view.
  ///
  /// Returned [LatLng] corresponds to a screen location. The screen location is specified in screen
  /// pixels (not display pixels) relative to the top left of the map, not top left of the whole screen.
  Future<LatLng> getLatLng(ScreenCoordinate screenCoordinate) {
    return NaverMapsFlutterPlatform.instance
        .getLatLng(screenCoordinate, mapId: mapId);
  }

  /// Programmatically show the Info Window for a [Marker].
  ///
  /// The `markerId` must match one of the markers on the map.
  /// An invalid `markerId` triggers an "Invalid markerId" error.
  ///
  /// * See also:
  ///   * [hideMarkerInfoWindow] to hide the Info Window.
  ///   * [isMarkerInfoWindowShown] to check if the Info Window is showing.
  Future<void> showMarkerInfoWindow(MarkerId markerId) {
    assert(markerId != null);
    return NaverMapsFlutterPlatform.instance
        .showMarkerInfoWindow(markerId, mapId: mapId);
  }

  /// Programmatically hide the Info Window for a [Marker].
  ///
  /// The `markerId` must match one of the markers on the map.
  /// An invalid `markerId` triggers an "Invalid markerId" error.
  ///
  /// * See also:
  ///   * [showMarkerInfoWindow] to show the Info Window.
  ///   * [isMarkerInfoWindowShown] to check if the Info Window is showing.
  Future<void> hideMarkerInfoWindow(MarkerId markerId) {
    assert(markerId != null);
    return NaverMapsFlutterPlatform.instance
        .hideMarkerInfoWindow(markerId, mapId: mapId);
  }

  /// Returns `true` when the [InfoWindow] is showing, `false` otherwise.
  ///
  /// The `markerId` must match one of the markers on the map.
  /// An invalid `markerId` triggers an "Invalid markerId" error.
  ///
  /// * See also:
  ///   * [showMarkerInfoWindow] to show the Info Window.
  ///   * [hideMarkerInfoWindow] to hide the Info Window.
  Future<bool> isMarkerInfoWindowShown(MarkerId markerId) {
    assert(markerId != null);
    return NaverMapsFlutterPlatform.instance
        .isMarkerInfoWindowShown(markerId, mapId: mapId);
  }

  /// Returns the current zoom level of the map
  Future<double> getZoomLevel() {
    return NaverMapsFlutterPlatform.instance.getZoomLevel(mapId: mapId);
  }

  /// Returns the image bytes of the map
  Future<Uint8List?> takeSnapshot() {
    return NaverMapsFlutterPlatform.instance.takeSnapshot(mapId: mapId);
  }

  /// Disposes of the platform resources
  void dispose() {
    NaverMapsFlutterPlatform.instance.dispose(mapId: mapId);
  }
}
