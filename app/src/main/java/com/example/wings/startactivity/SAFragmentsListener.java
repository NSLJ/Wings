package com.example.wings.startactivity;

import com.example.wings.models.User;
import com.example.wings.models.inParseServer.TrustedContact;

import java.util.List;

public interface SAFragmentsListener {
    public void onLogin(String key);
    public void toLoginFragment();
    public void toRegisterOneFragment();
    public void toRegisterTwoFragment(User user);
}
