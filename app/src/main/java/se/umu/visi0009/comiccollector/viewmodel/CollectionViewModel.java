package se.umu.visi0009.comiccollector.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.db.entities.Character;

/**
 * Used by CollectionFragment to store and manage UI-related data.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class CollectionViewModel extends AndroidViewModel {

    private final LiveData<List<Character>> mCharacters;

    /**
     * Constructor for the class. Loads the characters.
     *
     * @param application   An application.
     */
    public CollectionViewModel(Application application) {
        super(application);
        mCharacters = ((ComicCollectorApp)application).getRepository().loadCharacters();
    }

    /**
     * Accessor method for the characters.
     *
     * @return      The view model's characters.
     */
    public LiveData<List<Character>> getCharacters() {
        return mCharacters;
    }
}
