package com.naver.maps.model;

import android.graphics.Point;
import android.graphics.PointF;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;

public class CameraUpdateFactory {
    public static CameraUpdate newCameraPosition(CameraPosition toCameraPosition) {
        return CameraUpdate.toCameraPosition(toCameraPosition);
    }

    public static CameraUpdate newLatLng(LatLng toLatLng) {
        return CameraUpdate.scrollTo(toLatLng);
    }

    public static CameraUpdate newLatLngBounds(LatLngBounds toLatLngBounds, int toPixels) {
        return CameraUpdate.fitBounds(toLatLngBounds, toPixels);
    }

    public static CameraUpdate newLatLngZoom(LatLng toLatLng, float toFloat) {
        return CameraUpdate.scrollAndZoomTo(toLatLng, toFloat);
    }

    public static CameraUpdate scrollBy(float toFractionalPixels, float toFractionalPixels1) {
        return CameraUpdate.scrollBy(new PointF(toFractionalPixels, toFractionalPixels1));
    }

    public static CameraUpdate zoomBy(float toFloat) {
        return CameraUpdate.zoomBy(toFloat);
    }

    public static CameraUpdate zoomBy(float toFloat, Point toPoint) {
        return CameraUpdate.scrollBy(new PointF(toPoint.x, toPoint.y)).zoomTo(toFloat);
    }

    public static CameraUpdate zoomIn() {
        return CameraUpdate.zoomIn();
    }

    public static CameraUpdate zoomOut() {
        return CameraUpdate.zoomOut();
    }

    public static CameraUpdate zoomTo(float toFloat) {
        return CameraUpdate.zoomTo(toFloat);
    }
}
