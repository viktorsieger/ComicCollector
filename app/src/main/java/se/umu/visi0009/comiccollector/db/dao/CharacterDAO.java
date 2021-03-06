package se.umu.visi0009.comiccollector.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

import se.umu.visi0009.comiccollector.db.entities.Character;

/**
 * Data access object for the 'characters' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Dao
public interface CharacterDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCharacters(Character... characters);

    @Update
    void updateCharacters(Character... characters);

    @Query("SELECT * FROM characters WHERE id = :characterId")
    Character loadCharacter(int characterId);

    @Query("SELECT * FROM characters WHERE last_updated < :date")
    List<Character> loadCharactersNotUpdatedAfter(Date date);

    @Query("SELECT * FROM characters ORDER BY name ASC")
    LiveData<List<Character>> loadCharacters();

    @Query("SELECT * FROM characters WHERE id = :characterID")
    LiveData<Character> loadCharacterByID(int characterID);
}
