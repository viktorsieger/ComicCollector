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

public class ComicCollectorApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = new AppExecutors();
    }

    public AppExecutors getAppExecutors() {
        return mAppExecutors;
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getsInstance(getDatabase());
    }

    public MyRequestQueue getRequestQueue() {
        return MyRequestQueue.getsInstance(this);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager;
        NetworkInfo networkInfo;

        if((connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)) == null) {
            Log.d("TEST", "Error: Could not get connectivity manager");
            return false;
        }

        if((networkInfo = connectivityManager.getActiveNetworkInfo()) == null) {
            Log.d("TEST", "Error: No default network is currently active");
            return false;
        }

        return networkInfo.isConnected();
    }

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
            Log.d("TEST", e.getMessage());
        }

        return object;
    }

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
            Log.d("TEST", e.getMessage());
        }
    }
}
