package com.naver.maps.model;

import android.graphics.Bitmap;

public class BitmapDescriptorFactory {

    public static BitmapDescriptor fromAsset(String asset) {
      return new BitmapDescriptor(asset, BitmapDescriptor.Type.PATH_ASSET);
    }

    public static BitmapDescriptor fromBitmap(Bitmap bitmap) {
      return new BitmapDescriptor(bitmap);
    }

    public static BitmapDescriptor defaultMarker() {
      return null;
    }

    public static BitmapDescriptor defaultMarker(float toFloat) {
      return null;
    }
}
