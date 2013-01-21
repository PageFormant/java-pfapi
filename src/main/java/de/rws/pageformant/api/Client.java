package de.rws.pageformant.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Client {
	String APIKEY;
	String APIPASSWD;
	
	static String VERSION = "1.0";
	static String BASE_URL = "http://pageformant.de/";
	static String API_URL = "api/betreiber/nvp/";
	
	PageFormantException lastError = null;
	
	public PageFormantException lastError(){
		return lastError;
	}
	
	public Client(String key, String pass){
		this.APIKEY = key;
		this.APIPASSWD = pass;
	}
	
	public boolean sendMessage(int svcID, String message, String link){
		if(message.length() > 180 ){
			this.lastError  = new PageFormantException("Your message is too long!");
			return false;
		}
		
		
		List <NameValuePair> params = this.getDefaultParams();
		params.add(new BasicNameValuePair("METHOD","PostMessage"));
		params.add(new BasicNameValuePair("SERVICEID",String.valueOf(svcID)));
		params.add(new BasicNameValuePair("MESSAGE",message));
		params.add(new BasicNameValuePair("LINK",link));
		
		this.lastError = null;
		this.sendRequest(params);
		
		return this.lastError == null;
	}
	
	private void sendRequest(List<NameValuePair> params){
		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost(Client.BASE_URL + Client.API_URL);
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpclient.execute(httpPost);

		    int statusCode = response.getStatusLine().getStatusCode();
		    HttpEntity entity = response.getEntity();
		    InputStream is = entity.getContent();
			InputStreamReader isr = new InputStreamReader(is);
			char[] buffer = new char[1024];
			String cont = "";
			while(isr.read(buffer) != -1){
			  cont += String.valueOf(buffer);
			}
			  
			this.handleResponse(statusCode, cont);// do something useful with the response body
		    // and ensure it is fully consumed
		    EntityUtils.consume(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    httpPost.releaseConnection();
		}
	}
	
	private void handleResponse(int code, String content){
		if(code == 200) return;
		HashMap<String, String> nvps = new HashMap<String, String>();
		try {
			StringTokenizer tokenizer = new StringTokenizer(content, "&");
    		while (tokenizer.hasMoreTokens())
    		{
    			StringTokenizer internTokenizer = new StringTokenizer(tokenizer.nextToken(), "=");
    			if (internTokenizer.countTokens() == 2)
    			{
    				nvps.put(URLDecoder.decode(internTokenizer.nextToken(), "UTF-8"),
    						URLDecoder.decode(internTokenizer.nextToken(), "UTF-8"));
    			}
    		}
    		this.lastError = new PageFormantException(nvps.get("ERRSTR"), Integer.parseInt(nvps.get("ERRCODE")));
		} catch (UnsupportedEncodingException e) {
			this.lastError = new PageFormantException(e.getMessage());
		}
	}
	
	private List<NameValuePair> getDefaultParams(){
		List<NameValuePair> result = new LinkedList<NameValuePair>();
		result.add(new BasicNameValuePair("APIVERSION", 	Client.VERSION)); 
		result.add(new BasicNameValuePair("APIKEY", 		this.APIKEY));
		result.add(new BasicNameValuePair("APIPASSWD", 	this.APIPASSWD));
		return result;
	}
	
}
