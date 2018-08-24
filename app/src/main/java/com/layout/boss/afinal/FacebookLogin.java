package com.layout.boss.afinal;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class FacebookLogin extends AppCompatActivity {
    CallbackManager callbackManager;
    ProgressDialog progressDialog;
    ImageView avarta;
    TextView tv_name, tv_email, tv_birth;
    LoginButton loginButton;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        callbackManager = CallbackManager.Factory.create();

        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_email = (TextView) findViewById(R.id.tv_email);
        tv_birth = (TextView) findViewById(R.id.tv_birth);
        avarta = (ImageView) findViewById(R.id.avarta);

        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                progressDialog = new ProgressDialog(FacebookLogin.this);
                progressDialog.setMessage("Retrieving data ...");
                progressDialog.show();

                String accesstoken = loginResult.getAccessToken().getToken();

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        progressDialog.dismiss();
                        Log.d("response", response.toString());
                        getData(object);
                    }
                });

                //Request Graph API
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, email, birthday, last_name, first_name");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        //if already login
        if(AccessToken.getCurrentAccessToken() != null){
            tv_email.setText(AccessToken.getCurrentAccessToken().getUserId());
        }

    }

    @SuppressLint("SetTextI18n")
    private void getData(JSONObject object) {
        String first_name, last_name;
        try{
            URL profile_pic = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");
            Picasso.with(this).load(profile_pic.toString()).into(avarta);
            first_name = object.getString("first_name");
            last_name = object.getString("last_name");
            tv_email.setText("Email: "+object.getString("email"));
            tv_birth.setText("Birthday: "+object.getString("birthday"));
            tv_name.setText("Name: "+first_name+" "+last_name);

        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        }
    }
}
