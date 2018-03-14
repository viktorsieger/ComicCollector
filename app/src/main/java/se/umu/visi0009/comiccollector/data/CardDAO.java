package se.umu.visi0009.comiccollector.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import se.umu.visi0009.comiccollector.entities.Card;

@Dao
public interface CardDAO {

    @Insert
    public void insertCards(Card... cards);

    @Update
    public void updateCards(Card... cards);

    @Query("SELECT * FROM cards")
    public Card[] loadAllCards();
}
