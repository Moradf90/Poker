package apps.morad.com.poker.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import apps.morad.com.poker.R;
import apps.morad.com.poker.fragments.AppFragment;
import apps.morad.com.poker.fragments.LoginFragment;
import apps.morad.com.poker.interfaces.IMemberLoginManager;
import apps.morad.com.poker.interfaces.IRefreshView;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.receivers.StartSyncReceiver;
import apps.morad.com.poker.services.RegistrationIntentService;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

public class MainActivity extends AppCompatActivity implements IMemberLoginManager, IRefreshView{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String REGISTRATION_COMPLETE = "registration_completed";
    public static final String MEMBERS_UPDATED = "members_updated";
    public static final String EVENTS_UPDATED = "events_updated";
    public static final String GAMES_UPDATED = "games_updated";
    public static final String MEMBERS_IN_EVENTS_UPDATED = "members_in_events_updated";
    public static final String MEMBERS_IN_GAMES_UPDATED = "members_in_games_updated";

    CallbackManager _callbackManager;
    AccessTokenTracker _accessTokenTracker;
    Timer _timer;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    ProgressDialog prog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(getString(R.string.pref_server_url), "http://10.0.0.9:8090").commit();

        prog = new ProgressDialog(this);
        prog.setCancelable(false);
        prog.setMessage("Logging you in...");

        initFacebookConnection();

        _accessTokenTracker.startTracking();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                prog.hide();

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean( getString(R.string.pref_is_sent_token_to_server), false);
                if (!sentToken) {
                    Toast.makeText(context, "Failed to connect to GCM", Toast.LENGTH_LONG);
                }
            }
        };

        checkGooglePlayServiceAvailability();
    }

    private boolean checkGooglePlayServiceAvailability() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void initFacebookConnection(){
        FacebookSdk.sdkInitialize(getApplicationContext());
        _callbackManager = CallbackManager.Factory.create();

        _accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    onFacebookLogoutSuccess();
                } else {
                    onFacebookLoginSuccess();
                }
            }
        };

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken != null){
            onFacebookLoginSuccess();
        }
        else {
            onFacebookLogoutSuccess();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        _timer = new Timer();

        _timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        refreshView();
                    }
                });

            }
        }, 0, 1000);

        checkGooglePlayServiceAvailability();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        _timer.cancel();
        _timer = null;
        super.onPause();
    }

    private void calcHashKey() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo(
                    "apps.morad.com.poker",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e){

        }
        catch (NoSuchAlgorithmException e){

        }
    }

    @Override
    public void onFacebookLoginSuccess() {
        new LoginTask().execute();
    }

    @Override
    public void onFacebookLogoutSuccess() {

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putBoolean(getString(R.string.pref_is_login), false).commit();

        removeApplicationAccount();

        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.findFragmentByTag(LoginFragment.FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.context_fragment, LoginFragment.getInstance(), LoginFragment.FRAGMENT_TAG);
            transaction.commit();
        }
    }

    @Override
    public void onFacebookLoginCanceled() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putBoolean(getString(R.string.pref_is_login), false).commit();

        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.findFragmentByTag(LoginFragment.FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.context_fragment, LoginFragment.getInstance(), LoginFragment.FRAGMENT_TAG);
            transaction.commit();
        }
    }

    @Override
    public void onFacebookLoginError() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putBoolean(getString(R.string.pref_is_login), false).commit();

        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.findFragmentByTag(LoginFragment.FRAGMENT_TAG) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.context_fragment, LoginFragment.getInstance(), LoginFragment.FRAGMENT_TAG);
            transaction.commit();
        }
    }

    @Override
    public CallbackManager getCallbackManager() {
        return _callbackManager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _accessTokenTracker.stopTracking();
    }

    @Override
    public void refreshView() {

        FragmentManager fragmentManager = getFragmentManager();

        Fragment appFragment = fragmentManager.findFragmentByTag(AppFragment.FRAGMENT_TAG);

        if(appFragment != null) {
            ((IRefreshView)appFragment).refreshView();
        }

        MembersLoader.refresh();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public class LoginTask extends AsyncTask<Void,Void,JSONObject>{

        boolean _isLoggedIn = false;
        SharedPreferences _pref;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            _isLoggedIn = _pref.getBoolean(getString(R.string.pref_is_login), false);
            prog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            JSONObject result = new JSONObject();

            try{
                result.put("isLogin", _isLoggedIn);

                if(!_isLoggedIn) {

                    JSONObject member = new JSONObject();
                    Profile profile = Profile.getCurrentProfile();
                    member.put(Member.FB_ID_COLUMN, profile.getId());
                    member.put(Member.FIRST_NAME_COLUMN, profile.getFirstName());
                    member.put(Member.LAST_NAME_COLUMN, profile.getLastName());
                    String url = _pref.getString(getString(R.string.pref_server_url), "http://localhost");
                    JSONObject res = Utilities.sendRequest(url + "/login", "POST", member);

                    if (res != null) {
                        result = res;
                    }
                }

            }catch (Exception e){}

            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            try {
                Boolean isLoggedIn = (Boolean)result.get("isLogin");

                if(isLoggedIn) {

                    _pref.edit().putBoolean(getString(R.string.pref_is_login), isLoggedIn).commit();
                    addApplicationAccount();
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager.findFragmentByTag(AppFragment.FRAGMENT_TAG) == null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.context_fragment, AppFragment.getInstance(), AppFragment.FRAGMENT_TAG);
                        transaction.commit();
                    }

                    //send broadcast to start a services if its not started
                    Intent i = new Intent(StartSyncReceiver.ACTION_START_SYNC);
                    sendBroadcast(i);
                }
            } catch (JSONException e) {
                onFacebookLogoutSuccess();
            }

            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void addApplicationAccount() {
        String accountType = getString(R.string.account_type);
        AccountManager accountManager =
                (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(accountType);

        boolean accountExists = false;
        for (Account ac : accounts) {
            if (!ac.name.equals(Profile.getCurrentProfile().getName())){
                accountManager.removeAccountExplicitly(ac);
            }
            else accountExists = true;
        }

        if(!accountExists) {
            accountManager.addAccountExplicitly(new Account(Profile.getCurrentProfile().getName(), accountType), null, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void removeApplicationAccount() {
        String accountType = getString(R.string.account_type);
        AccountManager accountManager =
                (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(accountType);

        for (Account ac : accounts) {
            accountManager.removeAccountExplicitly(ac);
        }
    }
}
