package org.hafotzastehillim.spreadsheet;

public interface ColumnMatcher {

	boolean matches(String query, String value, Column col);
}
