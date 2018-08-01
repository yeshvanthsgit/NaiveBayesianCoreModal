package com.naivebayes.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class ReadConfig {

	Properties prop = null;
	
	String node = null;
	String site = null;
	String region = null;
	public static void main(String[] args) {
		ReadConfig rc = new ReadConfig();
		for(String s:rc.getNodeAttributes()){
			System.out.println(s);
		}
	}	
	public void loadProperties(){
		try {
			prop = new Properties();
			InputStream is = RegionLevelPredictor.class.getResourceAsStream("./config1.properties");
			prop.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String[] getNodeAttributes(){
		if(null == prop){
			loadProperties();
		}
		
		node = prop.getProperty("NodeTrainAttributes");
		if(null != node){
			return node.split(",");
		}else{
			return null;
		}
	}
	
	public String[] getNodeAttributes1(){
		if(null == prop){
			loadProperties();
		}
		
		node = prop.getProperty("NodeTestAttributes");
		if(null != node){
			return node.split(",");
		}else{
			return null;
		}
	}
	
	public String[] getSiteAttributes(){
		if(null == prop){
			loadProperties();
		}
		
		site = prop.getProperty("SiteTrainAttributes");
		if(null != site){
			return site.split(",");
		}else{
			return null;
		}
	}
	
	public String[] getSiteAttributes1(){
		if(null == prop){
			loadProperties();
		}
		
		site = prop.getProperty("SiteTestAttributes");
		if(null != site){
			return site.split(",");
		}else{
			return null;
		}
	}
	
	public String[] getRegionAttributes(){
		if(null == prop){
			loadProperties();
		}
		
		region = prop.getProperty("RegionTrainAttributes");
		if(null != region){
			return region.split(",");
		}else{
			return null;
		}
	}
	
	public String[] getRegionAttributes1(){
		if(null == prop){
			loadProperties();
		}
		
		region = prop.getProperty("RegionTestAttributes");
		if(null != region){
			return region.split(",");
		}else{
			return null;
		}
	}

}
