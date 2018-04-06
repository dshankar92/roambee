package vvdn.in.ble_ota.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;

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
import vvdn.in.ble_ota.listener.BluetoothConnectionStateInterface;
import vvdn.in.ble_ota.listener.ChoiceDialogClickListener;
import vvdn.in.ble_ota.listener.DataLoggingListener;
import vvdn.in.ble_ota.listener.HeaderViewClickListener;
import vvdn.in.ble_ota.listener.SnackBarActionButtonListener;
import vvdn.in.ble_ota.model.GroupConfigurationModel;

/**
 * Class Name : ChangeConfigurationActivity
 * Description : This class is used for changing the configuration data on device like txpower, timeout,
 * tamper,ambient value etc
 *
 * @author Durgesh-Shankar
 */
public class ChangeConfigurationActivity extends Activity implements View.OnClickListener {
    /**
     * EditText reference object
     */
    private EditText mAls_EditText, mTamper_EditText, mPharmaModeDelayValue_EditText, mUserNoteEdittext;
    /**
     * Spinner reference object
     */
    private Spinner mChannel_37_Spinner, mChannel_38_Spinner, mChannel_39_Spinner;
    private Spinner mLogin_Interval_Spinner, mPrf_timeout_spinner,
            mTx_power_spinner, mPharmaModeSpinner;
    private String mPrfTimeOutArray[], mChannel37SpinnerArray[], mChannel38SpinnerArray[],
            mChannel39SpinnerArray[], mDataLoggingArray[], mTxPowerArray[], mPharmaModeSpinnerArray[];
    /**
     * String reference object
     */
    private String channel_37_Int = "", channel_38_Int = "", channel_39_Int = "",
            mLogin_Interval_Int = "", mTxPowerInt = "", mPrfTimeOut = "", mAls_String = "",
            mTamper_String = "", mPharmaModeDelay_String = "", mPharmaMode_String;
    /**
     * Button reference object
     */
    private Button mCancel_Button, mSend_Button, btnClearData, btnResetDevice, btnSave, btnResetPharmaMode;
    /**
     * Byte data that needs to be written on failure
     */
    private byte[] mByteDataNeedToWritten;
    /**
     * DataLoggingListener reference object
     */
    public static DataLoggingListener mDataLoggingListener;
    /**
     * Debuggable TAG
     */
    private String TAG = ChangeConfigurationActivity.class.getSimpleName();
    /**
     * Activity reference object
     */
    private Activity mActivity;
    /**
     * String reference object
     */
    private String strTurnOff = AppHelper.ZERO_TWO;
    /**
     * Boolean reference object for handling the on/off of power button
     */
    private boolean boolISTurnedOff = true, boolIsClearDataCommandSend = false, boolIsResetDeviceCommandSend = false,
            boolIsUserNoteSend = false, boolIsUserNoteSendRequestGenerated = false,
            boolIsUserNoteRetrieveCommandSend = false, boolIsCommandForResettingPharmaModeSend = false;
    /**
     * LinearLayout reference object
     */
    private LinearLayout lLPharmaModeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_change_configuartion_dialog);
        initView();
        manageHeaderView();
        retrieveLastConfiguration();
        retrieveUserCommentForDevice();
    }

    /**
     * Method Name : retrieveLastConfiguration
     * Description : This method is used for retrieving the last saved configuration from the db
     */
    private void retrieveLastConfiguration() {
        AndroidAppUtils.showInfoLog(TAG, " ************** retrieveLastConfiguration *************** " + GlobalConstant.DEVICE_MAC);
        GroupConfigurationModel groupConfigurationModel = AppApplication.getInstance().getLastConfiguration(GlobalConstant.DEVICE_MAC);
        if (groupConfigurationModel != null) {
            AndroidAppUtils.showErrorLog(TAG, "groupConfigurationModel.getStrPrfTimeOut() : " + groupConfigurationModel.getStrPrfTimeOut()
                    + "\n  groupConfigurationModel.getStrTxPower() : " + groupConfigurationModel.getStrTxPower() +
                    "\n  groupConfigurationModel.getStrDataLoggingInterval() : " + groupConfigurationModel.getStrDataLoggingInterval() +
                    "\n  groupConfigurationModel.getStrChannel37() : " + groupConfigurationModel.getStrChannel37() +
                    " \ngroupConfigurationModel.getStrChannel38() : " + groupConfigurationModel.getStrChannel38() +
                    "\n  groupConfigurationModel.getStrChannel39() : " + groupConfigurationModel.getStrChannel39() +
                    "\n  groupConfigurationModel.getStrTamper() : " + groupConfigurationModel.getStrTamper() +
                    "\n groupConfigurationModel.getStrPharmaModeValue() : " + groupConfigurationModel.getStrPharmaModeValue() +
                    "\n  groupConfigurationModel.getStrPharmaModeDelay() : " + groupConfigurationModel.getStrPharmaModeDelay()
            );
            AndroidAppUtils.showErrorLog(TAG, "fetchPositionValue(mDataLoggingArray, checkStatusOfChannelArray(groupConfigurationModel.getStrDataLoggingInterval(), false), false) : " +
                    checkStatusOfChannelArray(groupConfigurationModel.getStrDataLoggingInterval(), false));
            mPrf_timeout_spinner.setSelection(fetchPositionValue(mPrfTimeOutArray, groupConfigurationModel.getStrPrfTimeOut(), false));
            mTx_power_spinner.setSelection(fetchPositionValue(mTxPowerArray, groupConfigurationModel.getStrTxPower(), false));
            mLogin_Interval_Spinner.setSelection(fetchPositionValue(mDataLoggingArray, checkStatusOfChannelArray(groupConfigurationModel.getStrDataLoggingInterval(), false), false));
            mChannel_37_Spinner.setSelection(fetchPositionValue(mChannel37SpinnerArray, checkStatusOfChannelArray(groupConfigurationModel.getStrChannel37(), true), true));
            mChannel_38_Spinner.setSelection(fetchPositionValue(mChannel38SpinnerArray, checkStatusOfChannelArray(groupConfigurationModel.getStrChannel38(), true), true));
            mChannel_39_Spinner.setSelection(fetchPositionValue(mChannel39SpinnerArray, checkStatusOfChannelArray(groupConfigurationModel.getStrChannel39(), true), true));
            mTamper_EditText.setText(groupConfigurationModel.getStrTamper());
            mPharmaModeSpinner.setSelection(fetchPositionValue(mPharmaModeSpinnerArray, checkStatusOfPharmaModeArray(groupConfigurationModel.getStrPharmaModeValue(), true), true));
            mPharmaModeDelayValue_EditText.setText(groupConfigurationModel.getStrPharmaModeDelay());
        } else {
            AndroidAppUtils.showInfoLog(TAG, " Data cannot be fetched");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActivity != null) {
            AppApplication.getInstance().setCurrentActivityReference(mActivity);
        }

        setUpListener();
        manageBluetoothConnectionSetupListener();

    }

    /**
     * Method Name : checkStatusOfChannelArray
     * Description : This method is used for checking the status of channel
     * array and return the status in readable format
     *
     * @param strStatus
     * @return
     */
    public String checkStatusOfChannelArray(String strStatus, boolean boolIsChannelArray) {
        String strResultStatus = "";
        if (boolIsChannelArray) {
            if (strStatus.equalsIgnoreCase(AppHelper.NUMBER_ZERO)) {
                strResultStatus = mActivity.getResources().getString(R.string.strEnableCaption);
            } else {
                strResultStatus = mActivity.getResources().getString(R.string.strDisableCaption);
            }
        } else {
            if (strStatus.equalsIgnoreCase(AppHelper.NUMBER_ZERO)) {
                strResultStatus = mActivity.getResources().getString(R.string.strDisableCaption);
            } else {
                strResultStatus = strStatus;
            }
        }
        return strResultStatus;
    }

    /**
     * Method Name : checkStatusOfPharmaModeArray
     * Description : This method is used for checking the status of pharma mode
     * array and return the status in readable format
     *
     * @param strStatus
     * @return
     */
    public String checkStatusOfPharmaModeArray(String strStatus, boolean boolIsPharmaModeArray) {
        String strResultStatus = "";
        if (boolIsPharmaModeArray) {
            if (strStatus.equalsIgnoreCase(AppHelper.NUMBER_ZERO)) {
                strResultStatus = mActivity.getResources().getString(R.string.strDisableCaption);
            } else {
                strResultStatus = mActivity.getResources().getString(R.string.strEnableCaption);
            }
        } else {
            if (strStatus.equalsIgnoreCase(AppHelper.NUMBER_ZERO)) {
                strResultStatus = mActivity.getResources().getString(R.string.strDisableCaption);
            } else {
                strResultStatus = strStatus;
            }
        }
        return strResultStatus;
    }

    /**
     * Method Name : fetchPositionValue
     * Description : this method is used for retrieving the
     * position of value in corresponding arrays
     *
     * @param strArray
     * @param strFetchedValue
     * @param boolISChannelArray
     * @return
     */
    private int fetchPositionValue(String strArray[], String strFetchedValue, boolean boolISChannelArray) {
        int strMatchedPosition = 0;
        if (strArray != null && strArray.length > 0 && !strFetchedValue.isEmpty()) {
            for (int i = 0; i < strArray.length; i++) {
                if (boolISChannelArray) {
                    if (strFetchedValue.equalsIgnoreCase(strArray[i])) {
                        strMatchedPosition = i;
                    }
                } else if (strFetchedValue.equalsIgnoreCase(mActivity.getResources().getString(R.string.strDisableCaption)) &&
                        strFetchedValue.equalsIgnoreCase(strArray[i].toString().trim())) {
                    strMatchedPosition = i;
                } else if (
                        strFetchedValue.equalsIgnoreCase(strArray[i].toString().trim().replaceAll("[^\\d.]", ""))) {
                    strMatchedPosition = i;
                }

            }
        }
        return strMatchedPosition;
    }

    /**
     * Method Name : setUpListener
     * Description : This method is used setting the listener for listening the changes
     * that has been made on ble device
     */
    private void setUpListener() {
        mDataLoggingListener = new DataLoggingListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void OnNotificationReceived(byte[] byteData) {
                byte[] byteDataReceived = new byte[byteData.length];
                AndroidAppUtils.showInfoLog(TAG, "byteData : " + byteData +
                        "\n converted string data : " + AndroidAppUtils.convertToHexString(byteDataReceived));

                if (!AndroidAppUtils.byteArrayCheckZero(byteData) && byteData.length > 19 && boolIsUserNoteRetrieveCommandSend) {

                    System.arraycopy(byteData, 0, byteDataReceived, 0, byteData.length);
                    try {
                        String normalString = new String(byteDataReceived, StandardCharsets.UTF_8);
                        AndroidAppUtils.showInfoLog(TAG,
                                " normalString : " + AndroidAppUtils.removeTrailingZeros(
                                        normalString));
                        normalString = !normalString.isEmpty() ? normalString.replaceAll("[^a-zA-Z0-9 ]", "") : "";
                        if (boolIsUserNoteRetrieveCommandSend) {
                            mUserNoteEdittext.setText(normalString);
                            boolIsUserNoteRetrieveCommandSend = false;
                            GlobalConstant.myCountDownTimer.cancel();
                            enableDisableNotification(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void OnDataLoggingWriteCharacteristicsDiscovered(String status) {
                if (boolIsClearDataCommandSend) {
                    AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strClearRequestSendSuccessMsg));
                    boolIsClearDataCommandSend = false;
                } else if (boolIsResetDeviceCommandSend) {
                    showMessageAccordingToOperation(strTurnOff);
                    GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = true;
                    boolIsResetDeviceCommandSend = false;
                } else if (boolIsUserNoteSendRequestGenerated) {
                    sendCommandForSavingUserNote(AppHelper.STRING_SEND_USER_DATA_TRANSFER);
                    boolIsUserNoteSendRequestGenerated = false;
                } else if (boolIsUserNoteSend) {
                    AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strUserNoteSaveSuccessMsg));
                    boolIsUserNoteSend = false;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mActivity != null) {
                                mActivity.finish();
                            }
                        }
                    },2000);

                } else if (boolIsCommandForResettingPharmaModeSend) {
                    AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strResetPharmaModeMsg));
                    boolIsCommandForResettingPharmaModeSend = false;
                } else if (boolIsUserNoteRetrieveCommandSend) {
                    enableDisableNotification(true);
                } else {
                    showMessageAccordingToOperation(strTurnOff);
                    GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = true;
                }
                AndroidAppUtils.hideProgressDialog();
            }

        };
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
     * Method Name : retrieveUserCommentForDevice
     * Description : This method is used for retrieving the configuration from the device
     */
    private void retrieveUserCommentForDevice() {
        AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.strRetrieveUserNote), false);
        final byte sendReadingToken[] = {(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x03};

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                GlobalConstant.myCountDownTimer.start();
                GlobalConstant.myCountDownTimer.stringGeneratedForActionOccurred(mActivity.getResources().getString(R.string.strFailToRetrieveMag));
                boolIsUserNoteRetrieveCommandSend = true;
                if (ConnectionControl.dl_write_characteristics != null)
                    DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, sendReadingToken);
            }
        }, 2000);
    }

    /**
     * Method Name : saveCurrentConfigurationData
     * Description : This method is used for saving the current configuartion changes
     */
    private void saveCurrentConfigurationData() {
        AndroidAppUtils.showInfoLog(TAG, "***************** saveCurrentConfigurationData ************** " + GlobalConstant.DEVICE_MAC);
        GroupConfigurationModel groupConfigurationModel = new GroupConfigurationModel();
        groupConfigurationModel.setStrPrfTimeOut(mPrfTimeOut);
        groupConfigurationModel.setStrTxPower(mTxPowerInt);
        groupConfigurationModel.setStrAls(mAls_EditText.getText().toString());
        groupConfigurationModel.setStrChannel37(channel_37_Int);
        groupConfigurationModel.setStrChannel38(channel_38_Int);
        groupConfigurationModel.setStrChannel39(channel_39_Int);
        groupConfigurationModel.setStrDataLoggingInterval(mLogin_Interval_Int);
        groupConfigurationModel.setStrTamper(mTamper_String);
        groupConfigurationModel.setStrPharmaModeDelay(mPharmaModeDelay_String);
        groupConfigurationModel.setStrPharmaModeValue(mPharmaMode_String);
        AppApplication.getInstance().saveConfigurationData(GlobalConstant.DEVICE_MAC, groupConfigurationModel);
        AndroidAppUtils.hideProgressDialog();
    }

    /**
     * Method Name : manageBluetoothConnectionSetupListener
     * Description : This method is used for setting the listener
     * for connection and read/write operation state
     */
    private void manageBluetoothConnectionSetupListener() {
        GlobalConstant.mBluetoothConnectionStateInterface = new BluetoothConnectionStateInterface() {
            @Override
            public void onGattConnected(Object... bluetoothData) {

            }

            @Override
            public void onGattConnecting(Object... bluetoothData) {

            }

            @Override
            public void onGattDisconnected(Object... bluetoothData) {
                AndroidAppUtils.hideProgressDialog();
                if (ConnectionControl.connectionControl != null) {
                    ConnectionControl.connectionControl.dismissDialog();
                }
                /**
                 * Retry for connection
                 */
                AndroidAppUtils.customAlertDialogWithGradiantBtn(mActivity, mActivity.getResources().getString(R.string.strWarningCaption), true, mActivity.getResources().getString(R.string.strConnectionBrakeMessage),
                        true, mActivity.getResources().getString(R.string.strRetryCaption), true, new ChoiceDialogClickListener() {
                            @Override
                            public void onClickOfPositive() {
                                GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = false;
                                AndroidAppUtils.showLog(TAG, " Device Mac Address : " + GlobalConstant.DEVICE_MAC);
                                BluetoothDevice bluetoothDevice = GlobalConstant.mBluetoothAdapter.getRemoteDevice(GlobalConstant.DEVICE_MAC);
                                DeviceAdapter.mConnectionControl = new ConnectionControl(ConnectionControl.connectionControl.mActivity, bluetoothDevice, mActivity);
                                /**
                                 * Setting Value to default for reading data in case of disconnection
                                 */
                                GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
                            }

                            @Override
                            public void onClickOfNegative() {
                                GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = false;
//                                            mActivity.finish();
                                if (DeviceAdapter.mConnectionControl != null)
                                    DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                            }
                        }, true);

            }

            @Override
            public void onGattServiceDiscovery(Object... bluetoothData) {
                AndroidAppUtils.showInfoLog(TAG, " *********** onGattServiceDiscovery ***********");
                sendChangeConfigurationDataToDevice();
            }

            @Override
            public void onGattServiceRead(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceWrite(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceWriteStatus(boolean status) {

            }

            @Override
            public void onGattServiceDataAvailable(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceReadNotPermitted(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceWriteNotPermitted(Object... bluetoothData) {

            }

            @Override
            public void onGattStartReadingResponse(Object... bluetoothData) {

            }
        };
    }

    /*******************************************************************
     * Function name - manageHeaderView
     * Description - manage the initialization, visibility and click
     * listener of view fields on Header view
     *******************************************************************/
    public void manageHeaderView() {
        HeaderViewManager.getInstance().InitializeMultiTitleHeaderView(mActivity, null, false, manageHeaderClick());
        HeaderViewManager.getInstance().setHeading(true, mActivity.getResources().getString(R.string.strConfigurationCaption), mActivity);
        HeaderViewManager.getInstance().setSubHeading(false, "");
        HeaderViewManager.getInstance().setLeftSideHeaderView(true, false, R.drawable.back, "");
        HeaderViewManager.getInstance().setRightSideHeaderView(true, false, R.drawable.ic_power_settings_new_black_24dp, "");
        HeaderViewManager.getInstance().setProgressLoader(false, true);
    }

    /*****************************************************************************
     * Function name - manageHeaderClick
     * Description - manage the click on the left and right image view of header
     *****************************************************************************/
    private HeaderViewClickListener manageHeaderClick() {
        HeaderViewClickListener headerViewClickListener = new HeaderViewClickListener() {
            @Override
            public void onClickOfHeaderLeftView() {
                if (mDataLoggingListener != null) {
                    mDataLoggingListener = null;
                }
                onBackPressed();
            }

            @Override
            public void onClickOfHeaderRightView() {
                /**
                 * Turned On Condition
                 */
                if (!boolISTurnedOff) {
                    strTurnOff = AppHelper.ZERO_TWO;
                    GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = false;
                    HeaderViewManager.getInstance().changeRightImageViewColor(mActivity.getResources().getColor(R.color.green_turn_on));
                    boolISTurnedOff = true;
                }
                /**
                 * Turned off condition
                 */
                else {
                    strTurnOff = AppHelper.ZERO_ONE;
                    GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = true;
                    HeaderViewManager.getInstance().changeRightImageViewColor(mActivity.getResources().getColor(R.color.red));
                    boolISTurnedOff = false;
                }
                AndroidAppUtils.showInfoLog(TAG, " strTurnOff : " + strTurnOff);
            }
        };
        return headerViewClickListener;
    }


    /**
     * Method Name : verifyPharmaModeDelayData
     * Description : This method is used for verifying the delay data
     *
     * @param mPharmaModeDelay_string
     * @return
     */
    private byte[] verifyPharmaModeDelayData(String mPharmaModeDelay_string) {
        byte[] bytePharmaModeDelayValue = new byte[2];
        if (!TextUtils.isEmpty(mPharmaModeDelay_string)) {
            int intPharmaModeDelayValueLength = mPharmaModeDelay_string.length();
            int intPharmaModeDelayValue = mPharmaModeDelay_string.length();
            if (intPharmaModeDelayValue >= 1 && intPharmaModeDelayValue <= 1440) {
                if (intPharmaModeDelayValueLength <= 2) {
                    bytePharmaModeDelayValue = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(mPharmaModeDelay_string));
                } else if (intPharmaModeDelayValueLength > 2 && intPharmaModeDelayValueLength <= 4) {
                    bytePharmaModeDelayValue = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(mPharmaModeDelay_string));
                }
            } else {
                AndroidAppUtils.showWarningLog(TAG, " Invalid Input");
            }
        }
        return bytePharmaModeDelayValue;
    }

    /**
     * Method Name : spinnerValueChange
     * Description : This method is used for fetching value based on selection done in spinner
     *
     * @param value
     * @return
     */
    private String spinnerValueChange(String value) {
        if (value.equalsIgnoreCase(mActivity.getResources().getString(R.string.strDisableCaption)))
            return AppHelper.NUMBER_ONE;
        else
            return AppHelper.NUMBER_ZERO;
    }

    /**
     * Method Name : spinnerPharmaModeValueChange
     * Description : This method is used for fetching value based on selection done in spinner
     *
     * @param value
     * @return
     */
    private String spinnerPharmaModeValueChange(String value) {
        if (value.equalsIgnoreCase(mActivity.getResources().getString(R.string.strDisableCaption)))
            return AppHelper.NUMBER_ZERO;
        else
            return AppHelper.NUMBER_ONE;
    }


    /**
     * Method Name : initView
     * Description : This method is used for initializing the view component
     */
    @SuppressLint("CutPasteId")
    private void initView() {
        mActivity = ChangeConfigurationActivity.this;
        GlobalConstant.mGlobalActivityArrayList.add(mActivity);
        GlobalConstant.myCountDownTimer = new MyCountDownTimer(mActivity, 5000, 1000,
                new SnackBarActionButtonListener() {
                    @Override
                    public void onClickOfSnackBarActionButtonView() {
                        boolIsUserNoteRetrieveCommandSend = false;
                        enableDisableNotification(false);
                    }
                }
        );
//        AppApplication.getInstance().setCurrentActivityReference(mActivity);
        mPrfTimeOutArray = mActivity.getResources().getStringArray(R.array.prf_time_out_array);
        mTxPowerArray = mActivity.getResources().getStringArray(R.array.tx_power_array);
        mChannel37SpinnerArray = mActivity.getResources().getStringArray(R.array.channel_array);
        mChannel38SpinnerArray = mActivity.getResources().getStringArray(R.array.channel_array);
        mChannel39SpinnerArray = mActivity.getResources().getStringArray(R.array.channel_array);
        mPharmaModeSpinnerArray = mActivity.getResources().getStringArray(R.array.channel_array);
        mDataLoggingArray = mActivity.getResources().getStringArray(R.array.login_interval_array);
        mAls_EditText = (EditText) findViewById(R.id.als_edittext);
        mTamper_EditText = (EditText) findViewById(R.id.temper_edittext);
        mUserNoteEdittext = (EditText) findViewById(R.id.user_note_edittext);
        mChannel_37_Spinner = (Spinner) findViewById(R.id.channel_37_spinner);
        mChannel_38_Spinner = (Spinner) findViewById(R.id.channel_38_spinner);
        mChannel_39_Spinner = (Spinner) findViewById(R.id.channel_39_spinner);
        mLogin_Interval_Spinner = (Spinner) findViewById(R.id.login_interval_spinner);
        mTx_power_spinner = (Spinner) findViewById(R.id.tx_power_spinner);
        mPrf_timeout_spinner = (Spinner) findViewById(R.id.prf_timeout_spinner);
        mSend_Button = (Button) findViewById(R.id.send_button);
        btnClearData = (Button) findViewById(R.id.btnClearData);
        btnResetDevice = (Button) findViewById(R.id.btnResetDevice);
        btnResetPharmaMode = (Button) findViewById(R.id.reset_pharma_button);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnClearData.setOnClickListener(this);
        mSend_Button.setOnClickListener(this);
        btnResetDevice.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnResetPharmaMode.setOnClickListener(this);
        /**
         * B4 Pharma Mode Fields
         */
        lLPharmaModeLayout = (LinearLayout) findViewById(R.id.lLPharmaModeLayout);
        mPharmaModeDelayValue_EditText = (EditText) findViewById(R.id.mPharmaModeDelayValue);
        mPharmaModeSpinner = (Spinner) findViewById(R.id.mPharmaModeSpinner);
        mPharmaModeSpinner.setSelection(1);
        if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B4_CONNECTED)) {
            lLPharmaModeLayout.setVisibility(View.VISIBLE);
        } else {
            lLPharmaModeLayout.setVisibility(View.GONE);
        }

        mUserNoteEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    AndroidAppUtils.showInfoLog(TAG, "Enter pressed");
                    AndroidAppUtils.hideKeyboard(mActivity, mUserNoteEdittext);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button:
                sendChangeConfigurationDataToDevice();
                logPrint();
                break;
            case R.id.btnClearData:

                sendCommandForClearData();
                break;
            case R.id.btnResetDevice:
                String strMessage = mActivity.getResources().getString(R.string.strFactoryResetDeviceMessage);
                AndroidAppUtils.customAlertDialogWithGradiantBtn(mActivity, mActivity.getResources().getString(R.string.strWarningCaption), true, strMessage,
                        true, mActivity.getResources().getString(R.string.strCaptionOK), true, new ChoiceDialogClickListener() {
                            @Override
                            public void onClickOfPositive() {
                                sendCommandForResettingDevice();
                            }

                            @Override
                            public void onClickOfNegative() {
                            }
                        }, true);

                break;
            case R.id.btnSave:
//                sendCommandForSavingUserNote(AppHelper.STRING_INITIATE_USER_DATA_TRANSFER);
                AndroidAppUtils.hideKeyboard(mActivity, btnSave);
                sendCommandForSavingUserNote(AppHelper.STRING_SEND_USER_DATA_TRANSFER);
                break;
            case R.id.reset_pharma_button:
                sendResetPharmaCommand();
                break;
        }
    }

    /**
     * Method Name : logPrint
     * Description : This method is used for logging the input made byt he user on click of submit
     */
    private void logPrint() {
        AndroidAppUtils.showLog("<<<< prf Data ", mPrfTimeOut);
        AndroidAppUtils.showLog("<<<< tx power Data ", mTxPowerInt);
        AndroidAppUtils.showLog("<<<< Als Data ", mAls_String);
        AndroidAppUtils.showLog("<<<< Temper Data ", mTamper_String);
        AndroidAppUtils.showLog("<<<< channel 37 Data ", channel_37_Int);
        AndroidAppUtils.showLog("<<<< channel 38 Data ", channel_38_Int);
        AndroidAppUtils.showLog("<<<< channel 39 Data ", channel_39_Int);
        AndroidAppUtils.showLog("<<<< logIn Data ", mLogin_Interval_Int);
        AndroidAppUtils.showLog("<<<< pharma mode ", mPharmaMode_String);
        AndroidAppUtils.showLog("<<<< pharma mode delay ", mPharmaModeDelay_String);
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

    /**
     * Method Name : showMessageAccordingToOperation
     * Description : This method is used for showing snack bar corresponding to event happened
     *
     * @param strOperationCode
     */
    public void showMessageAccordingToOperation(String strOperationCode) {
        switch (strOperationCode) {
            case AppHelper.ZERO_ONE:
                AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strDeviceTurnedOffSuccessfullyCaption));
                if (GlobalConstant.CONNECTED_STATE) {
                    GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
                    if (BluetoothLeService.getInstance() != null) {
                        BluetoothLeService.getInstance().disconnect();

                    } else {
                        AndroidAppUtils.showLog(TAG, "BluetoothLeService.getInstance() is null");
                    }
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (DeviceAdapter.mConnectionControl != null) {
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                        }
                    }
                }, 3000);
                break;
            case AppHelper.ZERO_TWO:
                saveCurrentConfigurationData();
                AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strConfigurationChangedSuccessfullyCaption));
                break;
            case AppHelper.ZERO_FIVE:
                AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strResetDeviceRequestSendSuccessMsg));
                if (GlobalConstant.CONNECTED_STATE) {
                    GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
                    if (BluetoothLeService.getInstance() != null) {
                        BluetoothLeService.getInstance().disconnect();

                    } else {
                        AndroidAppUtils.showLog(TAG, "BluetoothLeService.getInstance() is null");
                    }
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (DeviceAdapter.mConnectionControl != null) {
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                        }
                    }
                }, 3000);
                break;
        }

    }

    /**
     * Method Name : sendChangeConfigurationDataToDevice
     * Description : this method is used for sending the configuration bits to device
     */
    private void sendChangeConfigurationDataToDevice() {
        mAls_String = mAls_EditText.getText().toString().trim().replaceAll("[^\\d.]", "");
        mTamper_String = mTamper_EditText.getText().toString().trim().replaceAll("[^\\d.]", "");
        mPharmaModeDelay_String = mPharmaModeDelayValue_EditText.getText().toString().trim().replaceAll("[^\\d.]", "");
        AndroidAppUtils.showLog(TAG, "(mChannel_37_Spinner.getSelectedItem() : " + mChannel_37_Spinner.getItemAtPosition(mChannel_37_Spinner.getSelectedItemPosition()) +
                "\nmChannel_38_Spinner.getSelectedItem() " + mChannel_38_Spinner.getItemAtPosition(mChannel_38_Spinner.getSelectedItemPosition()) +
                "\nmChannel_39_Spinner.getSelectedItem() : " + mChannel_39_Spinner.getItemAtPosition(mChannel_39_Spinner.getSelectedItemPosition()) +
                "\n mPharmaModeDelay_String : " + mPharmaModeDelay_String);
        channel_37_Int = spinnerValueChange(mChannel_37_Spinner.getItemAtPosition(mChannel_37_Spinner.getSelectedItemPosition()).toString().trim());
        channel_38_Int = spinnerValueChange(mChannel_38_Spinner.getItemAtPosition(mChannel_38_Spinner.getSelectedItemPosition()).toString().trim());
        channel_39_Int = spinnerValueChange(mChannel_39_Spinner.getItemAtPosition(mChannel_39_Spinner.getSelectedItemPosition()).toString().trim());
        mPharmaMode_String = spinnerPharmaModeValueChange(mPharmaModeSpinner.getItemAtPosition(mPharmaModeSpinner.getSelectedItemPosition()).toString().trim());
        if (mLogin_Interval_Spinner.getSelectedItem().toString().trim().equalsIgnoreCase(mActivity.getResources().getString(R.string.strDisableCaption))) {
            mLogin_Interval_Int = AppHelper.NUMBER_ZERO;
        } else {
            mLogin_Interval_Int = mLogin_Interval_Spinner.getSelectedItem().toString().trim().replaceAll("[^\\d.]", "");
        }
        mTxPowerInt = mTx_power_spinner.getSelectedItem().toString().trim().replaceAll("[^\\d.]", "");
        mPrfTimeOut = mPrf_timeout_spinner.getSelectedItem().toString().trim().replaceAll("[^\\d.]", "");
        byte[] mBytePharmaModeValue = verifyPharmaModeDelayData(AndroidAppUtils.convertInHex(mPharmaModeDelay_String.isEmpty() ? AppHelper.NUMBER_ZERO : mPharmaModeDelay_String));
        String mData = mPrfTimeOut + mTxPowerInt +
               /* mAls_String + */mTamper_String +
                channel_37_Int + channel_38_Int
                + channel_39_Int + mLogin_Interval_Int + mPharmaMode_String;
        /**
         * Given arbitrary file id and record id for turning off the device
         */
        String strFileId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE;
        String strRecordId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE;
        byte[] etByteFileID = AndroidAppUtils.verifyForMoreThanTwoDigit(strFileId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE : AndroidAppUtils.appendNoOfZeroIfRequired(strFileId));
        byte[] etByteRecordID = AndroidAppUtils.verifyForMoreThanTwoDigit(strRecordId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE : AndroidAppUtils.appendNoOfZeroIfRequired(strRecordId));

        byte[] dataToWritten = {(byte) 0x00,
                (byte) 0x00,
//                (byte) (Integer.parseInt(mAls_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mAls_String)) & 0xFF),
                (byte) (Integer.parseInt(mTamper_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mTamper_String)) & 0xFF),
                (byte) (Integer.parseInt(channel_37_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_37_Int)) & 0xFF),
                (byte) (Integer.parseInt(channel_38_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_38_Int)) & 0xFF),
                (byte) (Integer.parseInt(channel_39_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_39_Int)) & 0xFF),
                etByteFileID[0],
                etByteFileID[1],
                etByteRecordID[0],
                etByteRecordID[1],
                (byte) (Integer.parseInt(mLogin_Interval_Int.isEmpty() ? "15" : AndroidAppUtils.appendNoOfZeroIfRequired(mLogin_Interval_Int)) & 0xFF),
                (byte) (Integer.parseInt(strTurnOff.isEmpty() ? AppHelper.ZERO_TWO : AndroidAppUtils.appendNoOfZeroIfRequired(strTurnOff)) & 0xFF),
                mBytePharmaModeValue[0],
                mBytePharmaModeValue[1],
                (byte) (Integer.parseInt(mPharmaMode_String.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(mPharmaMode_String)) & 0xFF),
                (byte) (Integer.parseInt(mPrfTimeOut.isEmpty() ? "0a" : AndroidAppUtils.appendNoOfZeroIfRequired(mPrfTimeOut)) & 0xFF),
                (byte) (Integer.parseInt(mTxPowerInt.isEmpty() ? AppHelper.ZERO_FOUR : AndroidAppUtils.appendNoOfZeroIfRequired(mTxPowerInt)) & 0xFF)
        };
        mByteDataNeedToWritten = dataToWritten;
        AndroidAppUtils.showLog(TAG, "DATA ENTERED : " + mData + "   ****** dataToWritten : " + dataToWritten +
                "Hex data : " + AndroidAppUtils.convertToHexString(dataToWritten));
        mTamper_EditText.clearFocus();
        GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
        if (GlobalConstant.CONNECTED_STATE) {
            AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.strChangingConfigurationLoading), false);
            if (ConnectionControl.dl_write_characteristics != null) {
                boolIsUserNoteRetrieveCommandSend = false;
                DeviceAdapter.mConnectionControl.writeFirstByteToDevice(ConnectionControl.dl_write_characteristics, dataToWritten, mActivity);
            } else {
                AndroidAppUtils.showErrorLog(TAG, "ConnectionControl.dl_write_characteristics is null");
            }
        } else {
            AndroidAppUtils.showLog(TAG, "Need to connect to device");
            BluetoothDevice bluetoothDevice = GlobalConstant.mBluetoothAdapter != null ? GlobalConstant.mBluetoothAdapter.getRemoteDevice(GlobalConstant.DEVICE_MAC) : null;
            AndroidAppUtils.hideProgressDialog();
            DeviceAdapter.mConnectionControl = new ConnectionControl(ConnectionControl.connectionControl.mActivity, bluetoothDevice, mActivity);
        }


    }

    /**
     * Method Name : sendCommandForClearData
     * Description : This method is used for sending command to device for clear data as well
     * as configuration on device
     */
    private void sendCommandForClearData() {
        try {
            String strClearData = AppHelper.ZERO_FOUR;
            /**
             * Given arbitrary file id and record id for turning off the device
             */
            String strFileId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE;
            String strRecordId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE;
            byte[] etByteFileID = AndroidAppUtils.verifyForMoreThanTwoDigit(strFileId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE : AndroidAppUtils.appendNoOfZeroIfRequired(strFileId));
            byte[] etByteRecordID = AndroidAppUtils.verifyForMoreThanTwoDigit(strRecordId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE : AndroidAppUtils.appendNoOfZeroIfRequired(strRecordId));
            final byte[] byteClearData = {(byte) (Integer.parseInt(mPrfTimeOut.isEmpty() ? "10" : AndroidAppUtils.appendNoOfZeroIfRequired(mPrfTimeOut)) & 0xFF),
                    (byte) (Integer.parseInt(mTxPowerInt.isEmpty() ? AppHelper.ZERO_FOUR : AndroidAppUtils.appendNoOfZeroIfRequired(mTxPowerInt)) & 0xFF),
                    (byte) (Integer.parseInt(mTamper_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mTamper_String)) & 0xFF),
                    (byte) (Integer.parseInt(channel_37_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_37_Int)) & 0xFF),
                    (byte) (Integer.parseInt(channel_38_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_38_Int)) & 0xFF),
                    (byte) (Integer.parseInt(channel_39_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_39_Int)) & 0xFF),
                    etByteFileID[0],
                    etByteFileID[1],
                    etByteRecordID[0],
                    etByteRecordID[1],
                    (byte) (Integer.parseInt(mLogin_Interval_Int.isEmpty() ? "15" : AndroidAppUtils.appendNoOfZeroIfRequired(mLogin_Interval_Int)) & 0xFF),
                    (byte) (Integer.parseInt(strTurnOff.isEmpty() ? AppHelper.ZERO_FOUR : AndroidAppUtils.appendNoOfZeroIfRequired(strClearData)) & 0xFF)
            };
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
        } catch (Exception e)
        {
            AndroidAppUtils.showInfoLog(TAG, "error message " + e.getMessage());
        }

    }

    /**
     * Method Name : sendCommandForResettingDevice
     * Description : This method is used for sending command to device for clear data that is stored
     * and set configuration to default making device as factory reset
     */
    private void sendCommandForResettingDevice() {
        try {
            String strResetDevice = AppHelper.ZERO_FIVE;
            /**
             * Given arbitrary file id and record id for resetting the device
             */
            String strFileId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE;
            String strRecordId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE;
            byte[] etByteFileID = AndroidAppUtils.verifyForMoreThanTwoDigit(strFileId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE : AndroidAppUtils.appendNoOfZeroIfRequired(strFileId));
            byte[] etByteRecordID = AndroidAppUtils.verifyForMoreThanTwoDigit(strRecordId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE : AndroidAppUtils.appendNoOfZeroIfRequired(strRecordId));
            final byte[] byteClearData = {(byte) (Integer.parseInt(mPrfTimeOut.isEmpty() ? "10" : AndroidAppUtils.appendNoOfZeroIfRequired(mPrfTimeOut)) & 0xFF),
                    (byte) (Integer.parseInt(mTxPowerInt.isEmpty() ? AppHelper.ZERO_FOUR : AndroidAppUtils.appendNoOfZeroIfRequired(mTxPowerInt)) & 0xFF),
                    (byte) (Integer.parseInt(mTamper_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mTamper_String)) & 0xFF),
                    (byte) (Integer.parseInt(channel_37_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_37_Int)) & 0xFF),
                    (byte) (Integer.parseInt(channel_38_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_38_Int)) & 0xFF),
                    (byte) (Integer.parseInt(channel_39_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_39_Int)) & 0xFF),
                    etByteFileID[0],
                    etByteFileID[1],
                    etByteRecordID[0],
                    etByteRecordID[1],
                    (byte) (Integer.parseInt(mLogin_Interval_Int.isEmpty() ? "15" : AndroidAppUtils.appendNoOfZeroIfRequired(mLogin_Interval_Int)) & 0xFF),
                    (byte) (Integer.parseInt(strTurnOff.isEmpty() ? AppHelper.ZERO_FIVE : AndroidAppUtils.appendNoOfZeroIfRequired(strResetDevice)) & 0xFF)
            };
            if (DeviceAdapter.mConnectionControl != null && ConnectionControl.connectionControl != null) {
                AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.please_wait), false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        strTurnOff = AppHelper.ZERO_FIVE;
                        boolIsResetDeviceCommandSend = true;
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
     * Method Name : sendCommandForSavingUserNote
     * Description : This method is used for sending command to device for saving the user notes to the device
     * Info like mac address, name,etc
     *
     * @param stringInitiateUserDataTransfer
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void sendCommandForSavingUserNote(String stringInitiateUserDataTransfer) {
        try {
            int intDataSize = 12;
            byte[] byteDataToWritten = new byte[intDataSize];
            if (!stringInitiateUserDataTransfer.isEmpty()
                    && stringInitiateUserDataTransfer.equalsIgnoreCase(AppHelper.STRING_INITIATE_USER_DATA_TRANSFER)) {
                intDataSize = 12;
                byteDataToWritten = new byte[intDataSize];
                String strUserNoteData = AppHelper.ZERO_FOUR;
                /**
                 * Given arbitrary file id and record id for initiating transfer of user test data to the device
                 */
                String strFileId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_TWO;
                String strRecordId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_TWO;
                byte[] etByteFileID = AndroidAppUtils.verifyForMoreThanTwoDigit(strFileId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE : AndroidAppUtils.appendNoOfZeroIfRequired(strFileId));
                byte[] etByteRecordID = AndroidAppUtils.verifyForMoreThanTwoDigit(strRecordId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE : AndroidAppUtils.appendNoOfZeroIfRequired(strRecordId));
                final byte[] byteClearData = {(byte) (Integer.parseInt(mPrfTimeOut.isEmpty() ? "10" : AndroidAppUtils.appendNoOfZeroIfRequired(mPrfTimeOut)) & 0xFF),
                        (byte) (Integer.parseInt(mTxPowerInt.isEmpty() ? AppHelper.ZERO_FOUR : AndroidAppUtils.appendNoOfZeroIfRequired(mTxPowerInt)) & 0xFF),
                        (byte) (Integer.parseInt(mTamper_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mTamper_String)) & 0xFF),
                        (byte) (Integer.parseInt(channel_37_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_37_Int)) & 0xFF),
                        (byte) (Integer.parseInt(channel_38_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_38_Int)) & 0xFF),
                        (byte) (Integer.parseInt(channel_39_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_39_Int)) & 0xFF),
                        etByteFileID[0],
                        etByteFileID[1],
                        etByteRecordID[0],
                        etByteRecordID[1],
                        (byte) (Integer.parseInt(mLogin_Interval_Int.isEmpty() ? "15" : AndroidAppUtils.appendNoOfZeroIfRequired(mLogin_Interval_Int)) & 0xFF),
                        (byte) (Integer.parseInt(strTurnOff.isEmpty() ? AppHelper.ZERO_THREE : AndroidAppUtils.appendNoOfZeroIfRequired(strUserNoteData)) & 0xFF)
                };
                System.arraycopy(byteClearData, 0, byteDataToWritten, 0, byteClearData.length);
                boolIsUserNoteSendRequestGenerated = true;
            } else if (stringInitiateUserDataTransfer.equalsIgnoreCase(AppHelper.STRING_SEND_USER_DATA_TRANSFER)) {
                intDataSize = 20;
                byteDataToWritten = new byte[intDataSize];
                String strUserNoteData = mUserNoteEdittext.getText().toString().trim();
                if (TextUtils.isEmpty(strUserNoteData)) {
                    mUserNoteEdittext.setError("Please enter text");
                } else {
                    int stringLength = strUserNoteData.toString().trim().length();
                    int intNoOfZeroRequired = 21 - stringLength;
                    StringBuffer sb = new StringBuffer();
                    sb.append(strUserNoteData);
                    strUserNoteData = sb.toString();
                    AndroidAppUtils.showInfoLog(TAG, "strUserNoteData : " + strUserNoteData
                            + "\n stringLength : " + stringLength +
                            "\n intNoOfZeroRequired :  " + intNoOfZeroRequired);
                    final byte[] byteClearData = strUserNoteData.getBytes(StandardCharsets.UTF_8); /*Base64.decode(strUserNoteData, Base64.DEFAULT);*/
                    int intNeedToCopy = stringLength - 11;
                    if (stringLength > 11) {
                        System.arraycopy(byteClearData, 0, byteDataToWritten, 0, 11);
                        byteDataToWritten[11] = 0x06;
                        System.arraycopy(byteClearData, 11, byteDataToWritten, 12, intNeedToCopy);
                    } else {
                        System.arraycopy(byteClearData, 0, byteDataToWritten, 0, stringLength);
                        byteDataToWritten[11] = 0x06;
                    }

                    AndroidAppUtils.showInfoLog(TAG, "byteClearData : " + byteClearData +
                            "\n converted byteClearData : " + AndroidAppUtils.convertToHexString(byteClearData) +
                            "\nByte data : " + byteDataToWritten +
                            "\n hex data : " + AndroidAppUtils.convertToHexString(byteDataToWritten)

                    );
                    boolIsUserNoteSend = true;

                    if (DeviceAdapter.mConnectionControl != null && ConnectionControl.connectionControl != null) {
                        AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.please_wait), false);
                        final byte[] finalByteDataToWritten = byteDataToWritten;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (DeviceAdapter.mConnectionControl != null)
                                    DeviceAdapter.mConnectionControl.writeFileToDevice
                                            (ConnectionControl.dl_notification_characteristics, finalByteDataToWritten);
                            }
                        }, 1000);

                    } else {
                        AndroidAppUtils.showErrorLog(TAG, "Either ConnectionControl.connectionControl or DeviceAdapter.mConnectionControl is null");
                    }
                }
            }
        } catch (Exception e) {
            AndroidAppUtils.showInfoLog(TAG, "error message " + e.getMessage());
        }

    }

    /**
     * Method Name : sendResetPharmaCommand
     * Description : This method is used for sending command to device for saving the user notes to the device
     * Info like mac address, name,etc
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void sendResetPharmaCommand() {
        try {
            mPharmaMode_String = spinnerPharmaModeValueChange(mPharmaModeSpinner.getItemAtPosition(mPharmaModeSpinner.getSelectedItemPosition()).toString().trim());
            if (mLogin_Interval_Spinner.getSelectedItem().toString().trim().equalsIgnoreCase(mActivity.getResources().getString(R.string.strDisableCaption))) {
                mLogin_Interval_Int = AppHelper.NUMBER_ZERO;
            } else {
                mLogin_Interval_Int = mLogin_Interval_Spinner.getSelectedItem().toString().trim().replaceAll("[^\\d.]", "");
            }
            mTxPowerInt = mTx_power_spinner.getSelectedItem().toString().trim().replaceAll("[^\\d.]", "");
            mPrfTimeOut = mPrf_timeout_spinner.getSelectedItem().toString().trim().replaceAll("[^\\d.]", "");
            byte[] mBytePharmaModeValue = verifyPharmaModeDelayData(AndroidAppUtils.convertInHex(mPharmaModeDelay_String.isEmpty() ? AppHelper.NUMBER_ZERO : mPharmaModeDelay_String));
            String mData = mPrfTimeOut + mTxPowerInt +
               /* mAls_String + */mTamper_String +
                    channel_37_Int + channel_38_Int
                    + channel_39_Int + mLogin_Interval_Int + mPharmaMode_String;
            /**
             * Given arbitrary file id and record id for initiating transfer of user test data to the device
             */
            String strFileId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FOUR;
            String strRecordId = AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE;
            strTurnOff = AppHelper.ZERO_SEVEN;
            byte[] etByteFileID = AndroidAppUtils.verifyForMoreThanTwoDigit(strFileId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_FIVE : AndroidAppUtils.appendNoOfZeroIfRequired(strFileId));
            byte[] etByteRecordID = AndroidAppUtils.verifyForMoreThanTwoDigit(strRecordId.isEmpty() ? AppHelper.DOUBLE_ZERO + AppHelper.ZERO_ONE : AndroidAppUtils.appendNoOfZeroIfRequired(strRecordId));
            byte[] byteDataToWritten = {(byte) 0x00,
                    (byte) 0x00,
//                (byte) (Integer.parseInt(mAls_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mAls_String)) & 0xFF),
                    (byte) (Integer.parseInt(mTamper_String.isEmpty() ? "20" : AndroidAppUtils.appendNoOfZeroIfRequired(mTamper_String)) & 0xFF),
                    (byte) (Integer.parseInt(channel_37_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_37_Int)) & 0xFF),
                    (byte) (Integer.parseInt(channel_38_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_38_Int)) & 0xFF),
                    (byte) (Integer.parseInt(channel_39_Int.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(channel_39_Int)) & 0xFF),
                    etByteFileID[0],
                    etByteFileID[1],
                    etByteRecordID[0],
                    etByteRecordID[1],
                    (byte) (Integer.parseInt(mLogin_Interval_Int.isEmpty() ? "15" : AndroidAppUtils.appendNoOfZeroIfRequired(mLogin_Interval_Int)) & 0xFF),
                    (byte) (Integer.parseInt(strTurnOff.isEmpty() ? AppHelper.ZERO_TWO : AndroidAppUtils.appendNoOfZeroIfRequired(strTurnOff)) & 0xFF),
                    mBytePharmaModeValue[0],
                    mBytePharmaModeValue[1],
                    (byte) (Integer.parseInt(mPharmaMode_String.isEmpty() ? AppHelper.DOUBLE_ZERO : AndroidAppUtils.appendNoOfZeroIfRequired(mPharmaMode_String)) & 0xFF),
                    (byte) (Integer.parseInt(mPrfTimeOut.isEmpty() ? "0a" : AndroidAppUtils.appendNoOfZeroIfRequired(mPrfTimeOut)) & 0xFF),
                    (byte) (Integer.parseInt(mTxPowerInt.isEmpty() ? AppHelper.ZERO_FOUR : AndroidAppUtils.appendNoOfZeroIfRequired(mTxPowerInt)) & 0xFF)
            };
            if (DeviceAdapter.mConnectionControl != null && ConnectionControl.connectionControl != null) {
                AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.please_wait), false);
                final byte[] finalByteDataToWritten = byteDataToWritten;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolIsCommandForResettingPharmaModeSend = true;
                        if (DeviceAdapter.mConnectionControl != null)
                            DeviceAdapter.mConnectionControl.writeFileToDevice
                                    (ConnectionControl.dl_notification_characteristics, finalByteDataToWritten);
                    }
                }, 1000);

            } else {
                AndroidAppUtils.showErrorLog(TAG, "Either ConnectionControl.connectionControl or DeviceAdapter.mConnectionControl is null");
            }

        } catch (Exception e) {
            AndroidAppUtils.showInfoLog(TAG, "error message " + e.getMessage());
        }

    }


}
