package com.mkyong.core;

import java.io.IOException;

import org.json.JSONException;
import org.json.simple.parser.ParseException;

public class JSON2CSV {
    public static void main(String myHelpers[]) throws ParseException{
        try {
        	JsonCsvUtils jsonCsvUtils = new JsonCsvUtilsImpl();
        	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl("http://localhost:8544/refinery/fetchData/TrainDB/Refinary"), "C:/FM/Bayesian/FInal project/fromJson.csv");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
    }

}
