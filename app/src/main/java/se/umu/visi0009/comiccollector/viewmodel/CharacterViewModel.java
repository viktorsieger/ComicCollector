package se.umu.visi0009.comiccollector.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.List;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.DataRepository;
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;

/**
 * Used by CharacterDetailsFragment to store and manage UI-related data.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class CharacterViewModel extends AndroidViewModel {

    private final LiveData<Character> mCharacter;
    private final LiveData<List<Card>> mCards;

    /**
     * Constructor for the class. Loads the character and the cards depicting
     * the character.
     *
     * @param application       An application.
     * @param dataRepository    A DataRepository with access to characters and
     *                          cards.
     * @param characterID       The ID of the affected character.
     */
    private CharacterViewModel(@NonNull Application application, DataRepository dataRepository, final int characterID) {
        super(application);
        mCharacter = dataRepository.loadCharacter(characterID);
        mCards = dataRepository.loadCardsWithCharacter(characterID);
    }

    /**
     * Accessor method for the character.
     *
     * @return      The view model's character.
     */
    public LiveData<Character> getCharacter() {
        return mCharacter;
    }

    /**
     * Accessor method for the cards.
     *
     * @return      The view model's cards.
     */
    public LiveData<List<Card>> getCards() {
        return mCards;
    }

    /**
     * Factory for the view model class.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mCharacterID;
        private final DataRepository mRepository;

        /**
         * Constructor for the class. Initializes attributes.
         *
         * @param application       An application.
         * @param characterID       The ID of the character to be used.
         */
        public Factory(@NonNull Application application, int characterID) {
            mApplication = application;
            mCharacterID = characterID;
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
            return (T) new CharacterViewModel(mApplication, mRepository, mCharacterID);
        }
    }
}
