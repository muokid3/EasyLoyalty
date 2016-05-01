package com.easycoach.easyloyalty;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.easycoach.easyloyalty.utils.Displays;
import com.easycoach.easyloyalty.utils.UserLocalStore;
import com.easycoach.easyloyalty.utils.VolleyErrors;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    EditText cName, IDNo, cPhone, cEmail;
    Button registerButton;
    UserLocalStore userLocalStore;
    SweetAlertDialog registeredDialog;

    public static final String REGISTER_URL = "http://loyalty.hallsam.com/volleyRegister.php";

    public static final String KEY_NAME = "name";
    public static final String KEY_ID = "idNo";
    public static final String KEY_PHONE = "phoneNo";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_AGENT_NAME = "transactedBy";
    public static final String KEY_BRANCH = "branch";

    SweetAlertDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        userLocalStore = new UserLocalStore(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);

        navigationDrawerFragment.setup(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);


        cName = (EditText)findViewById(R.id.cName);
        IDNo = (EditText)findViewById(R.id.IDNo);
        cPhone = (EditText)findViewById(R.id.cPhone);
        cEmail = (EditText)findViewById(R.id.cEmail);
        registerButton = (Button)findViewById(R.id.btnRegister);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (validateEditTexts())
               {
                   pDialog.show();
               }
                registerClient();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate()==false)
        {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        }
    }

    private boolean authenticate()
    {
        return userLocalStore.getBoolUserLoggedIn();
    }

    private void registerClient()
    {
        final String customerName = cName.getText().toString().trim();
        final String customerId = IDNo.getText().toString().trim();
        final String customerPhone = cPhone.getText().toString().trim();
        final String customerEmail = cEmail.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.equals("registered"))
                        {
                            registeredDialog = new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.WARNING_TYPE);
                            registeredDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                            registeredDialog.setTitleText("Warning!");
                            registeredDialog.setContentText("The provided ID Number is already registered!");
                            registeredDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    Bundle b = new Bundle();
                                    b.putBoolean("new_window", true); //sets new window
                                    intent.putExtras(b);
                                    startActivity(intent);
                                }
                            });

                            registeredDialog.show();

                        }
                        else
                        {
                            Intent writeCard = new Intent(RegisterActivity.this, WriteCardActivity.class);
                            Bundle b = new Bundle();
                            b.putString("accountNo", response);
                            writeCard.putExtras(b);


                            startActivity(writeCard);
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                pDialog.dismiss();

                Displays.displayErrorAlert("Error", VolleyErrors.getVolleyErrorMessages(error, RegisterActivity.this), RegisterActivity.this);

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map <String, String> params = new HashMap<String, String>();
                params.put(KEY_NAME, customerName);
                params.put(KEY_EMAIL, customerEmail);
                params.put(KEY_ID, customerId);
                params.put(KEY_PHONE, customerPhone);
                params.put(KEY_AGENT_NAME, userLocalStore.getLoggedInUser().name);
                params.put(KEY_BRANCH, userLocalStore.getLoggedInUser().branch);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if (validateEditTexts())
        {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }


    }

    private boolean validateEditTexts()
    {
        boolean valid = true;

        if(cName.getText().toString().trim().equals("") )
        {
            cName.setError("Please provide a name");
            valid = false;
        }

        if(IDNo.getText().toString().trim().equals(""))
        {
            IDNo.setError("Please provide an ID Number");
            valid = false;
        }

        if (IDNo.getText().toString().length() > 10)
        {
            IDNo.setError("ID Number must not exceed 10 characters");
            valid = false;
        }

        if(cPhone.getText().toString().trim().equals(""))
        {
            cPhone.setError("Please provide a Phone Number");
            valid = false;
        }

        if (cPhone.getText().toString().length() > 10)
        {
            cPhone.setError("Phone Number must not exceed 10 characters");
            valid = false;
        }

        return valid;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
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

        if (id == R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
