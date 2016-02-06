package apps.morad.com.poker.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 1/26/2016.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = MyGCMListenerService.MessageTopics.stringValues();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token, sharedPreferences);

            // Subscribe to topic channels
            subscribeTopics(token);


            sharedPreferences.edit().putBoolean(getString(R.string.pref_is_sent_token_to_server), true).apply();

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(getString(R.string.pref_is_sent_token_to_server), false).apply();
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(MainActivity.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, SharedPreferences preferences) {
        try {
            String url = preferences.getString(getString(R.string.pref_server_url), "http://localhost");
            JSONObject data = new JSONObject();

            if(Profile.getCurrentProfile() != null) {
                data.put("profile", Profile.getCurrentProfile().getId());
                data.put("token", token);
                Utilities.sendRequest(url + "/registerGCM", "POST", data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}