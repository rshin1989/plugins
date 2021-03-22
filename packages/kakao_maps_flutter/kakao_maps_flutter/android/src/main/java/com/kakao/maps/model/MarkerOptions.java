package android.src.main.java.com.kakao.maps.model;

import com.naver.maps.geometry.LatLng;

public class MarkerOptions {
    private float alpha;
    private float anchorU;
    private float anchorV;
    private boolean draggable;
    private boolean flat;
    private BitmapDescriptor icon;
    private float infoWindowAnchorU;
    private float infoWindowAnchorV;
    private String title;
    private String snippet;
    private LatLng position;
    private float rotation;
    private boolean visible;
    private float zIndex;

    public void alpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public void anchor(float u, float v) {
        this.anchorU = u;
        this.anchorV = v;
    }

    public float getAnchorU() {
        return anchorU;
    }

    public float getAnchorV() {
        return anchorV;
    }

    public void draggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void flat(boolean flat) {
        this.flat = flat;
    }

    public boolean isFlat() {
        return flat;
    }

    public void icon(BitmapDescriptor icon) {
        this.icon = icon;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void infoWindowAnchor(float u, float v) {
        this.infoWindowAnchorU = u;
        this.infoWindowAnchorV = v;
    }

    public float getInfoWindowAnchorU() {
        return infoWindowAnchorU;
    }

    public float getInfoWindowAnchorV() {
        return infoWindowAnchorV;
    }

    public void title(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void snippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSnippet() {
        return snippet;
    }

    public void position(LatLng position) {
        this.position = position;
    }

    public LatLng getPosition() {
        return position;
    }

    public void rotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void visible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void zIndex(float zIndex) {
        this.zIndex = zIndex;
    }

    public float getZIndex() {
        return zIndex;
    }
}
