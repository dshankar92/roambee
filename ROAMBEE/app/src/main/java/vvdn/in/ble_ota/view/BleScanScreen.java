package vvdn.in.ble_ota.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.blecontrols.ConnectionControl;
import vvdn.in.ble_ota.model.BLEDataModel;

/**
 * Class Name : BleScanScreen
 * Description : This class is used for scanning ble device. updating the recycler view with new discovered device.
 * updating the device with ints advertisement information if already added in list
 */
public class BleScanScreen extends Activity {

    /**
     * Debuggable TAG
     */
    private static final String TAG = BleScanScreen.class.getSimpleName();
    /**
     * Constant Code
     */
    private static final int REQUEST_CODE_WRITE = 101;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final long ONE_SEC = 1000;
    /**
     * Relative layout instance object
     */
    private RelativeLayout mlLRefresh;
    /**
     * Boolean for checking if filter is applied
     */
    private boolean boolIsFilteredApplied;
    /**
     * DeviceAdapter reference object
     */
    private DeviceAdapter mDeviceAdapter;
    /**
     * Handler reference object
     */
    public Handler mHandler;
    /**
     * BluetoothAdapter reference object
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * ArrayList reference object for holding discovered device data
     */
    private ArrayList<BLEDataModel> bleDeviceModelsList = new ArrayList<>();
    /**
     * TextView reference object for displaying message no device found
     */
    private TextView no_device_found;
    /**
     * ProgressDialog for showing scanning in progress
     */
    private ProgressBar scanProgressBar;
    /**
     * ListView reference object
     */
    private ListView csb_device_lv;
    /**
     * Recycler View reference object
     */
    private RecyclerView recyclerView;
    /**
     * ImageView reference object
     */
    private ImageView llRefreshBtn;
    /**
     * Activity reference object
     */
    private Activity mActivity;
    /**
     * EditText reference object for taking input from the user and the filter the list item
     * based on user input key
     */
    public static EditText etSearchFilter;
    /**
     * TextInputLayout reference object for setting the hint
     */
    private TextInputLayout layoutEtSearchFilter;
    /**
     * BluetoothManager instance object
     */
    private BluetoothManager bluetoothManager = null;
    /*Task to start scanning*/
    public Runnable scanRunnableTask;
    /*Receiver to check BLE scanning while Service is runnning*/
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            boolean status = intent.getBooleanExtra(AppHelper.STATUS, false);
            if (status) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkBluetoothAndGPS();
                    }
                }, ONE_SEC);

            }
        }
    };

    /**
     * Callback methods of BLE scanning.,
     */

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    getBLEDevices(device, rssi, scanRecord);

                }


            };


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * Initialise variables of activity.
     */
    private void init() {
        AndroidAppUtils.showLog(TAG, "Ble Scan Screen onCreate");
        mActivity = this;
        GlobalConstant.mGlobalActivityArrayList.add(mActivity);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        no_device_found = (TextView) findViewById(R.id.error_tv);
        llRefreshBtn = (ImageView) findViewById(R.id.refresh_btn);
        scanProgressBar = (ProgressBar) findViewById(R.id.scan_progress);
        scanProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        etSearchFilter = (EditText) findViewById(R.id.etSearchFilter);
        layoutEtSearchFilter = (TextInputLayout) findViewById(R.id.layoutEtSearchFilter);
        etSearchFilter.addTextChangedListener(textWatcher);
        etSearchFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if (etSearchFilter.getText().toString().isEmpty()) {
                        layoutEtSearchFilter.setHint("Filter");
                        layoutEtSearchFilter.setHintEnabled(true);
                    } else {
                        layoutEtSearchFilter.setHint("Filter");
                        layoutEtSearchFilter.setHintEnabled(true);
                    }
                } else {
                    if (etSearchFilter.getText().toString().isEmpty()) {
                        layoutEtSearchFilter.setHint("No Filter");
                        layoutEtSearchFilter.setHintEnabled(true);
                    } else {
                        layoutEtSearchFilter.setHint("Filter");
                        layoutEtSearchFilter.setHintEnabled(true);
                    }
                }
            }
        });
        if (etSearchFilter.getText().toString().trim().isEmpty()) {
            etSearchFilter.clearFocus();
        }
