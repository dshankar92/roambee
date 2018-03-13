package vvdn.in.ble_ota.view;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.Utils.GlobalKeys;
import vvdn.in.ble_ota.Utils.MyCountDownTimer;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.blecontrols.BluetoothLeService;
import vvdn.in.ble_ota.blecontrols.ConnectionControl;
import vvdn.in.ble_ota.control.HeaderViewManager;
import vvdn.in.ble_ota.listener.DataLoggingListener;
import vvdn.in.ble_ota.listener.HeaderViewClickListener;
import vvdn.in.ble_ota.listener.SnackBarActionButtonListener;
import vvdn.in.ble_ota.model.GroupConfigurationModel;

/**
 * Class Name : SelectionActivity
 * Description : This class is used for prompting user to select which operation he want to perform.
 *
 * @author Durgesh-Shankar
 */

public class SelectionActivity extends Activity implements View.OnClickListener {

    /**
     * Button reference object
     */
    private Button btn_upgrade, btn_data_logging, btnChangeConfiguration;
    /**
     * Activity reference object
     */
    private Activity mActivity;
    /**
     * Debuggable TAG
     */
    private String TAG = SelectionActivity.class.getSimpleName();
    /**
     * DataLoggingListener reference object
     */
    public static DataLoggingListener mDataLoggingListener;

