package apps.morad.com.poker.models;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONObject;

/**
 * Created by Morad on 12/12/2015.
 */
@Table(name = "Members", id = BaseColumns._ID)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member extends Model{

    public static final String FB_ID_COLUMN = "fbid";
    public static final String FIRST_NAME_COLUMN = "firstName";
    public static final String LAST_NAME_COLUMN = "lastName";
    public static final String SCORE_COLUMN = "score";
    public static final String MEDAL_COLUMN = "medal";
    public static final String IS_ADMIN_COLUMN = "isAdmin";
    public static final String REMAINING_GAMES = "games";


    @JsonProperty(FB_ID_COLUMN)
    @Column(name = FB_ID_COLUMN, index = true)
    String fbid;
    @JsonProperty(FIRST_NAME_COLUMN)
    @Column(name = FIRST_NAME_COLUMN)
    String firstName;
    @JsonProperty(LAST_NAME_COLUMN)
    @Column(name = LAST_NAME_COLUMN)
    String lastName;
    @JsonProperty(SCORE_COLUMN)
    @Column(name = SCORE_COLUMN)
    int score;
    @JsonProperty(MEDAL_COLUMN)
    @Column(name = MEDAL_COLUMN)
    String medal;
    @JsonProperty(IS_ADMIN_COLUMN)
    @Column(name = IS_ADMIN_COLUMN)
    boolean isAdmin;

    @JsonProperty(REMAINING_GAMES)
    @Column(name = REMAINING_GAMES)
    int remainingGame;

    public Member(){
        super();
    }

    public Member(String _id, String _firstName, String _lastName, int score, String medal, boolean _isAdmin, int _games) {
        this(_id,_firstName,_lastName,score,medal);
        isAdmin = _isAdmin;
        remainingGame = _games;
    }

    public Member(String _id, String _firstName, String _lastName, int score, String medal) {
        super();
        this.fbid = _id;
        this.firstName = _firstName;
        this.lastName = _lastName;
        this.score = score;
        this.medal = medal;
    }

    @JsonIgnore
    public String getFbId() {
        return fbid;
    }

    @JsonIgnore
    public String getFirstName() {
        return firstName;
    }

    @JsonIgnore
    public String getLastName() {
        return lastName;
    }

    @JsonIgnore
    public int getScore(){
        return score;
    }

    @JsonIgnore
    public String getMedal()
    {
        return medal;
    }

    @JsonIgnore
    public boolean getIsAdmin() { return isAdmin; }

    public void updateFromJson(JSONObject obj) {
        try {
            if (obj.has(MEDAL_COLUMN)) {
                this.medal = obj.getString(MEDAL_COLUMN);
            }
            if (obj.has(IS_ADMIN_COLUMN)){
                this.isAdmin = obj.getBoolean(IS_ADMIN_COLUMN);
            }
            if (obj.has(SCORE_COLUMN)){
                this.score = obj.getInt(SCORE_COLUMN);
            }
            if (obj.has(FIRST_NAME_COLUMN)){
                this.firstName = obj.getString(FIRST_NAME_COLUMN);
            }
            if (obj.has(LAST_NAME_COLUMN)){
                this.lastName = obj.getString(LAST_NAME_COLUMN);
            }
            if (obj.has(REMAINING_GAMES)){
                this.remainingGame = obj.getInt(REMAINING_GAMES);
            }
        }
        catch (Exception e){}
    }
}
