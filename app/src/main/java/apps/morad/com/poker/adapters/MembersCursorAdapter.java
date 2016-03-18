package apps.morad.com.poker.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Medal;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.thirdParty.EProfilePictureView;

/**
 * Created by Morad on 12/16/2015.
 */
public class MembersCursorAdapter extends CursorAdapter {

    private static class ViewHolder{
        EProfilePictureView image;
        TextView name;
        TextView score;
        TextView games;
        ImageView medal;
    }

    public MembersCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.member_in_list, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.image = (EProfilePictureView) view.findViewById(R.id.member_image);
        holder.name = (TextView)view.findViewById(R.id.member_name);
        holder.score = (TextView)view.findViewById(R.id.member_score);
        holder.medal = (ImageView)view.findViewById(R.id.member_medal_image);
        holder.games = (TextView)view.findViewById(R.id.member_games);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String fbId = cursor.getString(cursor.getColumnIndex(Member.FB_ID_COLUMN));
        String fname = cursor.getString(cursor.getColumnIndex(Member.FIRST_NAME_COLUMN));
        String lname = cursor.getString(cursor.getColumnIndex(Member.LAST_NAME_COLUMN));
        int score = cursor.getInt(cursor.getColumnIndex(Member.SCORE_COLUMN));
        int games = cursor.getInt(cursor.getColumnIndex(Member.REMAINING_GAMES));
        String medal = cursor.getString(cursor.getColumnIndex(Member.MEDAL_COLUMN));

        ViewHolder holder = (ViewHolder)view.getTag();
        holder.image.setProfileId(fbId);
        holder.name.setText(fname + " " + lname);
        holder.score.setText(score + " points");

        holder.games.setText(games + " games, ");
        holder.games.setVisibility(View.VISIBLE);
        if(games == 0){
            holder.games.setVisibility(View.GONE);
        }

        Medal medal1 = Medal.getByName(medal);

        if(medal1 == Medal.Empty) {
            holder.medal.setImageBitmap(null);
        }else {
            holder.medal.setImageResource(medal1.getDrawable());
        }
    }
}
