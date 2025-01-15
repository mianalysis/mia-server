package io.github.mianalysis.miaserver.requests;

public class SetParameterRequest {
    private String moduleID;
    private String parameterName;
    private String parameterValue;
    private String parentGroupName;
    private Number groupCollectionNumber;
    private String imageHash;

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public String getParentGroupName() {
        return parentGroupName;
    }

    public void setParentGroupName(String parentGroupName) {
        this.parentGroupName = parentGroupName;
    }

    public Number getGroupCollectionNumber() {
        return groupCollectionNumber;
    }

    public void setGroupCollectionNumber(Number groupCollectionNumber) {
        this.groupCollectionNumber = groupCollectionNumber;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }
    
}
