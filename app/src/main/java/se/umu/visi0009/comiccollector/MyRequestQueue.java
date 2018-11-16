package se.umu.visi0009.comiccollector;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyRequestQueue {

    public static final String REQUEST_TAG = "myRequestTag";

    private static volatile MyRequestQueue sInstance = null;

    private RequestQueue mRequestQueue;

    private MyRequestQueue(final Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static MyRequestQueue getsInstance(final Context context) {
        if(sInstance == null) {
            synchronized(MyRequestQueue.class) {
                if(sInstance == null) {
                    sInstance = new MyRequestQueue(context);
                }
            }
        }
        return sInstance;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        mRequestQueue.add(request);
    }

    public void cancelRequests(String tag) {
        mRequestQueue.cancelAll(tag);
    }
}
