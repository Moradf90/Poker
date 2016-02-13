package apps.morad.com.poker.models;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.json.JSONObject;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Date;

/**
 * Created by Morad on 12/16/2015.
 */
@Table(name = "Events", id = BaseColumns._ID)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event extends Model{

    public static final String EVENT_ID_COLUMN = "eventid";
    public static final String CREATED_DATE_COLUMN = "createdDate";
    public static final String DATE_COLUMN = "date";
    public static final String LOCATION_COLUMN = "location";
    public static final String TAG_COLUMN = "tag";
    public static final String IS_CLOSED_COLUMN = "isClosed";
    public static final String CREATOR_COLUMN = "creator";

    @JsonProperty(EVENT_ID_COLUMN)
    @Column(name = EVENT_ID_COLUMN, index = true)
    String eventId;
    @JsonProperty(CREATED_DATE_COLUMN)
    @Column(name = CREATED_DATE_COLUMN)
    long createdDate;
    @JsonProperty(DATE_COLUMN)
    @Column(name = DATE_COLUMN)
    long date;
    @JsonProperty(LOCATION_COLUMN)
    @Column(name = LOCATION_COLUMN)
    String location;
    @JsonProperty(TAG_COLUMN)
    @Column(name = TAG_COLUMN)
    String tag;
    @JsonProperty(IS_CLOSED_COLUMN)
    @Column(name = IS_CLOSED_COLUMN)
    boolean isClosed;
    @JsonProperty(CREATOR_COLUMN)
    @Column(name = CREATOR_COLUMN)
    String creator;

    public Event(){
        super();
    }

    public Event(String eventId, long createdDate, long date, String location, String tag, String creator) {
        this.eventId = eventId;
        this.createdDate = createdDate;
        this.date = date;
        this.location = location;
        this.tag = tag;
        this.creator = creator;
    }

    public Event(String eventId, long createdDate, String creator) {
        this.eventId = eventId;
        this.createdDate = createdDate;
        this.creator = creator;
    }

    @JsonIgnore
    public String getEventId() {
        return eventId;
    }

    @JsonIgnore
    public long getCreatedDate() {
        return createdDate;
    }

    @JsonIgnore
    public long getDate() {
        return date;
    }

    @JsonIgnore
    public String getLocation() {
        return location;
    }

    @JsonIgnore
    public String getTag() {
        return tag;
    }

    @JsonIgnore
    public boolean isClosed() {
        return isClosed;
    }

    @JsonIgnore
    public Event setEventId(String eventId){
        this.eventId = eventId;
        return this;
    }

    @JsonIgnore
    public void setIsClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    public void updateFromJson(JSONObject obj) {
        try {
            if (obj.has(TAG_COLUMN)) {
                this.tag = obj.getString(TAG_COLUMN);
            }
            if (obj.has(DATE_COLUMN)){
                this.date = obj.getLong(DATE_COLUMN);
            }
            if (obj.has(LOCATION_COLUMN)){
                this.location = obj.getString(LOCATION_COLUMN);
            }
            if (obj.has(IS_CLOSED_COLUMN)){
                this.isClosed = obj.getBoolean(IS_CLOSED_COLUMN);
            }
        }
        catch (Exception e){}
    }
}
