package com.pinasystems.servicebasketv2;

public class AppConfig {

    public static final String ROOT_URL = "http://www.pinasystemsdb.890m.com/sbv2php/";
    //login url
    static final String LOGIN = ROOT_URL + "login.php";
    //register url
    static final String REGISTER = ROOT_URL + "register.php";
    //Set provider or requester only url
    static final String SET_ACCOUNT_TYPE = ROOT_URL + "setacctype.php";
    //Store provider data url
    static final String SET_PROVIDER_DATA = ROOT_URL +"enterprovider.php";
    //Get provider profile status
    static final String PROFILE_STATUS = ROOT_URL + "profilestatus.php";
    //Get incoming requests
    static final String GET_REQUESTS_FOR_PROVIDER = ROOT_URL + "getrequests.php";
    //Get subcategory and category
    static final String CATEGORIES_AND_SUBCATEGORIES = ROOT_URL + "subcategories.php?action=";
    //Save user address
    static final String SAVE_ADDRESS = ROOT_URL + "saveaddress.php";
    //Get user address
    static final String GET_ADDRESS = ROOT_URL + "getaddresses.php?userid=";
    //Get default saved address
    static final String GET_DEFAULT_ADDRESS = ROOT_URL +"getdefaddress.php";
    //Create new request
    static final String CREATE_REQUEST = ROOT_URL + "createrequest.php";
    //Get provider details for requester
    static final String GET_PROVIDER_DETAILS = ROOT_URL +"getPdetails.php";
    //Reset password
    static final String RESET_PASSWORD = ROOT_URL  + "resetpassword.php";
    //Store FCM token
    static final String STORE_FCM_TOKEN = ROOT_URL + "savetoken.php";

    public static final String USER_ID = "id";
    static final String MISSING = "missing";
    static final String USERNAME = "username";
    static final String NAME = "name";
    static final String PASSWORD ="password";
    static final String ACCOUNT = "account";
    static final String CATEGORY = "category";
    static final String SUBCATEGORY ="subcategory";
    static final String STATE = "state";
    static final String CITY = "city";
    static final String PINCODE = "pincode";
    static final String ADDRESS_LABEL = "label";
    public static final String ADDRESS = "address";
    static final String POSTAL_ADDRESS = "postal";

    static final String ACTION = "action";
    static final String PROFILESTATUS = "profile_status";
    static final String JSON_ARRAY = "result";

    //Shared Preferences
    public static final String APP_PREFS_NAME = "servicebasket";
    static final String PREF_DATA = "storedData";
    static final String PREF_TEMP = "tag";
    static final String PREF_LOGIN_STATUS = "login";
    static final String PREF_ADDRESS_LABEL = "label";
    static final String PREF_SUBCATEGORY = "subcategory";
    static final String PREF_FCM_TOKEN = "fcm_token";
    static final String PREF_USERNAME = "username";

    //Address
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";

    static final String DESCRIPTION = "description";
    static final String ESTDATE = "date";
    static final String ESTTIME = "time";
    static final String MAXPRICE = "maxprice";

    static final String DATE = "est_date";
    static final String TIME = "est_time";
    static final String MAX_PRICE = "max_price";
    static final String MIN_PRICE = "min_price";
    static final String COMMENT = "comment";

    //Tags for category image
    static final String IMAGE_URL = "image_url";
    static final String VIDEO_URL = "video_url";

    static final String REQ_ID = "reqid";
    static final String FCM_TOKEN = "fcmtoken";

}
