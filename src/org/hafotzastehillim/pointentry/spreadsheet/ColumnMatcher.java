package org.hafotzastehillim.pointentry.spreadsheet;

public interface ColumnMatcher {

	boolean matches(String query, String value, int col);

	default boolean matches(String query, String value, Column col) {
		return matches(query, value, col.ordinal());
	}
}
