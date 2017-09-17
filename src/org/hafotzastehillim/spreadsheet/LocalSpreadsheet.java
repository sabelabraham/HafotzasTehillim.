package org.hafotzastehillim.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hafotzastehillim.fx.Main;

import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LocalSpreadsheet implements Spreadsheet {

	private File file;
	private XSSFWorkbook workbook;
	private ObservableList<Entry> entries;
	private Service<Void> search;
	private Service<Void> load;

	private AtomicReference<String> query;

	public LocalSpreadsheet(File file) throws InvalidFormatException, IOException {
		this.file = file;

		query = new AtomicReference<>();
		entries = FXCollections.observableArrayList();

		search = new Service<Void>() {

			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {

					@Override
					protected void scheduled() {
						entries.clear();
					}

					@Override
					protected Void call() throws Exception {
						String q = query.get().toLowerCase().replace(" ", "");

						for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
							Sheet sheet = workbook.getSheetAt(i);
							if (sheet == null)
								continue;

							for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {
								Row row = sheet.getRow(j);
								if (row == null)
									continue;

								if (Search.matches(LocalSpreadsheet.this, i, j, q)) {
									add(i, j);
								}

							}
						}

						return null;
					}

					private void add(int sheet, int row) {
						Platform.runLater(() -> entries.add(new Entry(LocalSpreadsheet.this, sheet, row)));
					}

				};
			}

		};

		search.setOnFailed(evt -> {
			Main.pushNotification("Search Failed");
			search.getException().printStackTrace();
		});

		load = new Service<Void>() {
			@Override
			protected void scheduled() {
				query.set(null);
				entries.clear();
			}

			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {
					@Override
					protected Void call() throws Exception {

						try (FileInputStream in = new FileInputStream(file)) {
							workbook = new XSSFWorkbook(in);
						}
						evaluator = workbook.getCreationHelper().createFormulaEvaluator();

						return null;
					}
				};
			}
		};

		load.setOnFailed(evt -> {
			Main.pushNotification("Load Failed");
			load.getException().printStackTrace();
		});
		reload();
	}

	private static final DataFormatter formatter = new DataFormatter();
	private FormulaEvaluator evaluator;

	@Override
	public List<String> getRow(int sheet, int row) {
		if (sheet >= workbook.getNumberOfSheets())
			return new ArrayList<>();

		Sheet s = workbook.getSheetAt(sheet);
		Row r = s.getRow(row);
		if (r == null)
			return new ArrayList<>();

		evaluator.clearAllCachedResultValues();

		List<String> list = new ArrayList<>();
		// FIXME NPE here!!!!
		for (Cell c : r) {
			list.add(formatter.formatCellValue(c));
		}

		return list;

	}

	@Override
	public String getCellValue(int sheet, int row, int column) {
		if (sheet >= workbook.getNumberOfSheets())
			return "";

		Sheet s = workbook.getSheetAt(sheet);
		Row r = s.getRow(row);
		if (r == null)
			return "";

		evaluator.clearAllCachedResultValues();
		return formatter.formatCellValue(r.getCell(column), evaluator);

	}

	@Override
	public void setCellValue(int sheet, int row, int column, String value) {
		Sheet s;
		if (sheet >= workbook.getNumberOfSheets())
			s = workbook.createSheet();
		else
			s = workbook.getSheetAt(sheet);

		Row r = s.getRow(row);
		if (r == null)
			r = s.createRow(row);

		Cell c = r.getCell(column);
		if (c == null)
			c = r.createCell(column);

		c.setCellValue(value);

		save();
	}

	@Override
	public void setCellValue(int sheet, int row, int column, double value) {
		Sheet s;
		if (sheet >= workbook.getNumberOfSheets())
			s = workbook.createSheet();
		else
			s = workbook.getSheetAt(sheet);

		Row r = s.getRow(row);
		if (r == null)
			r = s.createRow(row);

		Cell c = r.getCell(column);
		if (c == null)
			c = r.createCell(column);

		c.setCellValue(value);

		save();
	}

	@Override
	public int addRow(int sheet, List<String> data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateRow(int sheet, int row, List<String> data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int uniqueId(int tab) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void search(String query) {
		if (query == null || query.isEmpty())
			return;

		if (query.equals(this.query.get()))
			return;

		if (search.isRunning() || load.isRunning())
			return;

		this.query.set(query);
		search.restart();
	}

	@Override
	public Entry findFirst(String query, ColumnMatcher matcher, Column... columns) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int findRowInTab(int tab, String query, ColumnMatcher matcher, Column... columns) {
		throw new UnsupportedOperationException();
	}

	public Task<Void> saveTask() {
		return new Task<Void>() {
			@Override
			public Void call() throws Exception {

				Path temp = file.toPath().getParent().resolve("Working copy - " + file.getName());
				try (OutputStream out = Files.newOutputStream(temp)) {
					workbook.write(out);
					Files.move(temp, file.toPath(), StandardCopyOption.ATOMIC_MOVE,
							StandardCopyOption.REPLACE_EXISTING);
				} finally {
					Files.deleteIfExists(temp);
				}

				return null;
			}

			@Override
			protected void failed() {
				Main.pushNotification("Save Failed");
				getException().printStackTrace();
			}
		};
	}

	public void save() {
		new Thread(saveTask()).start();
	}

	public void saveAndWait() {
		saveTask().run();
	}

	@Override
	public void reload() {
		load.restart();
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	@Override
	public Service<Void> searchService() {
		return search;
	}

	@Override
	public Service<Void> loadService() {
		return load;
	}

	@Override
	public ObservableList<Entry> getResults() {
		return entries;
	}

}
