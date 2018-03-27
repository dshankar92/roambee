package vvdn.in.ble_ota.model;

/**
 * ClassName : DataLoggingModel
 * Description : This class is used to save data corresponding to each logged value from the device.
 * This logged information include temperature, light intensity and battery life
 *
 * @author Durgesh-Shankar
 */
public class DataLoggingModel {

    /**
     * Temperature of device indicating value before decimal point
     */
    String strLsbTemperature = "";
    /**
     * Temperature of device indicating value after decimal point
     */
    String strRsbTemperature = "";
    /**
     * Light Intensity value fetched from device
     */
    String strLightIntensity = "";
    /**
     * Battery Life value fetched from device i.e how much more can battery life will survive
     */
    String strBatteryLife = "";
    /**
     * DataLogIndex value fetched from device which indicate index for which data is retrieved
     */
    String strDataLogIndex = "";
    /**
     * Temper value : 1 indicate ambient , 0: not ambient
     */
    String strTemper = "";

    /**
     * Humidity of device indicating value before decimal point
     */
    String strLsbHumidity = "";
    /**
     * Humidity of device indicating value after decimal point
     */
    String strRsbHumidity = "";
    /**
     * Pressure of device indicating value before decimal point
     */
    String strLsbPressure = "";
    /**
     * Pressure of device indicating value after decimal point
     */
    String strRsbPressure = "";
    /**
     * Shock Value for Ble Beacon B5 , range from 0-7
     */
    String strShockValue = " ";
    /**
     * Tilt value fro BLE beacon device i.e what is the inclined angel with ground
     */
    String strLSBTiltAngel = "";
    /**
     * Tilt value fro BLE beacon device i.e what is the inclined angel with ground
     */
    String strTiltAngel="";

    public String getStrTiltAngel() {
        return strTiltAngel;
    }

    public void setStrTiltAngel(String strTiltAngel) {
        this.strTiltAngel = strTiltAngel;
    }



    public String getStrLSBTiltAngel() {
        return strLSBTiltAngel;
    }

    public void setStrLSBTiltAngel(String strLSBTiltAngel) {
        this.strLSBTiltAngel = strLSBTiltAngel;
    }

    public String getStrRSBTiltAngel() {
        return strRSBTiltAngel;
    }

    public void setStrRSBTiltAngel(String strRSBTiltAngel) {
        this.strRSBTiltAngel = strRSBTiltAngel;
    }

    /**
     * Tilt value fro BLE beacon device i.e what is the inclined angel with ground
     */
    String strRSBTiltAngel = "";

    public String getStrShockValue() {
        return strShockValue;
    }

    public void setStrShockValue(String strShockValue) {
        this.strShockValue = strShockValue;
    }


    /**
     * Pressure of device
     */
    String strPressure = "";

    public String getStrLsbHumidity() {
        return strLsbHumidity;
    }

    public void setStrLsbHumidity(String strLsbHumidity) {
        this.strLsbHumidity = strLsbHumidity;
    }

    public String getStrRsbHumidity() {
        return strRsbHumidity;
    }

    public void setStrRsbHumidity(String strRsbHumidity) {
        this.strRsbHumidity = strRsbHumidity;
    }

    public String getStrLsbPressure() {
        return strLsbPressure;
    }

    public void setStrLsbPressure(String strLsbPressure) {
        this.strLsbPressure = strLsbPressure;
    }

    public String getStrRsbPressure() {
        return strRsbPressure;
    }

    public void setStrRsbPressure(String strRsbPressure) {
        this.strRsbPressure = strRsbPressure;
    }


    public String getStrDataLogIndex() {
        return strDataLogIndex;
    }

    public void setStrDataLogIndex(String strDataLogIndex) {
        this.strDataLogIndex = strDataLogIndex;
    }


    public String getStrLsbTemperature() {
        return strLsbTemperature;
    }

    public void setStrLsbTemperature(String strLsbTemperature) {
        this.strLsbTemperature = strLsbTemperature;
    }

    public String getStrRsbTemperature() {
        return strRsbTemperature;
    }

    public void setStrRsbTemperature(String strRsbTemperature) {
        this.strRsbTemperature = strRsbTemperature;
    }

    public String getStrLightIntensity() {
        return strLightIntensity;
    }

    public void setStrLightIntensity(String strLightIntensity) {
        this.strLightIntensity = strLightIntensity;
    }

    public String getStrBatteryLife() {
        return strBatteryLife;
    }

    public void setStrBatteryLife(String strBatteryLife) {
        this.strBatteryLife = strBatteryLife;
    }


    public String getStrTemper() {
        return strTemper;
    }

    public void setStrTemper(String strTemper) {
        this.strTemper = strTemper;
    }

    public String getStrPressure() {
        return strPressure;
    }

    public void setStrPressure(String strPressure) {
        this.strPressure = strPressure;
    }
}
