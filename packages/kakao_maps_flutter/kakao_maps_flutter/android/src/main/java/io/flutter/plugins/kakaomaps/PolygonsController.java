// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package android.src.main.java.io.flutter.plugins.kakaomaps;

import com.naver.maps.map.NaverMap;
import com.naver.maps.model.Polygon;
import com.naver.maps.model.PolygonOptions;

import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PolygonsController {

  private final Map<String, PolygonController> polygonIdToController;
  private final Map<String, String> naverMapsPolygonIdToDartPolygonId;
  private final MethodChannel methodChannel;
  private final float density;
  private NaverMap naverMap;

  PolygonsController(MethodChannel methodChannel, float density) {
    this.polygonIdToController = new HashMap<>();
    this.naverMapsPolygonIdToDartPolygonId = new HashMap<>();
    this.methodChannel = methodChannel;
    this.density = density;
  }

  void setNaverMap(NaverMap naverMap) {
    this.naverMap = naverMap;
  }

  void addPolygons(List<Object> polygonsToAdd) {
    if (polygonsToAdd != null) {
      for (Object polygonToAdd : polygonsToAdd) {
        addPolygon(polygonToAdd);
      }
    }
  }

  void changePolygons(List<Object> polygonsToChange) {
    if (polygonsToChange != null) {
      for (Object polygonToChange : polygonsToChange) {
        changePolygon(polygonToChange);
      }
    }
  }

  void removePolygons(List<Object> polygonIdsToRemove) {
    //TODO("removePolygons")
//    if (polygonIdsToRemove == null) {
//      return;
//    }
//    for (Object rawPolygonId : polygonIdsToRemove) {
//      if (rawPolygonId == null) {
//        continue;
//      }
//      String polygonId = (String) rawPolygonId;
//      final PolygonController polygonController = polygonIdToController.remove(polygonId);
//      if (polygonController != null) {
//        polygonController.remove();
//        naverMapsPolygonIdToDartPolygonId.remove(polygonController.getNaverMapsPolygonId());
//      }
//    }
  }

  boolean onPolygonTap(String googlePolygonId) {
    String polygonId = naverMapsPolygonIdToDartPolygonId.get(googlePolygonId);
    if (polygonId == null) {
      return false;
    }
    methodChannel.invokeMethod("polygon#onTap", Convert.polygonIdToJson(polygonId));
    PolygonController polygonController = polygonIdToController.get(polygonId);
    if (polygonController != null) {
      return polygonController.consumeTapEvents();
    }
    return false;
  }

  private void addPolygon(Object polygon) {
    if (polygon == null) {
      return;
    }
    PolygonBuilder polygonBuilder = new PolygonBuilder(density);
    String polygonId = Convert.interpretPolygonOptions(polygon, polygonBuilder);
    PolygonOptions options = polygonBuilder.build();
    addPolygon(polygonId, options, polygonBuilder.consumeTapEvents());
  }

  private void addPolygon(
      String polygonId, PolygonOptions polygonOptions, boolean consumeTapEvents) {
    //TODO("addPolygon")
//    final Polygon polygon = naverMap.addPolygon(polygonOptions);
//    PolygonController controller = new PolygonController(polygon, consumeTapEvents, density);
//    polygonIdToController.put(polygonId, controller);
//    naverMapsPolygonIdToDartPolygonId.put(polygon.getId(), polygonId);
  }

  private void changePolygon(Object polygon) {
    if (polygon == null) {
      return;
    }
    String polygonId = getPolygonId(polygon);
    PolygonController polygonController = polygonIdToController.get(polygonId);
    if (polygonController != null) {
      Convert.interpretPolygonOptions(polygon, polygonController);
    }
  }

  @SuppressWarnings("unchecked")
  private static String getPolygonId(Object polygon) {
    Map<String, Object> polygonMap = (Map<String, Object>) polygon;
    return (String) polygonMap.get("polygonId");
  }
}
