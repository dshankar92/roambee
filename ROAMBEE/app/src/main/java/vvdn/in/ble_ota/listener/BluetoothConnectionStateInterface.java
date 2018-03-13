package vvdn.in.ble_ota.listener;

/**
 * Interface Name : BluetoothConnectionStateInterface
 * Description : Contains method that respond according to event generated
 * during ble operation to which class implements it.
 *
 * @author Durgesh-Shankar
 */
public interface BluetoothConnectionStateInterface {
    void onGattConnected(Object... bluetoothData);

    void onGattConnecting(Object... bluetoothData);

    void onGattDisconnected(Object... bluetoothData);

    void onGattServiceDiscovery(Object... bluetoothData);

    void onGattServiceRead(Object... bluetoothData);

    void onGattServiceWrite(Object... bluetoothData);

    void onGattServiceWriteStatus(boolean status);

    void onGattServiceDataAvailable(Object... bluetoothData);

    void onGattServiceReadNotPermitted(Object... bluetoothData);

    void onGattServiceWriteNotPermitted(Object... bluetoothData);

    void onGattStartReadingResponse(Object... bluetoothData);
}
