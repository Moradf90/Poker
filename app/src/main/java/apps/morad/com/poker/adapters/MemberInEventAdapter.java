package apps.morad.com.poker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.utilities.MembersLoader;

/**
 * Created by Morad on 12/31/2015.
 */
    public class MemberInEventAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> profiles;

    public MemberInEventAdapter(Context context, ArrayList<String> profiles){
        this.context = context;
        this.profiles = profiles;
    }

    @Override
    public int getCount() {
        return profiles.size();
    }

    @Override
    public Object getItem(int position) {
        return profiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = LayoutInflater.from(context).inflate(R.layout.member_in_event_layout, null);

        ProfilePictureView p = (ProfilePictureView)v.findViewById(R.id.profile_picture);
        p.setProfileId(profiles.get(position));
        Member member = MembersLoader.getById(profiles.get(position));

        if(member != null){
            ((TextView)v.findViewById(R.id.profile_name)).setText(member.getFirstName());
        }


        return v;
    }

    public void swapProfiles(ArrayList<String> profiles){
        if(this.profiles != profiles) {
            this.profiles.clear();
            this.profiles.addAll(profiles);
        }
        notifyDataSetChanged();
    }

    public boolean containsProfile(String id){
        return profiles.contains(id);
    }
}
