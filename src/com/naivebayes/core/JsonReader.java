package com.naivebayes.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonReader {

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException, ParseException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			Object object = null;
			JSONArray arrayObj = null;
			JSONParser jsonParser = new JSONParser();
			object = jsonParser.parse(jsonText);
			arrayObj = (JSONArray) object;
			System.out.println("Json object :: " + arrayObj);
			JSONObject json = (JSONObject) arrayObj.get(0);
			return json;
		} finally {
			is.close();
		}
	}

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException, ParseException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			Object object = null;
			JSONArray arrayObj = null;
			JSONParser jsonParser = new JSONParser();
			object = jsonParser.parse(jsonText);
			arrayObj = (JSONArray) object;
			return arrayObj;
		} finally {
			is.close();
		}
	}

	public static void postJson(String url, org.json.simple.JSONArray jsonArray) throws Exception {
		URL postURL = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) postURL.openConnection();
		connection.setConnectTimeout(5000);// 5 secs
		connection.setReadTimeout(5000);// 5 secs

		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data");

		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(jsonArray.toJSONString());
		out.flush();
		out.close();

		int res = connection.getResponseCode();

		System.out.println(res);

		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		connection.disconnect();

	}

	public static HttpResponse postMultiPartFile(String url, String path) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		
		File logFileToUpload = new File(path);
		FileBody uploadFilePart = new FileBody(logFileToUpload);
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("file", uploadFilePart);
		httpPost.setEntity(reqEntity);

		return httpclient.execute(httpPost);
	}

	public static void main(String[] args) throws Exception {
		try{
//			System.out.println(postMultiPartFile("http://localhost:8544/refinery/updateData/TestDB/Region").getStatusLine());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
