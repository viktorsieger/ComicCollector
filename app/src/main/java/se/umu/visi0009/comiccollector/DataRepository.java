package se.umu.visi0009.comiccollector;

import java.util.Date;
import java.util.List;

import se.umu.visi0009.comiccollector.db.AppDatabase;
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.db.entities.Player;

public class DataRepository {

    private static volatile DataRepository sInstance = null;

    private final AppDatabase mDatabase;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
    }

    public static DataRepository getsInstance(final AppDatabase database) {
        if(sInstance == null) {
            synchronized(DataRepository.class) {
                if(sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }




    public void insertCharacter(Character character) {
        mDatabase.characterDAO().insertCharacters(character);
    }

    public void updateCharacter(Character character) {
        mDatabase.characterDAO().updateCharacters(character);
    }

    public boolean isCharacterInDatabase(int characterID) {
        return (mDatabase.characterDAO().loadCharacter(characterID) != null);
    }

    public Player loadPlayer() {
        return mDatabase.playerDAO().loadPlayerFirst();
    }

    public void insertCard(Card card) {
        mDatabase.cardDAO().insertCards(card);
    }

    public List<Character> loadCharactersNotUpdatedAfter(Date date) {
        return mDatabase.characterDAO().loadCharactersNotUpdatedAfter(date);
    }
}
