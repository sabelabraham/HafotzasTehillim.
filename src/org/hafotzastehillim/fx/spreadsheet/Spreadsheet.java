package org.hafotzastehillim.fx.spreadsheet;

import java.util.List;

import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;

public interface Spreadsheet {

	String getCellValue(int tab, int row, int column);

	List<String> getRow(int tab, int row);

	void setCellValue(int tab, int row, int column, String value);

	void setCellValue(int tab, int row, int column, double value);

	void updateRow(int tab, int row, List<String> data);

	int addRow(int tab, List<String> data);

	int uniqueId(int tab);

	void searchEntries(String query, ObservableList<? super Entry> consumer, ColumnMatcher matcher, int... columns);

	void findEntry(String query, WritableValue<? super Entry> consumer,  ColumnMatcher matcher, int... columns);

	void searchRows(int tab, String query,  ObservableList<Integer> consumer, ColumnMatcher matcher, int... columns);

	void findRow(int tab, String query, WritableIntegerValue consumer, ColumnMatcher matcher, int... columns);

	int getRow(int tab, String query, ColumnMatcher matcher, int... columns);

	Service<Void> searchService();

	void reload();

	Service<Void> loadService();

	// default String getCellValue(Entry entry, Column column) {
	// return getCellValue(entry.getTab(), entry.getRow(), column.getColumn());
	// }
	//
	// default List<String> getRow(Entry entry) {
	// return getRow(entry.getTab(), entry.getRow());
	// }

	// default void setRow(Entry entry, List<String> data) {
	// setRow(entry.getTab(), entry.getRow(), data);
	// }
	//
	// default Task<String> getCellValueTask(int tab, int row, int column) {
	// return new Task<String>() {
	// protected String call() {
	// return getCellValue(sheet, row, column);
	// }
	// };
	// }
	//
	// default Task<String> getCellValueTask(Entry entry, ColumnData column) {
	// return getCellValueTask(entry.getSheet(), entry.getRow(),
	// column.getColumn());
	// }
	//
	// default void copyCellValue(int tab, int row, int column, StringProperty
	// property) {
	// Task<String> tsk = getCellValueTask(sheet, row, column);
	// tsk.setOnFailed(evt -> tsk.getException().printStackTrace());
	// tsk.setOnSucceeded(evt -> property.set(tsk.getValue()));
	//
	// new Thread(tsk).start();
	// }
	//
	// default void copyCellValue(Entry entry, ColumnData column, StringProperty
	// property) {
	// copyCellValue(entry.getSheet(), entry.getRow(), column.getColumn(),
	// property);
	// }
	//
	// default void setCellValue(Entry entry, Column column, String value) {
	// setCellValue(entry.getTab(), entry.getRow(), column.getColumn(), value);
	// }
	//
	// default void setCellValue(Entry entry, Column column, double value) {
	// setCellValue(entry.getTab(), entry.getRow(), column.getColumn(), value);
	// }

	// default Task<Void> setCellValueTask(int tab, int row, int column, String
	// value) {
	// return new Task<Void>() {
	// protected Void call() {
	// setCellValue(sheet, row, column, value);
	// return null;
	// }
	// };
	// }
	//
	// default Task<Void> setCellValueTask(Entry entry, ColumnData column, String
	// value) {
	// return setCellValueTask(entry.getSheet(), entry.getRow(), column.getColumn(),
	// value);
	// }
	//
	// default Task<Void> setCellValueTask(int tab, int row, int column, double
	// value) {
	// return new Task<Void>() {
	// protected Void call() {
	// setCellValue(sheet, row, column, value);
	// return null;
	// }
	// };
	// }
	//
	// default Task<Void> setCellValueTask(Entry entry, ColumnData column, double
	// value) {
	// return setCellValueTask(entry.getSheet(), entry.getRow(), column.getColumn(),
	// value);
	// }
}
