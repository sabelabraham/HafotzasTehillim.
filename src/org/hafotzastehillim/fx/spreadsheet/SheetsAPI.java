package org.hafotzastehillim.fx.spreadsheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import javafx.application.Platform;
import javafx.beans.value.WritableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.scene.control.Alert.AlertType;
import com.google.api.services.sheets.v4.Sheets;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.hafotzastehillim.fx.util.Util;

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

	private static boolean signedInCurrentSession;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/sheets.googleapis.com-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

	private static LocalServerReceiver server;

	static {
		init();
	}

	private static void init() {
		signedInCurrentSession = false;
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

		Credential credential = flow.loadCredential("user");
		if (credential == null) {
			credential = new AuthorizationCodeInstalledApp(flow, server).authorize("user");
			System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
			signedInCurrentSession = true;
		}

		server = null;

		return credential;
	}

	public static Credential authorizeNoCache() throws IOException {
		if (signedInCurrentSession())
			return authorize();

		// Load client secrets.
		InputStream in = SheetsAPI.class.getResourceAsStream("/resources/credentials/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		server = new LocalServerReceiver();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, server).authorize(null);

		server = null;
		signedInCurrentSession = true;

		return credential;
	}

	public static Credential authorizeFX(String message, boolean showDialogWhenLoggedIn) throws IOException {
		// Load client secrets.
		InputStream in = SheetsAPI.class.getResourceAsStream("/resources/credentials/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		server = new LocalServerReceiver();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();

		Credential credential = flow.loadCredential("user");

		if (credential == null || showDialogWhenLoggedIn) {

			LoginDialog login = new LoginDialog(true);
			if (message != null)
				login.setCustomMessage(message);

			Task<Credential> task = new Task<Credential>() {
				@Override
				protected Credential call() throws Exception {
					String redirect = server.getRedirectUri();
					login.setWebAddress(consentUrl());

					String code = server.waitForCode();
					server.stop();

					TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirect).execute();

					signedInCurrentSession = true;

					return flow.createAndStoreCredential(response, "user");
				}
			};

			task.stateProperty().addListener((obs, ov, nv) -> {
				if (ov == State.RUNNING)
					stop();
			});
			task.setOnFailed(evt -> task.getException().printStackTrace());

			login.setSignInAction(() -> task);
			login.showAndWait();

			server = null;

			if (task.getState() == State.FAILED) {
				if (task.getException().getMessage().equals("User authorization failed (access_denied)"))
					throw (IOException) task.getException();
			}

			return task.getValue();
		}

		return credential;
	}

	public static Credential authorizeFXNoCache(String message) throws IOException {
		if (signedInCurrentSession())
			return authorizeFX(message, false);

		// Load client secrets.
		InputStream in = SheetsAPI.class.getResourceAsStream("/resources/credentials/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		server = new LocalServerReceiver();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setAccessType("offline").build();

		LoginDialog login = new LoginDialog(false);
		if (message != null)
			login.setCustomMessage(message);

		Task<Credential> task = new Task<Credential>() {
			@Override
			protected Credential call() throws Exception {
				String redirect = server.getRedirectUri();
				login.setWebAddress(consentUrl());
				
				String code = server.waitForCode();
				server.stop();

				TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirect).execute();

				signedInCurrentSession = true;
				return flow.createAndStoreCredential(response, null);
			}
		};

		task.stateProperty().addListener((obs, ov, nv) -> {
			if (ov == State.RUNNING)
				stop();
		});

		login.setSignInAction(() -> task);
		login.showAndWait();

		server = null;

		if (task.getState() == State.FAILED) {
			if (task.getException().getMessage().equals("User authorization failed (access_denied)"))
				throw (IOException) task.getException();
		}

		return task.getValue();
	}

	public static void logout() throws IOException {
		for (java.io.File f : DATA_STORE_DIR.listFiles())
			Files.delete(f.toPath());

		init(); // drop cached credentials
	}

	public static String consentUrl() {
		if (server == null)
			return null;

		try {
			int port = server.getPort();
			if (port == -1) {
				String url = "https://accounts.google.com/o/oauth2/auth?access_type=offline&"
						+ "client_id=1033113482447-i1du04e2mrtrddfjsahtpmvfa19987mn.apps.googleusercontent.com&"
						+ "redirect_uri=" + server.getRedirectUri() + "&response_type=code&"
						+ "scope=https://www.googleapis.com/auth/spreadsheets";
				server.stop();
				
				return url;
			}
			String url = "https://accounts.google.com/o/oauth2/auth?access_type=offline&"
					+ "client_id=1033113482447-i1du04e2mrtrddfjsahtpmvfa19987mn.apps.googleusercontent.com&"
					+ "redirect_uri=http://localhost:" + port + "/Callback&response_type=code&"
					+ "scope=https://www.googleapis.com/auth/spreadsheets";
			
			return url;

		} catch (IOException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public static boolean isLoggedIn() throws IOException {
		if (signedInCurrentSession())
			return true;

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

	public static boolean signedInCurrentSession() {
		return signedInCurrentSession;
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
		if (credential == null)
			return null;

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static Sheets getSheetsServiceNoCache() throws IOException {
		Credential credential = authorizeNoCache();
		if (credential == null)
			return null;

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static Sheets getSheetsServiceFX(String message, boolean showDialogWhenLoggedIn) throws IOException {
		Credential credential = authorizeFX(message, showDialogWhenLoggedIn);
		if (credential == null)
			return null;

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static Sheets getSheetsServiceFXNoCache(String message) throws IOException {
		Credential credential = authorizeFXNoCache(message);
		if (credential == null)
			return null;

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static GoogleSpreadsheet loadSpreadsheet() throws IOException {
		Sheets service = getSheetsService();
		if (service == null)
			return null;

		return new GoogleSpreadsheet(SheetID.DATABASE, service);
	}

	public static GoogleSpreadsheet loadSpreadsheetFX(boolean showDialogWhenLoggedIn) throws IOException {
		Sheets service = getSheetsServiceFX("Point Entry", showDialogWhenLoggedIn);
		if (service == null)
			return null;

		return new GoogleSpreadsheet(SheetID.DATABASE, service);
	}

	public static boolean doPriviliged(String sheetId, Runnable run) {
		return doPriviligedOrElse(sheetId, run, null);
	}

	public static boolean doPriviligedNoCache(String sheetId, Runnable run) {
		return doPriviligedOrElseNoCache(sheetId, run, null);
	}

	public static boolean doPriviligedOrElse(String sheetId, Runnable run, Runnable orElse) {
		try {
			getSheetsService().spreadsheets().get(sheetId).execute();
			run.run();
			return true;
		} catch (Exception e) {
			if (orElse != null)
				orElse.run();
			return true;
		}
	}

	public static boolean doPriviligedOrElseNoCache(String sheetId, Runnable run, Runnable orElse) {
		try {
			getSheetsServiceNoCache().spreadsheets().get(sheetId).execute();
			run.run();
			return true;
		} catch (Exception e) {
			if (orElse != null)
				orElse.run();
			return false;
		}
	}

	public static boolean doPriviligedFXNoCache(String sheetId, String message, Runnable run) {
		return doPriviligedFXOrElseNoCache(sheetId, message, run, () -> Util.createAlert(AlertType.ERROR,
				"Access Denied", "Access Denied", "You don't have the valid credentials to access " + message));
	}

	public static boolean doPriviligedFXNoCache(String sheetId, Runnable run) {
		return doPriviligedFXNoCache(sheetId, null, run);
	}

	public static boolean doPriviligedFX(String sheetId, String message, boolean showDialogWhenLoggedIn, Runnable run) {
		return doPriviligedFXOrElse(sheetId, message, showDialogWhenLoggedIn, run,
				() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to access " + message));
	}

	public static boolean doPriviligedFX(String sheetId, boolean showDialogWhenLoggedIn, Runnable run) {
		return doPriviligedFX(sheetId, null, showDialogWhenLoggedIn, run);
	}

	public static boolean doPriviligedFXOrElseNoCache(String sheetId, String message, Runnable run, Runnable orElse) {
		try {
			getSheetsServiceFXNoCache(message).spreadsheets().get(sheetId).execute();
			run.run();
			return true;
		} catch (Exception e) {
			if (orElse != null)
				orElse.run();
			return false;
		}
	}

	public static boolean doPriviligedFXOrElseNoCache(String sheetId, Runnable run, Runnable orElse) {
		return doPriviligedFXOrElseNoCache(sheetId, null, run, orElse);
	}

	public static boolean doPriviligedFXOrElse(String sheetId, String message, boolean showDialogWhenLoggedIn,
			Runnable run, Runnable orElse) {
		try {
			getSheetsServiceFX(message, showDialogWhenLoggedIn).spreadsheets().get(sheetId).execute();
			run.run();
			return true;
		} catch (Exception e) {
			if (orElse != null)
				orElse.run();
			return false;
		}
	}

	public static boolean doPriviligedFXOrElse(String sheetId, boolean showDialogWhenLoggedIn, Runnable run,
			Runnable orElse) {
		return doPriviligedFXOrElse(sheetId, null, showDialogWhenLoggedIn, run, orElse);
	}

	public static boolean hasPermission(String sheetId) {
		try {
			if (!isLoggedIn())
				return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			getSheetsService().spreadsheets().get(sheetId).execute();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static void hasPermissionFX(String sheetId, WritableValue<Boolean> consumer) {
		new Thread(() -> {
			boolean has = hasPermission(sheetId);
			Platform.runLater(() -> consumer.setValue(has));
		}).start();
	}

}