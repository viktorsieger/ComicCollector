package se.umu.visi0009.comiccollector.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import se.umu.visi0009.comiccollector.enums.CardCondition;

@Entity(tableName = "cards",
        indices = {@Index(value = {"player_id", "id"}), @Index(value = {"character_id", "id"})},
        foreignKeys = {@ForeignKey(entity = Player.class, parentColumns = "id", childColumns = "player_id", onDelete = ForeignKey.CASCADE),
                       @ForeignKey(entity = Character.class, parentColumns = "id", childColumns = "character_id")})
public class Card {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "player_id")
    private int playerID;

    @ColumnInfo(name = "character_id")
    private int characterID;

    @ColumnInfo(name = "condition")
    private CardCondition condition;

    @ColumnInfo(name = "date_found")
    private Date dateFound;

    public Card() {

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

    public int getCharacterID() {
        return characterID;
    }

    public void setCharacterID(int characterID) {
        this.characterID = characterID;
    }

    public CardCondition getCondition() {
        return condition;
    }

    public void setCondition(CardCondition condition) {
        this.condition = condition;
    }

    public Date getDateFound() {
        return dateFound;
    }

    public void setDateFound(Date dateFound) {
        this.dateFound = dateFound;
    }
}
