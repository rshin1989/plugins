package com.naver.maps.model;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.naver.maps.map.overlay.OverlayImage;

/**
 * Defines an image. For a marker, it can be used to set the image of the marker icon. To obtain a BitmapDescriptor use the factory
 * class {@link BitmapDescriptorFactory}.
 */
public final class BitmapDescriptor {
    private static final String TAG = "BitmapDescriptor";
    private final Type mType;

    private final Bitmap mBitmap;
    final float mTintR;
    final float mTintG;
    final float mTintB;

    private final String mPathString;
    private final int mResourceId;

    BitmapDescriptor(Bitmap bitmap)
    {
        this(bitmap, -1, -1, -1);
    }

    BitmapDescriptor(Bitmap bitmap, float tintR, float tintG, float tintB)
    {
        mType = Type.BITMAP;
        mTintR = tintR;
        mTintG = tintG;
        mTintB = tintB;
        mBitmap = bitmap;
        mPathString = null;
        mResourceId = 0;
    }

    BitmapDescriptor(String pathString, Type type) {
        mType = type;

        mTintR = mTintG = mTintB = -1;
        mBitmap = null;
        mPathString = pathString;
        mResourceId = 0;
    }

    BitmapDescriptor(int resourceId) {
        mType = Type.RESOURCE_ID;

        mTintR = mTintG = mTintB = -1;
        mBitmap = null;
        mPathString = null;
        mResourceId = resourceId;
    }

    Bitmap loadBitmap(Context context)
    {
        InputStream is = null;
        try {
            switch (mType) {
                case DEFAULT:
                    return null;
                case BITMAP:
                    return mBitmap;
                case PATH_ABSOLUTE:
                    return BitmapFactory.decodeFile(mPathString);
                case PATH_ASSET:
                    is = context.getAssets().open(mPathString);
                    return BitmapFactory.decodeStream(is);
                case PATH_FILEINPUT:
                    is = context.openFileInput(mPathString);
                    return BitmapFactory.decodeStream(is);
                case RESOURCE_ID:
                    return ((BitmapDrawable)context.getResources().getDrawable(mResourceId)).getBitmap();
            }
            return null;
        } catch(IOException ex) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i(TAG, "Failed to load bitmap", e);
                }
            }
        }
    }

    enum Type {
        DEFAULT,
        BITMAP,
        PATH_ABSOLUTE,
        PATH_ASSET,
        PATH_FILEINPUT,
        RESOURCE_ID,
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int getIconTintColor() {
      if (mTintR == -1 || mTintG == -1 || mTintB == -1) {
          return -1;
      }
      return Color.valueOf(mTintR, mTintG, mTintB).toArgb();
    }

    public OverlayImage toOverlayImage(Context context) {
      if (context == null) {
        return null;
      }

      Bitmap bitmap = loadBitmap(context);
      if (bitmap == null) {
        return null;
      }
      return OverlayImage.fromBitmap(bitmap);
    }
}