// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.navermaps;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.model.BitmapDescriptor;

/** Controller of a single Marker on the map. */
class MarkerController implements MarkerOptionsSink {
  private final Context context;
  public final Marker marker;
  private final String naverMapsMarkerId;
  private boolean consumeTapEvents;
  private final InfoWindow infoWindow;

  private String infoWindowTitle;
  private String infoWindowSnippet;

  Marker getMarker() {
    return marker;
  }

  MarkerController(Context context, Marker marker, boolean consumeTapEvents) {
    this.context = context;
    this.marker = marker;
    this.consumeTapEvents = consumeTapEvents;
    this.naverMapsMarkerId = Integer.toString(marker.hashCode());

    this.infoWindow = new InfoWindow();
    this.infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(context) {
        @NonNull
        @Override
        public CharSequence getText(@NonNull InfoWindow infoWindow) {
            StringBuilder stringBuilder = new StringBuilder();
            if (infoWindowTitle != null) {
              stringBuilder.append(infoWindowTitle);
            }

            if (infoWindowSnippet != null) {
              stringBuilder.append("\n");
              stringBuilder.append(infoWindowSnippet);
            }
            return stringBuilder.toString();
        }
    });
  }

  void remove() {
    marker.setMap(null);
  }

  @Override
  public void setAlpha(float alpha) {
    marker.setAlpha(alpha);
  }

  @Override
  public void setAnchor(float u, float v) {
    marker.setAnchor(new PointF(u, v));
  }

  @Override
  public void setConsumeTapEvents(boolean consumeTapEvents) {
    this.consumeTapEvents = consumeTapEvents;
  }

  @Override
  public void setDraggable(boolean draggable) {
//    marker.setDraggable(draggable);
  }

  @Override
  public void setFlat(boolean flat) {
    marker.setFlat(flat);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void setIcon(BitmapDescriptor bitmapDescriptor) {
    if (bitmapDescriptor == null) {
      return;
    }

    OverlayImage overlayImage = bitmapDescriptor.toOverlayImage(context);
    if (overlayImage == null) {
      int colorInt = bitmapDescriptor.getIconTintColor();
      if (colorInt != -1) {
        marker.setIconTintColor(colorInt);
      }
      return;
    }
    marker.setIcon(overlayImage);
  }

  @Override
  public void setInfoWindowAnchor(float u, float v) {
    infoWindow.setAnchor(new PointF(u, v));
  }

  @Override
  public void setInfoWindowText(String title, String snippet) {
    infoWindowTitle = title;
    infoWindowSnippet = snippet;
  }

  @Override
  public void setPosition(LatLng position) {
    marker.setPosition(position);
    infoWindow.setPosition(position);
  }

  @Override
  public void setRotation(float rotation) {
    marker.setAngle(rotation);
  }

  @Override
  public void setVisible(boolean visible) {
    marker.setVisible(visible);
  }

  @Override
  public void setZIndex(float zIndex) {
    marker.setZIndex(Math.round(zIndex));
  }

  String getNaverMapsMarkerId() {
    return naverMapsMarkerId;
  }

  boolean consumeTapEvents() {
    return consumeTapEvents;
  }

  public void showInfoWindow(NaverMap naverMap) {
    infoWindow.open(naverMap);
  }

  public void hideInfoWindow() {
    infoWindow.close();
  }

  public boolean isInfoWindowShown() {
    return infoWindow.isVisible();
  }
}
