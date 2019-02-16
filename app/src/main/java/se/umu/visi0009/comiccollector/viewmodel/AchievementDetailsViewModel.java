package se.umu.visi0009.comiccollector.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.DataRepository;
import se.umu.visi0009.comiccollector.db.entities.Achievement;

/**
 * Used by AchievementDetailsFragment to store and manage UI-related data.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AchievementDetailsViewModel extends AndroidViewModel {

    private final LiveData<Achievement> mAchievement;

    /**
     * Constructor for the class. Loads the achievement.
     *
     * @param application       An application.
     * @param dataRepository    A DataRepository with access to achievements.
     * @param achievementID     The ID of the achievement to load.
     */
    private AchievementDetailsViewModel(@NonNull Application application, DataRepository dataRepository, final int achievementID) {
        super(application);
        mAchievement = dataRepository.loadAchievement(achievementID);
    }

    /**
     * Accessor method for the achievement.
     *
     * @return      The view model's achievement.
     */
    public LiveData<Achievement> getAchievement() {
        return mAchievement;
    }

    /**
     * Factory for the view model class.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mAchievementID;
        private final DataRepository mRepository;

        /**
         * Constructor for the class. Initializes attributes.
         *
         * @param application       An application.
         * @param achievementID     The ID of the achievement to be used.
         */
        public Factory(@NonNull Application application, int achievementID) {
            mApplication = application;
            mAchievementID = achievementID;
            mRepository = ((ComicCollectorApp)application).getRepository();
        }

        /**
         * Creates a new instance of the given Class.
         *
         * @param modelClass    A Class whose instance is requested.
         * @return              A newly created ViewModel.
         */
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AchievementDetailsViewModel(mApplication, mRepository, mAchievementID);
        }
    }
}
