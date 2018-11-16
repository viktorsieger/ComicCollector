package se.umu.visi0009.comiccollector.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import se.umu.visi0009.comiccollector.db.entities.Player;

@Dao
public interface PlayerDAO {

    @Insert
    long insertPlayer(Player player);

    @Delete
    void deletePlayer(Player player);

    @Query("SELECT * FROM players LIMIT 1")
    Player loadPlayerFirst();

    @Query("SELECT * FROM players WHERE id = :playerId")
    Player loadPlayer(int playerId);

    @Query("SELECT * FROM players ORDER BY id ASC")
    List<Player> loadPlayers();

    @Query("SELECT COUNT(*) FROM players")
    int size();
}
