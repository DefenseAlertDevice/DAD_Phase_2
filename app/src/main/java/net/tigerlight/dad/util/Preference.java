package net.tigerlight.dad.util;

import android.content.Context;
import android.content.SharedPreferences;
import net.tigerlight.dad.DADApplication;
import net.tigerlight.dad.R;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Created by M.T on 6 Oct, 2016.
 * <p>
 * Purpose of this class is to save data in preferance and retrive values from preferance througout the lifewcycle of application
 * This class is hold methods for storing and retriving values from preference.
 */
public class Preference {

    private static Preference mPreference;
    public SharedPreferences mEncryptedSharedPreferences;
    public SharedPreferences mSharedPreferences;

    public final String KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN";
    public final String KEY_LANG_ID = "KEY_LANG_ID";
    public final String KEY_EMP_MASTER_ID = "KEY_EMP_MASTER_ID";
    public final String KEY_JS_MASTER_ID = "KEY_JS_MASTER_ID";
//    public final String KEY_JS_CITIZENSHIP = "KEY_JS_CITIZENSHIP";


    //Umesh nepali has been working from here
    public final static String PREFERENCE_IS_LOGIN = "isLogIn";

    private Preference() {
        mSharedPreferences = DADApplication.mAppInstance.getSharedPreferences(DADApplication.mAppInstance.getString(R.string.app_name), Context.MODE_PRIVATE);

        try {
            MasterKey masterKey = new MasterKey.Builder(DADApplication.mAppInstance)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            mEncryptedSharedPreferences = EncryptedSharedPreferences.create(
                    DADApplication.mAppInstance,
                    "secure_prefs", // Name for encrypted prefs file
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception ignored) {}
    }

    /**
     * @return the {@link SharedPreferences} object that will be used to save values in the application preference
     */
    public static Preference getInstance() {
        if (mPreference == null) {
            mPreference = new Preference();
        }
        return mPreference;
    }

    /**
     * Stores the {@link String} value in encrypted preferences
     *
     * @param key   {@link String} key for the value to store
     * @param value {@link String} value to be stored
     */
    public void saveEncryptedPreferenceData(String key, String value) {
        SharedPreferences.Editor editor = mEncryptedSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Retrieves the {@link String} value from encrypted preferences
     *
     * @param key {@link String} key for the value
     * @return stored {@link String} value or null if not found
     */
    public String getEncryptedPreferenceData(String key) {
        return mEncryptedSharedPreferences.getString(key, null);
    }

    /**
     * To delete a particular value from the encrypted preference file
     *
     * @param key
     */
    public void clearEncryptedPreferenceItem(String key) {
        SharedPreferences.Editor editor = mEncryptedSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Stores the {@link String} value in the preference
     *
     * @param key   {@link String} parameter for the key for the values in preference
     * @param value {@link String} parameter for the value to be stored in preference
     */
    public void savePreferenceData(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    /**
     * Stores the {@link Boolean} value in the preference
     *
     * @param key   {@link String} parameter for the key for the values in preference
     * @param value {@link Boolean} parameter for the value to be stored in preference
     */
    public void savePreferenceData(String key, Boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    /**
     * Stores the {@link Integer} value in the preference
     *
     * @param key   {@link String} parameter for the key for the values in preference
     * @param value {@link Integer} parameter for the value to be stored in preference
     */
    public void savePreferenceData(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    public void clearPreferenceData() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * To delete particular value from the preference file
     *
     * @param key
     */
    public void clearPreferenceItem(String key) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }


}
