package vvdn.in.ble_ota.blecontrols;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.view.ChangeConfigurationActivity;
import vvdn.in.ble_ota.view.DataLoggingReadingActivity;
import vvdn.in.ble_ota.view.SelectionActivity;

/**
 * Class Name : BluetoothLeService
 * Description : This class is used
 */
public class BluetoothLeService extends Service {
    /**
     * Debuggable TAG
     */
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    /**
     * BluetoothGatt reference object
     */
    public static BluetoothGatt mBluetoothGatt;
    /**
     * BluetoothLeService reference object
     */
    private static BluetoothLeService mBluetoothLeServiceInstance = null;
    private byte[] mOldValue;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, int newState) {
            String intentAction;

            AndroidAppUtils.showLog(TAG, "BLE_Connection_New_State " + newState);
            AndroidAppUtils.showLog(TAG, "BLE_Connection_State " + status);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = GlobalConstant.ACTION_GATT_CONNECTED;
                GlobalConstant.CONNECTED_STATE = true;
                broadcastUpdate(intentAction);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = GlobalConstant.ACTION_GATT_DISCONNECTED;
                GlobalConstant.ACTION_PERFORMED=GlobalConstant.ACTION_GATT_DISCONNECTED;
                GlobalConstant.CONNECTED_STATE = false;
                AndroidAppUtils.showErrorLog(TAG, getResources().getString(R.string.Disconnected_from_GATT_server));
//                broadcastUpdate(intentAction);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    refreshDeviceCache(mBluetoothGatt);
                }
                if (status == GlobalConstant.STATUS_CODE_133 || status == GlobalConstant.STATUS_CODE_0) {
                    AndroidAppUtils.hideProgressDialog();
                    AndroidAppUtils.showLog(TAG, getString(R.string.Doing_retry));
                    AndroidAppUtils.showLog(TAG, "\nGlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE :" + GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE);
                    //  Go for  retry
                    if (GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE) {
                        if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                            GlobalConstant.mBluetoothConnectionStateInterface.onGattDisconnected(status);
                        }
                    } else {
                        broadcastUpdate(intentAction);
                    }
                    if (GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND) {
                        GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = false;
                        if (DeviceAdapter.mConnectionControl != null)
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                    }
                } else if (status == GlobalConstant.STATUS_CODE_8) {
                    AndroidAppUtils.hideProgressDialog();
                    AndroidAppUtils.showLog(TAG, getString(R.string.Doing_retry_record_group) +
                            "\nGlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE :" + GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE);
                    //  Go for  retry
                    if (GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE) {
                        if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                            GlobalConstant.mBluetoothConnectionStateInterface.onGattDisconnected(status);
                        }
                    } else {
                        if(!GlobalConstant.IS_DFU_OPERATION_STILL_IN_PROCESS)
                        {
                            GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE=true;
                        }
                        broadcastUpdate(intentAction);
                    }
                    if (GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND) {
//                        AndroidAppUtils.hideProgressDialog();
                        GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = false;
                        if (DeviceAdapter.mConnectionControl != null)
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                    }
                } else if (status == GlobalConstant.STATUS_CODE_59) {
                    AndroidAppUtils.hideProgressDialog();
                    if(!GlobalConstant.IS_DFU_OPERATION_STILL_IN_PROCESS)
                    {
                        GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE=true;
                    }
                    broadcastUpdate(intentAction);
                    if (GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND) {
                        GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = false;
                        if (DeviceAdapter.mConnectionControl != null)
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                    }


                } else if (status == GlobalConstant.STATUS_CODE_2) {
                    AndroidAppUtils.hideProgressDialog();
                    GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
                    broadcastUpdate(intentAction);
                    if (GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND) {
                        GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = false;
                        if (DeviceAdapter.mConnectionControl != null)
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                    }


                } else if (status == GlobalConstant.STATUS_CODE_19) {
                    AndroidAppUtils.hideProgressDialog();
                    if(!GlobalConstant.IS_DFU_OPERATION_STILL_IN_PROCESS)
                    {
                        GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE=true;
                    }
                    broadcastUpdate(intentAction);
                    if (GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND) {
                        GlobalConstant.BOOL_IS_TURN_OFF_COMMAND_SEND = false;
                        if (DeviceAdapter.mConnectionControl != null)
                            DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                    }


                } else {
                    AndroidAppUtils.hideProgressDialog();
                    GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
                    /**
                     * If none of the above case
                     */
                    broadcastUpdate(intentAction);
                }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(GlobalConstant.ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                AndroidAppUtils.showErrorLog(TAG, "Service Discovered Fail  :" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(GlobalConstant.ACTION_DATA_AVAILABLE, characteristic);
            } else if (status == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
               AndroidAppUtils.showToast(AppApplication.getInstance().getCurrentActivity(), getString(R.string.strReadNotPermitted));
                broadcastUpdate(GlobalConstant.ACTION_READ_DENIED);
            } else {
                AndroidAppUtils.showToast(AppApplication.getInstance().getCurrentActivity(), getString(R.string.strReadingFailed));
                broadcastUpdate(GlobalConstant.ACTION_READ_FAILED);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            AndroidAppUtils.showLog(TAG, "Notification  available : " + AndroidAppUtils.convertToHexString(characteristic.getValue()));
            if (characteristic != null && !Arrays.equals(characteristic.getValue(), mOldValue)) {
                broadcastUpdate(GlobalConstant.ACTION_DATA_AVAILABLE, characteristic);
            } else {
                AndroidAppUtils.showInfoLog(TAG, " ************** Value Received ************ " + AndroidAppUtils.convertToHexString(characteristic.getValue()));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                AndroidAppUtils.showLog(TAG, "characteristic : " + characteristic.getUuid().toString());
                broadcastUpdate(GlobalConstant.ACTION_WRITE_SUCCESS);
                AndroidAppUtils.showLog(TAG, "Action write success");
                if (characteristic.getUuid().toString().equalsIgnoreCase(AppHelper.WriteCharacteristics_META_DATA)
                        || characteristic.getUuid().toString().equalsIgnoreCase(AppHelper.WriteCharacteristics_FILE_DATA)) {
                    if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
                        GlobalConstant.mBluetoothConnectionStateInterface.onGattServiceWrite(characteristic);
                    }
                }
                if (SelectionActivity.mDataLoggingListener != null) {
                    SelectionActivity.mDataLoggingListener.OnDataLoggingWriteCharacteristicsDiscovered(getString(R.string.strSuccessCaption));
                }
                if (DataLoggingReadingActivity.mDataLoggingListener != null) {
                    DataLoggingReadingActivity.mDataLoggingListener.OnDataLoggingWriteCharacteristicsDiscovered(getString(R.string.strSuccessCaption));
                }
                if (ChangeConfigurationActivity.mDataLoggingListener != null) {
                    ChangeConfigurationActivity.mDataLoggingListener.OnDataLoggingWriteCharacteristicsDiscovered(getString(R.string.strSuccessCaption));
                }


            } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
                broadcastUpdate(GlobalConstant.ACTION_WRITE_DENIED);
                AndroidAppUtils.showToast(AppApplication.getInstance().getCurrentActivity(), getString(R.string.strWriteDenied));
                AndroidAppUtils.showLog(TAG, "Action write denied");
            } else {
                broadcastUpdate(GlobalConstant.ACTION_WRITE_FAILED);
                AndroidAppUtils.showToast(AppApplication.getInstance().getCurrentActivity(), getString(R.string.strWriteFailed));
                AndroidAppUtils.showLog(TAG, "Action write failed");

            }
        }


    };
    private final IBinder mBinder = new LocalBinder();

    /**
     * Singlton Instance of BluetoothLeService
     * @return
     */
    public static BluetoothLeService getInstance() {
        if (mBluetoothLeServiceInstance == null)
            mBluetoothLeServiceInstance = new BluetoothLeService();
        return mBluetoothLeServiceInstance;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(GlobalConstant.EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean connect(BluetoothDevice mBluetoothDevice) {
        if (GlobalConstant.mBluetoothAdapter == null) {
            AndroidAppUtils.showInfoLog(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null) {
            AndroidAppUtils.showLog(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothDevice;
        if (device == null) {
            AndroidAppUtils.showInfoLog(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        AndroidAppUtils.showLog(TAG, "Trying to create a new connection. " + mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH));

        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (GlobalConstant.mBluetoothAdapter == null) {
            AndroidAppUtils.showErrorLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null) {
            AndroidAppUtils.showErrorLog(TAG, "mBluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (GlobalConstant.mBluetoothAdapter == null || mBluetoothGatt == null) {
            AndroidAppUtils.showErrorLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        boolean isRead = mBluetoothGatt.readCharacteristic(characteristic);
        AndroidAppUtils.showLog(TAG, "Read State :" + isRead);

    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                 boolean enabled) {
        if (GlobalConstant.mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(AppHelper.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);

        }
        return true;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    /**
     * @param data
     * @param bluetoothGattCharacteristic
     * @return
     */
    public boolean send(byte[] data, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (mBluetoothGatt == null) {
           AndroidAppUtils.showWarningLog(TAG, "BluetoothGatt not initialized");
            return false;
        }
        if (bluetoothGattCharacteristic == null) {
            AndroidAppUtils.showWarningLog(TAG, "Send characteristic not found");
            return false;
        }

        bluetoothGattCharacteristic.setValue(data);

        bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }


    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    /**
     * Method Name : refreshDeviceCache
     * Description : This method is used for refreshing the gatt server
     *
     * @param gatt
     * @return
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            AndroidAppUtils.showErrorLog(TAG, "error message : " + localException.getMessage());
        }
        return false;
    }
}
