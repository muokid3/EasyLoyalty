package com.easycoach.easyloyalty.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by muoki on 2/13/2016.
 */
public class SampleBC extends BroadcastReceiver {
    static int noOfTimes = 0;
    @Override
    public void onReceive(final Context context, Intent intent) {
        noOfTimes++;

        //Toast.makeText(context, "BC Service running for "+noOfTimes+" times.", Toast.LENGTH_LONG).show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

         client.post("http://loyalty.hallsam.com/mysqlsqlitesync/getbrowcount.php", params, new AsyncHttpResponseHandler(){
             @Override
             public void onSuccess(String response) {

                 try
                 {
                     JSONObject jsonObject = new JSONObject(response);

                     if (jsonObject.getInt("count")!=0)
                     {
                         final Intent intent1 = new Intent(context, MyService.class);
                         intent1.putExtra("intentData", "Unsynched rows count "+jsonObject.getInt("count"));
                         context.startService(intent1);

                     }
                     else
                     {
                         //Toast.makeText(context, "Sync not needed", Toast.LENGTH_SHORT).show();
                     }

                 }
                 catch (JSONException e)
                 {
                     e.printStackTrace();
                 }

             }

             @Override
             public void onFailure(int statusCode, Throwable error, String content) {

                 if(statusCode == 404){
                     Toast.makeText(context, "Error 404", Toast.LENGTH_SHORT).show();
                 }else if(statusCode == 500){
                     Toast.makeText(context, "Error 500", Toast.LENGTH_SHORT).show();
                 }else{
                     //Toast.makeText(context, "Error occured!"+error.toString(), Toast.LENGTH_SHORT).show();
                 }

             }
         });

    }
}
