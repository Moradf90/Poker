package apps.morad.com.poker.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Morad on 12/16/2015.
 */
@Table(name = "MemberInGame")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberInGame extends Model {

    public static final int NOT_ORDERED_MEMBER = -1;
    public static final String FB_ID_COLUMN = "fbid";
    public static final String GAME_ID_COLUMN = "gameid";
    public static final String ORDER_ID_COLUMN = "orderId";
    public static final String SCORE_COLUMN = "score";
    public static final String ROUND_COLUMN = "round";

    @Column(name = FB_ID_COLUMN, index = true)
            @JsonProperty(FB_ID_COLUMN)
    String fbId;
    @Column(name = GAME_ID_COLUMN, index = true)
    @JsonProperty(GAME_ID_COLUMN)
    String gameId;
    @Column(name = ORDER_ID_COLUMN)
    @JsonProperty(ORDER_ID_COLUMN)
    int order;
    @Column(name = SCORE_COLUMN)
    @JsonProperty(SCORE_COLUMN)
    int score;
    @Column(name = ROUND_COLUMN)
    @JsonProperty(ROUND_COLUMN)
    int round;

    public MemberInGame(){
        super();
    }
    public MemberInGame(String fbId, String gameId) {
        super();
        this.fbId = fbId;
        this.gameId = gameId;
        this.order = NOT_ORDERED_MEMBER;
        this.score = 0;
        this.round = 1;
    }

    @JsonIgnore
    public String getFbId() {
        return fbId;
    }

    @JsonIgnore
    public String getGameId() {
        return gameId;
    }

    @JsonIgnore
    public int getOrder() {
        return order;
    }

    @JsonIgnore
    public int getScore() {
        return score;
    }

    @JsonIgnore
    public int getRound() {
        return round;
    }

    @JsonIgnore
    public MemberInGame setScore(int score){
        this.score = score;
        return this;
    }

    @JsonIgnore
    public MemberInGame setOrder(int order){
        this.order = order;
        return this;
    }

    @JsonIgnore
    public MemberInGame setRound(int round){
        this.round = round;
        return this;
    }
}
