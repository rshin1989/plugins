// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.kakaomaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.naver.maps.MapWrapperListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.KakaoMap;
import com.naver.maps.map.KakaoMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Symbol;
import com.naver.maps.map.indoor.IndoorSelection;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.model.Circle;
import com.naver.maps.model.Polygon;
import com.naver.maps.model.Polyline;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Controller of a single KakaoMaps MapView instance. */
final class KakaoMapController
    implements DefaultLifecycleObserver,
        ActivityPluginBinding.OnSaveInstanceStateListener,
        KakaoMapOptionsSink,
        MethodChannel.MethodCallHandler,
        OnMapReadyCallback,
        KakaoMapListener,
        MapWrapperListener,
        PlatformView {

  private static final String TAG = "KakaoMapController";
  private final int id;
  private final MethodChannel methodChannel;
  private final KakaoMapOptions options;
  @Nullable private MapView mapView;
  private KakaoMap kakaoMap;
  private boolean trackCameraPosition = false;
  private boolean myLocationEnabled = false;
  private boolean myLocationButtonEnabled = false;
  private boolean zoomControlsEnabled = true;
  private boolean indoorEnabled = true;
  private boolean trafficEnabled = false;
  private boolean buildingsEnabled = true;
  private boolean disposed = false;
  private final float density;
  private MethodChannel.Result mapReadyResult;
  private final Context context;
  private final LifecycleProvider lifecycleProvider;
  private final MarkersController markersController;
  private final PolygonsController polygonsController;
  private final PolylinesController polylinesController;
  private final CirclesController circlesController;
  private final TileOverlaysController tileOverlaysController;
  private List<Object> initialMarkers;
  private List<Object> initialPolygons;
  private List<Object> initialPolylines;
  private List<Object> initialCircles;
  private List<Map<String, ?>> initialTileOverlays;

  KakaoMapController(
      int id,
      Context context,
      BinaryMessenger binaryMessenger,
      LifecycleProvider lifecycleProvider,
      KakaoMapOptions options) {
    this.id = id;
    this.context = context;
    this.options = options;
    this.mapView = new MapView(context, options);
    this.density = context.getResources().getDisplayMetrics().density;
    methodChannel = new MethodChannel(binaryMessenger, "plugins.flutter.io/kakao_maps_" + id);
    methodChannel.setMethodCallHandler(this);
    this.lifecycleProvider = lifecycleProvider;
    this.markersController = new MarkersController(methodChannel);
    this.polygonsController = new PolygonsController(methodChannel, density);
    this.polylinesController = new PolylinesController(methodChannel, density);
    this.circlesController = new CirclesController(methodChannel, density);
    this.tileOverlaysController = new TileOverlaysController(methodChannel);
  }

  @Override
  public View getView() {
    return mapView;
  }

  void init() {
    lifecycleProvider.getLifecycle().addObserver(this);
    mapView.getMapAsync(this);
  }

  private void moveCamera(CameraUpdate cameraUpdate) {
    kakaoMap.moveCamera(cameraUpdate);
  }

  private void animateCamera(CameraUpdate cameraUpdate) {
    cameraUpdate.animate(CameraAnimation.Easing);
    kakaoMap.moveCamera(cameraUpdate);
  }

  private CameraPosition getCameraPosition() {
    return trackCameraPosition ? kakaoMap.getCameraPosition() : null;
  }

  @Override
  public void onMapReady(KakaoMap kakaoMap) {
    this.kakaoMap = kakaoMap;
    this.kakaoMap.setIndoorEnabled(this.indoorEnabled);
    this.kakaoMap.setLayerGroupEnabled(KakaoMap.LAYER_GROUP_TRAFFIC, this.trafficEnabled);
    this.kakaoMap.setLayerGroupEnabled(KakaoMap.LAYER_GROUP_BUILDING, this.buildingsEnabled);
    //TODO("마커의 정보창을 출력해주는 형태만 지원한다")
    //kakaoMap.setOnInfoWindowClickListener(this);
    if (mapReadyResult != null) {
      mapReadyResult.success(null);
      mapReadyResult = null;
    }
    setKakaoMapListener(this);
    updateMyLocationSettings();
    markersController.setKakaoMap(kakaoMap);
    polygonsController.setKakaoMap(kakaoMap);
    polylinesController.setKakaoMap(kakaoMap);
    circlesController.setKakaoMap(kakaoMap);
    tileOverlaysController.setKakaoMap(kakaoMap);
    updateInitialMarkers();
    updateInitialPolygons();
    updateInitialPolylines();
    updateInitialCircles();
    updateInitialTileOverlays();
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    Log.d("KakaoMapController", "[onMethodCall] ----------- " + call.method);
    switch (call.method) {
      case "map#waitForMap":
        if (kakaoMap != null) {
          result.success(null);
          return;
        }
        mapReadyResult = result;
        break;
      case "map#update":
        {
          Convert.interpretKakaoMapOptions(call.argument("options"), this);
          result.success(Convert.cameraPositionToJson(getCameraPosition()));
          break;
        }
      case "map#getVisibleRegion":
        {
          if (kakaoMap != null) {
              //TODO("map#getVisibleRegion")
//            LatLngBounds latLngBounds = kakaoMap.getProjection().getVisibleRegion().latLngBounds;
//            result.success(Convert.latlngBoundsToJson(latLngBounds));
              result.success(null);
          } else {
            result.error(
                "KakaoMap uninitialized",
                "getVisibleRegion called prior to map initialization",
                null);
          }
          break;
        }
      case "map#getScreenCoordinate":
        {
          if (kakaoMap != null) {
            LatLng latLng = Convert.toLatLng(call.arguments);
            PointF screenLocation = kakaoMap.getProjection().toScreenLocation(latLng);
            result.success(Convert.pointToJson(screenLocation));
          } else {
            result.error(
                "KakaoMap uninitialized",
                "getScreenCoordinate called prior to map initialization",
                null);
          }
          break;
        }
      case "map#getLatLng":
        {
          if (kakaoMap != null) {
            PointF point = Convert.toPoint(call.arguments);
            LatLng latLng = kakaoMap.getProjection().fromScreenLocation(point);
            result.success(Convert.latLngToJson(latLng));
          } else {
            result.error(
                "KakaoMap uninitialized", "getLatLng called prior to map initialization", null);
          }
          break;
        }
      case "map#takeSnapshot":
        {
          if (kakaoMap != null) {
            final MethodChannel.Result _result = result;
            kakaoMap.takeSnapshot(
                new KakaoMap.SnapshotReadyCallback() {
                  @Override
                  public void onSnapshotReady(Bitmap bitmap) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    //TODO("onSnapshotReady")

//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    bitmap.recycle();
                    _result.success(byteArray);
                  }
                });
          } else {
            result.error("KakaoMap uninitialized", "takeSnapshot", null);
          }
          break;
        }
      case "camera#move":
        {
          final CameraUpdate cameraUpdate =
              Convert.toCameraUpdate(call.argument("cameraUpdate"), density);
          moveCamera(cameraUpdate);
          result.success(null);
          break;
        }
      case "camera#animate":
        {
          final CameraUpdate cameraUpdate =
              Convert.toCameraUpdate(call.argument("cameraUpdate"), density);
          animateCamera(cameraUpdate);
          result.success(null);
          break;
        }
      case "markers#update":
        {
          List<Object> markersToAdd = call.argument("markersToAdd");
          markersController.addMarkers(markersToAdd);
          List<Object> markersToChange = call.argument("markersToChange");
          markersController.changeMarkers(markersToChange);
          List<Object> markerIdsToRemove = call.argument("markerIdsToRemove");
          markersController.removeMarkers(markerIdsToRemove);
          result.success(null);
          break;
        }
      case "markers#showInfoWindow":
        {
          Object markerId = call.argument("markerId");
          markersController.showMarkerInfoWindow((String) markerId, result);
          break;
        }
      case "markers#hideInfoWindow":
        {
          Object markerId = call.argument("markerId");
          markersController.hideMarkerInfoWindow((String) markerId, result);
          break;
        }
      case "markers#isInfoWindowShown":
        {
          Object markerId = call.argument("markerId");
          markersController.isInfoWindowShown((String) markerId, result);
          break;
        }
      case "polygons#update":
        {
          List<Object> polygonsToAdd = call.argument("polygonsToAdd");
          polygonsController.addPolygons(polygonsToAdd);
          List<Object> polygonsToChange = call.argument("polygonsToChange");
          polygonsController.changePolygons(polygonsToChange);
          List<Object> polygonIdsToRemove = call.argument("polygonIdsToRemove");
          polygonsController.removePolygons(polygonIdsToRemove);
          result.success(null);
          break;
        }
      case "polylines#update":
        {
          List<Object> polylinesToAdd = call.argument("polylinesToAdd");
          polylinesController.addPolylines(polylinesToAdd);
          List<Object> polylinesToChange = call.argument("polylinesToChange");
          polylinesController.changePolylines(polylinesToChange);
          List<Object> polylineIdsToRemove = call.argument("polylineIdsToRemove");
          polylinesController.removePolylines(polylineIdsToRemove);
          result.success(null);
          break;
        }
      case "circles#update":
        {
          List<Object> circlesToAdd = call.argument("circlesToAdd");
          circlesController.addCircles(circlesToAdd);
          List<Object> circlesToChange = call.argument("circlesToChange");
          circlesController.changeCircles(circlesToChange);
          List<Object> circleIdsToRemove = call.argument("circleIdsToRemove");
          circlesController.removeCircles(circleIdsToRemove);
          result.success(null);
          break;
        }
      case "map#isCompassEnabled":
        {
          result.success(kakaoMap.getUiSettings().isCompassEnabled());
          break;
        }
      case "map#isMapToolbarEnabled":
        {
            //TODO("map#isMapToolbarEnabled")
//          result.success(kakaoMap.getUiSettings().isMapToolbarEnabled());
          break;
        }
      case "map#getMinMaxZoomLevels":
        {
          List<Double> zoomLevels = new ArrayList<>(2);
          zoomLevels.add(kakaoMap.getMinZoom());
          zoomLevels.add(kakaoMap.getMaxZoom());
          result.success(zoomLevels);
          break;
        }
      case "map#isZoomGesturesEnabled":
        {
          result.success(kakaoMap.getUiSettings().isZoomGesturesEnabled());
          break;
        }
      case "map#isLiteModeEnabled":
        {
          result.success(options.isLiteModeEnabled());
          break;
        }
      case "map#isZoomControlsEnabled":
        {
          result.success(kakaoMap.getUiSettings().isZoomControlEnabled());
          break;
        }
      case "map#isScrollGesturesEnabled":
        {
          result.success(kakaoMap.getUiSettings().isScrollGesturesEnabled());
          break;
        }
      case "map#isTiltGesturesEnabled":
        {
          result.success(kakaoMap.getUiSettings().isTiltGesturesEnabled());
          break;
        }
      case "map#isRotateGesturesEnabled":
        {
          result.success(kakaoMap.getUiSettings().isRotateGesturesEnabled());
          break;
        }
      case "map#isMyLocationButtonEnabled":
        {
          result.success(kakaoMap.getUiSettings().isLocationButtonEnabled());
          break;
        }
      case "map#isTrafficEnabled":
        {
          result.success(kakaoMap.isLayerGroupEnabled(KakaoMap.LAYER_GROUP_TRAFFIC));
          break;
        }
      case "map#isBuildingsEnabled":
        {
          result.success(kakaoMap.isLayerGroupEnabled(KakaoMap.LAYER_GROUP_BUILDING));
          break;
        }
      case "map#getZoomLevel":
        {
          result.success(kakaoMap.getCameraPosition().zoom);
          break;
        }
      case "map#setStyle":
        {
          //TODO("map#setStyle")
//          String mapStyle = (String) call.arguments;
//          boolean mapStyleSet;
//          if (mapStyle == null) {
//            mapStyleSet = kakaoMap.setMapStyle(null);
//          } else {
//            mapStyleSet = kakaoMap.setMapStyle(new MapStyleOptions(mapStyle));
//          }
//          ArrayList<Object> mapStyleResult = new ArrayList<>(2);
//          mapStyleResult.add(mapStyleSet);
//          if (!mapStyleSet) {
//            mapStyleResult.add(
//                "Unable to set the map style. Please check console logs for errors.");
//          }
//          result.success(mapStyleResult);
          result.success(null);
          break;
        }
      case "tileOverlays#update":
        {
          List<Map<String, ?>> tileOverlaysToAdd = call.argument("tileOverlaysToAdd");
          tileOverlaysController.addTileOverlays(tileOverlaysToAdd);
          List<Map<String, ?>> tileOverlaysToChange = call.argument("tileOverlaysToChange");
          tileOverlaysController.changeTileOverlays(tileOverlaysToChange);
          List<String> tileOverlaysToRemove = call.argument("tileOverlayIdsToRemove");
          tileOverlaysController.removeTileOverlays(tileOverlaysToRemove);
          result.success(null);
          break;
        }
      case "tileOverlays#clearTileCache":
        {
          String tileOverlayId = call.argument("tileOverlayId");
          tileOverlaysController.clearTileCache(tileOverlayId);
          result.success(null);
          break;
        }
      case "map#getTileOverlayInfo":
        {
          String tileOverlayId = call.argument("tileOverlayId");
          result.success(tileOverlaysController.getTileOverlayInfo(tileOverlayId));
          break;
        }
      default:
        result.notImplemented();
    }
  }

  @Override
  public void onMapClick(LatLng latLng) {
    final Map<String, Object> arguments = new HashMap<>(2);
    arguments.put("position", Convert.latLngToJson(latLng));
    methodChannel.invokeMethod("map#onTap", arguments);
  }

  @Override
  public void onMapLongClick(LatLng latLng) {
    final Map<String, Object> arguments = new HashMap<>(2);
    arguments.put("position", Convert.latLngToJson(latLng));
    methodChannel.invokeMethod("map#onLongPress", arguments);
  }

  @Override
  public void onCameraMoveStarted(int reason) {
    final Map<String, Object> arguments = new HashMap<>(2);
    boolean isGesture = reason == CameraUpdate.REASON_GESTURE;
    arguments.put("isGesture", isGesture);
    methodChannel.invokeMethod("camera#onMoveStarted", arguments);
  }

  @Override
  public void onInfoWindowClick(Marker marker) {
    markersController.onInfoWindowTap(Integer.toString(marker.hashCode()));
  }

  @Override
  public void onCameraMove() {
    if (!trackCameraPosition) {
      return;
    }
    final Map<String, Object> arguments = new HashMap<>(2);
    arguments.put("position", Convert.cameraPositionToJson(kakaoMap.getCameraPosition()));
    methodChannel.invokeMethod("camera#onMove", arguments);
  }

  @Override
  public void onCameraIdle() {
    methodChannel.invokeMethod("camera#onIdle", Collections.singletonMap("map", id));
  }

  @Override
  public boolean onMarkerClick(Marker marker) {
    return markersController.onMarkerTap(Integer.toString(marker.hashCode()));
  }

  @Override
  public void onMarkerDragStart(Marker marker) {}

  @Override
  public void onMarkerDrag(Marker marker) {}

  @Override
  public void onMarkerDragEnd(Marker marker) {
    markersController.onMarkerDragEnd(Integer.toString(marker.hashCode()), marker.getPosition());
  }

  @Override
  public void onPolygonClick(Polygon polygon) {
    polygonsController.onPolygonTap(polygon.getId());
  }

  @Override
  public void onPolylineClick(Polyline polyline) {
    polylinesController.onPolylineTap(polyline.getId());
  }

  @Override
  public void onCircleClick(Circle circle) {
    circlesController.onCircleTap(circle.getId());
  }

  @Override
  public void dispose() {
    if (disposed) {
      return;
    }
    disposed = true;
    methodChannel.setMethodCallHandler(null);
    setKakaoMapListener(null);
    destroyMapViewIfNecessary();
    Lifecycle lifecycle = lifecycleProvider.getLifecycle();
    if (lifecycle != null) {
      lifecycle.removeObserver(this);
    }
  }

  private void setKakaoMapListener(@Nullable KakaoMapListener listener) {
    if (listener == null) {
      kakaoMap.removeOnCameraChangeListener(this);
      kakaoMap.removeOnCameraIdleListener(this);
      kakaoMap.removeOnIndoorSelectionChangeListener(this);
      kakaoMap.removeOnLocationChangeListener(this);
      kakaoMap.removeOnOptionChangeListener(this);
      kakaoMap.setOnMapClickListener(null);
      kakaoMap.setOnMapDoubleTapListener(null);
      kakaoMap.setOnMapLongClickListener(null);
      kakaoMap.setOnMapTwoFingerTapListener(null);
      kakaoMap.setOnSymbolClickListener(null);
      return;
    }
    kakaoMap.addOnCameraChangeListener(this);
    kakaoMap.addOnCameraIdleListener(this);
    kakaoMap.addOnIndoorSelectionChangeListener(this);
    kakaoMap.addOnLocationChangeListener(this);
    kakaoMap.addOnOptionChangeListener(this);
    kakaoMap.setOnMapClickListener(listener);
    kakaoMap.setOnMapDoubleTapListener(listener);
    kakaoMap.setOnMapLongClickListener(listener);
    kakaoMap.setOnMapTwoFingerTapListener(listener);
    kakaoMap.setOnSymbolClickListener(listener);
  }

  // @Override
  // The minimum supported version of Flutter doesn't have this method on the PlatformView interface, but the maximum
  // does. This will override it when available even with the annotation commented out.
  public void onInputConnectionLocked() {
    // TODO(mklim): Remove this empty override once https://github.com/flutter/flutter/issues/40126 is fixed in stable.
  }

  // @Override
  // The minimum supported version of Flutter doesn't have this method on the PlatformView interface, but the maximum
  // does. This will override it when available even with the annotation commented out.
  public void onInputConnectionUnlocked() {
    // TODO(mklim): Remove this empty override once https://github.com/flutter/flutter/issues/40126 is fixed in stable.
  }

  // DefaultLifecycleObserver

  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    if (disposed) {
      return;
    }
    mapView.onCreate(null);
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    if (disposed) {
      return;
    }
    mapView.onStart();
  }

  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    if (disposed) {
      return;
    }
    mapView.onResume();
  }

  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    if (disposed) {
      return;
    }
    mapView.onResume();
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    if (disposed) {
      return;
    }
    mapView.onStop();
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    owner.getLifecycle().removeObserver(this);
    if (disposed) {
      return;
    }
    destroyMapViewIfNecessary();
  }

  @Override
  public void onRestoreInstanceState(Bundle bundle) {
    if (disposed) {
      return;
    }
    mapView.onCreate(bundle);
  }

  @Override
  public void onSaveInstanceState(Bundle bundle) {
    if (disposed) {
      return;
    }
    mapView.onSaveInstanceState(bundle);
  }

  // KakaoMapOptionsSink methods

  @Override
  public void setCameraTargetBounds(LatLngBounds bounds) {
    //TODO("setCameraTargetBounds")
//    kakaoMap.setLatLngBoundsForCameraTarget(bounds);
  }

  @Override
  public void setCompassEnabled(boolean compassEnabled) {
    kakaoMap.getUiSettings().setCompassEnabled(compassEnabled);
  }

  @Override
  public void setMapToolbarEnabled(boolean mapToolbarEnabled) {
    //TODO("setMapToolbarEnabled")
//    kakaoMap.getUiSettings().setMapToolbarEnabled(mapToolbarEnabled);
  }

  @Override
  public void setMapType(int mapType) {
    KakaoMap.MapType kakaoMapType;
    switch (mapType) {
      case 0:
        kakaoMapType = KakaoMap.MapType.Basic;
        break;
      case 1:
        kakaoMapType = KakaoMap.MapType.Navi;
        break;
      case 2:
        kakaoMapType = KakaoMap.MapType.Satellite;
        break;
      case 3:
        kakaoMapType = KakaoMap.MapType.Hybrid;
        break;
      case 4:
        kakaoMapType = KakaoMap.MapType.Terrain;
        break;
      default:
        kakaoMapType = KakaoMap.MapType.None;
        break;
    }
    kakaoMap.setMapType(kakaoMapType);
  }

  @Override
  public void setTrackCameraPosition(boolean trackCameraPosition) {
    this.trackCameraPosition = trackCameraPosition;
  }

  @Override
  public void setRotateGesturesEnabled(boolean rotateGesturesEnabled) {
    kakaoMap.getUiSettings().setRotateGesturesEnabled(rotateGesturesEnabled);
  }

  @Override
  public void setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
    kakaoMap.getUiSettings().setScrollGesturesEnabled(scrollGesturesEnabled);
  }

  @Override
  public void setTiltGesturesEnabled(boolean tiltGesturesEnabled) {
    kakaoMap.getUiSettings().setTiltGesturesEnabled(tiltGesturesEnabled);
  }

  @Override
  public void setMinMaxZoomPreference(Float min, Float max) {
    if (min != null) {
      kakaoMap.setMinZoom(min);
    }
    if (max != null) {
      kakaoMap.setMaxZoom(max);
    }
  }

  @Override
  public void setPadding(float top, float left, float bottom, float right) {
    if (kakaoMap != null) {
      kakaoMap.setContentPadding(
          (int) (left * density),
          (int) (top * density),
          (int) (right * density),
          (int) (bottom * density));
    }
  }

  @Override
  public void setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
    kakaoMap.getUiSettings().setZoomGesturesEnabled(zoomGesturesEnabled);
  }

  /** This call will have no effect on already created map */
  @Override
  public void setLiteModeEnabled(boolean liteModeEnabled) {
    options.liteModeEnabled(liteModeEnabled);
  }

  @Override
  public void setMyLocationEnabled(boolean myLocationEnabled) {
    if (this.myLocationEnabled == myLocationEnabled) {
      return;
    }
    this.myLocationEnabled = myLocationEnabled;
    if (kakaoMap != null) {
      updateMyLocationSettings();
    }
  }

  @Override
  public void setMyLocationButtonEnabled(boolean myLocationButtonEnabled) {
    if (this.myLocationButtonEnabled == myLocationButtonEnabled) {
      return;
    }
    this.myLocationButtonEnabled = myLocationButtonEnabled;
    if (kakaoMap != null) {
      updateMyLocationSettings();
    }
  }

  @Override
  public void setZoomControlsEnabled(boolean zoomControlsEnabled) {
    if (this.zoomControlsEnabled == zoomControlsEnabled) {
      return;
    }
    this.zoomControlsEnabled = zoomControlsEnabled;
    if (kakaoMap != null) {
      kakaoMap.getUiSettings().setZoomControlEnabled(zoomControlsEnabled);
    }
  }

  @Override
  public void setInitialMarkers(Object initialMarkers) {
    ArrayList<?> markers = (ArrayList<?>) initialMarkers;
    this.initialMarkers = markers != null ? new ArrayList<>(markers) : null;
    if (kakaoMap != null) {
      updateInitialMarkers();
    }
  }

  private void updateInitialMarkers() {
    markersController.addMarkers(initialMarkers);
  }

  @Override
  public void setInitialPolygons(Object initialPolygons) {
    ArrayList<?> polygons = (ArrayList<?>) initialPolygons;
    this.initialPolygons = polygons != null ? new ArrayList<>(polygons) : null;
    if (kakaoMap != null) {
      updateInitialPolygons();
    }
  }

  private void updateInitialPolygons() {
    polygonsController.addPolygons(initialPolygons);
  }

  @Override
  public void setInitialPolylines(Object initialPolylines) {
    ArrayList<?> polylines = (ArrayList<?>) initialPolylines;
    this.initialPolylines = polylines != null ? new ArrayList<>(polylines) : null;
    if (kakaoMap != null) {
      updateInitialPolylines();
    }
  }

  private void updateInitialPolylines() {
    polylinesController.addPolylines(initialPolylines);
  }

  @Override
  public void setInitialCircles(Object initialCircles) {
    ArrayList<?> circles = (ArrayList<?>) initialCircles;
    this.initialCircles = circles != null ? new ArrayList<>(circles) : null;
    if (kakaoMap != null) {
      updateInitialCircles();
    }
  }

  private void updateInitialCircles() {
    circlesController.addCircles(initialCircles);
  }

  @Override
  public void setInitialTileOverlays(List<Map<String, ?>> initialTileOverlays) {
    this.initialTileOverlays = initialTileOverlays;
    if (kakaoMap != null) {
      updateInitialTileOverlays();
    }
  }

  private void updateInitialTileOverlays() {
    tileOverlaysController.addTileOverlays(initialTileOverlays);
  }

  @SuppressLint("MissingPermission")
  private void updateMyLocationSettings() {
    if (hasLocationPermission()) {
      // The plugin doesn't add the location permission by default so that apps that don't need
      // the feature won't require the permission.
      // Gradle is doing a static check for missing permission and in some configurations will
      // fail the build if the permission is missing. The following disables the Gradle lint.
      //noinspection ResourceType
//      kakaoMap.setMyLocationEnabled(myLocationEnabled);
//      kakaoMap.getUiSettings().setMyLocationButtonEnabled(myLocationButtonEnabled);
    } else {
      // TODO(amirh): Make the options update fail.
      // https://github.com/flutter/flutter/issues/24327
      Log.e(TAG, "Cannot enable MyLocation layer as location permissions are not granted");
    }
  }

  private boolean hasLocationPermission() {
    return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED;
  }

  private int checkSelfPermission(String permission) {
    if (permission == null) {
      throw new IllegalArgumentException("permission is null");
    }
    return context.checkPermission(
        permission, android.os.Process.myPid(), android.os.Process.myUid());
  }

  private void destroyMapViewIfNecessary() {
    if (mapView == null) {
      return;
    }
    mapView.onDestroy();
    mapView = null;
  }

  public void setIndoorEnabled(boolean indoorEnabled) {
    this.indoorEnabled = indoorEnabled;
  }

  public void setTrafficEnabled(boolean trafficEnabled) {
    this.trafficEnabled = trafficEnabled;
    if (kakaoMap == null) {
      return;
    }
    kakaoMap.setLayerGroupEnabled(KakaoMap.LAYER_GROUP_TRAFFIC, trafficEnabled);
  }

  public void setBuildingsEnabled(boolean buildingsEnabled) {
    this.buildingsEnabled = buildingsEnabled;
  }

  // Naver Map Original Listener

  @Override
  public void onCameraChange(int reason, boolean animated) {
    Log.d("KakaoMapController", "[onCameraChange] ------ " + reason + " " + animated);
  }

  @Override
  public void onIndoorSelectionChange(@Nullable IndoorSelection indoorSelection) {
    Log.d("KakaoMapController", "[onIndoorSelectionChange] ------ " + indoorSelection.toString());
  }

  @Override
  public void onLocationChange(@NonNull Location location) {
    Log.d("KakaoMapController", "[onLocationChange] ------ " + location.toString());
  }

  @Override
  public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
    Log.d("KakaoMapController", "[onMapClick] ------ " + pointF.toString() + " " + latLng.toString());
  }

  @Override
  public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
    Log.d("KakaoMapController", "[onMapLongClick] ------ " + pointF.toString() + " " + latLng.toString());
  }

  @Override
  public boolean onMapDoubleTap(@NonNull PointF pointF, @NonNull LatLng latLng)
  {
    Log.d("KakaoMapController", "[onMapDoubleTap] ------ " + pointF.toString() + " " + latLng.toString());
    return false;
  }

  @Override
  public boolean onMapTwoFingerTap(@NonNull PointF pointF, @NonNull LatLng latLng) {
    Log.d("KakaoMapController", "[onMapTwoFingerTap] ------ " + pointF.toString() + " " + latLng.toString());
    return false;
  }

  @Override
  public void onOptionChange() {
    Log.d("KakaoMapController", "[onOptionChange] ------ ");
  }

  @Override
  public boolean onSymbolClick(@NonNull Symbol symbol) {
    Log.d("KakaoMapController", "[onSymbolClick] ------ " + symbol.toString());
    return false;
  }

  @Override
  public void onSnapshotReady(@NonNull Bitmap bitmap) {
    Log.d("KakaoMapController", "[onSnapshotReady] ------ ");
  }
}
