package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface AchievementDAO {

    @Insert
    public void insertAchievements(Achievement... achievements);

    @Update
    public void updateAchievements(Achievement... achievements);

    @Query("SELECT * FROM achievements")
    public Achievement[] loadAllAchievements();
}
