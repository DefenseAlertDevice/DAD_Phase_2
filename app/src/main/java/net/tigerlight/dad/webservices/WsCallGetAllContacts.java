package net.tigerlight.dad.webservices;

import android.content.Context;
import net.tigerlight.dad.R;
import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WSUtil;
import net.tigerlight.dad.util.WsConstants;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is making api call for user login
 */
public class WsCallGetAllContacts {
    private Context context;
    private String message;
    private boolean success;
    private String user_id;

    public WsCallGetAllContacts(final Context context) {
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
     * @return
     */
    public JSONObject executeService() {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest());
        return parseResponse(response);
    }

    /**
     * Parse the json response from {@link String} to {@link JSONArray}.
     *
     * @param response {@link String} response that is recived from the api request.
     * @return {@link JSONArray} for success or failure response of request
     */
    private JSONObject parseResponse(final String response) {
        JSONObject jsonObject = null;
        if (response != null && response.trim().length() > 0) {
            try {
                jsonObject = new JSONObject(response);
                final WsConstants wsConstants = new WsConstants();
                if (jsonObject.length() > 0) {
                    success = jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("1");


                    if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("0")) {
                        message = context.getString(R.string.alert_invalid_credentials);
                    } else if (jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("2")) {
                        message = context.getString(R.string.alert_not_registered);
                    } else {
                        message = jsonObject.optString(wsConstants.PARAMS_MESSAGE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    /**
     * Generates RequestBody for making api call for okhttp
     *
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest() {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_ALL_CONTACTS);
        builder.append("&userid" + "=" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
        return builder.toString();
    }
}
