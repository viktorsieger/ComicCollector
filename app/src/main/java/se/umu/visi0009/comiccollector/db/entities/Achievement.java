package se.umu.visi0009.comiccollector.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import se.umu.visi0009.comiccollector.enums.AchievementDifficulty;

@Entity(tableName = "achievements",
        foreignKeys = @ForeignKey(entity = Player.class,
                                  parentColumns = "id",
                                  childColumns = "player_id",
                                  onDelete = ForeignKey.CASCADE,
                                  onUpdate = ForeignKey.CASCADE))
public class Achievement {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "player_id", index = true)
    private int playerID;

    @ColumnInfo(name = "name", index = true)
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "difficulty", index = true)
    private AchievementDifficulty difficulty;

    @ColumnInfo(name = "date_completed", index = true)
    private Date date_completed;

    public Achievement() {

    }

    @Ignore
    public Achievement(int playerID, String name, String description, AchievementDifficulty difficulty, Date date_completed) {
        this.playerID = playerID;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.date_completed = date_completed;
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
