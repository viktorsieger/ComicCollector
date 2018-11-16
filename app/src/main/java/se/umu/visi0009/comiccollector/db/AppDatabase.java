package se.umu.visi0009.comiccollector.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Date;

import se.umu.visi0009.comiccollector.AppExecutors;
import se.umu.visi0009.comiccollector.db.converter.DataTypeConverter;
import se.umu.visi0009.comiccollector.db.dao.AchievementDAO;
import se.umu.visi0009.comiccollector.db.dao.CardDAO;
import se.umu.visi0009.comiccollector.db.dao.CharacterDAO;
import se.umu.visi0009.comiccollector.db.dao.PlayerDAO;
import se.umu.visi0009.comiccollector.db.entities.Achievement;
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.db.entities.Player;
import se.umu.visi0009.comiccollector.enums.AchievementDifficulty;

@Database(entities = {Player.class, Character.class, Card.class, Achievement.class}, version = 1, exportSchema = false)
@TypeConverters({DataTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final long ACHIEVEMENT_DATE_LONG_DEFAULT = 0;
    private static final String DATABASE_NAME = "comicCollectorDatabase";

    private static volatile AppDatabase sInstance = null;

    public abstract PlayerDAO playerDAO();
    public abstract CharacterDAO characterDAO();
    public abstract CardDAO cardDAO();
    public abstract AchievementDAO achievementDAO();

    public static AppDatabase getInstance(final Context context, final AppExecutors executors) {
        if(sInstance == null) {
            synchronized(AppDatabase.class) {
                if(sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                }
            }
        }
        return sInstance;
    }

    private static AppDatabase buildDatabase(final Context appContext, final AppExecutors executors) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        executors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                //Pre-populate database
                                final AppDatabase database = AppDatabase.getInstance(appContext, executors);

                                database.runInTransaction(new Runnable() {
                                    @Override
                                    public void run() {
                                        long playerId = database.playerDAO().insertPlayer(new Player());

                                        Achievement achievement1 = new Achievement(new BigDecimal(playerId).intValueExact(), "Novice collector", "Collect 3 card.", AchievementDifficulty.VERY_EASY, new Date(ACHIEVEMENT_DATE_LONG_DEFAULT));
                                        Achievement achievement2 = new Achievement(new BigDecimal(playerId).intValueExact(), "\\u221A100", "Collect 10 cards.", AchievementDifficulty.EASY, new Date(ACHIEVEMENT_DATE_LONG_DEFAULT));
                                        Achievement achievement3 = new Achievement(new BigDecimal(playerId).intValueExact(), "CXI", "Collect 111 cards.", AchievementDifficulty.MEDIUM, new Date(ACHIEVEMENT_DATE_LONG_DEFAULT));
                                        Achievement achievement4 = new Achievement(new BigDecimal(playerId).intValueExact(), "The number of the beast", "Collect 666 cards.", AchievementDifficulty.HARD, new Date(ACHIEVEMENT_DATE_LONG_DEFAULT));
                                        Achievement achievement5 = new Achievement(new BigDecimal(playerId).intValueExact(), "Elite collector", "Collect 1337 cards.", AchievementDifficulty.VERY_HARD, new Date(ACHIEVEMENT_DATE_LONG_DEFAULT));

                                        database.achievementDAO().insertAchievements(achievement1, achievement2, achievement3, achievement4, achievement5);
                                    }
                                });
                            }
                        });
                    }
                }).build();
    }
}
