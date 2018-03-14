package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "achievements",
        indices = {@Index("name"), @Index(value = {"id", "player_id", "name", "description", "difficulty", "date_completed"})},
        foreignKeys = @ForeignKey(entity = Player.class, parentColumns = "id", childColumns = "player_id"))
public class Achievement {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "player_id")
    public int playerID;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "difficulty")
    public AchievementDifficulty difficulty;

    @ColumnInfo(name = "date_completed")
    public Date date_completed;

    public Achievement() {

    }
}
