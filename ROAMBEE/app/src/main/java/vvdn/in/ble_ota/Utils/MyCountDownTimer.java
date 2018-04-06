package vvdn.in.ble_ota.Utils;

import android.app.Activity;
import android.os.CountDownTimer;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.listener.ChoiceDialogClickListener;
import vvdn.in.ble_ota.listener.SnackBarActionButtonListener;
import vvdn.in.ble_ota.view.SelectionActivity;

/**
 * ClassName : MyCountDownTimer
 * Description : This class is used to display pop up
 * with error message on failure of action event generated after counter down timer
 * value is completed on failure.
 *
 * @author Durgesh-Shankar
 */
public class MyCountDownTimer extends CountDownTimer {
    /**
     * Debuggable TAG
     */
    String TAG = MyCountDownTimer.class.getSimpleName();
    /**
     * String message for handling the action by examining the event generated
     */
    String strMessage = "";
    /**
     * SnackBarActionButtonListener reference object
     */
    private SnackBarActionButtonListener mSnackBarActionButtonListener;
    /**
     * Activity reference object
     */
    private Activity mActivity;

    /**
     * Constructor Name : MyCountDownTimer
     * Description : This is public constructor of this class used for initialization of instance variable
     *
     * @param mActivity
     * @param millisInFuture
     * @param countDownInterval
     * @param mSnackBarActionButtonListener
     */
    public MyCountDownTimer(Activity mActivity, long millisInFuture, long countDownInterval,
                            SnackBarActionButtonListener mSnackBarActionButtonListener) {
        super(millisInFuture, countDownInterval);
        this.mSnackBarActionButtonListener = mSnackBarActionButtonListener;
        this.mActivity = mActivity;
    }

    /**
     * Constructor Name : MyCountDownTimer
     * Description : This is public constructor of this class used for initialization of instance variable
     *
     * @param mActivity
     * @param millisInFuture
     * @param countDownInterval
     */
    public MyCountDownTimer(Activity mActivity, long millisInFuture, long countDownInterval
    ) {
        super(millisInFuture, countDownInterval);
        this.mActivity = mActivity;
    }

    /**
     * Method Name : stringGeneratedForActionOccurred
     * Description : t\This method is used for holding the message for action generated
     *
     * @param strMessage
     */
    public void stringGeneratedForActionOccurred(String strMessage) {
        this.strMessage = strMessage;
    }

    @Override
    public void onTick(long l) {

        AndroidAppUtils.showInfoLog(TAG, "Tick Tock Value : " + l);

    }

    @Override
    public void onFinish() {
        AndroidAppUtils.showInfoLog(TAG, " ************* onFinish ************** ");
        if (strMessage.equalsIgnoreCase(mActivity.getResources().getString(R.string.strDataLoggingNotStarted))) {
            AndroidAppUtils.hideProgressDialog();
            AndroidAppUtils.customAlertDialogWithGradiantBtn(mActivity, mActivity.getResources().getString(R.string.strInformationCaption), true, strMessage,
                    true, mActivity.getResources().getString(R.string.strCaptionOK), true, new ChoiceDialogClickListener() {
                        @Override
                        public void onClickOfPositive() {
                            GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                            mActivity.finish();
                        }

                        @Override
                        public void onClickOfNegative() {
                            GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                            mActivity.finish();
                        }
                    }, true);
        } else if (strMessage.equalsIgnoreCase(mActivity.getResources().getString(R.string.str_Fail_to_retrieve_config))) {
            AndroidAppUtils.hideProgressDialog();
            if (AppApplication.getInstance().getCurrentActivity() != null &&
                    AppApplication.getInstance().getCurrentActivity() instanceof SelectionActivity)
                AndroidAppUtils.showSnackBarWithActionButton(AppApplication.getInstance(), strMessage, mSnackBarActionButtonListener);
        } else if (strMessage.equalsIgnoreCase(mActivity.getResources().getString(R.string.strFailToRetrieveMag))) {
            AndroidAppUtils.hideProgressDialog();
//            AndroidAppUtils.showSnackBarWithActionButton(AppApplication.getInstance(), strMessage, mSnackBarActionButtonListener);
        }

    }
}
