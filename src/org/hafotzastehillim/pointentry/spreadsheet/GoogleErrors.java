package org.hafotzastehillim.pointentry.spreadsheet;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

public class GoogleErrors {

	private GoogleErrors() {

	}

	public static boolean handle(Exception e) {
		if (!(e instanceof GoogleJsonResponseException))
			return false;
		
		GoogleJsonResponseException ge = (GoogleJsonResponseException)e;
		
//		if(ge.getStatusCode())
		
		return false;
	}
}
