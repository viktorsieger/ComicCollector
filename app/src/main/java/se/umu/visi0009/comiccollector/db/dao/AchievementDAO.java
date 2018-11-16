package se.umu.visi0009.comiccollector.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import se.umu.visi0009.comiccollector.db.entities.Achievement;

@Dao
public interface AchievementDAO {

    @Insert
    void insertAchievements(Achievement... achievements);

    @Update
    void updateAchievements(Achievement... achievements);

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    Achievement loadAchievement(int achievementId);

    @Query("SELECT * FROM achievements ORDER BY :columnToSortBy ASC")
    List<Achievement> loadCardsSortedAscendingOrder(String columnToSortBy);

    @Query("SELECT * FROM achievements ORDER BY :columnToSortBy DESC")
    List<Achievement> loadCardsSortedDescendingOrder(String columnToSortBy);
}
