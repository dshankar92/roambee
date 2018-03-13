package vvdn.in.ble_ota.listener;

/**
 * Interface Name : DeviceDataReadDurationListener
 * Description : Contains method that respond according to event generated
 * during ble read or write operation to which class implements it.
 *
 * @author Durgesh-Shankar
 */
public interface DeviceDataReadDurationListener {

    void onDataReadFailed(String strError);

    void onDataReadSuccess(String strSuccess);

}
