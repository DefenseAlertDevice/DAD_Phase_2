package net.tigerlight.dad.registration.webservices;

import android.content.Context;
import android.util.Log;

import net.tigerlight.dad.registration.util.Constant;
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
public class WsCallRegistrer {
    private Context context;
    private String message;
    private boolean success;
    private String userid;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    public WsCallRegistrer(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUserid() {
        return userid;
    }

    public String getAccessToken() { return accessToken; }

    public String getRefreshToken() { return refreshToken; }

    public long getExpiresIn() { return expiresIn; }

    /**
     * Calls the api user Login.
     *
     * @param email
     * @param password
     * @param longitude
     * @param latitude
     * @return
     */
    public JSONObject executeService(final String email, final String password, final String longitude, final String latitude, final String name, final String phone) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(email, password, longitude, latitude, name, phone));
        return parseResponse(response);
    }

    /**
     * Parse the json response from {@link String} to {@link JSONArray}.
     *
     * @param response {@link String} response that is recived from the api request.
     * @return {@link JSONArray} for success or failure response of request
     */
    private JSONObject parseResponse(final String response) {

        Log.d("Register Response", response);
        if (response != null && response.trim().length() > 0) {
            try {
                final Preference preference = Preference.getInstance();
                final JSONObject jsonObject = new JSONObject(response);
                final WsConstants wsConstants = new WsConstants();
                if (jsonObject.length() > 0) {
                    userid = jsonObject.optString(wsConstants.PARAMS_ID);
                    accessToken = jsonObject.optString(Constant.ACCESS_TOKEN);
                    refreshToken = jsonObject.optString(Constant.REFRESH_TOKEN);
                    expiresIn = jsonObject.optLong(Constant.EXPIRES_IN);
                    preference.savePreferenceData(Constant.USER_ID, userid);
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
     * @param email {@link String} value for email address for the employer
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(final String email, final String password, final String longitude, final String latitude, final String name, final String phone) {
        final WsConstants wsConstants = new WsConstants();
        //From here umesh nepali this is not working please check it out
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_REGISTER);
        builder.append("&" + wsConstants.PARAMS_EMAIL + "=" + URLEncoder.encode(email));
        builder.append("&" + wsConstants.PARAMS_PASSWORD + "=" + password);
        builder.append("&" + wsConstants.PARAMS_NAME + "=" + name);
        builder.append("&" + wsConstants.PARAMS_USER_NAME + "=" + name);
        builder.append("&" + wsConstants.PARAMS_PHONE + "=" + phone);
        builder.append("&" + wsConstants.PARAMS_LONGITUDE + "=" + longitude);
        builder.append("&" + wsConstants.PARAMS_LATITUDE + "=" + latitude);
        builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        return builder.toString();
    }
}
