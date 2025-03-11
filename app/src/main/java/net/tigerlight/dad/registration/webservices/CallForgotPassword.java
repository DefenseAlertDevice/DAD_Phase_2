package net.tigerlight.dad.registration.webservices;

import android.content.Context;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WSUtil;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M.T. on 7 Oct, 2016.
 * This class is making api call for user login
 */
public class CallForgotPassword {
    private Context context;
    private String message;
    private boolean success;

    public CallForgotPassword(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Calls the api for forgot password.
     *
     * @param email
     * @return
     */
    public JSONObject executeService(final String email) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(email));
        return parseResponse(response);
    }

    /**
     * Parse the json response from {@link String} to {@link JSONArray}.
     *
     * @param response {@link String} response that is recived from the api request.
     * @return {@link JSONArray} for success or failure response of request
     */
    private JSONObject parseResponse(final String response) {
        if (response != null && response.trim().length() > 0) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                final WsConstants wsConstants = new WsConstants();
                if (jsonObject.length() > 0) {
                    success = jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("1");

                    if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("0")) {
                        message = context.getString(R.string.forgor_password_email_not_found);
                    } else if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("1")) {
                        message = context.getString(R.string.alert_forgot_password_success);
                    } else if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("2")) {
                        message = context.getString(R.string.alert_forgot_password_error);
                    } else {
                        message = jsonObject.optString(wsConstants.PARAMS_MESSAGE);
                    }

                    if (success) {
                        return jsonObject;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Generates RequestBody for making api call for okhttp
     *
     * @param email {@link String} value for email address for the employer
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(final String email) {
        final WsConstants wsConstants = new WsConstants();

//        When this is set then throws error
        final Preference preference = Preference.getInstance();
        final StringBuilder builder = new StringBuilder();

        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_FORGOT_PASSWORD);
        builder.append("&" + wsConstants.PARAMS_EMAIL + "=" + URLEncoder.encode(email));
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        return builder.toString();
    }
}
