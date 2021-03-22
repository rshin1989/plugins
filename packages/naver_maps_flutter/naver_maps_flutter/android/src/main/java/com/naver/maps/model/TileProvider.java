package com.naver.maps.model;

public interface TileProvider {
    public static final Tile NO_TILE = null;

    Tile getTile(final int x, final int y, final int zoom);
}
