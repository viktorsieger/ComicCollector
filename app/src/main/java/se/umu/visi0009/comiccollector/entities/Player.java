package se.umu.visi0009.comiccollector.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "players",
        indices = {@Index("name"), @Index(value = {"id", "travel_distance_in_kilometers"})})
public class Player {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "travel_distance_in_kilometers")
    private double travelDistanceInKilometers;

    public Player() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTravelDistanceInKilometers() {
        return travelDistanceInKilometers;
    }

    public void setTravelDistanceInKilometers(double travelDistanceInKilometers) {
        this.travelDistanceInKilometers = travelDistanceInKilometers;
    }
}
