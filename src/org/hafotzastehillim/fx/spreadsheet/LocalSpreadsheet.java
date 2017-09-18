package org.hafotzastehillim.fx.spreadsheet;

import static org.hafotzastehillim.fx.spreadsheet.SearchType.ENTRY;
import static org.hafotzastehillim.fx.spreadsheet.SearchType.ENTRY_LIST;
import static org.hafotzastehillim.fx.spreadsheet.SearchType.ROW_IN_TAB;

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
import org.hafotzastehillim.fx.util.Search;

import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;

import javafx.application.Platform;
import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LocalSpreadsheet implements Spreadsheet {

	private File file;
	private XSSFWorkbook workbook;
	private Service<Void> search;
	private Service<Void> load;

	private volatile String query;
	private volatile int searchTab;
	private volatile ObservableList<? super Entry> consumerList;
	private volatile WritableValue<? super Entry> consumerRef;
	private volatile WritableIntegerValue consumerRow;
	private volatile ColumnMatcher matcher;
	private volatile int[] columns;
	private volatile SearchType type;

	public LocalSpreadsheet(File file) throws InvalidFormatException, IOException {
		this.file = file;
		reload();
	}

	private static final DataFormatter formatter = new DataFormatter();
	private volatile FormulaEvaluator evaluator;

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
		for (Cell c : r) {
			list.add(formatter.formatCellValue(c, evaluator));
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
	public void searchEntries(String q, ObservableList<? super Entry> consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
			return;

		if (q.equals(query))
			return;

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		query = q;
		consumerList = consumer;
		this.matcher = matcher;
		this.columns = columns;
		type = ENTRY_LIST;

		search.restart();
	}

	@Override
	public void findEntry(String q, WritableValue<? super Entry> consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
			return;

		if (q.equals(query))
			return;

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		query = q;
		consumerRef = consumer;
		this.matcher = matcher;
		this.columns = columns;
		type = ENTRY;

		search.restart();
	}

	@Override
	public void findRowInTab(int tab, String q, WritableIntegerValue consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
			return;

		if (q.equals(query))
			return;

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		query = q;
		searchTab = tab;
		consumerRow = consumer;
		this.matcher = matcher;
		this.columns = columns;
		type = ROW_IN_TAB;

		search.restart();
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
		loadService().restart();
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	@Override
	public Service<Void> searchService() {
		if (search == null) {

			search = new Service<Void>() {

				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {

						private List<? super Entry> list = consumerList;
						private WritableValue<? super Entry> ref = consumerRef;
						private WritableIntegerValue rowInTab = consumerRow;
						private int sTab = searchTab;

						@Override
						protected Void call() throws Exception {
							String q = query;
							ColumnMatcher m = matcher;
							int[] c = columns;

							if (type == ENTRY_LIST || type == ENTRY) {
								consumerList.clear();

								int size = Math.min(workbook.getNumberOfSheets(), Tab.cities().size());

								outer: for (int i = 0; i < size; i++) {
									Sheet sheet = workbook.getSheetAt(i);
									if (sheet == null)
										continue;

									for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {

										if (isCancelled())
											break outer;

										Row row = sheet.getRow(j);
										if (row == null)
											continue;

										if (Search.matches(LocalSpreadsheet.this, i, j, q, m, c)) {

											found(i, j);

											if (type == ENTRY)
												break outer;
										}

									}
								}
							} else if (type == ROW_IN_TAB) {
								Sheet sheet = workbook.getSheetAt(sTab);

								for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {

									if (isCancelled())
										break;

									Row row = sheet.getRow(j);
									if (row == null)
										continue;

									if (Search.matches(LocalSpreadsheet.this, sTab, j, q, m, c)) {
										rowInTab.set(j);
										break;
									}

								}

							}

							return null;
						}

						private void found(int sheet, int row) {
							Platform.runLater(() -> {
								Entry e = new Entry(LocalSpreadsheet.this, sheet, row);

								if (type == ENTRY_LIST)
									list.add(e);
								else
									ref.setValue(e);
							});
						}

					};
				}

			};

			search.setOnFailed(evt -> {
				Main.pushNotification("Search Failed");
				search.getException().printStackTrace();
			});

		}

		return search;
	}

	@Override
	public Service<Void> loadService() {
		if (load == null) {
			load = new Service<Void>() {
				@Override
				protected void scheduled() {
					query = null;
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
		}
		return load;
	}

}
