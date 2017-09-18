package org.hafotzastehillim.fx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.hafotzastehillim.fx.spreadsheet.SheetsAPI;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	public volatile static boolean running;
	private static Application app;

	private static JFXSnackbar snackbar;

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	private ServerSocket server;

	@Override
	public void init() throws Exception {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			pushNotification("An unexpected error occured");
			Util.showErrorDialog(e);

			e.printStackTrace();
		});

		server = new ServerSocket(9091, 0, InetAddress.getByName(null));
		running = true;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		app = this;

		View view = new View();
		snackbar = new JFXSnackbar(view);

		Scene s = new Scene(view);

		s.getStylesheets().add(getClass().getResource("/resources/css/root.css").toExternalForm());

		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/logo.png")));
		primaryStage.setTitle("Point Entry");
		primaryStage.setScene(s);
		primaryStage.show();

		if (Model.getInstance().getSpreadsheet() == null) // might be loaded as last connection
			view.showSpreadsheetDialog();
		if (Model.getInstance().getSpreadsheet() == null) // still didn't select something
			Platform.exit();
	}

	@Override
	public void stop() throws Exception {
		running = false;

		SheetsAPI.stop();

		if (server != null)
			server.close();
	}

	public static void showDocument(String url) {
		if (app != null)
			app.getHostServices().showDocument(url);
	}

	public static void pushNotification(String str) {
		if (snackbar == null)
			System.out.println(str);
		else
			snackbar.enqueue(new SnackbarEvent(str));
	}

}
