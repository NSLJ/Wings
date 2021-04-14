package com.example.wings.startactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import com.example.wings.R;
import com.example.wings.commonFragments.HelpFragment;
import com.example.wings.mainactivity.MainActivity;
import com.example.wings.commonFragments.EditTrustedContactsFragment;
import com.example.wings.commonFragments.SettingsFragment;
import com.example.wings.startactivity.fragments.LoginFragment;
import com.example.wings.startactivity.fragments.ProfileSetupFragment;
import com.example.wings.startactivity.fragments.RegisterOneFragment;
import com.example.wings.startactivity.fragments.RegisterTwoFragment;

/**
 * StartActivity.java
 * Purpose:         Displays the appropriate fragments that executes the beginning functions of the app. Essentially is a container to swap between each fragment.
 *
 */
public class StartActivity extends AppCompatActivity implements SAFragmentsListener {

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
    //On successful login --> intent to MainActivity and finish()
    public void onLogin() {
        Intent intent = new Intent(this, MainActivity.class);
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
    public void toRegisterTwoFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new RegisterTwoFragment()).commit();
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