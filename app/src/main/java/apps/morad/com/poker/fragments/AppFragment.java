package apps.morad.com.poker.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.ActionAdapter;
import apps.morad.com.poker.drawables.DrawerArrowDrawable;
import apps.morad.com.poker.interfaces.IActionItemsClicked;
import apps.morad.com.poker.interfaces.ITaggedFragment;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.utilities.MembersLoader;

import static android.view.Gravity.START;

/**
 * Created by Morad on 12/14/2015.
 */
public class AppFragment extends Fragment implements IActionItemsClicked {

    public static final String FRAGMENT_TAG = "AppFragment";

    private DrawerArrowDrawable drawerArrowDrawable;
    private float offset;
    private boolean flipped;
    private static AppFragment _instance;
    DrawerLayout drawer;
    private String currentFragment;
    private Fragment currentFragmentObj;
    private int actionPosition = 0;
    ActionAdapter actionAdapter;
    public static AppFragment getInstance()
    {
        if(_instance == null)
        {
            _instance = new AppFragment();
        }

        return _instance;
    }

    View fragmentView;
    private BroadcastReceiver _membersUpdatedBroadcastReceiver;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.app_fragment, container, false);

        drawer = (DrawerLayout) fragmentView.findViewById(R.id.drawer_layout);
        final ImageView imageView = (ImageView) fragmentView.findViewById(R.id.drawer_indicator);
        final Resources resources = getResources();

        drawerArrowDrawable = new DrawerArrowDrawable(resources);
        drawerArrowDrawable.setStrokeColor(resources.getColor(R.color.colorPrimary));
        imageView.setImageDrawable(drawerArrowDrawable);

        drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                offset = slideOffset;

                // Sometimes slideOffset ends up so close to but not quite 1 or 0.
                if (slideOffset >= .995) {
                    flipped = true;
                    drawerArrowDrawable.setFlip(flipped);
                } else if (slideOffset <= .005) {
                    flipped = false;
                    drawerArrowDrawable.setFlip(flipped);
                }

                drawerArrowDrawable.setParameter(offset);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawer();
            }
        });

        setProfileBarData(fragmentView);
        setSliderActionBar(fragmentView);

        ((TextView)fragmentView.findViewById(R.id.member_id)).setText(Profile.getCurrentProfile().getId());

        _membersUpdatedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshView();
            }
        };

        refreshView();

        return fragmentView;
    }

    private void refreshView() {
        MembersLoader.refresh();
        if(Profile.getCurrentProfile() != null) {
            Member current = MembersLoader.getById(Profile.getCurrentProfile().getId());

            if (current != null) {
                TextView score = ((TextView) fragmentView.findViewById(R.id.action_bar_profile_score));

                score.setText(current.getScore() + " points");

                if (current.getScore() <= 0) {
                    score.setTextColor(getResources().getColor(R.color.redColor));
                } else {
                    score.setTextColor(getResources().getColor(R.color.greenColor));
                }
            }
        }
    }

    private void toggleDrawer(){
        if (drawer.isDrawerVisible(START)) {
            drawer.closeDrawer(START);
        } else {
            drawer.openDrawer(START);
        }
    }

    private void setSliderActionBar(View pView){

        ListView lst = (ListView) pView.findViewById(R.id.action_list);

        actionAdapter = new ActionAdapter(getActivity());
        lst.setAdapter(actionAdapter);
        lst.setDivider(null);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String action = (String) actionAdapter.getItem(position);

                actionAdapter.setClickedItem(position);
                actionPosition = position;

                switch (action) {
                    case "Members":
                        onMembersItemClicked();
                        break;
                    case "Events":
                        onEventsItemClicked();
                        break;
                    case "Games":
                        onGamesItemClicked();
                        break;
                }

                actionAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setProfileBarData(View pView)
    {
        ((ProfilePictureView)pView.findViewById(R.id.action_bar_profile_pic)).setProfileId(Profile.getCurrentProfile().getId());
        ((TextView)pView.findViewById(R.id.action_bar_profile_name)).setText(Profile.getCurrentProfile().getName());
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentManager fragmentManager = getChildFragmentManager();
        if(currentFragment != null && currentFragmentObj != null) {

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.view_content, currentFragmentObj, ((ITaggedFragment) currentFragmentObj).getFragmentTag());
            transaction.commit();

            actionAdapter.setClickedItem(actionPosition);
            actionAdapter.notifyDataSetChanged();
        }else {
            onMembersItemClicked();
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_membersUpdatedBroadcastReceiver, new IntentFilter(MainActivity.MEMBERS_UPDATED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_membersUpdatedBroadcastReceiver);
    }

    @Override
    public void onHomeItemClicked() {

    }

    @Override
    public void onMembersItemClicked() {
        if(currentFragment != MembersFragment.FRAGMENT_TAG) {
            FragmentManager fragmentManager = getChildFragmentManager();
            if (fragmentManager.findFragmentByTag(MembersFragment.FRAGMENT_TAG) == null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.view_content, MembersFragment.getInstance(), MembersFragment.FRAGMENT_TAG);
                transaction.commit();
                currentFragment = MembersFragment.FRAGMENT_TAG;
                currentFragmentObj = MembersFragment.getInstance();
            }
        }
    }

    @Override
    public void onEventsItemClicked() {
        if(currentFragment != EventsFragment.FRAGMENT_TAG) {
            FragmentManager fragmentManager = getChildFragmentManager();
            if (fragmentManager.findFragmentByTag(EventsFragment.FRAGMENT_TAG) == null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.view_content, EventsFragment.getInstance(), EventsFragment.FRAGMENT_TAG);
                transaction.commit();
                currentFragment = EventsFragment.FRAGMENT_TAG;
                currentFragmentObj = EventsFragment.getInstance();
            }
        }
    }

    @Override
    public void onGamesItemClicked() {
        if(currentFragment != GamesFragments.FRAGMENT_TAG) {
            FragmentManager fragmentManager = getChildFragmentManager();
            if (fragmentManager.findFragmentByTag(GamesFragments.FRAGMENT_TAG) == null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.view_content, GamesFragments.getInstance(), GamesFragments.FRAGMENT_TAG);
                transaction.commit();
                currentFragment = GamesFragments.FRAGMENT_TAG;
                currentFragmentObj = GamesFragments.getInstance();
            }
        }
    }

    @Override
    public void onSettingsItemClicked() {
    }

}
