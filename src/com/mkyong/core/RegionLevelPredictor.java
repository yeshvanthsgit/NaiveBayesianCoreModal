package com.mkyong.core;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class RegionLevelPredictor {
	
	private static final String TRAIN_DB_REGION_URL = "http://localhost:8082/refinery/fetchData/TrainDB/Region";
	private static final String TEST_DB_REGION_URL = "http://localhost:8082/refinery/fetchData/TestDB/Region";
	private static final String PATH_TO_SAVE_UPDATED_REGION = "C:/Users/rsriramakavacham/Desktop/AI/Latest/RefineryAnalyticServiceFI/outputRegionPred.json";
	private static final String REGION_TEST_CSV = "C:\\Users\\rsriramakavacham\\Desktop\\Oil\\region.csv";
	private static final String REGION_TRAIN_CSV = "C:\\Users\\rsriramakavacham\\Desktop\\Oil\\regionPast.csv";
	private static final String UPDATED_REGION_URL = "http://localhost:8082/refinery/updateData/TestDB/Region";

	public static void predictRegion() throws Exception {
		try {
			
			generateRegionCsvs();
			
			String pastHistoryCsvFile = REGION_TRAIN_CSV;
			String pastHistoryArffFile = "regionPast.arff ";
			
			String predictableCsvFile = REGION_TEST_CSV;
			String predictableArffFile = "regionActual.arff";
			
			File input = new File(predictableCsvFile);
			File output = new File("outputRegion.json");

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
	
	private static void generateRegionCsvs() throws FileNotFoundException, JSONException, IOException, ParseException{
		JsonCsvUtils jsonCsvUtils = new JsonCsvUtilsImpl();
    	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl(TRAIN_DB_REGION_URL), REGION_TRAIN_CSV);
    	jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl(TEST_DB_REGION_URL), REGION_TEST_CSV);
	}

	private static void generateArffPastFiles(String excelPath, String arffFileName) throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(excelPath));
		Instances data = loader.getDataSet();
		ReadConfig rc = new ReadConfig(); 
		for(String attr : rc.getRegionAttributes()){
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
			for(String attr : rc.getRegionAttributes1()){
				if(data.attribute(attr) != null){
					data.deleteAttributeAt(data.attribute(attr).index());	
				}
			}

			FastVector attVals = new FastVector();
			attVals.addElement("GOOD");
			attVals.addElement("BAD");
			attVals.addElement("AVERAGE");
			Attribute attribute = new Attribute("Overall_Region_Performance", attVals);
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
		DataSource source = new DataSource("regionPast.arff");
		Instances traindata = source.getDataSet();
		traindata.setClassIndex(traindata.numAttributes() - 1);
		int numClasses = traindata.numClasses();
		for (int i = 0; i < numClasses; i++) {
			String classValue = traindata.classAttribute().value(i);
		}
		return traindata;
	}

	private static Instances loadTestDataToPredict() throws Exception {
		DataSource source2 = new DataSource("regionActual.arff");
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
			mapInfo.put("Overall_Region_Performance", predString);
			mapInfo.put("Calculated_Region_Performance", predString);
		}
		File outputPred = new File(PATH_TO_SAVE_UPDATED_REGION);
		mapper.writerWithDefaultPrettyPrinter().writeValue(outputPred, readAll);
		
		JsonReader.postMultiPartFile(UPDATED_REGION_URL, PATH_TO_SAVE_UPDATED_REGION);
        //PersistJsonToMongo.mongoJsonInsert(jsonArray, "RegionDB");
	}
	
	private static NaiveBayes modalBuildingAndEvaluation() throws Exception{
		DataSource source = new DataSource("regionPast.arff");
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
