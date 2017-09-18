package org.hafotzastehillim.fx.spreadsheet;

import java.util.List;

public enum Column {

	ID_NUMBER("A"),
	GENDER("B"),
	FIRST_NAME("C"),
	LAST_NAME("D"),
	ADDRESS_NUMBER("E"),
	ADDRESS_NAME("F"),
	APT("G"),
	CITY("H"),
	STATE("I"),
	ZIP("J"),
	CITY_YIDDISH("K"),
	CLASS("L"),
	SCHOOL("M"),
	PHONE("N"),
	FATHER_NAME("O"),
	LAST_NAME_YIDDISH("P"),
	FIRST_NAME_YIDDISH("Q"),
	TOTAL_POINTS("R"),
	FIRST_CAMPAIGN("S");


	public String getData(List<String> data) {
		if(data.size() <= getColumn())
			return "";

		return data.get(getColumn());
	}

	public int getColumn() {
		return ordinal();
	}

	public String getName() {
		return toName(getColumn());
	}

	Column(int column) {
	}

	Column(String name) {
		this(toNumber(name));
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
}
