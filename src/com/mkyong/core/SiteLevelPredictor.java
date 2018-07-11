package com.mkyong.core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class SiteLevelPredictor {
	private static final String UPDATE_DATA_SITE = "http://localhost:8082/refinery/updateData/TestDB/Site";
	private static final String SAVE_UPDATED_JSON = "C:/Users/rsriramakavacham/Desktop/AI/Latest/RefineryAnalyticServiceFI/outputSitePred.json";
	private static final String pastHistoryCsvFile = "C:\\Users\\rsriramakavacham\\Desktop\\Oil\\sitePast.csv ";
	private static final String pastHistoryArffFile = "sitePast.arff ";
	private static final String predictableCsvFile = "C:\\Users\\rsriramakavacham\\Desktop\\Oil\\site.csv ";
	private static final String predictableArffFile = "siteActual.arff";

	public static void main(String[] args) throws Exception {
		try {
			File input = new File(predictableCsvFile);
			File output = new File("outputSite.json");

			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			ObjectMapper mapper = new ObjectMapper();

			List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

			mapper.writerWithDefaultPrettyPrinter().writeValue(output, readAll);

			
			convertCsvToArff(pastHistoryCsvFile, pastHistoryArffFile, predictableCsvFile, predictableArffFile);

			NaiveBayes nb = modalBuildingAndEvaluation();

			Instances testdata = loadTestDataToPredict();

			predictFromNativeBayesModal(testdata, nb, readAll, mapper);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void generateCsvs() throws Exception{
		JsonCsvUtils jsonCsvUtils = new JsonCsvUtilsImpl();
    	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl("http://localhost:8082/refinery/fetchData/TrainDB/Site"), pastHistoryCsvFile);
    	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl("http://localhost:8082/refinery/fetchData/TestDB/Site"), predictableCsvFile);
	}
	
	public static void predictSite() throws Exception {
		try {
			
			generateCsvs();
			
			
			
			File input = new File(predictableCsvFile);
			File output = new File("outputSite.json");

			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			ObjectMapper mapper = new ObjectMapper();

			List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

			mapper.writerWithDefaultPrettyPrinter().writeValue(output, readAll);

			
			convertCsvToArff(pastHistoryCsvFile, pastHistoryArffFile, predictableCsvFile, predictableArffFile);

			NaiveBayes nb = modalBuildingAndEvaluation();

			Instances testdata = loadTestDataToPredict();

			predictFromNativeBayesModal(testdata, nb, readAll, mapper);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateArffPastFiles(String excelPath, String arffFileName) throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(excelPath));
		Instances data = loader.getDataSet();
		ReadConfig rc = new ReadConfig(); 
		for(String attr : rc.getSiteAttributes()){
			if(data.attribute(attr) != null){
				data.deleteAttributeAt(data.attribute(attr).index());	
			}
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(arffFileName));
		writer.write(data.toString());
		writer.flush();
		writer.close();
	}

	private static void generateArffCurrentFiles(String excelPath, String arffFileName) {

		try {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(excelPath));
			Instances data = loader.getDataSet();

			ReadConfig rc = new ReadConfig(); 
			for(String attr11 : rc.getSiteAttributes1()){
				if(data.attribute(attr11) != null){
					data.deleteAttributeAt(data.attribute(attr11).index());	
				}
			}

			FastVector attVals = new FastVector();
			attVals.addElement("GOOD");
			attVals.addElement("BAD");
			attVals.addElement("AVERAGE");
			Attribute attribute = new Attribute("Overall_Site_Performance", attVals);
			data.insertAttributeAt(attribute, data.numAttributes());

			BufferedWriter writer = new BufferedWriter(new FileWriter(arffFileName));
			writer.write(data.toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void convertCsvToArff(String csv1, String arff1, String csv2, String arff2) throws IOException {
		generateArffPastFiles(csv1, arff1);
		generateArffCurrentFiles(csv2, arff2);
	}

	private static Instances prepareFeaturesToBuildBayesModal() throws Exception {
		DataSource source = new DataSource("sitePast.arff");
		Instances traindata = source.getDataSet();
		traindata.setClassIndex(traindata.numAttributes() - 1);
		int numClasses = traindata.numClasses();
		for (int i = 0; i < numClasses; i++) {
			String classValue = traindata.classAttribute().value(i);
		}
		return traindata;
	}

	private static Instances loadTestDataToPredict() throws Exception {
		DataSource source2 = new DataSource("siteActual.arff");
		Instances testdata = source2.getDataSet();
		testdata.setClassIndex(testdata.numAttributes() - 1);
		return testdata;
	}

	private static NaiveBayes buildBayesModal(Instances ins) throws Exception {
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(ins);
		Evaluation eval_train = new Evaluation(ins);
	    eval_train.evaluateModel(nb,ins);
		return nb;
	}

	private static void predictFromNativeBayesModal(Instances testdata, NaiveBayes nb, List<Object> readAll,
			ObjectMapper mapper) throws Exception {
		for (int j = 0; j < testdata.numInstances(); j++) {
			double actualClass = testdata.instance(j).classValue();
			String actual = testdata.classAttribute().value((int) actualClass);
			Instance newInst = testdata.instance(j);
			double preNB = nb.classifyInstance(newInst);
			String predString = testdata.classAttribute().value((int) preNB);
			LinkedHashMap<String, String> mapInfo = (LinkedHashMap<String, String>) readAll.get(j);
			System.out.println(preNB + "::" + predString);
			mapInfo.put("Overall_Site_Performance", predString);
			mapInfo.put("Calculated_Site_Performance", predString);
		}
		File outputPred = new File(SAVE_UPDATED_JSON);
		mapper.writerWithDefaultPrettyPrinter().writeValue(outputPred, readAll);
		JsonReader.postMultiPartFile(UPDATE_DATA_SITE, SAVE_UPDATED_JSON);
	}
	
	private static NaiveBayes modalBuildingAndEvaluation() throws Exception{
		DataSource source = new DataSource("sitePast.arff");
		Instances dataset = source.getDataSet();	
		//set class index to the last attribute
		dataset.setClassIndex(dataset.numAttributes()-1);

		//create the classifier
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(dataset);

//		int seed = 1;
//		int folds = 5;
//		// randomize data
//		Random rand = new Random(seed);
//		//create random dataset
//		Instances randData = new Instances(dataset);
//		randData.randomize(rand);
//		//stratify	    
//		if (randData.classAttribute().isNominal())
//			randData.stratify(folds);
//
//		// perform cross-validation	    	    
//		for (int n = 0; n < folds; n++) {
//			Evaluation eval = new Evaluation(randData);
//			//get the folds	      
//			Instances train = randData.trainCV(folds, n);
//			Instances test = randData.testCV(folds, n);	      
//			// build and evaluate classifier	     
//			nb.buildClassifier(train);
//			eval.evaluateModel(nb, test);
//		}
		return nb;
	}

}
