package org.hafotzastehillim.fx.util;

import static org.hafotzastehillim.fx.spreadsheet.Column.*;

import java.util.List;

import org.hafotzastehillim.fx.spreadsheet.Column;
import org.hafotzastehillim.fx.spreadsheet.ColumnMatcher;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;

public class Search {

	private Search() {
	}

	public static boolean matches(Spreadsheet sheet, int tab, int row, String query, ColumnMatcher matcher,
			int... columns) {

		List<String> data = sheet.getRow(tab, row);
		for (int c : columns) {
			if (data.size() <= c)
				continue;

			String value = data.get(c);
			if (matcher.matches(query, value, c))
				return true;
		}

		return false;
	}

	public static ColumnMatcher getMatcher() {
		return new ColumnMatcher() {

			private String addressNumber;

			@Override
			public boolean matches(String query, String value, int col) {
				if (col == PHONE.getColumn()) {
					return value.endsWith(query);
				}
				if (col == ADDRESS_NUMBER.getColumn()) {
					addressNumber = value;
					return false;
				}
				if (col == ADDRESS_NAME.getColumn()) {
					value = value.toLowerCase();
					return value.startsWith(query) || (addressNumber + value).startsWith(query);
				}
				if (col == LAST_NAME.getColumn()) {
					return value.toLowerCase().startsWith(query);
				}
				if (col == LAST_NAME_YIDDISH.getColumn()) {
					return value.startsWith(query);
				}

				return false;
			}

		};

	}

	private static final int[] searchableColumns = new int[] { PHONE.getColumn(), ADDRESS_NUMBER.getColumn(),
			ADDRESS_NAME.getColumn(), LAST_NAME.getColumn(), LAST_NAME_YIDDISH.getColumn() };

	public static int[] getColumns() {
		return searchableColumns;
	}
}
