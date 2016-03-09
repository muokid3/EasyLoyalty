package com.easycoach.easyloyalty.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.easycoach.easyloyalty.R;

/**
 * Created by muoki on 1/28/2016.
 */
public class VolleyErrors {
    /**
     * Returns appropriate message which is to be displayed to the user
     * against the specified error object.
     *
     * @param error
     * @param context
     * @return
     */
    public static String getVolleyErrorMessages(Object error, Context context)
    {

        if (error instanceof TimeoutError) {
            return context.getResources().getString(R.string.generic_server_down);
        }
        else if (isServerProblem(error)) {
            return handleServerError(error, context);
        }
        else if (isNetworkProblem(error)) {
            return context.getResources().getString(R.string.connection);
        }
        return context.getResources().getString(R.string.generic_error);
    }


    /**
     * Determines whether the error is related to network
     * @param error
     * @return
     */
    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }
    /**
     * Determines whether the error is related to server
     * @param error
     * @return
     */
    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }
    /**
     * Handles the server error, tries to determine whether to show a stock message or to
     * show a message retrieved from the server.
     *
     * @param err
     * @param context
     * @return
     */
    private static String handleServerError(Object err, Context context) {
        VolleyError error = (VolleyError) err;

        NetworkResponse response = error.networkResponse;

        if (response != null) {
            switch (response.statusCode) {
                case 400:
                    return context.getResources().getString(R.string.error_400);
                case 401:
                    return context.getResources().getString(R.string.error_401);
                case 402:
                    return context.getResources().getString(R.string.error_402);
                case 403:
                    return context.getResources().getString(R.string.error_403);
                case 404:
                    return context.getResources().getString(R.string.error_404);
                case 405:
                    return context.getResources().getString(R.string.error_405);
                case 406:
                    return context.getResources().getString(R.string.error_406);
                case 407:
                    return context.getResources().getString(R.string.error_407);
                case 408:
                    return context.getResources().getString(R.string.error_408);
                case 409:
                    return context.getResources().getString(R.string.error_409);
                case 410:
                    return context.getResources().getString(R.string.error_410);
                case 411:
                    return context.getResources().getString(R.string.error_411);
                case 412:
                    return context.getResources().getString(R.string.error_412);
                case 413:
                    return context.getResources().getString(R.string.error_413);
                case 414:
                    return context.getResources().getString(R.string.error_414);
                case 415:
                    return context.getResources().getString(R.string.error_415);
                case 416:
                    return context.getResources().getString(R.string.error_416);
                case 417:
                    return context.getResources().getString(R.string.error_417);
                case 422:
                    return context.getResources().getString(R.string.error_422);
                case 500:
                    return context.getResources().getString(R.string.error_500);
                case 501:
                    return context.getResources().getString(R.string.error_501);
                case 502:
                    return context.getResources().getString(R.string.error_502);
                case 503:
                    return context.getResources().getString(R.string.error_503);
                case 504:
                    return context.getResources().getString(R.string.error_504);
                case 505:
                    return context.getResources().getString(R.string.error_505);

                default:
                    return context.getResources().getString(R.string.generic_server_down);
            }
        }
        return context.getResources().getString(R.string.generic_error);
    }
}
