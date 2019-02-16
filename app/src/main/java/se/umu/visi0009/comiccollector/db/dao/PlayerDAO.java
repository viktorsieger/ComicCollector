package se.umu.visi0009.comiccollector.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import se.umu.visi0009.comiccollector.db.entities.Player;

/**
 * Data access object for the 'players' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Dao
public interface PlayerDAO {

    @Insert
    long insertPlayer(Player player);

    @Query("SELECT * FROM players LIMIT 1")
    Player loadPlayerFirst();
}
