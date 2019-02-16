package se.umu.visi0009.comiccollector.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import se.umu.visi0009.comiccollector.db.entities.Card;

/**
 * Data access object for the 'cards' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Dao
public interface CardDAO {

    @Insert
    void insertCards(Card... cards);

    @Query("SELECT * FROM cards WHERE character_id = :characterID")
    LiveData<List<Card>> loadCardsWithCharacter(int characterID);

    @Query("SELECT COUNT(*) FROM cards")
    int getNumberOfCards();
}
