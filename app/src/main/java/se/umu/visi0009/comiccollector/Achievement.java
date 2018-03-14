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
    private int id;

    @ColumnInfo(name = "player_id")
    private int playerID;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "difficulty")
    private AchievementDifficulty difficulty;

    @ColumnInfo(name = "date_completed")
    private Date date_completed;

    public Achievement() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AchievementDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(AchievementDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Date getDate_completed() {
        return date_completed;
    }

    public void setDate_completed(Date date_completed) {
        this.date_completed = date_completed;
    }
}
