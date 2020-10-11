package com.saluja_apps.covid_widget.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class IndiaStateCovidCases {
    private String region;
    private int totalInfected;
    private int newInfected;
    private int recovered;
    private int newRecovered;
    private int deceased;
    private int newDeceased;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getTotalInfected() {
        return totalInfected;
    }

    public void setTotalInfected(int totalInfected) {
        this.totalInfected = totalInfected;
    }

    public int getRecovered() {
        return recovered;
    }

    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }

    public int getDeceased() {
        return deceased;
    }

    public void setDeceased(int deceased) {
        this.deceased = deceased;
    }

    public int getNewInfected() {
        return newInfected;
    }

    public void setNewInfected(int newInfected) {
        this.newInfected = newInfected;
    }

    public int getNewRecovered() {
        return newRecovered;
    }

    public void setNewRecovered(int newRecovered) {
        this.newRecovered = newRecovered;
    }

    public int getNewDeceased() {
        return newDeceased;
    }

    public void setNewDeceased(int newDeceased) {
        this.newDeceased = newDeceased;
    }

    public static IndiaStateCovidCases fromJSON(JSONObject jsonObject) {
        IndiaStateCovidCases indiaStateCovidCases = new IndiaStateCovidCases();
        try {

            indiaStateCovidCases.region = jsonObject.getString("region");
            indiaStateCovidCases.totalInfected = jsonObject.getInt("totalInfected");
            indiaStateCovidCases.newInfected = jsonObject.getInt("newInfected");
            indiaStateCovidCases.recovered = jsonObject.getInt("recovered");
            indiaStateCovidCases.newRecovered = jsonObject.getInt("newRecovered");
            indiaStateCovidCases.deceased = jsonObject.getInt("deceased");
            indiaStateCovidCases.newDeceased = jsonObject.getInt("newDeceased");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return indiaStateCovidCases;
    }

    public static ArrayList<IndiaStateCovidCases> fromJSONArray(JSONArray jsonArray) {
        ArrayList<IndiaStateCovidCases> indiaStateCovidCasesArray = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject regionJsonObject = null;
            try {
                regionJsonObject = jsonArray.getJSONObject(i);
                IndiaStateCovidCases regionData = IndiaStateCovidCases.fromJSON(regionJsonObject);

                if(regionData != null) {
                    indiaStateCovidCasesArray.add(regionData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return indiaStateCovidCasesArray;
    }

}
