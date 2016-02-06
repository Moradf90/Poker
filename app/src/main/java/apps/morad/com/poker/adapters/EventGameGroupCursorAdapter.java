package apps.morad.com.poker.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.facebook.Profile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;

/**
 * Created by Morad on 12/27/2015.
 */
public class EventGameGroupCursorAdapter extends CursorAdapter {


    public EventGameGroupCursorAdapter(Context context, Cursor c) {
        super(context, c, true);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.event_game_group, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final String eventId = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID_COLUMN));
        String tag = cursor.getString(cursor.getColumnIndex(Event.TAG_COLUMN));
        long date = cursor.getLong(cursor.getColumnIndex(Event.DATE_COLUMN));
        boolean isClosed = (cursor.getInt(cursor.getColumnIndex(Event.IS_CLOSED_COLUMN)) == 1);

        ((ImageView)view.findViewById(R.id.event_status)).setImageResource(R.drawable.closed_event);
        ((TextView) view.findViewById(R.id.event_tag)).setText(tag);
        SimpleDateFormat sdf = new SimpleDateFormat("E  dd/MM  HH:mm");
        String d = sdf.format(new Date(date));

        ((TextView)view.findViewById(R.id.event_date)).setText(d);

        if(!isClosed){
            ((ImageView)view.findViewById(R.id.event_status)).setImageResource(R.drawable.opened_event);
        }


    }
}
