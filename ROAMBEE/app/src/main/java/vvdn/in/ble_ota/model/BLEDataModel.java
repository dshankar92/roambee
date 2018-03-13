package vvdn.in.ble_ota.model;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Class Name : BLEDataModel
 * Description : This class is used to store data related ble device
 *
 * @author Durgesh-Shankar
 */
public class BLEDataModel implements Serializable {

    private String name="";
    private String macAddress="";
    private BluetoothDevice bleDevice;
    private String strRSSIStrength = "";
    private String strManufactureData = "";

    public String getStrRssiStrength() {
        return strRSSIStrength;
    }

    public void setStrRssiStrength(String strRssiStrength) {
        this.strRSSIStrength = strRssiStrength;
    }


    public boolean isDeviceAvailable() {
        return isDeviceAvailable;
    }

    public void setDeviceAvailable(boolean deviceAvailable) {
        isDeviceAvailable = deviceAvailable;
    }

    private boolean isDeviceAvailable = false;

    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getStrManufactureData() {
        return strManufactureData;
    }

    public void setStrManufactureData(String strManufactureData) {
        this.strManufactureData = strManufactureData;
    }


}
