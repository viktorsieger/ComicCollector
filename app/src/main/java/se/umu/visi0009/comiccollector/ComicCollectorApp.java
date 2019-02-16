package se.umu.visi0009.comiccollector;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import se.umu.visi0009.comiccollector.db.AppDatabase;

/**
 * Class containing global accessable functions.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class ComicCollectorApp extends Application {

    private static final String TAG = "ComicCollectorApp";

    private AppExecutors mAppExecutors;

    /**
     * Called when the application is starting, before any activity, service, or
     * receiver objects (excluding content providers) have been created.
     * Initializes executors for the app.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = new AppExecutors();
    }

    /**
     * Accessor method for the executors.
     *
     * @return      The app's executors.
     */
    public AppExecutors getAppExecutors() {
        return mAppExecutors;
    }

    /**
     * Accessor method for the app's database singleton.
     *
     * @return      The app's database.
     */
    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    /**
     * Accessor method for the app's repository singleton.
     *
     * @return      The app's repository.
     */
    public DataRepository getRepository() {
        return DataRepository.getsInstance(getDatabase());
    }

    /**
     * Accessor method for the app's (network) request queue singleton.
     *
     * @return      The app's request queue.
     */
    public MyRequestQueue getRequestQueue() {
        return MyRequestQueue.getsInstance(this);
    }

    /**
     * Examines if the app has network functionality.
     *
     * @return      True if network functionality exists.
     */
    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager;
        NetworkInfo networkInfo;

        if((connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)) == null) {
            Log.e(TAG, "Error: Could not get connectivity manager");
            return false;
        }

        if((networkInfo = connectivityManager.getActiveNetworkInfo()) == null) {
            Log.e(TAG, "Error: No default network is currently active");
            return false;
        }

        return networkInfo.isConnected();
    }

    /**
     * Checks if a file with the given filename exists in persistent storage.
     *
     * @param filename      Name of the file to look for.
     * @return              True if a file is found.
     */
    public boolean isFileInPersistentStorage(String filename) {
        String[] allFiles;

        allFiles = fileList();

        for(String tempFilename : allFiles) {
            if(filename.equals(tempFilename)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reads an object from a file in persistent storage.
     *
     * @param filename      Name of the file containing the object.
     * @return              Object contained in the file.
     */
    public Object readObjectFromPersistentStorage(String filename) {
        FileInputStream fis;
        Object object = null;
        ObjectInputStream ois;

        try {
            fis = openFileInput(filename);
            ois = new ObjectInputStream(fis);
            object = ois.readObject();
            ois.close();
            fis.close();
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return object;
    }

    /**
     * Writes an object to a file in persistent storage.
     *
     * @param filename      Name of the file to store the object in.
     * @param object        The object to write.
     */
    public void writeObjectToPersistentStorage(String filename, Object object) {
        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
