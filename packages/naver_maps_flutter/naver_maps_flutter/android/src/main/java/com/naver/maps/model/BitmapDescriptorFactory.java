package com.naver.maps.model;

import android.graphics.Bitmap;
import android.util.Log;

public class BitmapDescriptorFactory {

    public static BitmapDescriptor fromAsset(String asset) {
        Log.d("YSK", "[BitmapDescriptorFactory] ----- fromAsset: " + asset);
      return new BitmapDescriptor(asset, BitmapDescriptor.Type.PATH_ASSET);
    }

    public static BitmapDescriptor fromBitmap(Bitmap bitmap) {
      Log.d("YSK", "[BitmapDescriptorFactory] ----- fromBitmap: " + (bitmap != null));
      return new BitmapDescriptor(bitmap);
    }

    public static BitmapDescriptor defaultMarker() {
        Log.d("YSK", "[BitmapDescriptorFactory] ----- defaultMarker");
      return null;
    }

    public static BitmapDescriptor defaultMarker(float toFloat) {
        Log.d("YSK", "[BitmapDescriptorFactory] ----- " + toFloat);
      return null;
    }
}
