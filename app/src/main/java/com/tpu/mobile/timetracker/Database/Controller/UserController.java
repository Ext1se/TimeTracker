package com.tpu.mobile.timetracker.Database.Controller;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.tpu.mobile.timetracker.Database.Model.Project;
import com.tpu.mobile.timetracker.Database.Model.Task;
import com.tpu.mobile.timetracker.Database.Model.User;
import com.tpu.mobile.timetracker.Main.MainActivity;

import api.type.UserInput;
import io.realm.Realm;

/**
 * Created by Igorek on 27.12.2017.
 */

public class UserController {
    private Realm realm;

    public UserController(Realm realm)
    {
        this.realm = realm;
    }

    public void createUser(String id, GoogleSignInAccount account)
    {
        User user = new User();
        user.setId(id);
        user.setUsername(account.getDisplayName());
        user.setEmail(account.getEmail());
        user.setPhoto(account.getPhotoUrl().toString());
        realm.beginTransaction();
        realm.copyToRealm(user);
        realm.commitTransaction();
    }

    public void updateUser(String id, String name, String username)
    {
        User user = realm.where(User.class).equalTo("id", id).findFirst();
        realm.beginTransaction();
        user.setUsername(name);
        user.setName(username);
        realm.commitTransaction();
    }

    public User getUserByID(String id)
    {
        return realm.where(User.class).equalTo("id", id).findFirst();
    }

    public User getUserByEmail(String email)
    {
        return realm.where(User.class).equalTo("email", email).findFirst();
    }
}
