package com.tpu.mobile.timetracker;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.apollographql.apollo.ApolloClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;

/**
 * Created by Igorek on 22.11.2017.
 */

public class MainApplication extends Application {
    Realm realm;
    GoogleSignInAccount account;
    OkHttpClient client;
    ApolloClient apolloClient;
    SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        //realm = Realm.getDefaultInstance();
        RealmConfiguration config = new RealmConfiguration.Builder().inMemory().name("cache-realm").build();
        realm = Realm.getInstance(config);
        account = GoogleSignIn.getLastSignedInAccount(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        preferences.edit().putBoolean("sync", false).commit();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        X509TrustManager trustManager = createInsecureTrustManager();
        SSLSocketFactory sslSocketFactory = createInsecureSslSocketFactory(trustManager);
        builder.sslSocketFactory(sslSocketFactory, trustManager);
        builder.hostnameVerifier(createInsecureHostnameVerifier());
        client = builder.build();
        apolloClient = ApolloClient.builder()
                .serverUrl(getString(R.string.server_url))
                .okHttpClient(client)
                .build();
    }

    public Realm getRealm() {
        return realm;
    }

    public GoogleSignInAccount getAccount() {
        return account;
    }

    public void setAccount(GoogleSignInAccount account) {
        this.account = account;
    }

    public OkHttpClient getClient()
    {
        return client;
    }

    public ApolloClient getApolloClient()
    {
        return apolloClient;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    private static X509TrustManager createInsecureTrustManager() {
        return new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
            @Override public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static SSLSocketFactory createInsecureSslSocketFactory(TrustManager trustManager) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {trustManager}, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static HostnameVerifier createInsecureHostnameVerifier() {
        return new HostnameVerifier() {
            @Override public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
    }
}
