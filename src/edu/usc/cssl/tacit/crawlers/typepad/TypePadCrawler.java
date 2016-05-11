package edu.usc.cssl.tacit.crawlers.typepad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;


public class TypePadCrawler {
	public static String OUTPUT_PATH = "/Users/CSSLadmin/Desktop/TypePadOutput";

	public static void main(String[] args) throws Exception{
		
		TypePadCrawler typePadCrawler = new TypePadCrawler();
		typePadCrawler.getQueryResults("star wars");
	
	}
	
	//TODO: Improve Http connection code
	private String getHTTPResponse(String url) throws Exception {

		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//System.out.println(response.toString());
		
		return response.toString();

	}
	
	/**
	 * This method builds a query string from the raw query string.The returned query can be directly added to the URL to interact with the API and retrieve results.
	 * @param rawQuery (eg. star wars)
	 * @return buildQuery (eg. q=star%20wars)
	 */
	private String getQuerySubURL(String rawQuery){
		rawQuery = rawQuery.trim();
		String queryKeywords [] = rawQuery.split(" ");
		
		StringBuilder buildQuery = new StringBuilder();
		String finalBuildQuery = "";
		for (String queryKeyword : queryKeywords){
			if (!queryKeyword.equals("")){
				buildQuery.append(queryKeyword+"%20");
			}
		}

		buildQuery.delete(buildQuery.length()-3, buildQuery.length());
		finalBuildQuery = buildQuery.toString();
		finalBuildQuery = WebConstants.QUERY+finalBuildQuery;
		
		return finalBuildQuery;
		
	}
	
	/**
	 * This method outputs the query result blogs into files. 
	 * @param rawQuery (eg. star wars)
	 * @throws Exception
	 */
	public void getQueryResults(String rawQuery)throws Exception{

		//Initial Query Results
		String url = WebConstants.BASE_URL+WebConstants.ASSETS+WebConstants.PARAM_SEPARATOR;
		//Adding the query string
		url = url + getQuerySubURL("star wars");
		
		//Default Variable Declaration 
		JSONObject resultJSONObject = null; 
		JSONArray retrievedEntriesArray = null;
		String moreResultsToken = "";
		FileWriter fw = null;
		int blogCount = 1;
		
		//Generating common output file name 
		Date currentDate = new Date();
		String commonFileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(currentDate);
		
		do{

			resultJSONObject = new JSONObject(getHTTPResponse(url));
			retrievedEntriesArray = resultJSONObject.getJSONArray("entries");
			try{
				moreResultsToken = resultJSONObject.getString("moreResultsToken");
			}catch(JSONException e){
				moreResultsToken = null;
			}
			
			
			JSONObject retrievedEntryObject = null;
			String retrievedEntryContent = "";
			String finalEntryContent = "";
			
			for (int i= 0; i<retrievedEntriesArray.length();i++ ){
				fw = new FileWriter(new File(OUTPUT_PATH + File.separator + "blog_"+blogCount+"_"+commonFileName));
				retrievedEntryObject = (JSONObject)retrievedEntriesArray.get(i);
				retrievedEntryContent = retrievedEntryObject.getString("content");
				finalEntryContent = Jsoup.parse(retrievedEntryContent).text();
				fw.write(finalEntryContent);
				fw.close();
				blogCount++;
			}
			
			url = WebConstants.BASE_URL+WebConstants.ASSETS+WebConstants.PARAM_SEPARATOR+WebConstants.START_TOKEN + moreResultsToken;
			
		}while(moreResultsToken != null);
	}

}
