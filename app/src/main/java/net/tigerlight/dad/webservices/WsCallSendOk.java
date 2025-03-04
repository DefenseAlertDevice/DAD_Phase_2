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
 * Created by M.T. on 7 Oct, 2016.
 * This class is making api call for user login
 */
public class WsCallSendOk {
    private Context context;
    private String message;
    private boolean success;

    public WsCallSendOk(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Calls the api user SendOkAlert.
     *
     * @param pin
     * @param longitude
     * @param latitude
     * @return
     */
    public JSONObject executeService(final String pin, final Double latitude, final Double longitude) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String updateResponse = new WSUtil().callServiceHttpGet(context, url + generateUpdateRequest(latitude, longitude));
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(pin, latitude, longitude));
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

                    if (!jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("1")) {
                        if (jsonObject.optString("message").equalsIgnoreCase("pincode")) {
                            message = context.getString(R.string.alert_invalid_pin);
                        } else {
                            message = jsonObject.optString(wsConstants.PARAMS_MESSAGE);
                        }
                    }
                    return jsonObject;
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
     * @param pin {@link String} value of existing pin of user
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(final String pin, final Double latitude, final Double longitude) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_SEND_OK);
//        In previous version C.USER_ID had put in Interface,Here as off now it is in String Resource.
//        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + Preference.getInstance().mSharedPreferences.getString(C.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_PIN + "=" + pin);
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + latitude);
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + longitude);
        builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
//        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(Constant.IS_LANG_ID, ""));

//        Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "sv");
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
        builder.append("&userid" + "=" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + latitude);
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + longitude);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(Constant.IS_LANG_ID, ""));


        return builder.toString();
    }

}
