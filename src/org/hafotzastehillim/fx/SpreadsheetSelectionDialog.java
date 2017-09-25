package org.hafotzastehillim.fx;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hafotzastehillim.fx.spreadsheet.GoogleSpreadsheet;
import org.hafotzastehillim.fx.spreadsheet.LocalSpreadsheet;
import org.hafotzastehillim.fx.spreadsheet.SheetsAPI;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.util.Util;

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.services.sheets.v4.Sheets;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXSpinner;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class SpreadsheetSelectionDialog extends Stage {

	private static final Preferences prefs = Preferences.userNodeForPackage(SpreadsheetSelectionDialog.class);
	private static final String LAST_CONNECTION_KEY = "LastConnection";
	private static final String LAST_FILE_KEY = "currentFile";
	private static final String LAST_SHEET_LINK_KEY = "LastSheetLink";

	private Scene scene;
	private VBox box;

	private JFXRadioButton local;
	private JFXRadioButton google;

	private File currentFile;
	private Label currentFilename;

	private HBox localSettings;
	private VBox googleSettings;

	private JFXButton chooseFile;
	private TextField link;
	private Label loggedIn;
	private Hyperlink logout;

	private JFXButton ok;
	private JFXButton browser;
	private JFXSnackbar snackbar;

	private Spreadsheet sheet;
	private Service<Sheets> loader;

	public SpreadsheetSelectionDialog() {
		setResizable(false);
		setTitle("Choose connection");
		getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/logo.png")));
		initModality(Modality.APPLICATION_MODAL);

		box = new VBox();
		StackPane root = new StackPane(box);
		root.setPrefWidth(600);

		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/resources/css/root.css").toExternalForm());
		setScene(scene);

		root.getStyleClass().add("spreadsheet-selection-dialog");
		root.getStylesheets()
				.add(getClass().getResource("/resources/css/spreadsheet-selection-dialog.css").toExternalForm());

		local = new JFXRadioButton("Load local file");
		local.setDisable(true); // Problems with unique id and formula evaluation
		google = new JFXRadioButton("Connect to Google Sheets");

		ToggleGroup group = new ToggleGroup();
		local.setToggleGroup(group);
		google.setToggleGroup(group);

		if (prefs.getInt(LAST_CONNECTION_KEY, 0) == 0)
			local.setSelected(true);
		else
			google.setSelected(true);
		Platform.runLater(() -> getScene().getRoot().requestFocus());

		localSettings = new HBox(25);
		localSettings.setPadding(new Insets(10, 0, 20, 40));
		localSettings.disableProperty().bind(local.selectedProperty().not());

		currentFile = new File(prefs.get(LAST_FILE_KEY, System.getProperty("user.home")));

		currentFilename = new Label();
		currentFilename.setPadding(new Insets(10, 0, 0, 0));
		if (currentFile.exists() && currentFile.isFile())
			currentFilename.setText(currentFile.getName());

		chooseFile = new JFXButton("Choose File");
		chooseFile.setId("choose-file");
		chooseFile.setOnAction(evt -> {

			FileChooser chooser = new FileChooser();
			chooser.setTitle("Choose Spreadsheet");
			chooser.setInitialDirectory(currentFile.isDirectory() ? currentFile : currentFile.getParentFile());
			chooser.setInitialFileName(currentFile.isDirectory() ? "" : currentFile.getName());
			chooser.getExtensionFilters().add(new ExtensionFilter("Spreadsheet", "*.xlsx"));

			File file = chooser.showOpenDialog(getScene().getWindow());
			if (file == null)
				return;

			currentFile = file;
			prefs.put(LAST_FILE_KEY, file.getAbsolutePath());
			currentFilename.setText(file.getName());
		});
		localSettings.getChildren().addAll(chooseFile, currentFilename);

		googleSettings = new VBox(10);
		googleSettings.setPadding(new Insets(0, 0, 0, 40));
		googleSettings.disableProperty().bind(google.selectedProperty().not());

		link = new TextField(prefs.get(LAST_SHEET_LINK_KEY, ""));
		link.setPromptText("Paste Google Sheet link");

		loggedIn = new Label("You are currently logged in, ");
		logout = new Hyperlink("log out");
		logout.setOnAction(evt -> {
			try {
				SheetsAPI.logout();
				pushNotification("Successfully logged out");
			} catch (IOException e) {
				Util.showErrorDialog(e);
				e.printStackTrace();
				pushNotification("Log out failed");
			}
			setupLogoutLabel();
		});
		setupLogoutLabel();

		googleSettings.getChildren().addAll(link, new HBox(loggedIn, logout));

		ok = new JFXButton("OK");
		ok.setOnAction(evt ->

		load());
		ok.setId("ok");
		HBox okHolder = new HBox(ok);
		okHolder.setAlignment(Pos.CENTER_RIGHT);

		Pane spacer = new Pane();
		VBox.setVgrow(spacer, Priority.SOMETIMES);
		Separator sep = new Separator();
		sep.setPadding(new Insets(5, 0, 10, 0));
		box.getChildren().addAll(local, localSettings, spacer, google, googleSettings, sep, okHolder);

		snackbar = new JFXSnackbar(box);

		loader = new Service<Sheets>() {
			@Override
			protected Task<Sheets> createTask() {
				return new Task<Sheets>() {
					@Override
					protected Sheets call() throws Exception {
						Sheets s = SheetsAPI.getSheetsService();
						setupLogoutLabel();
						return s;
					}
				};
			}
		};

		ColorAdjust adj = new ColorAdjust(0, -0.9, -0.5, 0);
		GaussianBlur blur = new GaussianBlur(55);
		adj.setInput(blur);

		box.mouseTransparentProperty().bind(loader.runningProperty());
		box.effectProperty().bind(Bindings.when(loader.runningProperty()).then(adj).otherwise((ColorAdjust) null));

		setOnCloseRequest(evt -> {
			SheetsAPI.stop();
			loader.cancel();
		});

		JFXSpinner spinner = new JFXSpinner();
		spinner.visibleProperty().bind(loader.runningProperty());

		browser = new JFXButton("Open Browser");
		browser.setId("browser");
		browser.visibleProperty().bind(loader.runningProperty());
		browser.setOnAction(evt -> {
			String url = SheetsAPI.consentUrl();
			if (url == null)
				return;

			Main.showDocument(url);
		});

		VBox spinnerAndButton = new VBox(spinner, browser);
		spinnerAndButton.setSpacing(7);

		root.getChildren().add(spinnerAndButton);
		spinnerAndButton.mouseTransparentProperty().bind(loader.runningProperty().not());
		spinnerAndButton.setAlignment(Pos.CENTER);

		initSpreadsheet();
	}

	public Spreadsheet getSpreadsheet() {
		return sheet;
	}

	private void initSpreadsheet() {
		int lastCon = prefs.getInt(LAST_CONNECTION_KEY, -1);
		if (lastCon == -1)
			return;

		if (lastCon == 0) {
			try {
				if (currentFile.exists() && currentFile.isFile())
					sheet = new LocalSpreadsheet(currentFile);
			} catch (InvalidFormatException | IOException e) {
				Util.showErrorDialog(e);
				e.printStackTrace();
			}
		} else {
			try {
				if (!SheetsAPI.isLoggedIn())
					return;

				String id = extractId(prefs.get(LAST_SHEET_LINK_KEY, null));
				if (id == null)
					return;

				sheet = new GoogleSpreadsheet(id, SheetsAPI.getSheetsService());
			} catch (IOException e) {
				Util.showErrorDialog(e);
				e.printStackTrace();
			}
		}
	}

	private void load() {
		if (local.isSelected()) {
			try {
				sheet = new LocalSpreadsheet(currentFile);
				prefs.putInt(LAST_CONNECTION_KEY, 0);
				close();
			} catch (InvalidFormatException | IOException e) {
				pushNotification("Load Failed");
				Util.showErrorDialog(e);
				e.printStackTrace();
			}
		} else {

			String spreadsheetId = extractId(link.getText());
			if (spreadsheetId == null) {
				pushNotification("Invalid Link");
				return;
			}
			prefs.put(LAST_SHEET_LINK_KEY, link.getText());

			loader.setOnFailed(evt -> {
				pushNotification("Authentication Failed");
				loader.getException().printStackTrace();
			});
			loader.setOnSucceeded(evt -> {
				try {
					sheet = new GoogleSpreadsheet(spreadsheetId, loader.getValue());
					prefs.putInt(LAST_CONNECTION_KEY, 1);
					close();
				} catch (IOException e) {
					pushNotification("Connection Failed");
					Util.showErrorDialog(e);
					e.printStackTrace();
				}
			});
			loader.restart();
		}

	}

	private String extractId(String link) {
		if (link == null)
			return null;

		Pattern regex = Pattern.compile(".*\\/spreadsheets\\/d\\/([a-zA-Z0-9-_]+).*");
		Matcher match = regex.matcher(link);

		if (!match.matches())
			return null;

		return match.group(1);
	}

	private void setupLogoutLabel() {
		try {
			if (SheetsAPI.isLoggedIn()) {
				loggedIn.setVisible(true);
				logout.setVisible(true);
			} else {
				loggedIn.setVisible(false);
				logout.setVisible(false);
			}
		} catch (IOException e) {
			Util.showErrorDialog(e);
			e.printStackTrace();
		}
	}

	private void pushNotification(String str) {
		if (snackbar == null)
			System.out.println(str);
		else
			snackbar.enqueue(new SnackbarEvent(str));
	}
}
