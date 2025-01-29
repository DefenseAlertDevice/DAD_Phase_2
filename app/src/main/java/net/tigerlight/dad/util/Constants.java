package net.tigerlight.dad.util;

public interface Constants {

    int MAX_CLICK_INTERVAL = 500;
    String USER_ID = "userID";
    int MINIMUM_ACCEPTABLE_ACCURACY = 20; //PROD
    //int MINIMUM_ACCEPTABLE_ACCURACY = 16; //Test
    String NEW_UUID = "FD8C0AA6D40411E5AB30625662870761";
    String OLD_UUID = "E2C56DB5DFFB48D2B060D0F5A71096E0";
    String LAIRD_BEACON_LABEL = "Laird iBeacon";

    interface Extras {
        String FORCE_LOGOUT = "force_logout";
        String SMALLEST_DISPLACEMENT_VALUE = "smallest_displacement_value";
        String STOP_SELF = "stop_self";
    }

    interface REQUEST_CODES {
        int FORCE_LOGOUT = 1;
    }

    interface Preferences
    {
        interface Keys
        {
            String UUID_KEY = "uuid";
            String IDENTIFIER_KEY = "identifier";
            String MAJOR_KEY = "major";
            String MINOR_KEY = "minor";

            String NEW_UUID_KEY = "new_uuid";
            String NEW_MAJOR_KEY = "new_major";
            String NEW_MINOR_KEY = "new_minor";

            String OLD_UUID_KEY = "old_uuid";
            String OLD_MAJOR_KEY = "old_major";
            String OLD_MINOR_KEY = "old_minor";

        }
    }

    interface Actions
    {
        String SENT_ALERT_ACTION = "com.tigerlight.action.alert_sent";
    }

    interface JsonKeys
    {
        String NOTIFICATION_SOUND = "sound";
    }
}
