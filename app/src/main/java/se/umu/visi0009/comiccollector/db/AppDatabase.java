package se.umu.visi0009.comiccollector.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

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
import se.umu.visi0009.comiccollector.other.enums.AchievementDifficulty;

/**
 * Holds the database instance and methods to create and retrieve the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Database(entities = {Player.class, Character.class, Card.class, Achievement.class},
          version = 1,
          exportSchema = false)
@TypeConverters({DataTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "comicCollectorDatabase";

    private static volatile AppDatabase sInstance = null;

    public abstract PlayerDAO playerDAO();
    public abstract CharacterDAO characterDAO();
    public abstract CardDAO cardDAO();
    public abstract AchievementDAO achievementDAO();

    /**
     * Static method used to get the database singleton (or create the database
     * if no database exists). The method uses lazy initialization and is
     * thread-safe.
     *
     * @param context       The context for the database.
     * @param executors     Executors that perform background tasks.
     * @return              The database.
     */
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

    /**
     * Creates a database and populates it with data.
     *
     * @param appContext    The context for the database.
     * @param executors     Executors that perform background tasks.
     * @return              The created database.
     */
    private static AppDatabase buildDatabase(final Context appContext, final AppExecutors executors) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        executors.backgroundThreads().execute(new Runnable() {
                            @Override
                            public void run() {
                                //Pre-populate database
                                final AppDatabase database = AppDatabase.getInstance(appContext, executors);

                                database.runInTransaction(new Runnable() {
                                    @Override
                                    public void run() {
                                        long playerId = database.playerDAO().insertPlayer(new Player());

                                        Achievement achievement1 = new Achievement(new BigDecimal(playerId).intValueExact(), "Novice collector", "Collect 3 cards.", AchievementDifficulty.VERY_EASY, Achievement.DATE_INCOMPLETE);
                                        Achievement achievement2 = new Achievement(new BigDecimal(playerId).intValueExact(), "1010", "Collect 10 cards.", AchievementDifficulty.EASY, Achievement.DATE_INCOMPLETE);
                                        Achievement achievement3 = new Achievement(new BigDecimal(playerId).intValueExact(), "CXI", "Collect 111 cards.", AchievementDifficulty.MEDIUM, Achievement.DATE_INCOMPLETE);
                                        Achievement achievement4 = new Achievement(new BigDecimal(playerId).intValueExact(), "The number of the beast", "Collect 666 cards.", AchievementDifficulty.HARD, Achievement.DATE_INCOMPLETE);
                                        Achievement achievement5 = new Achievement(new BigDecimal(playerId).intValueExact(), "Elite collector", "Collect 1337 cards.", AchievementDifficulty.VERY_HARD, Achievement.DATE_INCOMPLETE);

                                        database.achievementDAO().insertAchievements(achievement1, achievement2, achievement3, achievement4, achievement5);
                                    }
                                });
                            }
                        });
                    }
                }).build();
    }
}
