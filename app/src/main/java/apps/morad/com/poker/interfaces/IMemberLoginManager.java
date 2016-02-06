package apps.morad.com.poker.interfaces;

import com.facebook.CallbackManager;

/**
 * Created by Morad on 12/11/2015.
 */
public interface IMemberLoginManager {
    CallbackManager getCallbackManager();
    void onFacebookLoginSuccess();
    void onFacebookLogoutSuccess();
    void onFacebookLoginCanceled();
    void onFacebookLoginError();
}