//        csb_device_lv = (ListView) findViewById(R.id.csb_devices_list);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mlLRefresh = (RelativeLayout) findViewById(R.id.ll_refresh);
        mlLRefresh.bringToFront();
        mlLRefresh.setVisibility(View.VISIBLE);
        setListener();
        bleDeviceModelsList = new ArrayList<>();
        isStoragePermissionGranted();
        setDataOnList();
        starBluetoothScanning();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
        /*registering device data receiver*/
        LocalBroadcastManager.getInstance(this).registerReceiver(deviceDataReceiver,
                new IntentFilter(GlobalConstant.KEY_UPDATE_UI));


        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            AndroidAppUtils.showInfoLog(TAG, "Multiple advertisement not supported");
        }
    }

    /*Broadcast Receiver to receive data from worker thread and update to UI */
    public BroadcastReceiver deviceDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDeviceAdapter != null) {
                            if (intent != null) {
                                BLEDataModel bleDataModel = new BLEDataModel();
                                bleDataModel.setName(intent.hasExtra(GlobalConstant.KEY_NAME) ? intent.getStringExtra(GlobalConstant.KEY_NAME) : "UNKNOWN");
                                bleDataModel.setMacAddress(intent.hasExtra(GlobalConstant.KEY_ADDRESS) ? intent.getStringExtra(GlobalConstant.KEY_ADDRESS) : "");
                                bleDataModel.setBleDevice(intent.hasExtra(GlobalConstant.KEY_BLUETOOTH_DEVICE) ? (BluetoothDevice) intent.getExtras().get(GlobalConstant.KEY_BLUETOOTH_DEVICE) : null);
                                bleDataModel.setStrRssiStrength(intent.hasExtra(GlobalConstant.KEY_RSSI_STRENGTH) ? intent.getStringExtra(GlobalConstant.KEY_RSSI_STRENGTH) : "0");
                                bleDataModel.setStrManufactureData(intent.hasExtra(GlobalConstant.KEY_MANUFACTURE_DATA) ? intent.getStringExtra(GlobalConstant.KEY_MANUFACTURE_DATA) : "0");

                                mDeviceAdapter.setMoreDataToList(bleDataModel);
                            } else {
                                AndroidAppUtils.showLog(TAG, "intent is null");
                            }
                        }
                    }
                });


            }
        }
    };


    /**
     * Method Name : setListener
     * Description : This method is used for setting the listener
     */
    private void setListener() {
        mlLRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidAppUtils.showLog(TAG, "Clicked ll refresh");
                if (llRefreshBtn.getVisibility() == View.VISIBLE) {
                    checkBluetoothAndGPS();
                } else {
                    AndroidAppUtils.showLog(TAG, " Scanning is going on in background");
                }

            }
        });

    }


    /**
     * Check bluetooth and gps enabled.
     */

    public void checkBluetoothAndGPS() {
        if (checkBluetooth()) {
            if (isBluetoothAndLocationEnabled()) {
                setAdapterToDefault();
                scanLeDevice(true);
                showSettingsAlert(BleScanScreen.this);
                showProgressBar();

            }
        }
    }

    /**
     * Method Name : setAdapterToDefault
     * Description : This method is used for resetting adaptor with default value
     */
    private void setAdapterToDefault() {
        bleDeviceModelsList = new ArrayList<>();
        if (mDeviceAdapter != null) {
            mDeviceAdapter.setListData(bleDeviceModelsList);
        } else {
            mDeviceAdapter = new DeviceAdapter(mActivity, this, no_device_found);
            mDeviceAdapter.setListData(bleDeviceModelsList);
        }
    }

    /**
     * Method Name : showProgressBar
     * Description : This method is used for displaying progressbar
     */
    private void showProgressBar() {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mlLRefresh.setVisibility(View.VISIBLE);
                    llRefreshBtn.setVisibility(View.GONE);
                    scanProgressBar.setVisibility(View.VISIBLE);

                }
            });
        }

    }

    /**
     * Method Name : hideProgressBar
     * Description : This method is used for hiding progressbar
     */
    private void hideProgressBar() {
        scanProgressBar.setVisibility(View.GONE);
        llRefreshBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Start Bluetooth scanning if it is enabled.
     */
    private void starBluetoothScanning() {

        mHandler = new Handler(Looper.getMainLooper());
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            AndroidAppUtils.showToast(mActivity, mActivity.getResources().getString(R.string.no_ble_supported));
            finish();
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();
        GlobalConstant.mBluetoothAdapter = mBluetoothAdapter;

    }

    /**
     * Check whether bluetooth enabled
     *
     * @return
     */
    private boolean isBluetoothAndLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled) {
                gps_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }
            if (!gps_enabled) {
                gps_enabled = lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && gps_enabled) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActivity != null)
            AppApplication.getInstance().setCurrentActivityReference(mActivity);
        checkBluetoothAndGPS();
        GlobalConstant.CONNECTED_STATE = false;
    }

    /**
     * Check Bluetooth enabled.
     */
    public boolean checkBluetooth() {

        AndroidAppUtils.showLog(TAG, "Checking bluetooth");

        if (mBluetoothAdapter == null)
            mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            AndroidAppUtils.showErrorLog(TAG, "Bluetooth is  OFF");

            mlLRefresh.setVisibility(View.GONE);

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        } else {
            AndroidAppUtils.showLog(TAG, "bluetooth is ON");
            return true;
        }

    }

    /**
     * Start scanning LE devices.
     *
     * @param enable
     */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            checkForBLEDevice();
            llRefreshBtn.setVisibility(View.GONE);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mHandler.postDelayed(scanRunnableTask, 500);
        }
    }

    /**
     * Stop the BLE Scan
     */
    public void StopBLEScan() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private void checkForBLEDevice() {

        //perform your action here
        scanRunnableTask = new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                scanLeDevice(false);
                if (bleDeviceModelsList.size() == 0)
                    no_device_found.setVisibility(View.VISIBLE);
                else no_device_found.setVisibility(View.GONE);
                if (etSearchFilter.getText().toString().trim().isEmpty()) {
                    etSearchFilter.clearFocus();
                }
            }
        };
