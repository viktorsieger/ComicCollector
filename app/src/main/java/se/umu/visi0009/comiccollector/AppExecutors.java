package se.umu.visi0009.comiccollector;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executors for background tasks.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AppExecutors {

    private final Executor mBackgroundThreads;

    /**
     * Constructor for the class. Initializes the Executor attribute.
     *
     * @param backgroundThreads     Executor to assign to the object.
     */
    private AppExecutors(Executor backgroundThreads) {
        this.mBackgroundThreads = backgroundThreads;
    }

    /**
     * Constructor for the class. Calls the private constructor with the
     * Executor to use.
     */
    public AppExecutors() {
        this(Executors.newFixedThreadPool(2));
    }

    /**
     * Accessor method for the executor.
     *
     * @return      The object's executor.
     */
    public Executor backgroundThreads() {
        return mBackgroundThreads;
    }
}
