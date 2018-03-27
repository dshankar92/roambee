package vvdn.in.ble_ota.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.Utils.GlobalKeys;
import vvdn.in.ble_ota.Utils.MyCountDownTimer;
import vvdn.in.ble_ota.adapter.DataLoggingListAdapter;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.adapter.StoredGroupListAdapter;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.blecontrols.ConnectionControl;
import vvdn.in.ble_ota.control.HeaderViewManager;
import vvdn.in.ble_ota.listener.BluetoothConnectionStateInterface;
import vvdn.in.ble_ota.listener.ChoiceDialogClickListener;
import vvdn.in.ble_ota.listener.DataLoggingListener;
import vvdn.in.ble_ota.listener.HeaderViewClickListener;
import vvdn.in.ble_ota.model.DataLoggingModel;
import vvdn.in.ble_ota.model.GroupConfigurationModel;
import vvdn.in.ble_ota.model.GroupIndexModel;

/**
 * Class Name : DataLoggingReadingActivity
 * Description : This class contains information of stored index there corresponding indexes on device.
 * Here we will also display the total number of group index in  dropdown.
 *
 * @author Durgesh-Shankar
 */

public class DataLoggingReadingActivity extends Activity {

    /**
     * Debuggable TAG
     */
    private String TAG = DataLoggingReadingActivity.class.getSimpleName();
    /**
     * Activity reference object
     */
    private Activity mActivity;
    /**
     * String reference object holding the  number of index to display in dropdown
     */
    private List<GroupIndexModel> mStoredGroupIndex = new ArrayList<>();
    /**
     * DataLoggingListener reference object
     */
    public static DataLoggingListener mDataLoggingListener;

    /**
     * Integer reference object for holding the total number of records
     */
    private int TOTAL_NUMBER_RECORDS = 0;
    /**
     * Integer reference object for handling the index of record to be retrieve next
     */
    private int intCounter = 1, intRestartingRecordDelay = 1, mIntRepeatCount = 0;
    /**
     * String reference object for handling the request made
     */
    private String strCurrentRequestType = GlobalKeys.DEVICE_CURRENT_INDEX_REQUEST, intGroupIndexCurrentlyFetching = AppHelper.NUMBER_ZERO;
    /**
     * String reference for holding the current index of record that is being read from the device
     */
    private String mStrCurrentRecordIndexSelected = AppHelper.BLANK_SPACE, mStrCurrentGroupIndex = AppHelper.BLANK_SPACE;
    /**
     * ArrayList<DataLoggingModel> reference object for holding the logs details
     */
    private ArrayList<DataLoggingModel> dataLoggingModelArrayList = new ArrayList<>();
    /**
     * ArrayList<DataLoggingModel> reference object for holding the logs details
     */
    private ArrayList<GroupIndexModel> mStoredGroupArrayList = new ArrayList<>();

    /**
     * RelativeLayout reference object for handling spinner item click
     */
    private RelativeLayout rlSpinnerListen;
    /**
     * DataLoggingListAdapter reference object
     */
    public static DataLoggingListAdapter mDataLoggingListAdapter;
    /**
     * StoredGroupListAdapter reference object
     */
    public StoredGroupListAdapter mStoredGroupListAdapter;
    /**
     * ListView reference object for showing logs
     */
    private ListView lvDataLogging;
    /**
     * Constant Byte that need to be appended starting of each write request for development
     */
    public static byte[] sendReadingToken = {(byte) 0x06, (byte) 0x04, (byte) 0x20, (byte) 0x20, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x01};

