package com.saluja_apps.covid_widget.model;

public class ConfigListViewItem {
    private String stateName;
    private boolean stateClickedState;

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public boolean isStateClickedState() {
        return stateClickedState;
    }

    public void setStateClickedState(boolean stateClickedState) {
        this.stateClickedState = stateClickedState;
    }
}
