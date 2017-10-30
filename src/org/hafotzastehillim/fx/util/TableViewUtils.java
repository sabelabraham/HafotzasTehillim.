package org.hafotzastehillim.fx.util;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hafotzastehillim.fx.DetailsPane;
import org.hafotzastehillim.fx.GiftResultsView;
import org.hafotzastehillim.fx.cell.FamilyTableRow;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.FamilyGrouping;
import org.hafotzastehillim.fx.spreadsheet.Selectable;
import org.hafotzastehillim.fx.spreadsheet.Tab;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

public class TableViewUtils {

	private TableViewUtils() {

	}

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, ''yy")
			.withZone(ZoneId.systemDefault());

	@SuppressWarnings("deprecation")
	public static TableView<Entry> getTable(ObservableList<Entry> items) {
		items.sort(null);
		items.forEach(e -> e.setSelected(false));

		TableView<Entry> table = new TableView<>();
		table.setOnKeyPressed(evt -> {
			if (evt.getCode() == KeyCode.TAB)
				evt.consume(); // don't move focus off checkbox
		});
		table.setOnMouseClicked(evt -> {
			if (evt.getClickCount() == 2) {
				Entry e = table.getSelectionModel().getSelectedItem();
				DetailsPane.showDialog(e);
			}
		});

		table.setEditable(true);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.getStyleClass().add("family-highlighted-table-view");
		table.getStylesheets()
				.add(GiftResultsView.class.getResource("/resources/css/results-table.css").toExternalForm());

		TableColumn<Entry, Integer> number = new TableColumn<>();
		number.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(items.indexOf(cdf.getValue()) + 1));
		Callback<TableColumn<Entry, Integer>, TableCell<Entry, Integer>> cellFactory = number.getCellFactory();

		TableColumn<Entry, Boolean> select = new TableColumn<>();
		select.setEditable(true);

		CheckBox columnBox = new CheckBox();
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

		number.setCellFactory(col -> {
			TableCell<Entry, Integer> cell = cellFactory.call(col);
			cell.setAlignment(Pos.CENTER_RIGHT);

			cell.getStyleClass().add("number-cell");

			return cell;
		});

		TableColumn<Entry, String> id = new TableColumn<>("ID");
		id.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<Entry, String> memberSince = new TableColumn<>("Member Since");
		memberSince.setCellValueFactory(cdf -> {
			return Bindings.createStringBinding(() -> FORMATTER.format(cdf.getValue().getCreatedInstant()),
					cdf.getValue().createdProperty());
		});

		TableColumn<Entry, String> gender = new TableColumn<>("Gender");
		gender.setCellValueFactory(new PropertyValueFactory<>("gender"));

