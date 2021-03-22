package android.src.main.java.com.kakao.maps.model;

public interface TileProvider {
    public static final Tile NO_TILE = null;

    Tile getTile(final int x, final int y, final int zoom);
}
