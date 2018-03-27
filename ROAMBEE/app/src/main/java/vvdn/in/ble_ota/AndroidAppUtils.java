package vvdn.in.ble_ota;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.Logger;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.listener.ChoiceDialogClickListener;
import vvdn.in.ble_ota.listener.SnackBarActionButtonListener;

/**
 * ClassName : AndroidAppUtils
 * Description : This class contains all the utiliy function that is being used throughout the
 * application
 *
 * @author Durgesh-Shankar
 */
public class AndroidAppUtils {

    /**
     * Debuggable TAG
     */
    private static final String TAG = AndroidAppUtils.class.getSimpleName();
    /**
     * ProgressDialog instance object
     */
    private static ProgressDialog mProgressDialog;
    /**
     * String constant for dividing string into two part based on regex
     */
    private static String SPLIT_STRING_INTO_TWO_CHARACTER_REGEX = "(?<=\\G.{2})";
    /**
     * Boolean for managing the visibility of logging when different type of build.
     * DEBUG : Show logs
     * RELEASE : Hide logs
     */
    private static boolean BOOL_IS_DEBUGGING = true;
    /**
     * String reference object for showing mCustomProgressDialog message
     */
    private static String mesg = "", old_message = "";

    /**
     * Dialog reference object
     */
    private static Dialog gradiantDialog;

