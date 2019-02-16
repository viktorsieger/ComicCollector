package se.umu.visi0009.comiccollector.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Defines the 'characters' table in the database.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
@Entity(tableName = "characters")
public class Character {

    @PrimaryKey
    @ColumnInfo(name = "id", index = true)
    private int id;

    @ColumnInfo(name = "last_updated", index = true)
    private Date lastUpdated;

    @ColumnInfo(name = "name", index = true)
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "path_local_image")
    private String pathLocalImage;

    @ColumnInfo(name = "comics")
    private ArrayList<String> comics;

    @ColumnInfo(name = "urlWiki")
    private String urlWiki;

    public Character() {

    }

    @Ignore
    public Character(int id, Date lastUpdated, String name, String description, String pathLocalImage, ArrayList<String> comics, String urlWiki) {
        this.id = id;
        this.lastUpdated = lastUpdated;
        this.name = name;
        this.description = description;
        this.pathLocalImage = pathLocalImage;
        this.comics = comics;
        this.urlWiki = urlWiki;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
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

    public String getPathLocalImage() {
        return pathLocalImage;
    }

    public void setPathLocalImage(String pathLocalImage) {
        this.pathLocalImage = pathLocalImage;
    }

    public ArrayList<String> getComics() {
        return comics;
    }

    public void setComics(ArrayList<String> comics) {
        this.comics = comics;
    }

    public String getUrlWiki() {
        return urlWiki;
    }

    public void setUrlWiki(String urlWiki) {
        this.urlWiki = urlWiki;
    }

    @Ignore
    public Bitmap getCharacterImage() {
        return BitmapFactory.decodeFile(pathLocalImage);
    }
}
