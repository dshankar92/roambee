package vvdn.in.ble_ota.model;

/**
 * ClassName : GroupIndexModel
 * Description : This class is used to save data corresponding to each group.
 * This information includes group index , group total record captured and last counter value for
 * last record written for this group
 *
 * @author Durgesh-Shankar
 */
public class GroupIndexModel {
    private String strGroupIndex = "";
    private String strGroupTotalRecord = "";
    private String strCurrentCounter = "";

    public String getStrGroupIndex() {
        return strGroupIndex;
    }

    public void setStrGroupIndex(String strGroupIndex) {
        this.strGroupIndex = strGroupIndex;
    }


    public String getStrGroupTotalRecord() {
        return strGroupTotalRecord;
    }

    public void setStrGroupTotalRecord(String strGroupTotalRecord) {
        this.strGroupTotalRecord = strGroupTotalRecord;
    }

    public String getStrCurrentCounter() {
        return strCurrentCounter;
    }

    public void setStrCurrentCounter(String strCurrentCounter) {
        this.strCurrentCounter = strCurrentCounter;
    }
}
