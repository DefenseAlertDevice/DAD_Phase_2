package net.tigerlight.dad.webservices;

import android.content.Context;

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
public class CallAddreceiver {
    private Context context;
    private String message;
    private String userId;
    private boolean success;

    public CallAddreceiver(final Context context) {
        this.context = context;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    /**
     * Calls the api user Login.
     *
     * @param first_name
     * @param last_name
     * @param nickName
     * @param email
     * @return
     */
    public JSONObject executeService(final String first_name, final String last_name, final String nickName, final String email, final String phone, final String address) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(first_name, last_name, nickName, email, phone, address));
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
                    userId = jsonObject.optString(wsConstants.PARAMS_ID);

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
    private String generateLoginRequest(final String first_name, final String last_name, final String nick_name, final String email, final String phone, final String address) {
        final WsConstants wsConstants = new WsConstants();
        final DadConstant mConstants = new DadConstant();
        final Preference preference = Preference.getInstance();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_ADD_CONTACT);
        builder.append("&" + wsConstants.PARAMS_FIRST_NAME + "=" + first_name);
        builder.append("&" + wsConstants.PARAMS_LAST_NAME + "=" + last_name);
        builder.append("&" + wsConstants.PARAMS_NICK_NAME + "=" + nick_name);
        builder.append("&" + wsConstants.PARAMS_EMAIL + "=" + URLEncoder.encode(email));
        builder.append("&" + wsConstants.PARAMS_PHONE + "=" + phone);
        builder.append("&" + wsConstants.PARAMS_ADDRESS + "=" + address);
        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + preference.mSharedPreferences.getString(mConstants.USER_ID, ""));
        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
        builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        return builder.toString();
    }
}
