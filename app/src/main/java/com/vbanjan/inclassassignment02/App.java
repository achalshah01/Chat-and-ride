package com.vbanjan.inclassassignment02;

import android.app.Application;
import android.content.Intent;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent i = new Intent(getApplicationContext(), LoginSignUp.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(i);
    }
}
