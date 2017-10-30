package org.hafotzastehillim.fx.spreadsheet;

import static org.hafotzastehillim.fx.spreadsheet.SearchType.*;
import static org.hafotzastehillim.fx.spreadsheet.ScanType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.Main;
import org.hafotzastehillim.fx.util.Search;
import org.hafotzastehillim.fx.util.Util;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Platform;
import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;

public class GoogleSpreadsheet implements Spreadsheet {

	private Sheets service;
	private String sheetId;

	private List<String> tabs;
	private ObservableList<String> cities;
	private Map<Integer, List<List<Object>>> cache;

	private Service<Void> search;
	private Service<Void> load;

	private LinkedBlockingQueue<Update> updates;
	private Thread updater;

	private volatile String query;
	private volatile ObservableList<? super Entry> entryList;
	private volatile WritableValue<? super Entry> entryRef;
	private volatile ObservableList<Integer> rowList;
	private volatile WritableIntegerValue consumerRow;
	private volatile int searchTab;
	private volatile ColumnMatcher matcher;
	private volatile int[] columns;
	private volatile Predicate<List<String>> tester;
	private volatile SearchType searchType;
	private volatile ScanType scanType;

	public GoogleSpreadsheet(String id, Sheets service) {
		sheetId = id;
		this.service = service;

		tabs = Arrays.stream(Tab.values()).map(tab -> tab.toString()).collect(Collectors.toList());
		cities = FXCollections.observableArrayList();

		cache = new ConcurrentHashMap<>();
		updates = new LinkedBlockingQueue<>();

		updater = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(3000);

						if (!Main.running && updates.isEmpty())
							break;

						List<Update> updateList = new ArrayList<>();
						List<Update> appendList;
						if (updates.drainTo(updateList) > 0) {
							appendList = updateList.stream().filter(u -> u.append).collect(Collectors.toList());
							updateList.removeAll(appendList);

							List<String> ranges = new ArrayList<>();

							List<List<List<Object>>> changes = new ArrayList<>();
							Map<String, List<List<Object>>> appendsMap = new HashMap<>();

							for (Update u : appendList) {
								List<List<Object>> l = appendsMap.get(u.range);
								if (l == null) {
									l = u.change;
									appendsMap.put(u.range, l);
								} else {
									l.addAll(u.change);
								}
							}

							for (Map.Entry<String, List<List<Object>>> e : appendsMap.entrySet()) {
								int sheet = tabs.indexOf(e.getKey());
								int row = doAppend(sheet, e.getValue());

								int index = 0;
								for (Update u : appendList) {
									if (u.range.equals(e.getKey())) {
										u.callback.accept(row + index);
										index++;
									}
								}
							}

							for (Update u : updateList) {
								ranges.add(u.range);
								changes.add(u.change);
							}

							boolean success = doUpdateRangeBatch(ranges, changes);
							for (Update u : updateList) {
								if (u.callback != null) {
									u.callback.accept(success ? 1 : -1);
								}
							}
						}

					} catch (Throwable t) {
						Util.showErrorDialog(t);
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
	public void setCellValue(int sheet, int row, int column, String value, Consumer<Integer> callback) {
		String cellAddress = Column.toName(column) + (row + 1);
		placeInCache(sheet, row, column, value);
		pushUpdate(new Update(tabs.get(sheet) + "!" + cellAddress + ":" + cellAddress, value, false, callback));
	}

	@Override
	public void setCellValue(int sheet, int row, int column, double value, Consumer<Integer> callback) {
		int i = (int) value;
		if (i == value) // remove decimal
			setCellValue(sheet, row, column, "" + i, callback);
		else
			setCellValue(sheet, row, column, "" + value, callback);
	}

	@Override
	public void addRow(int sheet, List<String> data, Consumer<Integer> callback) {

		List<Object> objData = new ArrayList<>(data);
		List<List<Object>> range = Arrays.asList(objData);

		pushUpdate(new Update(tabs.get(sheet), range, true, callback));
	}

	@Override
	public void updateRow(int sheet, int row, List<String> data, Consumer<Integer> callback) {
		if (data.size() == 0)
			return;

		List<Object> objData = new ArrayList<>(data);

		List<List<Object>> range = new ArrayList<>();
		range.add(objData);

		pushUpdate(new Update(tabs.get(sheet) + "!A" + (row + 1) + ":" + Column.toName(data.size() - 1) + (row + 1),
				range, false, callback));

		placeInCache(sheet, row, objData);
	}

	@Override
	public void persist(Entry e, Consumer<Integer> callback) {
		if (e.getRow() >= 0)
			throw new IllegalStateException("Entry already persisted.");

		if (e.getCityYiddish().isEmpty())
			throw new IllegalStateException("Could not infer entry tab for persistence.");
		if (e.getTab() < 0) {
			e.setTab(tabs.indexOf(e.getCityYiddish()));
			if (e.getTab() < 0)
				e.setTab(Tab.OTHER_CITIES.ordinal());
		}

		List<String> data = Arrays.asList("", "", "", "", "Please Wait...");
		addRow(e.getTab(), data, row -> {
			int id = assumingValidId(e.getTab(), row);

			e.setRow(row);
			e.setId(id + "");
			e.saveDetails();

			if (callback != null) {
				callback.accept(row);
			}
		});
	}

	public String doLoadCellValue(int sheet, int row, int column) {
		String columnRow = Column.toName(column + 1) + (row + 1);
		String range = tabs.get(sheet) + "!" + columnRow + ":" + columnRow;

		ValueRange response = null;
		try {
			response = service.spreadsheets().values().get(sheetId, range).execute();
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to access the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Util.showErrorDialog(e);
		}
		if (response == null)
			return "";

		String cell = response.getValues().get(0).get(0).toString();
		placeInCache(sheet, row, column, cell);

		return cell;
	}

	public boolean doUpdateCellValue(int sheet, int row, int column, String value) {
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
			return true;
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to modify the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Main.pushNotification("Update Failed");
			Util.showErrorDialog(e);
		}

		return false;

	}

