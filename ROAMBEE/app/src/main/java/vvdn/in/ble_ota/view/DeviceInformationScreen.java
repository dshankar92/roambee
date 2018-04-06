package vvdn.in.ble_ota.view;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.widget.TextView;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.BuildConfig;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.blecontrols.ConnectionControl;
import vvdn.in.ble_ota.control.HeaderViewManager;
import vvdn.in.ble_ota.listener.DataLoggingListener;
import vvdn.in.ble_ota.listener.HeaderViewClickListener;

/**
 * Class Name : DeviceInformationScreen
 * Description : This class is used for displaying the device information
 */
public class DeviceInformationScreen extends Activity {
    /**
     * TextView reference object
     */
    private TextView mTvDeviceName, mTvDeviceMacAddress, mTvDeviceModelNumber, mTvDeviceFirmwareVersion,
            mTvAppVersion;

    private Activity mActivity;
    /**
     * Debuggable TAG
     */
    private String TAG = DeviceInformationScreen.class.getSimpleName();
    /**
     * Boolean reference object for handling the retrieve device information command
     */
    private boolean boolIsDeviceInformationRetrieveCommandSend = false;
    /**
     * DataLoggingListener reference object
     */
    public static DataLoggingListener mDataLoggingListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_information_layout);
        initView();
        manageHeaderView();
        retrieveDeviceInformation();
    }


    /**
     * Method Name : initView
     * Description : This method is used for initializing the view component
     */
    private void initView() {
        mActivity = DeviceInformationScreen.this;
        AppApplication.getInstance().setCurrentActivityReference(mActivity);
        GlobalConstant.mGlobalActivityArrayList.add(mActivity);
        mTvAppVersion = (TextView) findViewById(R.id.mTvAppVersion);
        mTvDeviceFirmwareVersion = (TextView) findViewById(R.id.mTvDeviceFirmwareVersion);
        mTvDeviceName = (TextView) findViewById(R.id.mTvDeviceName);
        mTvDeviceModelNumber = (TextView) findViewById(R.id.mTvDeviceModelNumber);
        mTvDeviceMacAddress = (TextView) findViewById(R.id.mTvDeviceMacAddress);
    }


    /*******************************************************************
     * Function name - manageHeaderView
     * Description - manage the initialization, visibility and click
     * listener of view fields on Header view
     *******************************************************************/
    public void manageHeaderView() {
        HeaderViewManager.getInstance().InitializeMultiTitleHeaderView(mActivity, null, false, manageHeaderClick());
        HeaderViewManager.getInstance().setHeading(true, mActivity.getResources().getString(R.string.str_device_info), mActivity);
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
    protected void onResume() {
        super.onResume();
        setUpListener();
    }


    /**
     * Method Name : retrieveDeviceInformation
     * Description : This method is used for retrieving the information like model number
     * and firmware version from the device
     */
    private void retrieveDeviceInformation() {
        AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.strRetrieveDeviceInfo), false);
        final byte sendReadingToken[] = {(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x04};

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                GlobalConstant.myCountDownTimer.start();
//                GlobalConstant.myCountDownTimer.stringGeneratedForActionOccurred(mActivity.getResources().getString(R.string.strFailToRetrieveMag));
                boolIsDeviceInformationRetrieveCommandSend = true;
                if (ConnectionControl.dl_write_characteristics != null)
                    DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, sendReadingToken);
            }
        }, 2000);
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
                byte[] byteDataModelNumber = new byte[3];
                byte[] byteDataFirmwareVersion = new byte[3];
                AndroidAppUtils.showInfoLog(TAG, "byteData : " + byteData +
                        "\n converted string data : " + AndroidAppUtils.convertToHexString(byteData));

                if (!AndroidAppUtils.byteArrayCheckZero(byteData) && byteData.length > 5) {

                    System.arraycopy(byteData, 0, byteDataModelNumber, 0, 3);
                    System.arraycopy(byteData, 3, byteDataFirmwareVersion, 0, 3);
                    try {
                        String normalModelString = AndroidAppUtils.convertToHexString(byteDataModelNumber);// new String(byteDataModelNumber, StandardCharsets.UTF_8);
                        String normalFirmwareString = AndroidAppUtils.convertToHexString(byteDataFirmwareVersion);// new String(byteDataFirmwareVersion, StandardCharsets.UTF_8);
                        AndroidAppUtils.showInfoLog(TAG,
                                " normalModelString : " + AndroidAppUtils.removeTrailingZeros(
                                        normalModelString) +
                                        "\n normalFirmwareString : " + AndroidAppUtils.removeTrailingZeros(normalFirmwareString));
                        normalModelString = !normalModelString.isEmpty() ? normalModelString.replaceAll("[^a-zA-Z0-9 ]", "") : "";
                        normalFirmwareString = !normalFirmwareString.isEmpty() ? normalFirmwareString.replaceAll("[^a-zA-Z0-9 ]", "") : "";
                        setUpData(normalModelString, normalFirmwareString);
                        enableDisableNotification(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void OnDataLoggingWriteCharacteristicsDiscovered(String status) {
                if (boolIsDeviceInformationRetrieveCommandSend) {
//                    AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strClearRequestSendSuccessMsg));
                    enableDisableNotification(true);
                    boolIsDeviceInformationRetrieveCommandSend = false;
                }
                AndroidAppUtils.hideProgressDialog();
            }

        };
    }

    /**
     * Method Name : setUpData
     * Description : This method is used for setting the data for device information
     *
     * @param normalModelString
     * @param normalFirmwareString
     */
    private void setUpData(String normalModelString, String normalFirmwareString) {
        mTvDeviceMacAddress.setText(GlobalConstant.DEVICE_MAC);
        mTvDeviceName.setText(GlobalConstant.DEVICE_NAME);
        mTvDeviceModelNumber.setText(normalModelString);
        mTvDeviceFirmwareVersion.setText(mActivity.getResources().getString(R.string.str_char_v) + normalFirmwareString);
        mTvAppVersion.setText(BuildConfig.VERSION_NAME);
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

}
