package edu.usc.cssl.tacit.crawlers.typepad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;


public class TypePadCrawler {
	public static String OUTPUT_PATH = "/Users/CSSLadmin/Desktop/TypePadOutput";
	
	public static void main(String[] args) throws Exception{
		
		TypePadCrawler typePadCrawler = new TypePadCrawler();
		typePadCrawler.getQueryResults("star wars",1);
	
	}
	
	/**
	 * This method returns the JSON HTTP response string for the input string URL 
	 * @param url Connection URL
	 * @return JSON response string if the connection was successful and response was received else it returns an empty string.
	 */
	private String getHTTPResponse(String url) {
		
		BufferedReader br = null;
		HttpURLConnection con = null;
		String inputLine;
		StringBuffer response = new StringBuffer();
		try{
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setConnectTimeout(15000);

			int responseCode = con.getResponseCode();
			
			if(responseCode == HttpURLConnection.HTTP_OK){
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);

				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((inputLine = br.readLine()) != null) {
					response.append(inputLine);
				}
				br.close();
		
				return response.toString();
			}else{
				System.out.println("Internal Connection Error.");
				return "";
			}
			
		}catch(SocketTimeoutException e){
			System.out.println("Connection is taking too long.There is something wrong with the server.");
			System.out.println(e.getMessage());
			return "";
		}catch(UnknownHostException e){
			System.out.println("There seems to be no internet connection.");
			System.out.println(e.getMessage());
			return "";
		}catch(Exception e){
			System.out.println(e.getMessage());
			return "";
		}finally{
			if (con != null){
				con.disconnect();
			}
		}
		

	}
	
	/**
	 * This method builds a query string from the raw query string.The returned query can be directly added to the URL to interact with the API and retrieve results.
	 * @param rawQuery (eg. star wars)
	 * @param sortParam 0: published_time_relevance, 1: relevance, 2: published_time_asc, 3: published_time_desc, -1: no sort 
	 * @return buildQuery (eg. q=star%20wars)
	 */
	private String buildQuery(String rawQuery, int sortParam){
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
		
		
		//TODO: Temporary lines of code. Can be removed
		finalBuildQuery = "\""+ finalBuildQuery + "\"";
		finalBuildQuery = "title:"+finalBuildQuery;
		
		
		
		finalBuildQuery = WebConstants.QUERY+finalBuildQuery;
		
		if (sortParam >= 0){
			finalBuildQuery = finalBuildQuery + WebConstants.PARAM_SEPARATOR + WebConstants.SORT + WebConstants.SORT_PARAMS[sortParam];
		}
		
		return finalBuildQuery;
		
	}
	
	/**
	 * This method outputs the query result blogs into files. 
	 * @param rawQuery (eg. star wars)
	 * @param sortParam 0: published_time_relevance, 1: relevance, 2: published_time_asc, 3: published_time_desc, -1: no sort
	 * @throws Exception
	 */
	public void getQueryResults(String rawQuery, int sortParam)throws Exception{

		//Initial Query Results
		String url = WebConstants.BASE_URL+WebConstants.ASSETS+WebConstants.QUERY_SEPARATOR;
		//Adding the query string
		url = url + buildQuery("star wars",sortParam);
		
		//Default Variable Declaration 
		JSONObject resultJSONObject = null; 
		JSONArray retrievedEntriesArray = null;
		String moreResultsToken = "";
		String httpResponse = "";
		FileWriter fw = null;
		int blogCount = 1;
		
		//Generating common output file name 
		Date currentDate = new Date();
		String commonFileName = new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(currentDate);
		
		do{
			
			httpResponse = getHTTPResponse(url);
			if (!httpResponse.equals("")){
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
				
				url = WebConstants.BASE_URL+WebConstants.ASSETS+WebConstants.QUERY_SEPARATOR+WebConstants.START_TOKEN + moreResultsToken;
			}else{
				break;
			}

		}while(moreResultsToken != null);
	}

}
