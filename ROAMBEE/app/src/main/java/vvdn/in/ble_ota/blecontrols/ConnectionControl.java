package vvdn.in.ble_ota.blecontrols;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.view.BleScanScreen;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.FileLogHelper;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.Utils.GlobalKeys;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.listener.DataLoggingListener;
import vvdn.in.ble_ota.listener.SnackBarActionButtonListener;
import vvdn.in.ble_ota.view.DataLoggingReadingActivity;
import vvdn.in.ble_ota.view.DfuActivityOperation;
import vvdn.in.ble_ota.view.SelectionActivity;

import static vvdn.in.ble_ota.Utils.GlobalConstant.mBluetoothAdapter;
import static vvdn.in.ble_ota.blecontrols.BluetoothLeService.mBluetoothGatt;


public class ConnectionControl {
    /**************************************************
     * Debugging Tag
     ****************************************************/
    private final static String TAG = ConnectionControl.class.getSimpleName();
    /**
     * Integer value indicating the delay after which mobile app will retry from connection
     */
    private static final long POPUP_DURATION = 10000;
    /*isNavigate: don't navigate to other screen on disconnect.*/
    private boolean isConnectedValue = false;
    /**
     * ConnectionControl reference object for access method for read/write/notify and
     * retrying for connection to same device if connection is lost in between.
     */
    public static ConnectionControl connectionControl;
    /**
     * BluetoothDevice reference object for holding the details of currently connected device
     */
    public static BluetoothDevice mBluetoothDevice;
    /**
     * Data Logging characteristics
     */
    public static BluetoothGattCharacteristic dl_notification_characteristics, dl_write_characteristics;
    /**
     * DFU characteristics
     */
    public static BluetoothGattCharacteristic dfu_ota_meta_characteristics, dfu_ota_file_characteristics, dfu_ota_clear_characteristics;
    /**
     * List reference object for holding the list of services that an connected device has
     */
    private List<BluetoothGattService> gattServicesList;
    /**
     * BluetoothLeService reference object
     */
    private BluetoothLeService mBluetoothLeService;
    /**
     * Activity reference object
     */
    public Activity mActivity, mCurrentlyVisibleActivity;
    /**
     * Boolean variable for handling the condition that if connected device has certain characteristics
     */
    private boolean isCharacteristicsFound;
    /**
     * Handler for showing the pop up with message if services are not found on connected device
     */
    private Handler handlerServiceNotFound;
    /**
     * Runnable reference object
     */
    private Runnable runnableService, runnable;
    /**
     * AlertDialog reference object
     */
    private AlertDialog alertDialog;
    /**
     * Boolean variable for handling is pop up alertDialog showing or not
     */
    private boolean isDisconnectionPopupShowing = false;
    /**
     * Integer reference object for holding the retry count
     */
    private int retryCount;

    /*to check only one popup is visible at a time*/
    private boolean isInCompatiblePopupShowing;
    /**
     * Integer reference object for handling the fail count of record/group fetching
     */
    private int intRetryCountForServiceDiscovery = 0, intRetryCount = 0;
    /**
     * Boolean for handling the  Service Connection
     */
    private boolean boolIsServiceBinded = false;
    /**
     * Byte array containing the previously send byte array
     */
    private byte[] bytePreviousSend;
    /**
     * List holding the number of receiver registered
     */
    private List<BroadcastReceiver> mListReceivers = new ArrayList<BroadcastReceiver>();


    /**
     * Public constructor of this class for initializing the reference component
     */
    public ConnectionControl(Activity mActivity, BluetoothDevice mBluetoothDevice, Activity mCurrentlyVisibleActivity) {
        this.mActivity = mActivity;
        this.mBluetoothDevice = mBluetoothDevice;
        this.mCurrentlyVisibleActivity = mCurrentlyVisibleActivity;
        if (mBluetoothDevice != null) {
            String mStrDeviceName = mBluetoothDevice.getName();
            if (!TextUtils.isEmpty(mStrDeviceName)) {
                if (mStrDeviceName.startsWith(AppHelper.BEACON_B1)) {
                    GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED = GlobalKeys.BEACON_TYPE_B1_CONNECTED;
                } else if (mStrDeviceName.startsWith(AppHelper.BEACON_B4)) {
                    GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED = GlobalKeys.BEACON_TYPE_B4_CONNECTED;
                } else if (mStrDeviceName.startsWith(AppHelper.BEACON_B5)) {
                    GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED = GlobalKeys.BEACON_TYPE_B5_CONNECTED;
                } else {
                    //default case if beacon name doesn't start with B1 or B4
                    GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED = GlobalKeys.BEACON_TYPE_B1_CONNECTED;
                }
            }
        }
        initViews();
    }

