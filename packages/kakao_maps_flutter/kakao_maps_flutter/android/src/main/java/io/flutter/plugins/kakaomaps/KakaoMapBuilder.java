// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package android.src.main.java.io.flutter.plugins.kakaomaps;

import android.content.Context;
import android.graphics.Rect;

import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import io.flutter.plugin.common.BinaryMessenger;
import java.util.List;
import java.util.Map;

class KakaoMapBuilder implements NaverMapOptionsSink {
  private final NaverMapOptions options = new NaverMapOptions();
  private boolean trackCameraPosition = false;
  private boolean myLocationEnabled = false;
  private boolean myLocationButtonEnabled = false;
  private boolean indoorEnabled = true;
  private boolean trafficEnabled = false;
  private boolean buildingsEnabled = true;
  private Object initialMarkers;
  private Object initialPolygons;
  private Object initialPolylines;
  private Object initialCircles;
  private List<Map<String, ?>> initialTileOverlays;
  private Rect padding = new Rect(0, 0, 0, 0);

  NaverMapController build(
      int id,
      Context context,
      BinaryMessenger binaryMessenger,
      LifecycleProvider lifecycleProvider) {
    final NaverMapController controller =
        new NaverMapController(id, context, binaryMessenger, lifecycleProvider, options);
    controller.init();
    controller.setMyLocationEnabled(myLocationEnabled);
    controller.setMyLocationButtonEnabled(myLocationButtonEnabled);
    controller.setIndoorEnabled(indoorEnabled);
    controller.setTrafficEnabled(trafficEnabled);
    controller.setBuildingsEnabled(buildingsEnabled);
    controller.setTrackCameraPosition(trackCameraPosition);
    controller.setInitialMarkers(initialMarkers);
    controller.setInitialPolygons(initialPolygons);
    controller.setInitialPolylines(initialPolylines);
    controller.setInitialCircles(initialCircles);
    controller.setPadding(padding.top, padding.left, padding.bottom, padding.right);
    controller.setInitialTileOverlays(initialTileOverlays);
    return controller;
  }

  void setInitialCameraPosition(CameraPosition position) {
    options.camera(position);
  }

  @Override
  public void setCompassEnabled(boolean compassEnabled) {
    options.compassEnabled(compassEnabled);
  }

  @Override
  public void setMapToolbarEnabled(boolean setMapToolbarEnabled) {
    //TODO("setMapToolbarEnabled")
//    options.mapToolbarEnabled(setMapToolbarEnabled);
  }

  @Override
  public void setCameraTargetBounds(LatLngBounds bounds) {
    //TODO("setCameraTargetBounds")
//    options.latLngBoundsForCameraTarget(bounds);
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
    options.mapType(naverMapType);
  }

  @Override
  public void setMinMaxZoomPreference(Float min, Float max) {
    if (min != null) {
      options.minZoom(min);
    }
    if (max != null) {
      options.maxZoom(max);
    }
  }

  @Override
  public void setPadding(float top, float left, float bottom, float right) {
    this.padding = new Rect((int) left, (int) top, (int) right, (int) bottom);
  }

  @Override
  public void setTrackCameraPosition(boolean trackCameraPosition) {
    this.trackCameraPosition = trackCameraPosition;
  }

  @Override
  public void setRotateGesturesEnabled(boolean rotateGesturesEnabled) {
    options.rotateGesturesEnabled(rotateGesturesEnabled);
  }

  @Override
  public void setScrollGesturesEnabled(boolean scrollGesturesEnabled) {
    options.scrollGesturesEnabled(scrollGesturesEnabled);
  }

  @Override
  public void setTiltGesturesEnabled(boolean tiltGesturesEnabled) {
    options.tiltGesturesEnabled(tiltGesturesEnabled);
  }

  @Override
  public void setZoomGesturesEnabled(boolean zoomGesturesEnabled) {
    options.zoomGesturesEnabled(zoomGesturesEnabled);
  }

  @Override
  public void setLiteModeEnabled(boolean liteModeEnabled) {
    options.liteModeEnabled(liteModeEnabled);
  }

  @Override
  public void setIndoorEnabled(boolean indoorEnabled) {
    this.indoorEnabled = indoorEnabled;
  }

  @Override
  public void setTrafficEnabled(boolean trafficEnabled) {
    this.trafficEnabled = trafficEnabled;
  }

  @Override
  public void setBuildingsEnabled(boolean buildingsEnabled) {
    this.buildingsEnabled = buildingsEnabled;
  }

  @Override
  public void setMyLocationEnabled(boolean myLocationEnabled) {
    this.myLocationEnabled = myLocationEnabled;
  }

  @Override
  public void setZoomControlsEnabled(boolean zoomControlsEnabled) {
    options.zoomControlEnabled(zoomControlsEnabled);
  }

  @Override
  public void setMyLocationButtonEnabled(boolean myLocationButtonEnabled) {
    this.myLocationButtonEnabled = myLocationButtonEnabled;
  }

  @Override
  public void setInitialMarkers(Object initialMarkers) {
    this.initialMarkers = initialMarkers;
  }

  @Override
  public void setInitialPolygons(Object initialPolygons) {
    this.initialPolygons = initialPolygons;
  }

  @Override
  public void setInitialPolylines(Object initialPolylines) {
    this.initialPolylines = initialPolylines;
  }

  @Override
  public void setInitialCircles(Object initialCircles) {
    this.initialCircles = initialCircles;
  }

  @Override
  public void setInitialTileOverlays(List<Map<String, ?>> initialTileOverlays) {
    this.initialTileOverlays = initialTileOverlays;
  }
}
