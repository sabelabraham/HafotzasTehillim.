package org.hafotzastehillim.fx;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import org.hafotzastehillim.spreadsheet.Column;
import org.hafotzastehillim.spreadsheet.Entry;
import org.hafotzastehillim.spreadsheet.Search;
import org.hafotzastehillim.spreadsheet.Spreadsheet;
import org.hafotzastehillim.spreadsheet.Tab;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXTextField;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class View extends VBox {

	private Model model;

	private JFXTextField query;
	private JFXButton searchButton;

	private JFXButton refresh;
	private Refresh graphics;
	private JFXButton connect;
	private JFXButton newMember;
	private HBox topBar;

	private ListView<Entry> resultListView;
	private ObservableList<Entry> resultList;

	private Label campaignLabel;
	private Spinner<Integer> currentCampaign;

	private Text name;
	private Text address;
	private Text phone;
	private JFXButton details;

	private TextField points;
	private JFXButton saveButton;

	private Transition searchTransition;

	private static final Preferences prefs = Preferences.userNodeForPackage(View.class);
	private static final String CAMPAIGN_INDEX_KEY = "CampaignIndex";

	public View() {
		resultList = FXCollections.observableArrayList();

		model = Model.getInstance();
		model.spreadsheetProperty().addListener((obs, ov, nv) -> {
			detachOldSpreadsheet(ov);
			setupSpreadsheet(nv);
		});

		getStyleClass().add("view");
		getStylesheets().add(getClass().getResource("/resources/css/view.css").toExternalForm());

		AnchorPane searchPane = new AnchorPane();

		query = new JFXTextField();
		query.setFont(Font.font(20));
		query.setId("search-field");
		query.setPromptText("Search");
		query.focusedProperty().addListener((obs, ov, nv) -> {
			if (nv)
				Platform.runLater(() -> query.selectAll());
		});

		searchPane.disabledProperty().addListener((obs, ov, nv) -> {
			if (!nv) {
				query.requestFocus();
			}
		});

		searchButton = new JFXButton();
		searchButton.setId("search-button");

		SVGPath searchPath = new SVGPath();
		searchPath.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3"
				+ " 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49"
				+ " 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");

		searchPath.setId("search-icon");
		searchButton.setGraphic(searchPath);

		query.setOnAction(evt -> model.getSpreadsheet().search(query.getText(), resultList, Search.getMatcher(),
				Search.getColumns()));
		searchButton.setOnAction(query.getOnAction());

		Util.circleClip(searchButton);

		Pane fieldHolder = new StackPane(query);
		fieldHolder.setId("search-field-pane");

		AnchorPane.setTopAnchor(fieldHolder, 0.0);
		AnchorPane.setLeftAnchor(fieldHolder, 0.0);
		AnchorPane.setBottomAnchor(fieldHolder, 0.0);
		AnchorPane.setRightAnchor(fieldHolder, 0.0);

		AnchorPane.setTopAnchor(searchButton, 0.0);
		AnchorPane.setRightAnchor(searchButton, 0.0);
		AnchorPane.setBottomAnchor(searchButton, 0.0);

		searchPane.getChildren().addAll(fieldHolder, searchButton);

		RotateTransition flip = new RotateTransition(Duration.millis(300), searchPath);
		flip.setCycleCount(2);
		flip.setAutoReverse(true);
		flip.setAxis(new Point3D(1, 1, 0));
		flip.setByAngle(90);

		searchTransition = new SequentialTransition(flip, new PauseTransition(Duration.millis(500)));
		searchTransition.setOnFinished((evt) -> {
			if (model.getSpreadsheet().searchService().getState() != State.RUNNING) {
				searchTransition.stop();
			} else {
				searchTransition.play();
			}
		});

		refresh = new JFXButton();

		graphics = new Refresh();
		graphics.setAlwaysVisible(true);
		refresh.setGraphic(graphics);
		refresh.setOnAction(evt -> model.getSpreadsheet().reload());

		connect = new JFXButton();
		SVGPath g = new SVGPath();
		g.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-"
				+ "7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z");
		connect.setGraphic(g);
		connect.setOnAction(evt -> showSpreadsheetDialog());
		g.setFill(Color.GRAY);

		newMember = new JFXButton("\u2795");
		newMember.setId("new-member-button");
		newMember.setOnAction(evt -> DetailsPane.showNewDialog());

		resultListView = new ListView<>();
		resultListView.setItems(resultList);

		int campaignIndex = prefs.getInt(CAMPAIGN_INDEX_KEY, 1);

		campaignLabel = new Label("Campaign:  ");
		currentCampaign = new Spinner<>(1, 100, campaignIndex);
		currentCampaign.setEditable(true);
		currentCampaign.setFocusTraversable(false);
		currentCampaign.valueProperty().addListener((obs, ov, nv) -> {
			prefs.putInt(CAMPAIGN_INDEX_KEY, nv);
		});

		// hack for committing on focus lose
		TextFormatter<Integer> currentCampaignFmt = new TextFormatter<>(
				currentCampaign.getValueFactory().getConverter(), currentCampaign.getValue());
		currentCampaign.getEditor().setTextFormatter(currentCampaignFmt);
		currentCampaign.getValueFactory().valueProperty().bindBidirectional(currentCampaignFmt.valueProperty());

		HBox campaignLabelAndSpinner = new HBox(campaignLabel, currentCampaign);
		campaignLabelAndSpinner.setPadding(new Insets(15));
		campaignLabelAndSpinner.setAlignment(Pos.BASELINE_CENTER);

		ReadOnlyObjectProperty<Entry> selected = resultListView.getSelectionModel().selectedItemProperty();
		model.currentEntryProperty().bind(selected);

		name = new Text();
		address = new Text();
		phone = new Text();

		details = new JFXButton("Details");
		details.visibleProperty().bind(Bindings.createBooleanBinding( // FIXME so far we don't support other cities.
				() -> selected.get() != null && Tab.isCity(selected.get().getCityYiddish()), selected));

		details.setId("details-button");
		details.setOnAction(evt -> {
			DetailsPane.showDialog(selected.get());
		});
		Label notSupported = new Label("Details currently unsupported\nfor \"others\" tab");
		notSupported.visibleProperty().bind(details.visibleProperty().not());
		notSupported.setTextAlignment(TextAlignment.CENTER);

		name.visibleProperty().bind(selected.isNotNull());
		address.visibleProperty().bind(selected.isNotNull());
		phone.visibleProperty().bind(selected.isNotNull());

		name.textProperty()
				.bind(column(selected, Column.FIRST_NAME).concat(" ").concat(column(selected, Column.LAST_NAME)));
		address.textProperty().bind(
				column(selected, Column.ADDRESS_NUMBER).concat(" ").concat(column(selected, Column.ADDRESS_NAME)));
		phone.textProperty().bind(column(selected, Column.PHONE));

		Separator sep = new Separator();
		sep.setPadding(new Insets(10, 10, 5, 10));
		VBox info = new VBox(name, address, phone, sep, new StackPane(details, notSupported));
		info.visibleProperty().bind(selected.isNotNull());
		info.setAlignment(Pos.CENTER);
		info.setId("info");

		saveButton = new JFXButton();
		saveButton.setId("save-button");

		SVGPath savePath = new SVGPath();
		savePath.setContent("M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z");
		savePath.setId("save-icon");

		saveButton.setGraphic(savePath);
		saveButton.setOnAction(evt -> {
			Entry value = selected.getValue();
			if (points.getText().isEmpty()) {
				value.putPoint(currentCampaign.getValue() - 1, 0);
			} else {
				value.putPoint(currentCampaign.getValue() - 1, Integer.parseInt(points.getText()));
			}
		});

		points = new TextField();
		points.promptTextProperty().bind(
				new SimpleStringProperty("Points for campaign ").concat(currentCampaign.valueProperty().asString()));
		points.setTextFormatter(new TextFormatter<Integer>(change -> {
			change.setText(change.getText().replaceAll("[^\\d]", ""));
			return change;
		}));
		points.setOnAction(saveButton.getOnAction());
		points.focusedProperty().addListener((obs, ov, nv) -> {
			if (nv)
				Platform.runLater(() -> points.selectAll());
		});
		selected.addListener((obs, ov, nv) -> {
			Entry value = selected.get();
			if (value == null) {
				points.setText("");
				return;
			}

			int p = value.getPoint(currentCampaign.getValue() - 1);
			if (p == 0) {
				points.setText("");
			} else {
				points.setText("" + p);
			}
		});
		currentCampaign.valueProperty().addListener((obs, ov, nv) -> {
			Entry value = selected.get();
			if (value == null) {
				points.setText("");
				return;
			}

			int p = value.getPoint(currentCampaign.getValue() - 1);
			if (p == 0) {
				points.setText("");
			} else {
				points.setText("" + p);
			}
		});

		AnchorPane pointsPane = new AnchorPane();
		pointsPane.disableProperty().bind(selected.isNull());

		AnchorPane.setTopAnchor(points, 0.0);
		AnchorPane.setLeftAnchor(points, 0.0);
		AnchorPane.setBottomAnchor(points, 0.0);
		AnchorPane.setRightAnchor(points, 0.0);

		AnchorPane.setTopAnchor(saveButton, 0.0);
		AnchorPane.setRightAnchor(saveButton, 0.0);
		AnchorPane.setBottomAnchor(saveButton, 0.0);

		pointsPane.getChildren().addAll(points, saveButton);

		Pane spacer = new Pane();
		VBox.setVgrow(spacer, Priority.ALWAYS);
		VBox bottomRight = new VBox(campaignLabelAndSpinner, info, spacer, pointsPane);
		HBox bottom = new HBox(resultListView, bottomRight);
		bottom.setSpacing(10);
		bottom.setAlignment(Pos.CENTER);

		HBox.setHgrow(searchPane, Priority.ALWAYS);
		searchPane.disableProperty().bind(model.spreadsheetProperty().isNull().or(refresh.mouseTransparentProperty()));

		Pane snackbarSpace = new Pane();
		snackbarSpace.setMinHeight(30);
		snackbarSpace.setId("snackbar");

		topBar = new HBox(newMember, connect, refresh);
		topBar.setAlignment(Pos.CENTER_RIGHT);
		topBar.setId("top-bar");
		topBar.setMinHeight(30);

		getChildren().addAll(topBar, searchPane, bottom, snackbarSpace);

		dialog = new SpreadsheetSelectionDialog();
		model.setSpreadsheet(dialog.getSpreadsheet());
	}

	private StringBinding column(ObservableValue<Entry> entry, Column column) {
		return Bindings.createStringBinding(() -> {
			if (entry.getValue() == null)
				return "";

			Entry value = entry.getValue();
			return value.get(column);
		}, entry);
	}

	private void detachOldSpreadsheet(Spreadsheet s) {
		if (s == null)
			return;

		refresh.mouseTransparentProperty().unbind();
		graphics.refreshingProperty().unbind();
	}

	private void setupSpreadsheet(Spreadsheet s) {
		if (s == null)
			return;

		resultListView.setCellFactory(lv -> new EntryCell(7, Bindings.size(resultList)));

		refresh.mouseTransparentProperty().bind(s.loadService().runningProperty());
		s.loadService().setOnScheduled(evt -> resultList.clear());
		s.searchService().setOnScheduled(evt -> resultList.clear());

		s.searchService().setOnSucceeded(evt -> {
			resultListView.requestFocus();
			resultListView.getSelectionModel().selectFirst();
		});

		s.searchService().stateProperty().addListener((obs, ov, nv) -> {
			if (nv == State.RUNNING) {
				searchTransition.play();
			}
		});

		graphics.refreshingProperty().bind(s.loadService().runningProperty());
	}

	private SpreadsheetSelectionDialog dialog;

	public void showSpreadsheetDialog() {
		dialog.showAndWait();
		Spreadsheet sheet = dialog.getSpreadsheet();

		model.setSpreadsheet(sheet);
	}
}
