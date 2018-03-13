package vvdn.in.ble_ota.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.model.GroupConfigurationModel;
import vvdn.in.ble_ota.model.GroupIndexModel;


/**
 * This is the custom adapter for showing the list of data logged captured by the
 * device corresponding to the beacon device index
 *
 * @author Durgesh-Shankar
 */
public class StoredGroupListAdapter extends BaseAdapter {
    /**
     * Activity Object
     */
    private Activity mActivity;
    /**
     * LayoutInflater reference object
     */
    private LayoutInflater mLayoutInflater;
    /**
     * Debuggable TAG
     */
    private String TAG = StoredGroupListAdapter.class.getSimpleName();
    /**
     * ArrayList<DataLoggingModel> reference object
     */
    private List<GroupIndexModel> mSDataLoggingModelArrayList = new ArrayList<>();


    /**
     * Public Constructor of this class for initialization
     *
     * @param mActivity
     * @param mSDataLoggingModelArrayList
     */
    public StoredGroupListAdapter(Activity mActivity, List<GroupIndexModel> mSDataLoggingModelArrayList) {

        try {

            this.mActivity = mActivity;
            this.mSDataLoggingModelArrayList = mSDataLoggingModelArrayList;
            mLayoutInflater = (LayoutInflater) mActivity
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {
            AndroidAppUtils.showLog(TAG, "error message : " + e.getMessage());
        }
    }


    /**
     * to add data to list view.
     *
     * @param mSDataLoggingModelArrayList
     */
    public void updateList(List<GroupIndexModel> mSDataLoggingModelArrayList) {
        AndroidAppUtils.showLog(TAG, " *********** updateList *************");
        this.mSDataLoggingModelArrayList = new ArrayList<>();
        this.mSDataLoggingModelArrayList = mSDataLoggingModelArrayList;
    }

    /**
     * to add data to list view.
     *
     * @param groupIndexModel
     */
    public void addNewGroupIndexToList(GroupIndexModel groupIndexModel) {
        AndroidAppUtils.showLog(TAG, " *********** addNewGroupIndexToList *************");
        if (mSDataLoggingModelArrayList != null) {
            if (!mSDataLoggingModelArrayList.contains(groupIndexModel)) {
                mSDataLoggingModelArrayList.add(groupIndexModel);
            }
        }
    }

    @Override
    public int getCount() {
        return mSDataLoggingModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSDataLoggingModelArrayList.get(position);
    }

    public List<GroupIndexModel> getGroupArrayList() {
        if (mSDataLoggingModelArrayList != null && mSDataLoggingModelArrayList.size() > 0) {
            return mSDataLoggingModelArrayList;
        }
        return new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.stored_group_data_logging_list_item_row, parent,
                    false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mSDataLoggingModelArrayList != null && mSDataLoggingModelArrayList.size() > 0) {
            AndroidAppUtils.showLog(TAG, "inside adapter mSDataLoggingModelArrayList size : " + mSDataLoggingModelArrayList.size());
            GroupIndexModel groupIndexModel = mSDataLoggingModelArrayList.get(position);
            if (groupIndexModel != null) {
                if (groupIndexModel.getStrGroupIndex() != null) {
                    int intStorageInterval = 5;
                    GroupConfigurationModel groupConfigurationModel = AppApplication.getInstance().getLastConfiguration(GlobalConstant.DEVICE_MAC);
                    if (groupConfigurationModel != null && groupConfigurationModel.getStrDataLoggingInterval() != null && !TextUtils.isEmpty(groupConfigurationModel.getStrDataLoggingInterval())) {
                        intStorageInterval = Integer.parseInt(groupConfigurationModel.getStrDataLoggingInterval());
                    }

//                    /**
//                     * If current position group index matches current group index fetched then deduct
//                     * the counter for that group by one
//                     */
//                    AndroidAppUtils.showInfoLog(TAG, " Before GlobalConstant.INT_CURRENT_GROUP_INDEX : " + GlobalConstant.INT_CURRENT_GROUP_INDEX +
//                            "\n groupIndexModel.setStrCurrentCounter : " + groupIndexModel.getStrCurrentCounter());
//                    if (GlobalConstant.INT_CURRENT_GROUP_INDEX == Integer.parseInt(groupIndexModel.getStrGroupIndex())) {
//                        groupIndexModel.setStrCurrentCounter((Integer.parseInt(groupIndexModel.getStrCurrentCounter()) - 1) + "");
//                    }
//                    AndroidAppUtils.showInfoLog(TAG, "After GlobalConstant.INT_CURRENT_GROUP_INDEX : " + GlobalConstant.INT_CURRENT_GROUP_INDEX +
//                            "\n groupIndexModel.setStrCurrentCounter : " + groupIndexModel.getStrCurrentCounter());
                    holder.tvGroupIndexData.setText(!groupIndexModel.getStrGroupIndex().isEmpty() ? groupIndexModel.getStrGroupIndex() : null);
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
//                    int intDifferenceCountValue = intCurrentCount - intTotalRecordCount;
                    int intDifferenceCountValue = intTotalRecordCount;
                    int intCurrentRecordStartTime = intDifferenceCountValue * intStorageInterval;// intDifferenceCountValue * 1;
                    AndroidAppUtils.showLog(TAG, "intCurrentRecordStartTime : " +
                            AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(longCurrentRecordEndTime,
                                    intCurrentRecordStartTime + "")));

                    GlobalConstant.STRING_CURRENT_GROUP_TIMESTAMP = AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(longCurrentRecordEndTime,
                            intCurrentRecordStartTime + "")) + " - " + AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp,
                            intCurrentEnd + ""));
                    holder.tvGroupIndexData.setText(AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(longCurrentRecordEndTime,
                            intCurrentRecordStartTime + "")) + " - " + AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp,
                            intCurrentEnd + "")) + "( Total Record : " + intTotalRecordCount + " )" +
                            " ( Storage Interval : " + intStorageInterval + " )");

                }


            } else {
                AndroidAppUtils.showLog(TAG, " groupIndexModel is null");
            }
        }
        return convertView;

    }


    /**
     * List view row object and its views
     */
    private class ViewHolder {
        TextView tvGroupIndex, tvGroupIndexData;

        public ViewHolder(View view) {
            tvGroupIndex = (TextView) view.findViewById(R.id.tvGroupIndex);
            tvGroupIndexData = (TextView) view.findViewById(R.id.tvGroupIndexData);
        }

    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }


}