		TableColumn<Entry, String> firstName = new TableColumn<>("First");
		firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));

		TableColumn<Entry, String> lastName = new TableColumn<>("Last");
		lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

		TableColumn<Entry, String> addressNumber = new TableColumn<>("#");
		addressNumber.setCellValueFactory(new PropertyValueFactory<>("addressNumber"));

		TableColumn<Entry, String> addressName = new TableColumn<>("Address");
		addressName.setCellValueFactory(new PropertyValueFactory<>("addressName"));

		TableColumn<Entry, String> apt = new TableColumn<>("Apt");
		apt.setCellValueFactory(new PropertyValueFactory<>("apt"));

		TableColumn<Entry, String> city = new TableColumn<>("City");
		city.setCellValueFactory(new PropertyValueFactory<>("city"));

		TableColumn<Entry, String> state = new TableColumn<>("State");
		state.setCellValueFactory(new PropertyValueFactory<>("state"));

		TableColumn<Entry, String> zip = new TableColumn<>("Zip");
		zip.setCellValueFactory(new PropertyValueFactory<>("zip"));

		TableColumn<Entry, String> cityYiddish = new TableColumn<>("\u05e9\u05d8\u05d0\u05d8");
		cityYiddish.setCellValueFactory(new PropertyValueFactory<>("cityYiddish"));

		TableColumn<Entry, String> age = new TableColumn<>("\u05db\u05ea\u05d4");
		age.setCellValueFactory(new PropertyValueFactory<>("age"));

		TableColumn<Entry, String> school = new TableColumn<>("\u05de\u05d5\u05e1\u05d3");
		school.setCellValueFactory(new PropertyValueFactory<>("school"));

		TableColumn<Entry, String> phone = new TableColumn<>("\u05d8\u05e2\u05dc");
		phone.setCellValueFactory(new PropertyValueFactory<>("phone"));

		TableColumn<Entry, String> fatherName = new TableColumn<>("\u05d8\u05d0\u05d8\u05e2'\u05e1");
		fatherName.setCellValueFactory(new PropertyValueFactory<>("fatherName"));

		TableColumn<Entry, String> lastNameYiddish = new TableColumn<>("\u05dc\u05e2\u05e6\u05d8\u05e2");
		lastNameYiddish.setCellValueFactory(new PropertyValueFactory<>("lastNameYiddish"));

		TableColumn<Entry, String> firstNameYiddish = new TableColumn<>("\u05e2\u05e8\u05e9\u05d8\u05e2");
		firstNameYiddish.setCellValueFactory(new PropertyValueFactory<>("firstNameYiddish"));

		TableColumn<Entry, String> total = new TableColumn<>("Total");
		total.setCellValueFactory(new PropertyValueFactory<>("total"));

		table.getColumns()
				.addAll(Arrays.asList(number, select, id, memberSince, gender, firstName, lastName, addressNumber,
						addressName, apt, city, state, zip, cityYiddish, age, school, phone, fatherName,
						lastNameYiddish, firstNameYiddish, total));

		table.getColumns().forEach(col -> col.impl_setReorderable(false));
		table.getColumns().forEach(col -> col.setSortable(false));

		table.setItems(items);

		Map<Entry, Integer> familyIndexer = new HashMap<>();

		Entry prev = null;
		int index = -1;

		for (Entry e : items) {
			if (prev == null) {
				familyIndexer.put(e, ++index);
				prev = e;
				continue;
			}

			if (prev.getPhone().equals(e.getPhone())) {
				familyIndexer.put(e, index);
			} else {
				familyIndexer.put(e, ++index);
			}

			prev = e;
		}

		table.setRowFactory(tv -> new FamilyTableRow(e -> familyIndexer.get(e)));

		number.setMinWidth(45);
		number.setMaxWidth(45);

		select.setMinWidth(30);
		select.setMaxWidth(30);

		id.setMinWidth(50);
		id.setMaxWidth(50);

		gender.setMinWidth(45);
		gender.setMaxWidth(45);

		addressNumber.setMinWidth(45);
		addressNumber.setMaxWidth(45);

		apt.setMinWidth(45);
		apt.setMaxWidth(45);

		state.setMinWidth(50);
		state.setMaxWidth(50);

		zip.setMinWidth(50);
		zip.setMaxWidth(50);

		age.setMinWidth(40);
		age.setMaxWidth(40);

		total.setMinWidth(50);
		total.setMaxWidth(50);

		return table;
	}

	@SuppressWarnings("deprecation")
	public static TableView<FamilyGrouping> getFamilyTable(ObservableList<FamilyGrouping> items) {
		items.sort(null);
		items.forEach(f -> f.setSelected(false));

		TableView<FamilyGrouping> table = new TableView<>();
		table.setOnKeyPressed(evt -> {
			if (evt.getCode() == KeyCode.TAB)
				evt.consume(); // don't move focus off checkbox
		});
		table.setOnMouseClicked(evt -> {
			if (evt.getClickCount() == 2) {
				FamilyGrouping f = table.getSelectionModel().getSelectedItem();
				DetailsPane.showDialog(f.getSiblings());
			}
		});

		table.setEditable(true);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.getStylesheets()
				.add(GiftResultsView.class.getResource("/resources/css/results-table.css").toExternalForm());

		TableColumn<FamilyGrouping, Integer> number = new TableColumn<>();
		number.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(items.indexOf(cdf.getValue()) + 1));
		Callback<TableColumn<FamilyGrouping, Integer>, TableCell<FamilyGrouping, Integer>> cellFactory = number
				.getCellFactory();

		TableColumn<FamilyGrouping, Boolean> select = new TableColumn<>();
		select.setEditable(true);

		CheckBox columnBox = new CheckBox();
		columnBox.setFocusTraversable(false);
		columnBox.setOnAction(evt -> items.forEach(entry -> entry.setSelected(columnBox.isSelected())));

		select.setGraphic(columnBox);
		select.setCellFactory(col -> {
			CheckBoxTableCell<FamilyGrouping, Boolean> cell = new CheckBoxTableCell<>();

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

		number.setCellFactory(col -> {
			TableCell<FamilyGrouping, Integer> cell = cellFactory.call(col);
			cell.setAlignment(Pos.CENTER_RIGHT);

			cell.getStyleClass().add("number-cell");

			return cell;
		});

		TableColumn<FamilyGrouping, String> id = new TableColumn<>("ID");
		id.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<FamilyGrouping, String> lastName = new TableColumn<>("Last");
		lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

		TableColumn<FamilyGrouping, String> addressNumber = new TableColumn<>("#");
		addressNumber.setCellValueFactory(new PropertyValueFactory<>("addressNumber"));

		TableColumn<FamilyGrouping, String> addressName = new TableColumn<>("Address");
		addressName.setCellValueFactory(new PropertyValueFactory<>("addressName"));

		TableColumn<FamilyGrouping, String> apt = new TableColumn<>("Apt");
		apt.setCellValueFactory(new PropertyValueFactory<>("apt"));

		TableColumn<FamilyGrouping, String> city = new TableColumn<>("City");
		city.setCellValueFactory(new PropertyValueFactory<>("city"));

		TableColumn<FamilyGrouping, String> state = new TableColumn<>("State");
		state.setCellValueFactory(new PropertyValueFactory<>("state"));

		TableColumn<FamilyGrouping, String> zip = new TableColumn<>("Zip");
		zip.setCellValueFactory(new PropertyValueFactory<>("zip"));

		TableColumn<FamilyGrouping, String> cityYiddish = new TableColumn<>("\u05e9\u05d8\u05d0\u05d8");
		cityYiddish.setCellValueFactory(new PropertyValueFactory<>("cityYiddish"));

		TableColumn<FamilyGrouping, String> phone = new TableColumn<>("\u05d8\u05e2\u05dc");
		phone.setCellValueFactory(new PropertyValueFactory<>("phone"));

		TableColumn<FamilyGrouping, String> fatherName = new TableColumn<>("\u05d8\u05d0\u05d8\u05e2'\u05e1");
		fatherName.setCellValueFactory(new PropertyValueFactory<>("fatherName"));

		TableColumn<FamilyGrouping, String> lastNameYiddish = new TableColumn<>("\u05dc\u05e2\u05e6\u05d8\u05e2");
		lastNameYiddish.setCellValueFactory(new PropertyValueFactory<>("lastNameYiddish"));

		table.getColumns().addAll(Arrays.asList(number, select, lastName, addressNumber, addressName, apt, city, state,
				zip, cityYiddish, phone, fatherName, lastNameYiddish));

		table.getColumns().forEach(col -> col.impl_setReorderable(false));
		table.getColumns().forEach(col -> col.setSortable(false));

		table.setItems(items);

		number.setMinWidth(45);
		number.setMaxWidth(45);

		select.setMinWidth(30);
		select.setMaxWidth(30);

		id.setMinWidth(50);
		id.setMaxWidth(50);

		addressNumber.setMinWidth(45);
		addressNumber.setMaxWidth(45);

		apt.setMinWidth(45);
		apt.setMaxWidth(45);

		state.setMinWidth(50);
		state.setMaxWidth(50);

		zip.setMinWidth(50);
		zip.setMaxWidth(50);

		return table;
	}

	private static final Preferences prefs = Preferences.userNodeForPackage(TableViewUtils.class);
	private static final String LAST_FILE_KEY = "currentReportFile";

	public static void exportToExcel(TableView<Entry> table) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Spreadsheet", "*.xlsx"));
		File f = new File(prefs.get(LAST_FILE_KEY, System.getProperty("user.home")));
		if (!f.exists() || !f.isDirectory())
			f = new File(System.getProperty("user.home"));

		chooser.setInitialDirectory(f);
		chooser.setTitle("Export Report...");

		File file = chooser.showSaveDialog(table.getScene().getWindow());

		if (file == null)
			return;

		new Thread(() -> {
			prefs.put(LAST_FILE_KEY, file.getParentFile().getAbsolutePath());

			List<Entry> selected = table.getItems().stream().filter(e -> e.isSelected()).collect(Collectors.toList());
			Map<Entry, Integer> familyIndexer = new HashMap<>();

			Entry prev = null;
			int index = -1;

			for (Entry e : selected) {
				if (prev == null) {
					familyIndexer.put(e, ++index);
					prev = e;
					continue;
				}

				if (prev.getPhone().equals(e.getPhone())) {
					familyIndexer.put(e, index);
				} else {
					familyIndexer.put(e, ++index);
				}

				prev = e;
			}

			int campaignCount = 0;
			int giftCount = 0;
			for (Entry e : selected) {
				if (e.getPoints().size() > campaignCount)
					campaignCount = e.getPoints().size();
				if (e.getGiftsReceived().size() > giftCount)
					giftCount = e.getGiftsReceived().size();
			}

			try (XSSFWorkbook excel = new XSSFWorkbook()) {

				excel.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);

				XSSFCellStyle header = excel.createCellStyle();
				header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				header.setFillForegroundColor(new XSSFColor(new Color(165, 165, 165)));
				header.setAlignment(HorizontalAlignment.CENTER);

				XSSFCellStyle highlight = excel.createCellStyle();
				highlight.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				highlight.setFillForegroundColor(new XSSFColor(new Color(225, 225, 225)));

				Sheet sheet = excel.createSheet("Report");
				Row headerRow = sheet.createRow(0);

				List<TableColumn<Entry, ?>> columns = table.getColumns().subList(2, table.getColumns().size()).stream()
						.filter(col -> col.isVisible()).collect(Collectors.toList());

				for (int i = 0; i < columns.size(); i++) {
					Cell c = headerRow.getCell(i);
					c.setCellValue(columns.get(i).getText());
					c.setCellStyle(header);
				}

				for (int i = 0; i < campaignCount; i++) {
					Cell c = headerRow.getCell(columns.size() + i);
					c.setCellValue("Campaign " + (i + 1));
					c.setCellStyle(header);
				}

				for (int i = 0; i < giftCount; i++) {
					Cell c = headerRow.getCell(columns.size() + campaignCount + i);
					c.setCellValue("Gift " + (i + 1));
					c.setCellStyle(header);
				}

				for (Entry e : selected) {
					boolean even = familyIndexer.get(e) % 2 == 0;
					int idx = table.getItems().indexOf(e);

					Row row = sheet.createRow(sheet.getLastRowNum() + 1);
					Cell c;

					for (int i = 0; i < columns.size(); i++) {
						c = row.getCell(i);

						String value = columns.get(i).getCellData(idx).toString();
						c.setCellValue(value);
						if (even)
							c.setCellStyle(highlight);
					}

					for (int i = 0; i < campaignCount; i++) {
						c = row.getCell(columns.size() + i);
						int point = e.getPoint(i);
						if (point > 0)
							c.setCellValue(point);

						if (even)
							c.setCellStyle(highlight);
					}

					for (int i = 0; i < giftCount; i++) {
						c = row.getCell(columns.size() + campaignCount + i);

						if (e.isGiftRecieved(i)) {
							c.setCellValue(Entry.CHECKMARK);
						}

						if (even)
							c.setCellStyle(highlight);
					}

				}

				for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++)
					sheet.autoSizeColumn(i);

				try (OutputStream out = new FileOutputStream(file)) {
					excel.write(out);
				}
			} catch (IOException e) {
				Util.showErrorDialog(e);
			}
		}).start();

	}

	public static void exportSelectableToExcel(TableView<? extends Selectable> table) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Spreadsheet", "*.xlsx"));
		File f = new File(prefs.get(LAST_FILE_KEY, System.getProperty("user.home")));
		if (!f.exists() || !f.isDirectory())
			f = new File(System.getProperty("user.home"));

		chooser.setInitialDirectory(f);
		chooser.setTitle("Export Report...");

		File file = chooser.showSaveDialog(table.getScene().getWindow());

		if (file == null)
			return;

		new Thread(() -> {
			prefs.put(LAST_FILE_KEY, file.getParentFile().getAbsolutePath());

			List<Selectable> selected = table.getItems().stream().filter(e -> e.isSelected())
					.collect(Collectors.toList());

			try (XSSFWorkbook excel = new XSSFWorkbook()) {

				excel.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);

				XSSFCellStyle header = excel.createCellStyle();
				header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				header.setFillForegroundColor(new XSSFColor(new Color(165, 165, 165)));
				header.setAlignment(HorizontalAlignment.CENTER);

				Sheet sheet = excel.createSheet("Report");
				Row headerRow = sheet.createRow(0);

				List<TableColumn<?, ?>> columns = table.getColumns().subList(2, table.getColumns().size()).stream()
						.filter(col -> col.isVisible()).collect(Collectors.toList());

				for (int i = 0; i < columns.size(); i++) {
					Cell c = headerRow.getCell(i);
					c.setCellValue(columns.get(i).getText());
					c.setCellStyle(header);
				}

				for (Selectable s : selected) {
					int idx = table.getItems().indexOf(s);

					Row row = sheet.createRow(sheet.getLastRowNum() + 1);
					Cell c;

					for (int i = 0; i < columns.size(); i++) {
						c = row.getCell(i);

						String value = columns.get(i).getCellData(idx).toString();
						c.setCellValue(value);
					}
				}

				for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++)
					sheet.autoSizeColumn(i);

				try (OutputStream out = new FileOutputStream(file)) {
					excel.write(out);
				}
			} catch (IOException e) {
				Util.showErrorDialog(e);
			}
		}).start();

	}
}
