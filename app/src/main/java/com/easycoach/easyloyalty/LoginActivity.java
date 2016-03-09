package com.easycoach.easyloyalty;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.easycoach.easyloyalty.utils.Displays;
import com.easycoach.easyloyalty.utils.User;
import com.easycoach.easyloyalty.utils.UserLocalStore;
import com.easycoach.easyloyalty.utils.VolleyErrors;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText idNo, password;
    public static String idNumber, pass;
    Button login;
    StringBuilder stringBuilder;
    String loginUrl;
    String baseUrl = "http://loyalty.hallsam.com/login.php";
    SweetAlertDialog pDialog;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Logging In");
        pDialog.setCancelable(false);

        idNo = (EditText)findViewById(R.id.idNo);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.login);

        userLocalStore = new UserLocalStore(this);








        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                idNumber = idNo.getText().toString();
                pass = password.getText().toString();

                stringBuilder = new StringBuilder(baseUrl);

                stringBuilder.append("?idNo="+idNumber);
                stringBuilder.append("&password="+pass);

                loginUrl = stringBuilder.toString();
                if (validateEditTexts())
                {
                    pDialog.show();
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, loginUrl, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response)
                                {
                                    try
                                    {
                                        String message = response.getString("message");

                                        if (message.equals("success"))
                                        {
                                            pDialog.dismiss();
                                            String name = response.getString("name");
                                            String branch = response.getString("branch");


                                            User returnedUser = new User(name, idNumber, branch, pass);
                                            logUserIn(returnedUser);

                                        }
                                        else if (message.equals("loginFail"))
                                        {
                                            pDialog.dismiss();
                                            Displays.displayWarningAlert("Failure", "Wrong ID Number or Password", LoginActivity.this);
                                        }
                                        else
                                        {
                                            pDialog.dismiss();
                                            Displays.displayWarningAlert("Failure", "A fatal system error occurred. Please try again later", LoginActivity.this);
                                        }
                                    }
                                    catch (JSONException je)
                                    {
                                        pDialog.dismiss();

                                        Displays.displayWarningAlert("Failure", "A JSON Exception occurred. Please contact admin", LoginActivity.this);
                                    }


                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            pDialog.dismiss();

                            Displays.displayErrorAlert("Error", VolleyErrors.getVolleyErrorMessages(error, LoginActivity.this), LoginActivity.this);

                        }
                    });

                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                    requestQueue.add(jsonObjectRequest);

                }

            }
        });

    }

    private void logUserIn(User returnedUser)
    {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);

        Intent logIn = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(logIn);
        finish();
    }

    private boolean validateEditTexts()
    {
        boolean valid = true;

        if(password.getText().toString().trim().equals(""))
        {
            password.setError("Please provide a password");
            valid = false;
        }

        if(idNo.getText().toString().trim().equals(""))
        {
            idNo.setError("Please provide an ID NUmber");
            valid = false;
        }

        return valid;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "You just hit me!", Toast.LENGTH_LONG);
        }



        return super.onOptionsItemSelected(item);
    }

}
