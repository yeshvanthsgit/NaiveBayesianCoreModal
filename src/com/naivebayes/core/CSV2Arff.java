package com.naivebayes.core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
 
public class CSV2Arff {
  /**
   * takes 2 arguments:
   * - CSV input file
   * - ARFF output file
   */
  public static void main(String[] args) throws Exception {
	  try{
		  generateArffPastFiles(args[0],args[1]);
		  generateArffCurrentFiles(args[2],args[3]);
	  }catch(Exception e){
		  e.printStackTrace();
	  }
  }
  
  public static void generateArffPastFiles(String excelPath, String arffFileName) throws IOException{
	  CSVLoader loader = new CSVLoader();
	  loader.setSource(new File(excelPath));
	  Instances data = loader.getDataSet();
	 
	  BufferedWriter writer = new BufferedWriter(new FileWriter(arffFileName));
      writer.write(data.toString());
      writer.flush();
      writer.close();
  }
  
  private static void generateArffCurrentFiles(String excelPath, String arffFileName) throws IOException{
	  CSVLoader loader = new CSVLoader();
	  loader.setSource(new File(excelPath));
	  Instances data = loader.getDataSet();
	  
	  Attribute attr = data.attribute(0);
	  
	  String refString = attr.toString().substring(attr.toString().indexOf('{')+1, attr.toString().indexOf('}'));
	  String[]  info = refString.split(",");
	  
	  OilRefinariesList.setRefinariesList(Arrays.asList(info));
	  
	  BufferedWriter writer = new BufferedWriter(new FileWriter(arffFileName));
      writer.write(data.toString());
      writer.flush();
      writer.close();
  }
}