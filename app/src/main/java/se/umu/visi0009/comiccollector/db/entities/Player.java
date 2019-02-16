package se.umu.visi0009.comiccollector.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Defines the 'players' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Entity(tableName = "players")
public class Player {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    public Player() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
