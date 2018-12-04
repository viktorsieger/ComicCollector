package se.umu.visi0009.comiccollector.other;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.DataRepository;
import se.umu.visi0009.comiccollector.db.entities.Character;

public class CollectionViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final LiveData<List<Character>> mCharacters;

    public CollectionViewModel(Application application) {
        super(application);

        mRepository = ((ComicCollectorApp)application).getRepository();
        mCharacters = mRepository.loadCharacters();
    }

    public LiveData<List<Character>> getCharacters() {
        return mCharacters;
    }
}