	public void pushUpdate(Update update) {
		updates.add(update);
	}

	// FIXME add placeInCache()
	public List<List<Object>> doLoadRange(String range) {

		ValueRange response = null;
		try {
			response = service.spreadsheets().values().get(sheetId, range).execute();
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to access the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Main.pushNotification("Load Failed");
			Util.showErrorDialog(e);
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
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to access the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Main.pushNotification("Load Failed");
			Util.showErrorDialog(e);
		}

		if (response == null)
			return Arrays.asList(Arrays.asList());

		return response.getValueRanges().stream().map(vr -> vr.getValues()).collect(Collectors.toList());
	}

	public boolean doUpdateRange(String range, List<List<Object>> values) {
		try {
			ValueRange changes = new ValueRange();
			changes.setValues(values);
			service.spreadsheets().values().update(sheetId, range, changes).setValueInputOption("USER_ENTERED")
					.execute();

			return true;
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to modify the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Main.pushNotification("Update Failed");
			Util.showErrorDialog(e);
		}

		return false;
	}

	public boolean doUpdateRangeBatch(List<String> ranges, List<List<List<Object>>> values) {
		try {
			BatchUpdateValuesRequest batch = new BatchUpdateValuesRequest();
			List<ValueRange> data = new ArrayList<>();
			for (int i = 0; i < ranges.size(); i++) {
				ValueRange vr = new ValueRange();
				vr.setRange(ranges.get(i));
				vr.setValues(values.get(i));

				data.add(vr);
			}

			batch.setData(data);
			batch.setValueInputOption("USER_ENTERED");

			service.spreadsheets().values().batchUpdate(sheetId, batch).execute();

			return true;
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to modify the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Main.pushNotification("Update Failed");
			Util.showErrorDialog(e);
		}

		return false;
	}

	public boolean doUpdateRange(int sheet, String range, List<List<Object>> values) {
		range = tabs.get(sheet) + "!" + range;
		return doUpdateRange(range, values);
	}

