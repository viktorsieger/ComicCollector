package se.umu.visi0009.comiccollector.other;

import java.io.Serializable;
import java.util.Date;

/**
 * Used to keep track of available characters in Marvel's API.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
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
