package vvdn.in.ble_ota.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.AppHelper;
import vvdn.in.ble_ota.view.BleScanScreen;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.Utils.CustomRVItemTouchListener;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.blecontrols.BluetoothLeService;
import vvdn.in.ble_ota.blecontrols.ConnectionControl;
import vvdn.in.ble_ota.listener.RecyclerViewItemClickListener;
import vvdn.in.ble_ota.model.BLEDataModel;

/**
 * Class Name : Device Adapter
 * Description : This class is used for creating the view and listing the data in recycler view
 *
 * @author Durgesh-Shankar
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder> {
    /**
     * ArrayList for holding the ble scan device data
     */
    private ArrayList<BLEDataModel> bleDeviceModelsList = new ArrayList<>();
    private ArrayList<BLEDataModel> bleDeviceModelsOriginalList = new ArrayList<>();
    /**
     * Debuggable TAG
     */
    private String TAG = DeviceAdapter.class.getSimpleName();
    /**
     * TextView reference object for displaying No Device Found if BLe Scan list is empty
     */
    private TextView mTvNoDeviceFound;
    /**
     * ConnectionControl static reference object to initialize and make connection
     * operation even from other screen
     */
    public static ConnectionControl mConnectionControl;
    /**
     * BleScanScreen reference object
     */
    BleScanScreen csbListScreen;
    /**
     * Activity Object
     */
    private Activity mActivity;
    /**
     * String holding the name or address on which filter is applied
     */
    private String mStrFilterAppliedDeviceNameAddress = BleScanScreen.etSearchFilter.getText().toString();
    //    To ensure unique popup on the screen
    private boolean boolIsPopShowing;

    public DeviceAdapter(Activity mActivity, BleScanScreen csbListScreen, TextView no_device_found) {
        try {
            this.mActivity = mActivity;
            this.csbListScreen = csbListScreen;
            this.mTvNoDeviceFound = no_device_found;

        } catch (Exception e) {
            AndroidAppUtils.showLog(TAG, " error message : " + e.getMessage());
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.csb_list_row, parent, false);
        itemView.setClickable(true);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        mTvNoDeviceFound.setVisibility(View.GONE);
        BLEDataModel bleDeviceModel = null;
        if (bleDeviceModelsList != null && bleDeviceModelsList.size() > 0) {
            bleDeviceModel = bleDeviceModelsList.get(position);
        } else {
            AndroidAppUtils.showLog(TAG, "bleDeviceModelsList is null or empty");
        }
        if (bleDeviceModel != null) {
            final String csbName = bleDeviceModel.getName();

            holder.csb_name.setText(csbName);
            holder.tv_mac.setText(bleDeviceModel.getMacAddress());
            holder.rlConnect.setOnClickListener(mOnClickListener);
            holder.rlConnect.setTag(holder);
            String strManufactureData = bleDeviceModel.getStrManufactureData();

            if (!TextUtils.isEmpty(bleDeviceModel.getStrManufactureData()) && !strManufactureData.equalsIgnoreCase("0")) {
                holder.rlAdvertisement.setVisibility(View.VISIBLE);
                CharSequence cs = mActivity.getText(R.string.strManufactureData);
                String strManufactureString = String.format(mActivity.getResources().getString(R.string.strManufactureData));
                holder.tvManufactureData.setText(AndroidAppUtils.getSpannedText(strManufactureString + "\n" + bleDeviceModel.getStrManufactureData())
                );
            } else {
                holder.rlAdvertisement.setVisibility(View.GONE);
            }
            int intRSSIStrength = bleDeviceModel.getStrRssiStrength().isEmpty() ? 0 : Integer.parseInt(bleDeviceModel.getStrRssiStrength());
//            AndroidAppUtils.showInfoLog(TAG, "intRSSIStrength : " + intRSSIStrength + " csbName : " + csbName);
            /*if (intRSSIStrength > 22) {
//                holder.rlConnect.setEnabled(true);
                holder.rlConnect.setBackgroundColor(mActivity.getResources().getColor(R.color.black));
            } else {
//                holder.rlConnect.setEnabled(false);
                holder.rlConnect.setBackgroundColor(mActivity.getResources().getColor(R.color.card_bg));
            }*/
        } else {
            AndroidAppUtils.showLog(TAG, "bleDeviceModel is null");
        }

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            BleScanScreen.etSearchFilter.clearFocus();
            view.requestFocus();
            MyViewHolder holder = (MyViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            AndroidAppUtils.showErrorLog(TAG, "holder position clicked : " + position);
            if (holder.rlConnect.getId() == R.id.rlConnect) {
                AndroidAppUtils.showLog(TAG, " CONNECT BUTTON CLICKED : " + position);
                BLEDataModel finalBleDeviceModel = bleDeviceModelsList.get(position > 0 ? position : 0);

                AndroidAppUtils.hideKeyboard(mActivity, BleScanScreen.etSearchFilter);
                if (finalBleDeviceModel != null) {
                    if (finalBleDeviceModel.getName() != null &&
                            (finalBleDeviceModel.getName().startsWith(AppHelper.BEACON_B1) ||
                                    finalBleDeviceModel.getName().startsWith(AppHelper.BEACON_B4) ||
                                    finalBleDeviceModel.getName().startsWith(AppHelper.BEACON_B5)
                                    || finalBleDeviceModel.getName().startsWith(AppHelper.DFU_TAG))) {
                        GlobalConstant.IS_NEED_TO_SHOW_DISCONNECT_MESSAGE = true;
                        GlobalConstant.BOOL_IS_DATA_LOGGING_READING_ACTIVITY_VISIBLE = false;
                        BluetoothLeService.getInstance().disconnect();
                        BluetoothLeService.getInstance().close();
                        BluetoothLeService.getInstance().stopSelf();
                        GlobalConstant.DEVICE_CONNECTING_NAME = finalBleDeviceModel.getName();
                        GlobalConstant.CONNECTED_STATE = false;
                        if (mActivity != null) {

                            if (csbListScreen != null)
                                csbListScreen.StopBLEScan();
                            boolIsPopShowing = false;
                            GlobalConstant.DEVICE_NAME = finalBleDeviceModel.getName();
                            GlobalConstant.DEVICE_MAC = finalBleDeviceModel.getMacAddress();
                            if (GlobalConstant.CONNECTED_STATE) {
                                BluetoothLeService.getInstance().disconnect();
                            }
                            mConnectionControl = new ConnectionControl(mActivity, finalBleDeviceModel.getBleDevice(), mActivity);
                        } else {
                            AndroidAppUtils.showLog(TAG, "mActivity is null");
                        }
                    } else {
                        AndroidAppUtils.showToast(mActivity, "Please select beacon device");
                    }
                }
            }
        }
    };

    @Override
    public int getItemCount() {
        return bleDeviceModelsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView csb_name, connect_btn, tv_mac, tvManufactureData;
        RelativeLayout rlConnect, rlAdvertisement;

        private MyViewHolder(View itemView) {
            super(itemView);
            csb_name = (TextView) itemView
                    .findViewById(R.id.csb_name);
            connect_btn = (TextView) itemView.findViewById(R.id.connect_btn);
            tv_mac = (TextView) itemView.findViewById(R.id.tv_mac);

            tvManufactureData = (TextView) itemView.findViewById(R.id.tvManufactureData);
            rlAdvertisement = (RelativeLayout) itemView.findViewById(R.id.rlAdvertisement);
            rlConnect = (RelativeLayout) itemView.findViewById(R.id.rlConnect);
//            rlConnect.setOnClickListener(this);

        }

    }

    /**
     * Method Name : getBleDeviceModelsList
     * Description : this method is used for fetching the ble device list
     *
     * @return
     */
    public ArrayList<BLEDataModel> getBleDeviceModelsList() {
        if (bleDeviceModelsList != null && bleDeviceModelsList.size() > 0) {
            return bleDeviceModelsList;
        }
        return new ArrayList<>();
    }

    /**
     * Add newly discovered ble device to the former list
     *
     * @param bleDataModel
     */

    public void setMoreDataToList(final BLEDataModel bleDataModel) {
        mStrFilterAppliedDeviceNameAddress = BleScanScreen.etSearchFilter.getText().toString();
        AndroidAppUtils.showLog(TAG, " mStrFilterAppliedDeviceNameAddress  : " + mStrFilterAppliedDeviceNameAddress);
        boolean boolIsDeviceAlreadyAddedInCurrent = false, boolIsDeviceAlreadyAddedInOriginal = false;
        if (bleDeviceModelsList != null && bleDeviceModelsList.size() > 0) {
            for (BLEDataModel OldBleDataModel : bleDeviceModelsList)
                if (OldBleDataModel.getMacAddress().equalsIgnoreCase(bleDataModel.getMacAddress())
                        || OldBleDataModel.getName().equalsIgnoreCase(bleDataModel.getName())) {
                    boolIsDeviceAlreadyAddedInCurrent = true;
                    break;
                }
        }
        if (bleDeviceModelsOriginalList != null && bleDeviceModelsOriginalList.size() > 0) {
            for (BLEDataModel OldBleDataModel : bleDeviceModelsOriginalList)
                if (OldBleDataModel.getMacAddress().equalsIgnoreCase(bleDataModel.getMacAddress())
                        || OldBleDataModel.getName().equalsIgnoreCase(bleDataModel.getName())) {
                    boolIsDeviceAlreadyAddedInOriginal = true;
                    break;
                }
        }
        if (!boolIsDeviceAlreadyAddedInOriginal && !bleDeviceModelsOriginalList.contains(bleDataModel)) {
            this.bleDeviceModelsOriginalList.add(bleDataModel);
            this.bleDeviceModelsOriginalList = clearDuplicateEntries(bleDeviceModelsOriginalList);
        }
        /**
         * Check newly discovered device name or address starts with filter text applied then add it to display list
         * otherwise if not present then add it to back up or original list
         */
        if (!boolIsDeviceAlreadyAddedInCurrent && ((bleDataModel.getName().toLowerCase().startsWith(mStrFilterAppliedDeviceNameAddress.toLowerCase()) ||
                bleDataModel.getMacAddress().toLowerCase().startsWith(mStrFilterAppliedDeviceNameAddress.toLowerCase())))) {
            bleDeviceModelsList.add(bleDataModel);
            notifyDataSetChanged();
        } else {
            AndroidAppUtils.showLog(TAG, " Adding device to other list as it doesn't matches : " + bleDataModel.getName());
        }


    }

    /**
     * Update the old discovered ble lsit with new one
     *
     * @param bleDeviceModelsList
     */
    public void setListData(ArrayList<BLEDataModel> bleDeviceModelsList) {
        this.bleDeviceModelsList = bleDeviceModelsList;
        this.bleDeviceModelsOriginalList = new ArrayList<BLEDataModel>();
        bleDeviceModelsOriginalList.addAll(bleDeviceModelsList);
//        bleDeviceModelsOriginalList = clearDuplicateEntries(bleDeviceModelsOriginalList);
        notifyDataSetChanged();
    }

    /**
     * Function Name : filter
     * Description : This is used for filtering the country on the basis of user input
     *
     * @param charText
     */
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.ENGLISH);
        AndroidAppUtils.showErrorLog(TAG, "charText : " + charText + "   bleDeviceModelsOriginalList size : " + bleDeviceModelsOriginalList.size());
        bleDeviceModelsList.clear();
        if (charText.length() == 0) {
            mStrFilterAppliedDeviceNameAddress = "";
            bleDeviceModelsList.addAll(bleDeviceModelsOriginalList);
            notifyDataSetChanged();
        } else {
            for (int i = 0; i < bleDeviceModelsOriginalList.size(); i++) {
                if (bleDeviceModelsOriginalList.get(i).getMacAddress().toLowerCase(Locale.ENGLISH).startsWith(charText)
                        || bleDeviceModelsOriginalList.get(i).getName().toLowerCase(Locale.ENGLISH).startsWith(charText)) {
                    mStrFilterAppliedDeviceNameAddress = charText;
                    bleDeviceModelsList.add(bleDeviceModelsOriginalList.get(i));
                    notifyDataSetChanged();
                } else {
                    notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * Method Name : clearDuplicateEntries
     * Description : This method is used for clearing the duplicate element in the arraylist
     *
     * @param bleOldDataModelsList
     * @return
     */
    private ArrayList<BLEDataModel> clearDuplicateEntries(ArrayList<BLEDataModel> bleOldDataModelsList) {
        List<BLEDataModel> al = new ArrayList<>(bleOldDataModelsList);
        // add elements to al, including duplicates
        Set<BLEDataModel> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
        return new ArrayList<>(al);
    }

    /**
     * Method Name : getInstanceRecyclerViewTouchListener
     * Description : This method is used to set click listener for rcycler view
     *
     * @param mRecyclerView
     * @return
     */
    public CustomRVItemTouchListener getInstanceRecyclerViewTouchListener(RecyclerView mRecyclerView) {
        return new CustomRVItemTouchListener(mActivity, mRecyclerView, new RecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                AndroidAppUtils.showInfoLog(TAG, "View Clicked position: " + position + " view.getId() : " + view.getId() +
                        " R.id.rlConnect : " + R.id.rlConnect);
                if (view.getId() == R.id.rlConnect) {
                    AndroidAppUtils.showToast(mActivity, "Connect Button Position " + position + " clicked");
                }

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        });
    }

}
