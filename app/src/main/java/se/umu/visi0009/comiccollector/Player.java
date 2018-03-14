package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "players",
        indices = {@Index("name"), @Index(value = {"id", "travel_distance_in_kilometers"})})
public class Player {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "travel_distance_in_kilometers")
    public double travelDistanceInKilometers;

    public Player() {

    }
}
