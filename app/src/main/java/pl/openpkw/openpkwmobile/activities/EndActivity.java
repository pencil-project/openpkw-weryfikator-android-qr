package pl.openpkw.openpkwmobile.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.EndFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

public class EndActivity extends AppCompatActivity implements EndFragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        FragmentManager fm = getFragmentManager();
        EndFragment endFragment = (EndFragment) fm.findFragmentByTag(Utils.END_FRAGMENT_TAG);
        if (endFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.end_fragment_container, new EndFragment(), Utils.END_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
