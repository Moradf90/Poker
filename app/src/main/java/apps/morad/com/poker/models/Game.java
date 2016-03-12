package apps.morad.com.poker.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONObject;

/**
 * Created by Morad on 12/16/2015.
 */

@Table(name = "Games")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game extends Model {

    public static final int MIN_NUMBER_OF_MEMBERS = 2;
    public static final int MIN_NUMBER_OF_MEMBERS_FOR_CONSIDERED_GAME = 7;
    public static final int NUMBER_OF_MEMBERS_GET_POINTS = 3;
    public static final String EVENT_ID_COLUMN = "eventid";
    public static final String GAME_ID_COLUMN = "gameid";
    public static final String GAME_NAME_COLUMN = "name";
    public static final String START_TIME_COLUMN = "startTime";
    public static final String END_TIME_COLUMN = "endTime";
    public static final String ORDER_ID_COLUMN = "orderId";
    public static final String IS_CONSIDERED_COLUMN = "isConsidered";

    public static final long NOT_STARTED_GAME_TIME = -1;
    public static final long NOT_FINISHED_GAME_TIME = -1;

    @JsonProperty(EVENT_ID_COLUMN)
    @Column(name = EVENT_ID_COLUMN, index = true)
    String eventId;
    @JsonProperty(GAME_ID_COLUMN)
    @Column(name = GAME_ID_COLUMN, index = true)
    String gameId;
    @JsonProperty(START_TIME_COLUMN)
    @Column(name = START_TIME_COLUMN)
    long startTime;
    @JsonProperty(END_TIME_COLUMN)
    @Column(name = END_TIME_COLUMN)
    long endTime;
    @JsonProperty(ORDER_ID_COLUMN)
    @Column(name = ORDER_ID_COLUMN)
    int order;
    @JsonProperty(IS_CONSIDERED_COLUMN)
    @Column(name = IS_CONSIDERED_COLUMN)
    boolean isConsidered;
    @JsonProperty(GAME_NAME_COLUMN)
    @Column(name = GAME_NAME_COLUMN)
    String name;

    public Game(){
        super();
    }
    public Game(String gameId ,String eventId, String name, boolean isConsidered, int order) {
        super();
        this.eventId = eventId;
        this.name = name;
        this.startTime = NOT_STARTED_GAME_TIME;
        this.endTime = NOT_FINISHED_GAME_TIME;
        this.isConsidered = isConsidered;
        this.order = order;
        this.gameId = gameId;
    }

    public Game(String gameId ,String eventId)
    {
        super();
        this.eventId = eventId;
        this.gameId = gameId;
    }

    @JsonIgnore
    public String getEventId() {
        return eventId;
    }

    @JsonIgnore
    public String getGameId() {
        return gameId;
    }

    @JsonIgnore
    public long getStartTime() {
        return startTime;
    }

    @JsonIgnore
    public long getEndTime() {
        return endTime;
    }

    @JsonIgnore
    public int getOrder() {
        return order;
    }

    @JsonIgnore
    public void updateFromJson(JSONObject obj){
        try {
            if (obj.has(GAME_NAME_COLUMN)) {
                this.name = obj.getString(GAME_NAME_COLUMN);
            }
            if (obj.has(START_TIME_COLUMN)){
                this.startTime = obj.getLong(START_TIME_COLUMN);
            }
            if (obj.has(END_TIME_COLUMN)){
                this.endTime = obj.getLong(END_TIME_COLUMN);
            }
            if (obj.has(ORDER_ID_COLUMN)){
                this.order = obj.getInt(ORDER_ID_COLUMN);
            }
            if (obj.has(IS_CONSIDERED_COLUMN)){
                this.isConsidered = obj.getBoolean(IS_CONSIDERED_COLUMN);
            }
        }
        catch (Exception e){}
    }

    @JsonIgnore
    public static int getScoreByOrder(int numberOfMembers, int order){

        int d;
        if((d = numberOfMembers - order) < NUMBER_OF_MEMBERS_GET_POINTS){
            switch (d){
                case 0 : return numberOfMembers*5 -10;
                case 1 : return numberOfMembers*3 -10;
                case 2 : return numberOfMembers*2 -10;
            }
        }
        else {
            return -10;
        }

        return 0;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }
}
