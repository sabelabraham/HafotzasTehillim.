package org.hafotzastehillim.pointentry.spreadsheet;

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

import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.sheets.v4.Sheets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.util.DialogUtils;

public class SheetsAPI {

	/** Application name. */
	private static final String APPLICATION_NAME = "PointEntry";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/pointentry");

	private static final java.io.File DATA_STORE = new java.io.File(DATA_STORE_DIR, "StoredCredential");

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
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, "email", "profile");

	private static LocalServerReceiver server;

	private static Userinfoplus userinfo;

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

		requestUserInfo(credential);

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

					if (code == null)
						return null;

					TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirect).execute();

					signedInCurrentSession = true;

					Credential c = flow.createAndStoreCredential(response, "user");
					requestUserInfo(c);

					return c;
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

				if (code == null)
					return null;

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

	public static void logout() {
		for (java.io.File f : DATA_STORE_DIR.listFiles())
			try {
				Files.deleteIfExists(f.toPath());
			} catch (IOException e) {
			}

		init(); // drop cached credentials
	}

	public static String consentUrl(Collection<String> scopes) {
		if (server == null)
			return null;

		String loginHint = "";
		if (getUserInfo() != null && getUserInfo().getEmail() != null) {
			loginHint = "&login_hint=" + getUserInfo().getEmail();
		}
		String scopeString = "";
		if (!scopes.isEmpty()) {
			scopeString = "&scope=" + scopes.stream().collect(Collectors.joining("%20"));
		}

		try {
			int port = server.getPort();
			if (port == -1) {
				server.getRedirectUri();
				port = server.getPort();
				server.stop();
			}

			String url = "https://accounts.google.com/o/oauth2/auth?access_type=offline&"
					+ "client_id=1033113482447-i1du04e2mrtrddfjsahtpmvfa19987mn.apps.googleusercontent.com&"
					+ "redirect_uri=http://localhost:" + port + "/Callback&response_type=code" + scopeString
					+ loginHint;

			return url;

		} catch (IOException e) {
			return null;
		}

	}

	public static String consentUrl() {
		return consentUrl(SCOPES);
	}

	@SuppressWarnings("unchecked")
	public static boolean isLoggedIn() {
		if (signedInCurrentSession())
			return true;

		if (DATA_STORE_DIR.list().length == 0)
			return false;

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_STORE_DIR.listFiles()[0]))) {
			HashMap<String, byte[]> map = (HashMap<String, byte[]>) in.readObject();
			byte[] data = map.get("user");

			return data != null;
		} catch (Exception e) {
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
		return doPriviligedFXOrElseNoCache(sheetId, message, run, () -> DialogUtils.createAlert(AlertType.ERROR,
				"Access Denied", "Access Denied", "You don't have the valid credentials to access " + message));
	}

	public static boolean doPriviligedFXNoCache(String sheetId, Runnable run) {
		return doPriviligedFXNoCache(sheetId, null, run);
	}

	public static boolean doPriviligedFX(String sheetId, String message, boolean showDialogWhenLoggedIn, Runnable run) {
		return doPriviligedFXOrElse(sheetId, message, showDialogWhenLoggedIn, run,
				() -> DialogUtils.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
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
		if (!isLoggedIn())
			return false;

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

	@SuppressWarnings("unchecked")
	private static void requestUserInfo(Credential credential) throws IOException {
		if (credential == null)
			return;

		Oauth2 auth = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
		userinfo = auth.userinfo().get().execute();

		HashMap<String, byte[]> store = null;
		if (!DATA_STORE.exists()) {
			store = new HashMap<>();
		} else {
			try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(DATA_STORE.toPath()))) {
				try {
					store = (HashMap<String, byte[]>) in.readObject();
				} catch (ClassNotFoundException e) {
				}
			}
		}

		store.put("profile", userinfo.toString().getBytes());

		try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(DATA_STORE.toPath()))) {
			out.writeObject(store);
		}
	}

	@SuppressWarnings("unchecked")
	private static void loadUserInfo() {
		if (!DATA_STORE.exists())
			return;

		try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(DATA_STORE.toPath()))) {
			HashMap<String, byte[]> store = (HashMap<String, byte[]>) in.readObject();
			byte[] infoData = store.get("profile");

			if (infoData == null)
				return;

			String json = new String(infoData);

			userinfo = JSON_FACTORY.fromString(json, Userinfoplus.class);
		} catch (IOException | ClassNotFoundException e) {
			DialogUtils.showErrorDialog(e);
		}
	}

	public static Userinfoplus getUserInfo() {
		if (userinfo != null)
			return userinfo;

		loadUserInfo();

		return userinfo;

	}

	public static String getUsername() {
		if (getUserInfo() == null) {
			return "";
		}

		String name = getUserInfo().getName();
		if (name.isEmpty()) {
			return getUserInfo().getEmail();
		}

		if (name.contains(" | "))
			name = name.substring(0, name.indexOf(" | "));

		return name;
	}

}