    /**
     * Service waiting for device to be discovered for making connection
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            // Automatically connects to the device upon successful start-up initialization.
            if (mBluetoothLeService != null) {
                isConnectedValue = mBluetoothLeService.connect(mBluetoothDevice);

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
            boolIsServiceBinded = false;
        }
    };
    /**
     * BroadcastReceiver for listening service discovery and performing
     * appropriate action(read,write and notify)
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            AndroidAppUtils.showLog(TAG, "On Broadcast Receiver : " + action);
            if (GlobalConstant.ACTION_GATT_CONNECTED.equals(action)) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.hide();
                }
                AndroidAppUtils.showLog(TAG, "Connected : " + GlobalConstant.ACTION_GATT_CONNECTED);
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattConnected(GlobalConstant.ACTION_GATT_CONNECTED);
                }

                if (mBluetoothGatt != null) {
                    mBluetoothGatt.discoverServices();
                    showPopupIFServiceNotFound();
                } else
                    AndroidAppUtils.showErrorLog(TAG, "BluetoothLeService.mBluetoothGatt is  null.");
            } else if (GlobalConstant.ACTION_GATT_DISCONNECTED.equals(action)) {
                GlobalConstant.ACTION_PERFORMED = GlobalConstant.ACTION_GATT_DISCONNECTED;
                AndroidAppUtils.showLog(TAG, "Disconnected");
                connectionStateDisconnected();
                BluetoothLeService.getInstance().close();
            } else if (GlobalConstant.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                AndroidAppUtils.showLog(TAG, "Service Discovered");
                handlerServiceNotFound.removeCallbacksAndMessages(null);
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.hide();
                    isDisconnectionPopupShowing = false;
                }
                // Show all the supported services and characteristics on the user interface.
                if (mBluetoothLeService != null && mBluetoothLeService.getSupportedGattServices() != null) {
                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    if (GlobalConstant.mDataLoggingListener != null)
                        GlobalConstant.mDataLoggingListener.OnDataLoggingWriteCharacteristicsDiscovered(AppHelper.BLE_SERVICE_DISCOVERED_SUCCESS);
                    else
                        AndroidAppUtils.showLog(TAG, "GlobalConstant.mDataLoggingListener is null");
                    if (GlobalConstant.mBluetoothConnectionStateInterface != null && GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE) {
                        GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceDiscovery(GlobalConstant.ACTION_GATT_SERVICES_DISCOVERED);
                    }
                } else {
                    AndroidAppUtils.showLog(TAG, "mBluetoothLeService.getSupportedGattServices() is null");
                }

            } else if (GlobalConstant.ACTION_DATA_AVAILABLE.equals(action)) {

                displayReadData(intent.getByteArrayExtra(GlobalConstant.EXTRA_DATA));
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceDataAvailable(GlobalConstant.ACTION_DATA_AVAILABLE, intent.getByteArrayExtra(GlobalConstant.EXTRA_DATA));
                }
            } else if (GlobalConstant.ACTION_READ_DENIED.equals(action)) {
                GlobalConstant.ACTION_PERFORMED = GlobalConstant.ACTION_READ_DENIED;
                displayReadData(mActivity.getString(R.string.strReadDenied));
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceReadNotPermitted(GlobalConstant.ACTION_READ_DENIED);
                }
            } else if (GlobalConstant.ACTION_READING_DATA.equals(action)) {
                displayReadData(mActivity.getString(R.string.strReadingData));
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceRead(GlobalConstant.ACTION_READING_DATA);
                }
            } else if (GlobalConstant.ACTION_READ_FAILED.equals(action)) {
                GlobalConstant.ACTION_PERFORMED = GlobalConstant.ACTION_READ_FAILED;
                displayReadData(mActivity.getString(R.string.strReadFailed));
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceReadNotPermitted(GlobalConstant.ACTION_READ_FAILED);
                }
            } else if (GlobalConstant.ACTION_WRITE_DENIED.equals(action)) {
                GlobalConstant.ACTION_PERFORMED = GlobalConstant.ACTION_WRITE_DENIED;
                displayWriteData(mActivity.getString(R.string.strWriteDenied));
                AndroidAppUtils.showToast(mActivity, "Write Permission Denied");
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceWriteNotPermitted(GlobalConstant.ACTION_WRITE_DENIED);
                }
            } else if (GlobalConstant.ACTION_WRITE_SUCCESS.equals(action)) {
                AndroidAppUtils.showLog(TAG, "Write Success");
                intRetryCount = 0;
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceWrite(GlobalConstant.ACTION_WRITE_SUCCESS);
                }
            } else if (GlobalConstant.ACTION_WRITE_FAILED.equals(action)) {
                GlobalConstant.ACTION_PERFORMED = GlobalConstant.ACTION_WRITE_FAILED;
                AndroidAppUtils.showToast(mActivity, "Failure");
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceWriteNotPermitted(GlobalConstant.ACTION_WRITE_FAILED);
                }
            } else if (GlobalConstant.ACTION_PERFORMED.equals(action)) {
                GlobalConstant.ACTION_PERFORMED = GlobalConstant.ACTION_WRITE_FAILED;
                AndroidAppUtils.showToast(mActivity, "Failure");
                if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                    GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceWriteNotPermitted(GlobalConstant.ACTION_WRITE_FAILED);
                }
            }
        }
    };

    /**
     * Method Name : stringMessageToShow
     * Description : This method is used for extracting the message according to event action generated
     *
     * @param strAction
     */
    private String stringMessageToShow(String strAction) {
        String strMessage = "";
        switch (strAction) {
            case GlobalConstant.ACTION_READ_DENIED:
            case GlobalConstant.ACTION_READ_FAILED:
            case GlobalConstant.ACTION_WRITE_DENIED:
            case GlobalConstant.ACTION_WRITE_FAILED:
                strMessage = mActivity.getString(R.string.strFailToFetchData);
                break;
            case GlobalConstant.ACTION_GATT_DISCONNECTED:
                strMessage = mActivity.getString(R.string.strDeviceDisconnectedTryAgain);
                break;
            default:
                strMessage = mActivity.getString(R.string.connection_fail_pls_refresh);
                break;

        }

        return strMessage;
    }

