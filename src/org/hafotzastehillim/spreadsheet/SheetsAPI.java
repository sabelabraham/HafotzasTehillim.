package org.hafotzastehillim.spreadsheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SheetsAPI {

	/** Application name. */
	private static final String APPLICATION_NAME = "PointEntry";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/pointentry");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/sheets.googleapis.com-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

	static {
		init();
	}

	private static LocalServerReceiver server;

	private static void init() {
		try {
			// FIXME add livigent via trustCertificates()
			HTTP_TRANSPORT = new NetHttpTransport.Builder().doNotValidateCertificate().build();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Creates an authorized Credential object.
	 *
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = SheetsAPI.class.getResourceAsStream("/resources/credentials/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		server = new LocalServerReceiver();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, server).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());

		server = null;
		return credential;
	}

	public static void logout() throws IOException {
		for (java.io.File f : DATA_STORE_DIR.listFiles())
			Files.delete(f.toPath());

		init(); // drop cached credentials
	}

	public static String consentUrl() {
		if (server == null)
			return null;

		return "https://accounts.google.com/o/oauth2/auth?" + "access_type=offline&"
				+ "client_id=1033113482447-i1du04e2mrtrddfjsahtpmvfa19987mn.apps.googleusercontent.com&"
				+ "redirect_uri=http://localhost:" + server.getPort() + "/Callback&response_type=code&"
				+ "scope=https://www.googleapis.com/auth/spreadsheets";

	}

	// FIXME
	@SuppressWarnings("unchecked")
	public static boolean isLoggedIn() throws IOException {
		if (DATA_STORE_DIR.list().length == 0)
			return false;

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_STORE_DIR.listFiles()[0]))) {
			HashMap<String, byte[]> map = (HashMap<String, byte[]>) in.readObject();
			byte[] data = map.get("user");

			return data != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static void stop() {
		if (server == null)
			return;

		try {
			server.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Build and return an authorized Sheets API client service.
	 *
	 * @return an authorized Sheets API client service
	 * @throws IOException
	 */
	public static Sheets getSheetsService() throws IOException {
		Credential credential = authorize();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

}