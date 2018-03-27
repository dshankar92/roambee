package vvdn.in.ble_ota.model;

/**
 * ClassName : GroupConfigurationModel
 * Description : This class is used to save data corresponding to each group.
 * This information includes broadcasting interval, transmit power, ambient sensor value,
 * light sensor value,channel 37,38,39 and logging interval difference for each record
 *
 * @author Durgesh-Shankar
 */
public class GroupConfigurationModel {
    String strPrfTimeOut = "";
    String strTxPower = "";
    String strAls = "";
    String strTamper = "";
    String strChannel37 = "";
    String strChannel38 = "";
    String strChannel39 = "";
    String strDataLoggingInterval = "";
    String strPharmaModeValue = "";
    String strPharmaModeDelay = "";

    public String getStrPharmaModeValue() {
        return strPharmaModeValue;
    }

    public void setStrPharmaModeValue(String strPharmaModeValue) {
        this.strPharmaModeValue = strPharmaModeValue;
    }

    public String getStrPharmaModeDelay() {
        return strPharmaModeDelay;
    }

    public void setStrPharmaModeDelay(String strPharmaModeDelay) {
        this.strPharmaModeDelay = strPharmaModeDelay;
    }


    public String getStrPrfTimeOut() {
        return strPrfTimeOut;
    }

    public void setStrPrfTimeOut(String strPrfTimeOut) {
        this.strPrfTimeOut = strPrfTimeOut;
    }

    public String getStrTxPower() {
        return strTxPower;
    }

    public void setStrTxPower(String strTxPower) {
        this.strTxPower = strTxPower;
    }

    public String getStrAls() {
        return strAls;
    }

    public void setStrAls(String strAls) {
        this.strAls = strAls;
    }

    public String getStrTamper() {
        return strTamper;
    }

    public void setStrTamper(String strTamper) {
        this.strTamper = strTamper;
    }

    public String getStrChannel37() {
        return strChannel37;
    }

    public void setStrChannel37(String strChannel37) {
        this.strChannel37 = strChannel37;
    }

    public String getStrChannel38() {
        return strChannel38;
    }

    public void setStrChannel38(String strChannel38) {
        this.strChannel38 = strChannel38;
    }

    public String getStrChannel39() {
        return strChannel39;
    }

    public void setStrChannel39(String strChannel39) {
        this.strChannel39 = strChannel39;
    }

    public String getStrDataLoggingInterval() {
        return strDataLoggingInterval;
    }

    public void setStrDataLoggingInterval(String strDataLoggingInterval) {
        this.strDataLoggingInterval = strDataLoggingInterval;
    }
}
