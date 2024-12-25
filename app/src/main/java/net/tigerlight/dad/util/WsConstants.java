package net.tigerlight.dad.util;

public class WsConstants {

    public WsConstants() {
    }

    public final static int CONNECTION_TIMEOUT = 30;
    public final static int SUCCESS_1 = 1;
    public static final String DOMAIN = "https://dev.api.defensealertdevice.com/";
    public static final String ASSETS_DOMAIN = "https://dev.assets.defensealertdevice.com/";
    //Prod
    //public static final String DOMAIN = "https://devbean.defensealertdevice.com/"; //Test
    //public static final String DOMAIN = "http://develop.defensealertdevice.com:8080/";
    /* Live URL	*/
    //    public static final String MAIN_URL = "http://defensealertdevice.com/admin/";
    /* Staging URL	*/

    //This is development url  http://develop.defensealertdevice.com  -52.38.75.231
//    public static final String MAIN_URL = "https://services.defensealertdevice.com:8080/TigerServlet?";

    //this is live url    http://services.defensealertdevice.com/  -2.33.140.142
    //public static final String MAIN_URL = "http://52.33.140.142:8080/TigerServlet?";
    //public static final String MAIN_URL = " http://develop.defensealertdevice.com/";
    public static final String MAIN_URL = DOMAIN + "TigerServlet?";

    //public static final String IMAGE_MAIN_URL = "http://52.33.140.142:8080/TigerServlet/FileTransfer?";
    public static final String IMAGE_MAIN_URL = DOMAIN + "FileTransfer1";
    public static final String IMAGE_MAIN_URL_AMAZONE = "https://tigerlight.images.s3-website-us-west-2.amazonaws.com/user_image_";


    public static final String IMAGE_TEST_URL = "http://domain:8080/TigerServlet/FileTransfer?";


    /* Method Names */
    public final static String METHOD_LOGIN = "Login";
    public final static String METHOD_FORGOT_PASSWORD = "ForgotPassword";
    public final static String METHOD_REGISTER = "Register";
    public final static String METHOD_UPDATE_ACCOUNT = "UpdateAccount";
    public final static String METHOD_UPDATE_PASSWORD = "UpdatePassword";
    public final static String METHOD_CREATE_PIN = "CreatePin";
    public final static String METHOD_RESET_COUNT = "ResetBadgeNumber";
    public final static String METHOD_UPDATE_PIN = "UpdatePin";
    public final static String METHOD_FORGOT_PIN = "ForgotPin";
    public final static String METHOD_LOGOUT = "Logout";
    public final static String METHOD_GET_USER_DATA = "UserData";
    public final static String METHOD_CROWD_ALERT = "UpdateCrowdAlert";
    public final static String METHOD_SEND_OK = "SendOK";
    public final static String METHOD_ADD_CONTACT = "NewContact";

    public final static String METHOD_ALL_CONTACTS = "AllContacts";
    public final static String METHOD_DELETE_CONTACT = "DeleteContact";
    public final static String METHOD_UPDATE_CONTACT = "UpdateContact";
    public final static String METHOD_SEND_DANGER = "SendDanger";
    public final static String METHOD_GET_ALERT_COUNT = "GetAlerts";
    public final static String METHOD_DELETE_ALERT = "DeleteAlerts";
    public final static String METHOD_DAD_TEST = "DADTest";
    public final static String METHOD_UPDATE_LOCATION = "UpdateLocation";


    /* Common Params */
    public final String PARAMS_SUCCESS = "success";
    public final String PARAMS_MESSAGE = "message";
    public final String PARAMS_DATA = "data";
    public final String PARAMS_SETTINGS = "settings";
    public final String PARAMS_COMMAND = "command";
    public final String PARAMS_DEVICE_TOKEN = "device_token";
    public final String PARAMS_TAG = "tag";
    public final String PARAMS_TAG_VALUE = "Android";

    /* Login Params */
    public final String PARAMS_ID = "id";
    public final String PARAMS_IDINT = "idInt";
    public final String PARAMS_IDSTR = "idStr";
    public final String PARAMS_RESFRESH_LOCATION = "resfresh_Location";

    public final String PARAMS_EMAIL = "email";
    public final String PARAMS_PASSWORD = "password";
    public final String PARAMS_LONGITUDE = "longitude";
    public final String PARAMS_LATITUDE = "latitude";
    public final String PARAMS_LANGUAGE = "language";
    public final String PARAMS_HOURS = "hours";
    public final String PARAMS_ACCURACY = "accuracy";

    public final String PARAMS_ = "";

    public final String PARAMS_NAME = "name";
    public final String PARAMS_PHONE = "phone";
    public final String PARAMS_USER_NAME = "username";
    public final String PARAMS_USER_ID = "userid";

    public final String PARAMS_OLD_PASSWORD = "oldpassword";
    public final String PARAMS_NEW_PASSWORD = "newpassword";

    public final String PARAMS_PIN = "pincode";
    public final String PARAMS_NEW_PIN = "newpin";

    public final String PARAMS_CROWD_STATUS = "crowdStatus";
    public final String PARAMS_FILE_NAME = "filename";

    public final String PARAMS_FIRST_NAME = "firstname";
    public final String PARAMS_LAST_NAME = "lastname";
    public final String PARAMS_NICK_NAME = "nickname";
    public final String PARAMS_ADDRESS = "address";

    public final String PARAMS_CONTACT_USER_ID = "contactUserid";
    public static final String TAG_TIMEZONE = "timezone";

    public final String PARAMS_ALERT_ID = "alertid";

}