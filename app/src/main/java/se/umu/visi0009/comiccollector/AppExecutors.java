package se.umu.visi0009.comiccollector;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    private final Executor mDiskIO;
    private final Executor mNetworkIO;

    private AppExecutors(Executor diskIO, Executor networkIO) {
        this.mDiskIO = diskIO;
        this.mNetworkIO = networkIO;
    }

    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());
    }

    public Executor diskIO() {
        return mDiskIO;
    }

    public Executor networkIO() {
        return mNetworkIO;
    }
}
