package org.hafotzastehillim.spreadsheet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hafotzastehillim.fx.Main;
import org.hafotzastehillim.fx.Util;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class GoogleSpreadsheet implements Spreadsheet {

	private Sheets service;
	private String sheetId;

	private List<String> tabs; // Stick to enum
	private Map<Integer, List<List<Object>>> cache;

	private Service<Void> search;
	private Service<Void> load;

	private LinkedBlockingQueue<Update> updates;
	private Thread updater;

	private AtomicReference<String> query;
	private ObservableList<Entry> entries;

	private List<Integer> highestIds;

	public GoogleSpreadsheet(String id, Sheets service) throws IOException {
		sheetId = id;
		this.service = service;

		query = new AtomicReference<>();
		entries = FXCollections.observableArrayList();

		highestIds = new ArrayList<>();
		cache = new ConcurrentHashMap<>();
		updates = new LinkedBlockingQueue<>();

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

						for (int i = 0; i < cache.size(); i++) {
							for (int j = 0; j < cache.get(i).size(); j++) {
								if (Search.matches(GoogleSpreadsheet.this, i, j, q)) {
									add(i, j);
								}
							}
						}
						return null;
					}

					private void add(int sheet, int row) {
						Platform.runLater(() -> entries.add(new Entry(GoogleSpreadsheet.this, sheet, row)));
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
						lookForNewTabs();

						List<String> batch = new ArrayList<>();
						for (String tab : tabs) {
							batch.add(tab + "!A:ZZ");
						}

						List<List<List<Object>>> response = doLoadRangeBatch(batch);

						for (int i = 0; i < tabs.size(); i++) {
							List<List<Object>> tab = response.get(i);
							cache.put(i, tab);

							int max = 0;
							for (int row = 1; row < tab.size(); row++) {
								try {
									max = Math.max(
											Integer.parseInt(tab.get(row).get(Column.ID_NUMBER.getColumn()).toString()),
											max);
								} catch (NumberFormatException e) {
									Main.pushNotification("INVALID ID: Tab: " + (i + 1) + ", Row: " + (row + 1));
								}
							}

							if (i > highestIds.size())
								throw new AssertionError("Should not happen");

							if (i == highestIds.size()) {
								highestIds.add(max);
							} else {
								highestIds.set(i, max);
							}
						}

						return null;
					}
				};
			}
		};

		load.setOnFailed(evt -> {
			Main.pushNotification("Load Failed");
			load.getException().printStackTrace();
		});

		updater = new Thread() {
			@Override
			public void run() {
				while (!updates.isEmpty() || Main.running) {
					try {
						Update update = updates.poll(3, TimeUnit.SECONDS);
						if (update == null)
							continue;

						doUpdateRange(update.range, update.change);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		reload();

		updater.start();
	}

	@Override
	public String getCellValue(int sheet, int row, int column) {
		List<List<Object>> doc = cache.get(sheet);
		if (doc == null || sheet >= doc.size())
			return "";

		List<Object> r = doc.get(row);
		if (r == null || column >= r.size())
			return "";

		Object cell = r.get(column);
		if (cell == null)
			return "";

		return cell.toString();
	}

	@Override
	public List<String> getRow(int sheet, int row) {
		if (!cache.containsKey(sheet))
			return new ArrayList<>();

		List<List<Object>> tab = cache.get(sheet);
		if (tab.size() < row)
			return new ArrayList<>();

		return tab.get(row).stream().map(r -> r.toString()).collect(Collectors.toList());
	}

	@Override
	public void setCellValue(int sheet, int row, int column, String value) {
		String cellAddress = Column.toName(column) + (row + 1);
		placeInCache(sheet, row, column, value);
		pushUpdate(new Update(tabs.get(sheet) + "!" + cellAddress + ":" + cellAddress, value));
	}

	@Override
	public void setCellValue(int sheet, int row, int column, double value) {
		int i = (int) value;
		if (i == value) // remove decimal
			setCellValue(sheet, row, column, "" + i);
		else
			setCellValue(sheet, row, column, "" + value);
	}

	@Override
	public int addRow(int sheet, List<String> data) {
		List<List<Object>> tab = cache.get(sheet);
		int last = tab.size();

		List<Object> objData = new ArrayList<>(data);
		tab.add(objData);

		List<List<Object>> range = new ArrayList<>();
		range.add(objData);

		pushUpdate(new Update(tabs.get(sheet) + "!" + (last + 1) + ":" + (last + 1), range));
		return last;
	}

	@Override
	public void updateRow(int sheet, int row, List<String> data) {
		if (data.size() == 0)
			return;

		List<List<Object>> tab = cache.get(sheet);

		List<Object> objData = new ArrayList<>(data);
		tab.set(row, objData);

		List<List<Object>> range = new ArrayList<>();
		range.add(objData);

		pushUpdate(new Update(tabs.get(sheet) + "!A" + (row + 1) + ":" + Column.toName(data.size() - 1) + (row + 1),
				range));
	}

	@Override
	public int uniqueId(int tab) {
		int max = highestIds.get(tab);
		highestIds.set(tab, ++max);
		return max;
	}

	public String doLoadCellValue(int sheet, int row, int column) {
		String columnRow = Column.toName(column + 1) + (row + 1);
		String range = tabs.get(sheet) + "!" + columnRow + ":" + columnRow;

		ValueRange response = null;
		try {
			response = service.spreadsheets().values().get(sheetId, range).execute();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			e.printStackTrace();
		}
		if (response == null)
			return "";

		String cell = response.getValues().get(0).get(0).toString();
		placeInCache(sheet, row, column, cell);

		return cell;
	}

	public void doUpdateCellValue(int sheet, int row, int column, String value) {
		String columnRow = Column.toName(column + 1) + (row + 1);
		String range = tabs.get(sheet) + "!" + columnRow + ":" + columnRow;

		try {
			ValueRange changes = new ValueRange();
			List<List<Object>> list = new ArrayList<>();
			list.add(Arrays.asList(value));
			changes.setValues(list);
			service.spreadsheets().values().update(sheetId, range, changes).setValueInputOption("USER_ENTERED")
					.execute();

			placeInCache(sheet, row, column, value);
		} catch (IOException e) {
			Main.pushNotification("Update Failed");
			Util.showErrorDialog(e);
			e.printStackTrace();
		}

	}

	public void pushUpdate(Update update) {
		updates.add(update);
	}

	// FIXME add placeInCache()
	public List<List<Object>> doLoadRange(String range) {

		ValueRange response = null;
		try {
			response = service.spreadsheets().values().get(sheetId, range).execute();
		} catch (IOException e) {
			Main.pushNotification("Load Failed");
			Util.showErrorDialog(e);
			e.printStackTrace();
		}

		if (response == null)
			return Arrays.asList(Arrays.asList());

		return response.getValues();
	}

	public List<List<Object>> doLoadRange(int sheet, String range) {
		range = tabs.get(sheet) + "!" + range;
		return doLoadRange(range);
	}

	public List<List<List<Object>>> doLoadRangeBatch(List<String> range) {

		BatchGetValuesResponse response = null;
		try {
			response = service.spreadsheets().values().batchGet(sheetId).setRanges(range).execute();
		} catch (IOException e) {
			Main.pushNotification("Load Failed");
			Util.showErrorDialog(e);
			e.printStackTrace();
		}

		if (response == null)
			return Arrays.asList(Arrays.asList());

		return response.getValueRanges().stream().map(vr -> vr.getValues()).collect(Collectors.toList());
	}

	public void doUpdateRange(String range, List<List<Object>> values) {
		try {
			ValueRange changes = new ValueRange();
			changes.setValues(values);
			service.spreadsheets().values().update(sheetId, range, changes).setValueInputOption("USER_ENTERED")
					.execute();
		} catch (IOException e) {
			Main.pushNotification("Update Failed");
			Util.showErrorDialog(e);
			e.printStackTrace();
		}
	}

	public void doUpdateRange(int sheet, String range, List<List<Object>> values) {
		range = tabs.get(sheet) + "!" + range;
		doUpdateRange(range, values);
	}

	public void placeInCache(int sheet, int row, int column, String value) {
		List<List<Object>> tab = cache.get(sheet);
		if (tab == null)
			return;

		List<Object> r = tab.get(row);
		if (r == null) {
			if (row >= tab.size()) {

				while (row > tab.size()) {
					tab.add(new ArrayList<>());
				}

				r = new ArrayList<>();
				tab.add(r);
			}
		}

		if (column < r.size())
			r.set(column, value);
		else {
			while (column > r.size()) {
				r.add("");
			}
			r.add(value);
		}
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
		for (int i = 0; i < cache.size(); i++) {
			for (int j = 0; j < cache.get(i).size(); j++) {
				for (Column col : columns) {
					if (col.getColumn() >= cache.get(i).get(j).size())
						continue;

					if (matcher.matches(query, cache.get(i).get(j).get(col.getColumn()).toString(), col)) {
						return new Entry(this, i, j);
					}
				}
			}
		}

		return null;
	}

	@Override
	public int findRowInTab(int tab, String query, ColumnMatcher matcher, Column... columns) {
		for (int j = 0; j < cache.get(tab).size(); j++) {
			for (Column col : columns) {
				if (col.getColumn() >= cache.get(tab).get(j).size())
					continue;

				if (matcher.matches(query, cache.get(tab).get(j).get(col.getColumn()).toString(), col)) {
					return j;
				}
			}
		}

		return -1;

	}

	public void reload() {
		load.restart();
	}

	public void reloadTab(int tab) throws IOException {
		if (tabs.size() >= tab)
			lookForNewTabs();

		String name = tabs.get(tab);
		if (name == null)
			throw new IndexOutOfBoundsException("" + tab);

		for (int i = 0; i < tabs.size(); i++) {
			List<List<Object>> doc = doLoadRange(tabs.get(tab) + "!A:ZZ");
			cache.put(tab, doc);
		}

	}

	public void lookForNewTabs() throws IOException {
		tabs = service.spreadsheets().get(sheetId).execute().getSheets().stream().map(s -> s.getProperties().getTitle())
				.collect(Collectors.toList());
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

	private static class Update {
		private String range;
		private List<List<Object>> change;

		Update(String range, List<List<Object>> change) {
			this.range = range;
			this.change = change;
		}

		Update(String range, String change) {
			this.range = range;
			this.change = Arrays.asList(Arrays.asList(change));
		}
	}
}