    /**
     * Constant Byte that need to be appended starting of each write request for development
     */
    public static byte[] sendReadingGroupToken = {(byte) 0x0a, (byte) 0x04, (byte) 0x20/*, (byte) 0x20*/, (byte) 0x00,
            (byte) 0x00, (byte) 0x00};
    /**
     * byteOldPacket reference  variable for holding the previously received packet data
     */
    private byte[] byteOldPacket = new byte[5], byteTemporaryPacket = new byte[5], byteTemporaryCurrentGroupPacket = new byte[6];
    /**
     * TextView reference object for showing error message
     */
    private TextView mTvErrorMessage, mtvStoredGroupIndexSelected, mtvSelectedGroupData;
    /**
     * ArrayList<String> reference object
     */
    private ArrayList<String> mArrayListRecordByteArray = new ArrayList<>();
    /**
     * Boolean reference object for handling read/write/notify operation
     */
    private boolean boolIsNotifyClicked = false, boolIsNotificationOn = false, boolIsInvalidPopupShowing = false,
            boolIsGroupIndexShowing = true, boolIsToGroupReadingInProcess = false, boolIsGroupIndexWriteSuccess = false;
    /**
     * ProgressBar reference object
     */
    private ProgressBar mProgressBar;
    /**
     * Long reference object for holding the time when the group index packet was received
     */
    private long longGroupIndexTimeStamp = 0L;
    /**
     * ArrayList<GroupIndexModel> reference object for holding the data for each group
     */
    private ArrayList<GroupIndexModel> mGroupIndexHexData = new ArrayList<>();
    /**
     * Integer for handling the count number of group fetched until now
     */
    private int intGroupIndexTimeStampCount = 0, intGroupIndexTotalCount = 0;
    /**
     * Relative Layout reference object
     */
    private RelativeLayout rLBeaconB1DataLoggingHeader, mRlSpinnerLayout, mRlGroupInfoLayout, rLBeaconB4DataLoggingHeader,
            rLBeaconB5DataLoggingHeader;
    /**
     * Integer reference object for handling the retry count for connection
     */
    private int intRetryCountForEstablishingConnection = 0;
    /**
     * List<byte[]> reference object
     */
    private List<byte[]> mUnOrderedReceivedByteArray = new ArrayList<>();
    /**
     * Integer reference object for total number of record saved
     */
    private int mIntTotalCountIndexSaved = 0, intStartIndexAfterRemainingByte = 0,
            intGroupBytePacketDivisionIndex = 0, intRecordRemainder = 0, intGroupRemainder = 0, intRecordBytePacketDivisionIndex = 0;
    /**
     * Byte Array for holding the previous byte chunk data
     */
    private byte[] mByteOldChunkReceived;
    /**
     * Byte Array for holding the remaining byte after byte packet division
     */
    byte[] byteRemainingRecord = new byte[7];
    byte[] byteRemainingGroup = new byte[7];
    /**
     * Boolean reference object for handling the requirement of new packet that need to be fetched
     * for completion of packet
     */
    private boolean boolIsNewPacketRequired = false;
    /**
     * Integer reference object for holding the total byte required and total byte received
     */
    private int mIntTotalByteReceived = 0, mIntTotalByteSaved = 0;
    /**
     * Integer reference object for keeping track of retry count for data fetching and
     * token value for next group that needs to be received
     */
    int intRetryBackCount = 0, intNextGroupDataNeedToReceive = 4;
    /**
     * MyCountDownTimer reference object for handling the fetching record data availability
     */
    private MyCountDownTimer myCountDownTimer;
    /**
     * GroupIndexModel reference object
     */
    private GroupIndexModel mGroupIndexModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_logging_reading_activity_listview);
        GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = true;
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (GlobalConstant.CONNECTED_STATE)
            GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = false;
        else
            GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
        initView();
        manageHeaderView();
        readNoOfIndex();

    }

    /**
     * Method Name : manageDataLoggingHeaderVisibility
     * Description : This method is used for managing the visibility of header on data logging detail screen
     *
     * @param boolMakeHeaderVisible
     */
    public void manageDataLoggingHeaderVisibility(boolean boolMakeHeaderVisible) {
        AndroidAppUtils.showInfoLog(TAG, "GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED : " + GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED);
        if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.startsWith(GlobalKeys.BEACON_TYPE_B1_CONNECTED)) {
            if (boolMakeHeaderVisible) {
                rLBeaconB1DataLoggingHeader.setBackgroundColor(mActivity.getResources().getColor(R.color.yellow_70));
                rLBeaconB1DataLoggingHeader.setVisibility(View.VISIBLE);
                mRlGroupInfoLayout.setVisibility(View.VISIBLE);
                mRlSpinnerLayout.setVisibility(View.GONE);
            } else {
                rLBeaconB1DataLoggingHeader.setVisibility(View.GONE);
                mRlSpinnerLayout.setVisibility(View.VISIBLE);
                mRlGroupInfoLayout.setVisibility(View.GONE);
            }
        } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.startsWith(GlobalKeys.BEACON_TYPE_B4_CONNECTED)) {
            if (boolMakeHeaderVisible) {
                rLBeaconB4DataLoggingHeader.setBackgroundColor(mActivity.getResources().getColor(R.color.yellow_70));
                rLBeaconB4DataLoggingHeader.setVisibility(View.VISIBLE);
                mRlGroupInfoLayout.setVisibility(View.VISIBLE);
                mRlSpinnerLayout.setVisibility(View.GONE);
            } else {
                rLBeaconB4DataLoggingHeader.setVisibility(View.GONE);
                mRlSpinnerLayout.setVisibility(View.VISIBLE);
                mRlGroupInfoLayout.setVisibility(View.GONE);
            }
        } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.startsWith(GlobalKeys.BEACON_TYPE_B5_CONNECTED)) {
            if (boolMakeHeaderVisible) {
                rLBeaconB5DataLoggingHeader.setBackgroundColor(mActivity.getResources().getColor(R.color.yellow_70));
                rLBeaconB5DataLoggingHeader.setVisibility(View.VISIBLE);
                mRlGroupInfoLayout.setVisibility(View.VISIBLE);
                mRlSpinnerLayout.setVisibility(View.GONE);
            } else {
                rLBeaconB5DataLoggingHeader.setVisibility(View.GONE);
                mRlSpinnerLayout.setVisibility(View.VISIBLE);
                mRlGroupInfoLayout.setVisibility(View.GONE);
            }
        }
    }

    /*******************************************************************
     * Function name - manageHeaderView
     * Description - manage the initialization, visibility and click
     * listener of view fields on Header view
     *******************************************************************/
    public void manageHeaderView() {
        HeaderViewManager.getInstance().InitializeMultiTitleHeaderView(mActivity, null, false, manageHeaderClick());
        HeaderViewManager.getInstance().setHeading(true, mActivity.getResources().getString(R.string.strDataLoggingCaption), mActivity);
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
    public void onBackPressed() {
        if (!GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS) {

            AndroidAppUtils.showLog(TAG, " boolIsGroupIndexShowing : " + boolIsGroupIndexShowing);
            /**
             * Check for record item list View visibility
             * If Record List View is visible then redirect user to group index list View
             */
            if (!boolIsGroupIndexShowing) {
                manageDataLoggingHeaderVisibility(false);
                HeaderViewManager.getInstance().setHeading(true, mActivity.getResources().getString(R.string.strDataLoggingCaption));
                HeaderViewManager.getInstance().setHeadingTextSize(22);
                if (mDataLoggingListAdapter != null) {
                    dataLoggingModelArrayList = new ArrayList<>();
                    mDataLoggingListAdapter.setListData(dataLoggingModelArrayList);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                    mDataLoggingListAdapter = null;
                }
                if (mProgressBar.getVisibility() == View.VISIBLE)
                    mProgressBar.setVisibility(View.INVISIBLE);
                if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED && DeviceAdapter.mConnectionControl != null)
                    enableDisableNotification(false);
                GlobalConstant.BOOL_READING_TYPE_IN_PROCESS = "";
                setDataOnGroupIndexListView();
                boolIsGroupIndexShowing = true;
                mIntTotalByteSaved = 0;
                mIntTotalByteReceived = 0;
                intStartIndexAfterRemainingByte = 0;
                intRecordRemainder = 0;
                byteRemainingRecord = new byte[7];
                byteRemainingGroup = new byte[7];
                mByteOldChunkReceived = new byte[20];
                intRecordBytePacketDivisionIndex = 0;
                intGroupBytePacketDivisionIndex = 0;
            } else {
                if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED && DeviceAdapter.mConnectionControl != null)
                    enableDisableNotification(false);
                GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
                GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                unRegisterAllListener();
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                super.onBackPressed();
            }
        } else {
            /**
             * Check if user has pressed back button more than twice than stop all operation
             * and navigate back to previous screen.
             */
            if (intRetryBackCount == 1) {
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                intRetryBackCount = 0;
            }
            intRetryBackCount++;
            String strMessage = "Either Group or Record Fetching Is In Process";
            if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_GROUP_READING)) {
                strMessage = "Group information fetching is in process";
            } else if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_RECORD_READING)) {
                strMessage = "Record information fetching is in process";
            }
            AndroidAppUtils.showSnackBar(AppApplication.getInstance(), strMessage);
        }

    }

    /**
     * Method Name : manageListVisibility
     * Description : Make List visible if data is there otherwise show message
     *
     * @param boolMakeListVisible
     */
    private void manageListVisibility(final boolean boolMakeListVisible) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (boolMakeListVisible) {
                    lvDataLogging.setVisibility(View.VISIBLE);
                    mTvErrorMessage.setVisibility(View.INVISIBLE);
                } else {
                    lvDataLogging.setVisibility(View.INVISIBLE);
                    mTvErrorMessage.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /**
     * Method Name : initView
     * Description : This method is used for initialization of reference or instance object
     */
    private void initView() {
        GlobalConstant.BOOL_IS_NEED_TO_RETRIEVE_DATA = false;
        mActivity = DataLoggingReadingActivity.this;
        GlobalConstant.mGlobalActivityArrayList.add(mActivity);
        mTvErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
        mTvErrorMessage.setVisibility(View.INVISIBLE);
        rLBeaconB1DataLoggingHeader = (RelativeLayout) findViewById(R.id.rLBeaconB1DataLoggingHeader);
        rLBeaconB4DataLoggingHeader = (RelativeLayout) findViewById(R.id.rLBeaconB4DataLoggingHeader);
        rLBeaconB5DataLoggingHeader = (RelativeLayout) findViewById(R.id.rLBeaconB5DataLoggingHeader);
        mRlSpinnerLayout = (RelativeLayout) findViewById(R.id.rlSpinnerLayout);
        mtvStoredGroupIndexSelected = (TextView) findViewById(R.id.tvStoredGroupIndexSelected);
        mRlGroupInfoLayout = (RelativeLayout) findViewById(R.id.rlGroupInfoLayout);
        mtvSelectedGroupData = (TextView) findViewById(R.id.tvSelectedGroupData);
        lvDataLogging = (ListView) findViewById(R.id.lvDataLogging);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        /**
         *
         * Stored Group Adapter
         */
        mStoredGroupListAdapter = new StoredGroupListAdapter(mActivity, mGroupIndexHexData);
        lvDataLogging.setAdapter(mStoredGroupListAdapter);
        lvDataLogging.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                if (boolIsGroupIndexShowing) {
                    if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                        enableDisableNotification(false);
                    AndroidAppUtils.showInfoLog(TAG, "mGroupIndexHexData.size() : " + mGroupIndexHexData.size() +
                            "\n position : " + position);
                    new Handler(Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (mGroupIndexHexData.size() > position) {
                                        GroupIndexModel strSelectedData = mGroupIndexHexData.get(position);
                                        GlobalConstant.RECORD_NEED_TO_BE_RECEIVE = strSelectedData.getStrGroupIndex();
                                        GlobalConstant.TOTAL_RECORD_FOR_CURRENT_GROUP_SELECTED = Integer.parseInt(strSelectedData.getStrGroupTotalRecord().isEmpty() ? AppHelper.NUMBER_ZERO : strSelectedData.getStrGroupTotalRecord());
                                        TOTAL_NUMBER_RECORDS = GlobalConstant.TOTAL_RECORD_FOR_CURRENT_GROUP_SELECTED;
                                        AndroidAppUtils.showLog(TAG, "*************** strSelectedData ***************** " + strSelectedData.getStrGroupIndex());
                                        mProgressBar.setVisibility(View.VISIBLE);
                                        intGroupIndexCurrentlyFetching = mStrCurrentGroupIndex = strSelectedData.getStrGroupIndex();

                                        /**
                                         * Set data information for currently selected group
                                         */
                                        setCurrentGroupData(position);

                                        /**
                                         * Initialize the value to default for ech index selected by the user
                                         */
                                        setValueToDefault();
                                        manageDataLoggingHeaderVisibility(true);


                                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = true;
                                        boolIsToGroupReadingInProcess = false;
                                        boolIsGroupIndexShowing = false;
                                        //reset the adapter for setting record data
                                        mStoredGroupListAdapter = null;
                                        if (mDataLoggingListAdapter == null) {
                                            mDataLoggingListAdapter = new DataLoggingListAdapter(mActivity, dataLoggingModelArrayList);
                                            lvDataLogging.setAdapter(mDataLoggingListAdapter);
                                            mDataLoggingListAdapter.notifyDataSetChanged();
                                        } else {
                                            lvDataLogging.setAdapter(mDataLoggingListAdapter);
                                            mDataLoggingListAdapter.notifyDataSetChanged();
                                            AndroidAppUtils.showLog(TAG, "mDataLoggingListAdapter is already initialized ");
                                        }
                                        if (TOTAL_NUMBER_RECORDS != 0) {
                                            GlobalConstant.BOOL_READING_TYPE_IN_PROCESS = GlobalKeys.OPERATION_TYPE_RECORD_READING;
                                            mIntTotalByteReceived = 0;
                                            GlobalConstant.INT_CURRENT_RECORD_INDEX_BEFORE_FAIL = 1;
                                            retrieveDataAccordingToGroupIndexSelected(strSelectedData.getStrGroupIndex(), GlobalConstant.ONE_SECOND_DELAY_DURATION / 2);
                                        } else {
                                            //No record found for selected group
                                            AndroidAppUtils.showInfoLog(TAG, "No record found for this index");
                                            if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                                                enableDisableNotification(false);
                                            mTvErrorMessage.setText(mActivity.getResources().getString(R.string.strNoRecordFound));
                                            GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            manageListVisibility(false);
                                        }

                                    }

                                }
                            }, 300);
                } else {
                    AndroidAppUtils.showLog(TAG, " Record Item are shown ");
                }
            }
        });
        clearCacheForPreviousDevice();
    }

    /**
     * Method Name : setCurrentGroupData
     * Description : This method is used for fetching the currently selected group
     * info and setting data for data logging header
     *
     * @param position
     */
    private void setCurrentGroupData(int position) {
        List<GroupIndexModel> mGroupIndexModelArrayList = mStoredGroupListAdapter != null ? mStoredGroupListAdapter.getGroupArrayList() : new ArrayList<GroupIndexModel>();
        if (mGroupIndexModelArrayList != null && mGroupIndexModelArrayList.size() > 0) {
            GroupIndexModel groupIndexModel = mStoredGroupListAdapter.getGroupArrayList().get(position);

            if (groupIndexModel != null) {
                if (groupIndexModel.getStrGroupIndex() != null) {
                    int intStorageInterval = 5;
                    GroupConfigurationModel groupConfigurationModel = AppApplication.getInstance().getLastConfiguration(GlobalConstant.DEVICE_MAC);
                    if (groupConfigurationModel != null && groupConfigurationModel.getStrDataLoggingInterval() != null && !TextUtils.isEmpty(groupConfigurationModel.getStrDataLoggingInterval())) {
                        intStorageInterval = Integer.parseInt(groupConfigurationModel.getStrDataLoggingInterval());
                    }
                    //Current Index and Current Time Stamp
                    /**
                     *  GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE
                     *  GlobalConstant.longGroupIndexTimeStamp
                     */
                    AndroidAppUtils.showLog(TAG, " GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE : " + GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE +
                            "\n GlobalConstant.longGroupIndexTimeStamp : " + AndroidAppUtils.getDate(GlobalConstant.longGroupIndexTimeStamp));
                    //Current Record Index and Current  Record Time Stamp
                    /**
                     *  intTotalRecordCount
                     *  GlobalConstant.longGroupIndexTimeStamp
                     */
                    int intTotalRecordCount = Integer.parseInt(groupIndexModel.getStrGroupTotalRecord());
                    int intCurrentCount = Integer.parseInt(groupIndexModel.getStrCurrentCounter());
                    int intCurrentRecordEndTime = GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE - intCurrentCount;
                    long intCurrentEnd = intCurrentRecordEndTime * intStorageInterval;//intCurrentRecordEndTime * 1;
                    AndroidAppUtils.showLog(TAG, "intCurrentRecordEndTime : " +
                            AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp,
                                    intCurrentEnd + "")));
                    long longCurrentRecordEndTime = AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp,
                            intCurrentEnd + "");
                    int intDifferenceCountValue = intTotalRecordCount;
                    int intCurrentRecordStartTime = intDifferenceCountValue * intStorageInterval;
                    AndroidAppUtils.showLog(TAG, "intCurrentRecordStartTime : " +
                            AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(longCurrentRecordEndTime,
                                    intCurrentRecordStartTime + "")));

                    String strTimeStampData = AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(longCurrentRecordEndTime,
                            intCurrentRecordStartTime + "")) + AppHelper.BLANK_SPACE + AppHelper.HYPHEN + AppHelper.BLANK_SPACE + AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp,
                            intCurrentEnd + "")) + AppHelper.OPENING_BRACKET + AppHelper.BLANK_SPACE + mActivity.getResources().getString(R.string.mStrTotalRecord) + AppHelper.BLANK_SPACE + AppHelper.DOUBLE_COLON + intTotalRecordCount + AppHelper.BLANK_SPACE + AppHelper.CLOSING_BRACKET +
                            AppHelper.OPENING_BRACKET + AppHelper.BLANK_SPACE + mActivity.getResources().getString(R.string.mStrStorageInterval) + AppHelper.BLANK_SPACE + AppHelper.DOUBLE_COLON + intStorageInterval + AppHelper.BLANK_SPACE + AppHelper.CLOSING_BRACKET;
                    mtvSelectedGroupData.setText(strTimeStampData);

                }


            } else {
                AndroidAppUtils.showLog(TAG, " groupIndexModel is null");
            }
        } else {
            AndroidAppUtils.showInfoLog(TAG, " mGroupIndexModelArrayList is null or empty");
        }
    }

    /**
     * Method Name : clearCacheForPreviousDevice
     * Description : this method is used for clear the hashmap of record of old device if
     * new device connected is not same to old device mac address
     */
    private void clearCacheForPreviousDevice() {
        if (!GlobalConstant.DEVICE_MAC.toLowerCase().equalsIgnoreCase(GlobalConstant.OLD_DEVICE_MAC.toLowerCase())) {
            GlobalConstant.OLD_DEVICE_MAC = GlobalConstant.DEVICE_MAC;
            AppApplication.getInstance().clearRecordHashMap();
        } else {
            AndroidAppUtils.showLog(TAG, " Same Device Connected");
        }
    }

    /**
     * Method Name : isValueInCache
     * Description : This method is used for fetching saved logging data if present otherwise get data directly from the device
     * corresponding to selected index
     *
     * @param strSelectedData
     * @return
     */
    private boolean isValueInCache(String strSelectedData) {
        boolean boolIsPresentInCache = false;
        if (!strSelectedData.isEmpty()) {
            ArrayList<DataLoggingModel> savedDataLoggingModelArrayList = AppApplication.getInstance().getArrayListLinkedHashMap(strSelectedData);
            if (savedDataLoggingModelArrayList != null && savedDataLoggingModelArrayList.size() > 0) {
                if (mDataLoggingListAdapter != null) {
                    AndroidAppUtils.hideProgressDialog();
                    manageListVisibility(true);
                    mDataLoggingListAdapter.setListData(savedDataLoggingModelArrayList);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                } else {
                    AndroidAppUtils.showLog(TAG, "mDataLoggingListAdapter is null");
                }
                boolIsPresentInCache = true;

            } else {
                AndroidAppUtils.showLog(TAG, " savedDataLoggingModelArrayList is null or size is zero");
            }
        }
        return boolIsPresentInCache;
    }

    /**
     * Method Name : setValueToDefault
     * Description : Initialize the value to default for ech index selected by the user
     */
    private void setValueToDefault() {

        /**
         * Need to clear data from listview so that record data can be set
         */
        if (mStoredGroupListAdapter != null) {
            mStoredGroupListAdapter.updateList(new ArrayList<GroupIndexModel>());
            mStoredGroupListAdapter.notifyDataSetChanged();
            mStoredGroupListAdapter = null;
        }
        if (mDataLoggingListAdapter != null) {
            dataLoggingModelArrayList = new ArrayList<>();
            mDataLoggingListAdapter.setListData(dataLoggingModelArrayList);
            mDataLoggingListAdapter.notifyDataSetChanged();
        }
        intCounter = 1;
        mArrayListRecordByteArray = new ArrayList<>();
        mTvErrorMessage.setVisibility(View.INVISIBLE);
        GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
    }

    /**
     * Method Name : retrieveDataAccordingToGroupIndexSelected
     * Description : This method is used for retrieving the records
     * corresponding to index selected
     *
     * @param strSelectedData
     */
    private void retrieveDataAccordingToGroupIndexSelected(String strSelectedData, int intWriteDelayDuration) {
        String intSelectedGroupIndex = AndroidAppUtils.convertIntToHex(Integer.parseInt(strSelectedData));// Integer.parseInt(String.valueOf(strSelectedData), 16);

        AndroidAppUtils.showLog(TAG, "********* Selected index in Hex ************* " + intSelectedGroupIndex);
        //Constant file id need to be send from the application to the device
        String strFileId = GlobalKeys.FILE_RECORD_ID_INDEX;
        if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_GROUP_READING)) {
            strFileId = GlobalKeys.FILE_RECORD_ID_INDEX;
            intSelectedGroupIndex = AndroidAppUtils.convertIntToHex(Integer.parseInt(strSelectedData));
        } else if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_RECORD_READING)) {
            strFileId = strSelectedData;
            intSelectedGroupIndex = GlobalKeys.FILE_RECORD_ID_INDEX;
        }
        byte[] byteFileId = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(AndroidAppUtils.convertIntToHex(Integer.parseInt(strFileId))));
        byte[] byteSelectedData = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(intSelectedGroupIndex));
        final byte[] mRecordByte = new byte[byteFileId.length + byteSelectedData.length];
        System.arraycopy(byteFileId, 0, mRecordByte, 0, byteFileId.length);
        System.arraycopy(byteSelectedData, 0, mRecordByte, byteFileId.length, byteSelectedData.length);
        if (isDeviceConnected()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, mRecordByte);
                }
            }, intWriteDelayDuration);
            /**
             * Since garbage data is received for group info inbetween so disabling the notification
             */
            if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                enableDisableNotification(false);
            if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_GROUP_READING)) {
                strCurrentRequestType = GlobalKeys.DEVICE_ALL_GROUP_INFORMATION_REQUEST;
            } else if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_RECORD_READING)) {
                strCurrentRequestType = GlobalKeys.DEVICE_NEXT_RECORD_FETCH_REQUEST;
//                strCurrentRequestType = GlobalKeys.DEVICE_SELECTED_INDEX_REQUEST;
            }
        } else {
            AndroidAppUtils.showLog(TAG, " Device is disconnected .Please connect the device");
            AndroidAppUtils.showLog(TAG, "Connection is broken with device mac address : " + GlobalConstant.DEVICE_MAC);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActivity != null) {
            AppApplication.getInstance().setCurrentActivityReference(mActivity);
        }
        manageBluetoothConnectionSetupListener();
        manageDataLoggingListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterAllListener();
        unRegisterAllStaticListener();
    }

    /**
     * Method Name : unRegisterAllListener
     * Description : This method is used for un registering
     * all the listener for avoiding the memory leak
     */
    private void unRegisterAllListener() {
        /**
         * Disconnect with the device once user(we) travel back or switch to scanning screen
         */
        if (DeviceAdapter.mConnectionControl != null && !GlobalConstant.CONNECTED_STATE) {
            DeviceAdapter.mConnectionControl.UnregisterAllServices();
            DeviceAdapter.mConnectionControl.UnregisterUnBindAll();
        }
    }

    /**
     * Method Name : manageDataLoggingListener
     * Description : This method is used for listening the status
     * for read/write and notification receive from the device connected
     */
    private void manageDataLoggingListener() {
        mDataLoggingListener = new DataLoggingListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void OnNotificationReceived(byte[] mByteDataReceived) {
                AndroidAppUtils.showLog(TAG, "\n byte to hex data : " + AndroidAppUtils.convertToHexString(mByteDataReceived) +
                        "\tstrCurrentRequestType : " + strCurrentRequestType);
                if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED) {
                    switch (strCurrentRequestType) {
                        case GlobalKeys.DEVICE_CURRENT_INDEX_REQUEST:
                            AndroidAppUtils.hideProgressDialog();
                            mProgressBar.setVisibility(View.VISIBLE);
                            retrieveDataDeviceCurrentIndex(mByteDataReceived);


                            break;

                        case GlobalKeys.DEVICE_ALL_GROUP_INFORMATION_REQUEST:
                            String strGroupInValidValue = "00080004000d0000000000000000002013000100";

                            /**
                             * Check the beacon type connected and accordingly parse the group data
                             */
                            if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B1_CONNECTED)) {
                                checkAndSaveGroupDataAccordingToBeaconType(7, mByteDataReceived, strGroupInValidValue);
                            } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B4_CONNECTED)) {
                                checkAndSaveGroupDataAccordingToBeaconType(10, mByteDataReceived, strGroupInValidValue);
                            } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B5_CONNECTED)) {
                                checkAndSaveGroupDataAccordingToBeaconType(10, mByteDataReceived, strGroupInValidValue);
                            }


                            break;
                        case GlobalKeys.DEVICE_NEXT_RECORD_FETCH_REQUEST:
                            strGroupInValidValue = "00080004000d0000000000000000002013000100";
                            /**
                             * Check the beacon type connected and accordingly parse the group data
                             */
                            if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B1_CONNECTED)) {
                                checkAndSaveRecordDataAccordingToBeaconType(7, mByteDataReceived, strGroupInValidValue);
                            } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B4_CONNECTED)) {
                                checkAndSaveRecordDataAccordingToBeaconType(10, mByteDataReceived, strGroupInValidValue);
                            } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B5_CONNECTED)) {
                                checkAndSaveRecordDataAccordingToBeaconType(10, mByteDataReceived, strGroupInValidValue);
                            }

                            break;

                        case GlobalKeys.DEVICE_SELECTED_INDEX_REQUEST:

                            /**
                             * User selected index from dropdown request is made to retrieve total number of record
                             * corresponding to selected index
                             */
