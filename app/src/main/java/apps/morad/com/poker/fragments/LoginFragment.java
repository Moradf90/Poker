package apps.morad.com.poker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import apps.morad.com.poker.R;
import apps.morad.com.poker.interfaces.IMemberLoginManager;

public class LoginFragment extends Fragment {

    public static final String FRAGMENT_TAG = "LoginFragment";

    IMemberLoginManager _memberLoginManager;



    public LoginFragment() {
        // Required empty public constructor
    }

    private static LoginFragment _instance;

    public static LoginFragment getInstance()
    {
        if(_instance == null)
        {
            _instance = new LoginFragment();
        }

        return _instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _memberLoginManager = (IMemberLoginManager)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.login_fragment, container, false);

        LoginButton loginButton = (LoginButton) fragmentView.findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");
        loginButton.setReadPermissions("user_friends");
        // If using in a fragment
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(_memberLoginManager.getCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //_memberLoginManager.onFacebookLoginSuccess();
            }

            @Override
            public void onCancel() {
                _memberLoginManager.onFacebookLoginCanceled();
            }

            @Override
            public void onError(FacebookException exception) {
                fragmentView.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
                _memberLoginManager.onFacebookLoginError();
            }
        });

        return fragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _memberLoginManager.getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }
}
