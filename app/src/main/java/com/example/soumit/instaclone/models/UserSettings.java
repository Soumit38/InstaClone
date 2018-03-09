package com.example.soumit.instaclone.models;

/**
 * Created by Soumit on 2/28/2018.
 */

public class UserSettings {

    /**
     *  User and UserAccountSettings combined to one model
     */
    private User user;
    private UserAccountSettings settings;

    public UserSettings() {
    }

    public UserSettings(User user, UserAccountSettings settings) {
        this.user = user;
        this.settings = settings;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAccountSettings getSettings() {
        return settings;
    }

    public void setSettings(UserAccountSettings settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "user=" + user +
                ", settings=" + settings +
                '}';
    }
}
