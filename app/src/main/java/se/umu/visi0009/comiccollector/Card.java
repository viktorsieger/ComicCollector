package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "cards",
        indices = {@Index("name"), @Index(value = {"id", "player_id", "character_id", "condition", "date_found"})},
        foreignKeys = {@ForeignKey(entity = Player.class, parentColumns = "id", childColumns = "player_id", onDelete = ForeignKey.CASCADE),
                       @ForeignKey(entity = Character.class, parentColumns = "id", childColumns = "character_id")})
public class Card {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "player_id")
    public int playerID;

    @ColumnInfo(name = "character_id")
    public int characterID;

    @ColumnInfo(name = "condition")
    public CardCondition condition;

    @ColumnInfo(name = "date_found")
    public Date dateFound;

    public Card() {

    }
}
