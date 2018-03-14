package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface CharacterDAO {

    @Insert
    public void insertCharacters(Character... characters);

    @Update
    public void updateCharacters(Character... characters);

    @Query("SELECT * FROM characters")
    public Character[] loadAllCharacters();
}
