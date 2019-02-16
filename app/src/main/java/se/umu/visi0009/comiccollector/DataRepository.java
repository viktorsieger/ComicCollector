package se.umu.visi0009.comiccollector;

import android.arch.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import se.umu.visi0009.comiccollector.db.AppDatabase;
import se.umu.visi0009.comiccollector.db.entities.Achievement;
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.db.entities.Player;

/**
 * Repository handling communication with the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class DataRepository {

    private static volatile DataRepository sInstance = null;

    private final AppDatabase mDatabase;

    /**
     * Constructor for the class. Initializes the AppDatabase attribute.
     *
     * @param database      Database to assign to the object.
     */
    private DataRepository(final AppDatabase database) {
        mDatabase = database;
    }

    /**
     * Static method used to get the repository singleton (or create the
     * repository if no repository exists). The method uses lazy initialization
     * and is thread-safe.
     *
     * @param database      Database for the repository to communicate with.
     * @return              The repository.
     */
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

    /**
     * Inserts a character into the database.
     *
     * @param character     The character to insert into the database.
     */
    public void insertCharacter(Character character) {
        mDatabase.characterDAO().insertCharacters(character);
    }

    /**
     * Updates an existing character in the database.
     *
     * @param character     The character with new information.
     */
    public void updateCharacter(Character character) {
        mDatabase.characterDAO().updateCharacters(character);
    }

    /**
     * Checks whether a character with the given ID is in the database.
     *
     * @param characterID       ID of the character to find.
     * @return                  True if character is in the database.
     */
    public boolean isCharacterInDatabase(int characterID) {
        return (mDatabase.characterDAO().loadCharacter(characterID) != null);
    }

    /**
     * Returns the first player from the database.
     *
     * @return      The first player from the database.
     */
    public Player loadPlayer() {
        return mDatabase.playerDAO().loadPlayerFirst();
    }

    /**
     * Inserts a card into the database.
     *
     * @param card      The card to insert into the database.
     */
    public void insertCard(Card card) {
        mDatabase.cardDAO().insertCards(card);
    }

    /**
     * Returns a list of character from the database that have not been updated
     * after the given date.
     *
     * @param date      A date.
     * @return          A list of characters from the database that have not
     * been updated after the given date.
     */
    public List<Character> loadCharactersNotUpdatedAfter(Date date) {
        return mDatabase.characterDAO().loadCharactersNotUpdatedAfter(date);
    }

    /**
     * Retrieves a list of all characters in the database.
     *
     * @return      A list of all characters in the database.
     */
    public LiveData<List<Character>> loadCharacters() {
        return mDatabase.characterDAO().loadCharacters();
    }

    /**
     * Retrieves a character in the database with the given ID.
     *
     * @param characterID       ID of character to find.
     * @return                  A character with the given ID.
     */
    public LiveData<Character> loadCharacter(int characterID) {
        return mDatabase.characterDAO().loadCharacterByID(characterID);
    }

    /**
     * Retrieves a list of cards in the database depicting a character with the
     * given ID.
     *
     * @param characterID       ID of character that is depicted on the cards.
     * @return                  A list of cards in the database depicting the
     *                          character with the given ID.
     */
    public LiveData<List<Card>> loadCardsWithCharacter(int characterID) {
        return mDatabase.cardDAO().loadCardsWithCharacter(characterID);
    }

    /**
     * Retrieves a list of all achievements in the database.
     *
     * @return      A list of all achievements in the database.
     */
    public LiveData<List<Achievement>> loadAchievements() {
        return mDatabase.achievementDAO().loadAchievements();
    }

    /**
     * Retrieves an achievement in the database with the given ID.
     *
     * @param achievementID     ID of achievement to find.
     * @return                  An achievement with the given ID.
     */
    public LiveData<Achievement> loadAchievement(int achievementID) {
        return mDatabase.achievementDAO().loadAchievementByID(achievementID);
    }

    /**
     * Returns the number of cards in the database.
     *
     * @return      Number of cards in the database.
     */
    public int getNumberOfCards() {
        return mDatabase.cardDAO().getNumberOfCards();
    }

    /**
     * Returns an achievement in the database with the given name.
     *
     * @param achievementName       Name of achievement to find.
     * @return                      An achievement with the given name.
     */
    public Achievement loadAchievement(String achievementName) {
        return mDatabase.achievementDAO().loadAchievementByName(achievementName);
    }

    /**
     * Updates existing achievements in the database.
     *
     * @param achievements      The achievements with new information.
     */
    public void updateAchievements(Achievement... achievements) {
        mDatabase.achievementDAO().updateAchievements(achievements);
    }
}
