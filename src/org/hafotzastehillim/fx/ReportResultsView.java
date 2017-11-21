package org.hafotzastehillim.fx;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.FamilyGrouping;
import org.hafotzastehillim.fx.util.TableViewUtils;
import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReportResultsView extends VBox {

	public ReportResultsView(Map<String, ObservableList<Entry>> results, boolean familyGrouping) {

		getStylesheets().add(getClass().getResource("/resources/css/report-results-view.css").toExternalForm());
		getStyleClass().add("report-results-view");

		GridPane grid = new GridPane();
		grid.getColumnConstraints().addAll(Collections.nCopies(3, new ColumnConstraints(100)));
		grid.setAlignment(Pos.CENTER);
		grid.setPadding(new Insets(15));

		int i = 0;
		int total = 0;

		ObservableList<FamilyGrouping> allFamilies = null; // used only if grouping families
		if (familyGrouping) {
			allFamilies = FXCollections.observableArrayList();
		}

		for (Map.Entry<String, ObservableList<Entry>> mapEntry : results.entrySet()) {
			Label label;
			Label amount;
			JFXButton button = new JFXButton("View");

			if (familyGrouping) {
				ObservableList<FamilyGrouping> family = group(mapEntry.getValue());
				allFamilies.addAll(family);

				label = new Label("    " + mapEntry.getKey() + ":");
				amount = new Label("" + family.size());

				button.setOnAction(evt -> {
					TableView<FamilyGrouping> table = getFamilyTable(family);
					showStage(table, mapEntry.getKey());
				});

				total += family.size();
			} else {
				label = new Label("    " + mapEntry.getKey() + ":");
				amount = new Label("" + mapEntry.getValue().size());

				button.setOnAction(evt -> {
					TableView<Entry> table = getTable(mapEntry.getValue());
					showStage(table, mapEntry.getKey());
				});

				total += mapEntry.getValue().size();
			}

			button.setVisible(mapEntry.getValue().size() > 0);

			grid.add(label, 0, i);
			grid.add(amount, 1, i);
			grid.add(button, 2, i);

			i++;

		}

		if (results.size() > 1) {

			JFXButton viewAll = new JFXButton("View All");

			ObservableList<FamilyGrouping> allFamilies2 = allFamilies;
			viewAll.setOnAction(evt -> {
				if (familyGrouping) {
					TableView<FamilyGrouping> table = getFamilyTable(allFamilies2);
					showStage(table, "All");
				} else {
					ObservableList<Entry> all = FXCollections.observableArrayList();

					for (Map.Entry<String, ObservableList<Entry>> mapEntry : results.entrySet()) {
						all.addAll(mapEntry.getValue());
					}

					TableView<Entry> table = getTable(all);
					showStage(table, "All");
				}

			});

			grid.add(new Separator(), 0, i, 3, 1);
			i++;

			grid.add(new Label("    Total:"), 0, i);
			grid.add(new Label(total + ""), 1, i);
			grid.add(viewAll, 2, i);
			i++;
		}

		getChildren().addAll(grid);
	}

	private TableView<FamilyGrouping> getFamilyTable(ObservableList<FamilyGrouping> items) {
		TableView<FamilyGrouping> table = TableViewUtils.getFamilyTable(items);

		ContextMenu menu = new ContextMenu();
		MenuItem export = new MenuItem("Export Selected");
		export.setOnAction(evt -> TableViewUtils.exportSelectableToExcel(table));
		export.disableProperty().bind(new BooleanBinding() {

			{
				items.forEach(i -> bind(i.selectedProperty()));
			}

			@Override
			protected boolean computeValue() {
				return items.stream().noneMatch(item -> item.isSelected());
			}

		});

		menu.getItems().add(export);
		table.setContextMenu(menu);

		return table;
	}

	private ObservableList<FamilyGrouping> group(List<Entry> entries) {
		entries.sort((e1, e2) -> e1.getPhone().compareTo(e2.getPhone()));

		ObservableList<FamilyGrouping> family = FXCollections.observableArrayList();

		Entry prev = null;
		List<Entry> singleFamily = new ArrayList<>();

		for (Entry e : entries) {
			if (prev == null) {
				singleFamily.add(e);
				prev = e;
				continue;
			}

			if (!prev.getPhone().isEmpty() && prev.getPhone().equals(e.getPhone())) {
				singleFamily.add(e);
			} else {
				try {
					family.add(new FamilyGrouping(singleFamily));
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
				singleFamily = new ArrayList<>();
				singleFamily.add(e);
			}

			prev = e;
		}

		return family;
	}

	void showStage(Node n, String title) {
		Stage stage = new Stage();
		Model.getInstance().registerStage(stage);
		stage.setTitle(title);
		stage.setMaximized(true);
		stage.getIcons().add(Main.ICON);

		Scene scene = new Scene(new StackPane(n));
		stage.setScene(scene);

		scene.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
			if (evt.getCode() == KeyCode.ESCAPE)
				stage.close();
		});

		stage.showAndWait();
	}

	private TableView<Entry> getTable(ObservableList<Entry> items) {

		TableView<Entry> table = TableViewUtils.getTable(items);

		ContextMenu menu = new ContextMenu();
		MenuItem export = new MenuItem("Export Selected");
		export.setOnAction(evt -> TableViewUtils.exportToExcel(table));
		export.disableProperty().bind(new BooleanBinding() {

			{
				items.forEach(i -> bind(i.selectedProperty()));
			}

			@Override
			protected boolean computeValue() {
				return items.stream().noneMatch(item -> item.isSelected());
			}

		});

		menu.getItems().add(export);
		table.setContextMenu(menu);

		return table;
	}

}