    /**
     * String reference object for holding the operation that need to be done
     */
    public String strOperationType = GlobalKeys.OPERATION_TYPE_DFU;
    private boolean boolIsClearDataCommandSend = false;
    /**
     * MyCountDownTimer reference object for handling configuration retrieval
     */
    private MyCountDownTimer myCountDownTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_screen);
        initView();
        manageHeaderView();

    }

    /**
     * Method Name : retrieveCurrentConfigurationOfDevice
     * Description : This method is used for retrieving the configuration from the device
     */
    private void retrieveCurrentConfigurationOfDevice() {
        AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.strRetrievingConfiguration), false);
        final byte sendReadingToken[] = {(byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x01};

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                myCountDownTimer.start();
                myCountDownTimer.stringGeneratedForActionOccurred(mActivity.getResources().getString(R.string.str_Fail_to_retrieve_config));

                if (ConnectionControl.dl_write_characteristics != null)
                    DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, sendReadingToken);
            }
        }, 2000);
    }

    /**
     * Method Name : initView
     * Description : this method is used for initializing the view component
     */
    private void initView() {
        mActivity = SelectionActivity.this;
        GlobalConstant.mGlobalActivityArrayList.add(mActivity);
        btn_data_logging = (Button) findViewById(R.id.btn_data_logging);
        btn_upgrade = (Button) findViewById(R.id.btn_upgrade);
        btnChangeConfiguration = (Button) findViewById(R.id.btnChangeConfiguration);
        btn_upgrade.setOnClickListener(this);
        btn_data_logging.setOnClickListener(this);
        btnChangeConfiguration.setOnClickListener(this);
        GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
        if (getIntent() != null) {
            if (getIntent().hasExtra(GlobalKeys.STRING_OPERATION_TYPE)) {
                strOperationType = getIntent().getStringExtra(GlobalKeys.STRING_OPERATION_TYPE);
                AndroidAppUtils.showInfoLog(TAG, "strOperationType : " + strOperationType);
            } else {
                AndroidAppUtils.showInfoLog(TAG, "Intent data do not have GlobalKeys.STRING_OPERATION_TYPE");
            }
        } else {
            AndroidAppUtils.showInfoLog(TAG, "Intent Data is null");
        }
        changeButtonUI(strOperationType);
        /**
         * Setting timer of 10 second for fetching the configuration
         */
        myCountDownTimer = new MyCountDownTimer(mActivity, 10000, 1000, new SnackBarActionButtonListener() {
            @Override
            public void onClickOfSnackBarActionButtonView() {
                retrieveCurrentConfigurationOfDevice();
            }
        });
    }

    /**
     * Method Name :changeButtonUI
     * Description : This method is used for enabling/disabling the button accordingly
     *
     * @param strOperationType
     */
    private void changeButtonUI(String strOperationType) {
        if (strOperationType.contains(GlobalKeys.OPERATION_TYPE_DFU + GlobalKeys.OPERATION_TYPE_DATA_LOGGING)) {
            btn_data_logging.setEnabled(true);
            btnChangeConfiguration.setEnabled(true);
            btn_upgrade.setEnabled(true);
        } else if (strOperationType.contains(GlobalKeys.OPERATION_TYPE_DATA_LOGGING)) {
            btn_data_logging.setEnabled(true);
            btnChangeConfiguration.setEnabled(true);
            btn_upgrade.setEnabled(false);
        } else if (strOperationType.contains(GlobalKeys.OPERATION_TYPE_DFU)) {
            btn_upgrade.setEnabled(true);
            btn_data_logging.setEnabled(false);
            btnChangeConfiguration.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_upgrade:
                if (checkDeviceConnectivity()) {
                    GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = false;
                    if (strOperationType.contains(GlobalKeys.OPERATION_TYPE_DFU + GlobalKeys.OPERATION_TYPE_DATA_LOGGING)) {
                        /**
                         * Send Command to device for entering into boot loader mode
                         */
//                        sendCommandForBootLoader();
                        sendCommandForClearData();
                    } else {
                        mDataLoggingListener = null;
                        startActivity(new Intent(mActivity, DfuActivityOperation.class));
                    }
                }
                break;
            case R.id.btn_data_logging:
                mDataLoggingListener = null;
                startActivity(new Intent(mActivity, DataLoggingReadingActivity.class));
                break;
            case R.id.btnChangeConfiguration:
                mDataLoggingListener = null;
                GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = false;
                startActivity(new Intent(mActivity, ChangeConfigurationActivity.class));
                break;
        }
    }

    /**
     * Method Name : sendCommandForBootLoader
     * Description : This method is used for sending command to device for switching it into
     * bootloader mode
     */
    private void sendCommandForBootLoader() {
        try {
            final byte[] byteBootLoader = {(byte) 0x01};
            if (DeviceAdapter.mConnectionControl != null && ConnectionControl.connectionControl != null) {
//                AndroidAppUtils.showProgressDialog(mActivity, "Please Wait...", false);
                DeviceAdapter.mConnectionControl.enableNotification(ConnectionControl.connectionControl.getCharacteristicFromUUID(AppHelper.WitreCharacteristicsClearDFU));
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GlobalConstant.BOOL_COMMAND_FOR_BOOT_LOADER_SENT = true;
                        DeviceAdapter.mConnectionControl.writeFileToDevice
                                (ConnectionControl.connectionControl.getCharacteristicFromUUID(AppHelper.WitreCharacteristicsClearDFU), byteBootLoader);
                    }
                }, 1000);

            } else {
                AndroidAppUtils.showErrorLog(TAG, "Either ConnectionControl.connectionControl or DeviceAdapter.mConnectionControl is null");
            }
        } catch (Exception e) {
            AndroidAppUtils.showInfoLog(TAG, "error message " + e.getMessage());
        }

    }

    /*******************************************************************
     * Function name - manageHeaderView
     * Description - manage the initialization, visibility and click
     * listener of view fields on Header view
     *******************************************************************/
    public void manageHeaderView() {
        HeaderViewManager.getInstance().InitializeMultiTitleHeaderView(mActivity, null, false, manageHeaderClick());
        HeaderViewManager.getInstance().setHeading(true, mActivity.getResources().getString(R.string.strSelectionScreenCaption), mActivity);
        HeaderViewManager.getInstance().setSubHeading(false, "");
        HeaderViewManager.getInstance().setLeftSideHeaderView(true, false, R.drawable.back, "");
        HeaderViewManager.getInstance().setRightSideHeaderView(false, false, 0, "");
    }

    /*****************************************************************************
     * Function name - manageHeaderClick
     * Description - manage the click on the left and right image view of header
     *****************************************************************************/
    private HeaderViewClickListener manageHeaderClick() {
        HeaderViewClickListener headerViewClickListener = new HeaderViewClickListener() {
            @Override
            public void onClickOfHeaderLeftView() {
                onBackPressed();
            }

            @Override
            public void onClickOfHeaderRightView() {

            }
        };
        return headerViewClickListener;
    }

    @Override
    public void onBackPressed() {
        if (GlobalConstant.CONNECTED_STATE) {
            if (BluetoothLeService.getInstance() != null) {
                BluetoothLeService.getInstance().disconnect();
                GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
                GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = true;
            } else {
                AndroidAppUtils.showLog(TAG, "BluetoothLeService.getInstance() is null");
                GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        setUpListener();
        if (strOperationType.contains(GlobalKeys.OPERATION_TYPE_DFU + GlobalKeys.OPERATION_TYPE_DATA_LOGGING)) {
            if (checkDeviceConnectivity()) {
                if (GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA) {
                    retrieveCurrentConfigurationOfDevice();
                    GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = false;
                }
            }
        }
        if (mActivity != null) {
            AppApplication.getInstance().setCurrentActivityReference(mActivity);
        }
        super.onResume();
    }

    /**
     * Method Name : sendCommandForClearData
     * Description : This method is used for sending command to device for clear data as well
     * as configuration on device
     */
    private void sendCommandForClearData() {
        try {
            final byte[] byteClearData = {(byte) 0x10, (byte) 0x04, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05
                    , (byte) 0x00, (byte) 0x01, (byte) 0x15, (byte) 0x04};
            if (DeviceAdapter.mConnectionControl != null && ConnectionControl.connectionControl != null) {
                AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.please_wait), false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolIsClearDataCommandSend = true;
                        DeviceAdapter.mConnectionControl.writeFileToDevice
                                (ConnectionControl.dl_notification_characteristics, byteClearData);
                    }
                }, 1000);

            } else {
                AndroidAppUtils.showErrorLog(TAG, "Either ConnectionControl.connectionControl or DeviceAdapter.mConnectionControl is null");
            }
        } catch (
                Exception e)

        {
            AndroidAppUtils.showInfoLog(TAG, "error message " + e.getMessage());
        }

    }

    /**
     * Method Name : setUpListener
     * Description : This method is used for setting up the listener for listening the changed data
     */
    private void setUpListener() {
        mDataLoggingListener = new DataLoggingListener() {
            @Override
            public void OnNotificationReceived(byte[] byteData) {
                AndroidAppUtils.showInfoLog(TAG, "configuration data : " + AndroidAppUtils.convertToHexString(byteData) +
                        " byteData length : " + byteData.length);


                if (byteData != null && byteData.length > 3) {
                    String strInValidValue = AndroidAppUtils.unHex(AndroidAppUtils.convertToHexString(byteData));
//                    int intValue = Integer.parseInt(strInValidValue);
//                    AndroidAppUtils.showErrorLog(TAG, "intValue : " + intValue);
                    if (!strInValidValue.equalsIgnoreCase("000000000000"))
                        saveRetrievedData(byteData);
                    enableDisableNotification(false);
                    myCountDownTimer.cancel();
                } else {
                    GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
                    AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strEnteredBootLoaderModeCaption));
                    GlobalConstant.BOOL_COMMAND_FOR_BOOT_LOADER_SENT = false;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (DeviceAdapter.mConnectionControl != null)
                                DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                        }
                    }, 2000);
                }

                AndroidAppUtils.hideProgressDialog();

            }

            @Override
            public void OnDataLoggingWriteCharacteristicsDiscovered(String status) {
                if (strOperationType.contains(GlobalKeys.OPERATION_TYPE_DFU + GlobalKeys.OPERATION_TYPE_DATA_LOGGING)) {

                    if (boolIsClearDataCommandSend) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendCommandForBootLoader();
                            }
                        }, 2000);

                        boolIsClearDataCommandSend = false;
                    } else {
                        enableDisableNotification(true);
                    }
                }
            }

        };
    }

    /**
     * Method Name : saveRetrievedData
     * Description : This method is used for saving the retrieved configuration from the device
     *
     * @param byteData
     */
    private void saveRetrievedData(byte[] byteData) {
        byte byteConfigurationData;
        if (byteData != null && byteData.length > 0) {
            GroupConfigurationModel groupConfigurationModel = new GroupConfigurationModel();
            for (int i = 0; i < byteData.length; i++) {
                if (i == 0) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "broadcasting interval : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrPrfTimeOut((byteConfigurationData & 0xff) + "");
                } else if (i == 1) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "tx power : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrTxPower((byteConfigurationData & 0xff) + "");
                } else if (i == 2) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "tamper : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrTamper((byteConfigurationData & 0xff) + "");
                } else if (i == 3) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "channel 37 : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrChannel37((byteConfigurationData & 0xff) + "");
                } else if (i == 4) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "channel 38 : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrChannel38((byteConfigurationData & 0xff) + "");
                } else if (i == 5) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "channel 39 : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrChannel39((byteConfigurationData & 0xff) + "");
                } else if (i == 6) {
                    byteConfigurationData = byteData[i];
                    AndroidAppUtils.showInfoLog(TAG, "data logging interval : " + (byteConfigurationData & 0xff));
                    groupConfigurationModel.setStrDataLoggingInterval((byteConfigurationData & 0xff) + "");
                }

            }
            if (groupConfigurationModel != null) {
                AppApplication.getInstance().saveConfigurationData(GlobalConstant.DEVICE_MAC, groupConfigurationModel);
            } else {
                AndroidAppUtils.showInfoLog(TAG, "groupConfigurationModel is null");
            }
        }
    }

    /**
     * Method Name : enableDisableNotification
     * Description : this method is used for enabling/disabling the notification on data logging
     * characteristics
     *
     * @param enableDisableStatus
     */
    private void enableDisableNotification(boolean enableDisableStatus) {
        if (enableDisableStatus) {
            DeviceAdapter.mConnectionControl.enableNotification(ConnectionControl.dl_notification_characteristics);
        } else {
            DeviceAdapter.mConnectionControl.disableNotification(ConnectionControl.dl_notification_characteristics);
        }
    }

    /**
     * Method Name : checkDeviceConnectivity
     * Description : This method is used for checking the device connectivity
     * and if connection is lost retry for making the connection
     *
     * @return
     */
    private boolean checkDeviceConnectivity() {
        boolean boolIsDeviceConnected = true;
        if (GlobalConstant.CONNECTED_STATE) {
            boolIsDeviceConnected = true;
        } else {
            boolIsDeviceConnected = false;
            AndroidAppUtils.showLog(TAG, "Need to connect to device");
            BluetoothDevice bluetoothDevice = GlobalConstant.mBluetoothAdapter != null ? GlobalConstant.mBluetoothAdapter.getRemoteDevice(GlobalConstant.DEVICE_MAC) : null;
            AndroidAppUtils.hideProgressDialog();
            if (ConnectionControl.connectionControl != null && ConnectionControl.connectionControl.mActivity != null)
                DeviceAdapter.mConnectionControl = new ConnectionControl(ConnectionControl.connectionControl.mActivity, bluetoothDevice, mActivity);
            else
                AndroidAppUtils.showErrorLog(TAG, " ConnectionControl.connectionControl.mActivity is null");
        }
        return boolIsDeviceConnected;
    }

    @Override
    protected void onDestroy() {
        if (mDataLoggingListener != null) {
            mDataLoggingListener = null;
        }
//        if (!GlobalConstant.CONNECTED_STATE && ConnectionControl.connectionControl != null)
//            ConnectionControl.connectionControl.UnregisterAllServices();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mDataLoggingListener != null) {
            mDataLoggingListener = null;
        }
        super.onPause();
    }
}
