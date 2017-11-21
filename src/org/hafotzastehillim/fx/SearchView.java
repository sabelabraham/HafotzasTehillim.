package org.hafotzastehillim.fx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.hafotzastehillim.fx.cell.EntryCell;
import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.FamilyGrouping;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.spreadsheet.Tab;
import org.hafotzastehillim.fx.util.Search;
import org.hafotzastehillim.fx.util.Util;
import org.reactfx.value.Val;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXRippler.RipplerMask;
import com.jfoenix.controls.JFXRippler.RipplerPos;
import com.jfoenix.controls.JFXTextField;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
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

public class SearchView extends VBox {

	private Model model;

	private JFXTextField query;
	private String queryString;
	private JFXButton searchButton;

	private ListView<Entry> resultListView;
	private ObservableList<Entry> resultList;

	private Label campaignLabel;
	private Spinner<Integer> currentCampaign;
	private IntegerProperty campaignIndex;

	private Text accountLabel;
	private Text account;
	private Text idLabel;
	private Text id;
	private Text name;
	private Text address;
	private Text phone;
	private Text total;

	private TextField points;
	private JFXButton saveButton;

	private Transition searchTransition;

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
			if (query.getText().equals(queryString) && model.getSpreadsheet().loadService().isRunning())
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

		resultListView = new ListView<>();
		resultListView.setItems(resultList);

		campaignLabel = new Label("Campaign:  ");
		currentCampaign = new Spinner<>(1, 100, 1);
		currentCampaign.setEditable(true);
		currentCampaign.setFocusTraversable(false);
		campaignIndex = IntegerProperty.integerProperty(currentCampaign.getValueFactory().valueProperty());
		campaignIndex.bindBidirectional(model.campaignIndexProperty());

		Util.commitOnFocusLose(currentCampaign);

		HBox campaignLabelAndSpinner = new HBox(campaignLabel, currentCampaign);
		campaignLabelAndSpinner.setPadding(new Insets(15));
		campaignLabelAndSpinner.setAlignment(Pos.BASELINE_CENTER);

		Val<Entry> selected = Val.wrap(resultListView.getSelectionModel().selectedItemProperty());

		model.currentEntryProperty().bind(selected);

		accountLabel = new Text("Account: ");
		account = new Text();
		idLabel = new Text("Memeber ID: ");
		id = new Text();
		name = new Text();
		address = new Text();
		phone = new Text();
		total = new Text();

		accountLabel.setFont(Font.font("Monospaced", 15));
		accountLabel.visibleProperty().bind(account.textProperty().isNotEmpty());
		
		account.setFont(Font.font("Monospaced", 15));
		account.setFill(Color.DODGERBLUE);

		idLabel.setFont(Font.font("Monospaced", 15));
		id.setFont(Font.font("Monospaced", 15));
		id.setFill(Color.DODGERBLUE);

		account.textProperty().bind(selected.flatMap(e -> e.accountProperty()));
		id.textProperty().bind(selected.flatMap(e -> e.idProperty()));
		name.textProperty().bind(Bindings.format("%s %s", selected.flatMap(e -> e.firstNameProperty()),
				selected.flatMap(e -> e.lastNameProperty())));
		address.textProperty().bind(Bindings.format("%s %s %s", selected.flatMap(e -> e.addressNumberProperty()),
				selected.flatMap(e -> e.addressNameProperty()), selected.flatMap(e -> e.aptProperty())));
		phone.textProperty().bind(selected.flatMap(e -> e.phoneProperty()));
		total.textProperty()
				.bind(Bindings.format("Total Points: %s", selected.flatMap(e -> e.totalProperty().asString())));

		Separator sep1 = new Separator();
		sep1.setMouseTransparent(true);
		sep1.setPadding(new Insets(10, 10, 5, 10));
		Separator sep2 = new Separator();
		sep2.setMouseTransparent(true);
		sep2.setPadding(new Insets(10));

		HBox accountBox = new HBox(accountLabel, account);
		accountBox.setAlignment(Pos.CENTER);
		HBox idBox = new HBox(idLabel, id);
		idBox.setAlignment(Pos.CENTER);

		VBox info = new VBox(accountBox, idBox, sep1, name, address, phone, sep2, total);
		info.visibleProperty().bind(selected.map(e -> true).orElseConst(false));
		info.setAlignment(Pos.CENTER);
		info.setId("info");
		info.setOnMouseClicked(evt -> {
			Entry se = selected.getValue();
			if (!se.getPhone().isEmpty() && se.getId().equals(query.getText())) {
				// Find family members

				ObservableList<Entry> list = FXCollections.observableArrayList();
				String phone = se.getPhone().replaceAll("[^\\d]", "");

				Util.onceOnSucceeded(model.getSpreadsheet().searchService().stateProperty(), () -> {
					list.add(0, se);
					DetailsPane.showDialog(list);
				});

				model.getSpreadsheet().searchEntries(list, data -> data.get(Column.PHONE.ordinal()).equals(phone)
						&& !data.get(Column.ID_NUMBER.ordinal()).equals(se.getId()));

			} else {
				List<Entry> family = new ArrayList<>();
				family.add(se);
				for (Entry e : resultList) {
					if (e == selected.getValue())
						continue;

					if (!e.getPhone().isEmpty() && e.getPhone().equals(selected.getValue().getPhone()))
						family.add(e);
				}

				DetailsPane.showDialog(family);
			}
		});

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
		points.setOnAction(evt -> {
			saveButton.fire();
			points.selectAll();
		});
		Util.selectOnFocus(points);

		selected.addListener((obs, ov, nv) -> {
			Entry value = selected.getValue();
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
			Entry value = selected.getValue();
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
		pointsPane.disableProperty().bind(selected.map(e -> false).orElseConst(true));

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
		bottom.disableProperty().bind(model.spreadsheetProperty().isNull());

		HBox.setHgrow(searchPane, Priority.ALWAYS);
		searchPane.disableProperty().bind(sheetNotSucceeded());

		Pane snackbarSpace = new Pane();
		snackbarSpace.setMinHeight(30);
		snackbarSpace.setId("snackbar");

		TopBar topBar = new TopBar();

		getChildren().addAll(topBar, searchPane, bottom, snackbarSpace);

	}

	private BooleanBinding sheetNotSucceeded() {
		return new BooleanBinding() {

			{
				bind(model.spreadsheetProperty());
				model.spreadsheetProperty().addListener((obs, ov, nv) -> {
					if (ov != null)
						unbind(ov.loadService().stateProperty());
					if (nv != null)
						bind(nv.loadService().stateProperty());
				});
			}

			@Override
			protected boolean computeValue() {
				return model.getSpreadsheet() == null
						|| model.getSpreadsheet().loadService().getState() != State.SUCCEEDED;
			}

		};
	}

	private void detachOldSpreadsheet(Spreadsheet s) {
		// NO-OP
	}

	private void setupSpreadsheet(Spreadsheet s) {
		if (s == null)
			return;

		resultListView.setCellFactory(lv -> new EntryCell(7, Bindings.size(resultList)));

		s.loadService().setOnScheduled(evt -> {
			resultList.clear();
			queryString = null;
		});

		s.searchService().stateProperty().addListener((obs, ov, nv) -> {
			if (nv == State.RUNNING) {
				searchTransition.play();
			}
			if (nv == State.SUCCEEDED) {
				resultListView.requestFocus();
				resultListView.getSelectionModel().selectFirst();
			}
		});
	}
}
