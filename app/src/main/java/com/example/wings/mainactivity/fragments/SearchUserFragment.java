package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;

import com.example.wings.adapters.SearchUserAdapter;
import com.example.wings.databinding.FragmentSearchUserBinding;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.mainactivity.fragments.dialogs.AddFriendDialog;
import com.example.wings.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Purpose:         Allows for the current user to search for other user's and look at their profiles! This way, the user can choose to add friends!
 */

public class SearchUserFragment extends Fragment {
    public static final String TAG = "SearchUsersFragment";

    FragmentSearchUserBinding binding;
    private MAFragmentsListener listener;
    private RecyclerView rvUsers;

    //make this a parseUser adapter
    protected SearchUserAdapter adapter;
    protected List<ParseUser> users;
    SearchView searchBar;
    TextView tvNoResults;

    public SearchUserFragment() {}

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchUserBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUsers = binding.rvsearchforfriends;
        users = new ArrayList<>();
        searchBar = binding.searchBar;
        tvNoResults = binding.tvNoResults;                 //Shown by default --> toggle visibilitiy when query

        //Editing some colors on the searchbar here as I couldn't do it on the layout.xml!
        EditText searchEditText = (EditText) searchBar.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.gray, null));
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray_background, null));

        //Create a SearchUserOnclickListener to create the adapter!
        adapter = new SearchUserAdapter(getContext(), users, new SearchUserAdapter.SearchUserOnClickListener() {
            @Override
            public void onClick(ParseUser userClickedOn) {
                //Go to dialog to display friend information + ask if want to add the friend
               /* AddFriendDialog dialog = AddFriendDialog.newInstance(nameToDisplay);
                dialog.setTargetFragment(SearchUserFragment.this, 1);
                dialog.show(getFragmentManager(), "SeeFriendDialog");*/

                //go to OtherProfileFrag to see this person's profile!
                listener.toOtherProfileFragment(userClickedOn);
            }
        });

        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //called when a query is submitted
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit()");
                queryForUsers(query);
                return true;    //bc we are handling it
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    //TODO: allow querying for usernames, actual names, emails, etc, and automatic querying without having to click on search
    private void queryForUsers(String queriedUsername) {
        Log.d(TAG, "queryUsers()");
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        query.whereEqualTo(User.KEY_USERNAME, queriedUsername);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue getting users", e);
                    return;
                }
                else {
                    Log.d(TAG, "query for users was success!");
                    if(objects.size() > 0) {
                        for (ParseUser user : objects) {
                            Log.i(TAG, "Username: " + user.getUsername());
                        }
                        adapter.clear();
                        adapter.addAll(objects);
                        tvNoResults.setVisibility(View.INVISIBLE);
                    }

                    else{
                        Toast toast = Toast.makeText(getContext(), "There were no users with that username!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                        tvNoResults.setVisibility(View.VISIBLE);
                        //erase what was previously displayed if anything:
                        adapter.clear();
                    }
                }
            }
        });

    }

}