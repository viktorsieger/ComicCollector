package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Player.class, Character.class, Card.class, Achievement.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    abstract public PlayerDAO playerDAO();
    abstract public CharacterDAO characterDAO();
    abstract public CardDAO cardDAO();
    abstract public AchievementDAO achievementDAO();
}
