package net.tigerlight.dad.webservices;

import android.content.Context;

import com.net.tigerlight.dad.R;
import net.tigerlight.dad.registration.util.DadConstant;
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
public class CallGetAlertCount {
    private Context context;
    private String message;
    private boolean success;
    private String user_id;

    public CallGetAlertCount(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUser_id() {
        return user_id;
    }

    /**
     * Calls the api user Login.
     *
     * @param email
     * @param hours
     * @return
     */
    public JSONObject executeService(final String email, final String hours) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(email, hours));
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
                    user_id = jsonObject.optString("id");

                    if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("0")) {
                        message = context.getString(R.string.alert_invalid_credentials);
                    } else if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("2")) {
                        message = context.getString(R.string.alert_not_registered);
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
    private String generateLoginRequest(final String email, final String hours) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_GET_ALERT_COUNT);
        builder.append("&" + wsConstants.PARAMS_EMAIL + "=" + URLEncoder.encode(email));
        builder.append("&" + wsConstants.PARAMS_HOURS + "=" + hours);
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(DadConstant.IS_LANG_ID, ""));
        return builder.toString();
//
//        final WsConstants wsConstants = new WsConstants();
//        final Preference preference = Preference.getInstance();
//        StringBuilder builder = new StringBuilder();
//        builder.append("api_key" + "=" + "bf8fad621d2161654770d0a3d4ffbc7d");
//
////        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "sv"));
//        return builder.toString();
    }
}
