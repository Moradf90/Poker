package apps.morad.com.poker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInGame;
import apps.morad.com.poker.utilities.MembersLoader;

/**
 * Created by Morad on 1/9/2016.
 */
public class MemberOutGameAdapter extends BaseAdapter {

    private static class ViewHolder{
        ProfilePictureView image;
        TextView name;
        TextView order;
        TextView score;
    }

    List<MemberInGame> members;
    Context context;

    public MemberOutGameAdapter(Context context, List<MemberInGame> members){
        this.context = context;
        this.members = members;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null){
            view = newView();
        }else {
            view = convertView;
        }

        bindView(view, position);

        return view;
    }


    private void bindView(View view, int position){

        MemberInGame mig = (MemberInGame) getItem(position);
        Member member = MembersLoader.getById( mig.getFbId());
        ViewHolder holder = (ViewHolder) view.getTag();

        if(member != null && holder != null){

            holder.image.setProfileId(mig.getFbId());
            holder.name.setText(member.getFirstName() + " " + member.getLastName());
            holder.order.setText("#"+ mig.getOrder());
            holder.score.setText((mig.getScore() > 0 ? "+ " + mig.getScore() : "- " + (-mig.getScore()))+ " pnt");

            if(mig.getScore() > 0){
                holder.score.setTextColor(context.getResources().getColor(R.color.greenColor));
            } else {
                holder.score.setTextColor(context.getResources().getColor(R.color.redColor));
            }
        }
    }

    private View newView(){
        View v = LayoutInflater.from(context).inflate(R.layout.member_status_in_game, null);

        ViewHolder holder = new ViewHolder();
        holder.image = (ProfilePictureView)v.findViewById(R.id.member_image);
        holder.name = (TextView) v.findViewById(R.id.member_name);
        holder.score = (TextView) v.findViewById(R.id.member_score);
        holder.order = (TextView) v.findViewById(R.id.member_order);

        v.setTag(holder);

        return v;
    }

    public void swapProfiles(ArrayList<MemberInGame> membersInGame){
        this.members = membersInGame;
        notifyDataSetChanged();
    }
}
