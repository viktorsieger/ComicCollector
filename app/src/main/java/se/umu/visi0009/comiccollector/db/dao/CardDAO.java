package se.umu.visi0009.comiccollector.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import se.umu.visi0009.comiccollector.db.entities.Card;

@Dao
public interface CardDAO {

    @Insert
    void insertCards(Card... cards);

    @Update
    void updateCards(Card... cards);

    @Delete
    void deleteCards(Card... cards);

    @Query("SELECT * FROM cards WHERE id = :cardId")
    Card loadCard(int cardId);

    @Query("SELECT cards.* FROM cards, characters WHERE cards.character_id = characters.id ORDER BY :columnToSortBy ASC")
    List<Card> loadCardsSortedAscendingOrder(String columnToSortBy);

    @Query("SELECT cards.* FROM cards, characters WHERE cards.character_id = characters.id ORDER BY :columnToSortBy DESC")
    List<Card> loadCardsSortedDescendingOrder(String columnToSortBy);

    @Query("SELECT cards.* FROM cards, characters WHERE (cards.character_id = characters.id) AND (characters.name LIKE '%' || :phraseToFilterBy || '%') ORDER BY :columnToSortBy ASC")
    List<Card> loadCardsFilteredSortedAscendingOrder(String phraseToFilterBy, String columnToSortBy);

    @Query("SELECT cards.* FROM cards, characters WHERE (cards.character_id = characters.id) AND (characters.name LIKE '%' || :phraseToFilterBy || '%') ORDER BY :columnToSortBy DESC")
    List<Card> loadCardsFilteredSortedDescendingOrder(String phraseToFilterBy, String columnToSortBy);

    @Query("SELECT COUNT(*) FROM cards")
    int size();
}