	public int doAppend(int sheet, List<List<Object>> values) {
		try {
			ValueRange changes = new ValueRange();
			changes.setValues(values);
			AppendValuesResponse response = service.spreadsheets().values()
					.append(sheetId, tabs.get(sheet) + "!A:A", changes).setValueInputOption("USER_ENTERED")
					.setInsertDataOption("INSERT_ROWS").execute();

			String range = response.getUpdates().getUpdatedRange();
			int start = range.lastIndexOf('!') + 1;
			int end = range.lastIndexOf(':');

			if (end == -1)
				end = range.length();

			String subrange = range.substring(start, end);

			StringBuilder b = new StringBuilder();
			for (char c : subrange.toCharArray()) {
				if (Character.isDigit(c))
					b.append(c);
			}

			int row = Integer.parseInt(b.toString()) - 1;

			for (int i = 0; i < values.size(); i++) {
				placeInCache(sheet, row + i, values.get(i));
			}

			return row;

		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 403) {
				Platform.runLater(() -> Util.createAlert(AlertType.ERROR, "Access Denied", "Access Denied",
						"You don't have the valid credentials to modify the Point Entry database"));
			} else {
				Util.showErrorDialog(e);
			}
		} catch (IOException e) {
			Main.pushNotification("Update Failed");
			Util.showErrorDialog(e);
		}

