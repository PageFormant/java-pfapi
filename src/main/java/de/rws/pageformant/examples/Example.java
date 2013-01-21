package de.rws.pageformant.examples;

import de.rws.pageformant.api.Client;

public class Example {

	public static void main(String[] args) {
		int svcID = 1;
		
		String KEY = "===FILL=ME===";
		String PASSWD = "===FILL=ME===";
		
		String message = "THIS IS YOUR MESSAGE";
		String link = "http://YOUR.LINK.FOR.THE.MESSAGE/";
		
		Client pfClient = new Client(KEY, PASSWD);
		
		if(pfClient.sendMessage(svcID, message, link)){
			System.out.println("OK");
		}else{
			System.err.println(pfClient.lastError().getMessage());
		}
		
	}
}
