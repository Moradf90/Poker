package apps.morad.com.poker.adapters;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import apps.morad.com.poker.R;
import apps.morad.com.poker.fragments.AppFragment;
import apps.morad.com.poker.interfaces.IActionItemsClicked;

/**
 * Created by Morad on 12/15/2015.
 */
public class ActionAdapter extends BaseAdapter {

    String[] _actionTitles;
    int[] _actionDrawables;

    Context _context;

    int clickedItem = 0;
    public ActionAdapter(Context context){
        this._context = context;
        _actionTitles = new String[2];
        _actionDrawables = new int[2];

//        _actionDrawables[0] = R.drawable.action_home;
//        _actionTitles[0] = "Home";
        _actionDrawables[0] = R.drawable.action_members;
        _actionTitles[0] = "Members";
//        _actionDrawables[2] = R.drawable.action_games;
//        _actionTitles[2] = "Games";
        _actionDrawables[1] = R.drawable.action_events;
        _actionTitles[1] = "Events";
//        _actionDrawables[4] = R.drawable.action_settings;
//        _actionTitles[4] = "Settings";
    }

    @Override
    public int getCount() {
        return _actionTitles.length;
    }

    @Override
    public Object getItem(int position) {
        return _actionTitles[position];
    }

    @Override
    public long getItemId(int position) {
        return _actionDrawables[position];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View mView = ((LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.action_item, parent, false);

        ((TextView)mView.findViewById(R.id.action_name)).setText(_actionTitles[position]);

        if(position == clickedItem){
            ((TextView)mView.findViewById(R.id.action_name)).setTextColor(_context.getResources().getColor(R.color.colorPrimary));

            Drawable d = _context.getResources().getDrawable(_actionDrawables[position]);
            d.setColorFilter(new
                    PorterDuffColorFilter(_context.getResources().getColor(R.color.colorPrimary),PorterDuff.Mode.SRC_IN));
            ((ImageView)mView.findViewById(R.id.action_image)).setImageDrawable(d);
        }
        else {
            ((ImageView)mView.findViewById(R.id.action_image)).setImageResource(_actionDrawables[position]);
        }

        return mView;
    }

    public void setClickedItem(int position){
        if(position == 4) return; // position of the settings
        clickedItem = position;
    }
}
