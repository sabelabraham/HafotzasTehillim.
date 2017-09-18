package org.hafotzastehillim.fx.spreadsheet;

import static org.hafotzastehillim.fx.spreadsheet.SearchType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.Main;
import org.hafotzastehillim.fx.util.Search;
import org.hafotzastehillim.fx.util.Util;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Platform;
import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class GoogleSpreadsheet implements Spreadsheet {

	private Sheets service;
	private String sheetId;

	private List<String> tabs;
	private Map<Integer, List<List<Object>>> cache;

	private Service<Void> search;
	private Service<Void> load;

	private LinkedBlockingQueue<Update> updates;
	private Thread updater;

	private volatile String query;
	private volatile ObservableList<? super Entry> consumerList;
	private volatile WritableValue<? super Entry> consumerRef;
	private volatile WritableIntegerValue consumerRow;
	private volatile int searchTab;
	private volatile ColumnMatcher matcher;
	private volatile int[] columns;
	private volatile SearchType type;

	private List<Integer> highestIds;

	public GoogleSpreadsheet(String id, Sheets service) throws IOException {
		sheetId = id;
		this.service = service;

		highestIds = new ArrayList<>();
		cache = new ConcurrentHashMap<>();
		updates = new LinkedBlockingQueue<>();

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

	public void reload() {
		loadService().restart();
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

								int size = Math.min(cache.size(), Tab.cities().size());

								outer: for (int i = 0; i < size; i++) {
									for (int j = 0; j < cache.get(i).size(); j++) {
										if (isCancelled())
											break outer;

										if (Search.matches(GoogleSpreadsheet.this, i, j, q, m, c)) {
											found(i, j);

											if (type == ENTRY)
												break outer;
										}
									}
								}
							} else if (type == ROW_IN_TAB) {
								for (int j = 0; j < cache.get(sTab).size(); j++) {
									if (isCancelled())
										break;

									if (Search.matches(GoogleSpreadsheet.this, sTab, j, q, m, c)) {
										rowInTab.set(j);
										break;
									}
								}

							}

							return null;
						}

						private void found(int sheet, int row) {
							Platform.runLater(() -> {
								Entry e = new Entry(GoogleSpreadsheet.this, sheet, row);

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
												Integer.parseInt(
														tab.get(row).get(Column.ID_NUMBER.getColumn()).toString()),
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
		}

		return load;
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
