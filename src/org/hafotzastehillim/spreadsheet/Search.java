package org.hafotzastehillim.spreadsheet;

import java.util.List;
import static org.hafotzastehillim.spreadsheet.Column.*;

public class Search {

	private Search() {
	}

	public static boolean matches(Spreadsheet sheet, int tab, int row, String query, ColumnMatcher matcher,
			Column... columns) {
		//
		// String phone = sheet.getCellValue(sheetIndex, row, Column.PHONE.getColumn());
		// String addressName = sheet.getCellValue(sheetIndex, row,
		// Column.ADDRESS_NAME.getColumn()).toLowerCase();
		// String addressFull = sheet.getCellValue(sheetIndex, row,
		// Column.ADDRESS_NUMBER.getColumn()) + addressName;
		// String lastName = sheet.getCellValue(sheetIndex, row,
		// Column.LAST_NAME.getColumn()).toLowerCase();
		// String yiddishLastName = sheet.getCellValue(sheetIndex, row,
		// Column.LAST_NAME_YIDDISH.getColumn());
		//
		// return phone.endsWith(query) || addressName.startsWith(query)
		// || addressFull.startsWith(query)
		// || lastName.startsWith(query)
		// || yiddishLastName.startsWith(query);

		List<String> data = sheet.getRow(tab, row);
		for (Column c : columns) {
			if (data.size() <= c.getColumn())
				continue;

			String value = data.get(c.getColumn());
			if (matcher.matches(query, value, c))
				return true;
		}

		return false;
	}

	public static ColumnMatcher getMatcher() {
		return new ColumnMatcher() {

			private String addressNumber;

			@Override
			public boolean matches(String query, String value, Column col) {
				if(col == PHONE) {
					return value.endsWith(query);
				}
				if(col == ADDRESS_NUMBER) {
					addressNumber = value;
					return false;
				}
				if(col == ADDRESS_NAME) {
					value = value.toLowerCase();
					return value.startsWith(query) || (addressNumber + value).startsWith(query);
				}
				if(col == LAST_NAME) {
					return value.toLowerCase().startsWith(query);
				}
				if(col == LAST_NAME_YIDDISH) {
					return value.startsWith(query);
				}

				return false;
			}

		};

	}

	private static final Column[] searchableColumns = new Column[] { PHONE, ADDRESS_NUMBER, ADDRESS_NAME, LAST_NAME,
			LAST_NAME_YIDDISH };

	public static Column[] getColumns() {
		return searchableColumns;
	}
}
