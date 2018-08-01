package com.naivebayes.core;
import java.util.List;

public class OilRefinariesList {
	private static List<String> refinariesList;
	private static List<String> siteList;

	public static List<String> getRefinariesList() {
		return refinariesList;
	}

	public static void setRefinariesList(List<String> refinariesList) {
		OilRefinariesList.refinariesList = refinariesList;
	}

	public static List<String> getSiteList() {
		return siteList;
	}

	public static void setSiteList(List<String> siteList) {
		OilRefinariesList.siteList = siteList;
	}
	
}
