package apps.morad.com.poker.activities;

import android.animation.Animator;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.activeandroid.query.Select;
import com.facebook.Profile;

import java.util.ArrayList;

import apps.morad.com.poker.R;
import apps.morad.com.poker.fragments.AddOrUpdateEventDialogFragment;
import apps.morad.com.poker.fragments.AddOrUpdateGameDialogFragment;
import apps.morad.com.poker.fragments.GamesInEventFragment;
import apps.morad.com.poker.fragments.MembersInEventFragment;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.utilities.MembersLoader;

public class EventDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton mAddGameButton;
    private static Event mEvent;
    private BroadcastReceiver mBroadcastReceiver;

    public static Event getEvent() {
        return mEvent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        mAddGameButton = (FloatingActionButton) findViewById(R.id.add_game_btn);
        mAddGameButton.setVisibility(View.GONE);

        mAddGameButton.setOnClickListener(this);

        String eventId = getIntent().getStringExtra(Event.EVENT_ID_COLUMN);
        if(eventId != null) {
            mEvent = new Select().from(Event.class).where(Event.EVENT_ID_COLUMN + "=?", eventId).executeSingle();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(mEvent != null ? mEvent.getTag() : "Unkown event");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateAddButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateAddButton(mViewPager.getCurrentItem());
            }
        };
    }

    private void updateAddButton(int position){

        mEvent = new Select().from(Event.class).where(Event.EVENT_ID_COLUMN + "=?", mEvent.getEventId()).executeSingle();
        Game game = new Select().from(Game.class).where(Game.END_TIME_COLUMN + "= -1").executeSingle();

        if(mSectionsPagerAdapter.sections.get(position).equals("Games")&&
                !mEvent.isClosed() && game == null &&
                MembersLoader.getById(Profile.getCurrentProfile().getId()).getIsAdmin()){

            mAddGameButton.animate().alpha(1.0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mAddGameButton.setAlpha(0.0f);
                    mAddGameButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }else {
            mAddGameButton.setAlpha(1.0f);
            mAddGameButton.animate().alpha(0.0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddGameButton.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.GAMES_UPDATED);
        intentFilter.addAction(MainActivity.EVENTS_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        android.app.Fragment prev = getFragmentManager().findFragmentByTag(AddOrUpdateGameDialogFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = AddOrUpdateGameDialogFragment.newInstance();
        newFragment.show(ft, AddOrUpdateGameDialogFragment.TAG);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<String> sections = new ArrayList<String>(){{
            add("Members");
            add("Games");
        }};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(sections.get(position).equals("Members")){
                return MembersInEventFragment.newInstance(mEvent.getEventId());
            }
            else if(sections.get(position).equals("Games")) {
                return GamesInEventFragment.newInstance(mEvent.getEventId());
            }

            return null;
        }

        @Override
        public int getCount() {
            return sections.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sections.get(position);
        }
    }
}
