package se.umu.visi0009.comiccollector.other;

import java.io.Serializable;
import java.util.Date;

public class CharacterCountHelper implements Serializable {

    private Date lastUpdated;
    private int characterCount;

    public CharacterCountHelper(Date lastUpdated, int characterCount) {
        this.lastUpdated = lastUpdated;
        this.characterCount = characterCount;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public int getCharacterCount() {
        return characterCount;
    }
}
