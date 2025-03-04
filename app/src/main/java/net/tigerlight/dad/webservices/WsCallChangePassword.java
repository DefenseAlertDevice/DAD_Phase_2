package net.tigerlight.dad.webservices;

import android.content.Context;

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
public class WsCallChangePassword {
    private Context context;
    private String message;
    private boolean success;

    public WsCallChangePassword(final Context context) {
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
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public JSONObject executeService(final String oldPassword, final String newPassword) {
        final String url;
        url = WsConstants.MAIN_URL;
        final String response = new WSUtil().callServiceHttpGet(context, url + generateLoginRequest(oldPassword, newPassword));
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
     * @param oldPassword  {@link String} value for oldPassword
     * @param newwPassword {@link String} value for newPassword
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest(final String oldPassword, final String newwPassword) {
        final WsConstants wsConstants = new WsConstants();
//        final Constant mConstants = new Constant();
        final Preference preference = Preference.getInstance();

        final Constant mConstants = new Constant();
        String user_id = Preference.getInstance().mSharedPreferences.getString(mConstants.USER_ID, "");


//        http://52.33.140.142:8080/TigerServlet?command=UpdatePassword&userid=2070&oldpassword=Indianic123&newpassword=Indianic123&device_token=123&tag=Android
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_UPDATE_PASSWORD);
        builder.append("&" + wsConstants.PARAMS_USER_ID + "=" + user_id);
        builder.append("&" + wsConstants.PARAMS_OLD_PASSWORD + "=" + oldPassword);
        builder.append("&" + wsConstants.PARAMS_NEW_PASSWORD + "=" + newwPassword);
        builder.append("&" + wsConstants.PARAMS_DEVICE_TOKEN + "=" + newwPassword);

        builder.append("&" + wsConstants.PARAMS_TAG + "=" + wsConstants.PARAMS_TAG_VALUE);
//        builder.append("&" + wsConstants.PARAMS_LANGUAGE + "=" + preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        return builder.toString();
    }

}
