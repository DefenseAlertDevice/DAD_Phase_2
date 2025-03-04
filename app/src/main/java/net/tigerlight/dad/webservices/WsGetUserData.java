package net.tigerlight.dad.webservices;

import android.content.Context;

import net.tigerlight.dad.R;
import net.tigerlight.dad.registration.model.GetUserInfoModel;
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
public class WsGetUserData {
    private Context context;
    private String message;
    private boolean success;
    private String user_id;
    private GetUserInfoModel getUserInfoModel;
//    private ArrayList<GetUserInfoModel> listProfileModel;


    public WsGetUserData(final Context context) {
        this.context = context;
        getUserInfoModel = new GetUserInfoModel();
    }


    public GetUserInfoModel getGetUserInfoModel() {
        return getUserInfoModel;
    }

    public void setGetUserInfoModel(GetUserInfoModel getUserInfoModel) {
        this.getUserInfoModel = getUserInfoModel;
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
        if (response != null && response.trim().length() > 0) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                final WsConstants wsConstants = new WsConstants();
                if (jsonObject.length() > 0) {
                    success = jsonObject.optString(wsConstants.PARAMS_SUCCESS).equals("1");
                    final JSONObject jsonObject1 = jsonObject.optJSONObject(wsConstants.PARAMS_DATA);
                    final String address = jsonObject1.optString("address");
                    final String email = jsonObject1.optString("email");
                    final String username = jsonObject1.optString("username");
                    final String phonenumber = jsonObject1.optString("phonenumber");
                    final String id = jsonObject1.optString("id");

                    getUserInfoModel.setUser_id(address);
                    getUserInfoModel.setEmail(email);
                    getUserInfoModel.setUsername(username);
                    getUserInfoModel.setPhone_no(phonenumber);
                    getUserInfoModel.setUser_id(id);

                    setGetUserInfoModel(getUserInfoModel);
//                    sectionsModel.setId(id);

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
     * @return {@link String} that will store all the parameters to be passed to the server for execultion.
     */
    private String generateLoginRequest() {
        final Constant mConstants = new Constant();
        String user_id = Preference.getInstance().mSharedPreferences.getString(mConstants.USER_ID, "");

        final WsConstants wsConstants = new WsConstants();
        StringBuilder builder = new StringBuilder();
        builder.append(wsConstants.PARAMS_COMMAND + "=" + WsConstants.METHOD_GET_USER_DATA);
//        builder.append("&userid" + "=" + Preference.getInstance().mSharedPreferences.getString(Constantss.USER_ID, ""));
        builder.append("&userid" + "=" + user_id);
        return builder.toString();
    }
}
