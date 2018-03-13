package vvdn.in.ble_ota.webservices.iHelper;

/**
 * Web API Response Helper
 *
 * @author Anshuman
 */
public interface WebAPIResponseListener {
    /**
     * On response of API Call
     *
     * @param arguments
     */
    void onSuccessResponse(Object... arguments);

    /**
     * On APi Fail
     *
     * @param arguments
     */
    void onFailResponse(Object... arguments);

    /**
     * On App Offline Manager
     *
     * @param arguments
     */
    void onOfflineResponse(Object... arguments);
}
