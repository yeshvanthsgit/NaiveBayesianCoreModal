package com.mkyong.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
	private static String UPDATE_DATA_SITE = "";
	private static String PATH_TO_SAVE_UPDATED_SITE = "";
	private static String pastHistoryCsvFile = "";
	private static String PAST_HISTORY_ARFF_FILE = "";
	private static String predictableCsvFile = "";
	private static String PREDICTABLE_ARFF_FILE = "";
	private static String SITE_TEST_DATA_URL = "";
	private static String SITE_TRAIN_DATA_URL = "";
	private static String OUTPUT_SITE_JSON = "";
	static List<String> siteAttributesList = null;

	public static void main(String[] args) throws Exception {
		try {
			generateCsvs();
			
			File input = new File(predictableCsvFile);
			File output = new File("OUTPUT_SITE_JSON");

			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			ObjectMapper mapper = new ObjectMapper();

			List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

			mapper.writerWithDefaultPrettyPrinter().writeValue(output, readAll);

			convertCsvToArff(pastHistoryCsvFile, PAST_HISTORY_ARFF_FILE, predictableCsvFile, PREDICTABLE_ARFF_FILE);

//			NaiveBayes nb = modalBuildingAndEvaluation();
//
//			Instances testdata = loadTestDataToPredict();
//
//			predictFromNativeBayesModal(testdata, nb, readAll, mapper);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateCsvs() throws Exception {
		Properties prop = null;
		prop = new Properties();
		InputStream is = SiteLevelPredictor.class.getResourceAsStream("./config1.properties");
		prop.load(is);
		UPDATE_DATA_SITE = prop.getProperty("UpdateTestDataSite");
		PATH_TO_SAVE_UPDATED_SITE = prop.getProperty("PathToSaveUpdatedSite");
		pastHistoryCsvFile = prop.getProperty("pastHistorySiteCsvFile");
		predictableCsvFile = prop.getProperty("predictableSiteCsvFile");
		SITE_TEST_DATA_URL = prop.getProperty("SiteTestDataURL");
		SITE_TRAIN_DATA_URL = prop.getProperty("SiteTrainDataURL");
		PAST_HISTORY_ARFF_FILE = prop.getProperty("pastHistorysiteArffFile");
		PREDICTABLE_ARFF_FILE = prop.getProperty("predictableSiteArffFile");
		OUTPUT_SITE_JSON =  prop.getProperty("OutputSiteJson");
		
		
		
		JsonCsvUtils jsonCsvUtils = new JsonCsvUtilsImpl();
		jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl(SITE_TRAIN_DATA_URL),
				pastHistoryCsvFile);
		jsonCsvUtils.jsonToCsv(JsonReader.readJsonArrayFromUrl(SITE_TEST_DATA_URL),
				predictableCsvFile);
	}

	public static void predictSite() throws Exception {
		try {

			generateCsvs();

			File input = new File(predictableCsvFile);
			File output = new File("OUTPUT_SITE_JSON");

			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			ObjectMapper mapper = new ObjectMapper();

			List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

			mapper.writerWithDefaultPrettyPrinter().writeValue(output, readAll);

			convertCsvToArff(pastHistoryCsvFile, PAST_HISTORY_ARFF_FILE, predictableCsvFile, PREDICTABLE_ARFF_FILE);

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
		for (String attr : rc.getSiteAttributes()) {
			if (data.attribute(attr) != null) {
				data.deleteAttributeAt(data.attribute(attr).index());
			}
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(arffFileName));
		writer.write(data.toString());
		writer.flush();
		writer.close();

		siteAttributesList = new ArrayList<String>();

		BufferedReader b = new BufferedReader(new FileReader(arffFileName));

		String readLine = "";

		System.out.println("Reading file using Buffered Reader");

		while ((readLine = b.readLine()) != null) {
			if (readLine.contains("@attribute")) {
				System.out.println(readLine);
				siteAttributesList.add(readLine);
			}
		}
	}

	private static void generateArffCurrentFiles(String excelPath, String arffFileName) {

		try {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(excelPath));
			Instances data = loader.getDataSet();

			ReadConfig rc = new ReadConfig();
			for (String attr11 : rc.getSiteAttributes1()) {
				if (data.attribute(attr11) != null) {
					data.deleteAttributeAt(data.attribute(attr11).index());
				}
			}

			FastVector attVals = new FastVector();
			attVals.addElement("GOOD");
			attVals.addElement("BAD");
			attVals.addElement("AVERAGE");
			Attribute attribute = new Attribute("Overall_Site_Performance", attVals);
			data.insertAttributeAt(attribute, data.numAttributes());

			BufferedWriter writer = new BufferedWriter(new FileWriter("sampleTrainArff2.arff"));
			writer.write(data.toString());
			writer.flush();
			writer.close();
			String s;
		    String totalStr = "";
		    int count = 0;
			
		    BufferedReader b = new BufferedReader(new FileReader("sampleTrainArff2.arff"));

            String readLine = "";

            System.out.println("Reading file using Buffered Reader");

            while ((readLine = b.readLine()) != null) {
            	if(readLine.contains("@attribute")){
            		totalStr += siteAttributesList.get(count) + "\n";
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

	public static void convertCsvToArff(String csv1, String arff1, String csv2, String arff2) throws IOException {
		generateArffPastFiles(csv1, arff1);
		generateArffCurrentFiles(csv2, arff2);
	}

	private static Instances prepareFeaturesToBuildBayesModal() throws Exception {
		DataSource source = new DataSource(PAST_HISTORY_ARFF_FILE);
		Instances traindata = source.getDataSet();
		traindata.setClassIndex(traindata.numAttributes() - 1);
		int numClasses = traindata.numClasses();
		for (int i = 0; i < numClasses; i++) {
			String classValue = traindata.classAttribute().value(i);
		}
		return traindata;
	}

	private static Instances loadTestDataToPredict() throws Exception {
		DataSource source2 = new DataSource(PREDICTABLE_ARFF_FILE);
		Instances testdata = source2.getDataSet();
		testdata.setClassIndex(testdata.numAttributes() - 1);
		return testdata;
	}

	private static NaiveBayes buildBayesModal(Instances ins) throws Exception {
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(ins);
		Evaluation eval_train = new Evaluation(ins);
		eval_train.evaluateModel(nb, ins);
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
		File outputPred = new File(PATH_TO_SAVE_UPDATED_SITE);
		mapper.writerWithDefaultPrettyPrinter().writeValue(outputPred, readAll);
		JsonReader.postMultiPartFile(UPDATE_DATA_SITE, PATH_TO_SAVE_UPDATED_SITE);
	}

	private static NaiveBayes modalBuildingAndEvaluation() throws Exception {
		DataSource source = new DataSource(PAST_HISTORY_ARFF_FILE);
		Instances dataset = source.getDataSet();
		// set class index to the last attribute
		dataset.setClassIndex(dataset.numAttributes() - 1);

		// create the classifier
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(dataset);

		// int seed = 1;
		// int folds = 5;
		// // randomize data
		// Random rand = new Random(seed);
		// //create random dataset
		// Instances randData = new Instances(dataset);
		// randData.randomize(rand);
		// //stratify
		// if (randData.classAttribute().isNominal())
		// randData.stratify(folds);
		//
		// // perform cross-validation
		// for (int n = 0; n < folds; n++) {
		// Evaluation eval = new Evaluation(randData);
		// //get the folds
		// Instances train = randData.trainCV(folds, n);
		// Instances test = randData.testCV(folds, n);
		// // build and evaluate classifier
		// nb.buildClassifier(train);
		// eval.evaluateModel(nb, test);
		// }
		return nb;
	}

}
