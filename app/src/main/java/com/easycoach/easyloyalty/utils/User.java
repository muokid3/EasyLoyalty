package com.easycoach.easyloyalty.utils;

/**
 * Created by muoki on 2/1/2016.
 */
public class User {
    public String name;
    public String ID;
    public String branch;
    public String password;

    public User(String name, String ID, String branch, String password)
    {
        this.name = name;
        this.ID = ID;
        this.branch = branch;
        this.password = password;
    }
}
