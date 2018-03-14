package se.umu.visi0009.comiccollector.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import se.umu.visi0009.comiccollector.entities.Achievement;

@Dao
public interface AchievementDAO {

    @Insert
    public void insertAchievements(Achievement... achievements);

    @Update
    public void updateAchievements(Achievement... achievements);

    @Query("SELECT * FROM achievements")
    public Achievement[] loadAllAchievements();
}
