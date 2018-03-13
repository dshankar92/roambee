package vvdn.in.ble_ota.listener;

/***
 * This interface to manage Packet read/write operation
 *
 * @author Durgesh-Shankar
 *
 */
public interface DataLoggingListener {
    /**
     * Listener for new data or byte packet received for current packet write success
     *
     * @param byteData
     */
    void OnNotificationReceived(byte[] byteData);

    /**
     * Listener for deliverying the result of packet write either WRITE SUCCESS or WRITE FAILURE
     *
     * @param status
     */
    void OnDataLoggingWriteCharacteristicsDiscovered(String status);


}
