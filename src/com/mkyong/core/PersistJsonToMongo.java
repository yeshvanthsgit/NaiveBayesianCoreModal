package com.mkyong.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class PersistJsonToMongo {

	
	private static final String LOCALHOST = "localhost";
	private static final MongoClient mongo = new MongoClient(LOCALHOST, 27017);
	private static final DB db = mongo.getDB("TESTDB");

	public static void updateSiteAndRegionPerformaces() throws UnknownHostException {
		updateSitePerformace();
		updateRegionPerformace();
	}

	private static void updateSitePerformace() {
		try {
			
			String sitePerformance = null;

			DBCollection table = db.getCollection("Refinarycoll");
			ArrayList asd = (ArrayList) table.distinct("Site_Name");
			Iterator itr = asd.iterator();

			while (itr.hasNext()) {
				String data = (String) itr.next();

				if(data != null && !data.trim().equals("")){
					BasicDBObject searchQuery = new BasicDBObject();
					searchQuery.put("Site_Name", data);

					DBCursor cursor = table.find(searchQuery);
					List<String> perf = new ArrayList<String>();

					while (cursor.hasNext()) {
						DBObject ob = cursor.next();
						perf.add((String) ob.get("Overall_Refinery_Performance"));
					}

					int badCount = 0;
					int averageCount = 0;
					int goodCount = 0;
					for (String str : perf) {
						if (str.equals("GOOD")) {
							goodCount++;
						} else if (str.equals("BAD")) {
							badCount++;
						} else if (str.equals("AVERAGE")) {
							averageCount++;
						}
					}
					String refineryPerformance = null;
					boolean check = true;
					long size = perf.size();

//					long s1 =(Long.parseLong(badCount)) / size;
//					long s2 = (Long.parseLong(mediumPerf)) / size;
//					long s3 = Long.parseLong(heavyPerf) / size;
					
					double badPer = ((double) badCount / size);
					double avgPer = ((double) averageCount / size);
					double goodPer = ((double) goodCount / size);
					double basePer = ((double) 2 / 3);
					

					if (badPer >= basePer) {
						refineryPerformance = "BAD";
						check = false;
					} else if (avgPer >= basePer) {
						refineryPerformance = "AVERAGE";
						check = false;
					} else if (goodPer >= basePer) {
						refineryPerformance = "GOOD";
						check = false;
					}
					
					DBCollection table1 = db.getCollection("Sitecoll");

					if (check) {
						if(badCount > 0){
							refineryPerformance = "BAD";
						}else{
								if (averageCount >= goodCount) {
									refineryPerformance = "AVERAGE";
								} else {
									refineryPerformance = "GOOD";
								}
							
							BasicDBObject searchQuery1 = new BasicDBObject();
							searchQuery1.put("Site_Name", data);

							DBCursor cursor1 = table1.find(searchQuery1);
							
							while (cursor1.hasNext()) {
								DBObject ob = cursor1.next();
								sitePerformance =(String) ob.get("Overall_Site_Performance");
							}
							
							if(refineryPerformance.equals("GOOD")){
								sitePerformance = "GOOD";
							}else if(refineryPerformance.equals("BAD") && sitePerformance.equals("GOOD")){
								sitePerformance = "GOOD";
							}else if(refineryPerformance.equals("BAD") && sitePerformance.equals("BAD")){
								sitePerformance = "BAD";
							}else if(refineryPerformance.equals("BAD") && sitePerformance.equals("AVERAGE")){
								sitePerformance = "BAD";
							}else if(refineryPerformance.equals("AVERAGE") && sitePerformance.equals("GOOD")){
								sitePerformance = "GOOD";
							}else if(refineryPerformance.equals("AVERAGE") && sitePerformance.equals("BAD")){
								sitePerformance = "BAD";
							}else if(refineryPerformance.equals("AVERAGE") && sitePerformance.equals("AVERAGE")){
								sitePerformance = "AVERAGE";
							}
						}
					}

					BasicDBObject updateFields = new BasicDBObject();
					if(sitePerformance == null)
						updateFields.append("Calculated_Site_Performance", refineryPerformance);
					else
						updateFields.append("Calculated_Site_Performance", sitePerformance);
					
					sitePerformance = null;
					BasicDBObject setQuery = new BasicDBObject();
					setQuery.append("$set", updateFields);
					
					table1.updateMulti(searchQuery, setQuery);
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateRegionPerformace() {
		try {
			String regionPerformace = null;

			boolean test = true;
			DBCollection table = db.getCollection("Sitecoll");
			ArrayList asd = (ArrayList) table.distinct("Region_Name");
			Iterator itr = asd.iterator();

			while (itr.hasNext()) {
				String data = (String) itr.next();
				
				if(data != null && !data.trim().equals("")){

					BasicDBObject searchQuery = new BasicDBObject();
					searchQuery.put("Region_Name", data);

					DBCursor cursor = table.find(searchQuery);
					
					List<String> perf = new ArrayList<String>();

					while (cursor.hasNext()) {
						DBObject ob = cursor.next();
						perf.add((String) ob.get("Calculated_Site_Performance"));
					}

					int badCount = 0;
					int avgCount = 0;
					int goodCount = 0;
					for (String str : perf) {
						if (str != null) {
							if (str.equals("GOOD")) {
								goodCount++;
							} else if (str.equals("BAD")) {
								badCount++;
							} else if (str.equals("AVERAGE")) {
								avgCount++;
							}
						}
					}
					
					String sitePerf = null;
					boolean check = true;

					long size = perf.size();

					double badPer = ((double) badCount / size);
					double avgPer = ((double) avgCount / size);
					double goodPer = ((double) goodCount / size);
					double cmp = ((double) 2 / 3);

					if (goodPer >= cmp) {
						sitePerf = "GOOD";
						check = false;
					} else if (badPer >= cmp) {
						sitePerf = "BAD";
						check = false;
					} else if (avgPer >= cmp) {
						sitePerf = "AVERAGE";
						check = false;
					}
					
					DBCollection table1 = db.getCollection("Regioncoll");

					if (check) {
						if(badCount > 0){
							sitePerf = "BAD";
						}else{
//							if (badCount >= mediumPerf) {
//								if (badCount >= heavyPerf) {
//									sitePerf = "GOOD";
//								} else {
//									sitePerf = "AVERAGE";
//								}
//							} else {
								if (avgCount >= goodCount) {
									sitePerf = "AVERAGE";
								} else {
									sitePerf = "GOOD";
								}
//							}
							
							BasicDBObject searchQuery1 = new BasicDBObject();
							searchQuery1.put("Region_Name", data);

							DBCursor cursor1 = table1.find(searchQuery1);
							
							while (cursor1.hasNext()) {
								DBObject ob = cursor1.next();
								regionPerformace =(String) ob.get("Overall_Region_Performance");
							}
							
							if(sitePerf.equals("GOOD")){
								regionPerformace = "GOOD";
							}else if(sitePerf.equals("BAD") && regionPerformace.equals("GOOD")){
								regionPerformace = "GOOD";
							}else if(sitePerf.equals("BAD") && regionPerformace.equals("BAD")){
								regionPerformace = "BAD";
							}else if(sitePerf.equals("BAD") && regionPerformace.equals("AVERAGE")){
								regionPerformace = "BAD";
							}else if(sitePerf.equals("AVERAGE") && regionPerformace.equals("GOOD")){
								regionPerformace = "GOOD";
							}else if(sitePerf.equals("AVERAGE") && regionPerformace.equals("BAD")){
								regionPerformace = "BAD";
							}else if(sitePerf.equals("AVERAGE") && regionPerformace.equals("AVERAGE")){
								regionPerformace = "AVERAGE";
							}
						}
					}
					
					BasicDBObject updateFields = new BasicDBObject();
					if(regionPerformace == null)
						updateFields.append("Calculated_Region_Performance", sitePerf);
					else
						updateFields.append("Calculated_Region_Performance", regionPerformace);
					
					regionPerformace = null;
					BasicDBObject setQuery = new BasicDBObject();
					setQuery.append("$set", updateFields);
					
					table1.updateMulti(searchQuery, setQuery);
				}
				
				

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
