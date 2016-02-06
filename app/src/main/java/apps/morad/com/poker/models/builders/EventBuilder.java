package apps.morad.com.poker.models.builders;

import java.security.SecureRandom;

import apps.morad.com.poker.models.Event;

/**
 * Created by Morad on 12/23/2015.
 */
public class EventBuilder
{
    long createdDate;
    long date;
    String location;
    String tag;
    String creator;

    public static EventBuilder instance(){
        return new EventBuilder();
    }

    public EventBuilder setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public EventBuilder setDate(long date) {
        this.date = date;
        return this;
    }

    public EventBuilder setLocation(String location) {
        this.location = location;
        return this;
    }

    public EventBuilder setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public EventBuilder setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public Event build(){

        String id = "n_" + new SecureRandom().nextLong();

        return new Event(id, createdDate, date, location, tag, creator);
    }
}
