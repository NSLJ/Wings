package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;

import com.example.wings.adapters.SearchUserAdapter;
import com.example.wings.mainactivity.MAFragmentsListener;
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

    private MAFragmentsListener listener;
    private RecyclerView rvUsers;
    //make this a parseUser adapter
    protected SearchUserAdapter adapter;
    protected List<ParseUser> users;
    SearchView searchBar;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUsers = view.findViewById(R.id.rvsearchforfriends);
        users = new ArrayList<>();
        searchBar = view.findViewById(R.id.searchbarFriends);
        adapter = new SearchUserAdapter(getContext(), users);

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
      //  searchBar.getQuery()

        //queryUsers();
    }

    private void queryForUsers(String queriedUsername) {
        Log.d(TAG, "queryUsers()");
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        //do we want a limit?
        //order it maybe by who is closest? for now just order by when the user was made
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
                    }

                    else{
                        Toast.makeText(getContext(), "There were no users with that username!", Toast.LENGTH_SHORT).show();
                        //erase what was previously displayed if anything:
                        adapter.clear();
                    }
                }
            }
        });

    }

}