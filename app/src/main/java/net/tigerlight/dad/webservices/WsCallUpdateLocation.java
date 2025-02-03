package net.tigerlight.dad.webservices;

import android.content.Context;

import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WSUtil;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created on 22/11/16.
 */

public class WsCallUpdateLocation {
    private Context context;
    private String message;
    private boolean success;


    public WsCallUpdateLocation(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Calls the api user updates.
     *
     * @param longitude
     * @param latitude
     */
    public JSONObject executeService(double latitude, double longitude) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(latitude, longitude));
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
                    message = jsonObject.optString(wsConstants.PARAMS_MESSAGE);

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
     * @param longitude
     * @param latitude
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(double latitude, double longitude) {

        final Preference preference = Preference.getInstance();
        final WsConstants wsConstants = new WsConstants();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_UPDATE_LOCATION);
        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + latitude);
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + longitude);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(Constant.IS_LANG_ID, ""));

        return builder.toString();
    }
}
