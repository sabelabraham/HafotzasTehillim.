package org.hafotzastehillim.fx;

import java.io.IOException;
import java.util.List;

import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.util.NoSelectionModel;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.MultipleSelectionModelBuilder;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DetailsPane extends VBox {

	private Entry entry;
	private FormController controller;
	private ObservableList<PointEditorData> pointData;

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
				pointData.add(new PointEditorData(point));
		}

		ListView<PointEditorData> pointsList = new ListView<>();
		pointsList.setCellFactory(view -> new PointEditor());
		pointsList.setItems(pointData);
		pointsList.setSelectionModel(new NoSelectionModel<PointEditorData>());
		pointsList.prefWidthProperty().bind(widthProperty().divide(2.5));
		pointsList.setPrefHeight(200);
		pointsList.setId("points-list");

		Label label = new Label("Weekly Program");
		JFXButton add = new JFXButton("Add");
		add.setId("add");
		add.setOnAction(evt -> pointData.add(new PointEditorData(0)));

		VBox pointPane = new VBox(10);
		HBox header = new HBox(15);
		header.getChildren().addAll(label, add);
		header.setPadding(new Insets(10, 10, 0, 10));
		header.setAlignment(Pos.BASELINE_CENTER);

		Label total = new Label();
		total.setPadding(new Insets(0, 0, 10, 0));
		if (entry != null)
			total.setText("Total Points: " + entry.getTotal());

		pointPane.getChildren().addAll(header, pointsList, new StackPane(total));
		pointPane.setId("point-pane");

		HBox allLists = new HBox(pointPane); // FIXME add shavuos program list
		allLists.setPadding(new Insets(0, 40, 20, 40));

		getChildren().addAll(allLists);
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
		} else if (e.isDetailsChanged()) {
			e.reload();
		}
	}
}
