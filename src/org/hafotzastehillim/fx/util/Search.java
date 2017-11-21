package org.hafotzastehillim.fx.util;

import static org.hafotzastehillim.fx.spreadsheet.Column.*;

import java.util.List;
import java.util.function.Predicate;

import org.hafotzastehillim.fx.spreadsheet.ColumnMatcher;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;

public class Search {

	private Search() {
	}

	public static boolean matches(Spreadsheet sheet, int tab, int row, String query, ColumnMatcher matcher,
			int... columns) {

		List<String> data = sheet.getRow(tab, row);
		if(!data.stream().filter(d -> !d.isEmpty()).findAny().isPresent())
			return false;
		
		for (int c : columns) {
			if (data.size() <= c)
				continue;

			String value = data.get(c);
			if (matcher.matches(query, value, c))
				return true;
		}

		return false;
	}

	public static boolean matches(Spreadsheet sheet, int tab, int row, Predicate<List<String>> tester) {
		List<String> data = sheet.getRow(tab, row);
		
		if(!data.stream().filter(d -> !d.isEmpty()).findAny().isPresent())
			return false;
		
		while (TOTAL_POINTS.ordinal() > data.size())
			data.add("");

		return tester.test(data);
	}

	public static ColumnMatcher getMatcher() {
		return new ColumnMatcher() {

			private String addressNumber;

			@Override
			public boolean matches(String query, String value, int col) {
				if (col == ACCOUNT_NUMBER.ordinal() || col == ID_NUMBER.ordinal()) {
					return !value.isEmpty() && value.equals(query);
				}
				if (col == PHONE.ordinal() || col == CELL_PHONE.ordinal()) {
					return query.length() >= 4 && value.endsWith(query);
				}
				if (col == ADDRESS_NUMBER.ordinal()) {
					if (query.length() >= value.length())
						addressNumber = value;
					else
						addressNumber = "";
					
					return false;
				}
				if (col == ADDRESS_NAME.ordinal()) {
					if(query.length() < 4)
						return false;
					
					value = value.toLowerCase();
					return value.startsWith(query) || (addressNumber + value).startsWith(query);
				}
				if (col == LAST_NAME.ordinal()) {
					return value.toLowerCase().startsWith(query);
				}
				if (col == LAST_NAME_YIDDISH.ordinal()) {
					return value.startsWith(query);
				}

				return false;
			}

		};

	}

	private static final int[] searchableColumns = new int[] { PHONE.ordinal(), CELL_PHONE.ordinal(),
			ACCOUNT_NUMBER.ordinal(), ID_NUMBER.ordinal(), ADDRESS_NUMBER.ordinal(), ADDRESS_NAME.ordinal(),
			LAST_NAME.ordinal(), LAST_NAME_YIDDISH.ordinal() };

	public static int[] getColumns() {
		return searchableColumns;
	}
}
