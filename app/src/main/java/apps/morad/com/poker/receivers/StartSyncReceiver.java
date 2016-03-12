package apps.morad.com.poker.receivers;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Profile;

import apps.morad.com.poker.R;

/**
 * Created by Morad on 1/25/2016.
 */
public class StartSyncReceiver extends BroadcastReceiver {

    public static final String ACTION_START_SYNC = "apps.morad.com.poker.receivers.START_SYNC";

    @Override
    public void onReceive(Context context, Intent intent) {
        String accountType = context.getString(R.string.account_type);

        Account account = new Account(Profile.getCurrentProfile().getName(), accountType);
        String authority = context.getString(R.string.authority);

        ContentResolver resolver = context.getContentResolver();
        resolver.setIsSyncable(account, authority, 1);
        resolver.setSyncAutomatically(account, authority, true);

        resolver.addPeriodicSync(
                account,
                authority,
                Bundle.EMPTY,
                60 * 30); // every half hour
    }
}
