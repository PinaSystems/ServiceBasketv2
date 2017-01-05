package com.pinasystems.servicebasketv2;

/**
 * Created by admin on 10/6/2016.
 */
public class AppConfig {

    public static final String ROOT_URL = "http://www.pinasystemsdb.890m.com/sbv2php/";
    //login url
    public static final String LOGIN = ROOT_URL + "login.php";
    //register url
    public static final String REGISTER = ROOT_URL + "register.php";
    //Set provider or requester only url
    public static final String SET_ACCOUNT_TYPE = ROOT_URL + "setacctype.php";
    //Store provider data url
    public static final String SET_PROVIDER_DATA = ROOT_URL +"enterprovider.php";
    //Get provider profile status
    public static final String PROFILE_STATUS = ROOT_URL + "profilestatus.php";
    //
    public static final String GET_REQUESTS_FOR_PROVIDER = ROOT_URL + "getrequests.php";

    public static final String CATEGORIES_AND_SUBCATEGORIES = ROOT_URL + "subcategories.php?action=";

    public static final String GET_CATEGORIES = ROOT_URL + "categories.php";

    public static final String SAVE_ADDRESS = ROOT_URL + "saveaddress.php";

    public static final String GET_ADDRESS = ROOT_URL + "getaddresses.php?userid=";

    public static final String GET_DEFAULT_ADDRESS = ROOT_URL +"getdefaddress.php";

    public static final String CREATE_REQUEST = ROOT_URL + "createrequest.php";

    public static final String GET_PROVIDER_DETAILS = ROOT_URL +"getPdetails.php";

    public static final String RESET_PASSWORD = ROOT_URL  + "resetpassword.php";

    public static final String STORE_FCM_TOKEN = ROOT_URL + "savetoken.php";

    public static final String USER_ID = "id";
    public static final String MISSING = "missing";
    public static final String USERNAME = "username";
    public static final String NAME = "name";
    public static final String PASSWORD ="password";
    public static final String ACCOUNT = "account";
    public static final String CATEGORY = "category";
    public static final String SUBCATEGORY ="subcategory";
    public static final String STATE = "state";
    public static final String CITY = "city";
    public static final String PINCODE = "pincode";
    public static final String ADDRESS_LABEL = "label";
    public static final String ADDRESS = "address";
    public static final String POSTAL_ADDRESS = "postal";

    public static final String ACTION = "action";
    public static final String PROFILESTATUS = "profile_status";
    public static final String JSON_ARRAY = "result";

    //Shared Preferences
    public static final String APP_PREFS_NAME = "servicebasket";
    public static final String PREF_DATA = "storedData";
    public static final String PREF_TEMP = "tag";
    public static final String PREF_LOGIN_STATUS = "login";
    public static final String PREF_ADDRESS_LABEL = "label";
    public static final String PREF_SUBCATEGORY = "subcategory";
    public static final String PREF_FCM_TOKEN = "fcm_token";

    //Address
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static final String DESCRIPTION = "description";
    public static final String ESTDATE = "date";
    public static final String ESTTIME = "time";
    public static final String MAXPRICE = "maxprice";

    public static final String DATE = "est_date";
    public static final String TIME = "est_time";
    public static final String MAX_PRICE = "max_price";
    public static final String MIN_PRICE = "min_price";
    public static final String COMMENT = "comment";

    //Tags for category image
    public static final String IMAGE_URL = "image_url";
    public static final String VIDEO_URL = "video_url";

    public static final String REQ_ID = "reqid";
    public static final String FCM_TOKEN = "fcmtoken";

}
