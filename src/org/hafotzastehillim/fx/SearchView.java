package org.hafotzastehillim.fx;

import java.util.prefs.Preferences;

import org.hafotzastehillim.fx.cell.EntryCell;
import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.spreadsheet.Tab;
import org.hafotzastehillim.fx.util.Search;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;
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
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
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

/*
 * All SVG paths are from Google's Material Design https://material.io/icons
 */
public class SearchView extends VBox {

	private Model model;

	private JFXTextField query;
	private String queryString;
	private JFXButton searchButton;

	private JFXButton refresh;
	private Refresh graphics;
	private JFXButton connect;
	private JFXButton newMember;
	private JFXButton report;
	private JFXButton gift;
	private HBox topBar;

	private ListView<Entry> resultListView;
	private ObservableList<Entry> resultList;

	private Label campaignLabel;
	private Spinner<Integer> currentCampaign;

	private Text id;
	private Text name;
	private Text address;
	private Text phone;
	private JFXButton details;

	private TextField points;
	private JFXButton saveButton;

	private Transition searchTransition;

	private static final Preferences prefs = Preferences.userNodeForPackage(SearchView.class);
	private static final String CAMPAIGN_INDEX_KEY = "CampaignIndex";

	public SearchView() {
		resultList = FXCollections.observableArrayList();

		model = Model.getInstance();
		model.spreadsheetProperty().addListener((obs, ov, nv) -> {
			detachOldSpreadsheet(ov);
			setupSpreadsheet(nv);
		});

		getStyleClass().add("search-view");
		getStylesheets().add(getClass().getResource("/resources/css/search-view.css").toExternalForm());

		AnchorPane searchPane = new AnchorPane();

		query = new JFXTextField();
		query.setFont(Font.font(20));
		query.setId("search-field");
		query.setPromptText("Search");
		Util.selectOnFocus(query);

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

		query.setOnAction(evt -> {
			if (query.getText().equals(queryString))
				return;

			queryString = query.getText();
			model.getSpreadsheet().searchEntries(query.getText().toLowerCase().replace(" ", ""), resultList,
					Search.getMatcher(), Search.getColumns());
		});
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
		SVGPath connectPath = new SVGPath();
		connectPath.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-"
				+ "7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z");
		connect.setGraphic(connectPath);
		connect.setOnAction(evt -> showSpreadsheetDialog());
		connectPath.setFill(Color.GRAY);

		newMember = new JFXButton();
		SVGPath newMemberPath = new SVGPath();
		newMemberPath.setContent("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
		newMemberPath.setFill(Color.GRAY);
		newMember.setGraphic(newMemberPath);
		newMember.setOnAction(evt -> DetailsPane.showNewDialog());

		report = new JFXButton();
		SVGPath reportPath = new SVGPath();
		reportPath.setContent("M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z");
		reportPath.setFill(Color.GRAY);
		reportPath.setTranslateY(3);
		report.setGraphic(reportPath);
		report.disableProperty().bind(newMember.disabledProperty());

		gift = new JFXButton();
		SVGPath giftPath = new SVGPath();
		giftPath.setContent("M20 6h-2.18c.11-.31.18-.65.18-1 0-1.66-1.34-3" + "-3-3-1.05 0-1.96.54-2.5 1.35l"
				+ "-.5.67-.5-.68C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99"
				+ " 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 "
				+ "1-1-.45-1-1 .45-1 1-1zM9 4c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm11 15H4v-2h16v2zm0-5H4"
				+ "V8h5.08L7 10.83 8.62 12 11 8.76l1-1.36 1 1.36L15.38 12 17 10.83 14.92 8H20v6z");
		gift.setGraphic(giftPath);
		giftPath.setFill(Color.GRAY);
		gift.disableProperty().bind(newMember.disabledProperty());

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

		Util.commitOnFocusLose(currentCampaign);

		HBox campaignLabelAndSpinner = new HBox(campaignLabel, currentCampaign);
		campaignLabelAndSpinner.setPadding(new Insets(15));
		campaignLabelAndSpinner.setAlignment(Pos.BASELINE_CENTER);

		ReadOnlyObjectProperty<Entry> selected = resultListView.getSelectionModel().selectedItemProperty();
		model.currentEntryProperty().bind(selected);

		id = new Text();
		name = new Text();
		address = new Text();
		phone = new Text();

		id.setFill(Color.DODGERBLUE);
		id.setFont(Font.font("Monospaced", 15));

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

		id.visibleProperty().bind(selected.isNotNull());
		name.visibleProperty().bind(selected.isNotNull());
		address.visibleProperty().bind(selected.isNotNull());
		phone.visibleProperty().bind(selected.isNotNull());

		id.textProperty().bind(column(selected, Column.ID_NUMBER));
		name.textProperty()
				.bind(column(selected, Column.FIRST_NAME).concat(" ").concat(column(selected, Column.LAST_NAME)));
		address.textProperty().bind(
				column(selected, Column.ADDRESS_NUMBER).concat(" ").concat(column(selected, Column.ADDRESS_NAME)));
		phone.textProperty().bind(column(selected, Column.PHONE));

		Separator sep1 = new Separator();
		sep1.setPadding(new Insets(10, 10, 5, 10));
		Separator sep2 = new Separator();
		sep2.setPadding(new Insets(10, 10, 5, 10));

		VBox info = new VBox(id, sep1, name, address, phone, sep2, new StackPane(details, notSupported));
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
		Util.selectOnFocus(points);

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
		newMember.disableProperty().bind(searchPane.disabledProperty());

		Pane snackbarSpace = new Pane();
		snackbarSpace.setMinHeight(30);
		snackbarSpace.setId("snackbar");

		Pane space = new Pane();
		HBox.setHgrow(space, Priority.ALWAYS);
		topBar = new HBox(report, gift, connect, refresh, space, newMember);
		topBar.setAlignment(Pos.CENTER_RIGHT);
		topBar.setId("top-bar");
		topBar.setMinHeight(30);
		topBar.setPadding(new Insets(0, 10, 10, 10));
		topBar.getChildren().forEach(n -> n.setFocusTraversable(false));

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

		s.loadService().setOnScheduled(null);
		s.loadService().setOnSucceeded(null);
	}

	private void setupSpreadsheet(Spreadsheet s) {
		if (s == null)
			return;

		resultListView.setCellFactory(lv -> new EntryCell(7, Bindings.size(resultList)));

		refresh.mouseTransparentProperty().bind(s.loadService().stateProperty().isNotEqualTo(State.SUCCEEDED));
		s.loadService().setOnScheduled(evt -> resultList.clear());

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
