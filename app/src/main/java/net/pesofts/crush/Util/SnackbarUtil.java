package net.pesofts.crush.Util;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class SnackbarUtil {

    public static Snackbar getSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View innerView = snackbar.getView();
        TextView tv = (TextView) innerView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTextColor(Color.WHITE);

        return snackbar;
    }

}
