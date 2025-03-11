package net.tigerlight.dad.webservices;

import net.tigerlight.dad.registration.util.DadConstant;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WSUtil;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

/**
 * Created by M.T. on 7 Oct, 2016.
 * This class is making api call for user login
 */
public class CallDADTest {
    private Context context;
    private String message;
    private boolean success;

    public CallDADTest(final Context context) {
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
     *
     * @return
     */
    public JSONObject executeService() {
        final String url;
        url = WsConstants.MAIN_URL;
        final String updateResponse = new WSUtil().callServiceHttpGet(context, url + generateUpdateRequest());
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest("", ""));
        return parseResponse(response);
    }

    public JSONObject executeService(String uuid, String identifier) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String updateResponse = new WSUtil().callServiceHttpGet(context, url + generateUpdateRequest());
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(uuid, identifier));
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
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(String uuid, String identifier) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_DAD_TEST);
        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(DadConstant.IS_LANG_ID, ""));
        builder.append("&" + wsConstants.PARAMS_ACCURACY + "=" + preference.mSharedPreferences.getInt(DadConstant.COMMON_ACCURACY, 0));
        builder.append("&uuid=" + uuid);
        builder.append("&identifier=" + identifier);
//        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));

        return builder.toString();
    }

    /**
     * Generates RequestBody for making api call for okhttp
     */
    private String generateUpdateRequest() {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_UPDATE_LOCATION);
        builder.append("&userid" + "=" + Preference.getInstance().mSharedPreferences.getString(DadConstant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + Preference.getInstance().mSharedPreferences.getString(DadConstant.COMMON_LATITUDE, ""));
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + Preference.getInstance().mSharedPreferences.getString(DadConstant.COMMON_LONGITUDE, ""));
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(DadConstant.IS_LANG_ID, ""));
        return builder.toString();
    }

}
