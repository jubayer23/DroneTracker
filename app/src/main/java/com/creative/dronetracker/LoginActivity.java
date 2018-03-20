package com.creative.dronetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.dronetracker.Utility.CommonMethods;
import com.creative.dronetracker.Utility.DeviceInfoUtils;
import com.creative.dronetracker.Utility.RunnTimePermissions;
import com.creative.dronetracker.alertbanner.AlertDialogForAnything;
import com.creative.dronetracker.appdata.GlobalAppAccess;
import com.creative.dronetracker.appdata.MydApplication;
import com.creative.dronetracker.model.Login;
import com.creative.dronetracker.model.User;

public class LoginActivity extends BaseActivity implements View.OnClickListener{


    private Button btn_submit;
    private EditText ed_username, ed_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * If User is already logged-in then redirect user to the home page
         * */
        if (MydApplication.getInstance().getPrefManger().getUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        init();

        initializeCacheValue();

        RunnTimePermissions.requestForAllRuntimePermissions(this);
    }

    private void init(){
        ed_username = (EditText) findViewById(R.id.ed_username);
        ed_password = (EditText) findViewById(R.id.ed_password);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
    }

    private void initializeCacheValue() {
        ed_username.setText(MydApplication.getInstance().getPrefManger().getUsernameCache());
    }
    private void saveCache(String username) {
        MydApplication.getInstance().getPrefManger().setUsernameCache(username);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (!DeviceInfoUtils.isConnectingToInternet(LoginActivity.this)) {
            AlertDialogForAnything.showAlertDialogWhenComplte(LoginActivity.this, "Error", "Please connect to a working internet connection!", false);
            return;
        }

        if (!DeviceInfoUtils.isGooglePlayServicesAvailable(LoginActivity.this)) {
            AlertDialogForAnything.showAlertDialogWhenComplte(this, "Warning", "This app need google play service to work properly. Please install it!!", false);
            return;
        }

        if (!RunnTimePermissions.requestForAllRuntimePermissions(this)){
            return;
        }

        if (id == R.id.btn_submit) {
            //if (isValidCredentialsProvided()) {

            CommonMethods.hideKeyboardForcely(this, ed_username);
            CommonMethods.hideKeyboardForcely(this, ed_password);

            saveCache(ed_username.getText().toString());

            sendRequestForLogin(GlobalAppAccess.URL_LOGIN, ed_username.getText().toString(), ed_password.getText().toString());
            // }
        }
    }


    public void sendRequestForLogin(String url, final String username, final String password) {

        url = url + "?" + "username=" + username + "&password=" + password;
        // TODO Auto-generated method stub
        showProgressDialog("Loading..", true, false);

        final StringRequest req = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("DEBUG",response);

                        dismissProgressDialog();

                        Login login = MydApplication.gson.fromJson(response, Login.class);

                        if(login.getSuccess() == 1){

                            User user = login.getUser();
                            user.setUsername(username);

                            MydApplication.getInstance().getPrefManger().setUser(user);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        }else{
                            AlertDialogForAnything.showAlertDialogWhenComplte(LoginActivity.this,"Error","Wrong login information!",false);
                        }



                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dismissProgressDialog();

                AlertDialogForAnything.showAlertDialogWhenComplte(LoginActivity.this, "Error", "Network problem. please try again!", false);

            }
        })// {

                // @Override
                // protected Map<String, String> getParams() throws AuthFailureError {
                //    Map<String, String> params = new HashMap<String, String>();
                //    params.put("email", email);
                //     params.put("password", password);
                ///     return params;
                //  }
                //}
                ;

        req.setRetryPolicy(new DefaultRetryPolicy(60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        MydApplication.getInstance().addToRequestQueue(req);
    }
}