    /**
     * check whether service found or not
     */
    private void showPopupIFServiceNotFound() {
        handlerServiceNotFound = new Handler();
        runnableService = new Runnable() {
            @Override
            public void run() {
                if (!isCharacteristicsFound)
                    showInCompatiblePopUp();
            }
        };
        handlerServiceNotFound.postDelayed(runnableService, 5000);
    }


    /**
     * Method Name : doRetryForConnection
     * Description : This method is used for retrying to make connection tro selected
     * device if connection attempt fail for first time, until is fails for third time
     */
    public void doRetryForConnection() {
        retryCount++;
        AndroidAppUtils.showLog(TAG, "Retrying connection attempt  : " + retryCount);
        if (retryCount <= 2 && !isDisconnectionPopupShowing) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initViews();
                        }
                    }, 500);

                }
            });
        }

    }

    /*****************************************************************
     * Function Name : makeGattUpdateIntentFilter
     * Description : this function will make intent filter for registering Gatt update
     *
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConstant.ACTION_GATT_CONNECTED);
        intentFilter.addAction(GlobalConstant.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(GlobalConstant.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(GlobalConstant.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Method Name : sendMessageToActivity
     * Description : This method is used for updating the message to activity
     *
     * @param status
     */
    private static void sendMessageToActivity(boolean status) {
        Intent intent = new Intent(GlobalKeys.INTENT_FILTER_GPS_LOCATION_UPDATES);
        intent.putExtra(AppHelper.STATUS, status);
        LocalBroadcastManager.getInstance(BluetoothLeService.getInstance()).sendBroadcast(intent);
        if (BluetoothLeService.getInstance() != null)
            BluetoothLeService.getInstance().disconnect();
    }


    /**
     * disconnected from BLE automatically
     */
    private void connectionStateDisconnected() {
        showDisconnectionPopup();
        UnregisterAllServices();
        UnregisterUnBindAll();
        boolIsServiceBinded = false;
    }

    /**************************************************************
     * Function Name - initViews
     * Description - Initialize views that are included in this layout
     */
    private void initViews() {
        if (GlobalConstant.DEVICE_NAME.toUpperCase().startsWith(GlobalConstant.DEVICE_CONNECTING_NAME) ||
                GlobalConstant.DEVICE_NAME.toUpperCase().equalsIgnoreCase(GlobalConstant.DEVICE_CONNECTING_NAME)) {
            retryCount = 3;
        }
        connectionControl = this;
        Intent gattServiceIntent = new Intent(mActivity, BluetoothLeService.class);
        boolIsServiceBinded = mActivity.bindService(gattServiceIntent, mServiceConnection, mActivity.BIND_AUTO_CREATE);
        AndroidAppUtils.showProgressDialog(mCurrentlyVisibleActivity, mActivity.getResources().getString(R.string.strCaptionConnecting), false);
        registerAllServices();
    }


    /**
     * Method Name : showDisconnectionPopup
     * Description : this method is used for showing alert message alertDialog indicating connection is broken
     */
    private void showDisconnectionPopup() {

        if (!GlobalConstant.CONNECTED_STATE) {
            AndroidAppUtils.showLog(TAG, "Showing can't connect popup");
            showConnectionFailPopup();
        }

    }


    /**
     * Register All Things
     */
    private void registerAllServices() {
        try {
            if (mActivity != null) {
                mActivity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                mListReceivers.add(mGattUpdateReceiver);
            }
        } catch (Exception e) {
            AndroidAppUtils.showErrorLog(TAG, "registerAllServices error message : " + e.getMessage());
        }
    }

    /**
     * UnRegister All Things
     */
    public void UnregisterAllServices() {
        AndroidAppUtils.showLog(TAG, "UnregisterAllServices");
        try {
            if (mGattUpdateReceiver != null && isReceiverRegistered(mGattUpdateReceiver)) {
                mActivity.unregisterReceiver(mGattUpdateReceiver);
                mListReceivers.remove(mGattUpdateReceiver);
            }
        } catch (Exception e) {
            AndroidAppUtils.showErrorLog(TAG, "UnregisterAllServices receiver error  : " + e.getMessage());
        }
    }

    /**
     * Method Name : isReceiverRegistered
     * Description : This method is used for checking if the receiver is registered or not
     *
     * @param receiver
     * @return
     */
    public boolean isReceiverRegistered(BroadcastReceiver receiver) {
        boolean registered = mListReceivers.contains(receiver);
        AndroidAppUtils.showInfoLog(TAG, "is receiver " + receiver + " registered? " + registered);
        return registered;
    }

    /**
     * UnRegister All Things
     */
    public void UnregisterUnBindAll() {
        AndroidAppUtils.showLog(TAG, "UnregisterUnBindAll");
        try {
            if (mActivity != null && boolIsServiceBinded) {
                mActivity.unbindService(mServiceConnection);
                boolIsServiceBinded = false;
            }
        } catch (Exception e) {
            AndroidAppUtils.showErrorLog(TAG, " binder error  : " + e.getMessage());
        }
        mBluetoothLeService = null;
    }

    /************************************************************************
     * Function Name : displayGattServices
     * Description:This function will Display the Supported GattServices
     *
     * @param supportedGattServices
     */
    private void displayGattServices(List<BluetoothGattService> supportedGattServices) {
        if (supportedGattServices == null)
            return;
        gattServicesList = supportedGattServices;
        isCharacteristicsFound = false;
        if (gattServicesList != null) {
            // loop for gatt services
            AndroidAppUtils.showLog(TAG, "BLE service List " + gattServicesList.size());
            for (BluetoothGattService mGattService : gattServicesList) {
                String Service_UUID = mGattService.getUuid().toString();
                // check for custom service
                AndroidAppUtils.showLog(TAG, "Service UUID:  " + Service_UUID);
              /*  OAD SERVICE*/
                if (Service_UUID.equalsIgnoreCase(AppHelper.serviceUUIDDFU)) {

                    AndroidAppUtils.showLog(TAG, " *** DEVICE_PACKET_DATA_SERVICE_UUID WRITE***" + AppHelper.DEVICE_PACKET_DATA_SERVICE_WRITE_UUID);
                    List<BluetoothGattCharacteristic> gattCharacteristics = mGattService.getCharacteristics();
                    if (gattCharacteristics != null) {
                        // loop for characteristics
                        for (BluetoothGattCharacteristic mBlueGattCharacteristic : gattCharacteristics) {
                            String characteristics_UUID = mBlueGattCharacteristic.getUuid().toString();
                            AndroidAppUtils.showLog(TAG, "characteristics UUID : " + characteristics_UUID);
                            if (characteristics_UUID.equalsIgnoreCase(AppHelper.WitreCharacteristicsClearDFU)) {
                                AndroidAppUtils.showLog(TAG, "Firmware revision Number Characteristics Found");
                                dfu_ota_clear_characteristics = mBlueGattCharacteristic;
                                isCharacteristicsFound = true;
                                mBluetoothLeService.readCharacteristic(mBlueGattCharacteristic);
                            }
                        }
                    } else {
                        AndroidAppUtils.showErrorLog(TAG, "DFU Services or Characteristics Not Found");
                        break;
                    }
                }

             /*   DEVICE_INFORMATION SERVICE*/
                else if (Service_UUID.equalsIgnoreCase(AppHelper.serviceUUIDOTA)) {

                    AndroidAppUtils.showLog(TAG, "****DEVICE_INFORMATION_UUID *****");
                    List<BluetoothGattCharacteristic> gattCharacteristics = mGattService.getCharacteristics();
                    for (int i = 0; i < gattCharacteristics.size(); i++) {
                        AndroidAppUtils.showLog(TAG, "DIS characteristics UUID: " + gattCharacteristics.get(i).getUuid().toString());
                    }
                    if (gattCharacteristics != null) {
                        // loop for characteristics
                        AndroidAppUtils.showLog(TAG, " DIS Characteristics Size :" + gattCharacteristics.size());
                        for (BluetoothGattCharacteristic mBlueGattCharacteristic : gattCharacteristics) {
                            String characterstics_UUID = mBlueGattCharacteristic.getUuid().toString();

                            if (characterstics_UUID.equalsIgnoreCase(AppHelper.WriteCharacteristics_FILE_DATA)) {
                                AndroidAppUtils.showLog(TAG, "Firmware revision Number Characteristics Found");
//                                enableNotification(mBlueGattCharacteristic);
                                dfu_ota_clear_characteristics = null;
                                dl_write_characteristics = null;
                                dl_notification_characteristics = null;
                                dfu_ota_file_characteristics = mBlueGattCharacteristic;
                                isCharacteristicsFound = true;
                            } else if (characterstics_UUID.equalsIgnoreCase(AppHelper.WriteCharacteristics_META_DATA)) {
                                /**
                                 * Disable Notification for all DFU feature need to enable whe packet writing in process
                                 */
//                                enableNotification(mBlueGattCharacteristic);
                                dfu_ota_meta_characteristics = mBlueGattCharacteristic;
                                isCharacteristicsFound = true;
                            } else {
                                AndroidAppUtils.showErrorLog(TAG, "Firmware revision Number Characteristics NOT  Found");
                            }
                        }
                    } else {
                        AndroidAppUtils.showErrorLog(TAG, "gattCharacteristics is NULL--NO Bluetooth Gatt Characteristics are available ");
                        break;
                    }
                }
                /*DATA LOGGING UUID COMPARISION*/
                else if (Service_UUID.equalsIgnoreCase(AppHelper.service_DATA_LOGGING_UUID)) {
                    AndroidAppUtils.showLog(TAG, "****DATA_LOGGING_UUID *****");
                    List<BluetoothGattCharacteristic> gattCharacteristics = mGattService.getCharacteristics();
                    for (int i = 0; i < gattCharacteristics.size(); i++) {
                        AndroidAppUtils.showLog(TAG, "DIS characteristics UUID: " + gattCharacteristics.get(i).getUuid().toString());
                    }
                    if (gattCharacteristics != null) {
                        // loop for characteristics
                        AndroidAppUtils.showLog(TAG, " DIS Characteristics Size :" + gattCharacteristics.size());
                        for (BluetoothGattCharacteristic mBlueGattCharacteristic : gattCharacteristics) {
                            String characteristics_UUID = mBlueGattCharacteristic.getUuid().toString();
                            /*DATA  to written for fetching value from device*/
                            if (characteristics_UUID.equalsIgnoreCase(AppHelper.DATA_LOGGING_WRITE_CHARACTERISTICS_UUID)) {

                                dl_write_characteristics = mBlueGattCharacteristic;
                                AndroidAppUtils.showLog(TAG, "DATA_LOGGING_UUID Writing Characteristics Found : " + characteristics_UUID);
                                isCharacteristicsFound = true;
                            }
                              /*Enabling Notification for receiving the data*/
                            else if (characteristics_UUID.equalsIgnoreCase(AppHelper.Notification_DATA_LOGGING_UUID)) {
                                AndroidAppUtils.showLog(TAG, "Notification_DATA_LOGGING_UUID Found : " + characteristics_UUID);
                                dl_notification_characteristics = mBlueGattCharacteristic;
                                isCharacteristicsFound = true;
                                onDataReceived(mBlueGattCharacteristic);
                            } else {
                                AndroidAppUtils.showLog(TAG, "Data Logging Characteristics Not  Found");
                            }
                        }
                    } else {
                        AndroidAppUtils.showErrorLog(TAG, "gattCharacteristics is NULL--NO Bluetooth Gatt Characteristics are available ");
                        break;
                    }
                }


            }
            /**
             * Check for dfu service as well as data logging characteristics.
             * DFU service for loading the device into bootloader mode.
             */
            if ((dfu_ota_clear_characteristics != null) &&
                    (dl_notification_characteristics != null && dl_write_characteristics != null)) {
                AndroidAppUtils.showLog(TAG, "DFU and Data logging Notification and Write characteristics found");
                if (!isDisconnectionPopupShowing && !GlobalConstant.boolIsDataLoggingActivityVisible) {
                    proceedAccordingToActivityInstance(GlobalKeys.OPERATION_TYPE_DFU + GlobalKeys.OPERATION_TYPE_DATA_LOGGING);
                } else {
                    AndroidAppUtils.showLog(TAG, "Could Not Navigate to other screen");
                }
                AndroidAppUtils.showLog(TAG, "isDisconnectionPopupShowing : " + isDisconnectionPopupShowing + "\n"
                        + "  GlobalConstant.boolIsDataLoggingActivityVisible  : " + GlobalConstant.boolIsDataLoggingActivityVisible);
            }
            /**
             * Check for Data logging service and characteristics for reading logged data
             */
            else if (dl_notification_characteristics != null && dl_write_characteristics != null) {
                AndroidAppUtils.showLog(TAG, "Data Logging Notification and Write characteristics found");
                if (!isDisconnectionPopupShowing && !GlobalConstant.boolIsDataLoggingActivityVisible) {
                    proceedAccordingToActivityInstance(GlobalKeys.OPERATION_TYPE_DATA_LOGGING);
                } else {
                    AndroidAppUtils.showLog(TAG, "Could Not Navigate to other screen");
                }
                AndroidAppUtils.showLog(TAG, "isDisconnectionPopupShowing : " + isDisconnectionPopupShowing + "\n"
                        + "  GlobalConstant.boolIsDataLoggingActivityVisible  : " + GlobalConstant.boolIsDataLoggingActivityVisible);
            }
            /**
             * Check for dfu service and characteristics for performing upgradable operation
             */
            else if (dfu_ota_meta_characteristics != null && dfu_ota_file_characteristics != null) {
                AndroidAppUtils.showLog(TAG, "DFU Notification and Write characteristics found");
                if (!isDisconnectionPopupShowing && !GlobalConstant.boolIsDataLoggingActivityVisible) {
                    proceedAccordingToActivityInstance(GlobalKeys.OPERATION_TYPE_DFU);
                } else {
                    AndroidAppUtils.showErrorLog(TAG, "Could Not Navigate to other screen");
                }
                AndroidAppUtils.showLog(TAG, "isDisconnectionPopupShowing : " + isDisconnectionPopupShowing + "\n"
                        + "  GlobalConstant.boolIsDataLoggingActivityVisible  : " + GlobalConstant.boolIsDataLoggingActivityVisible);
            } else {
                AndroidAppUtils.showLog(TAG, "Neither Data Logging nor DFU Notification and Write characteristics Not found");
            }
        }
        AndroidAppUtils.showLog(TAG, "intRetryCountForServiceDiscovery : " + intRetryCountForServiceDiscovery +
                "isCharacteristicsFound : " + isCharacteristicsFound);
        /**
         * Checking if service is not discoverable on device then need to show snackbar
         */
        if (isCharacteristicsFound) {
            AndroidAppUtils.showSnackBarWithActionButton(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strDiscoveringServicesFailed),
                    new SnackBarActionButtonListener() {

                        @Override
                        public void onClickOfSnackBarActionButtonView() {
                            displayGattServices(mBluetoothLeService.getSupportedGattServices());
                        }
                    });
        }
        AndroidAppUtils.hideProgressDialog();
    }

    /**
     * Method Name : proceedAccordingToActivityInstance
     * Description : This method  is used for proceeding to next screen on successful
     * connection/discovery depending on the Activity reference object
     */
    private void proceedAccordingToActivityInstance(String strOperationType) {
        if (mCurrentlyVisibleActivity != null) {
            intRetryCountForServiceDiscovery = 0;
            Intent mActivityIntent;
            if (mCurrentlyVisibleActivity instanceof BleScanScreen) {
                mCurrentlyVisibleActivity.overridePendingTransition(0, 0);
                mActivityIntent = new Intent(mActivity, SelectionActivity.class);
                mActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mActivityIntent.putExtra(GlobalKeys.STRING_OPERATION_TYPE, strOperationType);
                mCurrentlyVisibleActivity.startActivity(mActivityIntent);
            } else if (mCurrentlyVisibleActivity instanceof DataLoggingReadingActivity) {
                // Do nothing
            } else if (mCurrentlyVisibleActivity instanceof DfuActivityOperation) {
                // Do nothing
            }
        }
    }

    /**
     * Method Name : onDataReceived
     * Description : This method is used for receiving the byte data received from device
     *
     * @param mBlueGattCharacteristic
     */
    private void onDataReceived(final BluetoothGattCharacteristic mBlueGattCharacteristic) {
        GlobalConstant.mDataLoggingListener = new DataLoggingListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void OnNotificationReceived(byte[] byteData) {
                AndroidAppUtils.showLog(TAG, "byteData : " + byteData + "byteData length : " + byteData.length);
                if (AppApplication.getInstance().getCurrentActivity() != null) {
                    if (DataLoggingReadingActivity.mDataLoggingListener != null) {
                        DataLoggingReadingActivity.mDataLoggingListener.OnNotificationReceived(byteData);
                    } else {
                        AndroidAppUtils.showLog(TAG, " DataLoggingReadingActivity.mDataLoggingListener is null");
                    }
                    if (SelectionActivity.mDataLoggingListener != null) {
                        SelectionActivity.mDataLoggingListener.OnNotificationReceived(byteData);
                    }
                }
            }

            @Override
            public void OnDataLoggingWriteCharacteristicsDiscovered(String status) {

            }
        };
    }


    /**
     * Enables or disables notification on a give characteristic.     *
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            AndroidAppUtils.showLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(AppHelper.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            if (enabled) {
                GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            } else {
                GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
                if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED) {
                    GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
                }
            }
        } else {
            AndroidAppUtils.showLog(TAG, "No descriptor is Available");
        }
    }

    /***************************************************************************
     * Function Name- enableNotification
     * Descriptor - This function will enable the notification
     *     * @param mCharacteristic
     **************************************************************************/
    public void enableNotification(BluetoothGattCharacteristic mCharacteristic) {
        final int charaProp = mCharacteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            AndroidAppUtils.showLog(TAG, "Notification Feature is there enable it");
            setCharacteristicNotification(mCharacteristic, true);
        } else {
            AndroidAppUtils.showLog(TAG, "No Notification Descriptor is there");
        }
    }

    /***************************************************************************
     * Function Name- disableNotification
     * Descriptor - This function will enable the notification
     *     * @param mCharacteristic
     **************************************************************************/
    public void disableNotification(BluetoothGattCharacteristic mCharacteristic) {
        final int charaProp = mCharacteristic != null ? mCharacteristic.getProperties() : -1;
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            AndroidAppUtils.showLog(TAG, "Notification Feature is there disable it");
            setCharacteristicNotification(mCharacteristic, false);
        } else {
            AndroidAppUtils.showLog(TAG, "No Notification Descriptor is there");
        }
    }

    /**
     * To show device is not compatible
     */
    private void showInCompatiblePopUp() {

        if (mActivity != null) {
            if (!(mActivity instanceof BleScanScreen))
                AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getString(R.string.not_campatible));

            AlertDialog.Builder builder = AndroidAppUtils.showAlertDialogWithButtonControls(mActivity, mActivity.getString(R.string.not_campatible));

            builder.setPositiveButton(mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    isInCompatiblePopupShowing = false;
                    dialog.dismiss();
                }
            });
            if (GlobalConstant.CONNECTED_STATE && !isInCompatiblePopupShowing) {
                builder.show();
                isCharacteristicsFound = true;
                AndroidAppUtils.showLog(TAG, "Popup incompatible showing : " + isInCompatiblePopupShowing);
                isInCompatiblePopupShowing = true;
            }
        }
        if (GlobalConstant.CONNECTED_STATE)
            AndroidAppUtils.showLog(TAG, mActivity.getString(R.string.not_campatible));
        AndroidAppUtils.hideProgressDialog();
    }

    /**
     * Method Name : getCharacteristicFromUUID
     * Description : This method is used for getting the characteristics from UUID
     *
     * @param UUID
     * @return
     */
    public BluetoothGattCharacteristic getCharacteristicFromUUID(String UUID) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = null;

        for (BluetoothGattService gattService : gattServicesList) {

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                String uuid = gattCharacteristic.getUuid().toString();

                if (uuid.equalsIgnoreCase(UUID)) {
                    bluetoothGattCharacteristic = gattCharacteristic;
                    return bluetoothGattCharacteristic;
                }
            }
        }

        return bluetoothGattCharacteristic;
    }

    /**
     * Method Name : displayWriteData
     * Description : This method is used for displaying the status of written packet
     *
     * @param data
     */
    private void displayWriteData(String data) {
        if (data != null) {
            AndroidAppUtils.showToast(mActivity, data);
        }
    }

    /**
     * Method Name : displayReadData
     * Description : This method is used for displaying the reading data
     *
     * @param data
     */
    private void displayReadData(String data) {
        if (data != null) {
            AndroidAppUtils.showLog(TAG, "DATA READ : " + data);
            data = data.replaceAll("[^\\p{Alpha}\\p{Digit}]+", "").trim();
            data = ValidatedData(data);
            AndroidAppUtils.showLog(TAG, "DATA READ After Removing special char : " + data);
        } else
            AndroidAppUtils.showErrorLog(TAG, "Data is null");
    }

    /**
     * display data for notifications received (OAD)
     *
     * @param data
     */
    private void displayReadData(byte[] data) {
        if (data != null) {
            AndroidAppUtils.showInfoLog(TAG, "Notification Data Received : " + AndroidAppUtils.convertToHexString(data));
            if (GlobalConstant.mDataLoggingListener != null)
                GlobalConstant.mDataLoggingListener.OnNotificationReceived(data);
        } else
            AndroidAppUtils.showErrorLog(TAG, "Data is null");
    }

    /**
     * Method Name : ValidatedData
     * Description : This method is used for validating the word for length 4
     *
     * @param word
     * @return
     */
    private String ValidatedData(String word) {
        if (word.length() == 4) {
            return word;
        } else if (word.length() > 4) {
            return word.substring(word.length() - 4);
        } else {
            // whatever is appropriate in this case
            throw new IllegalArgumentException("word has less than 3 characters!");
        }
    }


    /**
     * Method Name : convertToHexString
     * Description : This method is used for converting byte array
     * data into hex string
     *
     * @param data
     * @return
     */
    private static String convertToHexString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) buf.append((char) ('0' + halfbyte));
                else buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Method Name : writeFileToDevice
     * Description : This method is used for writing the first byte to the device
     *
     * @param mBlueGattCharacteristic
     * @param mByteToBeWritten
     */
    public void writeFileToDevice(BluetoothGattCharacteristic mBlueGattCharacteristic, byte[] mByteToBeWritten) {
        if (mBlueGattCharacteristic != null) {
            byte[] byteValueToBeWritten = mByteToBeWritten;
            AndroidAppUtils.showLog(TAG, "byte data to be written : " + byteValueToBeWritten + "hex string : " + convertToHexString(byteValueToBeWritten));
            mBlueGattCharacteristic.setValue(byteValueToBeWritten);
            mBlueGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            boolean status = BluetoothLeService.getInstance().mBluetoothGatt.writeCharacteristic(mBlueGattCharacteristic);
            AndroidAppUtils.showLog(TAG, "Sending status " + status);
            if (status) {
                AndroidAppUtils.showLog(TAG, "----SEND DATA for Entering into bootloader mode successful----");
            } else {
                AndroidAppUtils.showLog(TAG, "----SEND DATA for Entering into bootloader mode fail----");
                AndroidAppUtils.hideProgressDialog();
                if (mByteToBeWritten != null) {
                    if (mByteToBeWritten.length > 2) {
                        AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getString(R.string.strClearCommandFailureMsg));
                    } else {
                        AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getString(R.string.strDFUFailureMsg));
                    }
                }

            }
        } else {
            AndroidAppUtils.showLog(TAG, "Gatt Characteristics in NULL");
        }
    }

    /**
     * Method Name : showConnectionFailPopup
     * Description : This method is used for displaying the pop up with message for connection failure to device
     */
    public void showConnectionFailPopup() {
        AndroidAppUtils.showLog(TAG, " GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE :"
                + GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE +
                " \n isDisconnectionPopupShowing  : " + isDisconnectionPopupShowing +
                " GlobalConstant.ACTION_PERFORMED : " + GlobalConstant.ACTION_PERFORMED);
        retryCount = 3;
        if (AppApplication.getInstance() != null) {
            String strMessage = stringMessageToShow(GlobalConstant.ACTION_PERFORMED);
            if (GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE) {
                dismissDialog();
                showMessageDialog(AppApplication.getInstance(), strMessage
                        , mActivity.getString(R.string.ok));
            } else {
                AndroidAppUtils.showInfoLog(TAG, " No Need to Show disconnect pop up");
            }


        } else AndroidAppUtils.showInfoLog(TAG, "mActivity is null");
    }

    /**
     * Method Name : removeAllActivityExceptScanning
     * Description : This  method is used for removing the all the activity except BLE Scanning
     */
    public void removeAllActivityExceptScanning() {
        AndroidAppUtils.showInfoLog(TAG, "GlobalConstant.mGlobalActivityArrayList size : " + GlobalConstant.mGlobalActivityArrayList.size());
        if (GlobalConstant.mGlobalActivityArrayList != null && GlobalConstant.mGlobalActivityArrayList.size() > 0) {
            if (DeviceAdapter.mConnectionControl != null) {
                DeviceAdapter.mConnectionControl.UnregisterAllServices();
                DeviceAdapter.mConnectionControl.UnregisterUnBindAll();
            }
            for (int i = GlobalConstant.mGlobalActivityArrayList.size() - 1; i > 0; i--) {
                if (GlobalConstant.mGlobalActivityArrayList.get(i) instanceof BleScanScreen) {
                    AndroidAppUtils.showInfoLog(TAG, " Current Activity is Ble Screen");
                    /**
                     * When all the activity is removed except ble scan screen then we need to set reference for this activity
                     */
                    AppApplication.getInstance().setCurrentActivityReference(GlobalConstant.mGlobalActivityArrayList.get(i));
                } else {
                    GlobalConstant.mGlobalActivityArrayList.get(i).finish();
                    GlobalConstant.mGlobalActivityArrayList.remove(i);
                }
            }
        }
    }


    /**
     * Show Message Info View
     *
     * @param mActivity
     * @param msg
     */
    @SuppressLint("InflateParams")
    private void showMessageDialog(final AppApplication mActivity, final String msg, final String buttonMsg) {
        if (mActivity != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom));
            builder.setTitle(R.string.app_name);
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton(buttonMsg,
                    new DialogInterface.OnClickListener() {

                        @SuppressWarnings("static-access")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isDisconnectionPopupShowing = false;
                            alertDialog.dismiss();
                            alertDialog = null;
                            removeAllActivityExceptScanning();
                            if (AppApplication.getInstance().getCurrentActivity() != null && AppApplication.getInstance().getCurrentActivity() instanceof BleScanScreen) {
                                ((BleScanScreen) (AppApplication.getInstance().getCurrentActivity())).checkBluetoothAndGPS();
                            }

                        }
                    });

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (alertDialog == null) {
                        alertDialog = builder.create();
                        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        AndroidAppUtils.showLog(TAG, "mActivity.getCurrentActivity() : " + mActivity.getCurrentActivity().getClass().getSimpleName());
                        if (!mActivity.getCurrentActivity().isFinishing()) {
                            try {
                                if (alertDialog != null && !isDisconnectionPopupShowing && !alertDialog.isShowing()) {
                                    isDisconnectionPopupShowing = true;
                                    alertDialog.show();
                                }
                            } catch (Exception e) {
                                AndroidAppUtils.showLog(TAG, "error message while showing dialog " + e.getMessage());
                            }
                        }
                    } else {
                        AndroidAppUtils.showErrorLog(TAG, " Alert Dialog is already showing");
                        alertDialog = null;
                    }
                }
            });
        } else {
            AndroidAppUtils.showLog(TAG, " mActivity is null");
        }
    }

    /**
     * Method Name : writeToDevice
     * Description : This method is used for write byte array data to beacon device
     *
     * @param mBlueGattCharacteristic
     * @param mByteToBeWritten
     */
    public void writeToDevice(final BluetoothGattCharacteristic mBlueGattCharacteristic, final byte[] mByteToBeWritten) {
        if (mBlueGattCharacteristic != null) {
            /**
             * Last byte code performing retrieval purpose
             */
            byte[] byteLastCharacter = {(byte) 0x0F, (byte) 0x03};
            byte[] value = new byte[DataLoggingReadingActivity.sendReadingGroupToken.length + mByteToBeWritten.length + byteLastCharacter.length];
            System.arraycopy(DataLoggingReadingActivity.sendReadingGroupToken, 0, value, 0, DataLoggingReadingActivity.sendReadingGroupToken.length);
            System.arraycopy(mByteToBeWritten, 0, value, DataLoggingReadingActivity.sendReadingGroupToken.length, mByteToBeWritten.length);
            value[value.length - 2] = (byte) 0xf;
            value[value.length - 1] = (byte) 0x3;
            AndroidAppUtils.showInfoLog(TAG, "byte send to device : "/* + value */ + "hex string : " + AndroidAppUtils.convertToHexString(value));
            FileLogHelper.getInstance().addLogTag(TAG, "i");
            mBlueGattCharacteristic.setValue(value);
            mBlueGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            boolean status = false;
            if (BluetoothLeService.getInstance() != null && BluetoothLeService.getInstance().mBluetoothGatt != null)
                status = BluetoothLeService.getInstance().mBluetoothGatt.writeCharacteristic(mBlueGattCharacteristic);

            AndroidAppUtils.showLog(TAG, "Sending status " + status);
            if (status) {
                /**
                 * Saving the byte array for handling the  first group request response
                 */
                bytePreviousSend = value;
                AndroidAppUtils.showLog(TAG, "----SEND DATA successful----");
            } else {
                AndroidAppUtils.showLog(TAG, "----SEND DATA To failed---- + " + intRetryCount);

            }
        } else {
            AndroidAppUtils.showLog(TAG, "Gatt Characteristics in NULL");
        }
    }

    /**
     * Method Name : writeToDevice
     * Description : This method is used for write byte array data to beacon device
     *
     * @param mBlueGattCharacteristic
     * @param mByteToBeWritten
     * @param mActivity
     */
    public void writeFirstByteToDevice(BluetoothGattCharacteristic mBlueGattCharacteristic, byte[] mByteToBeWritten, Activity mActivity) {
        if (mBlueGattCharacteristic != null) {
            byte[] value = mByteToBeWritten;
            AndroidAppUtils.showLog(GlobalConstant.GLOBAL_SAVING_LOG_TAG, "byte data to be written : " + value + "hex string : " + AndroidAppUtils.convertToHexString(value));
            mBlueGattCharacteristic.setValue(value);
            mBlueGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            boolean status = BluetoothLeService.getInstance().mBluetoothGatt.writeCharacteristic(mBlueGattCharacteristic);
            AndroidAppUtils.showLog(TAG, "Sending status " + status);
            if (status) {
                AndroidAppUtils.showLog(TAG, "----SEND DATA successful----");

            } else {
                AndroidAppUtils.showLog(TAG, "----SEND DATA TO failed----");
            }
        } else {
            AndroidAppUtils.showLog(TAG, "Gatt Characteristics in NULL");
        }
    }

    /**
     * Method Name : dismissDialog
     * Description : this method is used for dismissing the alertDialog
     */
    public void dismissDialog() {
        AndroidAppUtils.showLog(TAG, "*********** dismissDialog **************");
        if (alertDialog != null) {
            alertDialog.dismiss();
            isDisconnectionPopupShowing = false;
            alertDialog = null;
        }
    }
}
