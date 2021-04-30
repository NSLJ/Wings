package com.example.wings.mainactivity.fragments;

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

import com.example.wings.R;

import com.example.wings.UserAdapter;
import com.example.wings.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


//All auto-filled stuff, just follow the samples I left behind!

public class SearchUserFragment extends Fragment {

    public static final String TAG = "SearchUsersFragment";
    private RecyclerView rvUsers;
    protected UserAdapter adapter;
    protected List<User> users;
    protected SwipeRefreshLayout swipeContainer;


    public SearchUserFragment() {
        // Required empty public constructor
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
        adapter = new UserAdapter(getContext(), users);

        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));


        queryUsers();

    }

    private void queryUsers() {
        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        //dont think we need query include for the general one since it is to include the user but im not sure

        //do we want a limit?
        //order it maybe by who is closest? for now just order by when the user was made
        query.addDescendingOrder(User.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> objects, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue getting users", e);
                    return;
                }

                for(User user: objects){
                    //for testing purposes

                    Log.i(TAG, "Username: " + user.getUsername() + " Pin: " + user.getPin());

                }

                adapter.clear();
                users.addAll(objects);
                adapter.notifyDataSetChanged();

            }
        });

    }
}