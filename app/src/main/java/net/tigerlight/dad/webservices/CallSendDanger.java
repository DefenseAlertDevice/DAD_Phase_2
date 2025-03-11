package net.tigerlight.dad.webservices;

import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WSUtil;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

public class CallSendDanger {

    private Context context;
    private String message;
    private boolean success;

    public CallSendDanger(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Calls the api user Login.
     */
    public JSONObject executeService(final Double latitude, final Double longitude, String timezoneID, int accuracy) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String updateResponse = new WSUtil().callServiceHttpGet(context, url + generateUpdateRequest(latitude, longitude));
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(latitude, longitude, timezoneID, accuracy, "", ""));
        return parseResponse(response);
    }

    /**
     * Calls the api user Login.
     */
    public JSONObject executeService(final Double latitude, final Double longitude, String timezoneID, int accuracy, String uuid, String identifier) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String updateResponse = new WSUtil().callServiceHttpGet(context, url + generateUpdateRequest(latitude, longitude));
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(latitude, longitude, timezoneID, accuracy, uuid, identifier));
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
     */
    private String generateLoginRequest(final Double latitude, final Double longitude, String timezoneID, int accuracy, String uuid, String identifier) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_SEND_DANGER);
        builder.append("&userid" + "=" + Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + latitude);
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + longitude);

        //builder.append("&" + wsConstants.TAG_TIMEZONE + "=" + latitude);
//        list.add(new BasicNameValuePair(TAG_TIMEZONE, timezoneID));
        //builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(DadConstant.IS_LANG_ID, ""));
        builder.append("&" + wsConstants.PARAMS_ACCURACY + "=" + accuracy);
        builder.append("&uuid=" + uuid);
        builder.append("&identifier=" + identifier);
//        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "EN"));
        return builder.toString();
    }

    /**
     * Generates RequestBody for making api call for okhttp
     */
    private String generateUpdateRequest(final Double latitude, final Double longitude) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_UPDATE_LOCATION);
        builder.append("&userid" + "=" + Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + latitude);
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + longitude);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(DadConstant.IS_LANG_ID, ""));

        return builder.toString();
    }

}
