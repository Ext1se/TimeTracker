package com.tpu.mobile.timetracker.Database.Controller;

import org.json.JSONException;

/**
 * Created by Igorek on 30.09.2017.
 */

public interface ApolloResponse {
    public void onNext(boolean success);
    public void onError();
    public void onComplete();
}
