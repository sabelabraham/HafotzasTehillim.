package org.hafotzastehillim.fx;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;

import org.hafotzastehillim.fx.spreadsheet.LoginDialog;
import org.hafotzastehillim.fx.spreadsheet.SheetsAPI;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
	public static final int FIRST_SHAVUOS_YEAR = 2017;
	public static final Image ICON;

	static {
		ICON = new Image(Main.class.getResource("/resources/images/logo.png").toExternalForm());
	}

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
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Model.getInstance().setPrimaryStage(primaryStage);

		initSingleInstance();
		running = true;
		app = this;

		SearchView searchView = new SearchView();
		snackbar = new JFXSnackbar(searchView);

		BorderPane border = new BorderPane(searchView);
		border.setTop(new DateTimeBar());

		Scene s = new Scene(border);

		s.getStylesheets().add(getClass().getResource("/resources/css/root.css").toExternalForm());

		primaryStage.getIcons().add(ICON);
		primaryStage.setTitle("Point Entry");
		primaryStage.setScene(s);
		primaryStage.show();

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

					Platform.runLater(() -> Model.getInstance().getPrimaryStage().toFront());
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
