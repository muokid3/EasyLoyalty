package com.easycoach.easyloyalty;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.easycoach.easyloyalty.utils.Displays;
import com.easycoach.easyloyalty.utils.EasyDBHelper;
import com.easycoach.easyloyalty.utils.SampleBC;
import com.easycoach.easyloyalty.utils.UserLocalStore;
import com.easycoach.easyloyalty.utils.VolleyErrors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Toolbar toolbar;
    Button proceed;
    RadioButton payTypeButton;
    RadioGroup payTypeGroup;
    ScrollView firstView, secondView;
    String transType, amount, pin, spinFromString, spinToString;
    NfcAdapter nfcAdapter;
    EditText pinET;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private String accountNo;

    public static final String TRANSACT_URL = "http://loyalty.hallsam.com/transact.php";

    public static final String KEY_ACCOUNT_NO = "accountNo";
    public static final String KEY_PAY_TYPE = "payType";
    public static final String KEY_TRANS_TYPE = "transType";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_PIN = "pin";
    public static final String KEY_AGENT_NAME = "name";
    public static final String KEY_BRANCH = "branch";

    SweetAlertDialog pDialog, syncPDialog, syncSuccessPdialog, routeUnavailablePdialog;

    Spinner spinTo, spinFrom;

    UserLocalStore userLocalStore;
    EasyDBHelper easyDBHelper;
    HashMap<String, String> queryValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userLocalStore = new UserLocalStore(this);

        easyDBHelper = new EasyDBHelper(this);
        //SQLiteDatabase easySQliteDb = easyDBHelper.getWritableDatabase();


        toolbar = (Toolbar) findViewById(R.id.app_bar);
        firstView = (ScrollView)findViewById(R.id.firstView);
        secondView = (ScrollView)findViewById(R.id.secondView);
        secondView.setVisibility(View.INVISIBLE);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        syncPDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        syncPDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        syncPDialog.setTitleText("Syncing...");
        syncPDialog.setContentText("Transferring Data from Remote MySQL DB and Syncing SQLite. Please wait...");
        syncPDialog.setCancelable(false);

        syncSuccessPdialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        syncSuccessPdialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        syncSuccessPdialog.setTitleText("Success!");
        syncSuccessPdialog.setContentText("Database Synced Successfully!");
        syncSuccessPdialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                reloadActivity();
            }
        });

        routeUnavailablePdialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        routeUnavailablePdialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        routeUnavailablePdialog.setTitleText("Warning!");
        routeUnavailablePdialog.setContentText("This route has not yet been registered");
        routeUnavailablePdialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });

        pinET = (EditText)findViewById(R.id.pin);


        payTypeGroup = (RadioGroup)findViewById(R.id.payTypeRadioGroup);


        proceed = (Button)findViewById(R.id.proceed);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEditTexts())
                {
                    pin = pinET.getText().toString();

                    int selected = payTypeGroup.getCheckedRadioButtonId();

                    payTypeButton = (RadioButton) findViewById(selected);

                    if (transType.equals("Travel"))
                    {
                        amount = easyDBHelper.getRouteTravelAmount(spinFromString, spinToString);

                        if (payTypeButton.getText().toString().equals("Cash")) {

                            if (amount.equals("0"))
                            {
                                routeUnavailablePdialog.show();
                            }
                            else
                            {
                                pDialog.show();
                                authenticateUserCashPayment(amount, pin, accountNo);
                            }
                        }
                        else
                        {
                            pDialog.show();
                            authenticateUserCardPayment(amount, pin, accountNo);
                        }
                    }
                    else
                    {
                        amount = easyDBHelper.getRouteParcelAmount(spinFromString, spinToString);

                        if (payTypeButton.getText().toString().equals("Cash"))
                        {

                            if (amount.equals("0"))
                            {
                                routeUnavailablePdialog.show();
                            }
                            else
                            {
                                pDialog.show();
                                authenticateUserCashPayment(amount, pin, accountNo);
                            }
                        }
                        else
                        {
                            pDialog.show();
                            authenticateUserCardPayment(amount, pin, accountNo);
                        }
                    }
                }
            }
        });

        Spinner transType = (Spinner)findViewById(R.id.transType);
        transType.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.transType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transType.setAdapter(adapter);

        spinFrom = (Spinner)findViewById(R.id.spinFrom);
        spinTo = (Spinner)findViewById(R.id.spinTo);


        ArrayAdapter<CharSequence> fromAdapter = ArrayAdapter.createFromResource(this, R.array.spinFrom, android.R.layout.simple_spinner_item);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinFrom.setAdapter(fromAdapter);
        spinFrom.setOnItemSelectedListener(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);

        navigationDrawerFragment.setup(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        intentFilters = new IntentFilter[]{tagDiscovered};

        Intent alarmIntent = new Intent(getApplicationContext(), SampleBC.class);
        PendingIntent newPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 5000, 10 * 1000, newPendingIntent);


    }

    public void launchIntent()
    {

        firstView.setVisibility(View.INVISIBLE);
        secondView.setVisibility(View.VISIBLE);
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

    private boolean validateEditTexts()
    {
        boolean valid = true;


        if(pinET.getText().toString().trim().equals(""))
        {
            pinET.setError("Please provide a PIN");
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

        if (id == R.id.refresh) {
            syncSQLiteMySQLDB();
            return true;
        }

        if (id == R.id.intent) {
            launchIntent();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //case transtype spinfrom

        int spinnerId = parent.getId();

        switch (spinnerId)
        {
            case R.id.transType:
                transType = (String) parent.getItemAtPosition(position);
                break;
            case R.id.spinFrom:
                spinFromString = (String) parent.getItemAtPosition(position);

                if (spinFromString.equals("Nairobi"))
                {
                    ArrayAdapter<CharSequence> toAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.spinTo1, android.R.layout.simple_spinner_item);
                    toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinTo.setAdapter(toAdapter);
                    spinTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            spinToString = (String) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                else if (spinFromString.equals("Nakuru"))
                {
                    ArrayAdapter<CharSequence> toAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.spinTo2, android.R.layout.simple_spinner_item);
                    toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinTo.setAdapter(toAdapter);
                    spinTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            spinToString = (String) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                else if (spinFromString.equals("Eldoret"))
                {
                    ArrayAdapter<CharSequence> toAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.spinTo3, android.R.layout.simple_spinner_item);
                    toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinTo.setAdapter(toAdapter);
                    spinTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            spinToString = (String) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                else if (spinFromString.equals("Kisumu"))
                {
                    ArrayAdapter<CharSequence> toAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.spinTo4, android.R.layout.simple_spinner_item);
                    toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinTo.setAdapter(toAdapter);
                    spinTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            spinToString = (String) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

                else {
                    ArrayAdapter<CharSequence> toAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.spinFrom, android.R.layout.simple_spinner_item);
                    toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinTo.setAdapter(toAdapter);
                    spinTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            spinToString = (String) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing nigga
    }

    //cash payments
    private void authenticateUserCashPayment(final String amount, final String pin, final String accountNo)
    {
        final String payType = payTypeButton.getText().toString();
        //Toast.makeText(MainActivity.this, payTypeButton.getText().toString()+amount+"pin"+accountNo+"transType"+transType, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, TRANSACT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        pDialog.dismiss();
                        if (response.equals("success"))
                        {
                            pDialog.dismiss();


                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Success!")
                                    .setContentText("Transaction Recorded Successfully")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        }

                                    })
                                    .show();

                        }
                        else if (response.equals("pinFail"))
                        {
                            pDialog.dismiss();

                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("Sorry, you entered an incorrect PIN")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        }

                                    })
                                    .show();

                            //Displays.displayErrorAlert("Error", "Sorry, you entered an incorrect PIN", MainActivity.this);

                        }

                        else if (response.equals("wait"))
                        {
                            pDialog.dismiss();

                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Warning")
                                    .setContentText("Sorry, Card payments are not yet supported")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        }

                                    })
                                    .show();

                            //Displays.displayWarningAlert("Warning", "Sorry, Card payments are not yet supported", MainActivity.this);

                        }
                        else
                        {
                            pDialog.dismiss();

                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("Sorry, an error occurred during your transaction. Please try again")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        }

                                    })
                                    .show();

                            //Displays.displayErrorAlert("Error", "Sorry, an error occurred during your transaction. Please try again", MainActivity.this);

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                pDialog.dismiss();

                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText(VolleyErrors.getVolleyErrorMessages(error, MainActivity.this))
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                startActivity(new Intent(MainActivity.this, MainActivity.class));
                            }

                        })
                        .show();

                //Displays.displayErrorAlert("Error", VolleyErrors.getVolleyErrorMessages(error, MainActivity.this), MainActivity.this);

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map <String, String> params = new HashMap<String, String>();
                params.put(KEY_ACCOUNT_NO, accountNo);
                params.put(KEY_AMOUNT, amount);
                params.put(KEY_PAY_TYPE, payType);
                params.put(KEY_PIN, pin);
                params.put(KEY_TRANS_TYPE, transType);
                params.put(KEY_AGENT_NAME, userLocalStore.getLoggedInUser().name);
                params.put(KEY_BRANCH, userLocalStore.getLoggedInUser().branch);
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authenticate()==false)
        {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    private boolean authenticate()
    {
        return userLocalStore.getBoolUserLoggedIn();
    }

    //card payments
    private void authenticateUserCardPayment(String amount, String pin, String accountNo)
    {
        pDialog.dismiss();


        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Warning")
                .setContentText("Sorry, Card payments are not yet supported")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }

                })
                .show();


        //Displays.displayWarningAlert("Warning", "Sorry, Card payments are not yet supported", MainActivity.this);
        //Toast.makeText(MainActivity.this, payTypeButton.getText().toString()+amount+"pin"+pin+"transType"+transType, Toast.LENGTH_LONG).show();
    }


    public void syncSQLiteMySQLDB()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();

        syncPDialog.show();

        client.post("http://loyalty.hallsam.com/mysqlsqlitesync/getusers.php", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                syncPDialog.hide();
                updateSQLite(response);

            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                syncPDialog.hide();

                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet]",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }



    public void updateSQLite(String response)
    {
        ArrayList<HashMap<String, String>> userSyncList;
        userSyncList = new ArrayList<HashMap<String, String>>();

        Gson gson = new GsonBuilder().create();

        try
        {



            JSONArray arr = new JSONArray(response);
            if (arr.length() != 0)
            {
                for (int i=0; i<arr.length(); i++)
                {
                    JSONObject jsonObject = (JSONObject) arr.get(i);
                    queryValues = new HashMap<String, String>();

                    queryValues.put("u_id", jsonObject.get("u_id").toString());
                    queryValues.put("destination_from", jsonObject.get("destination_from").toString());
                    queryValues.put("destination_to", jsonObject.get("destination_to").toString());
                    queryValues.put("parcel_charge", jsonObject.get("parcel_charge").toString());
                    queryValues.put("travel_charge", jsonObject.get("travel_charge").toString());

                    easyDBHelper.insertPrice(queryValues);

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Id", jsonObject.get("u_id").toString());
                    map.put("status", "1");
                    userSyncList.add(map);
                }

            }

            updateMySQLSyncStatus(gson.toJson(userSyncList));

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }


    public void updateMySQLSyncStatus(String json)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("syncsts", json);

        client.post("http://loyalty.hallsam.com/mysqlsqlitesync/updatesyncsts.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                //Toast.makeText(getApplicationContext(), "MySQL DB has been informed about Sync activity", Toast.LENGTH_LONG).show();
                syncSuccessPdialog.show();
            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                //Toast.makeText(getApplicationContext(), "An Error Occured", Toast.LENGTH_LONG).show();

            }
        });

    }

    public void reloadActivity()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);

    }



}