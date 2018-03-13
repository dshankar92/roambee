package vvdn.in.ble_ota.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import no.nordicsemi.android.log.Logger;
import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.Utils.GlobalConstant;
import vvdn.in.ble_ota.Utils.GlobalKeys;
import vvdn.in.ble_ota.adapter.DeviceAdapter;
import vvdn.in.ble_ota.model.DataLoggingModel;
import vvdn.in.ble_ota.model.GroupConfigurationModel;

/**
 * Class Name : AppApplication
 * Description : This class is singleton class is used for perform operation like saving data into cache or db
 */
public class AppApplication extends Application {
    /**
     * AppApplication instance object
     */
    private static AppApplication mInstance;
    /**
     * Debuggable TAG
     */
    private String TAG = AppApplication.class.getSimpleName();
    /**
     * SharedPreferences reference object
     */
    private SharedPreferences mSharedPreferences;
    /**
     * Editor reference object
     */
    private SharedPreferences.Editor mEditor;
    /**
     * LinkedHashMap<String, ArrayList<DataLoggingModel>> reference object for holding the record data corresponding to the index
     */
    private LinkedHashMap<String, ArrayList<DataLoggingModel>> mArrayListLinkedHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String, GroupConfigurationModel> mGroupConfigurationHashMap = new LinkedHashMap<>();
    private Activity mActivity;


    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
    }

    /**
     * Method Name : getInstance
     * Description : This method is used for getting instance of singleton class
     *
     * @return
     */
    public static synchronized AppApplication getInstance() {
        if (mInstance == null) {
            mInstance = new AppApplication();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mSharedPreferences = getSharedPreferences(GlobalKeys.ROAMBEE_SHARED_PREFERENCE, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        GlobalConstant.mGlobalActivityArrayList = new ArrayList<>();
        GlobalConstant.mLogSession = Logger.newSession(mInstance, TAG, "Application");
    }

    /**
     * Method Name : getArrayListLinkedHashMap
     * Description : This method is used for retriving the arraylist corresponding to index requested.
     *
     * @param strGroupIndex
     * @return
     */
    public ArrayList<DataLoggingModel> getArrayListLinkedHashMap(String strGroupIndex) {
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, ArrayList<DataLoggingModel>>>() {
        }.getType();
        String strDataLogging = mSharedPreferences.getString(GlobalKeys.ROAMBEE_DATA_LOGGING_HASH_MAP, "");
        mArrayListLinkedHashMap = new LinkedHashMap<>();
        mArrayListLinkedHashMap = gson.fromJson(strDataLogging, type);
        if (mArrayListLinkedHashMap != null && mArrayListLinkedHashMap.size() > 0 && mArrayListLinkedHashMap.containsKey(strGroupIndex))
            return mArrayListLinkedHashMap.get(strGroupIndex);
        else
            return new ArrayList<>();
    }

    /**
     * Method Name : setArrayListLinkedHashMap
     * Description : This method is used for saving the data corresponding to each group index in
     * LinkedHashMap and then saving it into shared preference for future retrieval
     *
     * @param strKey
     * @param mArrayListData
     */
    public void setArrayListLinkedHashMap(String strKey, ArrayList<DataLoggingModel> mArrayListData) {
        if (mArrayListLinkedHashMap != null && mArrayListLinkedHashMap.size() > 0) {
            if (mArrayListLinkedHashMap.containsKey(strKey)) {
                ArrayList<DataLoggingModel> tempDataLoggingModelList = mArrayListLinkedHashMap.get(strKey);
                tempDataLoggingModelList.addAll(mArrayListData);
                this.mArrayListLinkedHashMap.put(strKey, tempDataLoggingModelList);
            } else {
                this.mArrayListLinkedHashMap.put(strKey, mArrayListData);
            }
        } else {
            this.mArrayListLinkedHashMap = new LinkedHashMap<>();
            mArrayListLinkedHashMap.put(strKey, mArrayListData);
        }
        Gson gson = new Gson();
        String mDataLoggingArrayList = gson.toJson(mArrayListLinkedHashMap);
        mEditor.putString(GlobalKeys.ROAMBEE_DATA_LOGGING_HASH_MAP, mDataLoggingArrayList);
        mEditor.commit();

    }

    /**
     * Method Name : getLastConfiguration
     * Description : This method is used for retrieving the last configuration
     *
     * @param strGroupIndex
     * @return
     */
    public GroupConfigurationModel getLastConfiguration(String strGroupIndex) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<LinkedHashMap<String, GroupConfigurationModel>>() {
            }.getType();
            String strDataLogging = mSharedPreferences.getString(GlobalKeys.ROAMBEE_DATA_LOGGING_CONFIGURATION_HASH_MAP, "");
            mGroupConfigurationHashMap = new LinkedHashMap<>();

            mGroupConfigurationHashMap = gson.fromJson(strDataLogging, type);
            if (mGroupConfigurationHashMap != null && mGroupConfigurationHashMap.size() > 0 && mGroupConfigurationHashMap.containsKey(strGroupIndex))
                return mGroupConfigurationHashMap.get(strGroupIndex);


        } catch (Exception e) {
            AndroidAppUtils.showInfoLog("AppApplication", "error message " + e.getMessage());
        }
        return new GroupConfigurationModel();
    }

    /**
     * Method Name : saveConfigurationData
     * Description : This method is used for saving the last configuration data
     *
     * @param strKey
     * @param groupConfigurationModel
     */
    public void saveConfigurationData(String strKey, GroupConfigurationModel groupConfigurationModel) {
        if (mGroupConfigurationHashMap != null && mGroupConfigurationHashMap.size() > 0) {
            if (mGroupConfigurationHashMap.containsKey(strKey)) {
                this.mGroupConfigurationHashMap.put(strKey, groupConfigurationModel);
            } else {
                this.mGroupConfigurationHashMap.put(strKey, groupConfigurationModel);
            }
        } else {
            this.mGroupConfigurationHashMap = new LinkedHashMap<>();
            mGroupConfigurationHashMap.put(strKey, groupConfigurationModel);
        }
        Gson gson = new Gson();
        String mDataLoggingArrayList = gson.toJson(mGroupConfigurationHashMap);
        mEditor.putString(GlobalKeys.ROAMBEE_DATA_LOGGING_CONFIGURATION_HASH_MAP, mDataLoggingArrayList);
        mEditor.commit();

    }

    /**
     * Method Name : clearRecordHashMap
     * Description : This method is used for clearing the hashmap
     * holding the record data fro previous device connected
     */
    public void clearRecordHashMap() {
        if (mEditor != null) {
            mEditor.remove(GlobalKeys.ROAMBEE_DATA_LOGGING_HASH_MAP);
//            mEditor.clear();
            mEditor.commit();
            this.mArrayListLinkedHashMap = new LinkedHashMap<>();
        }
    }

    /**
     * Method Name : getCurrentActivity
     * Description : This method is used for getting the reference of
     * currently visible activity
     *
     * @return
     */
    public Activity getCurrentActivity() {
        if (mActivity != null) {
            return mActivity;
        }
        return null;
    }

    /**
     * Method Name : setCurrentActivityReference
     * Description : This method is used for setting the reference of
     * currently visible activity
     *
     * @return
     */
    public void setCurrentActivityReference(Activity mActivity) {
        this.mActivity = mActivity;
    }


    @Override
    public void onTerminate() {
        /**
         * Deregister all the service and unbind the it on killing/termination of application
         */
        if (DeviceAdapter.mConnectionControl != null) {
            DeviceAdapter.mConnectionControl.UnregisterAllServices();
            DeviceAdapter.mConnectionControl.UnregisterUnBindAll();
        }
        super.onTerminate();
    }
}
