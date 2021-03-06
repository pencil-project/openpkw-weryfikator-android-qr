package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.LoginFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.security.KeyWrapper;
import pl.openpkw.openpkwmobile.security.SecurityECC;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

public class LoginActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback{

    private boolean doubleBackToExitPressedOnce = false;
    private View loginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginLayout = findViewById(R.id.login_fragment_container);

        FragmentManager fm = getFragmentManager();
        LoginFragment loginFragment = (LoginFragment) fm.findFragmentByTag(Utils.LOGIN_FRAGMENT_TAG);
        if (loginFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.login_fragment_container, new LoginFragment(), Utils.LOGIN_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }

        // add spongy castle security provider
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        //generate and wrap ECDSA keys
        generateKeys();

        //show process info log in
        showProcessInfo();
    }

    private void showProcessInfo() {
        Snackbar.make(loginLayout, "Krok 1 - Logowanie do systemu",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        //hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        //begin transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Create new fragment and transaction
                Fragment settingsFragment = new SettingsFragment();
                transaction.replace(android.R.id.content, settingsFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            case R.id.about_project:
                Fragment aboutFragment = new AboutFragment();
                transaction.replace(android.R.id.content, aboutFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();
            //show main fragment
            getFragmentManager().popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                timer.cancel();
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.fragment_login_twotaptoexit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);
        }
    }

    public void generateKeys(){
        try {
            SharedPreferences sharedPref = getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
            KeyWrapper keyWrapper = new KeyWrapper(getApplicationContext(), Utils.KEY_ALIAS);
            if(sharedPref.getString(Utils.PRIVATE_KEY,null)==null)
            {
                KeyPair keyPair = SecurityECC.generateKeys();
                if (keyPair != null) {
                    byte [] privateKeyByteArr = keyWrapper.wrapPrivateKey(keyPair.getPrivate());
                    byte [] publicKeyByteArr = keyWrapper.wrapPublicKey(keyPair.getPublic());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Utils.PRIVATE_KEY,Base64.encodeToString(privateKeyByteArr,Base64.DEFAULT));
                    editor.putString(Utils.PUBLIC_KEY, Base64.encodeToString(publicKeyByteArr, Base64.DEFAULT));
                    editor.apply();
                }
            }
        } catch (GeneralSecurityException e) {
            Log.e(Utils.TAG, "GeneralSecurityException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
