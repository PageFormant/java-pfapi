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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

public class Client {
	String APIKEY;
	String APIPASSWD;
	
	String VERSION = "1.0";
	String BASE_URL = "http://pageformant.de/";
	String API_URL = "api/betreiber/nvp/";
	
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
		
		List<NameValuePair> params = this.getDefaultParams();
		params.add(new NameValuePair("METHOD","PostMessage"));
		params.add(new NameValuePair("SERVICEID",String.valueOf(svcID)));
		params.add(new NameValuePair("MESSAGE",message));
		params.add(new NameValuePair("LINK",link));
		
		this.lastError = null;
		this.sendRequest(params);
		
		return this.lastError == null;
	}
	
	private void sendRequest(List<NameValuePair> params){
		HttpClient client = new HttpClient();
		
		PostMethod method = new PostMethod(BASE_URL + API_URL);
		method.addRequestHeader("User-Agent","PF-Agent");
		try{
			
			method.setRequestBody(params.toArray(new NameValuePair[params.size()]));
			
            int statusCode = client.executeMethod(method);
    		InputStream is = method.getResponseBodyAsStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1024];
            String cont = "";
            while(isr.read(buffer) != -1){
            	cont += String.valueOf(buffer);
            }
            
            this.handleResponse(statusCode, cont);
		}
        catch(IOException e) {
        	this.lastError = new PageFormantException(e.getMessage());
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
		result.add(new NameValuePair("APIVERSION", 	this.VERSION)); 
		result.add(new NameValuePair("APIKEY", 		this.APIKEY));
		result.add(new NameValuePair("APIPASSWD", 	this.APIPASSWD));
		return result;
	}
	
}
