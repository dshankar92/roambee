package vvdn.in.ble_ota.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;

import in.gauriinfotech.commons.Commons;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.blecontrols.BluetoothLeService;
import vvdn.in.ble_ota.control.HeaderViewManager;
import vvdn.in.ble_ota.dfu.DfuService;
import vvdn.in.ble_ota.listener.ChoiceDialogClickListener;
import vvdn.in.ble_ota.listener.HeaderViewClickListener;

/**
 * Class Name : DfuActivityOperation
 * Description : This class is used for uploading the new
 * image on device connected
 */
public class DfuActivityOperation extends Activity {

    /**
     * BluetoothLeService instance object
     */
    private BluetoothLeService mBluetoothLeService = null;
    /**
     * Boolean for handling the reading operation in process
     */
    public static boolean isReadAllowed = true;
    /**
     * TextView reference object
     */
    private TextView mTvDeviceUpgradeBtn, mTvDeviceMac, mTvUploadingBtn, mTvPercentageText, mTvDeviceName, mTvFileName,
            mTvSelectFileBtn;
    /**
     * ProgressDialog reference object
     */
    private ProgressDialog mProgressDialog;
    /**
     * Boolean reference object for handling the case of dfu operation
     */
    private boolean isOADInProcess = false;
    /**
     * ProgressBar reference object
     */
    private ProgressBar pecentageBar;
    /**
     * Activity reference object
     */
    private Activity mActivity;
    /**
     * Debuggable TAG
     */
    private String TAG = DfuActivityOperation.class.getSimpleName();
    /**
     * Int reference object holding the request code for file selection
     */
    private int ZIP_FILE_REQUEST_CODE = 111;
    /**
     * String reference object for holding file path and file name
     */
    private String strFilePath = "", strFileName = "";
    /**
     * DfuServiceController reference object
     */
    private DfuServiceController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_firmware_screen);
        mActivity = DfuActivityOperation.this;
        AppApplication.getInstance().setCurrentActivityReference(mActivity);
        GlobalConstant.mGlobalActivityArrayList.add(mActivity);
        isOADInProcess = false;
        initView();
        if (!TextUtils.isEmpty(GlobalConstant.DEVICE_NAME)) {
            mTvDeviceName.setText(GlobalConstant.DEVICE_NAME);
            mTvDeviceMac.setText(GlobalConstant.DEVICE_MAC);
        } else {
            mTvDeviceName.setVisibility(View.GONE);
            mTvDeviceMac.setVisibility(View.GONE);
        }
        mTvDeviceUpgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalConstant.CONNECTED_STATE) {
//                    File zipFile = new File(Environment.getExternalStorageDirectory().toString() + mActivity.getResources().getString(R.string.doorlock_folder) + "ShippingImageV1Test.zip");
//                    String strFilePath = Environment.getExternalStorageDirectory().toString() + mActivity.getResources().getString(R.string.doorlock_folder) + "ShippingImageV1Test.zip";
                    AndroidAppUtils.showInfoLog(TAG, "File Path or strFilePath : " + Environment.getExternalStorageDirectory().toString() + mActivity.getResources().getString(R.string.doorlock_folder) + "ShippingImageV1Test.zip");
                    AndroidAppUtils.showInfoLog(TAG, "File Path Selected : " + strFilePath);
                    if (!strFilePath.isEmpty()) {
                        startDFU(GlobalConstant.DEVICE_MAC, GlobalConstant.DEVICE_NAME, strFilePath);
                    } else {
                        AndroidAppUtils.showSnackBar(AppApplication.getInstance(), mActivity.getResources().getString(R.string.strPleaseSelectFileForUpgradingCaption));
                    }
                } else {

                    AlertDialog.Builder builder = AndroidAppUtils.showAlertDialogWithButtonControls(mActivity, getString(R.string.conect_first));
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ExitFromScreen();
                        }
                    });
                    builder.show();

                }
            }
        });
        mTvSelectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileBrowser();
            }
        });
        mBluetoothLeService = BluetoothLeService.getInstance();
        manageHeaderView();
    }

    /**
     * Method Name : initView
     * Description : This method is used for initializing the view component
     */
    private void initView() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.fetching));
        mProgressDialog.setCancelable(false);
        mTvDeviceUpgradeBtn = (TextView) findViewById(R.id.btn_upgrade);
        pecentageBar = (ProgressBar) findViewById(R.id.determinateBar);
        mTvUploadingBtn = (TextView) findViewById(R.id.btn_uploading);
        mTvPercentageText = (TextView) findViewById(R.id.tv_perc);
        mTvDeviceName = (TextView) findViewById(R.id.tv_device);
        mTvDeviceMac = (TextView) findViewById(R.id.tv_device_mac);
        mTvFileName = (TextView) findViewById(R.id.tv_file_name);
        mTvSelectFileBtn = (TextView) findViewById(R.id.btn_select_file);
    }

    /*******************************************************************
     * Function name - manageHeaderView
     * Description - manage the initialization, visibility and click
     * listener of view fields on Header view
     *******************************************************************/
    public void manageHeaderView() {
        HeaderViewManager.getInstance().InitializeMultiTitleHeaderView(mActivity, null, false, manageHeaderClick());
        HeaderViewManager.getInstance().setHeading(true, mActivity.getResources().getString(R.string.strDFUCaption), mActivity);
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
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    /**
     * Method Name : onBackButtonClick
     * Description : This method is used for checking if back button is clicked then show
     * alert dialog with message  "Do You Want Dfu operation to stop"
     * If yes then stop the current operation and exit the screen
     * If no then continue with current operation
     */
    private void onBackButtonClick() {
        if (isOADInProcess) {

            AndroidAppUtils.showDialogWithOptions(mActivity, mActivity.getResources().getString(R.string.stop_oad_exit_app),
                    mActivity.getResources().getString(R.string.ok),
                    mActivity.getResources().getString(R.string.cancel), mActivity.getResources().getString(R.string.app_name),
                    new ChoiceDialogClickListener() {
                        @Override
                        public void onClickOfPositive() {
                            if (controller != null)
                                controller.abort();
                            ExitFromScreen();
                        }

                        @Override
                        public void onClickOfNegative() {

                        }
                    });

        } else if (mActivity != null && (!isReadAllowed)) {
            AndroidAppUtils.showToast(mActivity, mActivity.getResources().getString(R.string.read_in_process));
        } else {
//            ExitFromScreen();
            super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        onBackButtonClick();
    }

    /**
     * Method Name : openFileBrowser
     * Description : This method is used for opening file browser for selection of file
     */
    public void openFileBrowser() {
        String strMimeType = "*/*";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(strMimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", strMimeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, mActivity.getResources().getString(R.string.strOpenFileCaption));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, mActivity.getResources().getString(R.string.strOpenFileCaption));
        }

        try {
            startActivityForResult(chooserIntent, ZIP_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            AndroidAppUtils.showToast(mActivity, mActivity.getResources().getString(R.string.strNoSuitableFileManagerFoundCaption));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ZIP_FILE_REQUEST_CODE) {

                Uri selectedMediaUri = data.getData();
                String strFileManagerString = "", strSelectedMediaPath = "";
                strFileManagerString = selectedMediaUri.getPath();
                strSelectedMediaPath = getPath(selectedMediaUri);
                if (!TextUtils.isEmpty(strSelectedMediaPath) && !strSelectedMediaPath.equals("")) {
                    strFilePath = strSelectedMediaPath;
                } else if (!TextUtils.isEmpty(strFileManagerString) && !strFileManagerString.equals("")) {
                    strFilePath = strFileManagerString;
                }
                strFilePath = Commons.getPath(selectedMediaUri, mActivity);

                String mimeType = getMimeType(selectedMediaUri);
                AndroidAppUtils.showErrorLog(TAG, "mimeType : " + mimeType);
                if (mimeType != null && !mimeType.isEmpty() && mimeType.endsWith("zip")) {
                    int lastIndex = strFilePath.lastIndexOf("/");
                    strFileName = strFilePath.substring(lastIndex + 1);
                    mTvFileName.setText(strFileName);
                } else {
                    AndroidAppUtils.showToast(mActivity, mActivity.getResources().getString(R.string.strSelectFileWithCorrectExtensionCaption));
                }

                //filepath is your file's path
            }
        }
    }

    /**
     * Method Name : getMimeType
     * Description : This method is used for getting the mime type of file using uri or file
     *
     * @param uri
     * @return
     */
    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    /**
     * Method Name : getPath
     * Description : This method is used for getting path for file
     * selected by user for firmware upgrade
     *
     * @param uri
     * @return
     */
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULL POINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Exit  from  this  view
     */
    private void ExitFromScreen() {
        if (mActivity != null) {
            mActivity.finishAffinity();
            startActivity(new Intent(mActivity, BleScanScreen.class));
        } else
            AndroidAppUtils.showErrorLog(TAG, "mActivity  is  null");

        if (mBluetoothLeService != null)
            mBluetoothLeService.disconnect();
        else AndroidAppUtils.showErrorLog(TAG, "mBluetoothLeService  is  null");

    }


    /**
     * Method Name : startDFU
     * Description : This method is used for starting the DFU operation i.e uploading the
     * new image on device
     *
     * @param address
     * @param name
     * @param filePath
     */
    public void startDFU(String address, String name, String filePath) {
        try {
            final DfuServiceInitiator starter = new DfuServiceInitiator(address)
                    .setKeepBond(false);
            if (name != null) {
                starter.setDeviceName(name);
            }
            starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
            starter.setZip(filePath);
            starter.setForeground(false);
            isOADInProcess = true;
            controller = starter.start(this, DfuService.class);
        } catch (Exception e) {
            AndroidAppUtils.showErrorLog(TAG, " dfu error : " + e.getMessage());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    /**
     * The progress listener receives events from the DFU Service.
     * If is registered in onCreate() and unregistered in onDestroy() so methods here may also be called
     * when the screen is locked or the app went to the background. This is because the UI needs to have the
     * correct information after user comes back to the activity and this information can't be read from the service
     * as it might have been killed already (DFU completed or finished with error).
     */
    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            AndroidAppUtils.showInfoLog(TAG, "CONNECTING : " + deviceAddress);

        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            mTvUploadingBtn.setText(mActivity.getResources().getString(R.string.strDFUStartedCaption));
            mTvUploadingBtn.setVisibility(View.VISIBLE);
            AndroidAppUtils.showInfoLog(TAG, "DFU_PROCESS_STARTING " + deviceAddress);
            GlobalConstant.IS_DFU_OPERATION_STILL_IN_PROCESS = true;
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            AndroidAppUtils.showInfoLog(TAG, "ENABLING_DFU_MODE : " + deviceAddress);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            AndroidAppUtils.showInfoLog(TAG, "FIRMWARE_VALIDATING : " + deviceAddress);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            AndroidAppUtils.showInfoLog(TAG, "DEVICE_DISCONNECTING : " + deviceAddress);
            GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;

        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            AndroidAppUtils.showInfoLog(TAG, "DFU_COMPLETED : " + deviceAddress);
            AndroidAppUtils.showSnackBar(AppApplication.getInstance(), "Firmware Upgrade Successful");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                    isOADInProcess = false;
                    GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = true;
                    GlobalConstant.IS_DFU_OPERATION_STILL_IN_PROCESS = false;
                    ExitFromScreen();
                }
            }, 200);


        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            AndroidAppUtils.showInfoLog(TAG, "DFU_ABORTED : " + deviceAddress);

        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            mTvUploadingBtn.setText(mActivity.getResources().getString(R.string.strCaptionUploading));
            mTvUploadingBtn.setVisibility(View.VISIBLE);
            mTvPercentageText.setVisibility(View.VISIBLE);
            pecentageBar.setVisibility(View.VISIBLE);
            pecentageBar.setProgress((int) percent);
            mTvPercentageText.setText("" + (long) percent + " %");

        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            AndroidAppUtils.showInfoLog(TAG, "DFU_FAILED : " + deviceAddress);
            AndroidAppUtils.showErrorLog(TAG, "error : " + error +
                    "errorType : " + errorType + "message : " + message);
            GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
            AndroidAppUtils.customAlertDialogWithGradiantBtn(AppApplication.getInstance().getCurrentActivity(), mActivity.getResources().getString(R.string.strInformationCaption),
                    true, message, true, mActivity.getResources().getString(R.string.strCaptionOK), true,
                    new ChoiceDialogClickListener() {
                        @Override
                        public void onClickOfPositive() {
                            if (DeviceAdapter.mConnectionControl != null)
                                DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                            else if (mActivity != null)
                                mActivity.finish();

                        }

                        @Override
                        public void onClickOfNegative() {

                        }
                    }, false);
        }
    };

}
