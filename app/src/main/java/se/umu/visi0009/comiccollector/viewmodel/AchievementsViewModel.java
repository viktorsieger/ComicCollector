package se.umu.visi0009.comiccollector.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.db.entities.Achievement;

/**
 * Used by AchievementsFragment to store and manage UI-related data.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AchievementsViewModel extends AndroidViewModel {

    private final LiveData<List<Achievement>> mAchievements;

    /**
     * Constructor for the class. Loads the achievements.
     *
     * @param application   An application.
     */
    public AchievementsViewModel(Application application) {
        super(application);
        mAchievements = ((ComicCollectorApp)application).getRepository().loadAchievements();
    }

    /**
     * Accessor method for the achievements.
     *
     * @return      The view model's achievements.
     */
    public LiveData<List<Achievement>> getAchievements() {
        return mAchievements;
    }
}
