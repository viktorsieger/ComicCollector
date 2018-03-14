package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface PlayerDAO {

    @Insert
    public void insertPlayers(Player... players);

    @Update
    public void updatePlayers(Player... players);

    @Query("SELECT * FROM players")
    public Player[] loadAllPlayers();
}
