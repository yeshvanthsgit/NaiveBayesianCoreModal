package com.mkyong.core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.parser.ParseException;

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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;

public class RefinaryLevelPredictor {
	private static final String PATH_TO_SAVE_UPDATED_REFINERY = "C:/FM/Bayesian/FInal project/RefineryAnalyticServiceFI/outputRefinaryPred.json";
	private static final String UPDATE_DATA_TEST_DB_REFINARY = "http://ushydykumarbar1:8082/refinery/updateData/TestDB/Refinary";
	private static final String REFINARY_DATA_TEST_ARFF = "OilRefinaryDataActual.arff";
	private static final String REFINARY_DATA_TRAIN_ARFF = "OilRefinaryDataPast.arff";
	private static final String REFINARY_TEST_DATA_CSV = "C:\\FM\\Bayesian\\FInal project\\actualOilRefinaryPrediction.csv";
	private static final String REFINARY_TRAIN_DATA_CSV = "C:\\FM\\Bayesian\\FInal project\\OilRefinaryData.csv";
	private static final String REFINERY_TEST_DATA_URL = "http://ushydykumarbar1:8082/refinery/fetchData/TestDB/Refinary";
	private static final String REFINERY_TRAIN_DATA_URL = "http://ushydykumarbar1:8082/refinery/fetchData/TrainDB/Refinary";
	static List<String> attributesList = null;

	public static void main(String[] args) throws Exception {
		try {
			predict();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void predict() throws Exception {
		RegionLevelPredictor.predictRegion();
		SiteLevelPredictor.predictSite();
		predictRefinery();
	}
	
	private static void generateRefineryCsvs() throws FileNotFoundException, JSONException, IOException, ParseException{
		JsonCsvUtils jsonCsvUtils = new JsonCsvUtilsImpl();
    	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl(REFINERY_TRAIN_DATA_URL), REFINARY_TRAIN_DATA_CSV);
    	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl(REFINERY_TEST_DATA_URL), REFINARY_TEST_DATA_CSV);
	}
	
	
	public static void predictRefinery() throws Exception {
		try {
			
			generateRefineryCsvs();
			
			String pastHistoryCsvFile = REFINARY_TRAIN_DATA_CSV;
			String pastHistoryArffFile = REFINARY_DATA_TRAIN_ARFF;
			
			String predictableCsvFile = REFINARY_TEST_DATA_CSV;
			String predictableArffFile = REFINARY_DATA_TEST_ARFF;
			
			File input = new File(predictableCsvFile);
			File output = new File("output.json");

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

	private static void generateArffPastFiles(String excelPath, String arffFileName) throws Exception {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(excelPath));
		Instances data = loader.getDataSet();
		
		attributesList = new ArrayList<String>();
		

		ReadConfig rc = new ReadConfig(); 
		for(String attr : rc.getNodeAttributes()){
			if(data.attribute(attr) != null){
				data.deleteAttributeAt(data.attribute(attr).index());	
			}
		}
		
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(arffFileName));
		writer.write(data.toString());
		writer.flush();
		writer.close();
		
		BufferedReader b = new BufferedReader(new FileReader(arffFileName));

        String readLine = "";

        System.out.println("Reading file using Buffered Reader");

        while ((readLine = b.readLine()) != null) {
        	if(readLine.contains("@attribute")){
        		System.out.println(readLine);
        		attributesList.add(readLine);
        	}
        }
	}

	private static void generateArffCurrentFiles(String excelPath, String arffFileName) {

		try {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(excelPath));
			Instances data = loader.getDataSet();
			
			ReadConfig rc = new ReadConfig(); 
			for(String attr11 : rc.getNodeAttributes1()){
				if(data.attribute(attr11) != null){
					data.deleteAttributeAt(data.attribute(attr11).index());	
				}
			}
			
			FastVector attVals = new FastVector();
			attVals.addElement("GOOD");
			attVals.addElement("BAD");
			attVals.addElement("AVERAGE");
			Attribute attribute = new Attribute("Overall_Refinery_Performance", attVals);
			data.insertAttributeAt(attribute, data.numAttributes());
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("sampleTrainArff.arff"));
			writer.write(data.toString());
			writer.flush();
			writer.close();
			String s;
		    String totalStr = "";
		    int count = 0;
			
		    BufferedReader b = new BufferedReader(new FileReader("sampleTrainArff.arff"));

            String readLine = "";

            System.out.println("Reading file using Buffered Reader");

            while ((readLine = b.readLine()) != null) {
            	if(readLine.contains("@attribute")){
            		totalStr += attributesList.get(count) + "\n";
            		count++;
            	}else{
            		totalStr += readLine + "\n";
            	}
                
            }
			
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(arffFileName));
			writer1.write(totalStr);
			writer1.flush();
			writer1.close();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void convertCsvToArff(String csv1, String arff1, String csv2, String arff2) throws Exception {
		generateArffPastFiles(csv1, arff1);
		generateArffCurrentFiles(csv2, arff2);
	}

	private static Instances prepareFeaturesToBuildBayesModal() throws Exception {
		DataSource source = new DataSource(REFINARY_DATA_TRAIN_ARFF);
		Instances traindata = source.getDataSet();
		traindata.setClassIndex(traindata.numAttributes() - 1);
		int numClasses = traindata.numClasses();
		for (int i = 0; i < numClasses; i++) {
			String classValue = traindata.classAttribute().value(i);
		}
		return traindata;
	}

	private static Instances loadTestDataToPredict() throws Exception {
		DataSource source2 = new DataSource(REFINARY_DATA_TEST_ARFF);
		Instances testdata = source2.getDataSet();
		testdata.setClassIndex(testdata.numAttributes() - 1);
		return testdata;
	}
	
	private static Instances loadTrainData() throws Exception {
		DataSource source2 = new DataSource(REFINARY_DATA_TRAIN_ARFF);
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
		try{
			for (int j = 0; j < testdata.numInstances(); j++) {
					Instance newInst = testdata.instance(j);
					double preNB = nb.classifyInstance(newInst);
					String predString = testdata.classAttribute().value((int) preNB);
					LinkedHashMap<String, String> mapInfo = (LinkedHashMap<String, String>) readAll.get(j);
					mapInfo.put("Overall_Refinery_Performance", predString);
			}
			File outputPred = new File(PATH_TO_SAVE_UPDATED_REFINERY);
			mapper.writerWithDefaultPrettyPrinter().writeValue(outputPred, readAll);
			
			JsonReader.postMultiPartFile(UPDATE_DATA_TEST_DB_REFINARY, PATH_TO_SAVE_UPDATED_REFINERY);
			PersistJsonToMongo.updateSiteAndRegionPerformaces();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static NaiveBayes modalBuildingAndEvaluation() throws Exception{
		DataSource source = new DataSource(REFINARY_DATA_TRAIN_ARFF);
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
