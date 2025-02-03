package net.tigerlight.dad.webservices;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;
import net.tigerlight.dad.util.WSUtil;
import net.tigerlight.dad.util.WsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by M.T. on 7 Oct, 2016.
 * This class is making api call for user login
 */
public class WsUploadImage {
    private Context context;
    private String message;
    private boolean success;

    public WsUploadImage(final Context context) {
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
     * @param profileImageId
     * @return
     */
    public JSONObject executeService(final String profileImageId) {
        final String url;
        url = WsConstants.IMAGE_MAIN_URL;
        //url = WsConstants.IMAGE_TEST_URL;
        final String response = new WSUtil().callServiceHttpPost(context, url, generateLoginRequest(profileImageId));
        return parseResponse(response);
    }

    /**
     * Parse the json response from {@link String} to {@link JSONArray}.
     *
     * @param response {@link String} response that is recived from the api request.
     * @return {@link JSONArray} for success or failure response of request
     */
    private JSONObject parseResponse(final String response) {

        Log.d("Upload Response", response);
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
     * @param profileImageId {@link String} value for email address for the employer
     * @return {@link RequestBody} that will store all the parameters to be passed to the server for execultion.
     */
    private RequestBody generateLoginRequest(final String profileImageId) {
        final WsConstants wsConstants = new WsConstants();
        final Preference preference = Preference.getInstance();
        final MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
        multipartBuilder.setType(MultipartBody.FORM);
        multipartBuilder.addFormDataPart(wsConstants.PARAMS_USER_ID, preference.mSharedPreferences.getString(Constant.USER_ID, ""));
//        multipartBuilder.addFormDataPart(wsConstants.PARAMS_USER_ID, "6047");
        if (!TextUtils.isEmpty(profileImageId)) {
            final File fileAvatarImage = new File(profileImageId);

            // multipartBuilder.addFormDataPart(wsConstants.PARAMS_FILE_NAME, preference.mSharedPreferences.getString(Constant.USER_ID, "") + ".png", RequestBody.create(MediaType.parse("image/*"), fileAvatarImage));

            if (!TextUtils.isEmpty(profileImageId) && fileAvatarImage.exists()) {
//                multipartBuilder.addFormDataPart(wsConstants.PARAMS_FILE_NAME, "user_image_" + preference.mSharedPreferences.getString(C.USER_ID, "") + ".png", RequestBody.create(MediaType.parse("image/*"), fileAvatarImage));
                multipartBuilder.addFormDataPart(wsConstants.PARAMS_FILE_NAME, preference.mSharedPreferences.getString(Constant.USER_ID, "") + ".png", RequestBody.create(MediaType.parse("image/*"), fileAvatarImage));
            }
        }
//        multipartBuilder.addFormDataPart(wsConstants.PARAMS_DEVICE_TOKEN, preference.mSharedPreferences.getString(preference.KEY_DEVICE_TOKEN, ""));
        multipartBuilder.addFormDataPart(wsConstants.PARAMS_TAG, wsConstants.PARAMS_TAG_VALUE);
//        multipartBuilder.addFormDataPart(wsConstants.PARAMS_LONGITUDE, preference.mSharedPreferences.getString(Constant.COMMON_LONGITUDE,""));
//        multipartBuilder.addFormDataPart(wsConstants.PARAMS_LATITUDE, preference.mSharedPreferences.getString(Constant.COMMON_LATITUDE,""));
        multipartBuilder.addFormDataPart(wsConstants.PARAMS_TAG, wsConstants.PARAMS_TAG_VALUE);
        multipartBuilder.addFormDataPart(wsConstants.PARAMS_LANGUAGE, preference.mSharedPreferences.getString(preference.KEY_LANG_ID, "en"));
        return multipartBuilder.build();
    }
}
