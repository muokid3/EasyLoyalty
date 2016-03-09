package com.easycoach.easyloyalty.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by muoki on 2/1/2016.
 */
public class UserLocalStore {
    public static String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context)
    {
        userLocalDatabase = context.getSharedPreferences(SP_NAME,0);
    }

    public void storeUserData(User user)
    {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("name", user.name);
        spEditor.putString("branch", user.branch);
        spEditor.putString("ID", user.ID);
        spEditor.putString("password", user.password);
        spEditor.commit();
    }

    public User getLoggedInUser()
    {
        String name = userLocalDatabase.getString("name","");
        String ID = userLocalDatabase.getString("ID","");
        String branch = userLocalDatabase.getString("branch","");
        String password = userLocalDatabase.getString("password","");


        User newUser = new User(name, ID, branch, password);
        return newUser;
    }

    public void setUserLoggedIn(boolean loggedIn)
    {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.commit();
    }

    public boolean getBoolUserLoggedIn()
    {
        if (userLocalDatabase.getBoolean("loggedIn", false) == true)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public void clearUserData()
    {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }
}
