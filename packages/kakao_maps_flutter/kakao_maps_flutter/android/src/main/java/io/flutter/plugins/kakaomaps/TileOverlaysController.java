// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.kakaomaps;

import com.naver.maps.map.KakaoMap;
import com.naver.maps.model.TileOverlay;
import com.naver.maps.model.TileOverlayOptions;

import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TileOverlaysController {

  private final Map<String, TileOverlayController> tileOverlayIdToController;
  private final MethodChannel methodChannel;
  private KakaoMap kakaoMap;

  TileOverlaysController(MethodChannel methodChannel) {
    this.tileOverlayIdToController = new HashMap<>();
    this.methodChannel = methodChannel;
  }

  void setKakaoMap(KakaoMap kakaoMap) {
    this.kakaoMap = kakaoMap;
  }

  void addTileOverlays(List<Map<String, ?>> tileOverlaysToAdd) {
    if (tileOverlaysToAdd == null) {
      return;
    }
    for (Map<String, ?> tileOverlayToAdd : tileOverlaysToAdd) {
      addTileOverlay(tileOverlayToAdd);
    }
  }

  void changeTileOverlays(List<Map<String, ?>> tileOverlaysToChange) {
    if (tileOverlaysToChange == null) {
      return;
    }
    for (Map<String, ?> tileOverlayToChange : tileOverlaysToChange) {
      changeTileOverlay(tileOverlayToChange);
    }
  }

  void removeTileOverlays(List<String> tileOverlayIdsToRemove) {
    if (tileOverlayIdsToRemove == null) {
      return;
    }
    for (String tileOverlayId : tileOverlayIdsToRemove) {
      if (tileOverlayId == null) {
        continue;
      }
      removeTileOverlay(tileOverlayId);
    }
  }

  void clearTileCache(String tileOverlayId) {
    if (tileOverlayId == null) {
      return;
    }
    TileOverlayController tileOverlayController = tileOverlayIdToController.get(tileOverlayId);
    if (tileOverlayController != null) {
      tileOverlayController.clearTileCache();
    }
  }

  Map<String, Object> getTileOverlayInfo(String tileOverlayId) {
    if (tileOverlayId == null) {
      return null;
    }
    TileOverlayController tileOverlayController = tileOverlayIdToController.get(tileOverlayId);
    if (tileOverlayController == null) {
      return null;
    }
    return tileOverlayController.getTileOverlayInfo();
  }

  private void addTileOverlay(Map<String, ?> tileOverlayOptions) {
    return;
    //TODO('Implement addTileOverlay')
//    if (tileOverlayOptions == null) {
//      return;
//    }
//    TileOverlayBuilder tileOverlayOptionsBuilder = new TileOverlayBuilder();
//    String tileOverlayId =
//        Convert.interpretTileOverlayOptions(tileOverlayOptions, tileOverlayOptionsBuilder);
//    TileProviderController tileProviderController =
//        new TileProviderController(methodChannel, tileOverlayId);
//    tileOverlayOptionsBuilder.setTileProvider(tileProviderController);
//    TileOverlayOptions options = tileOverlayOptionsBuilder.build();
//    TileOverlay tileOverlay = kakaoMap.addTileOverlay(options);
//    TileOverlayController tileOverlayController = new TileOverlayController(tileOverlay);
//    tileOverlayIdToController.put(tileOverlayId, tileOverlayController);
  }

  private void changeTileOverlay(Map<String, ?> tileOverlayOptions) {
    if (tileOverlayOptions == null) {
      return;
    }
    String tileOverlayId = getTileOverlayId(tileOverlayOptions);
    TileOverlayController tileOverlayController = tileOverlayIdToController.get(tileOverlayId);
    if (tileOverlayController != null) {
      Convert.interpretTileOverlayOptions(tileOverlayOptions, tileOverlayController);
    }
  }

  private void removeTileOverlay(String tileOverlayId) {
    TileOverlayController tileOverlayController = tileOverlayIdToController.get(tileOverlayId);
    if (tileOverlayController != null) {
      tileOverlayController.remove();
      tileOverlayIdToController.remove(tileOverlayId);
    }
  }

  @SuppressWarnings("unchecked")
  private static String getTileOverlayId(Map<String, ?> tileOverlay) {
    return (String) tileOverlay.get("tileOverlayId");
  }
}
