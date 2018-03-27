package vvdn.in.ble_ota;

/**
 * Interface Name : AppHelper
 * Description : This class contains all the string constant used as parameter in the
 * application
 *
 * @author Durgesh-Shankar
 */
public interface AppHelper {

    String DEVICE_PACKET_DATA_SERVICE_WRITE_UUID = "F000FFC0-0451-4000-B000-000000000000";
    String STATUS = "status";

    /*LATEST ROAM BEE DETAILS*/
    /*OTA*/
    String serviceUUIDDFU = "8e400001-f315-4f60-9fb8-838830daea50";
    String WitreCharacteristicsClearDFU = "8e400001-f315-4f60-9fb8-838830daea50";
    String serviceUUIDOTA = "0000fe59-0000-1000-8000-00805f9b34fb";
    String WriteCharacteristicsROAMBEE = "8ec90002-f315-4f60-9fb8-838830daea50";

    String WriteCharacteristics_META_DATA = "8ec90001-f315-4f60-9fb8-838830daea50";
    String WriteCharacteristics_FILE_DATA = "8ec90002-f315-4f60-9fb8-838830daea50";
    String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    /*DATA LOGGING UUID*/
    String service_DATA_LOGGING_UUID = "00001110-0000-1000-8000-00805f9b34fb";
    String Notification_DATA_LOGGING_UUID = "0000bbbb-0000-1000-8000-00805f9b34fb";
    String DATA_LOGGING_WRITE_CHARACTERISTICS_UUID = "00001111-0000-1000-8000-00805f9b34fb";

    String BLE_SERVICE_DISCOVERED_SUCCESS = "BLE_SERVICE_DISCOVERED_SUCCESS";
    String BLE_SERVICE_DISCOVERED_FAILURE = "BLE_SERVICE_DISCOVERED_FAILURE";

    /**
     * APP Helper Constant
     */
    String HYPHEN = "-";
    String BLANK_SPACE = " ";
    String OPENING_BRACKET = "(";
    String CLOSING_BRACKET = ")";
    String DOUBLE_COLON = ":";
    String DOT = ".";
    String NUMBER_ZERO = "0";
    String NUMBER_ONE = "1";
    String DOUBLE_ZERO = "00";
    String ZERO_ONE = "01";
    String ZERO_TWO = "02";
    String ZERO_THREE = "03";
    String ZERO_FOUR = "04";
    String ZERO_FIVE = "05";

    /**
     * String Constant depicting beacon type
     */
    String BEACON_B1 = "B1";
    String BEACON_B4 = "B4";
    String BEACON_B5 = "B5";
    String DFU_TAG = "DFU";

    /**
     * String constant for user note saving feature
     */
    String STRING_INITIATE_USER_DATA_TRANSFER = "string_initiate_user_data_transfer";
    String STRING_SEND_USER_DATA_TRANSFER = "string_send_user_data_transfer";
}
