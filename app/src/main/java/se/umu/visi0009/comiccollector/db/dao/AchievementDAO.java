package se.umu.visi0009.comiccollector.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import se.umu.visi0009.comiccollector.db.entities.Achievement;

/**
 * Data access object for the 'achievements' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Dao
public interface AchievementDAO {

    @Insert
    void insertAchievements(Achievement... achievements);

    @Update
    void updateAchievements(Achievement... achievements);

    @Query("SELECT * FROM achievements ORDER BY name ASC")
    LiveData<List<Achievement>> loadAchievements();

    @Query("SELECT * FROM achievements WHERE id = :achievementID")
    LiveData<Achievement> loadAchievementByID(int achievementID);

    @Query("SELECT * FROM achievements WHERE name = :achievementName")
    Achievement loadAchievementByName(String achievementName);
}
