package se.umu.visi0009.comiccollector;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * RequestQueue handling network request. Implemented using Volley.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class MyRequestQueue {

    private static volatile MyRequestQueue sInstance = null;

    private RequestQueue mRequestQueue;

    /**
     * Constructor for the class. Initializes the RequestQueue attribute.
     *
     * @param context       A context to use for creating the cache directory.
     */
    private MyRequestQueue(final Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Static method used to get the request queue singleton (or create the
     * request queue if no request queue exists). The method uses lazy
     * initialization and is thread-safe.
     *
     * @param context       A context to use for creating the cache directory.
     * @return              The request queue.
     */
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

    /**
     * Adds a request to the dispatch queue.
     *
     * @param request       The request to service.
     */
    public <T> void addToRequestQueue(Request<T> request) {
        mRequestQueue.add(request);
    }
}
