package com.example.wings.startactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.wings.R;
import com.example.wings.commonFragments.HelpFragment;
import com.example.wings.mainactivity.MainActivity;
import com.example.wings.commonFragments.EditTrustedContactsFragment;
import com.example.wings.commonFragments.SettingsFragment;
import com.example.wings.models.ParcelUser;
import com.example.wings.models.User;
import com.example.wings.startactivity.fragments.LoginFragment;
import com.example.wings.mainactivity.fragments.ProfileSetupFragment;
import com.example.wings.startactivity.fragments.RegisterOneFragment;
import com.example.wings.startactivity.fragments.RegisterTwoFragment;

import org.parceler.Parcels;

//import org.parceler.Parcels;

/**
 * StartActivity.java
 * Purpose:         Displays the appropriate fragments that executes the beginning functions of the app. Essentially is a container to swap between each fragment.
 *
 */
public class StartActivity extends AppCompatActivity implements SAFragmentsListener {
    public static final String KEY_SEND_USER = "user";
    public static final String KEY_PROFILESETUPFRAG = "ProfileSetupFrag?";
    private static final String TAG = "StartActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    /**
     * Purpose:         called automatically when activity is launched, displays the LoginFragment by default.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //By default: open with the Login page
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new LoginFragment()).commit();
    }

    @Override
    //Purpose:      based on the given key --> goes to either ProfileSetUpFrag or HomeFrag
    public void onLogin(String key) {
        Intent intent = new Intent(this, MainActivity.class);
        if(key.equals(KEY_PROFILESETUPFRAG)){
            intent.putExtra(KEY_PROFILESETUPFRAG, true);        //tell MainActivity to start on ProfileSetupFrag
        }
        startActivity(intent);
        finish();
    }

    //Required implementation of methods from SAFragmentsListener
    //Purpose:      Displays the corresponding Fragment classes
    @Override
    public void toLoginFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new LoginFragment()).commit();
    }

    @Override
    public void toRegisterOneFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new RegisterOneFragment()).commit();
    }

    @Override
    public void toRegisterTwoFragment(User user) {
        Log.d(TAG, "in toRegisterTwoFragment()");
        //1.) Package a bundle
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SEND_USER, Parcels.wrap(new ParcelUser(user)));

        //2.) Create the Fragment with the bundle and display it
        Fragment registerTwoFrag= new RegisterTwoFragment();
        registerTwoFrag.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, registerTwoFrag).commit();
    }

    @Override
    public void toProfileSetupFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new ProfileSetupFragment()).commit();
    }

    @Override
    public void toEditTrustedContacts() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new EditTrustedContactsFragment()).commit();
    }

    @Override
    public void toSettingsFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new SettingsFragment()).commit();
    }

    @Override
    public void toHelpFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new HelpFragment()).commit();
    }
}