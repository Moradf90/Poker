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
@Table(name = "MembersInEvents")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberInEvent extends Model {

    public static final int ACCEPT_STATUS = 1;
    public static final int REJECT_STATUS = 2;
    public static final int NOT_SET_STATUS = -1;
    public static final int MAYBE_STATUS = 3;

    public static final String FB_ID_COLUMN = "fbid";
    public static final String EVENT_ID_COLUMN = "eventid";
    public static final String STATUS_COLUMN = "status";

    @Column(name = FB_ID_COLUMN, index = true)
    @JsonProperty(FB_ID_COLUMN)
    String fbId;
    @Column(name = EVENT_ID_COLUMN, index = true)
    @JsonProperty(EVENT_ID_COLUMN)
    String eventId;
    @Column(name = STATUS_COLUMN)
    @JsonProperty(STATUS_COLUMN)
    int status;

    public MemberInEvent(){
        super();
    }

    public MemberInEvent(String fbId, String eventId, int status) {
        super();
        this.fbId = fbId;
        this.eventId = eventId;
        this.status = status;
    }

    @JsonIgnore
    public MemberInEvent setEventId(String eventId){
        this.eventId = eventId;
        return this;
    }

    @JsonIgnore
    public String getFbId() {
        return fbId;
    }

    @JsonIgnore
    public String getEventId() {
        return eventId;
    }

    @JsonIgnore
    public int getStatus() {
        return status;
    }
}
