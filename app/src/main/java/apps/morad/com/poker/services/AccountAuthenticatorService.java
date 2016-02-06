package apps.morad.com.poker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import apps.morad.com.poker.authenticators.AccountAuthenticator;

/**
 * Created by Morad on 1/24/2016.
 */
public class AccountAuthenticatorService extends Service {

    private AccountAuthenticator _authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        _authenticator = new AccountAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _authenticator.getIBinder();
    }
}