//                        if (mByteDataReceived[3] == 0) {
//                            mByteDataReceived[3] = 4;
//                        }
                            if (mByteDataReceived != null && mByteDataReceived.length > 3) {
                                if ((mByteDataReceived[2] == 0 || mByteDataReceived[2] == -1) &&
                                        (mByteDataReceived[3] == 0 || mByteDataReceived[3] == -1)) {
                                    AndroidAppUtils.hideProgressDialog();
                                    if (boolIsToGroupReadingInProcess && mGroupIndexHexData != null && mGroupIndexHexData.size() > 0) {
                                        AndroidAppUtils.showInfoLog(TAG, " ArrayList Have element so not remove list View");
                                        fetchTotalRecordNumber(mByteDataReceived);
                                    } else {
                                        /**
                                         * If number of group index is greater then one but none of them
                                         * have been received then also make request for it from the device because
                                         * current packet may be dump or garbage packet like 00000000
                                         */
                                        if (boolIsToGroupReadingInProcess && intGroupIndexTotalCount > 0) {
                                            fetchTotalRecordNumber(mByteDataReceived);
                                        } else {
                                            AndroidAppUtils.showInfoLog(TAG, "No record found for this index");
                                            enableDisableNotification(false);
                                            mTvErrorMessage.setText(mActivity.getResources().getString(R.string.strNoRecordFound));
                                            GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            manageListVisibility(false);
                                        }
                                    }
                                } else {
                                    manageListVisibility(true);
                                    byteTemporaryPacket = mByteDataReceived;
                                    fetchTotalRecordNumber(mByteDataReceived);
                                }
                            } else {
                                AndroidAppUtils.showErrorLog(TAG, "byteData is null or byteData is empty");
                            }
                            break;
                        case GlobalKeys.DEVICE_SELECTED_INDEX_LAST_REQUEST:
                            /**
                             * Disable Notification and stop reading data
                             */
                            AndroidAppUtils.showLog(TAG, " LAST PACKET DATA READ SUCCESSFUL");
                            AndroidAppUtils.showLog(TAG, "OLD PACKET DATA : " + AndroidAppUtils.convertToHexString(byteOldPacket) +
                                    "\n new packet data : " + AndroidAppUtils.convertToHexString(mByteDataReceived) +
                                    "\n intCounter : " + intCounter +
                                    "\n  Integer.parseInt(String.valueOf(byteTemporaryPacket[3]) : "
                                    + Integer.parseInt(String.valueOf(byteTemporaryPacket[3])) +
                                    " \n  mArrayListRecordByteArray.size() : " + mArrayListRecordByteArray.size());
                            enableDisableNotification(false);
                            if (!Arrays.equals(byteOldPacket, mByteDataReceived)) {
                                byteOldPacket = mByteDataReceived;
                                dataLoggingModelArrayList = new ArrayList<>();
                                displayDataOnListView(mArrayListRecordByteArray);
//                            strCurrentRequestType = "";
                            }
                            break;
                    }
                }
            }


            @Override
            public void OnDataLoggingWriteCharacteristicsDiscovered(String status) {
                if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                    enableDisableNotification(false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * Check added for stopping currently going operation if user presses back button
                         */
                        AndroidAppUtils.showLog(TAG, "GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED : " + GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED);
                        if (!GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                            enableDisableNotification(true);
                    }
                }, 500);

                if (strCurrentRequestType.equalsIgnoreCase(GlobalKeys.DEVICE_CURRENT_INDEX_REQUEST)) {
                    /**
                     * Start a timer task and wait for response for 10 sec after that show message if
                     * response is not available or data logging has yet not started
                     */
                    myCountDownTimer.start();
                    myCountDownTimer.stringGeneratedForActionOccurred(mActivity.getResources().getString(R.string.strDataLoggingNotStarted));

                }
            }


        };
    }

    /**
     * Method Name : retrieveDataDeviceCurrentIndex
     * Description : This method is used for retrieving the current running index data
     *
     * @param mByteDataReceived
     */
    private void retrieveDataDeviceCurrentIndex(byte[] mByteDataReceived) {
        /**
         * Disable Notification after receiving the first packet
         */
        if (checkReceiveValue(mByteDataReceived)) {
            /***
             * Request made to retrieve current index of log from the device
             */
            if (mByteDataReceived != null && mByteDataReceived.length > 0) {
                AndroidAppUtils.showLog(TAG, "mFirstByteData length : " + mByteDataReceived.length);
                AndroidAppUtils.IterateOverByteArray(mByteDataReceived);
                GlobalConstant.longGroupIndexTimeStamp = System.currentTimeMillis();
                byte[] mFirstByteDataIndex = new byte[2];
                byte[] mTotalStoredRecords = new byte[2];
                byte[] mCurrentTotalRecords = new byte[2];
                byte[] mCurrentRollBackIndex = new byte[1];
                for (int i = 0; i < mByteDataReceived.length; i++) {
                    if (i == 0 || i == 1) {
                        mFirstByteDataIndex[i] = mByteDataReceived[i];
                    } else if (i == 2 || i == 3) {
                        mTotalStoredRecords[i - 2] = mByteDataReceived[i];
                    } else if (i == 4 || i == 5) {
                        mCurrentTotalRecords[i - 4] = mByteDataReceived[i];
                    } else if (i == 6) {
                        mCurrentRollBackIndex[i - 6] = mByteDataReceived[i];
                    }
                }
                AndroidAppUtils.showLog(TAG, "mFirstByteDataIndex : " + AndroidAppUtils.convertToHexString(mFirstByteDataIndex) +
                        "\n" + "mTotalStoredRecords : " + AndroidAppUtils.convertToHexString(mTotalStoredRecords) +
                        "\n" + "mCurrentTotalRecords : " + AndroidAppUtils.convertToHexString(mCurrentTotalRecords) +
                        "\n" + "mCurrentRollBackIndex : " + AndroidAppUtils.convertToHexString(mCurrentRollBackIndex));
                AndroidAppUtils.showLog(TAG,
                        "mTotalStoredRecords : " + Integer.parseInt(AndroidAppUtils.convertToHexString(mTotalStoredRecords), 16) +
                                "mCurrentTotalRecords using function  : " + Integer.parseInt(AndroidAppUtils.convertToHexString(mCurrentTotalRecords), 16));
                GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE = Integer.parseInt(AndroidAppUtils.convertToHexString(mCurrentTotalRecords), 16);
                GlobalConstant.CURRENT_ROLL_BACK_INDEX = Integer.parseInt(AndroidAppUtils.convertToHexString(mCurrentRollBackIndex), 16);
                /**
                 * Deducting one from  GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE as current counter received
                 * record is not being written on index received
                 */
//                                    if (GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE > 0) {
//                                        GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE -= 1;
//                                    }
                /**
                 * Current Group Index stored on which currently record writing is going on.
                 */
                GlobalConstant.INT_CURRENT_GROUP_INDEX = Integer.parseInt(AndroidAppUtils.convertToHexString(mFirstByteDataIndex), 16);
                /**
                 * If GlobalConstant.INT_CURRENT_GROUP_INDEX received is odd the deducting it by one as we need to
                 * read only even group
                 */
//                                    if (GlobalConstant.INT_CURRENT_GROUP_INDEX > 0 && GlobalConstant.INT_CURRENT_GROUP_INDEX % 2 != 0) {
//                                        GlobalConstant.INT_CURRENT_GROUP_INDEX -= 1;
//                                    }
                byteTemporaryCurrentGroupPacket = mByteDataReceived;
                mGroupIndexModel = new GroupIndexModel();
                mGroupIndexModel.setStrGroupIndex(Integer.parseInt(AndroidAppUtils.convertToHexString(mFirstByteDataIndex), 16) + "");
                mGroupIndexModel.setStrGroupTotalRecord((Integer.parseInt(AndroidAppUtils.convertToHexString(mTotalStoredRecords), 16) - 1) + "");
                mGroupIndexModel.setStrCurrentCounter(Integer.parseInt(AndroidAppUtils.convertToHexString(mCurrentTotalRecords), 16) + "");
                calculateTotalStoredGroupIndex(mFirstByteDataIndex);
            } else {
                AndroidAppUtils.showLog(TAG, "mFirstByteData is null or mFirstByteData length is zero");
            }
        } else {
            /**
             * Retrieve again value
             */
            AndroidAppUtils.showLog(TAG, "Wrong packet picked or no group index found");
            showErrorMessageForWrongInvalidValue(mByteDataReceived, mActivity.getResources().getString(R.string.strDataLoggingNotStarted));
        }
    }

    /**
     * Method Name :checkAndSaveGroupDataAccordingToBeaconType
     * Description : This method is used for extracting the packet according to beacon type and
     * packet partition value for group data
     *
     * @param intPartitionCount
     * @param mByteDataReceived
     * @param strGroupGarbageValue
     */
    private void checkAndSaveGroupDataAccordingToBeaconType(int intPartitionCount, byte[] mByteDataReceived, String strGroupGarbageValue) {
        if (!AndroidAppUtils.convertToHexString(mByteDataReceived).equalsIgnoreCase(strGroupGarbageValue) &&
                !Arrays.equals(mByteOldChunkReceived, mByteDataReceived)) {
            mUnOrderedReceivedByteArray.add(mByteDataReceived);
            AndroidAppUtils.showInfoLog(TAG, "mUnOrderedReceivedByteArray size : " + mUnOrderedReceivedByteArray.size());
            int intTotalByteRequired = intGroupIndexTotalCount * intPartitionCount;
            int intByteRemainder = intTotalByteRequired % 20;
//                                int intByteRequiredForTwentyMultiple = 20 - intByteRemainder;
//                                intTotalByteRequired = intTotalByteRequired + intByteRequiredForTwentyMultiple;


            AndroidAppUtils.showErrorLog(TAG, " Group intTotalByteRequired : " + intTotalByteRequired +
                    "\n mIntTotalByteReceived : " + mIntTotalByteReceived);
            if (mIntTotalByteReceived < intTotalByteRequired) {

                if (!Arrays.equals(mByteOldChunkReceived, mByteDataReceived)) {
                    AndroidAppUtils.showInfoLog(TAG, "last byte data : " + AndroidAppUtils.convertToHexString(mByteDataReceived));
                    boolIsNewPacketRequired = false;
                    checkAndSetDataToGroupListView(mByteDataReceived, intPartitionCount);
                    mByteOldChunkReceived = mByteDataReceived;
                    mIntTotalByteReceived += mByteDataReceived.length;//20;
                    if (mIntTotalByteReceived == intTotalByteRequired)
                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                }
            } else if (mIntTotalByteReceived == intTotalByteRequired) {
                //All record data received
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
            }
        }
    }

    /**
     * Method Name :checkAndSaveRecordDataAccordingToBeaconType
     * Description : This method is used for extracting the packet according to beacon type and
     * packet partition value for record data
     *
     * @param intPartitionCount
     * @param mByteDataReceived
     * @param strGroupGarbageValue
     */
    private void checkAndSaveRecordDataAccordingToBeaconType(int intPartitionCount, byte[] mByteDataReceived, String strGroupGarbageValue) {
        if (!Arrays.equals(mByteOldChunkReceived, mByteDataReceived)) {
            mUnOrderedReceivedByteArray.add(mByteDataReceived);
            AndroidAppUtils.showInfoLog(TAG, "mUnOrderedReceivedByteArray size : " + mUnOrderedReceivedByteArray.size());
            int intTotalByteRequired = TOTAL_NUMBER_RECORDS * intPartitionCount;
            int intByteRemainder = intTotalByteRequired % 20;
//                                int intByteRequiredForTwentyMultiple = 20 - intByteRemainder;
//                                intTotalByteRequired = intTotalByteRequired + intByteRequiredForTwentyMultiple;


            AndroidAppUtils.showErrorLog(TAG, " Record intTotalByteRequired : " + intTotalByteRequired +
                    "\n mIntTotalByteReceived : " + mIntTotalByteReceived);
            if (mIntTotalByteReceived < intTotalByteRequired) {

                if (!Arrays.equals(mByteOldChunkReceived, mByteDataReceived)) {
                    AndroidAppUtils.showInfoLog(TAG, "last byte data : " + AndroidAppUtils.convertToHexString(mByteDataReceived));
                    boolIsNewPacketRequired = false;
                    checkAndSetDataToRecordListView(mByteDataReceived, intPartitionCount);
                    mByteOldChunkReceived = mByteDataReceived;
                    mIntTotalByteReceived += mByteDataReceived.length;//20;
                    if (mIntTotalByteReceived == intTotalByteRequired)
                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;

                }
            } else if (mIntTotalByteReceived == intTotalByteRequired) {
                //All record data received
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
            }
        }
    }


    /**
     * Method Name : checkAndSetDataToGroupListView
     * Description : This method is used for verifying byte data and parse group data
     *
     * @param mByteDataReceived
     * @param intBytePacketLength
     */
    private void checkAndSetDataToGroupListView(byte[] mByteDataReceived, int intBytePacketLength) {
        if (mByteDataReceived != null) {

            int intRemainder = intBytePacketLength;
            int intGroups = -1;
            byte[] byteGroupArray = new byte[intBytePacketLength];

            if (mByteDataReceived.length > intBytePacketLength) {
                if (mByteDataReceived.length % intBytePacketLength == 0)
                    intGroups = mByteDataReceived.length / intBytePacketLength;
                else
                    intGroups = (mByteDataReceived.length / intBytePacketLength) + 1;
            } else {
                /**
                 * If mByteDataReceived.length is less then required packet length then it is remaining byte
                 * of previous byte array packet then we need it to consider it as one record to
                 * read final data
                 */
                intGroups = 1;

            }
            //When partition value is 10 then third packet start index should be zero
            intGroupBytePacketDivisionIndex = 0;
            while (intGroups > 0) {
                if (intGroupRemainder < intBytePacketLength) {
                    System.arraycopy(byteRemainingGroup, 0, byteGroupArray, 0, intGroupRemainder);
                    intStartIndexAfterRemainingByte = intGroupRemainder;
                    intRemainder = intRemainder - intGroupRemainder;
                }
                if (!boolIsNewPacketRequired) {
                    AndroidAppUtils.showErrorLog(TAG, "start index for copying bytes : " + intGroupBytePacketDivisionIndex +
                            "\n converted byteRemainingRecord array : " + AndroidAppUtils.convertToHexString(byteRemainingGroup) +
                            "\n intStartIndexAfterRemainingByte : " + intStartIndexAfterRemainingByte);
                    System.arraycopy(mByteDataReceived, intGroupBytePacketDivisionIndex, byteGroupArray, intStartIndexAfterRemainingByte, intRemainder);
                    if (mIntTotalByteSaved <= intGroupIndexTotalCount * intBytePacketLength) {
                        fetchTotalGroupNumber(byteGroupArray);
                    }
                    intGroupBytePacketDivisionIndex += intRemainder;
                    if (intRemainder < intBytePacketLength) {
                        intRemainder = intBytePacketLength;
                        intStartIndexAfterRemainingByte = 0;
                    }
                    if (mByteDataReceived.length % intBytePacketLength != 0 && mByteDataReceived.length - intGroupBytePacketDivisionIndex < intBytePacketLength) {
                        intGroupRemainder = mByteDataReceived.length - intGroupBytePacketDivisionIndex;
                        System.arraycopy(mByteDataReceived, intGroupBytePacketDivisionIndex, byteRemainingGroup, 0, intGroupRemainder);
                        intGroupBytePacketDivisionIndex = 0;
                        boolIsNewPacketRequired = true;
                    } else {
                        intGroupRemainder = intBytePacketLength;
                    }
                }
                intGroups--;
            }
        }
    }

    /**
     * Method Name : checkAndSetDataToRecordListView
     * Description : This method is used for verifying byte data and parse record data
     *
     * @param mByteDataReceived
     * @param intBytePacketLength
     */
    private void checkAndSetDataToRecordListView(byte[] mByteDataReceived, int intBytePacketLength) {
        if (mByteDataReceived != null) {

            int intRemainder = intBytePacketLength;
            int intRecords = -1;
            byte[] byteRecordArray = new byte[intBytePacketLength];

            if (mByteDataReceived.length > intBytePacketLength) {
                if (mByteDataReceived.length % intBytePacketLength == 0)
                    intRecords = mByteDataReceived.length / intBytePacketLength;
                else
                    intRecords = (mByteDataReceived.length / intBytePacketLength) + 1;
            } else {
                /**
                 * If mByteDataReceived.length is less then required packet length then it is remaining byte
                 * of previous byte array packet then we need it to consider it as one record to
                 * read final data
                 */
                intRecords = 1;
            }
            //When partition value is 10 then third packet start index should be zero
            //Starting index for new packet read should be from zero index
            intRecordBytePacketDivisionIndex = 0;
            while (intRecords > 0) {
                if (intRecordRemainder < intBytePacketLength) {
                    System.arraycopy(byteRemainingRecord, 0, byteRecordArray, 0, intRecordRemainder);
                    intStartIndexAfterRemainingByte = intRecordRemainder;
                    intRemainder = intRemainder - intRecordRemainder;

                }
                if (!boolIsNewPacketRequired) {
                    AndroidAppUtils.showErrorLog(TAG, "start index for copying bytes : " + intRecordBytePacketDivisionIndex +
                            "\n converted byteRemainingRecord array : " + AndroidAppUtils.convertToHexString(byteRemainingRecord) +
                            "\n intStartIndexAfterRemainingByte : " + intStartIndexAfterRemainingByte);
                    System.arraycopy(mByteDataReceived, intRecordBytePacketDivisionIndex, byteRecordArray, intStartIndexAfterRemainingByte, intRemainder);
                    if (mIntTotalByteSaved <= TOTAL_NUMBER_RECORDS * intBytePacketLength) {

                        switch (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED) {
                            case GlobalKeys.BEACON_TYPE_B1_CONNECTED:
                                saveBeaconB1DataLoggingData(byteRecordArray);
                                break;
                            case GlobalKeys.BEACON_TYPE_B4_CONNECTED:
                                saveBeaconB4DataLoggingData(byteRecordArray);
                                break;
                            case GlobalKeys.BEACON_TYPE_B5_CONNECTED:
                                saveBeaconB5DataLoggingData(byteRecordArray);
                                break;
                        }
                    }
                    intRecordBytePacketDivisionIndex += intRemainder;
                    if (intRemainder < intBytePacketLength) {
                        intRemainder = intBytePacketLength;
                        intStartIndexAfterRemainingByte = 0;
                    }
                    if (mByteDataReceived.length % intBytePacketLength != 0 && mByteDataReceived.length - intRecordBytePacketDivisionIndex < intBytePacketLength) {
                        intRecordRemainder = mByteDataReceived.length - intRecordBytePacketDivisionIndex;
                        System.arraycopy(mByteDataReceived, intRecordBytePacketDivisionIndex, byteRemainingRecord, 0, intRecordRemainder);
                        intRecordBytePacketDivisionIndex = 0;
                        boolIsNewPacketRequired = true;
                    } else {
                        intRecordRemainder = intBytePacketLength;
                    }
                }
                intRecords--;
            }
        }
    }

    @SuppressLint("NewApi")
    private void mFragmentUnOrderedListInToByteArray(List<byte[]> mUnOrderedReceivedByteArray) {

        if (mUnOrderedReceivedByteArray != null && mUnOrderedReceivedByteArray.size() > 0) {
            byte mUnOrderedByteReceived[] = new byte[mUnOrderedReceivedByteArray.size() * 20];
            for (int i = 0; i < mUnOrderedReceivedByteArray.size(); i++) {
                System.arraycopy(mUnOrderedReceivedByteArray.get(i), 0, mUnOrderedByteReceived, mIntTotalCountIndexSaved, mUnOrderedReceivedByteArray.get(i).length);
                AndroidAppUtils.showInfoLog(TAG, "mUnOrderedByteReceived length : " + mUnOrderedByteReceived.length +
                        " mIntTotalCountIndexSaved : " + mIntTotalCountIndexSaved);
                mIntTotalCountIndexSaved += mUnOrderedReceivedByteArray.get(i).length;
            }
            /**
             * Divide the sorted byte array into chunks of 7 byte each for beacon b1
             */
            int intTotalRecords = -1;
            if (mUnOrderedByteReceived.length % 7 == 0) {
                intTotalRecords = mUnOrderedByteReceived.length / 7;
            } else if ((mUnOrderedByteReceived.length % 7) != 0) {
                int intRemainder = mUnOrderedByteReceived.length % 7;
                AndroidAppUtils.showInfoLog(TAG, "mUnOrderedByteReceived.length / 7  : " + (mUnOrderedByteReceived.length / 7) +
                        "intRemainder : " + intRemainder);
                intTotalRecords = mUnOrderedByteReceived.length / 7 + 1;
            }
            AndroidAppUtils.showInfoLog(TAG, "intTotalRecords : " + intTotalRecords);
            int j = 0, i = 0;
            while (i < intTotalRecords - 1) {
                byte[] mByteRecords = new byte[7];
                System.arraycopy(mUnOrderedByteReceived, j, mByteRecords, 0, 7);
                saveBeaconB1DataLoggingData(mByteRecords);
                j += 7;
                i++;
            }
            AndroidAppUtils.showInfoLog(TAG, "intBytePacketDivisionIndex : " + j +
                    "i : " + i);
            if (i == intTotalRecords - 1) {
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
            }

        }
    }

    /**
     * Method Name : showErrorMessageForWrongInvalidValue
     * Description : This method is used for showing the error pop up when current index value in invalid
     *
     * @param mByteDataReceived
     */
    private void showErrorMessageForWrongInvalidValue(byte[] mByteDataReceived, final String strMessage) {
        if (mByteDataReceived != null) {
            String strInValidValue = AndroidAppUtils.convertToHexString(mByteDataReceived);

            if (((Integer.parseInt(String.valueOf(mByteDataReceived[1]), 10)) == 0 || strInValidValue.equalsIgnoreCase("0000000000000000000000000000000000000000")) && !boolIsInvalidPopupShowing/*strInValidValue.equalsIgnoreCase("000000000000")*/) {
                boolIsInvalidPopupShowing = true;
                enableDisableNotification(false);
                AndroidAppUtils.customAlertDialogWithGradiantBtn(mActivity, mActivity.getResources().getString(R.string.strInformationCaption), true, strMessage,
                        true, mActivity.getResources().getString(R.string.strCaptionOK), true, new ChoiceDialogClickListener() {
                            @Override
                            public void onClickOfPositive() {
                                mProgressBar.setVisibility(View.GONE);
                                boolIsInvalidPopupShowing = false;
                                GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                                if (strMessage.equalsIgnoreCase(mActivity.getResources().getString(R.string.strNoRecordFound))) {
                                    GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                                    onBackPressed();
                                } else {
                                    mActivity.finish();
                                }
                            }

                            @Override
                            public void onClickOfNegative() {
                                mProgressBar.setVisibility(View.GONE);
                                boolIsInvalidPopupShowing = false;
                                GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                                mActivity.finish();
                            }
                        }, true);
            }
        } else {
            AndroidAppUtils.showLog(TAG, " mByteDataReceived is null");
        }
    }

    /**
     * Method Name : enableDisableNotification
     * Description : this method is used for enabling/disabling the notification on data logging
     * characteristics
     *
     * @param enableDisableStatus
     */
    private void enableDisableNotification(boolean enableDisableStatus) {
        if (enableDisableStatus) {
            DeviceAdapter.mConnectionControl.enableNotification(ConnectionControl.dl_notification_characteristics);
        } else {
            DeviceAdapter.mConnectionControl.disableNotification(ConnectionControl.dl_notification_characteristics);
        }
    }

    /**
     * Method Name : displayDataOnListView
     * Description : This method is used for showing the record data on the listview corresponding
     * to index selected by the user from the drop downview
     *
     * @param arrayListRecordByteArray
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void displayDataOnListView(ArrayList<String> arrayListRecordByteArray) {
        if (arrayListRecordByteArray != null && arrayListRecordByteArray.size() > 0) {
            for (String strHexByteData : arrayListRecordByteArray) {
                byte[] mByteData = AndroidAppUtils.hexStringToByteArray(strHexByteData);
                switch (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED) {
                    case GlobalKeys.BEACON_TYPE_B1_CONNECTED:
                        saveBeaconB1DataLoggingData(mByteData);
                        break;
                    case GlobalKeys.BEACON_TYPE_B4_CONNECTED:
                        saveBeaconB4DataLoggingData(mByteData);
                        break;
                    case GlobalKeys.BEACON_TYPE_B5_CONNECTED:
                        saveBeaconB5DataLoggingData(mByteData);
                        break;
                }
            }
            //Save logging corresponding to index selected
            if (dataLoggingModelArrayList != null && dataLoggingModelArrayList.size() > 0)
                AppApplication.getInstance().setArrayListLinkedHashMap(mStrCurrentGroupIndex, dataLoggingModelArrayList);
        } else {
            AndroidAppUtils.showLog(TAG, " mArrayListRecordByteArray is null or size is zero");
        }
    }

    /**
     * Method Name : checkReceiveValue
     * Description : this method is used for checking the first byte as not null and not "00"
     *
     * @param mByteDataReceived
     * @return
     */
    private boolean checkReceiveValue(byte[] mByteDataReceived) {
        if (mByteDataReceived != null && mByteDataReceived.length > 0) {
            if (mByteDataReceived.length > 2 && ((Integer.parseInt(String.valueOf(mByteDataReceived[0]), 10)) != 0
                    || (Integer.parseInt(String.valueOf(mByteDataReceived[1]), 10)) != 0)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /***
     * Method Name : checkReceiveValue
     * Description : this method is used for checking the first byte as not null and not "00"
     *
     * @param mByteDataReceived
     * @param byteTemporaryCurrentGroupPacket
     * @return
     */
    private boolean checkRecordReceiveValue(byte[] mByteDataReceived, byte[] byteTemporaryCurrentGroupPacket) {
        try {
            AndroidAppUtils.showLog(TAG, "mByteDataReceived length : " + mByteDataReceived.length);
            if (mByteDataReceived != null && mByteDataReceived.length > 0) {
                if (mByteDataReceived.length > 2 && ((Integer.parseInt(String.valueOf(mByteDataReceived[1]), 10)) != (Integer.parseInt(String.valueOf(byteTemporaryCurrentGroupPacket[1]), 10))
                        || (mByteDataReceived.length > 2 && (Integer.parseInt(String.valueOf(mByteDataReceived[3]), 10)) != (Integer.parseInt(String.valueOf(byteTemporaryCurrentGroupPacket[3]), 10))))
                        || (mByteDataReceived.length > 2 && (Integer.parseInt(String.valueOf(mByteDataReceived[5]), 10)) != (Integer.parseInt(String.valueOf(byteTemporaryCurrentGroupPacket[5]), 10)))) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            AndroidAppUtils.showInfoLog(TAG, "error : " + e.getMessage());
        }
        return false;
    }

    /**
     * Method Name : fetchTotalRecordNumber
     * Description : This method is used for parsing the data corresponding to record data obtained
     *
     * @param byteData
     */
    private void fetchTotalGroupNumber(byte[] byteData) {
        AndroidAppUtils.showLog(TAG, " *********** fetchTotalRecordNumber ************* hex data : " + AndroidAppUtils.convertToHexString(byteData));
        //Assign this value for not including this in our list as it contain
        // information about group index and total number of record
        byteOldPacket = byteData;

        final byte[] byteRecordIndexData;
        if (byteData != null && byteData.length > 0) {
           /* 1.) Packet we receive after selection of index from spinner
            2.) Packet Structure 00 05 00 06 00 : Second Index indicate Group index on device
            and fourth index indicate Total number of records*/
            byte[] mByteGroupIndexFetchedFromDevice = new byte[2];
            byte[] mByteNoOfRecord = new byte[2];
            byte[] mByteCurrentCounter = new byte[2];
            for (int i = 0; i < byteData.length; i++) {

                if (i == 0 || i == 1) {
                    mByteGroupIndexFetchedFromDevice[i] = byteData[i];
                } else if (i == 2 || i == 3) {
                    mByteNoOfRecord[i - 2] = byteData[i];
                } else if (i == 4 || i == 5) {
                    mByteCurrentCounter[i - 4] = byteData[i];
                }
            }
            if (verifyForCorrectGroupRecordInformation(mByteGroupIndexFetchedFromDevice)) {
//                intGroupIndexCurrentlyFetching = AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice);
                byte[] byteGroupIndexData = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice)));
                String strRecordCurrentIndex = "";
                int intGroupIndex = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice), 16);
                int intTotalRecord = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteNoOfRecord), 16);
                int intCurrentCounter = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteCurrentCounter), 16);
                AndroidAppUtils.showLog(TAG, "****** Total number of record  : " + intTotalRecord + "" +
                        "  intGroupIndex : " + intGroupIndex + "  intCurrentCounter : " + intCurrentCounter +
                        "\n intCounter : " + intCounter + "\n intCounterValue  in hex : " + AndroidAppUtils.convertIntToHex(intCounter));
                TOTAL_NUMBER_RECORDS = intTotalRecord;
                //Only Need to Group Index , Total and Current Counter
                if (boolIsToGroupReadingInProcess) {
                    if (mGroupIndexHexData != null && !Arrays.equals(byteTemporaryCurrentGroupPacket, byteData)) {
                        GroupIndexModel groupIndexModel = new GroupIndexModel();
                        groupIndexModel.setStrGroupIndex(intGroupIndex + "");
                        groupIndexModel.setStrGroupTotalRecord(intTotalRecord + "");
                        groupIndexModel.setStrCurrentCounter(intCurrentCounter + "");
                        /**
                         * If current position group index matches current group index fetched then deduct
                         * the counter for that group by one
                         */
                        AndroidAppUtils.showInfoLog(TAG, " Before GlobalConstant.INT_CURRENT_GROUP_INDEX : " + GlobalConstant.INT_CURRENT_GROUP_INDEX +
                                "\n groupIndexModel.setStrCurrentCounter : " + groupIndexModel.getStrCurrentCounter());
                        if (GlobalConstant.INT_CURRENT_GROUP_INDEX == Integer.parseInt(groupIndexModel.getStrGroupIndex())) {
                            groupIndexModel.setStrCurrentCounter((Integer.parseInt(groupIndexModel.getStrCurrentCounter()) - 1) + "");
                        }
                        AndroidAppUtils.showInfoLog(TAG, "After GlobalConstant.INT_CURRENT_GROUP_INDEX : " + GlobalConstant.INT_CURRENT_GROUP_INDEX +
                                "\n groupIndexModel.setStrCurrentCounter : " + groupIndexModel.getStrCurrentCounter());
                        if (boolIsGroupIndexWriteSuccess) {

                            if (!mGroupIndexHexData.contains(groupIndexModel))
                                mGroupIndexHexData.add(groupIndexModel);
                            boolIsGroupIndexWriteSuccess = false;
                            byteTemporaryCurrentGroupPacket = byteData;
                        }
                        if (mGroupIndexHexData.size() < intGroupIndexTotalCount) {
                            mProgressBar.setVisibility(View.GONE);
                            intNextGroupDataNeedToReceive += 1;
                            addGroupIndex(groupIndexModel);
                            /**
                             * When size of list is equal to one decremented value of total group
                             * required then add the current running group data to the list
                             */
                            if (mGroupIndexHexData.size() == intGroupIndexTotalCount - 1) {
                                if (!mGroupIndexHexData.contains(mGroupIndexModel)) {
                                    addGroupIndex(mGroupIndexModel);
                                }
                            }

                        } else if (mGroupIndexHexData.size() == intGroupIndexTotalCount) {
                            mProgressBar.setVisibility(View.GONE);
                            setDataOnGroupIndexListView();
                            if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                                enableDisableNotification(false);
                            boolIsToGroupReadingInProcess = false;

                            GlobalConstant.BOOL_READING_TYPE_IN_PROCESS = "";
                            GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                        } else if (mGroupIndexHexData.size() > 0) {
                            mProgressBar.setVisibility(View.GONE);
                            setDataOnGroupIndexListView();
                        }
                    }

                    if (mGroupIndexHexData != null && mGroupIndexHexData.size() == intGroupIndexTotalCount) {
                        AndroidAppUtils.showLog(TAG, " mGroupIndexHexData size: " + mGroupIndexHexData.size());
                        if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                            enableDisableNotification(false);
                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                        //Group that need to be received next initialize it to 4 for starting fetch group information from
//                        group index 5
                        intNextGroupDataNeedToReceive = 4;
                    }
                }
            } else {
                AndroidAppUtils.showErrorLog(TAG, "********* WRONG RECORD DATA RECEIVED ***********");
                /**
                 * Need to stop performing operation for reading group data as invalid data '00000000000000000000000000000'
                 * is being received
                 */
                mProgressBar.setVisibility(View.GONE);
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                AndroidAppUtils.showSnackBar(AppApplication.getInstance(), " INVALID DATA RECEVIED ");
            }
        } else {
            AndroidAppUtils.showLog(TAG, "Received Data is null or empty");
        }
    }

    /**
     * Method Name : fetchTotalRecordNumber
     * Description : This method is used for parsing the data corresponding to record data obtained
     *
     * @param byteData
     */
    private void fetchTotalRecordNumber(byte[] byteData) {
        AndroidAppUtils.showLog(TAG, " *********** fetchTotalRecordNumber ************* hex data : " + AndroidAppUtils.convertToHexString(byteData));
        //Assign this value for not including this in our list as it contain
        // information about group index and total number of record
        byteOldPacket = byteData;

        final byte[] byteRecordIndexData;
        if (byteData != null && byteData.length > 0) {
           /* 1.) Packet we receive after selection of index from spinner
            2.) Packet Structure 00 05 00 06 00 : Second Index indicate Group index on device
            and fourth index indicate Total number of records*/
            byte[] mByteGroupIndexFetchedFromDevice = new byte[2];
            byte[] mByteNoOfRecord = new byte[2];
            byte[] mByteCurrentCounter = new byte[2];
            for (int i = 0; i < byteData.length; i++) {

                if (i == 0 || i == 1) {
                    mByteGroupIndexFetchedFromDevice[i] = byteData[i];
                } else if (i == 2 || i == 3) {
                    mByteNoOfRecord[i - 2] = byteData[i];
                } else if (i == 4 || i == 5) {
                    mByteCurrentCounter[i - 4] = byteData[i];
                }
            }
            if (verifyForCorrectGroupRecord(mByteGroupIndexFetchedFromDevice)) {
//                intGroupIndexCurrentlyFetching = AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice);
                byte[] byteGroupIndexData = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice)));
