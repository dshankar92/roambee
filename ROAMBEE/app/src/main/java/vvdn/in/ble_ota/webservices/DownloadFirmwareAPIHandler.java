package vvdn.in.ble_ota.webservices;

import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalKeys;
import vvdn.in.ble_ota.webservices.iHelper.WebAPIResponseListener;


/**
 * Get Camera WiFI API Handler
 *
 * @author Anshuman
 */
public class DownloadFirmwareAPIHandler {
    /**
     * Instance object of Login API
     */
    private Activity mActivity;
    /**
     * Debug TAG
     */
    private String TAG = DownloadFirmwareAPIHandler.class.getSimpleName();
    /**
     * API Response Listener
     */
    private WebAPIResponseListener mResponseListener;


    /**
     * @param mActivity
     * @param webAPIResponseListener
     */
    public DownloadFirmwareAPIHandler(Activity mActivity,
                                      WebAPIResponseListener webAPIResponseListener,
                                      boolean isProgressShowing) {
        if (isProgressShowing) {
            AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.please_wait), false);
        }
        this.mActivity = mActivity;
        this.mResponseListener = webAPIResponseListener;


        postAPICall();
    }

    /**
     * Making json object request
     */
    public void postAPICall() {
        /**
         * JSON Request
         */
        StringRequest jsonObjReq = new StringRequest(Method.POST,
                (GlobalKeys.ROAMBEE_BASE_APP_API + GlobalKeys.META_DATA).trim(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        AndroidAppUtils.showLog(TAG, "Response :"
                                + response);
                        parseAPIResponse(response);
                        AndroidAppUtils.hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                AndroidAppUtils.showErrorLog(TAG, "Volley error: " + error);
                mResponseListener.onFailResponse(error);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(GlobalKeys.CONTENT_TYPE, GlobalKeys.APP_JSON);
                return params;
            }
        };

        // Adding request to request queue
       /* ApplicationController.getInstance().addToRequestQueue(jsonObjReq,
                SetWiFiConnectionAPIHandler.class.getSimpleName());
        // set request time-out
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                GlobalConfig.ONE_SECOND * GlobalConfig.API_REQUEST_TIME,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        // Canceling request
        // ApplicationController.getInstance().getRequestQueue()
        // .cancelAll(GlobalKeys.IS_SONY_APP_API_KEY);
    }

    /*Parse WiFi Connection Response
     * @param response*/
    protected void parseAPIResponse(String response) {
        try {
            if (response.contains("OK"))
                mResponseListener.onSuccessResponse();
            else
                mResponseListener.onFailResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
