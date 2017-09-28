package org.hafotzastehillim.fx;

import java.io.IOException;

import org.hafotzastehillim.fx.cell.GiftEditorCell;
import org.hafotzastehillim.fx.cell.PointEditorCell;
import org.hafotzastehillim.fx.cell.ShavuosEditorCell;
import org.hafotzastehillim.fx.cell.EditorData;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.util.NoSelectionModel;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DetailsPane extends VBox {

	private Entry entry;
	private FormController controller;
	private ObservableList<EditorData<Integer>> pointData;
	private ObservableList<EditorData<Integer>> shavuosData;
	private ObservableList<EditorData<Boolean>> giftsData;

	public DetailsPane(Entry entry) {
		getStyleClass().add("details-pane");

		this.entry = entry;

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Form.fxml"));
		GridPane form = null;
		try {
			form = loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			e.printStackTrace();
			return;
		}

		form.getStyleClass().add("form");
		form.getStylesheets().add(getClass().getResource("/resources/css/form.css").toExternalForm());

		getStyleClass().add("details");
		getStylesheets().add(getClass().getResource("/resources/css/details.css").toExternalForm());

		controller = loader.getController();
		controller.setEntry(entry);

		getChildren().add(form);

		pointData = FXCollections.observableArrayList();
		if (entry != null) {
			for (int point : entry.getPoints())
				pointData.add(new EditorData<>(point));
		}

		ListView<EditorData<Integer>> pointsList = new ListView<>();
		pointsList.setCellFactory(view -> new PointEditorCell(Bindings.size(pointData)));
		pointsList.setItems(pointData);
		pointsList.setSelectionModel(new NoSelectionModel<EditorData<Integer>>());
		pointsList.setPrefHeight(200);
		pointsList.setId("points-list");

		Label pointsLabel = new Label("Weekly Program");
		JFXButton pointsAdd = new JFXButton("Add");
		pointsAdd.setId("points-add");
		pointsAdd.setOnAction(evt -> pointData.add(new EditorData<>(0)));

		VBox pointsPane = new VBox();
		HBox pointsHeader = new HBox(15);
		pointsHeader.getChildren().addAll(pointsLabel, pointsAdd);
		pointsHeader.setPadding(new Insets(10, 10, 0, 10));
		pointsHeader.setAlignment(Pos.BASELINE_CENTER);

		Label total = new Label();
		total.setPadding(new Insets(5));
		if (entry != null)
			total.setText("Total Points: " + entry.getTotal());

		StackPane totalContainer = new StackPane(total);
		totalContainer.setId("total-container");
		totalContainer.visibleProperty().bind(total.textProperty().isEmpty().not());

		pointsPane.getChildren().addAll(pointsHeader, pointsList, totalContainer);
		pointsPane.setId("points-pane");

		// -----------------

		shavuosData = FXCollections.observableArrayList();
		if (entry != null) {
			for (int data : entry.getShavuosData())
				shavuosData.add(new EditorData<>(data));
		}

		ListView<EditorData<Integer>> shavuosList = new ListView<>();
		shavuosList.setCellFactory(view -> new ShavuosEditorCell(Bindings.size(shavuosData)));
		shavuosList.setItems(shavuosData);
		shavuosList.setSelectionModel(new NoSelectionModel<EditorData<Integer>>());
		shavuosList.setPrefHeight(200);
		shavuosList.setId("shavuos-list");

		Label shavuosLabel = new Label("Shavuos Program");
		JFXButton shavuosAdd = new JFXButton("Add");
		shavuosAdd.setId("shavuos-add");
		shavuosAdd.setOnAction(evt -> shavuosData.add(new EditorData<>(0)));

		VBox shavuosPane = new VBox();
		HBox shavuosHeader = new HBox(15);
		shavuosHeader.getChildren().addAll(shavuosLabel, shavuosAdd);
		shavuosHeader.setPadding(new Insets(10, 10, 0, 10));
		shavuosHeader.setAlignment(Pos.BASELINE_CENTER);

		shavuosPane.getChildren().addAll(shavuosHeader, shavuosList);
		shavuosPane.setId("shavuos-pane");

		// -----------------

		giftsData = FXCollections.observableArrayList();
		if (entry != null) {
			for (boolean data : entry.getGiftsReceived())
				giftsData.add(new EditorData<>(data));

			int eligable = entry.getTotal() / 100;
			while (giftsData.size() < eligable) {
				giftsData.add(new EditorData<>(false));
			}
		}

		ListView<EditorData<Boolean>> giftsList = new ListView<>();
		giftsList.setCellFactory(view -> new GiftEditorCell(Bindings.size(giftsData)));
		giftsList.setItems(giftsData);
		giftsList.setSelectionModel(new NoSelectionModel<EditorData<Boolean>>());
		giftsList.setPrefHeight(200);
		giftsList.setId("gifts-list");

		Label giftsLabel = new Label("Gifts");
		giftsLabel.setPadding(new Insets(4));

		VBox giftsPane = new VBox();
		HBox giftsHeader = new HBox(15);
		giftsHeader.getChildren().addAll(giftsLabel);
		giftsHeader.setPadding(new Insets(10, 10, 0, 10));
		giftsHeader.setAlignment(Pos.BASELINE_CENTER);

		giftsPane.getChildren().addAll(giftsHeader, giftsList);
		giftsPane.setId("gifts-pane");

		// -----------------

		HBox allLists = new HBox(pointsPane, shavuosPane);
		if (entry != null) {
			allLists.getChildren().add(giftsPane);
		}
		allLists.setSpacing(10);
		allLists.setPadding(new Insets(0, 40, 20, 40));
		allLists.setAlignment(Pos.CENTER);

		getChildren().addAll(allLists);
		setAlignment(Pos.CENTER);
	}

	public FormController getController() {
		return controller;
	}

	public static void showNewDialog() {
		DetailsPane pane = new DetailsPane(null);
		Util.createDialog(pane, "New Member",
				pane.getController().cityYiddish.getSelectionModel().selectedItemProperty().isNull(), ButtonType.CANCEL,
				ButtonType.OK).filter(b -> b == ButtonType.OK).ifPresent(b -> {
					Entry e = pane.getController().getEntry();
					e.saveDetails();
					for (int i = 0; i < pane.pointData.size(); i++) {
						if (pane.pointData.get(i).changed()) {
							e.putPoint(i, pane.pointData.get(i).getValue());
						}
					}
					for (int i = 0; i < pane.shavuosData.size(); i++) {
						if (pane.shavuosData.get(i).changed()) {
							e.putShavuosData(i, pane.shavuosData.get(i).getValue());
						}
					}
					for (int i = 0; i < pane.giftsData.size(); i++) {
						if (pane.giftsData.get(i).changed()) {
							e.putGiftReceived(i, pane.giftsData.get(i).getValue());
						}
					}
				});
	}

	public static void showDialog(Entry entry) {
		DetailsPane pane = new DetailsPane(entry);
		ButtonType type = Util.createDialog(pane, "Details", ButtonType.CANCEL, ButtonType.APPLY)
				.filter(b -> b == ButtonType.APPLY).orElse(null);
		Entry e = pane.getController().getEntry();
		if (type == ButtonType.APPLY) {
			e.saveDetails();
			for (int i = 0; i < pane.pointData.size(); i++) {
				if (pane.pointData.get(i).changed()) {
					e.putPoint(i, pane.pointData.get(i).getValue());
				}
			}
			for (int i = 0; i < pane.shavuosData.size(); i++) {
				if (pane.shavuosData.get(i).changed()) {
					e.putShavuosData(i, pane.shavuosData.get(i).getValue());
				}
			}
			for (int i = 0; i < pane.giftsData.size(); i++) {
				if (pane.giftsData.get(i).changed()) {
					e.putGiftReceived(i, pane.giftsData.get(i).getValue());
				}
			}

		} else if (e.isDetailsChanged()) {
			e.reload();
		}
	}
}