//        mHandler.postDelayed(scanRunnableTask, SCAN_PERIOD);
        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    /**
     * Check  each  BLE  Device
     *
     * @param device
     * @param rssi
     * @param scanRecord
     */
    private void getBLEDevices(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        String name = device.getName();
        String address = device.getAddress();
        final int rssiPercent = (int) (100.0f * (127.0f + rssi) / (127.0f + 20.0f));
        if (name != null && !name.isEmpty()) {
            final BLEDataModel bleDataModel = new BLEDataModel();
            bleDataModel.setName(name);
            bleDataModel.setMacAddress(address);
            bleDataModel.setBleDevice(device);
            bleDataModel.setStrRssiStrength(rssiPercent + "");
            bleDataModel.setStrManufactureData(fetchManufactureData(scanRecord, name));
            boolean isDeviceAdded = false;
            bleDeviceModelsList = mDeviceAdapter.getBleDeviceModelsList();
            if (bleDeviceModelsList != null && bleDeviceModelsList.size() > 0) {
                for (BLEDataModel dataModel : bleDeviceModelsList) {
                    if (dataModel.getMacAddress().equalsIgnoreCase(device.getAddress())) {
                        isDeviceAdded = true;
                        break;
                    }
                }
            }
            if (!isDeviceAdded && !bleDeviceModelsList.contains(bleDataModel)) {
                AndroidAppUtils.showInfoLog(TAG, "bleDataModel.getName() : " + bleDataModel.getName() +
                        "\n scanRecord in hex : " + AndroidAppUtils.convertToHexString(scanRecord));
                AndroidAppUtils.showLog(TAG, "Device adding... : " + bleDeviceModelsList.size());
                sendBroadcastMessageToUpdateUI(bleDataModel);
            } else {
                final List<BLEDataModel> bleDataModelList = mDeviceAdapter.getBleDeviceModelsList();
                if (bleDataModelList != null && bleDataModelList.size() > 0) {
                    for (int i = 0; i < bleDataModelList.size(); i++) {
                        BLEDataModel mOldBleDataModel = bleDataModelList.get(i);
                        if (mOldBleDataModel.getMacAddress().equalsIgnoreCase(bleDataModel.getMacAddress())) {
                            if (!AndroidAppUtils.byteArrayCheckZero(bleDataModel.getStrManufactureData().getBytes())) {
                                bleDataModelList.set(i, bleDataModel);
                            } else {
                                bleDataModel.setStrManufactureData(mOldBleDataModel.getStrManufactureData());
                                bleDataModelList.set(i, bleDataModel);
                            }

                        }

                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mDeviceAdapter.setListData((ArrayList<BLEDataModel>) bleDataModelList);
                            mDeviceAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }

        }

    }

    /**
     * Method Name : fetchManufactureData
     * Description : This method is used for extracting the manufacture data from scan record data
     *
     * @param scanRecord
     * @param name
     * @return
     */
    private String fetchManufactureData(byte[] scanRecord, String name) {
        String strManufactureData = "";
        if (scanRecord != null && scanRecord.length > 0) {
            for (int i = 0; i < scanRecord.length; i++) {
                if (i == 15) {
                    int intTotalMFByteIndex = 9;
                    byte[] mByteData;
                    AndroidAppUtils.showInfoLog(TAG, "scanRecord[i] : " + scanRecord[i] +
                            "\n scanRecord[i] : " + (scanRecord[i] & 0xFF));
                    if (scanRecord[i] == 01) {
                        intTotalMFByteIndex = 10;
                    } else if (scanRecord[i] == 04) {
                        intTotalMFByteIndex = 16;
                    } else if (scanRecord[i] == 05) {
                        intTotalMFByteIndex = 13;
                    }
                    mByteData = new byte[intTotalMFByteIndex];

                    System.arraycopy(scanRecord, i - 5, mByteData, 0, intTotalMFByteIndex);
                    AndroidAppUtils.showInfoLog(TAG, "start data : " + AndroidAppUtils.convertToHexString(mByteData));
                    strManufactureData = AndroidAppUtils.convertToHexString(mByteData);
                }
            }
        }
        return strManufactureData;
    }


    /**
     * Method to send data to UI thread to update UI.
     *
     * @param bleDataModel
     */
    private void sendBroadcastMessageToUpdateUI(BLEDataModel bleDataModel) {
        Intent intent = new Intent(GlobalConstant.KEY_UPDATE_UI);
        intent.putExtra(GlobalConstant.KEY_NAME, bleDataModel.getName());
        intent.putExtra(GlobalConstant.KEY_ADDRESS, bleDataModel.getMacAddress());
        intent.putExtra(GlobalConstant.KEY_BLUETOOTH_DEVICE, bleDataModel.getBleDevice());
        intent.putExtra(GlobalConstant.KEY_RSSI_STRENGTH, bleDataModel.getStrRssiStrength());
        intent.putExtra(GlobalConstant.KEY_MANUFACTURE_DATA, bleDataModel.getStrManufactureData());
        LocalBroadcastManager.getInstance(BleScanScreen.this).sendBroadcast(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }

            }
            case REQUEST_CODE_WRITE: {
            }
            return;
        }


        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            AndroidAppUtils.showLog(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    /**
     * Method Name : isStoragePermissionGranted
     * Description : This method is used for checking the permission regarding
     * storage read/write operation on device
     *
     * @return
     */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                AndroidAppUtils.showLog(TAG, "Permission is granted");
                return true;
            } else {

                AndroidAppUtils.showLog(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            AndroidAppUtils.showLog(TAG, "Permission is granted");
            return true;
        }

    }


    /**
     * OnActivityResult method.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                showSettingsAlert(this);
            }

        }
        if (isBluetoothAndLocationEnabled()) {
            setAdapterToDefault();
            scanLeDevice(true);
            AndroidAppUtils.showLog(TAG, "Scanning started");
        }

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            setDataOnList();
        }
    }

    /**
     * Method Name : setDataOnList
     * Description : This method is used for attaching adapter with listview for displaying data
     */
    private void setDataOnList() {
        mDeviceAdapter = new DeviceAdapter(BleScanScreen.this, this, no_device_found);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);
        recyclerView.setAdapter(mDeviceAdapter);

//        recyclerView.addOnItemTouchListener(mDeviceAdapter.getInstanceRecyclerViewTouchListener(recyclerView));
    }

    /**
     * Location alert popup.
     *
     * @param context
     */
    private void showSettingsAlert(final Activity context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user

        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = AndroidAppUtils.showAlertDialogWithButtonControls(this, getString(R.string.exit_text));
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();

            }
        });
        builder.show();
    }

    /**
     * Text Watcher for taking input from user and filtering the list accordingly
     */
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() > 0 && !TextUtils.isEmpty(etSearchFilter.getText())) {
                boolIsFilteredApplied = true;
            } else {
                boolIsFilteredApplied = false;
            }

            if (mDeviceAdapter != null) {
                mDeviceAdapter.filter(etSearchFilter.getText().toString());
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @Override
    protected void onDestroy() {
        if (ConnectionControl.connectionControl != null)
            ConnectionControl.connectionControl.UnregisterAllServices();
        super.onDestroy();
    }


}



