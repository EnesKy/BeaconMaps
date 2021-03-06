package eky.beaconmaps.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import eky.beaconmaps.BeaconMaps;
import eky.beaconmaps.R;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String ENTER_ANIMATION = "enterAnimation";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        BeaconMaps app = (BeaconMaps) getApplication();
        if (!app.isBeaconNotificationsEnabled()) {
            Log.d("BaseActivity", "Enabling beacon notifications");
            app.enableBeaconNotifications();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        int enterAnim = this.getIntent().getIntExtra(ENTER_ANIMATION, R.anim.enter_from_right);
        if (enterAnim == R.anim.enter_from_right) {
            overridePendingTransition(R.anim.enter_to_right, R.anim.exit_to_right);
        } else if (enterAnim == R.anim.enter_from_bottom) {
            overridePendingTransition(R.anim.hold, R.anim.exit_to_bottom);
        }
    }

    public void openActivity(@Nullable Bundle extras, Class openClass) {
        Intent open = new Intent(this, openClass);
        if (extras != null) {
            open.putExtras(extras);
        }
        open.putExtra(ENTER_ANIMATION, R.anim.enter_from_bottom);
        startActivity(open);
        overridePendingTransition(R.anim.enter_from_bottom, R.anim.hold);
    }

    protected void openFragment(int replaceLayoutId, Fragment page, String tag, int enterAnim, int exitAnim) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(enterAnim, exitAnim);
        fragmentTransaction.replace(replaceLayoutId, page, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    protected void closeFragment(String tag, int enterAnim, int exitAnim) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment page = fragmentManager.findFragmentByTag(tag);

        if (page != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(enterAnim, exitAnim);
            fragmentTransaction.remove(page).commit();
        }
    }

    public void isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
        } else {
            Snackbar.make(this.findViewById(R.id.cl_main),
                    "Internet Connection Is Required" , Snackbar.LENGTH_LONG)
                    .setAction(" Ok ", view -> { })
                    .setActionTextColor(getResources().getColor(R.color.rallyGreen))
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show();
        }
    }

}
