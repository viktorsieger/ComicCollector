package se.umu.visi0009.comiccollector.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import se.umu.visi0009.comiccollector.other.enums.CardCondition;

/**
 * Defines the 'cards' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Entity(tableName = "cards",
        foreignKeys = {@ForeignKey(entity = Player.class,
                                   parentColumns = "id",
                                   childColumns = "player_id",
                                   onDelete = ForeignKey.CASCADE,
                                   onUpdate = ForeignKey.CASCADE),
                       @ForeignKey(entity = Character.class,
                                   parentColumns = "id",
                                   childColumns = "character_id",
                                   onDelete = ForeignKey.RESTRICT,
                                   onUpdate = ForeignKey.CASCADE)})
public class Card {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "player_id", index = true)
    private int playerID;

    @ColumnInfo(name = "character_id", index = true)
    private int characterID;

    @ColumnInfo(name = "condition", index = true)
    private CardCondition condition;

    @ColumnInfo(name = "date_found", index = true)
    private Date dateFound;

    public Card() {

    }

    @Ignore
    public Card(int playerID, int characterID, CardCondition condition, Date dateFound) {
        this.playerID = playerID;
        this.characterID = characterID;
        this.condition = condition;
        this.dateFound = dateFound;
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
