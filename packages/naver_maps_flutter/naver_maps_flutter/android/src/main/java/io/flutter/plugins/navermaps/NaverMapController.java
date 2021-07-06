// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.navermaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Symbol;
import com.naver.maps.map.indoor.IndoorSelection;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
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

/**
 * Controller of a single NaverMaps MapView instance.
 */
final class NaverMapController
        implements DefaultLifecycleObserver,
        ActivityPluginBinding.OnSaveInstanceStateListener,
        NaverMapOptionsSink,
        MethodChannel.MethodCallHandler,
        OnMapReadyCallback,
        NaverMapListener,
        MapWrapperListener,
        PlatformView {

    private static final String TAG = "NaverMapController";
    private final int id;
    private final MethodChannel methodChannel;
    private final NaverMapOptions options;
    @Nullable
    private MapView mapView;
    private NaverMap naverMap;
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

    // For my location
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    public static FusedLocationSource locationSource;

    NaverMapController(
            int id,
            Context context,
            BinaryMessenger binaryMessenger,
            LifecycleProvider lifecycleProvider,
            NaverMapOptions options) {
        Log.d("NaverMapController", "id:" + id);
        this.id = id;
        this.context = context;
        this.options = options;
        this.mapView = new MapView(context, options);
        this.density = context.getResources().getDisplayMetrics().density;
        methodChannel = new MethodChannel(binaryMessenger, "plugins.flutter.io/naver_maps_" + id);
        methodChannel.setMethodCallHandler(this);
        this.lifecycleProvider = lifecycleProvider;
        this.markersController = new MarkersController(context, methodChannel);
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
        naverMap.moveCamera(cameraUpdate);
    }

    private void animateCamera(CameraUpdate cameraUpdate) {
        cameraUpdate.animate(CameraAnimation.Easing);
        naverMap.moveCamera(cameraUpdate);
    }

    private CameraPosition getCameraPosition() {
        return trackCameraPosition ? naverMap.getCameraPosition() : null;
    }

    @Override
    public void onMapReady(NaverMap naverMap) {
        Log.d("NaverMapController", "onMapReady");
        this.naverMap = naverMap;

        // Map Layer Initialize
        this.naverMap.setIndoorEnabled(this.indoorEnabled);
        this.naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, this.buildingsEnabled);
        this.naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, this.trafficEnabled);
        this.naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, this.trafficEnabled);
        this.naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, this.trafficEnabled);
        this.naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, this.trafficEnabled);
        this.naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, this.buildingsEnabled);
        //TODO("마커의 정보창을 출력해주는 형태만 지원한다")
        //naverMap.setOnInfoWindowClickListener(this);
        if (mapReadyResult != null) {
            mapReadyResult.success(null);
            mapReadyResult = null;
        }
        setNaverMapListener(this);
        updateMyLocationSettings();
        markersController.setNaverMap(naverMap);
        polygonsController.setNaverMap(naverMap);
        polylinesController.setNaverMap(naverMap);
        circlesController.setNaverMap(naverMap);
        tileOverlaysController.setNaverMap(naverMap);
        updateInitialMarkers();
        updateInitialPolygons();
        updateInitialPolylines();
        updateInitialCircles();
        updateInitialTileOverlays();
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        Log.d("NaverMapController", "call.method: " + call.method + ", " + call.arguments);
        switch (call.method) {
            case "map#waitForMap":
                if (naverMap != null) {
                    result.success(null);
                    return;
                }
                mapReadyResult = result;
                break;
            case "map#update": {
                Convert.interpretNaverMapOptions(call.argument("options"), this);
                result.success(Convert.cameraPositionToJson(getCameraPosition()));
                break;
            }
            case "map#getVisibleRegion": {
                if (naverMap != null) {
                    //TODO("map#getVisibleRegion")
//            LatLngBounds latLngBounds = naverMap.getProjection().getVisibleRegion().latLngBounds;
//            result.success(Convert.latlngBoundsToJson(latLngBounds));
                    result.success(null);
                } else {
                    result.error(
                            "NaverMap uninitialized",
                            "getVisibleRegion called prior to map initialization",
                            null);
                }
                break;
            }
            case "map#getScreenCoordinate": {
                if (naverMap != null) {
                    LatLng latLng = Convert.toLatLng(call.arguments);
                    PointF screenLocation = naverMap.getProjection().toScreenLocation(latLng);
                    result.success(Convert.pointToJson(screenLocation));
                } else {
                    result.error(
                            "NaverMap uninitialized",
                            "getScreenCoordinate called prior to map initialization",
                            null);
                }
                break;
            }
            case "map#getLatLng": {
                if (naverMap != null) {
                    PointF point = Convert.toPoint(call.arguments);
                    LatLng latLng = naverMap.getProjection().fromScreenLocation(point);
                    result.success(Convert.latLngToJson(latLng));
                } else {
                    result.error(
                            "NaverMap uninitialized", "getLatLng called prior to map initialization", null);
                }
                break;
            }
            case "map#takeSnapshot": {
                if (naverMap != null) {
                    final MethodChannel.Result _result = result;
                    naverMap.takeSnapshot(
                            new NaverMap.SnapshotReadyCallback() {
                                @Override
                                public void onSnapshotReady(Bitmap bitmap) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    bitmap.recycle();
                                    _result.success(byteArray);
                                }
                            });
                } else {
                    result.error("NaverMap uninitialized", "takeSnapshot", null);
                }
                break;
            }
            case "camera#move": {
                final CameraUpdate cameraUpdate =
                        Convert.toCameraUpdate(call.argument("cameraUpdate"), density);
                moveCamera(cameraUpdate);
                result.success(null);
                break;
            }
            case "camera#animate": {
                final CameraUpdate cameraUpdate =
                        Convert.toCameraUpdate(call.argument("cameraUpdate"), density);
                animateCamera(cameraUpdate);
                result.success(null);
                break;
            }
            case "markers#update": {
                List<Object> markersToAdd = call.argument("markersToAdd");
                markersController.addMarkers(markersToAdd);
                List<Object> markersToChange = call.argument("markersToChange");
                markersController.changeMarkers(markersToChange);
                List<Object> markerIdsToRemove = call.argument("markerIdsToRemove");
                markersController.removeMarkers(markerIdsToRemove);
                result.success(null);
                break;
            }
            case "markers#showInfoWindow": {
                Object markerId = call.argument("markerId");
                markersController.showMarkerInfoWindow((String) markerId, result);
                break;
            }
            case "markers#hideInfoWindow": {
                Object markerId = call.argument("markerId");
                markersController.hideMarkerInfoWindow((String) markerId, result);
                break;
            }
            case "markers#isInfoWindowShown": {
                Object markerId = call.argument("markerId");
                markersController.isInfoWindowShown((String) markerId, result);
                break;
            }
            case "polygons#update": {
                List<Object> polygonsToAdd = call.argument("polygonsToAdd");
                polygonsController.addPolygons(polygonsToAdd);
                List<Object> polygonsToChange = call.argument("polygonsToChange");
                polygonsController.changePolygons(polygonsToChange);
                List<Object> polygonIdsToRemove = call.argument("polygonIdsToRemove");
                polygonsController.removePolygons(polygonIdsToRemove);
                result.success(null);
                break;
            }
            case "polylines#update": {
                List<Object> polylinesToAdd = call.argument("polylinesToAdd");
                polylinesController.addPolylines(polylinesToAdd);
                List<Object> polylinesToChange = call.argument("polylinesToChange");
                polylinesController.changePolylines(polylinesToChange);
                List<Object> polylineIdsToRemove = call.argument("polylineIdsToRemove");
                polylinesController.removePolylines(polylineIdsToRemove);
                result.success(null);
                break;
            }
            case "circles#update": {
                List<Object> circlesToAdd = call.argument("circlesToAdd");
                circlesController.addCircles(circlesToAdd);
                List<Object> circlesToChange = call.argument("circlesToChange");
                circlesController.changeCircles(circlesToChange);
                List<Object> circleIdsToRemove = call.argument("circleIdsToRemove");
                circlesController.removeCircles(circleIdsToRemove);
                result.success(null);
                break;
            }
            case "map#isCompassEnabled": {
                result.success(naverMap.getUiSettings().isCompassEnabled());
                break;
            }
            case "map#isMapToolbarEnabled": {
                //TODO("map#isMapToolbarEnabled")
//          result.success(naverMap.getUiSettings().isMapToolbarEnabled());
                break;
            }
            case "map#getMinMaxZoomLevels": {
                List<Double> zoomLevels = new ArrayList<>(2);
                zoomLevels.add(naverMap.getMinZoom());
                zoomLevels.add(naverMap.getMaxZoom());
                result.success(zoomLevels);
                break;
            }
            case "map#isZoomGesturesEnabled": {
                result.success(naverMap.getUiSettings().isZoomGesturesEnabled());
                break;
            }
            case "map#isLiteModeEnabled": {
                result.success(options.isLiteModeEnabled());
                break;
            }
            case "map#isZoomControlsEnabled": {
                result.success(naverMap.getUiSettings().isZoomControlEnabled());
                break;
            }
            case "map#isScrollGesturesEnabled": {
                result.success(naverMap.getUiSettings().isScrollGesturesEnabled());
                break;
            }
            case "map#isTiltGesturesEnabled": {
                result.success(naverMap.getUiSettings().isTiltGesturesEnabled());
                break;
            }
            case "map#isRotateGesturesEnabled": {
                result.success(naverMap.getUiSettings().isRotateGesturesEnabled());
                break;
            }
            case "map#isMyLocationButtonEnabled": {
                result.success(naverMap.getUiSettings().isLocationButtonEnabled());
                break;
            }
            case "map#isTrafficEnabled": {
                result.success(naverMap.isLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC));
                break;
            }
            case "map#isBuildingsEnabled": {
                result.success(naverMap.isLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING));
                break;
            }
            case "map#getZoomLevel": {
                result.success(naverMap.getCameraPosition().zoom);
                break;
            }
            case "map#setStyle": {
                //TODO("map#setStyle")
//          String mapStyle = (String) call.arguments;
//          boolean mapStyleSet;
//          if (mapStyle == null) {
//            mapStyleSet = naverMap.setMapStyle(null);
//          } else {
//            mapStyleSet = naverMap.setMapStyle(new MapStyleOptions(mapStyle));
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
            case "tileOverlays#update": {
                List<Map<String, ?>> tileOverlaysToAdd = call.argument("tileOverlaysToAdd");
                tileOverlaysController.addTileOverlays(tileOverlaysToAdd);
                List<Map<String, ?>> tileOverlaysToChange = call.argument("tileOverlaysToChange");
                tileOverlaysController.changeTileOverlays(tileOverlaysToChange);
                List<String> tileOverlaysToRemove = call.argument("tileOverlayIdsToRemove");
                tileOverlaysController.removeTileOverlays(tileOverlaysToRemove);
                result.success(null);
                break;
            }
            case "tileOverlays#clearTileCache": {
                String tileOverlayId = call.argument("tileOverlayId");
                tileOverlaysController.clearTileCache(tileOverlayId);
                result.success(null);
                break;
            }
            case "map#getTileOverlayInfo": {
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
        arguments.put("position", Convert.cameraPositionToJson(naverMap.getCameraPosition()));
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
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

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
        setNaverMapListener(null);
        destroyMapViewIfNecessary();
        Lifecycle lifecycle = lifecycleProvider.getLifecycle();
        if (lifecycle != null) {
            lifecycle.removeObserver(this);
        }
    }

    private void setNaverMapListener(@Nullable NaverMapListener listener) {
        if (listener == null) {
            naverMap.removeOnCameraChangeListener(this);
            naverMap.removeOnCameraIdleListener(this);
            naverMap.removeOnIndoorSelectionChangeListener(this);
            naverMap.removeOnLocationChangeListener(this);
            naverMap.removeOnOptionChangeListener(this);
            naverMap.setOnMapClickListener(null);
            naverMap.setOnMapDoubleTapListener(null);
            naverMap.setOnMapLongClickListener(null);
            naverMap.setOnMapTwoFingerTapListener(null);
            naverMap.setOnSymbolClickListener(null);
            return;
        }
        naverMap.addOnCameraChangeListener(this);
        naverMap.addOnCameraIdleListener(this);
        naverMap.addOnIndoorSelectionChangeListener(this);
        naverMap.addOnLocationChangeListener(this);
        naverMap.addOnOptionChangeListener(this);
        naverMap.setOnMapClickListener(listener);
        naverMap.setOnMapDoubleTapListener(listener);
        naverMap.setOnMapLongClickListener(listener);
        naverMap.setOnMapTwoFingerTapListener(listener);
        naverMap.setOnSymbolClickListener(listener);
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
        Log.d("NaverMapController", "------- onCreate");
        if (disposed) {
            return;
        }
        mapView.onCreate(null);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("NaverMapController", "------- onStart");
        if (disposed) {
            return;
        }
        mapView.onStart();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        Log.d("NaverMapController", "------- onResume");
        if (disposed) {
            return;
        }
        mapView.onResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        Log.d("NaverMapController", "------- onPause");
        if (disposed) {
            return;
        }
        mapView.onResume();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("NaverMapController", "------- onStop");
        if (disposed) {
            return;
        }
        mapView.onStop();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        Log.d("NaverMapController", "------- onDestroy");
        owner.getLifecycle().removeObserver(this);
        if (disposed) {
            return;
        }
        destroyMapViewIfNecessary();
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        Log.d("NaverMapController", "------- onRestoreInstanceState");
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

    // NaverMapOptionsSink methods

    @Override
    public void setCameraTargetBounds(LatLngBounds bounds) {
      if (bounds == null) {
        return;
      }
      CameraUpdate cameraUpdate = CameraUpdate.fitBounds(bounds);
      naverMap.moveCamera(cameraUpdate);
    }

    @Override
    public void setCompassEnabled(boolean compassEnabled) {
        naverMap.getUiSettings().setCompassEnabled(compassEnabled);
    }

    @Override
    public void setMapToolbarEnabled(boolean mapToolbarEnabled) {
        //TODO("setMapToolbarEnabled")
//    naverMap.getUiSettings().setMapToolbarEnabled(mapToolbarEnabled);
    }

    @Override
    public void setMapType(int mapType) {
        NaverMap.MapType naverMapType;
        switch (mapType) {
            case 0:
                naverMapType = NaverMap.MapType.Basic;
                break;
            case 1:
                naverMapType = NaverMap.MapType.Navi;
                break;
            case 2:
                naverMapType = NaverMap.MapType.Satellite;
                break;
            case 3:
                naverMapType = NaverMap.MapType.Hybrid;
                break;
            case 4:
                naverMapType = NaverMap.MapType.Terrain;
                break;
            default:
                naverMapType = NaverMap.MapType.None;
                break;
        }
        naverMap.setMapType(naverMapType);
    }

    @Override
    public void setTrackCameraPosition(boolean trackCameraPosition) {
        this.trackCameraPosition = trackCameraPosition;
    }

    @Override
    public void setRotateGesturesEnabled(boolean rotateGesturesEnabled) {
        naverMap.getUiSettings().setRotateGesturesEnabled(rotateGesturesEnabled);
    }

    @Override
    public void setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
        naverMap.getUiSettings().setScrollGesturesEnabled(scrollGesturesEnabled);
    }

    @Override
    public void setTiltGesturesEnabled(boolean tiltGesturesEnabled) {
        naverMap.getUiSettings().setTiltGesturesEnabled(tiltGesturesEnabled);
    }

    @Override
    public void setMinMaxZoomPreference(Float min, Float max) {
        if (min != null) {
            naverMap.setMinZoom(min);
        }
        if (max != null) {
            naverMap.setMaxZoom(max);
        }
    }

    @Override
    public void setPadding(float top, float left, float bottom, float right) {
        if (naverMap != null) {
            naverMap.setContentPadding(
                    (int) (left * density),
                    (int) (top * density),
                    (int) (right * density),
                    (int) (bottom * density));
        }
    }

    @Override
    public void setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
        naverMap.getUiSettings().setZoomGesturesEnabled(zoomGesturesEnabled);
    }

    /**
     * This call will have no effect on already created map
     */
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
        if (naverMap != null) {
            updateMyLocationSettings();
        }
    }

    @Override
    public void setMyLocationButtonEnabled(boolean myLocationButtonEnabled) {
        if (this.myLocationButtonEnabled == myLocationButtonEnabled) {
            return;
        }
        this.myLocationButtonEnabled = myLocationButtonEnabled;
        if (naverMap != null) {
            updateMyLocationSettings();
        }
    }

    @Override
    public void setZoomControlsEnabled(boolean zoomControlsEnabled) {
        if (this.zoomControlsEnabled == zoomControlsEnabled) {
            return;
        }
        this.zoomControlsEnabled = zoomControlsEnabled;
        if (naverMap != null) {
            naverMap.getUiSettings().setZoomControlEnabled(zoomControlsEnabled);
        }
    }

    @Override
    public void setInitialMarkers(Object initialMarkers) {
        ArrayList<?> markers = (ArrayList<?>) initialMarkers;
        this.initialMarkers = markers != null ? new ArrayList<>(markers) : null;
        if (naverMap != null) {
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
        if (naverMap != null) {
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
        if (naverMap != null) {
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
        if (naverMap != null) {
            updateInitialCircles();
        }
    }

    private void updateInitialCircles() {
        circlesController.addCircles(initialCircles);
    }

    @Override
    public void setInitialTileOverlays(List<Map<String, ?>> initialTileOverlays) {
        this.initialTileOverlays = initialTileOverlays;
        if (naverMap != null) {
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
//            this.naverMap.setLocationSource(locationSource);
            this.naverMap.getUiSettings().setLocationButtonEnabled(myLocationButtonEnabled);

            if (!myLocationButtonEnabled) {
                return;
            }

//            if (!myLocationEnabled) {
//                this.naverMap.setLocationTrackingMode(LocationTrackingMode.None);
//            } else {
//                this.naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
//            }
        } else {
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
        if (naverMap == null) {
            return;
        }
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, trafficEnabled);
    }

    public void setBuildingsEnabled(boolean buildingsEnabled) {
        this.buildingsEnabled = buildingsEnabled;
    }

    // Naver Map Original Listener

    @Override
    public void onCameraChange(int reason, boolean animated) {
    }

    @Override
    public void onIndoorSelectionChange(@Nullable IndoorSelection indoorSelection) {
    }

    @Override
    public void onLocationChange(@NonNull Location location) {
    }

    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        // Connect with google maps interface
        onMapClick(latLng);
    }

    @Override
    public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        // Connect with google maps interface
        onMapLongClick(latLng);
    }

    @Override
    public boolean onMapDoubleTap(@NonNull PointF pointF, @NonNull LatLng latLng) {
        return false;
    }

    @Override
    public boolean onMapTwoFingerTap(@NonNull PointF pointF, @NonNull LatLng latLng) {
        return false;
    }

    @Override
    public void onOptionChange() {

    }

    @Override
    public boolean onSymbolClick(@NonNull Symbol symbol) {
        return false;
    }

    @Override
    public void onSnapshotReady(@NonNull Bitmap bitmap) {
    }
}
