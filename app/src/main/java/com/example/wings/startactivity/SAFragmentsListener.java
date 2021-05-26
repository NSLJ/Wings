package com.example.wings.startactivity;

import com.example.wings.models.User;

public interface SAFragmentsListener {
    public void onLogin(String key);
    public void toLoginFragment();
    public void toRegisterOneFragment();
    public void toRegisterTwoFragment(User user);
    public void toProfileSetupFragment();
    public void toEditTrustedContacts();
    public void toSettingsFragment();
    public void toHelpFragment();
}
