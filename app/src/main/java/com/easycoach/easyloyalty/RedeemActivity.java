package com.easycoach.easyloyalty;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RedeemActivity extends AppCompatActivity {

    private Toolbar toolbar;
    Button redeem;
    ScrollView firstView, secondView;
    NfcAdapter nfcAdapter;
    EditText pinET, pointsET;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private String accountNo, points, pin;
    UserLocalStore userLocalStore;

    public static final String REGISTER_URL = "http://loyalty.hallsam.com/redeemPoints.php";

    public static final String KEY_ACCOUNT_NO = "accountNo";
    public static final String KEY_PIN = "pin";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_AGENT_NAME = "name";
    public static final String KEY_BRANCH = "branch";

    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        firstView = (ScrollView)findViewById(R.id.firstView);
        secondView = (ScrollView)findViewById(R.id.secondView);
        secondView.setVisibility(View.INVISIBLE);

        userLocalStore = new UserLocalStore(this);

        pinET = (EditText)findViewById(R.id.pin);
        pointsET = (EditText)findViewById(R.id.points);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        redeem = (Button)findViewById(R.id.redeem);
        redeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEditTexts())
                {
                    pDialog.show();
                    pin = pinET.getText().toString();
                    points = pointsET.getText().toString();
                    processPointsRedeem(accountNo, pin, points);
                }
            }
        });

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);

        navigationDrawerFragment.setup(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDiscovered = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        intentFilters = new IntentFilter[]{tagDiscovered};

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate()==false)
        {
            startActivity(new Intent(RedeemActivity.this, LoginActivity.class));
        }
    }
    private boolean authenticate()
    {
        return userLocalStore.getBoolUserLoggedIn();
    }

    private boolean validateEditTexts()
    {
        boolean valid = true;

        if(pointsET.getText().toString().trim().equals(""))
        {
            pointsET.setError("Please Enter Number of Points");
            valid = false;
        }

        if(pinET.getText().toString().trim().equals(""))
        {
            pinET.setError("Please provide a PIN");
            valid = false;
        }

        return valid;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (parcelables != null && parcelables.length>0)
        {
            readTextFromMessage((NdefMessage) parcelables[0]);
        }
        else
        {
            Toast.makeText(this, "No NDEF message found", Toast.LENGTH_LONG).show();
        }
        super.onNewIntent(intent);
    }


    private void readTextFromMessage(NdefMessage ndefMessage)
    {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if (ndefRecords != null && ndefRecords.length>0)
        {
            NdefRecord ndefRecord = ndefRecords[0];

            String tagContent = getTextFromNdefRecord(ndefRecord);

            this.accountNo=tagContent;
            firstView.setVisibility(View.INVISIBLE);
            secondView.setVisibility(View.VISIBLE);

        }
        else
        {
            Toast.makeText(this, "Sorry, this card does not have any information", Toast.LENGTH_LONG).show();
        }

    }


    public String getTextFromNdefRecord (NdefRecord ndefRecord)
    {
        String tagContent = null;

        try
        {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding;
            if ((payload[0] & 128) == 0) textEncoding = "UTF-8";
            else textEncoding = "UTF-16";
            int languageSize = payload[0] & 0063;

            tagContent = new String(payload, languageSize+1,payload.length-languageSize-1, textEncoding);

        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("fail: ", e.getMessage());
        }
        return tagContent;
    }


    protected void onPause() {
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();

        if (nfcAdapter != null)
        {
            if (!nfcAdapter.isEnabled())
            {

                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.nfc_off,(ViewGroup) findViewById(R.id.action_settings));
                new AlertDialog.Builder(this).setView(dialoglayout)
                        .setPositiveButton("Go to NFC settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(setnfc);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {

                            public void onCancel(DialogInterface dialog) {
                                finish(); // exit application if user cancels
                            }
                        }).create().show();

            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
        }
        else
        {
            Toast.makeText(this, "Sorry, this device is not NFC enabled", Toast.LENGTH_LONG).show();
        }

    }

    private void processPointsRedeem(final String accountNo, final String pin, final String points)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        pDialog.dismiss();
                        if (response.equals("success"))
                        {
                            pDialog.dismiss();

                            
                            new SweetAlertDialog(RedeemActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success!")
                                    .setContentText("You have successfully redeemed " + points + " points!")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            startActivity(new Intent(RedeemActivity.this, MainActivity.class));
                                        }

                                    })
                                    .show();

                        }
                        else if (response.equals("pinFail"))
                        {
                            pDialog.dismiss();

                            Displays.displayErrorAlert("Error", "Sorry, you entered a wrong PIN", RedeemActivity.this);

                        }
                        else if (response.equals("pointsFail"))
                        {
                            pDialog.dismiss();

                            Displays.displayErrorAlert("Error", "Sorry, you do not have enough points to redeem "+points+" points", RedeemActivity.this);

                        }
                        else
                        {
                            pDialog.dismiss();

                            Displays.displayErrorAlert("Error", "Sorry, an error ocurred during your transaction. Please try again", RedeemActivity.this);

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                pDialog.dismiss();

                Displays.displayErrorAlert("Error", VolleyErrors.getVolleyErrorMessages(error, RedeemActivity.this), RedeemActivity.this);

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map <String, String> params = new HashMap<String, String>();
                params.put(KEY_ACCOUNT_NO, accountNo);
                params.put(KEY_AMOUNT, points);
                params.put(KEY_PIN, pin);
                params.put(KEY_AGENT_NAME, userLocalStore.getLoggedInUser().name);
                params.put(KEY_BRANCH, userLocalStore.getLoggedInUser().branch);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



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
