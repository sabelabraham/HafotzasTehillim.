package org.hafotzastehillim.pointentry.spreadsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Column {

	ACCOUNT_NUMBER, //
	ID_NUMBER, //
	CREATED, //
	MODIFIED, //
	GENDER, //
	FIRST_NAME, //
	LAST_NAME, //
	ADDRESS_NUMBER, //
	ADDRESS_NAME, //
	APT, //
	CITY, //
	STATE, //
	ZIP, //
	CITY_YIDDISH, //
	CLASS, //
	SCHOOL, //
	CELL_PHONE, //
	PHONE, //
	FATHER_NAME, //
	LAST_NAME_YIDDISH, //
	FIRST_NAME_YIDDISH, //
	TOTAL_POINTS, //
	FIRST_CAMPAIGN;//

	public String getData(List<String> data) {
		if (data.size() <= ordinal())
			return "";

		return data.get(ordinal());
	}

	public String getName() {
		return toName(ordinal());
	}

	public static int toNumber(String name) {
		int number = 0;
		for (int i = 0; i < name.length(); i++) {
			number = number * 26 + (name.charAt(i) - ('A' - 1));
		}
		return number - 1;
	}

	public static String toName(int number) {
		number++;
		StringBuilder sb = new StringBuilder();
		while (number-- > 0) {
			sb.append((char) ('A' + (number % 26)));
			number /= 26;
		}
		return sb.reverse().toString();
	}

	private static final List<Column> details = Collections.unmodifiableList(Arrays.asList(ACCOUNT_NUMBER, ID_NUMBER,
			GENDER, FIRST_NAME, LAST_NAME, ADDRESS_NUMBER, ADDRESS_NAME, APT, CITY, STATE, ZIP, CITY_YIDDISH, CLASS,
			SCHOOL, PHONE, FATHER_NAME, LAST_NAME_YIDDISH, FIRST_NAME_YIDDISH));

	public static List<Column> details() {
		return details;
	}
}
