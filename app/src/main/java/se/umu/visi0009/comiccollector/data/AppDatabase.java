package se.umu.visi0009.comiccollector.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import se.umu.visi0009.comiccollector.entities.Achievement;
import se.umu.visi0009.comiccollector.entities.Card;
import se.umu.visi0009.comiccollector.entities.Character;
import se.umu.visi0009.comiccollector.entities.Player;

@Database(entities = {Player.class, Character.class, Card.class, Achievement.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    abstract public PlayerDAO playerDAO();
    abstract public CharacterDAO characterDAO();
    abstract public CardDAO cardDAO();
    abstract public AchievementDAO achievementDAO();
}
