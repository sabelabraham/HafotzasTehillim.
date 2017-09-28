package org.hafotzastehillim.fx;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.hafotzastehillim.fx.spreadsheet.Entry;
import com.jfoenix.controls.JFXButton;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ReportResultsView extends VBox {

	public ReportResultsView(Map<String, ObservableList<Entry>> results) {
		StackPane tableContainer = new StackPane();
		tableContainer.setPadding(new Insets(10));

		GridPane listContainer = new GridPane();
		listContainer.getColumnConstraints().addAll(Collections.nCopies(3, new ColumnConstraints(100)));
		listContainer.setAlignment(Pos.CENTER);
		listContainer.setPadding(new Insets(15));

		JFXButton viewAll = new JFXButton("View All");
		viewAll.setTextFill(Color.DODGERBLUE);
		viewAll.setOnAction(evt -> {
			TabPane tabs = new TabPane();
			tabs.setTabMinWidth(75);
			tabs.setStyle("-fx-border-color: #bababa");

			for (Map.Entry<String, ObservableList<Entry>> mapEntry : results.entrySet()) {
				Tab tab = new Tab(mapEntry.getKey());
				tab.setContent(getTable(mapEntry.getValue()));

				tabs.getTabs().add(tab);
			}

			tableContainer.getChildren().setAll(tabs);
		});

		int i = 0;
		for (Map.Entry<String, ObservableList<Entry>> mapEntry : results.entrySet()) {
			Label label = new Label(mapEntry.getKey() + ": ");
			Label amount = new Label("" + mapEntry.getValue().size());

			JFXButton button = new JFXButton("View");
			button.setTextFill(Color.DODGERBLUE);
			button.setOnAction(evt -> tableContainer.getChildren().setAll(getTable(mapEntry.getValue())));

			listContainer.add(label, 0, i);
			listContainer.add(amount, 1, i);
			listContainer.add(button, 2, i);

			i++;
		}

		if (results.size() > 1)
			listContainer.add(viewAll, 1, i);

		getChildren().addAll(listContainer, tableContainer);
		setPrefSize(700, 600);
	}

	private TableView<Entry> getTable(ObservableList<Entry> items) {

		TableView<Entry> table = new TableView<>();
		table.setOnKeyPressed(evt -> {
			if (evt.getCode() == KeyCode.TAB)
				evt.consume(); // don't move focus off checkbox
		});

		table.setEditable(true);
		table.setTableMenuButtonVisible(true);

		TableColumn<Entry, Boolean> select = new TableColumn<>("Select");
		select.setEditable(true);

		CheckBox columnBox = new CheckBox();
		columnBox.setTranslateX(2);
		columnBox.setFocusTraversable(false);
		columnBox.setOnAction(evt -> items.forEach(entry -> entry.setSelected(columnBox.isSelected())));

		select.setGraphic(columnBox);
		select.setCellFactory(col -> {
			CheckBoxTableCell<Entry, Boolean> cell = new CheckBoxTableCell<>();

			cell.itemProperty().addListener((obs, ov, nv) -> {
				long selectCount = items.stream().filter(entry -> entry.isSelected()).count();
				if (selectCount == 0) {
					columnBox.setSelected(false);
					columnBox.setIndeterminate(false);
				} else if (selectCount == items.size()) {
					columnBox.setSelected(true);
					columnBox.setIndeterminate(false);
				} else {
					columnBox.setSelected(false);
					columnBox.setIndeterminate(true);
				}

			});
			return cell;
		});
		select.setCellValueFactory(new PropertyValueFactory<>("selected"));
		select.setMaxWidth(30);
		select.setMinWidth(30);

		TableColumn<Entry, String> id = new TableColumn<>("ID");
		id.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<Entry, String> gender = new TableColumn<>("Gender");
		gender.setCellValueFactory(new PropertyValueFactory<>("gender"));

		TableColumn<Entry, String> firstName = new TableColumn<>("First");
		firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));

		TableColumn<Entry, String> lastName = new TableColumn<>("Last");
		lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

		table.getColumns().addAll(Arrays.asList(select, id, gender, firstName, lastName));
		table.setItems(items);

		return table;
	}

}
