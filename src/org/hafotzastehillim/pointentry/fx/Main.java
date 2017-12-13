package org.hafotzastehillim.pointentry.fx;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.fx.util.sync.Synchronize;
import org.hafotzastehillim.pointentry.spreadsheet.SheetsAPI;
import org.scenicview.ScenicView;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	public static final int FIRST_SHAVUOS_YEAR = 2017;

	public volatile static boolean running;
	public static boolean debug;
	
	private static Application app;
	private static JFXSnackbar snackbar;

	public static void main(String[] args) throws Exception {
		debug = true;
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		
		launch(args);
	}

	private ServerSocket server;
	private volatile Stage primary;

	@Override
	public void init() throws Exception {
		Synchronize.sync();
		
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			pushNotification("An unexpected error occured");
			DialogUtils.showErrorDialog(e);
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		DialogUtils.setupStage(primaryStage);

		primary = primaryStage;
		initSingleInstance();

		running = true;
		app = this;

		SearchView searchView = new SearchView();
		snackbar = new JFXSnackbar(searchView);

		BorderPane border = new BorderPane(searchView);
		border.setTop(new DateTimeBar());

		Scene s = new Scene(border);

		primaryStage.setTitle("Point Entry");
		primaryStage.setResizable(false);
		primaryStage.setScene(s);
		primaryStage.show();

		Model.getInstance().spreadsheetProperty().addListener((obs, ov, nv) -> {

			if (nv == null) {
				primaryStage.setTitle("Point Entry");
				return;
			}

			primaryStage.setTitle("Point Entry | " + SheetsAPI.getUsername());
		});

		// -javaagent:lib/scenicView.jar

	}

	@Override
	public void stop() throws Exception {
		running = false;

		SheetsAPI.stop();

		if (server != null)
			server.close();

		if (!Model.getInstance().isStaySignedIn()) {
			SheetsAPI.logout();
		}
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

	private void initSingleInstance() {
		try {
			server = new ServerSocket(9091, 0, InetAddress.getByName(null));
			Thread listen = new Thread(() -> {
				while (true) {
					try {
						server.accept().close();
					} catch (IOException e) {
					}

					Platform.runLater(() -> {
						primary.toFront();
					});
				}
			});

			listen.setDaemon(true);
			listen.start();
		} catch (BindException e) {
			try {
				Socket signal = new Socket("localhost", 9091);
				signal.close();

				System.out.println("Use existing instance.");
				System.exit(0);
			} catch (IOException e1) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
