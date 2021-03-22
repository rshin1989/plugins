package com.naver.maps;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.model.Circle;
import com.naver.maps.model.Polygon;
import com.naver.maps.model.Polyline;

public class MapListener {

    public interface OnCameraIdleListener {
    }

    public interface OnCameraMoveListener {
        void onCameraMove();
    }

    public interface OnCameraMoveStartedListener {
        void onCameraMoveStarted(int reason);
    }

    public interface OnInfoWindowClickListener {
    }

    public interface OnMarkerClickListener {
        boolean onMarkerClick(Marker marker);

        void onInfoWindowClick(Marker marker);
    }

    public interface OnPolygonClickListener {
        void onPolygonClick(Polygon polygon);
    }

    public interface OnPolylineClickListener {
        void onPolylineClick(Polyline polyline);
    }

    public interface OnCircleClickListener {
        void onCircleClick(Circle circle);
    }

    public interface OnMapClickListener {
        void onMapClick(LatLng latLng);
    }

    public interface OnMapLongClickListener {
        void onMapLongClick(LatLng latLng);
    }

    public interface OnMarkerDragListener {
        void onMarkerDrag(Marker marker);

        void onMarkerDragStart(Marker marker);

        void onMarkerDragEnd(Marker marker);

    }
    
}
