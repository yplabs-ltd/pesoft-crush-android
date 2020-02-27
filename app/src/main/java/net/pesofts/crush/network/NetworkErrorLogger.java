package net.pesofts.crush.network;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import net.pesofts.crush.Util.OneSignalUtil;
import net.pesofts.crush.Util.SharedPrefHelper;
import net.pesofts.crush.activity.StartActivity;
import net.pesofts.crush.model.Result;

public class NetworkErrorLogger extends Interceptor {

    private Context context;

    public NetworkErrorLogger(Context context) {
        this.context = context;
    }

    @Override
    public void onError(HttpNetworkError error) {
        if (error.getResponseBody() != null) {
            try {
                Gson gson = new Gson();
                Result result = gson.fromJson(error.getResponseBody(), Result.class);
                if (result != null && !"Undefiened".equals(result.getCode())) {

                    if (!TextUtils.isEmpty(result.getDescription())) {
                        Toast.makeText(context, result.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    // TODO : 확인해볼것
                    if ("CrushAccessTokenError".equals(result.getCode())) {
                        OneSignalUtil.deleteTags();
                        SharedPrefHelper.getInstance(context).removeAllSharedPreferences();

                        Intent intent = new Intent(context, StartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }
                }
                return;
            } catch (Exception e) {
            }
        }
//        Toast.makeText(context, Integer.toString(error.getStatusCode()), Toast.LENGTH_LONG).show();
    }
}
