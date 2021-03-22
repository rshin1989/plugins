package com.naver.maps;

public interface MapWrapperListener extends MapListener.OnCameraIdleListener,
        MapListener.OnCameraMoveListener,
        MapListener.OnCameraMoveStartedListener,
        MapListener.OnInfoWindowClickListener,
        MapListener.OnMarkerClickListener,
        MapListener.OnPolygonClickListener,
        MapListener.OnPolylineClickListener,
        MapListener.OnCircleClickListener,
        MapListener.OnMapClickListener,
        MapListener.OnMapLongClickListener,
        MapListener.OnMarkerDragListener {
}