		return -1;
	}

	public int doAppendRow(int sheet, List<Object> values) {
		return doAppend(sheet, Arrays.asList(values));
	}

	public int doAppendCell(int sheet, int column, String value) {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i <= column; i++)
			list.add("");

		list.add(value);

		return doAppendRow(sheet, list);
	}

	public void placeInCache(int sheet, int row, int column, String value) {
		List<List<Object>> tab = cache.get(sheet);
		if (tab == null)
			throw new IllegalArgumentException("Unknown tab: " + sheet);

		List<Object> r = null;
		if (row >= tab.size()) {

			while (row > tab.size()) {
				tab.add(new ArrayList<>());
			}

			r = new ArrayList<>();
			tab.add(r);
		} else {
			r = tab.get(row);
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

	public void placeInCache(int sheet, int row, List<Object> values) {
		List<List<Object>> tab = cache.get(sheet);
		if (tab == null)
			throw new IllegalArgumentException("Unknown tab: " + sheet);

		List<Object> cachedList = null;
		if (row >= tab.size()) {

			while (row > tab.size()) {
				tab.add(new ArrayList<>());
			}

			cachedList = new ArrayList<>();
			tab.add(cachedList);
		} else {
			cachedList = tab.get(row);
		}

		for (int i = 0; i < values.size(); i++) {
			// Ignore formulae since it's very rare to need the formula text but rather the
			// result value. You can force inserting formula string by placeInCache(int, int, int, String);
			
			if (values.get(i).toString().startsWith("="))
				values.set(i, "");
		}

		cachedList.clear();
		cachedList.addAll(values);
	}

	@Override
	public void searchEntries(String q, ObservableList<? super Entry> consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
			return;

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		query = q;
		entryList = consumer;
		this.matcher = matcher;
		this.columns = columns;
		searchType = ENTRY_LIST;
		scanType = SCAN_CELLS;

		search.restart();
	}

	@Override
	public void searchEntries(ObservableList<? super Entry> consumer, Predicate<List<String>> tester) {
		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		entryList = consumer;
		this.tester = tester;
		searchType = ENTRY_LIST;
		scanType = SCAN_ROWS;

		search.restart();
	}

	@Override
	public List<Entry> getEntries(String query, ColumnMatcher matcher, int... columns) {
		List<Entry> list = new ArrayList<>();

		int size = Math.min(cache.size(), Tab.cities().size());
		for (int i = 0; i < size; i++) {
			for (int j = 1; j < cache.get(i).size(); j++) {
				if (Search.matches(this, i, j, query, matcher, columns))
					list.add(new Entry(this, i, j));
			}
		}

		return list;
	}

	@Override
	public List<Entry> getEntries(Predicate<List<String>> tester) {
		List<Entry> list = new ArrayList<>();

		int size = Math.min(cache.size(), Tab.cities().size());
		for (int i = 0; i < size; i++) {
			for (int j = 1; j < cache.get(i).size(); j++) {
				if (Search.matches(this, i, j, tester))
					list.add(new Entry(this, i, j));
			}
		}

		return list;
	}

	@Override
	public void findEntry(String q, WritableValue<? super Entry> consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
			return;

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		query = q;
		entryRef = consumer;
		this.matcher = matcher;
		this.columns = columns;
		searchType = ENTRY;
		scanType = SCAN_CELLS;

		search.restart();
	}

	@Override
	public void findEntry(WritableValue<? super Entry> consumer, Predicate<List<String>> tester) {
		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		entryRef = consumer;
		this.tester = tester;
		searchType = ENTRY;
		scanType = SCAN_ROWS;

		search.restart();
	}

	@Override
	public Entry getEntry(String query, ColumnMatcher matcher, int... columns) {
		int size = Math.min(cache.size(), Tab.cities().size());
		for (int i = 0; i < size; i++) {
			for (int j = 1; j < cache.get(i).size(); j++) {
				if (Search.matches(this, i, j, query, matcher, columns))
					return new Entry(this, i, j);
			}
		}

		return null;
	}

	@Override
	public Entry getEntry(Predicate<List<String>> tester) {
		int size = Math.min(cache.size(), Tab.cities().size());
		for (int i = 0; i < size; i++) {
			for (int j = 1; j < cache.get(i).size(); j++) {
				if (Search.matches(this, i, j, tester))
					return new Entry(this, i, j);
			}
		}

		return null;
	}

	@Override
	public void searchRows(int tab, String q, ObservableList<Integer> consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
			return;

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		query = q;
		searchTab = tab;
		rowList = consumer;
		this.matcher = matcher;
		this.columns = columns;
		searchType = ROW_LIST;
		scanType = SCAN_CELLS;

		search.restart();

	}

	@Override
	public void searchRows(int tab, ObservableList<Integer> consumer, Predicate<List<String>> tester) {
		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		searchTab = tab;
		rowList = consumer;
		this.tester = tester;
		searchType = ROW_LIST;
		scanType = SCAN_ROWS;

		search.restart();

	}

	@Override
	public List<Integer> getRows(int tab, String query, ColumnMatcher matcher, int... columns) {
		List<Integer> list = new ArrayList<>();

		for (int row = 1; row < cache.get(tab).size(); row++) {
			if (Search.matches(this, tab, row, query, matcher, columns)) {
				list.add(row);
			}
		}

		return list;
	}

	@Override
	public List<Integer> getRows(int tab, Predicate<List<String>> tester) {
		List<Integer> list = new ArrayList<>();

		for (int row = 1; row < cache.get(tab).size(); row++) {
			if (Search.matches(this, tab, row, tester)) {
				list.add(row);
			}
		}

		return list;
	}

	@Override
	public void findRow(int tab, String q, WritableIntegerValue consumer, ColumnMatcher matcher, int... columns) {
		if (q == null || q.isEmpty())
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
		searchType = ROW;
		scanType = SCAN_CELLS;

		search.restart();

	}

	@Override
	public void findRow(int tab, WritableIntegerValue consumer, Predicate<List<String>> tester) {

		if (load.isRunning())
			return;

		if (search.isRunning())
			search.cancel();

		searchTab = tab;
		consumerRow = consumer;
		this.tester = tester;
		searchType = ROW;
		scanType = SCAN_ROWS;

		search.restart();
	}

	public int getRow(int tab, String q, ColumnMatcher matcher, int... columns) {
		for (int row = 1; row < cache.get(tab).size(); row++) {
			if (Search.matches(this, tab, row, q, matcher, columns)) {
				return row;
			}
		}

		return -1;
	}

	@Override
	public int getRow(int tab, Predicate<List<String>> tester) {
		for (int row = 1; row < cache.get(tab).size(); row++) {
			if (Search.matches(this, tab, row, tester)) {
				return row;
			}
		}

		return -1;
	}

	public void reload() {
		loadService().restart();
	}

	public void reloadTab(int tab) throws IOException {
		String name = tabs.get(tab);
		if (name == null)
			throw new IndexOutOfBoundsException("" + tab);

		for (int i = 0; i < tabs.size(); i++) {
			List<List<Object>> doc = doLoadRange(tabs.get(tab) + "!A:ZZ");
			cache.put(tab, doc);
		}

	}

	@Override
	public Service<Void> searchService() {
		if (search == null) {
			search = new Service<Void>() {

				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {

						private List<? super Entry> eList = entryList;
						private WritableValue<? super Entry> eRef = entryRef;
						private ObservableList<Integer> rList = rowList;
						private WritableIntegerValue rowInTab = consumerRow;
						private int sTab = searchTab;
						private Predicate<List<String>> test = tester;
						private SearchType searchT = searchType;
						private ScanType scanT = scanType;

						@Override
						protected Void call() throws Exception {

							String q = query;
							ColumnMatcher m = matcher;
							int[] c = columns;

							if (searchT == ENTRY_LIST || searchT == ENTRY) {
								if (searchT == ENTRY_LIST)
									Platform.runLater(() -> eList.clear());

								int size = Math.min(cache.size(), Tab.cities().size());

								outer: for (int i = 0; i < size; i++) {
									for (int j = 1; j < cache.get(i).size(); j++) {
										if (isCancelled())
											break outer;

										if ((scanT == SCAN_CELLS
												&& Search.matches(GoogleSpreadsheet.this, i, j, q, m, c))
												|| scanT == SCAN_ROWS
														&& Search.matches(GoogleSpreadsheet.this, i, j, test)) {
											found(i, j);

											if (searchT == ENTRY)
												break outer;
										}
									}
								}
							} else if (searchT == ROW_LIST || searchT == ROW) {
								if (searchT == ROW_LIST)
									Platform.runLater(() -> rList.clear());

								for (int j = 1; j < cache.get(sTab).size(); j++) {
									if (isCancelled())
										break;

									if ((scanT == SCAN_CELLS
											&& Search.matches(GoogleSpreadsheet.this, sTab, j, q, m, c))
											|| scanT == SCAN_ROWS
													&& Search.matches(GoogleSpreadsheet.this, sTab, j, test)) {

										int localJ = j;
										if (searchT == ROW_LIST) {
											Platform.runLater(() -> rList.add(localJ));
										} else if (searchT == ROW) {
											Platform.runLater(() -> rowInTab.set(localJ));
											break;
										}
									}
								}

							}

							return null;
						}

						private void found(int sheet, int row) {
							Entry e = new Entry(GoogleSpreadsheet.this, sheet, row);

							Platform.runLater(() -> {
								if (searchT == ENTRY_LIST)
									eList.add(e);
								else
									eRef.setValue(e);
							});
						}
					};
				}

			};

			search.setOnFailed(evt -> {
				Main.pushNotification("Search Failed");
				Util.showErrorDialog(search.getException());
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
				protected void succeeded() {
					// TODO populate cities
				}

				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							List<String> batch = new ArrayList<>();
							for (String tab : tabs) {
								batch.add(tab + "!A:ZZ");
							}

							List<List<List<Object>>> response = doLoadRangeBatch(batch);

							for (int i = 0; i < response.size(); i++) {
								List<List<Object>> tab = response.get(i);
								cache.put(i, tab);
							}

							return null;
						}
					};
				}
			};

			load.setOnFailed(evt -> {
				Throwable e = load.getException();

				Main.pushNotification("Load Failed");
				Util.showErrorDialog(e);
			});
		}

		return load;
	}

	private int parseTab(String range) {
		range = range.replace("'", "");

		String tab = range.substring(0, range.indexOf("!"));
		return tabs.indexOf(tab);
	}

	private int assumingValidId(int tab, int row) {
		int checkRow = row;
		String id;

		while (true) {
			try {
				do {
					checkRow--;
					id = getCellValue(tab, checkRow, Column.ID_NUMBER.ordinal());
				} while (id.isEmpty());

				int diff = row - checkRow;

				return Integer.parseInt(id) + diff;
			} catch (NumberFormatException e) {
			}
		}
	}

	private static class Update {
		private String range;
		private List<List<Object>> change;
		private boolean append;
		private Consumer<Integer> callback;

		Update(String range, List<List<Object>> change, boolean append, Consumer<Integer> callback) {
			this.range = range;
			this.change = change;
			this.append = append;
			this.callback = callback;
		}

		Update(String range, String change, boolean append, Consumer<Integer> callback) {
			this(range, Arrays.asList(Arrays.asList(change)), append, callback);
		}
	}
}
