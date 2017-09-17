package org.hafotzastehillim.spreadsheet;

public class Search {

	private Search() {
	}

	// FIXME completely migrate to ColumnMatcher idea
	public static boolean matches(Spreadsheet sheet, int sheetIndex, int row, String query) {

		String phone = sheet.getCellValue(sheetIndex, row, Column.PHONE.getColumn());
		String addressName = sheet.getCellValue(sheetIndex, row, Column.ADDRESS_NAME.getColumn()).toLowerCase();
		String addressFull = sheet.getCellValue(sheetIndex, row, Column.ADDRESS_NUMBER.getColumn()) + addressName;
		String lastName = sheet.getCellValue(sheetIndex, row, Column.LAST_NAME.getColumn()).toLowerCase();
		String yiddishLastName =  sheet.getCellValue(sheetIndex, row, Column.LAST_NAME_YIDDISH.getColumn());

		return phone.endsWith(query) || addressName.startsWith(query)
				|| addressFull.startsWith(query)
				|| lastName.startsWith(query)
				|| yiddishLastName.startsWith(query);
	}
}
