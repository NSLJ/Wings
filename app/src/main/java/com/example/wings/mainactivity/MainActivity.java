package com.example.wings.mainactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.mainactivity.fragments.ChooseBuddyFragment;
import com.example.wings.mainactivity.fragments.HomeFragment;
import com.example.wings.mainactivity.fragments.OtherProfileFragment;
import com.example.wings.mainactivity.fragments.SearchUserFragment;
import com.example.wings.mainactivity.fragments.UserProfileFragment;
import com.example.wings.settingsactivity.SettingsActivity;
import com.example.wings.startactivity.fragments.LoginFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity.java
 * Purpose:         Displays the appropriate fragments that executes the main functions of the app. Essentially is a container to swap between each fragment.
 *
 */
public class MainActivity extends AppCompatActivity implements MAFragmentsListener{

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private static BottomNavigationView bottomNavigationView;

    @Override
    /**
     * Purpose:         called automatically when activity is launched. Initializes the BottomNavigationView with listeners when each menu item is clicked. on click --> raise correct fragment class.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize and set up listener:
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    /**
                     * Purpose:         called when a specific item is clicked/selected --> raise correct fragment
                     */
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_search_friends:
                                fragment = new SearchUserFragment();
                                break;

                            case R.id.action_home:
                                fragment = new HomeFragment();
                                break;

                            case R.id.action_profile:
                                fragment = new UserProfileFragment();
                                break;
                            default:
                                break;
                        }

                        //Raise the fragment:
                        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, fragment).commit();
                        return true;
                    }
                });
        //By default: select/press the action_home action --> start user on home timeline page once logged in!
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    /**
     * Purpose:         called automatically when activity launched --> inflates and displays menu items across the activity using "menu_main_navigation.xml".
     * @param menu
     * @return whether or not to show the menu
     */
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_top_navigation, menu);
        return true;
    }

    /**
     * Purpose:         Attaches events when menu items are pushed. Only one menu item: log off button --> intent request to StartingActivity{}, using GO_LOGIN_KEY to signal StartingActivity{} to start on Login page
     * @param item, which item was selected
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_logout:
                //1.) Log off the user using Parse:
            //    ParseUser.logOut();

                //2.) Intent to go to StartingActivity, finish() this activity
              //  Intent intent = new Intent(this, StartingActivity.class);
              //  intent.putExtra(GO_LOGIN_KEY, true);    //Tell StartingActivity to skip the Create account page!
              //  startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:  //Go to SettingsActivity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_safety_toolkit:
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * Purpose:     Needs to be implemented here so Safety Toolkit will execute on any screen
     */
    public void onClickSafetyToolkit(){

    }

    //Required implementation of methods from MAFragmentsListener
    //Purpose:      Displays the corresponding Fragment classes
    @Override
    public void toHomeFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new HomeFragment()).commit();
    }

    @Override
    public void toUserProfileFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new UserProfileFragment()).commit();
    }

    @Override
    public void toSearchUserFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new SearchUserFragment()).commit();
    }

    @Override
    public void toOtherProfileFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new OtherProfileFragment()).commit();
    }

    @Override
    public void toChooseBuddyFragment() {
        fragmentManager.beginTransaction().replace(R.id.flFragmentContainer, new ChooseBuddyFragment()).commit();
    }
}