    /**
     * Method Name : convertToHexString
     * Description : This method is used for converting byte array into hex string
     *
     * @param data
     * @return
     */
    public static String convertToHexString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) buf.append((char) ('0' + halfbyte));
                else buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Method Name : getStringFromHex
     * Description : This method is used for getting string data from hex string
     *
     * @param manufacturing
     * @return
     */
    public static String getStringFromHex(String manufacturing) {

        String hexString = manufacturing.substring(4);
        String str = "";
        for (int i = 0; i < hexString.length(); i += 2) {
            String s = hexString.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str = str + (char) decimal;
        }
        return str;
    }

    /**
     * Method Name : hexToBin
     * Description : This method is used for converting hex string to binary data
     *
     * @param str
     * @return
     */
    public static byte[] hexToBin(String str) {
        int len = str.length();
        byte[] out = new byte[len / 2];
        int endIndx;

        for (int i = 0; i < len; i = i + 2) {
            endIndx = i + 2;
            if (endIndx > len)
                endIndx = len - 1;
            out[i / 2] = (byte) Integer.parseInt(str.substring(i, endIndx), 16);
        }
        return out;
    }

    /**
     * Method Name : checkInternet
     * Description : This method is used for checking is device connected to internet
     *
     * @param context
     * @return
     */
    public static boolean checkInternet(Context context) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            connected = false;
        }
        return connected;
    }

    /**
     * Showing progress dialog
     *
     * @param msg
     */
    public static void showProgressDialog(final Activity mActivity,
                                          final String msg, final boolean isCancelable) {
        try {
            if (mActivity != null && mProgressDialog != null
                    /*&& mProgressDialog.isShowing()*/) {
                try {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            mProgressDialog = null;
            if (mProgressDialog == null && mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog = new ProgressDialog(mActivity);
                        mProgressDialog.setMessage(msg);
                        mProgressDialog.setCancelable(isCancelable);
                    }
                });

            }
            if (mActivity != null && !mActivity.isFinishing() && mProgressDialog != null
                    && !mProgressDialog.isShowing()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.show();
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hide progress dialog
     */
    public static void hideProgressDialog() {
        showErrorLog(TAG, "hideProgressDialog called:::: ");
        if (mProgressDialog != null /*&& mProgressDialog.isShowing()*/) {
            try {
                mProgressDialog.dismiss();
                mProgressDialog = null;

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        } else {
            showErrorLog(TAG, "mProgressDialog is null");
        }
    }

    /**
     * Check device have internet connection or not
     *
     * @param activity
     * @return
     */
    public static boolean isOnline(Activity activity) {
        {
            boolean haveConnectedWifi = false;
            boolean haveConnectedMobile = false;

            ConnectivityManager cm = (ConnectivityManager) activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                    if (ni.isConnected()) {
                        haveConnectedWifi = true;
                        showLog(TAG, "WIFI CONNECTION : AVAILABLE");
                    } else {
                        showLog(TAG, "WIFI CONNECTION : NOT AVAILABLE");
                    }
                }
                if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                    if (ni.isConnected()) {
                        haveConnectedMobile = true;
                        showLog(TAG,
                                "MOBILE INTERNET CONNECTION : AVAILABLE");
                    } else {
                        showLog(TAG,
                                "MOBILE INTERNET CONNECTION : NOT AVAILABLE");
                    }
                }
            }
            return haveConnectedWifi || haveConnectedMobile;
        }

    }

    /**
     * Method Name : showLog
     * Description : This method is used for showing debug log.
     *
     * @param tag
     * @param text
     */
    public static void showLog(String tag, String text) {
        if (BOOL_IS_DEBUGGING) {
            Log.d(tag, text);
            Logger.log(GlobalConstant.mLogSession, LogContract.Log.Level.DEBUG, text);
        }
    }

    /**
     * Method Name : showInfoLog
     * Description : This method is used for showing information log.
     *
     * @param tag
     * @param text
     */
    public static void showInfoLog(String tag, String text) {
        if (BOOL_IS_DEBUGGING) {
            Log.i(tag, text);
            Logger.log(GlobalConstant.mLogSession, LogContract.Log.Level.INFO, text);
        }
    }

    /**
     * Method Name : showVerboseLog
     * Description : This method is used for showing verbose log.
     *
     * @param tag
     * @param text
     */
    public static void showVerboseLog(String tag, String text) {
        if (BOOL_IS_DEBUGGING) {
            Log.v(tag, text);
            Logger.log(GlobalConstant.mLogSession, LogContract.Log.Level.VERBOSE, text);
        }
    }

    /**
     * Method Name : showToast
     * Description : This method is used for showing toast message.
     *
     * @param mActivity
     * @param s
     */
    public static void showToast(final Activity mActivity, final String s) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, s, Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Show Message Info View
     *
     * @param mActivity
     * @param msg
     */
    @SuppressLint("InflateParams")
    public static void showMessageDialog(final Activity mActivity, final String msg, final String buttonMsg) {
        if (mActivity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.app_name);
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton(buttonMsg,
                    new DialogInterface.OnClickListener() {

                        @SuppressWarnings("static-access")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (mActivity != null) {
                                mActivity.finish();
                            }
                        }
                    });

            AlertDialog dialog = builder.show();
            if (mActivity != null && !mActivity.isFinishing()) {
                try {
                    dialog.show();
                } catch (Exception e) {
                    AndroidAppUtils.showLog(TAG, "" + e);
                }
            }
        }
    }

    /**
     * Show Message Info View
     *
     * @param mActivity
     * @param msg
     */
    @SuppressLint("InflateParams")
    public static void showMessageDialogODA(final Activity mActivity, final String msg, final String buttonMsg) {
        if (mActivity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.app_name);
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton(buttonMsg,
                    new DialogInterface.OnClickListener() {

                        @SuppressWarnings("static-access")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.show();
            if (mActivity != null && !mActivity.isFinishing()) {
                try {
                    dialog.show();
                } catch (Exception e) {
                    AndroidAppUtils.showLog(TAG, "" + e);
                }
            }
        }
    }

    /**
     * Show button with controls.
     *
     * @param mActivity
     * @param msg
     * @return
     */
    public static AlertDialog.Builder showAlertDialogWithButtonControls(final Activity mActivity, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mActivity);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * to convert string to hexadecimal
     *
     * @param row
     * @return
     */
    public static String convertInHex(String row) {
        Float abc;
        int pqr;
        String final_row = "";
        if (row != "" && row != null) {
            abc = Float.parseFloat(row);
            pqr = (int) Math.round(abc);
            final_row = Integer.toHexString(pqr);
        }
        return final_row;

    }

    /**
     * Method Name : showErrorLog
     * Description : This method is used for showing error log.
     *
     * @param tag
     * @param text
     */
    public static void showErrorLog(String tag, String text) {

        if (BOOL_IS_DEBUGGING) {
            Log.e(tag, text);
            Logger.log(GlobalConstant.mLogSession, LogContract.Log.Level.ERROR, text);
        }
    }

    /**
     * Method Name : showWarningLog
     * Description : This method is used for showing warning log.
     *
     * @param tag
     * @param text
     */
    public static void showWarningLog(String tag, String text) {

        if (BOOL_IS_DEBUGGING) {
            Log.w(tag, text);
            Logger.log(GlobalConstant.mLogSession, LogContract.Log.Level.WARNING, text);
        }
    }

    /**
     * Multiple choice dialog
     *
     * @param mActivity
     * @param msg
     * @param positiveButtonText
     * @param negativeButtonText
     * @param mChoiceDialogClickListener
     */
    public static void showDialogWithOptions(Activity mActivity, String msg,
                                             String positiveButtonText, String negativeButtonText, String title,
                                             final ChoiceDialogClickListener mChoiceDialogClickListener) {
        if (mActivity != null && !mActivity.isFinishing()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    mActivity);

            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setPositiveButton(positiveButtonText,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mChoiceDialogClickListener.onClickOfPositive();
                        }
                    });
            alertDialogBuilder.setNegativeButton(negativeButtonText,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            mChoiceDialogClickListener.onClickOfNegative();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    /**
     * Unzip folder     *
     * * @param _zipFile
     * * @param _location
     */
    public static boolean unzip(String zipFile, String location) {
        AndroidAppUtils.showErrorLog(TAG, "called------------");
        AndroidAppUtils.showErrorLog(TAG, "_zipFile------------" + zipFile);
        AndroidAppUtils.showErrorLog(TAG, "_location------------" + location);
        try {

            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                AndroidAppUtils.showErrorLog(TAG, "Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    dirChecker(location, ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(location + ze.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zin.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    bufout.close();
                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
            return true;
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
            return false;
        }
    }

    /**
     * Method Name : dirChecker
     * Description : This method is used for cheking the existence of directory/folder at
     * specified location
     *
     * @param location
     * @param dir
     */
    public static void dirChecker(String location, String dir) {
        showLog(TAG, "dir : " + dir);
        File f = new File(location + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        } else if (f.isDirectory()) {
            f.mkdirs();
        }
    }

    /**
     * Function Name: unHex
     * Description: Function is used for converting hex data into string
     *
     * @param arg
     * @return
     */
    public static String unHex(String arg) {
        String str = "";
        for (int i = 0; i < arg.length(); i += 2) {
            String s = arg.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str = str + (char) decimal;
        }

        return str;
    }

    /**
     * Method Name : convertHexToString
     * Description : This method is used for converting hex string to string data
     *
     * @param hex
     */
    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());

        return temp.toString();
    }

    /**
     * Method Name : IterateOverByteArray
     * Description : This method is used for iterating over byte array
     *
     * @param byteData
     */
    public static void IterateOverByteArray(byte[] byteData) {
        for (int i = 0; i < byteData.length; i++) {
            AndroidAppUtils.showLog(TAG, " DATA BYTE : " + byteData[i]
                    + "\n DATA IN HEX : " + AndroidAppUtils.convertToHexString(byteData)
            );
        }
    }

    /**
     * Method Name : verifyForMoreThanTwoDigit
     * Description : This method is used for dividing the string into chunks to two character
     *
     * @param mData
     * @return
     */
    public static byte[] verifyForMoreThanTwoDigit(String mData) {
        String strTempArray = "";
        byte[] byteObtained = {(byte) 0x00, (byte) 0x00};
        int byteIndex = 0;
        if (!mData.isEmpty()) {
            if (mData.length() > 2) {
                strTempArray = Arrays.toString(mData.split(AndroidAppUtils.SPLIT_STRING_INTO_TWO_CHARACTER_REGEX));
                AndroidAppUtils.showLog(TAG, "strTempArray : " + strTempArray.replace("[", "").replace("]", ""));
                strTempArray = strTempArray.replace("[", "").replace("]", "");
                String[] strArray = strTempArray.split(",");
                byteObtained = new byte[strArray.length];
                for (int i = 0; i < strArray.length; i++) {
                    byteObtained[i] = (byte) (Integer.parseInt(strArray[i].trim().isEmpty() ? "00" : strArray[i].trim(), 16) & 0xFF);
                }
            }
        } else {
            showInfoLog(TAG, "mData is empty");
        }
        return byteObtained;
    }

    /**
     * Method Name : appendNoOfZeroIfRequired
     * Description : This method is used for appendding required no of zero in input
     *
     * @param strInputData
     * @return
     */
    public static String appendNoOfZeroIfRequired(String strInputData) {
        String strOutputData = "";
        if (!strInputData.isEmpty()) {
            if (strInputData.length() == 0) {
                strOutputData = "0000";
            } else if (strInputData.length() == 1) {
                strOutputData = "000" + strInputData;
            } else if (strInputData.length() == 2) {
                strOutputData = "00" + strInputData;
            } else if (strInputData.length() == 3) {
                strOutputData = "0" + strInputData;
            } else {
                strOutputData = strInputData;
            }
        } else {
            AndroidAppUtils.showErrorLog(TAG, "*********** strInputData is empty **************");
        }
        return strOutputData;
    }

    /**
     * Down Keyboard
     *
     * @param mActivity
     */
    public static void hideKeyboard(Activity mActivity, View view) {
        try {
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            showErrorLog(TAG, " Exception error : " + e);
        }
    }

    /**
     * Function Name: showSnackBar
     * Description : This function is used for showing Snack bar with message
     *
     * @param mActivity
     * @param message
     */
    public static void showSnackBar(AppApplication mActivity
            , String message) {
        Activity mInnnerActivity = mActivity.getCurrentActivity();
        View rootView = mInnnerActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundResource(R.drawable.gs_bg_snackbar);
        TextView snackBarText = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        snackBarText.setTextColor(Color.WHITE);
        snackBarText.setTextSize(15);
        // set text to center
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            snackBarText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            snackBarText.setGravity(Gravity.CENTER_HORIZONTAL);
        snackbar.show();
    }

    /**
     * Function Name: showSnackBar
     * Description : This function is used for showing Snack bar with message
     * and is provieded with action button
     *
     * @param mActivity
     * @param message
     */
    public static void showSnackBarWithActionButton(AppApplication mActivity
            , String message, final SnackBarActionButtonListener snackBarActionButtonListener) {
        Activity mInnnerActivity = mActivity.getCurrentActivity();
        View rootView = mInnnerActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundResource(R.drawable.gs_bg_snackbar);
        TextView snackBarText = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        snackBarText.setTextColor(Color.WHITE);
        snackBarText.setTextSize(15);
        // set text to center
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            snackBarText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            snackBarText.setGravity(Gravity.CENTER_HORIZONTAL);
        snackbar.setAction(mActivity.getString(R.string.strRetryCaption), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBarActionButtonListener.onClickOfSnackBarActionButtonView();
            }
        });
        snackbar.setDuration(4000);
        snackbar.show();
    }

    /**
     * Method Name : convertIntToHex
     * Description : This method is used for converting int value to hex string
     *
     * @param intValue
     * @return
     */
    public static String convertIntToHex(int intValue) {
        String hex_value = intValue < 0
                ? "-" + Integer.toHexString(-intValue)
                : Integer.toHexString(intValue);
        return hex_value;
    }

    /**
     * Method Name : hexStringToByteArray
     * Description : this method is used for converting hex string into byte array
     *
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Method Name : calculateTimeStamp
     * Description : This method is used for calculating the timestap according to difference
     * specified
     *
     * @param longCurrentTineStamp  : Current timestamp for the device
     * @param strDifferenceInMinute : difference for each record capturing in minute
     */
    public static long calculateTimeStamp(long longCurrentTineStamp, String strDifferenceInMinute) {
        int intDifference = Integer.parseInt(strDifferenceInMinute);
        long now = longCurrentTineStamp;
        long nowPlus5Minutes = now - TimeUnit.MINUTES.toMillis(intDifference);
        return nowPlus5Minutes;
    }

    /**
     * Get Data & time from timestamp
     *
     * @param timeStamp
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String convertTimeStampToDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";

        }
    }

    /**
     * Get Data & time from timestamp
     *
     * @param timeStamp
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String convertTimeStampToDateTime(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("MMM dd yyyy, hh:mm a (ZZZZ)"); // Jan 1 2016, 12:00PM (UTC+0:00)
            Date netDate = (new Date(timeStamp * 1000L));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /***************************************************************
     * Function NAme : convertTimeStampToTimeDate
     * Description : This function will convert timestamp into specific format.
     *
     * @param currentCalDate
     * @return
     */

    public static String convertTimeStampToTimeDate(Calendar currentCalDate) {
        try {
            String dayNumberSuffix = getDayNumberSuffix(currentCalDate.get(Calendar.DAY_OF_MONTH));
            showLog(TAG, " dayNumberSuffix : " + dayNumberSuffix);
            DateFormat sdf = new SimpleDateFormat("hh:mmaa MMM. dd'" + dayNumberSuffix + "'");
            long timestamp = currentCalDate.getTimeInMillis();
            Date newDate = new Date(timestamp);
            return sdf.format(newDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Method Name  : convertTimeStampToTimeDates
     * Description : Date Format JUL 14,2017 example is returned
     *
     * @param currentCalDate
     * @return
     */
    public static String convertTimeStampToTimeDates(Calendar currentCalDate) {
        try {
            DateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            long timestamp = currentCalDate.getTimeInMillis();
            Date newDate = new Date(timestamp);
            showLog(TAG, "sdf.format(newDate) : " + sdf.format(newDate));
            return sdf.format(newDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /********************************
     * Function Name : getDayNumberSuffix
     * Description : This function will get day number suffix.
     *
     * @param day
     * @return
     */
    private static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    /**
     * Method Name : getDate
     * Description : This method is used for converting the timestamp into current time and date
     *
     * @param timeStamp
     * @return
     */
    public static String getDate(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy, hh:mm aa");//mm/dd/yyyy
        Date startDate;
        String newDateString = "";
        try {
            startDate = new Date(timeStamp);
            newDateString = sdf.format(startDate);
        } catch (Exception e) {
            showErrorLog(TAG, " Exception error : " + e);
        }
        showLog(TAG, "Formatted Date:" + newDateString);
        return newDateString;
    }

    /**
     * Method Name : getDate
     * Description : This method is used for converting the timestamp into current time and date
     *
     * @param timeStamp
     * @return
     */
    public static String getOnlyDate(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");//"MMM dd yyyy" for 1 Jan 2018
        Date startDate;
        String newDateString = "";
        try {
            startDate = new Date(timeStamp);
            newDateString = sdf.format(startDate);
        } catch (Exception e) {
            showErrorLog(TAG, " Exception error : " + e);
        }
        showLog(TAG, "Formatted Date:" + newDateString);
        return newDateString;
    }

    /**
     * Method Name : getDate
     * Description : This method is used for converting the timestamp into current time and date
     *
     * @param timeStamp
     * @return
     */
    public static String getTime(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");//mm/dd/yyyy
        Date startDate;
        String newDateString = "";
        try {
            startDate = new Date(timeStamp);
            newDateString = sdf.format(startDate);
        } catch (Exception e) {
            showErrorLog(TAG, " Exception error : " + e);
        }
        showLog(TAG, "Formatted Date:" + newDateString);
        return newDateString;
    }

    /**
     * Method Name : hex2decimal
     * Description : This method is used for converting the hexa decimal value in to decimal value
     *
     * @param s
     * @return
     */
    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    /**
     * Method Name : reverseByteArray
     * Description : This method is use for reversing the byte array
     *
     * @param array
     */
    public static void reverseByteArray(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * Function Name :customAlertDialogWithGradiantBtn
     * Description : This function will show custom alert dialog box with colored text button.
     *
     * @param mActivity                   : Activity instance
     * @param title                       : Dialog title
     * @param boolIsNeedToShowTitle       ; Boolean variable for managing visibility of title
     * @param mStrMsg                     : String object that holds message that need to be shown on dialog
     * @param boolIsNeedToMessage         : Boolean variable for managing the visibility dialog message
     * @param btnMsg                      : String object for holding the text to be shown on button
     * @param boolIsNeedToShowOKBtn       : Boolean variable for managing visibility of Ok button
     * @param mChoiceDialogClickListener  : ChoiceDialogClickListener for managing the action perform on OK and cancel button
     * @param boolIsNeedToShowCrossButton : Boolean variable for managing visibility of Cross button
     */
    public static void customAlertDialogWithGradiantBtn(
            final Activity mActivity, final String title, final boolean boolIsNeedToShowTitle,
            final String mStrMsg, final boolean boolIsNeedToMessage, final String btnMsg, final boolean boolIsNeedToShowOKBtn,
            final ChoiceDialogClickListener mChoiceDialogClickListener, final boolean boolIsNeedToShowCrossButton) {


        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView mBtnClose;
                TextView mTvMessage, title_tv;
                View horizontalLineView;
                Button mBtnPositive;
                String mStrMessage = mStrMsg;
                String mStrTitleMessage = title;
                boolean boolIsNeedToShow = true;
                if (TextUtils.isEmpty(mStrTitleMessage))
                    mStrTitleMessage = mActivity.getResources().getString(R.string.app_name);
                if (mStrMessage.isEmpty())
                    mStrMessage = mActivity.getResources().getString(R.string.please_try_again);
                try {
                    if (gradiantDialog != null && gradiantDialog.isShowing()) {
                        showInfoLog(TAG, "dialog not  null and already displayed");
                /* Dialog is already showing */
                        if (mStrMessage.equalsIgnoreCase(old_message)) {
                            showInfoLog(TAG, "message repeat");
                            boolIsNeedToShow = false;
                        } else {
                            showInfoLog(TAG, "message not repeat");
                            old_message = mStrMessage;
                            if (!mActivity.isFinishing()) {
                                showLog(TAG, " Dissmissed already displayed dialog");
                                gradiantDialog.dismiss();
                                gradiantDialog = null;
                            }
                        }
                    }
                } catch (Exception e) {
                    showLog(TAG, "print exception : " + e.getMessage());
                }
                if (boolIsNeedToShow) {
                    gradiantDialog = null;
                    gradiantDialog = new Dialog(mActivity);
                    gradiantDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    gradiantDialog.setContentView(R.layout.custom_dialog);
                    gradiantDialog.setCancelable(false);
                    mBtnClose = (ImageView) gradiantDialog.findViewById(R.id.close_btn);
                    mTvMessage = (TextView) gradiantDialog.findViewById(R.id.msg);
                    title_tv = (TextView) gradiantDialog.findViewById(R.id.title);
                    mBtnPositive = (Button) gradiantDialog.findViewById(R.id.positive_btn);
                    horizontalLineView = (View) gradiantDialog.findViewById(R.id.horizontalLineView);
                    mTvMessage.setText(mStrMessage);
                    mBtnPositive.setText(btnMsg);
                    if (mStrTitleMessage.isEmpty())
                        mStrTitleMessage = mActivity.getResources().getString(R.string.app_name);
                    title_tv.setText(mStrTitleMessage);
                    //Manage Close Button Visibility
                    if (boolIsNeedToShowCrossButton) {
                        mBtnClose.setVisibility(View.VISIBLE);
                    } else {
                        mBtnClose.setVisibility(View.GONE);
                    }
                    //Manage Title TextView Visibility
                    if (boolIsNeedToShowTitle) {
                        title_tv.setVisibility(View.VISIBLE);
                    } else {
                        title_tv.setVisibility(View.GONE);
                    }
                    //Manage Ok Button Visibility
                    if (boolIsNeedToShowOKBtn) {
                        mBtnPositive.setVisibility(View.VISIBLE);
                    } else {
                        mBtnPositive.setVisibility(View.GONE);
                    }
                    //Manage Horizontal Line View Corresponding to Title TextView Visibility
                    if (boolIsNeedToShowTitle) {
                        horizontalLineView.setVisibility(View.VISIBLE);
                    } else {
                        horizontalLineView.setVisibility(View.GONE);
                    }
                    //Close Button action event mHandler
                    mBtnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Perform action corresponding to click event perform on close button
                            if (mChoiceDialogClickListener != null)
                                mChoiceDialogClickListener.onClickOfNegative();
                            //Dismiss Dialog when close button is clicked
                            if (gradiantDialog != null) {
                                gradiantDialog.dismiss();
                                gradiantDialog = null;
                            }
                            old_message = "";
                        }
                    });
                    mBtnPositive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Perform action corresponding to click event perform on OK button
                            if (mChoiceDialogClickListener != null)
                                mChoiceDialogClickListener.onClickOfPositive();
                            //Dismiss Dialog when close button is clicked
                            if (gradiantDialog != null) {
                                gradiantDialog.dismiss();
                                gradiantDialog = null;
                            }
                            old_message = "";
                        }
                    });
                    //Make Dialog Background Transparent
                    gradiantDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    //Make Dialog not cancelable/dismissible when user tab any were on screen
                    gradiantDialog.setCancelable(false);
                    //Check current activity is in foreground and dialog is already not displaying
                    if (!mActivity.isFinishing() && (gradiantDialog != null && !gradiantDialog.isShowing())) {
                        gradiantDialog.show();
                        showLog(TAG, " Showing gradient pop ");
                    }
                } else {
                    showLog(TAG, "Custom gradiantDialog is showing");
                }
            }
        });


    }

    /**
     * Method Name : getSpannedText
     * Description : This method is used for extracting the spanned text from the string
     *
     * @param text
     */
    public static Spanned getSpannedText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }

    /**
     * Method Name : removeTrailingZeros
     * Description : This method is used for removing zero from integer value
     * at end or at trailing position
     *
     * @param str
     * @return : Trimmed value or value excluding zero at end position
     */
    public static String removeTrailingZeros(java.lang.String str) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        int length, index;
        length = str.length();
        index = length - 1;
        for (; index >= 0; index--) {
            if (chars[index] != '0') {
                break;
            }
        }
        return (index == length - 1) ? str : str.substring(0, index + 1);
    }

    /**
     * Method Name : removeLeadingZeros
     * Description : This method is used for removing zero from integer value
     * at starting or at leading position
     *
     * @param str
     * @return : Trimmed value or value excluding zero at leading position
     */
    public static String removeLeadingZeros(java.lang.String str) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        int index = 0;
        for (; index < str.length(); index++) {
            if (chars[index] != '0') {
                break;
            }
        }
        return (index == 0) ? str : str.substring(index);
    }

    /**
     * Method Name : byteArrayCheckZero
     * Description : this method is used for checking the byte array content
     * for value zero
     *
     * @param array
     * @return
     */
    public static boolean byteArrayCheckZero(final byte[] array) {
        int hits = 0;
        for (byte b : array) {
            if (b != 0) {
                hits++;
            }
        }
        return (hits == 0);
    }

    /**
     * Method Name : dismissGradientDialog
     * Description : This method is used for dismissing the gradient dialog
     */
    public static void dismissGradientDialog() {
        if (gradiantDialog != null) {
            gradiantDialog.dismiss();
            gradiantDialog = null;
        }
    }
}
