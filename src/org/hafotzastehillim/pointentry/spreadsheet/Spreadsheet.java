package org.hafotzastehillim.pointentry.spreadsheet;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;

public interface Spreadsheet {

	String getCellValue(int tab, int row, int column);

	List<String> getRow(int tab, int row);

	void setCellValue(int tab, int row, int column, String value, Consumer<Boolean> callback);

	default void setCellValue(int tab, int row, int column, String value) {
		setCellValue(tab, row, column, value, null);
	}
//
//	void setCellValue(int tab, int row, int column, double value, Consumer<Boolean> callback);
//
//	default void setCellValue(int tab, int row, int column, double value) {
//		setCellValue(tab, row, column, value, null);
//	}

	void updateRow(int tab, int row, List<String> data, Consumer<Boolean> callback);

	default void updateRow(int tab, int row, List<String> data) {
		updateRow(tab, row, data, null);
	}

	void addRow(int tab, List<String> data, Consumer<Integer> callback);

	default void addRow(int tab, List<String> data) {
		addRow(tab, data, null);
	}

	void persist(Entry e, Consumer<Integer> callback);

	void searchEntries(String query, ObservableList<? super Entry> consumer, ColumnMatcher matcher, int... columns);

	void searchEntries(ObservableList<? super Entry> consumer, Predicate<List<String>> tester);

	List<Entry> getEntries(String query, ColumnMatcher matcher, int... columns);

	List<Entry> getEntries(Predicate<List<String>> tester);

	void findEntry(String query, WritableValue<? super Entry> consumer, ColumnMatcher matcher, int... columns);

	void findEntry(WritableValue<? super Entry> consumer, Predicate<List<String>> tester);

	Entry getEntry(String query, ColumnMatcher matcher, int... columns);

	Entry getEntry(Predicate<List<String>> tester);

	void searchRows(int tab, String query, ObservableList<Integer> consumer, ColumnMatcher matcher, int... columns);

	void searchRows(int tab, ObservableList<Integer> consumer, Predicate<List<String>> tester);

	List<Integer> getRows(int tab, String query, ColumnMatcher matcher, int... columns);

	List<Integer> getRows(int tab, Predicate<List<String>> tester);

	void findRow(int tab, String query, WritableIntegerValue consumer, ColumnMatcher matcher, int... columns);

	void findRow(int tab, WritableIntegerValue consumer, Predicate<List<String>> tester);

	int getRow(int tab, String query, ColumnMatcher matcher, int... columns);

	int getRow(int tab, Predicate<List<String>> tester);

	Service<Void> searchService();

	void reload();

	Service<Void> loadService();
}