//                byte[] byteGroupIndexData = {(byte) 0x00, (byte) 0x0c};
                String strRecordCurrentIndex = "";
                int intGroupIndex = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice), 16);
                int intTotalRecord = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteNoOfRecord), 16);
                int intCurrentCounter = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteCurrentCounter), 16);
                AndroidAppUtils.showLog(TAG, "****** Total number of record  : " + intTotalRecord + "" +
                        "  intGroupIndex : " + intGroupIndex + "  intCurrentCounter : " + intCurrentCounter +
                        "\n intCounter : " + intCounter + "\n intCounterValue  in hex : " + AndroidAppUtils.convertIntToHex(intCounter));
                TOTAL_NUMBER_RECORDS = intTotalRecord;
                //Only Need to Group Index , Total and Current Counter
                if (boolIsToGroupReadingInProcess) {
                    if (mGroupIndexHexData != null && !Arrays.equals(byteTemporaryCurrentGroupPacket, byteData)) {
                        GroupIndexModel groupIndexModel = new GroupIndexModel();
                        groupIndexModel.setStrGroupIndex(intGroupIndex + "");
                        groupIndexModel.setStrGroupTotalRecord(intTotalRecord + "");
                        groupIndexModel.setStrCurrentCounter(intCurrentCounter + "");
                        /**
                         * If current position group index matches current group index fetched then deduct
                         * the counter for that group by one
                         */
                        AndroidAppUtils.showInfoLog(TAG, " Before GlobalConstant.INT_CURRENT_GROUP_INDEX : " + GlobalConstant.INT_CURRENT_GROUP_INDEX +
                                "\n groupIndexModel.setStrCurrentCounter : " + groupIndexModel.getStrCurrentCounter());
                        if (GlobalConstant.INT_CURRENT_GROUP_INDEX == Integer.parseInt(groupIndexModel.getStrGroupIndex())) {
                            groupIndexModel.setStrCurrentCounter((Integer.parseInt(groupIndexModel.getStrCurrentCounter()) - 1) + "");
                        }
                        AndroidAppUtils.showInfoLog(TAG, "After GlobalConstant.INT_CURRENT_GROUP_INDEX : " + GlobalConstant.INT_CURRENT_GROUP_INDEX +
                                "\n groupIndexModel.setStrCurrentCounter : " + groupIndexModel.getStrCurrentCounter());
                        if (boolIsGroupIndexWriteSuccess) {

                            if (!mGroupIndexHexData.contains(groupIndexModel))
                                mGroupIndexHexData.add(groupIndexModel);
                            boolIsGroupIndexWriteSuccess = false;
                            byteTemporaryCurrentGroupPacket = byteData;
                        }
                        if (mGroupIndexHexData.size() < intGroupIndexTotalCount) {
                            mProgressBar.setVisibility(View.GONE);
                            addGroupIndex(groupIndexModel);

                        } else if (mGroupIndexHexData.size() == intGroupIndexTotalCount) {
                            mProgressBar.setVisibility(View.GONE);

                            if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                                enableDisableNotification(false);

                            boolIsToGroupReadingInProcess = false;
                            if (!mGroupIndexHexData.contains(mGroupIndexModel))
                                mGroupIndexHexData.add(mGroupIndexModel);
                            setDataOnGroupIndexListView();
                            GlobalConstant.BOOL_READING_TYPE_IN_PROCESS = "";
                            GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                        } else if (mGroupIndexHexData.size() > 0) {
                            mProgressBar.setVisibility(View.GONE);
                            setDataOnGroupIndexListView();
                        }
                    }

                    if (mGroupIndexHexData != null && mGroupIndexHexData.size() == intGroupIndexTotalCount) {
                        AndroidAppUtils.showLog(TAG, " mGroupIndexHexData size: " + mGroupIndexHexData.size());
                        if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                            enableDisableNotification(false);
                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                    }
                } else {
                    /**
                     *   Record Index starts from 1 not from "0" so initializing the intCounter=1 and condition that needs to
                     be satisfy is less then equal
                     */
//                    if (TOTAL_NUMBER_RECORDS == 0) {
//                        TOTAL_NUMBER_RECORDS = 1;
//                    }
                    if (intCounter <= TOTAL_NUMBER_RECORDS) {

                        AndroidAppUtils.showInfoLog(TAG, "intCounter : " + intCounter + " AndroidAppUtils.convertIntToHex(intCounter) : " + AndroidAppUtils.convertIntToHex(intCounter));
                        strRecordCurrentIndex = AndroidAppUtils.appendNoOfZeroIfRequired(AndroidAppUtils.convertIntToHex(intCounter));
                        byte[] byteCurrentRecordIndexData = AndroidAppUtils.verifyForMoreThanTwoDigit(strRecordCurrentIndex);
                        byteRecordIndexData = new byte[byteGroupIndexData.length + byteCurrentRecordIndexData.length];

                        System.arraycopy(byteGroupIndexData, 0, byteRecordIndexData, 0, byteGroupIndexData.length);
                        System.arraycopy(byteCurrentRecordIndexData, 0, byteRecordIndexData, byteGroupIndexData.length, byteCurrentRecordIndexData.length);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mArrayListRecordByteArray = new ArrayList<>();
                                DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, byteRecordIndexData);
                            }
                        }, 1/*1000*/);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                strCurrentRequestType = GlobalKeys.DEVICE_NEXT_RECORD_FETCH_REQUEST;
                            }
                        }, 500);

                        intCounter++;
                    }
                }
            } else {
                AndroidAppUtils.showErrorLog(TAG, "********* WRONG RECORD DATA RECEIVED ***********");
                /**
                 * Need to stop performing operation for reading group data as invalid data '00000000000000000000000000000'
                 * is being received
                 */
                mProgressBar.setVisibility(View.GONE);
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                AndroidAppUtils.showSnackBar(AppApplication.getInstance(), " INVALID DATA RECEIVED ");
            }
        } else {
            AndroidAppUtils.showLog(TAG, "Received Data is null or empty");
        }
    }

    /**
     * Method Name : verifyForCorrectGroupRecord
     * Description : This method is used for verifying the correct record is received/fetched
     * from the device or not
     *
     * @param mByteGroupIndexFetchedFromDevice
     * @return
     */
    private boolean verifyForCorrectGroupRecord(byte[] mByteGroupIndexFetchedFromDevice) {
        boolean boolIsCorrectRecordDataReceive = false;
        if (mByteGroupIndexFetchedFromDevice != null && mByteGroupIndexFetchedFromDevice.length > 0) {
            int intRecordNeedToReceives = Integer.parseInt(GlobalConstant.RECORD_NEED_TO_BE_RECEIVE) + 4;
            int intRecordNeedToReceive = Integer.parseInt(AndroidAppUtils.convertIntToHex(intRecordNeedToReceives), 16);
            int intRecordReceived = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice), 16);
            AndroidAppUtils.showInfoLog(TAG, "GlobalConstant.RECORD_NEED_TO_BE_RECEIVE : " + GlobalConstant.RECORD_NEED_TO_BE_RECEIVE +
                    "\n strRecordNeedToReceive : " + intRecordNeedToReceive +
                    "\n strRecordReceived : " + intRecordReceived);
            if (intRecordNeedToReceive == intRecordReceived) {
                boolIsCorrectRecordDataReceive = true;
            } else {
                boolIsCorrectRecordDataReceive = false;
            }
        }
        return boolIsCorrectRecordDataReceive;
    }

    /**
     * Method Name : verifyForCorrectGroupRecord
     * Description : This method is used for verifying the correct record is received/fetched
     * from the device or not
     *
     * @param mByteGroupIndexFetchedFromDevice
     * @return
     */
    private boolean verifyForCorrectGroupRecordInformation(byte[] mByteGroupIndexFetchedFromDevice) {
        boolean boolIsCorrectRecordDataReceive = false;
        if (mByteGroupIndexFetchedFromDevice != null && mByteGroupIndexFetchedFromDevice.length > 0) {
            int intRecordNeedToReceives = Integer.parseInt(GlobalConstant.RECORD_NEED_TO_BE_RECEIVE) + intNextGroupDataNeedToReceive;
            int intRecordNeedToReceive = Integer.parseInt(AndroidAppUtils.convertIntToHex(intRecordNeedToReceives), 16);
            int intRecordReceived = Integer.parseInt(AndroidAppUtils.convertToHexString(mByteGroupIndexFetchedFromDevice), 16);
            AndroidAppUtils.showInfoLog(TAG, "GlobalConstant.RECORD_NEED_TO_BE_RECEIVE : " + GlobalConstant.RECORD_NEED_TO_BE_RECEIVE +
                    "\n strRecordNeedToReceive : " + intRecordNeedToReceive +
                    "\n strRecordReceived : " + intRecordReceived);
            if (intRecordNeedToReceive == intRecordReceived) {
                boolIsCorrectRecordDataReceive = true;
            } else {
                boolIsCorrectRecordDataReceive = false;
            }
        }
        return boolIsCorrectRecordDataReceive;
    }


    /**
     * Method Name : calculateTotalStoredGroupIndex
     * Description : This method is used for calculation of total stored group index
     *
     * @param mFirstByteDataIndexValue
     */
    private void calculateTotalStoredGroupIndex(byte[] mFirstByteDataIndexValue) {
        if (!String.valueOf(mFirstByteDataIndexValue).isEmpty()) {
            int intTotalStoredGroupIndexes =
                    Integer.parseInt(AndroidAppUtils.convertToHexString(mFirstByteDataIndexValue), 16) -
                            Integer.parseInt(String.valueOf(GlobalConstant.STORED_FIRST_INDEX), 16);
            AndroidAppUtils.showLog(TAG, " **********  intTotalStoredGroupIndexes ******** " + intTotalStoredGroupIndexes);
            if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                enableDisableNotification(false);
            setUpSpinnerItem(intTotalStoredGroupIndexes, Integer.parseInt(AndroidAppUtils.convertToHexString(mFirstByteDataIndexValue), 16));
        } else {
            AndroidAppUtils.showLog(TAG, "********** FIRST INDEX VALUE IS EMPTY ************* " + mFirstByteDataIndexValue);
        }
    }

    /**
     * Method Name : setUpSpinnerItem
     * Description : This method is used for setting the index into spinner
     *
     * @param intTotalStoredGroupIndexes
     * @param intCurrentlyRunningIndex
     */
    private void setUpSpinnerItem(int intTotalStoredGroupIndexes, int intCurrentlyRunningIndex) {
        intGroupIndexTotalCount = checkGroupCount(intTotalStoredGroupIndexes, intCurrentlyRunningIndex);

        for (int i = 0; i < mStoredGroupIndex.size(); i++) {
            AndroidAppUtils.showInfoLog(TAG, "index : " + mStoredGroupIndex.get(i).getStrGroupIndex());
        }
        /**
         * Reverse the list for fetching the currently going on group information top of the list
         */
        Collections.reverse(mStoredGroupIndex);
        mtvStoredGroupIndexSelected.setText(mActivity.getResources().getString(R.string.strTotalStoredGroupCaption)
                + AppHelper.BLANK_SPACE + AppHelper.DOUBLE_COLON + AppHelper.BLANK_SPACE + intGroupIndexTotalCount);
        if (intGroupIndexTotalCount == 0) {
            if (GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED)
                enableDisableNotification(false);
            myCountDownTimer.stringGeneratedForActionOccurred(mActivity.getResources().getString(R.string.strDataLoggingStarted));
            myCountDownTimer.cancel();
            AndroidAppUtils.customAlertDialogWithGradiantBtn(mActivity, mActivity.getResources().getString(R.string.strInformationCaption), true, "Data Logging has not started",
                    true, mActivity.getResources().getString(R.string.strCaptionOK), true, new ChoiceDialogClickListener() {
                        @Override
                        public void onClickOfPositive() {
                            mProgressBar.setVisibility(View.GONE);
                            GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                            mActivity.finish();
                        }

                        @Override
                        public void onClickOfNegative() {
                            mProgressBar.setVisibility(View.GONE);
                            GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                            mActivity.finish();
                        }
                    }, true);
        }
        /**
         * Currently first group index data logging writting is in process
         */
        else {
            myCountDownTimer.stringGeneratedForActionOccurred(mActivity.getResources().getString(R.string.strDataLoggingStarted));
            myCountDownTimer.cancel();
            /**
             * If currently writting group index is 5X1 then only group is formed.
             * Dont send 1X1 request as only one group.
             * Set and Dispaly data for current group i.e 5X1
             */
            if (intGroupIndexTotalCount == 1) {
                addGroupIndex(mGroupIndexModel);
                mProgressBar.setVisibility(View.GONE);
                GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
            }
            /**
             * More then one group has been formed .
             * Send request for 1X1 to retrieve data for remaining group except for group data
             * fetched from 3X1
             */
            else {
                if (intGroupIndexTimeStampCount < intGroupIndexTotalCount) {
                    if (mStoredGroupIndex != null && mStoredGroupIndex.size() > 0) {
                        boolIsToGroupReadingInProcess = true;
                        GroupIndexModel groupIndexModel = mStoredGroupIndex.get(intGroupIndexTimeStampCount);
                        GlobalConstant.RECORD_NEED_TO_BE_RECEIVE = groupIndexModel.getStrGroupIndex();
                        AndroidAppUtils.showLog(TAG, "groupIndexModel.getStrGroupIndex() : " + groupIndexModel.getStrGroupIndex());
                        retrieveDataAccordingToGroupIndexSelected(groupIndexModel.getStrGroupIndex(), (GlobalConstant.ONE_SECOND_DELAY_DURATION * 2));
                        intGroupIndexTimeStampCount = intGroupIndexTimeStampCount + 1;
                    }
                }
            }
        }
    }

    /**
     * Method Name : checkGroupCount
     * Description : This method is used for getting the total number of group conut
     *
     * @param intTotalStoredGroupIndexes
     * @param intCurrentlyRunningIndex
     * @return
     */
    private int checkGroupCount(int intTotalStoredGroupIndexes, int intCurrentlyRunningIndex) {
        int intTotalGroupRecordNeedToFetch = 0;
        if (intTotalStoredGroupIndexes == 0)
            return intTotalGroupRecordNeedToFetch;
        else
            intTotalGroupRecordNeedToFetch = intTotalStoredGroupIndexes;
        GroupIndexModel mGroupIndexModel = new GroupIndexModel();
        mGroupIndexModel.setStrGroupIndex(AppHelper.NUMBER_ONE);
        mStoredGroupIndex.add(mGroupIndexModel);
        return intTotalGroupRecordNeedToFetch;
    }

    /**
     * Method Name : setDataOnGroupIndexListView
     * Description : This method is used for setting data on group index list view
     */
    private void setDataOnGroupIndexListView() {
        AndroidAppUtils.showLog(TAG, "mStoredGroupIndex size : " + mGroupIndexHexData.size());
        if (mStoredGroupListAdapter != null && mGroupIndexHexData.size() > 0) {
            mStoredGroupListAdapter.updateList(mGroupIndexHexData);
            mStoredGroupListAdapter.notifyDataSetChanged();
            manageListVisibility(true);
        } else {
            mStoredGroupListAdapter = new StoredGroupListAdapter(mActivity, mGroupIndexHexData);
            lvDataLogging.setAdapter(mStoredGroupListAdapter);
            mStoredGroupListAdapter.notifyDataSetChanged();
            manageListVisibility(true);
        }
    }

    /**
     * Method Name : setDataOnGroupIndexListView
     * Description : This method is used for setting data on group index list view
     */
    private void addGroupIndex(GroupIndexModel groupIndexModel) {
        AndroidAppUtils.showLog(TAG, "************** addGroupIndex ************ " + mGroupIndexHexData.size());
        if (mStoredGroupListAdapter != null) {
            mStoredGroupListAdapter.addNewGroupIndexToList(groupIndexModel);
            mStoredGroupListAdapter.notifyDataSetChanged();
            manageListVisibility(true);
        } else {
            mStoredGroupListAdapter = new StoredGroupListAdapter(mActivity, mGroupIndexHexData);
            lvDataLogging.setAdapter(mStoredGroupListAdapter);
            mStoredGroupListAdapter.notifyDataSetChanged();
            manageListVisibility(true);
        }
    }

    /**
     * Method Name : readNoOfIndex
     * Description : This method is used for fetching the total
     * number for group index stored on device
     */
    private void readNoOfIndex() {
        if (isDeviceConnected()) {
            AndroidAppUtils.showProgressDialog(mActivity, mActivity.getResources().getString(R.string.strRetrievingIndexCaption), false);
            GlobalConstant.BOOL_READING_TYPE_IN_PROCESS = GlobalKeys.OPERATION_TYPE_GROUP_READING;
            sendTokenForCurrentIndex();
            /**
             * Count Down interval for fetching the current group index data
             */
            myCountDownTimer = new MyCountDownTimer(mActivity, 10000, 1000);
        } else {
            AndroidAppUtils.showLog(TAG, "Connection is broken with device mac address : " + GlobalConstant.DEVICE_MAC);
        }
    }

    /**
     * Method Name : sendTokenForCurrentIndex
     * Description : this method is used for reading the current index for
     * calculation of of total number of stored groups
     */
    private void sendTokenForCurrentIndex() {
        //Sending '?' for reading the current index
        char charReadingToken = '?';
        final byte sendReadingToken[] = {(byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x01};
        strCurrentRequestType = GlobalKeys.DEVICE_CURRENT_INDEX_REQUEST;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ConnectionControl.dl_write_characteristics != null) {
                    DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, sendReadingToken);
                    GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = true;
                }
            }
        }, 2000);


    }

    /**
     * Method  Name : isDeviceConnected
     * Description : This method is used for checking the connection of device
     *
     * @return
     */
    private boolean isDeviceConnected() {
        if (GlobalConstant.CONNECTED_STATE) {
            return true;
        } else {
            AndroidAppUtils.showLog(TAG, " Device Mac Address : " + GlobalConstant.DEVICE_MAC);
            BluetoothDevice bluetoothDevice = GlobalConstant.mBluetoothAdapter != null ? GlobalConstant.mBluetoothAdapter.getRemoteDevice(GlobalConstant.DEVICE_MAC) : BluetoothAdapter.getDefaultAdapter().getRemoteDevice(GlobalConstant.DEVICE_MAC);
            DeviceAdapter.mConnectionControl = new ConnectionControl(ConnectionControl.connectionControl.mActivity, bluetoothDevice, mActivity);
            strCurrentRequestType = GlobalKeys.DEVICE_CURRENT_INDEX_REQUEST;
            return false;
        }
    }

    /**
     * Method Name : manageBluetoothConnectionSetupListener
     * Description : This method is used for setting the listener
     * for connection and read/write operation state
     */
    private void manageBluetoothConnectionSetupListener() {
        GlobalConstant.mBluetoothConnectionStateInterface = new BluetoothConnectionStateInterface() {
            @Override
            public void onGattConnected(Object... bluetoothData) {

            }

            @Override
            public void onGattConnecting(Object... bluetoothData) {

            }

            @Override
            public void onGattDisconnected(Object... bluetoothData) {
                AndroidAppUtils.hideProgressDialog();
                AndroidAppUtils.showLog(TAG, "bluetoothData length : " + bluetoothData.length +
                        "\n status one :" + bluetoothData[0]);
                Object objConnectionStatus = null;
                int intConnectionStatus = -1;
                if (bluetoothData[0] instanceof String) {
                    objConnectionStatus = bluetoothData[0];
                } else if (bluetoothData[0] instanceof Integer) {
                    intConnectionStatus = (int) (bluetoothData[0]);
                }
                try {
                    intConnectionStatus = (int) (bluetoothData[0]);
                    if (intConnectionStatus == GlobalConstant.STATUS_CODE_0
                            || intConnectionStatus == GlobalConstant.STATUS_CODE_8
                            || intConnectionStatus == GlobalConstant.STATUS_CODE_133
                            || intConnectionStatus == GlobalConstant.STATUS_CODE_14) {
                        unRegisterAllListener();
                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = false;
                        if (intRetryCountForEstablishingConnection < 2) {
                            /**
                             * Retry for connection
                             */
                            AndroidAppUtils.dismissGradientDialog();
                            AndroidAppUtils.customAlertDialogWithGradiantBtn(mActivity, mActivity.getResources().getString(R.string.strWarningCaption), true, getString(R.string.strConnectionBrakeMessage)
                                    ,
                                    true, mActivity.getResources().getString(R.string.strRetryCaption), true, new ChoiceDialogClickListener() {
                                        @Override
                                        public void onClickOfPositive() {
                                            /**
                                             * Setting Value to default for reading data in case of disconnection
                                             */

                                            GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
                                            AndroidAppUtils.showLog(TAG, " Device Mac Address : " + GlobalConstant.DEVICE_MAC);
                                            BluetoothDevice bluetoothDevice = GlobalConstant.mBluetoothAdapter.getRemoteDevice(GlobalConstant.DEVICE_MAC);
                                            DeviceAdapter.mConnectionControl = new ConnectionControl(ConnectionControl.connectionControl.mActivity, bluetoothDevice, mActivity);
                                            intRestartingRecordDelay = 1000;
                                            intRetryCountForEstablishingConnection++;
                                        }

                                        @Override
                                        public void onClickOfNegative() {
                                            /**
                                             * Setting Value to default for reading data in case of disconnection
                                             */
                                            GlobalConstant.BOOL_IS_NOTIFICATION_ALREADY_ENABLED = false;
                                            GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                                            mProgressBar.setVisibility(View.GONE);
                                            intRetryCountForEstablishingConnection = 0;
//                                            mActivity.finish();
                                            if (DeviceAdapter.mConnectionControl != null)
                                                DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                                        }
                                    }, true);

                        } else {
                            GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
                            GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                            intRetryCountForEstablishingConnection = 0;
                            if (DeviceAdapter.mConnectionControl != null)
                                DeviceAdapter.mConnectionControl.removeAllActivityExceptScanning();
                        }
                    }
                } catch (Exception e) {
                    AndroidAppUtils.showLog(TAG, "data logging connection class error : " + e.getMessage());
                }
            }

            @Override
            public void onGattServiceDiscovery(Object... bluetoothData) {
                AndroidAppUtils.showInfoLog(TAG, " *********** onGattServiceDiscovery ***********");
                intRetryCountForEstablishingConnection = 0;
                mProgressBar.setVisibility(View.INVISIBLE);
                if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_RECORD_READING)) {
                    AndroidAppUtils.showLog(TAG, " GlobalConstant.INT_CURRENT_RECORD_INDEX_BEFORE_FAIL : "
                            + GlobalConstant.INT_CURRENT_RECORD_INDEX_BEFORE_FAIL + "\n TOTAL_NUMBER_RECORDS :  " + TOTAL_NUMBER_RECORDS);
                    AndroidAppUtils.showInfoLog(TAG, "intCounter : " + intCounter +
                            " AndroidAppUtils.convertIntToHex(intCounter) : " + AndroidAppUtils.convertIntToHex(intCounter));
                    if (GlobalConstant.INT_CURRENT_RECORD_INDEX_BEFORE_FAIL <= TOTAL_NUMBER_RECORDS) {
                        AndroidAppUtils.showLog(TAG, "intGroupIndexCurrentlyFetching : " + intGroupIndexCurrentlyFetching);
                        final byte byteRecordIndexData[];
                               /* *
                                 *Handling the case for disconnect and failure
                                   */
                        GlobalConstant.INT_CURRENT_GROUP_INDEX_BEFORE_FAIL = Integer.parseInt(intGroupIndexCurrentlyFetching, 16);
                        intCounter = GlobalConstant.INT_CURRENT_RECORD_INDEX_BEFORE_FAIL;
                        byte[] mStrGroupIndexData = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(intGroupIndexCurrentlyFetching));
                        byte[] byteRecordInnerCurrentIndex = AndroidAppUtils.verifyForMoreThanTwoDigit(AndroidAppUtils.appendNoOfZeroIfRequired(AndroidAppUtils.convertIntToHex(intCounter)));
                        byteRecordIndexData = new byte[mStrGroupIndexData.length + byteRecordInnerCurrentIndex.length];
                        System.arraycopy(mStrGroupIndexData, 0, byteRecordIndexData, 0, mStrGroupIndexData.length);
                        System.arraycopy(byteRecordInnerCurrentIndex, 0, byteRecordIndexData, mStrGroupIndexData.length, byteRecordInnerCurrentIndex.length);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DeviceAdapter.mConnectionControl.writeToDevice(ConnectionControl.dl_write_characteristics, byteRecordIndexData);
                            }
                        }, 1000);
                        GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = true;
                        strCurrentRequestType = GlobalKeys.DEVICE_NEXT_RECORD_FETCH_REQUEST;
                        intCounter++;
                    }
                } else if (GlobalConstant.BOOL_READING_TYPE_IN_PROCESS.equalsIgnoreCase(GlobalKeys.OPERATION_TYPE_GROUP_READING) || boolIsToGroupReadingInProcess) {
                    AndroidAppUtils.showLog(TAG, " Group Fetching request is in progress");
                    boolIsGroupIndexWriteSuccess = true;
                    AndroidAppUtils.showLog(TAG, "intGroupIndexTimeStampCount : " + intGroupIndexTimeStampCount +
                            "\n intGroupIndexTotalCount : " + intGroupIndexTotalCount +
                            "\n GlobalConstant.INT_CURRENT_GROUP_INDEX_REQUEST_BEFORE_FAIL : " + GlobalConstant.INT_CURRENT_GROUP_INDEX_REQUEST_BEFORE_FAIL);
                    if (GlobalConstant.INT_CURRENT_GROUP_INDEX_REQUEST_BEFORE_FAIL < intGroupIndexTotalCount) {
                        if (mStoredGroupIndex != null && mStoredGroupIndex.size() > 0) {
                            intGroupIndexTimeStampCount = GlobalConstant.INT_CURRENT_GROUP_INDEX_REQUEST_BEFORE_FAIL;
                            GroupIndexModel groupIndexModel = mStoredGroupIndex.get(intGroupIndexTimeStampCount);
                            /**
                             * If request for first group index was made then we need not deduct the value but
                             * if its not the first value then we need to deduct it by one
                             */
                            if (intGroupIndexTimeStampCount > 1) {
                                intGroupIndexTimeStampCount = intGroupIndexTimeStampCount - 1;
                            }
                            GlobalConstant.RECORD_NEED_TO_BE_RECEIVE = mStoredGroupIndex.get(intGroupIndexTimeStampCount).getStrGroupIndex();
                            AndroidAppUtils.showLog(TAG, "groupIndexModel.getStrGroupIndex() : " + groupIndexModel.getStrGroupIndex());
                            retrieveDataAccordingToGroupIndexSelected(groupIndexModel.getStrGroupIndex(), GlobalConstant.ONE_SECOND_DELAY_DURATION * 1);
                            intGroupIndexTimeStampCount = intGroupIndexTimeStampCount + 1;
                            GlobalConstant.BOOL_IS_ANY_OPERATION_IN_PROCESS = true;
                        }
                    } else {
                        //Check for last index that need to retrieved to make total record on device available
                        if (mStoredGroupIndex != null && mStoredGroupIndex.size() > 0)
                            GlobalConstant.RECORD_NEED_TO_BE_RECEIVE = mStoredGroupIndex.get(intGroupIndexTimeStampCount - 1).getStrGroupIndex();
                        /**
                         * If connection is broken even before sending packet for current group index
                         */
                        if (GlobalConstant.INT_CURRENT_GROUP_INDEX_REQUEST_BEFORE_FAIL == intGroupIndexTotalCount) {
                            readNoOfIndex();
                        }
                    }
                } else {
                    AndroidAppUtils.showLog(TAG, " Reading Stop for Group Index TimeStamp as well as for record fetching");
                    /**
                     * If none of the above operation is not performed then start from reading
                     * the total number of group index
                     */
//                    readNoOfIndex();
                }
            }

            @Override
            public void onGattServiceRead(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceWrite(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceWriteStatus(boolean status) {
                AndroidAppUtils.showLog(TAG, "write status : " + status);

            }

            @Override
            public void onGattServiceDataAvailable(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceReadNotPermitted(Object... bluetoothData) {

            }

            @Override
            public void onGattServiceWriteNotPermitted(Object... bluetoothData) {

            }

            @Override
            public void onGattStartReadingResponse(Object... bluetoothData) {

            }
        };
    }

    /**
     * Method Name :saveBeaconB1DataLoggingData
     * Description : This method is used for saving each log data into array list
     * and displaying the newly log generated into the list for Beacon B1
     *
     * @param byteDataReceived
     */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveBeaconB1DataLoggingData(byte[] byteDataReceived) {
        byte byteData;
        String strLsbTemp = "", strRsbTemp = "";

        if (byteDataReceived != null) {
            mIntTotalByteSaved += 7;
            AndroidAppUtils.showLog(TAG, " DATA IN HEX INSIDE saveBeaconB1DataLoggingData : " + AndroidAppUtils.convertToHexString(byteDataReceived));
            String strData = AndroidAppUtils.convertToHexString(byteDataReceived);
            String strUnHexData = AndroidAppUtils.convertHexToString(strData);
            AndroidAppUtils.showLog(TAG, " DATA IN UN HEX INSIDE saveBeaconB1DataLoggingData : " + strUnHexData);
            char[] strSplitData = strUnHexData.toCharArray();
            final DataLoggingModel dataLoggingModel = new DataLoggingModel();
            byte[] byteLogIndex = new byte[2];
            for (int i = 0; i < strSplitData.length; i++) {
                switch (i) {
                    case 0:
                        byteLogIndex[i] = byteDataReceived[i];
                        break;
                    case 1:
                        byteLogIndex[i] = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " byteLogIndex : " + AndroidAppUtils.convertToHexString(byteLogIndex));
                        AndroidAppUtils.showLog(TAG, "Log Index : " + Integer.parseInt(AndroidAppUtils.convertToHexString(byteLogIndex), 16));
                        dataLoggingModel.setStrDataLogIndex(Integer.parseInt(AndroidAppUtils.convertToHexString(byteLogIndex), 16) + "");
                        break;
                    case 2:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX RSB TEM : " + (byteData & 0xff));
                        strRsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrRsbTemperature(strRsbTemp);
                        break;
                    case 3:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX LSB TEM : " + (byteData & 0xff));
                        strLsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrLsbTemperature(strLsbTemp);
                        break;
                    case 4:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " Battery Weeks : " + (byteData & 0xff));
                        dataLoggingModel.setStrBatteryLife((byteData & 0xff) + "");
                        break;
                    case 5:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " Light Intensity : " + (byteData & 0xff));

                        int num = (byteData & 0xff);
                        AndroidAppUtils.showLog(TAG, "num : " + num + "   BINARY : " + Integer.toBinaryString(num));
                        String strBinaryData = Integer.toBinaryString(num);
                        strBinaryData = appendZeroToMakeEightBit(strBinaryData);
                        char[] strTempArray = strBinaryData.toCharArray();
                        String strLightValue = strBinaryData;
                        if (strTempArray != null && strTempArray.length > 0) {
                            if (strTempArray.length == 8 && strTempArray[0] == '1') {
                                AndroidAppUtils.showLog(TAG, "Is Light Ambient Sensor");
                            } else {
                                AndroidAppUtils.showLog(TAG, "Is Not Light Ambient Sensor");
                            }
                            if (!strLightValue.isEmpty()) {
                                dataLoggingModel.setStrTemper(strTempArray[0] + "");
                                AndroidAppUtils.showLog(TAG, "Before Value : " + strLightValue + " converted value : " + Integer.parseInt(strLightValue, 2));
                                strLightValue = strLightValue.substring(1, strLightValue.length());
                                AndroidAppUtils.showLog(TAG, "After Value : " + strLightValue + " converted value : " + (!strLightValue.isEmpty() ? Integer.parseInt(strLightValue, 2) : "00"));
                                dataLoggingModel.setStrLightIntensity(!strLightValue.isEmpty() ? Integer.parseInt(strLightValue, 2) + "" : "00");
                            }

                        } else {
                            AndroidAppUtils.showLog(TAG, "strTempArray is null or size is zero");
                        }
                        break;
                    case 6:
                        break;

                }

            }

            if (!dataLoggingModelArrayList.contains(dataLoggingModel)) {
                dataLoggingModelArrayList.add(dataLoggingModel);

                if (mDataLoggingListAdapter != null) {
                    mProgressBar.setVisibility(View.GONE);
//                    boolIsGroupIndexShowing = false;
                    mDataLoggingListAdapter.addItemToList(dataLoggingModel);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                }
                AndroidAppUtils.showLog(TAG, "dataLoggingModelsList size : " + dataLoggingModelArrayList.size());
            }
            if (mDataLoggingListAdapter != null) {
                if (dataLoggingModelArrayList.size() > 0 && dataLoggingModelArrayList.size() == TOTAL_NUMBER_RECORDS) {
                    boolIsGroupIndexShowing = false;
                    mDataLoggingListAdapter.setListData(dataLoggingModelArrayList);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                }
            } else {
                AndroidAppUtils.showLog(TAG, "mDataLoggingListAdapter is null");
            }
        } else

        {
            AndroidAppUtils.showLog(TAG, "byteDataReceived in null");
        }

    }

    /**
     * Method Name :saveBeaconB4DataLoggingData
     * Description : This method is used for saving each log data into array list
     * and displaying the newly log generated into the list for Beacon B4
     *
     * @param byteDataReceived
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveBeaconB4DataLoggingData(byte[] byteDataReceived) {
        byte byteData;
        String strLsbTemp = "", strRsbTemp = "";
        if (byteDataReceived != null) {
            AndroidAppUtils.showLog(TAG, " DATA IN HEX INSIDE saveBeaconB4DataLoggingData : " + AndroidAppUtils.convertToHexString(byteDataReceived));
            String strData = AndroidAppUtils.convertToHexString(byteDataReceived);
            String strUnHexData = AndroidAppUtils.convertHexToString(strData);
            AndroidAppUtils.showLog(TAG, " DATA IN UN HEX INSIDE saveBeaconB4DataLoggingData : " + strUnHexData);
            char[] strSplitData = strUnHexData.toCharArray();
            final DataLoggingModel dataLoggingModel = new DataLoggingModel();
            byte[] byteLogIndex = new byte[2], bytePressureValue = new byte[2];
            for (int i = 0; i < strSplitData.length; i++) {
                switch (i) {
                    case 0:
                        byteLogIndex[i] = byteDataReceived[i];
                        break;
                    case 1:
                        byteLogIndex[i] = byteDataReceived[i];

                        int intLogIndex = Integer.parseInt(AndroidAppUtils.convertToHexString(byteLogIndex), 16);
                        AndroidAppUtils.showLog(TAG, " byteLogIndex : " + AndroidAppUtils.convertToHexString(byteLogIndex) +
                                " intLogIndex : " + intLogIndex);
                        if (intLogIndex > 10) {
                            String strStartIndex = AndroidAppUtils.byteArrayCheckZero(byteLogIndex) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.convertToHexString(byteLogIndex);
                            AndroidAppUtils.showLog(TAG, "Log Index : " + strStartIndex);

                            dataLoggingModel.setStrDataLogIndex(Integer.parseInt(strStartIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strStartIndex, 16) + "");
                        } else {
                            String strStartIndex = AndroidAppUtils.byteArrayCheckZero(byteLogIndex) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.removeTrailingZeros(AndroidAppUtils.convertToHexString(byteLogIndex));
                            AndroidAppUtils.showLog(TAG, "Log Index : " + strStartIndex);

                            dataLoggingModel.setStrDataLogIndex(Integer.parseInt(strStartIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strStartIndex, 16) + "");
                        }
                        break;
                    case 2:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX RSB TEM : " + (byteData & 0xff));
                        strRsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrRsbTemperature(strRsbTemp);
                        break;
                    case 3:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX LSB TEM : " + (byteData & 0xff));
                        strLsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrLsbTemperature(strLsbTemp);
                        break;
                    case 4:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " Battery Weeks : " + (byteData & 0xff));
                        dataLoggingModel.setStrBatteryLife((byteData & 0xff) + "");
                        break;
                    case 5:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " Light Intensity : " + (byteData & 0xff));

                        int num = (byteData & 0xff);
                        AndroidAppUtils.showLog(TAG, "num : " + num + "   BINARY : " + Integer.toBinaryString(num));
                        String strBinaryData = Integer.toBinaryString(num);
                        strBinaryData = appendZeroToMakeEightBit(strBinaryData);
                        char[] strTempArray = strBinaryData.toCharArray();
                        String strLightValue = strBinaryData;
                        if (strTempArray != null && strTempArray.length > 0) {
                            if (strTempArray.length == 8 && strTempArray[0] == '1') {
                                AndroidAppUtils.showLog(TAG, "Is Light Ambient Sensor");
                            } else {
                                AndroidAppUtils.showLog(TAG, "Is Not Light Ambient Sensor");
                            }
                            if (!strLightValue.isEmpty()) {
                                dataLoggingModel.setStrTemper(strTempArray[0] + "");
                                AndroidAppUtils.showLog(TAG, "Before Value : " + strLightValue + " converted value : " + Integer.parseInt(strLightValue, 2));
                                strLightValue = strLightValue.substring(1, strLightValue.length());
                                AndroidAppUtils.showLog(TAG, "After Value : " + strLightValue + " converted value : " + (!strLightValue.isEmpty() ? Integer.parseInt(strLightValue, 2) : "00"));
                                dataLoggingModel.setStrLightIntensity(!strLightValue.isEmpty() ? Integer.parseInt(strLightValue, 2) + "" : "00");
                            }

                        } else {
                            AndroidAppUtils.showLog(TAG, "strTempArray is null or size is zero");
                        }
                        break;
                    case 6:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX RSB HUMIDITY : " + (byteData & 0xff));
                        strRsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrRsbHumidity(strRsbTemp);
                        break;
                    case 7:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX LSB HUMIDITY : " + (byteData & 0xff));
                        strLsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrLsbHumidity(strLsbTemp);
                        break;
                    case 8:
                        bytePressureValue[i - 8] = byteDataReceived[i];
                        break;
                    case 9:
                        bytePressureValue[i - 8] = byteDataReceived[i];
                        int intPressureIndex = Integer.parseInt(AndroidAppUtils.convertToHexString(bytePressureValue), 16);
                        if (intPressureIndex > 10) {
                            String strPressureIndex = AndroidAppUtils.byteArrayCheckZero(bytePressureValue) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.convertToHexString(bytePressureValue);
                            AndroidAppUtils.showLog(TAG, "PRESSURE : " + AndroidAppUtils.convertToHexString(bytePressureValue) + "" +
                                    " value : " + strPressureIndex);

                            strLsbTemp = Integer.parseInt(strPressureIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strPressureIndex, 16) + "";
                            dataLoggingModel.setStrPressure(strLsbTemp);
                        } else {
                            String strPressureIndex = AndroidAppUtils.byteArrayCheckZero(bytePressureValue) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.removeTrailingZeros(AndroidAppUtils.convertToHexString(bytePressureValue));
                            AndroidAppUtils.showLog(TAG, "PRESSURE : " + AndroidAppUtils.convertToHexString(bytePressureValue) + "" +
                                    " value : " + strPressureIndex);

                            strLsbTemp = Integer.parseInt(strPressureIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strPressureIndex, 16) + "";
                            dataLoggingModel.setStrPressure(strLsbTemp);
                        }
                        break;
                }

            }

            if (!dataLoggingModelArrayList.contains(dataLoggingModel)) {
                dataLoggingModelArrayList.add(dataLoggingModel);

                if (mDataLoggingListAdapter != null) {
                    mProgressBar.setVisibility(View.GONE);
                    boolIsGroupIndexShowing = false;
                    mDataLoggingListAdapter.addItemToList(dataLoggingModel);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                }
                AndroidAppUtils.showLog(TAG, "dataLoggingModelsList size : " + dataLoggingModelArrayList.size());
            }
            if (mDataLoggingListAdapter != null) {
                if (dataLoggingModelArrayList.size() > 0 && dataLoggingModelArrayList.size() == TOTAL_NUMBER_RECORDS) {
//                    AndroidAppUtils.hideProgressDialog();
                    boolIsGroupIndexShowing = false;
                    mDataLoggingListAdapter.setListData(dataLoggingModelArrayList);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                }
            } else {
                AndroidAppUtils.showLog(TAG, "mDataLoggingListAdapter is null");
            }
        } else

        {
            AndroidAppUtils.showLog(TAG, "byteDataReceived in null");
        }

    }

    /**
     * Method Name :saveBeaconB5DataLoggingData
     * Description : This method is used for saving each log data into array list
     * and displaying the newly log generated into the list for Beacon B5
     *
     * @param byteDataReceived
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveBeaconB5DataLoggingData(byte[] byteDataReceived) {
        byte byteData;
        String strLsbTemp = "", strRsbTemp = "";
        if (byteDataReceived != null) {
            AndroidAppUtils.showLog(TAG, " DATA IN HEX INSIDE saveBeaconB4DataLoggingData : " + AndroidAppUtils.convertToHexString(byteDataReceived));
            String strData = AndroidAppUtils.convertToHexString(byteDataReceived);
            String strUnHexData = AndroidAppUtils.convertHexToString(strData);
            AndroidAppUtils.showLog(TAG, " DATA IN UN HEX INSIDE saveBeaconB4DataLoggingData : " + strUnHexData);
            char[] strSplitData = strUnHexData.toCharArray();
            final DataLoggingModel dataLoggingModel = new DataLoggingModel();
            byte[] byteLogIndex = new byte[2], byteTiltAngelValue = new byte[2];
            for (int i = 0; i < strSplitData.length; i++) {
                switch (i) {
                    case 0:
                        byteLogIndex[i] = byteDataReceived[i];
                        break;
                    case 1:
                        byteLogIndex[i] = byteDataReceived[i];

                        int intLogIndex = Integer.parseInt(AndroidAppUtils.convertToHexString(byteLogIndex), 16);
                        AndroidAppUtils.showLog(TAG, " byteLogIndex : " + AndroidAppUtils.convertToHexString(byteLogIndex) +
                                " intLogIndex : " + intLogIndex);
                        if (intLogIndex > 10) {
                            String strStartIndex = AndroidAppUtils.byteArrayCheckZero(byteLogIndex) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.convertToHexString(byteLogIndex);
                            AndroidAppUtils.showLog(TAG, "Log Index : " + strStartIndex);
                            dataLoggingModel.setStrDataLogIndex(Integer.parseInt(strStartIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strStartIndex, 16) + "");
                        } else {
                            String strStartIndex = AndroidAppUtils.byteArrayCheckZero(byteLogIndex) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.removeTrailingZeros(AndroidAppUtils.convertToHexString(byteLogIndex));
                            AndroidAppUtils.showLog(TAG, "Log Index : " + strStartIndex);
                            dataLoggingModel.setStrDataLogIndex(Integer.parseInt(strStartIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strStartIndex, 16) + "");
                        }
                        break;
                    case 2:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX RSB TEM : " + (byteData & 0xff));
                        strRsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrRsbTemperature(strRsbTemp);
                        break;
                    case 3:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX LSB TEM : " + (byteData & 0xff));
                        strLsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrLsbTemperature(strLsbTemp);
                        break;
                    case 4:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " Battery Weeks : " + (byteData & 0xff));
                        dataLoggingModel.setStrBatteryLife((byteData & 0xff) + "");
                        break;
                    case 5:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " Light Intensity : " + (byteData & 0xff));

                        int num = (byteData & 0xff);
                        AndroidAppUtils.showLog(TAG, "num : " + num + "   BINARY : " + Integer.toBinaryString(num));
                        String strBinaryData = Integer.toBinaryString(num);
                        strBinaryData = appendZeroToMakeEightBit(strBinaryData);
                        char[] strTempArray = strBinaryData.toCharArray();
                        String strLightValue = strBinaryData;
                        if (strTempArray != null && strTempArray.length > 0) {
                            if (strTempArray.length == 8 && strTempArray[0] == '1') {
                                AndroidAppUtils.showLog(TAG, "Is Light Ambient Sensor");
                            } else {
                                AndroidAppUtils.showLog(TAG, "Is Not Light Ambient Sensor");
                            }
                            if (!strLightValue.isEmpty()) {
                                dataLoggingModel.setStrTemper(strTempArray[0] + "");
                                AndroidAppUtils.showLog(TAG, "Before Value : " + strLightValue + " converted value : " + Integer.parseInt(strLightValue, 2));
                                strLightValue = strLightValue.substring(1, strLightValue.length());
                                AndroidAppUtils.showLog(TAG, "After Value : " + strLightValue + " converted value : " + (!strLightValue.isEmpty() ? Integer.parseInt(strLightValue, 2) : "00"));
                                dataLoggingModel.setStrLightIntensity(!strLightValue.isEmpty() ? Integer.parseInt(strLightValue, 2) + "" : "00");
                            }

                        } else {
                            AndroidAppUtils.showLog(TAG, "strTempArray is null or size is zero");
                        }
                        break;
                    case 6:
                        byteData = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " HEX SHOCK VALUE : " + (byteData & 0xff));
                        strRsbTemp = (byteData & 0xff) + "";
                        dataLoggingModel.setStrShockValue(strRsbTemp);
                        break;

                    case 7:
                        byteData = byteDataReceived[i];
                        byteTiltAngelValue[i - 7] = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " RSB TILT VALUE : " + (byteData & 0xff));
//                        dataLoggingModel.setStrTiltAngel((byteData & 0xff) + "");
                        break;
                    case 8:
                        byteData = byteDataReceived[i];
                        byteTiltAngelValue[i - 7] = byteDataReceived[i];
                        AndroidAppUtils.showLog(TAG, " LSB TILT VALUE : " + (byteData & 0xff));
                        int intTiltAngel = Integer.parseInt(AndroidAppUtils.convertToHexString(byteTiltAngelValue), 16);
                        AndroidAppUtils.showLog(TAG, " intTiltAngel : " + intTiltAngel);
                        if (intTiltAngel > 10) {
                            String strTileIndex = AndroidAppUtils.byteArrayCheckZero(byteTiltAngelValue) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.convertToHexString(byteTiltAngelValue);
                            AndroidAppUtils.showLog(TAG, "strTileIndex  : " + strTileIndex);
                            int intTiltAngelActualValue = Integer.parseInt(strTileIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strTileIndex, 16);
                            if (intTiltAngelActualValue > 180) {
                                intTiltAngelActualValue = 360 - intTiltAngelActualValue;
                                dataLoggingModel.setStrTiltAngel(AppHelper.HYPHEN + intTiltAngelActualValue + "");
                            } else {
                                dataLoggingModel.setStrTiltAngel(intTiltAngelActualValue + "");
                            }

                        } else {
                            String strTileIndex = AndroidAppUtils.byteArrayCheckZero(byteTiltAngelValue) ? AppHelper.NUMBER_ZERO : AndroidAppUtils.removeTrailingZeros(AndroidAppUtils.convertToHexString(byteTiltAngelValue));
                            AndroidAppUtils.showLog(TAG, "strTileIndex : " + strTileIndex);
                            int intTiltAngelActualValue = Integer.parseInt(strTileIndex.isEmpty() ? AppHelper.NUMBER_ZERO : strTileIndex, 16);
                            if (intTiltAngelActualValue > 180) {
                                intTiltAngelActualValue = 360 - intTiltAngelActualValue;
                                dataLoggingModel.setStrTiltAngel(AppHelper.HYPHEN + intTiltAngelActualValue + "");
                            } else {
                                dataLoggingModel.setStrTiltAngel(intTiltAngelActualValue + "");
                            }
                        }
                        break;
                }

            }

            if (!dataLoggingModelArrayList.contains(dataLoggingModel)) {
                dataLoggingModelArrayList.add(dataLoggingModel);

                if (mDataLoggingListAdapter != null) {
                    mProgressBar.setVisibility(View.GONE);
                    boolIsGroupIndexShowing = false;
                    mDataLoggingListAdapter.addItemToList(dataLoggingModel);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                }
                AndroidAppUtils.showLog(TAG, "dataLoggingModelsList size : " + dataLoggingModelArrayList.size());
            }
            if (mDataLoggingListAdapter != null) {
                if (dataLoggingModelArrayList.size() > 0 && dataLoggingModelArrayList.size() == TOTAL_NUMBER_RECORDS) {
                    boolIsGroupIndexShowing = false;
                    mDataLoggingListAdapter.setListData(dataLoggingModelArrayList);
                    mDataLoggingListAdapter.notifyDataSetChanged();
                }
            } else {
                AndroidAppUtils.showLog(TAG, "mDataLoggingListAdapter is null");
            }
        } else

        {
            AndroidAppUtils.showLog(TAG, "byteDataReceived in null");
        }

    }

    /**
     * Method Name : appendZeroToMakeEightBit
     * Description : This method is used for appending required no of zero bit to start to make one byte of data
     *
     * @param strBinaryData
     * @return
     */
    private String appendZeroToMakeEightBit(String strBinaryData) {
        String strCompleteData = strBinaryData;
        if (!strBinaryData.isEmpty()) {
            int intZeroRequiredToAdd = 8 - strBinaryData.length();
            AndroidAppUtils.showLog(TAG, " No of zero required to add before : " + intZeroRequiredToAdd);
            String strTemporary = "";
            for (int i = 0; i < intZeroRequiredToAdd; i++) {
                strTemporary = AppHelper.NUMBER_ZERO + strTemporary;
            }
            strCompleteData = strTemporary + strBinaryData;
            AndroidAppUtils.showLog(TAG, " strCompleteData : " + strCompleteData);
        }
        return strCompleteData;
    }


    @Override
    protected void onStop() {
        unRegisterAllListener();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void unRegisterAllStaticListener() {
        if (mDataLoggingListener != null) {
            mDataLoggingListener = null;
        }
        if (GlobalConstant.mBluetoothConnectionStateInterface != null) {
            GlobalConstant.mBluetoothConnectionStateInterface = null;
        }
    }


}
