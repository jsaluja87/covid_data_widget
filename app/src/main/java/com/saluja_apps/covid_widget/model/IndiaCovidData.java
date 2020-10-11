package com.saluja_apps.covid_widget.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class IndiaCovidData {
    public ArrayList<IndiaStateCovidCases> regionData = new ArrayList<>();
    //public IndiaStateCovidCases regionData;
    private int activeCases;
    private int activeCasesNew;
    private int recovered;
    private int recoveredNew;
    private int deaths;
    private int deathsNew;
    private int totalCases;
    private String sourceUrl;
    private String lastUpdatedAtApify;


    public ArrayList<IndiaStateCovidCases> getRegionData() {
        return regionData;
    }

    public void setRegionData(ArrayList<IndiaStateCovidCases> regionData) {
        this.regionData = regionData;
    }

    public int getActiveCases() {
        return activeCases;
    }

    public void setActiveCases(int activeCases) {
        this.activeCases = activeCases;
    }

    public int getRecovered() {
        return recovered;
    }

    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLastUpdatedAtApify() {
        return lastUpdatedAtApify;
    }

    public void setLastUpdatedAtApify(String lastUpdatedAtApify) {
        this.lastUpdatedAtApify = lastUpdatedAtApify;
    }

    public int getActiveCasesNew() {
        return activeCasesNew;
    }

    public void setActiveCasesNew(int activeCasesNew) {
        this.activeCasesNew = activeCasesNew;
    }

    public int getRecoveredNew() {
        return recoveredNew;
    }

    public void setRecoveredNew(int recoveredNew) {
        this.recoveredNew = recoveredNew;
    }

    public int getDeathsNew() {
        return deathsNew;
    }

    public void setDeathsNew(int deathsNew) {
        this.deathsNew = deathsNew;
    }

    public static IndiaCovidData fromJSON(JSONObject jsonObject) {
        IndiaCovidData indiaCovidData = new IndiaCovidData();
        try {
            indiaCovidData.regionData = IndiaStateCovidCases.fromJSONArray(jsonObject.getJSONArray("regionData"));
            indiaCovidData.activeCases = jsonObject.getInt("activeCases");
            indiaCovidData.activeCasesNew = jsonObject.getInt("activeCasesNew");
            indiaCovidData.recovered = jsonObject.getInt("recovered");
            indiaCovidData.recoveredNew = jsonObject.getInt("recoveredNew");
            indiaCovidData.deaths = jsonObject.getInt("deaths");
            indiaCovidData.deathsNew = jsonObject.getInt("deathsNew");
            indiaCovidData.totalCases = jsonObject.getInt("totalCases");
            indiaCovidData.sourceUrl = jsonObject.getString("sourceUrl");
            indiaCovidData.lastUpdatedAtApify = jsonObject.getString("lastUpdatedAtApify");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return indiaCovidData;
    }
}
