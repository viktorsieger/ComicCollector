package se.umu.visi0009.comiccollector;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;
import java.util.List;

@Entity(tableName = "characters",
        indices = {@Index("name"), @Index(value = {"id", "name", "description", "thumbnail_path", "comics", "last_updated"})})
public class Character {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "thumbnail_path")
    public String thumbnailPath;

    @ColumnInfo(name = "comics")
    public List<String> comics;

    @ColumnInfo(name = "last_updated")
    public Date lastUpdated;

    @Ignore
    public Character(int id, String name, String description, String thumbnailPath, List<String> comics, Date lastUpdated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.comics = comics;
        this.lastUpdated = lastUpdated;
    }
}
