package org.hafotzastehillim.fx;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hafotzastehillim.fx.cell.FamilyTableRow;
import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Tab;
import org.hafotzastehillim.fx.util.TableViewUtils;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import static org.hafotzastehillim.fx.spreadsheet.Column.*;

public class GiftResultsView extends VBox {

	public GiftResultsView(Map<Integer, ObservableList<Entry>> results) {

		getStylesheets().add(getClass().getResource("/resources/css/report-results-view.css").toExternalForm());
		getStyleClass().add("report-results-view");

		GridPane grid = new GridPane();
		grid.getColumnConstraints().addAll(Collections.nCopies(3, new ColumnConstraints(100)));
		grid.setAlignment(Pos.CENTER);
		grid.setPadding(new Insets(15));

		int i = 0;
		for (Map.Entry<Integer, ObservableList<Entry>> mapEntry : results.entrySet()) {
			if (mapEntry.getValue().size() == 0)
				continue;

			Label label = new Label("    " + ((mapEntry.getKey() + 1) * 100) + " points:");
			Label amount = new Label("" + mapEntry.getValue().size());

			JFXButton button = new JFXButton("View");
			button.setOnAction(evt -> {
				TableView<Entry> table = getTable(mapEntry.getKey(), mapEntry.getValue());

				showStage(table, ((mapEntry.getKey() + 1) * 100) + " points");
			});
			button.setVisible(mapEntry.getValue().size() > 0);

			grid.add(label, 0, i);
			grid.add(amount, 1, i);
			grid.add(button, 2, i);

			i++;
		}

		getChildren().addAll(grid);
	}

	void showStage(Node n, String title) {
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setMaximized(true);
		stage.getIcons().add(Main.ICON);

		stage.setScene(new Scene(new StackPane(n)));
		stage.showAndWait();
	}
	
	TableView<Entry> getTable(int gift, ObservableList<Entry> items) {
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

		MenuItem received = new MenuItem("Mark as Received");
		received.setOnAction(evt -> {
			items.stream().filter(e -> e.isSelected()).forEach(e -> e.putGiftReceived(gift, true));
		});
		received.disableProperty().bind(export.disableProperty());

		MenuItem notReceived = new MenuItem("Mark as Not Received");
		notReceived.setOnAction(evt -> {
			items.stream().filter(e -> e.isSelected()).forEach(e -> e.putGiftReceived(gift, false));
		});
		notReceived.disableProperty().bind(export.disableProperty());

		menu.getItems().addAll(export, received, notReceived);
		table.setContextMenu(menu);
		
		return table;
	}

}
