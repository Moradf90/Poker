package apps.morad.com.poker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.activeandroid.ActiveAndroid;

/**
 * Created by Morad on 12/16/2015.
 */
public class PokerApp extends com.activeandroid.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }
}
