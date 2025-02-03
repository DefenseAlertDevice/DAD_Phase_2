package net.tigerlight.dad;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;

import java.util.Locale;

public class DADApplication extends Application  {

    public static final boolean IS_APP_OPEN =true ;
    public static DADApplication mAppInstance;
    public SharedPreferences mSharedPreferences;



    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d(TAG, "Creat");


//        Fabric.with(this, new Crashlytics());
        mAppInstance = this;
        mSharedPreferences = getSharedPreferences(this.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
//        configLanguage();
        Log.d("d_lang",Locale.getDefault().getLanguage() );
        String lang=Locale.getDefault().getLanguage();

        if(lang.equalsIgnoreCase("en"))
        {
            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "en");
            Constant.IS_LANGUAGE = false;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng));
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, true);
        }else if(lang.equalsIgnoreCase("da"))
        {
            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "da");
            Constant.IS_LANGUAGE = true;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_da));
        } else if (lang.equalsIgnoreCase("nb")) {

            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "nb");

            Constant.IS_LANGUAGE = true;

            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_nb));
        }
        else
        {
            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "en");
            Constant.IS_LANGUAGE = false;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng));
            Preference.getInstance().savePreferenceData(Constant.IS_ENG, true);
        }





    }


//    public void configLanguage() {
//        final String currentLanguageCode = Preference.getInstance().mSharedPreferences.getString(getResources().getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng));
//
//        if (currentLanguageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_eng))) {
//            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "en");
//            Locale locale = new Locale("en");
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//            //this is set english language
//            Constant.IS_LANGUAGE = false;
//            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng));
//            Preference.getInstance().savePreferenceData(Constant.IS_ENG, true);
//
//            Log.d("Lang", "english");
//
//
//        } else if (currentLanguageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_da))) {
//            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "da");
//            Locale locale = new Locale("da");
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//            //this is set arabic language
//            Constant.IS_LANGUAGE = true;
//            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_da));
//            Log.d("Lang", "danish");
//
//
//        } else if (currentLanguageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_nb))) {
//            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "nb");
//            Locale locale = new Locale("nb");
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//            //this is set arabic language
//            Constant.IS_LANGUAGE = true;
//
//            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_nb));
//            Log.d("Lang", "norwegian");
//
//
//        } else if (currentLanguageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_sv))) {
//            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "sv");
//            Locale locale = new Locale("sv");
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//            //this is set arabic language
//            Constant.IS_LANGUAGE = true;
//
//            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_sv));
//            Log.d("Lang", "swedish");
//
//
//        } else {
//            Preference.getInstance().savePreferenceData(Constant.IS_LANG_ID, "en");
//            Locale locale = new Locale("en");
//            Locale.setDefault(locale);
//            Configuration config = new Configuration();
//            config.locale = locale;
//            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//            //this is set english language
//            Constant.IS_LANGUAGE = false;
//            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng));
//            Preference.getInstance().savePreferenceData(Constant.IS_ENG, true);
//
//            Log.d("Lang", "english");
//
//        }
//
//
//    }

    public void configLanguage(Context context, String languageCode) {


        if (languageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_eng))) {
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            //this is set english language
            Constant.IS_LANGUAGE = false;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng));
//            this.getSharedPreferences().edit().putString(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_eng)).commit();
            Log.d("Lang", "english");


        } else if (languageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_da))) {
            Locale locale = new Locale("da");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            //this is set arabic language
            Constant.IS_LANGUAGE = true;

//            Const.ISLANGUAGE = true;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_da));
//            this.getSharedPreferences().edit().putString(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_ar)).commit();
            Log.d("Lang", "danish");


        } else if (languageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_nb))) {
            Locale locale = new Locale("nb");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            //this is set arabic language
            Constant.IS_LANGUAGE = true;
//            Const.ISLANGUAGE = true;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_nb));
//            this.getSharedPreferences().edit().putString(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_ar)).commit();
            Log.d("Lang", "norwegian");

        } else if (languageCode.equalsIgnoreCase(getBaseContext().getString(R.string.pref_key_language_sv))) {
            Locale locale = new Locale("sv");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            //this is set arabic language
            Constant.IS_LANGUAGE = true;
//            Const.ISLANGUAGE = true;
            Preference.getInstance().savePreferenceData(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_sv));
//            this.getSharedPreferences().edit().putString(getString(R.string.pref_key_language_code), getString(R.string.pref_key_language_ar)).commit();
            Log.d("Lang", "swedish");


        }


    }



}
