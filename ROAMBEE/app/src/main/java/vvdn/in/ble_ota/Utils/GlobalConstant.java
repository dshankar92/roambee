package vvdn.in.ble_ota.Utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;

import no.nordicsemi.android.log.LogSession;
import vvdn.in.ble_ota.listener.BluetoothConnectionStateInterface;
import vvdn.in.ble_ota.listener.DataLoggingListener;

/**
 * Class Name : GlobalConstant
 * Description : This method is used as the constant of this application
 */
public class GlobalConstant {
    /**
     * BLE_HCI_STATUS_CODE_SUCCESS	Everything ok.
     */
    public static final int STATUS_CODE_0 = 0;
    /**
     * GATT_ERROR
     * Can be anything, from device not in Range to a random error.
     */
    public static final int STATUS_CODE_133 = 133;
    /**
     * BLE_HCI_CONNECTION_TIMEOUT
     * Could not establish a connection in specified period.
     * Maybe device is currently connected to something else
     */
    public static final int STATUS_CODE_8 = 8;
    /**
     * GATT_AUTH_FAIL
     */
    public static final int STATUS_CODE_59 = 59;
    /**
     * BLE_HCI_STATUS_CODE_UNKNOWN_CONNECTION_IDENTIFIER
     */
    public static final int STATUS_CODE_2 = 2;
    /**
     * BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION
     * Remote device has forced a disconnect.
     */
    public static final int STATUS_CODE_19 = 19;
    public static final String KEY_UPDATE_UI = "key_update_ui";
    public static final String KEY_DATA_TO_UI = "key_data_to_ui";
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_BLUETOOTH_DEVICE = "key_bluetooth_device";
    public static final String KEY_RSSI_STRENGTH = "key_rssi_strength";
    public static final String KEY_MANUFACTURE_DATA = "key_manufacture_data";

    public static String DEVICE_MAC = "";
    public static String OLD_DEVICE_MAC = "";
    public static BluetoothAdapter mBluetoothAdapter;
    public static DataLoggingListener mDataLoggingListener;
    public static BluetoothConnectionStateInterface mBluetoothConnectionStateInterface;
    public static boolean CONNECTED_STATE = false;
    public static String DEVICE_NAME = "";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_WRITE_SUCCESS = "com.example.bluetooth.le.ACTION_WRITE_SUCCESS";
    public final static String ACTION_WRITE_DENIED = "com.example.bluetooth.le.ACTION_WRITE_DENIED";
    public final static String ACTION_WRITE_FAILED = "com.example.bluetooth.le.ACTION_WRITE_FAILED";
    public final static String ACTION_READ_DENIED = "com.example.bluetooth.le.ACTION_READ_DENIED";
    public final static String ACTION_READ_FAILED = "com.example.bluetooth.le.ACTION_READ_FAILED";
    public final static String ACTION_READING_DATA = "com.example.bluetooth.le.ACTION_READING_DATA";
    public final static String ACTION_FOUND_DATA = "com.example.bluetooth.le.ACTION_FOUND";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public static BluetoothGattCharacteristic Notification_Characteristics_UUID;
    public static boolean boolIsDataLoggingActivityVisible = false;

    /*STORED FIRST INDEX ON DEVICE*/

    public static int STORED_FIRST_INDEX = 0x05;
    public static String DEVICE_CONNECTING_NAME = "";
    public static int TOTAL_STORED_RECORDS_ON_DEVICE = 0;
    public static int INT_CURRENT_GROUP_INDEX = 0;
    public static boolean BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
    public static long longGroupIndexTimeStamp = 0L;
    public static int ONE_SECOND_DELAY_DURATION = 1000;
    public static String RECORD_NEED_TO_BE_RECEIVE = "0";
    public static ArrayList<Activity> mGlobalActivityArrayList;
    public static String ACTION_PERFORMED = "";
    public static boolean IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
    public static int TOTAL_RECORD_FOR_CURRENT_GROUP_SELECTED = 0;
    public static boolean IS_DFU_OPERATION_STILL_IN_PROCESS = false;
    /**
     * Constant for handling the fail/disconnect packet information like
     * group index and record index
     */
    public static int INT_CURRENT_GROUP_INDEX_BEFORE_FAIL = 0;
    public static int INT_CURRENT_RECORD_INDEX_BEFORE_FAIL = 1;
    public static int INT_CURRENT_GROUP_INDEX_REQUEST_BEFORE_FAIL = 0;

    /**
     * This boolean variable is used for handling the fetching of record or group if
     * data logging activity is already visible or not
     */
    public static boolean BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
    public static String BOOL_READING_TYPE_IN_PROCESS = "bool_reading_type_in_process";
    public static boolean BOOL_COMMAND_FOR_BOOT_LOADER_SENT = false;
    public static boolean BOOL_IS_NEED_TO_RETRIEVE_DATA = true;
    public static String GLOBAL_SAVING_LOG_TAG = "Application";
    public static String GLOBAL_BYTE_SEND_RECEIVE_LOG_TAG = "Beacon : ";
    /**
     * Boolean for handling that currently any process is going on or not
     */
    public static boolean BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
    /**
     * Boolean for handling the case of turn off command send to the device
     */
    public static boolean BOOL_IS_TURN_OFF_COMMAND_SEND = false;

    /**
     * String for holding the timestamp of current group selected by the user
     */
    public static String STRING_CURRENT_GROUP_TIMESTAMP = "string_current_group_timestamp";
    /**
     * LogSession reference object
     */
    public static LogSession mLogSession;
    /**
     * String for managing the data according to type of beacon connected
     */
    public static String STRING_CURRENT_BEACON_TYPE_CONNECTED = "string_current_beacon_type_connected";



}
