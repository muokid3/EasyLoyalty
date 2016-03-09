package com.easycoach.easyloyalty.utils;

import android.content.Context;
import android.graphics.Color;
import com.easycoach.easyloyalty.R;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by muoki on 1/28/2016.
 */
public class Displays {
    public static void displayErrorAlert (String tittle, String message, Context context)
    {
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
        .setTitleText(tittle)
        .setContentText(message)
        .setConfirmText("Ok")
        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }

        })
         .show();
    }

    public static void displayWarningAlert (String tittle, String message, Context context)
    {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(tittle)
                .setContentText(message)
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }

                })
                .show();
    }
}
