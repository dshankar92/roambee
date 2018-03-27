package vvdn.in.ble_ota.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.Utils.GlobalKeys;
import vvdn.in.ble_ota.application.AppApplication;
import vvdn.in.ble_ota.model.DataLoggingModel;
import vvdn.in.ble_ota.model.GroupConfigurationModel;


/**
 * This is the custom adapter for showing the list of data logged captured by the
 * device corresponding to the beacon device index
 *
 * @author Durgesh-Shankar
 */
public class DataLoggingListAdapter extends BaseAdapter {
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
    private String TAG = DataLoggingListAdapter.class.getSimpleName();
    /**
     * ArrayList<DataLoggingModel> reference object
     */
    private ArrayList<DataLoggingModel> dataLoggingModelList = new ArrayList<>();


    /**
     * Public Constructor of this class for initialization
     *
     * @param mActivity
     * @param dataLoggingModelList
     */
    public DataLoggingListAdapter(Activity mActivity, ArrayList<DataLoggingModel> dataLoggingModelList) {

        try {

            this.mActivity = mActivity;
            this.dataLoggingModelList = new ArrayList<>();
            this.dataLoggingModelList = dataLoggingModelList;
            mLayoutInflater = (LayoutInflater) mActivity
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {
            AndroidAppUtils.showLog(TAG, "error message : " + e.getMessage());
        }
    }

    public void addItemToList(DataLoggingModel dataLoggingModel) {
        AndroidAppUtils.showLog(TAG, " ********** addItemToList size of list *********** " + dataLoggingModelList.size());
        boolean boolIsAlreadyPresent = false;
        if (dataLoggingModelList != null && dataLoggingModelList.size() > 0) {
            for (DataLoggingModel dataLoggingModel1 : dataLoggingModelList) {
                if (dataLoggingModel1.getStrDataLogIndex().equalsIgnoreCase(dataLoggingModel.getStrDataLogIndex())) {
                    boolIsAlreadyPresent = true;
                    break;
                }
            }

        }
        if (!boolIsAlreadyPresent) {
            dataLoggingModelList.add(dataLoggingModel);
//                notifyDataSetChanged();
        }
    }

    /**
     * to add data to list view.
     *
     * @param dataLoggingModelList
     */
    public void setListData(ArrayList<DataLoggingModel> dataLoggingModelList) {
        this.dataLoggingModelList = new ArrayList<>();
        this.dataLoggingModelList = dataLoggingModelList;

    }

    @Override
    public int getCount() {
        return dataLoggingModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataLoggingModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            AndroidAppUtils.showLog(TAG, "GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED : " + GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED);
            if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B1_CONNECTED)) {
                convertView = mLayoutInflater.inflate(R.layout.data_logging_tabular_data_view, parent,
                        false);
            } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B4_CONNECTED)) {
                convertView = mLayoutInflater.inflate(R.layout.data_logging_beacon_b4_tabular_data_view, parent,
                        false);
            } else if (GlobalConstant.STRING_CURRENT_BEACON_TYPE_CONNECTED.equalsIgnoreCase(GlobalKeys.BEACON_TYPE_B5_CONNECTED)) {
                convertView = mLayoutInflater.inflate(R.layout.data_logging_beacon_b5_tabular_data_view, parent,
                        false);
            }
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (dataLoggingModelList != null && dataLoggingModelList.size() > 0) {
            AndroidAppUtils.showLog(TAG, "inside adapter dataLoggingModelList size : " + dataLoggingModelList.size());
            DataLoggingModel dataLoggingModel = dataLoggingModelList.get(position);
            String strLsbTemp = "", strRsbTemp = "", strLsbPressure = "",
                    strRsbPressure = "", strLsbHumidity = "", strRsbHumidity = "", strPressure = "";
            if (dataLoggingModel != null) {
                if (dataLoggingModel.getStrBatteryLife() != null) {
                    holder.mtvBatteryLifeData.setText(!dataLoggingModel.getStrBatteryLife().isEmpty() ? dataLoggingModel.getStrBatteryLife() : null);
                }
                if (dataLoggingModel.getStrLightIntensity() != null) {
                    holder.mtvLightIntensityData.setText(!dataLoggingModel.getStrLightIntensity().isEmpty() ? dataLoggingModel.getStrLightIntensity() : null);
                }
                if (dataLoggingModel.getStrLsbTemperature() != null) {
                    strLsbTemp = !dataLoggingModel.getStrLsbTemperature().isEmpty() ? dataLoggingModel.getStrLsbTemperature() : null;

                }
                if (dataLoggingModel.getStrRsbTemperature() != null) {
                    strRsbTemp = !dataLoggingModel.getStrRsbTemperature().isEmpty() ? dataLoggingModel.getStrRsbTemperature() : null;
                }
                if ((strLsbTemp != null && !strLsbTemp.isEmpty()) && (strRsbTemp != null && !strRsbTemp.isEmpty())) {
//                    holder.mtvTemperatureData.setText("20" + "." + "0");
                    holder.mtvTemperatureData.setText(strRsbTemp + AppHelper.DOT + (strLsbTemp.isEmpty() ? AppHelper.NUMBER_ZERO : strLsbTemp.length() > 2 ? strLsbTemp.substring(0, 2) : strLsbTemp));
                } else if (strRsbTemp != null && strRsbTemp.isEmpty()) {
                    holder.mtvTemperatureData.setText(strRsbTemp);
                } else if (strLsbTemp != null && strLsbTemp.isEmpty()) {
                    holder.mtvTemperatureData.setText(AppHelper.NUMBER_ZERO + AppHelper.DOT + strLsbTemp);
                }
                /**
                 * Feature (Pressure,Humidity is for Beacon B4 Device Only
                 */
                if (dataLoggingModel.getStrLsbHumidity() != null) {
                    strLsbHumidity = !dataLoggingModel.getStrLsbHumidity().isEmpty() ? dataLoggingModel.getStrLsbHumidity() : null;

                }
                if (dataLoggingModel.getStrRsbHumidity() != null) {
                    strRsbHumidity = !dataLoggingModel.getStrRsbHumidity().isEmpty() ? dataLoggingModel.getStrRsbHumidity() : null;
                }
                if ((strLsbHumidity != null && !strLsbHumidity.isEmpty()) && (strRsbHumidity != null && !strRsbHumidity.isEmpty())) {
                    holder.mTvHumidity.setText(strRsbHumidity + AppHelper.DOT + (strLsbHumidity.isEmpty() ? AppHelper.NUMBER_ZERO : strLsbHumidity.length() > 2 ? strLsbHumidity.substring(0, 2) : strLsbHumidity));
                } else if (strRsbHumidity != null && strRsbHumidity.isEmpty()) {
                    holder.mTvHumidity.setText(strRsbHumidity);
                } else if (strLsbHumidity != null && strLsbHumidity.isEmpty()) {
                    holder.mTvHumidity.setText(AppHelper.NUMBER_ZERO + AppHelper.DOT + strLsbHumidity);
                }

                if (dataLoggingModel.getStrPressure() != null) {
                    strPressure = !dataLoggingModel.getStrPressure().isEmpty() ? dataLoggingModel.getStrPressure() : null;

                }
                if ((strPressure != null && !strPressure.isEmpty())) {
                    holder.mTvPressure.setText(strPressure);
                }
                if (!dataLoggingModel.getStrTemper().isEmpty()) {
                    holder.tvTamperData.setText(dataLoggingModel.getStrTemper());
                } else {
                    holder.tvTamperData.setText(AppHelper.NUMBER_ZERO);
                }
                if (!dataLoggingModel.getStrDataLogIndex().isEmpty()) {
                    int intStorageInterval = 5;
                    GroupConfigurationModel groupConfigurationModel = AppApplication.getInstance().getLastConfiguration(GlobalConstant.DEVICE_MAC);
                    if (groupConfigurationModel != null && groupConfigurationModel.getStrDataLoggingInterval() != null && !TextUtils.isEmpty(groupConfigurationModel.getStrDataLoggingInterval())) {
                        intStorageInterval = Integer.parseInt(groupConfigurationModel.getStrDataLoggingInterval());
                    }
                    int total = GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE - Integer.parseInt(dataLoggingModel.getStrDataLogIndex());
                    AndroidAppUtils.showLog(TAG, "GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE  : " + GlobalConstant.TOTAL_STORED_RECORDS_ON_DEVICE +
                            "\n  Integer.parseInt(dataLoggingModel.getStrDataLogIndex() : " + Integer.parseInt(dataLoggingModel.getStrDataLogIndex()) +
                            "\n totalDifference  : " + total);
                    int intCalculatedDifference = total * intStorageInterval;//total * 1;
                    String strTimeStamp = AndroidAppUtils.getDate(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp, intCalculatedDifference + ""));
                    AndroidAppUtils.showLog(TAG, "intCalculatedDifference : " + intCalculatedDifference +
                            "\n strTimeStamp : " + strTimeStamp +
                            "\n AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp,intCalculatedDifference) : " +
                            AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp, intCalculatedDifference + ""));

                    holder.mTvDate.setText(AndroidAppUtils.getOnlyDate(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp, intCalculatedDifference + "")));
                    holder.tvTimeStampData.setText(AndroidAppUtils.getTime(AndroidAppUtils.calculateTimeStamp(GlobalConstant.longGroupIndexTimeStamp, intCalculatedDifference + "")));
                    holder.mTvSerialNumber.setText(position + 1 + "");

                }
                String strShockValue = "", strLSBTiltValue = "", strRSBTiltValue = "", strTiltAngel = "";
                if (dataLoggingModel.getStrShockValue() != null && !dataLoggingModel.getStrShockValue().isEmpty()) {
                    strShockValue = dataLoggingModel.getStrShockValue();
                }
                if (holder.mTvShock != null && (strShockValue != null && !strShockValue.isEmpty())) {
                    holder.mTvShock.setText(strShockValue);
                }
                if (dataLoggingModel.getStrLSBTiltAngel() != null) {
                    strLSBTiltValue = !dataLoggingModel.getStrLSBTiltAngel().isEmpty() ? dataLoggingModel.getStrLSBTiltAngel() : null;

                }
                if (dataLoggingModel.getStrRSBTiltAngel() != null) {
                    strRSBTiltValue = !dataLoggingModel.getStrRSBTiltAngel().isEmpty() ? dataLoggingModel.getStrRSBTiltAngel() : null;
                }
                if (dataLoggingModel.getStrTiltAngel() != null) {
                    strTiltAngel = !dataLoggingModel.getStrTiltAngel().isEmpty() ? dataLoggingModel.getStrTiltAngel() : null;
                }
                /*if ((strLSBTiltValue != null && !strLSBTiltValue.isEmpty()) && (strRSBTiltValue != null && !strRSBTiltValue.isEmpty())) {
                    holder.mTvTiltAngel.setText(strRSBTiltValue + AppHelper.DOT + (strLSBTiltValue.isEmpty() ? AppHelper.NUMBER_ZERO : strLSBTiltValue.length() > 2 ? strLSBTiltValue.substring(0, 2) : strLSBTiltValue));
                } else if (strRSBTiltValue != null && strRSBTiltValue.isEmpty()) {
                    holder.mTvTiltAngel.setText(strRSBTiltValue);
                } else if (strLSBTiltValue != null && strLSBTiltValue.isEmpty()) {
                    holder.mTvTiltAngel.setText(AppHelper.NUMBER_ZERO + AppHelper.DOT + strLSBTiltValue);
                }*/
                if (strTiltAngel != null && !(strTiltAngel.isEmpty())) {
                    holder.mTvTiltAngel.setText(strTiltAngel);
                }
            }
            if (position % 2 == 0) {
                holder.lLTabularDataHeader.setBackgroundColor(mActivity.getResources().getColor(R.color.warm_grey_two_30_opacity));
            } else {
                holder.lLTabularDataHeader.setBackgroundColor(mActivity.getResources().getColor(R.color.white_six));
            }
            holder.tvTamperData.setTypeface(null, Typeface.NORMAL);
            holder.mtvTemperatureData.setTypeface(null, Typeface.NORMAL);
            holder.mtvBatteryLifeData.setTypeface(null, Typeface.NORMAL);
            holder.mtvLightIntensityData.setTypeface(null, Typeface.NORMAL);
            holder.mTvDate.setTypeface(null, Typeface.NORMAL);
            holder.tvTimeStampData.setTypeface(null, Typeface.NORMAL);
            holder.tvTimeStampData.setGravity(Gravity.CENTER_VERTICAL);
            if (holder.mTvPressure != null && holder.mTvHumidity != null) {
                holder.mTvHumidity.setTypeface(null, Typeface.NORMAL);
                holder.mTvPressure.setTypeface(null, Typeface.NORMAL);
            }
            if (holder.mTvShock != null && holder.mTvTiltAngel != null) {
                holder.mTvShock.setTypeface(null, Typeface.NORMAL);
                holder.mTvTiltAngel.setTypeface(null, Typeface.NORMAL);
            }
        } else {
            AndroidAppUtils.showLog(TAG, " dataLoggingModel is null");
        }

        return convertView;


    }

    /**
     * List view row object and its views
     */
    private class ViewHolder {
        TextView mtvTemperatureData, mtvBatteryLifeData, mtvLightIntensityData,
                tvTamperData, tvTimeStampData, mTvDate, mTvSerialNumber, mTvPressure, mTvHumidity,
                mTvShock, mTvTiltAngel;
        LinearLayout lLTabularDataHeader;
        RelativeLayout rLTime;

        public ViewHolder(View view) {
            mtvTemperatureData = (TextView) view.findViewById(R.id.mTvTemperature);
            mTvDate = (TextView) view.findViewById(R.id.mTvDate);
            mtvLightIntensityData = (TextView) view.findViewById(R.id.mTvLight);
            tvTamperData = (TextView) view.findViewById(R.id.mTvTamper);
            tvTimeStampData = (TextView) view.findViewById(R.id.mTvTime);
            mtvBatteryLifeData = (TextView) view.findViewById(R.id.mTvBatteryLife);
            lLTabularDataHeader = (LinearLayout) view.findViewById(R.id.lLTabularDataHeader);
            mTvSerialNumber = (TextView) view.findViewById(R.id.mTvSerialNumber);
            rLTime = (RelativeLayout) view.findViewById(R.id.rLTime);
            mTvHumidity = (TextView) view.findViewById(R.id.mTvHumidity);
            mTvPressure = (TextView) view.findViewById(R.id.mTvPressure);
            mTvShock = (TextView) view.findViewById(R.id.mTvShock);
            mTvTiltAngel = (TextView) view.findViewById(R.id.mTvTiltAngel);
        }


    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

}
