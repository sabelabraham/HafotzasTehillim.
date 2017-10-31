package org.hafotzastehillim.fx.spreadsheet;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Range {

	private String tab;
	private int startRow;
	private int endRow;
	private int startCol;
	private int endCol;

	private Range() {
	}

	public static Range parse(String str) {
		Range range = new Range();
		int tabDelimiter = str.indexOf('!');
		if (tabDelimiter < 0) {
			throw new ParseException("Missing \"!\" character.");
		}

		String t = str.substring(0, tabDelimiter);
		if (t.isEmpty()) {
			throw new ParseException("Missing tab data.");
		}

		range.tab = t.replace("'", "");

		int rangeDelimiter = str.indexOf(':');

		String rangeFrom = str.substring(tabDelimiter + 1, rangeDelimiter < 0 ? str.length() : rangeDelimiter);
		String rangeTo = str.substring(rangeDelimiter < 0 ? tabDelimiter + 1 : rangeDelimiter + 1, str.length());

		range.startRow = parseRow(rangeFrom);
		range.startCol = parseCol(rangeFrom);

		range.endRow = parseRow(rangeTo);
		range.endCol = parseCol(rangeTo);

		if (range.startRow > range.endRow) {
			throw new ParseException("End row is less than start row.");
		}
		if (range.startCol > range.endCol) {
			throw new ParseException("End column is less than start column.");
		}

		return range;
	}

	public static Range ofRange(String tab, int startColumn, int startRow, int endColumn, int endRow) {
		if (tab.isEmpty()) {
			throw new IllegalArgumentException("Tab is empty.");
		}

		if (startRow > endRow) {
			throw new IllegalArgumentException("End row is less than start row.");
		}
		if (startColumn > endColumn) {
			throw new ParseException("End column is less than start column.");
		}

		if (startRow < 0) {
			throw new IllegalArgumentException("Start row is negative.");
		}
		if (startColumn < 0) {
			throw new IllegalArgumentException("Start column is negative.");
		}

		Range r = new Range();
		r.tab = tab;
		r.startRow = startRow;
		r.startCol = startColumn;
		r.endRow = endRow;
		r.endCol = endColumn;

		return r;
	}

	public static Range ofCell(String tab, int column, int row) {
		return ofRange(tab, column, row, column, row);
	}

	public String getTab() {
		return tab;
	}

	public int getStartRow() {
		return startRow;
	}

	public int getEndRow() {
		return endRow;
	}

	public int getStartColumn() {
		return startCol;
	}

	public int getEndColumn() {
		return endCol;
	}

	public boolean isCell() {
		return getStartRow() == getEndRow() && getStartColumn() == getEndColumn();
	}

	@Override
	public String toString() {
		return tab + "!" + Column.toName(getStartColumn()) + (getStartRow() + 1)
				+ (isCell() ? "" : ":" + Column.toName(getEndColumn()) + (getEndRow() + 1));
	}

	private static int parseRow(String str) {
		if (!Pattern.matches("[A-Za-z]+\\d+", str)) {
			throw new ParseException("Invalid format: " + str);
		}

		String nums = str.chars().mapToObj(c -> String.valueOf((char) c)).filter(c -> Character.isDigit(c.charAt(0)))
				.collect(Collectors.joining());

		return Integer.parseInt(nums) - 1;
	}

	private static int parseCol(String str) {
		if (!Pattern.matches("[A-Za-z]+\\d+", str)) {
			throw new ParseException("Invalid format: " + str);
		}

		String col = str.chars().mapToObj(c -> String.valueOf((char) c)).filter(c -> !Character.isDigit(c.charAt(0)))
				.collect(Collectors.joining());

		return Column.toNumber(col);
	}

	public static class ParseException extends RuntimeException {
		private static final long serialVersionUID = -1500152723505365996L;

		public ParseException(String msg) {
			super(msg);
		}

		public ParseException() {

		}
	}
}
