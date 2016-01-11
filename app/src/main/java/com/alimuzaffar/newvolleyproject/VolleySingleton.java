package com.alimuzaffar.newvolleyproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Ali Muzaffar on 11/01/2016.
 */
public class VolleySingleton {

    public static VolleySingleton sInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public static synchronized VolleySingleton getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VolleySingleton(context);
        }
        return sInstance;
    }

    private VolleySingleton(Context context) {
        mRequestQueue = Volley.newRequestQueue(context, new SslHttpStack(false));
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

            }
        });
    }

    public Request add(Request request) {
        return mRequestQueue.add(request);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}
