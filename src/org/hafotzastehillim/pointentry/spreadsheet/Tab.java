package org.hafotzastehillim.pointentry.spreadsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tab {

	WILLIAMSBURG("\u05d5\u05d5\u05d9\u05dc\u05d9\u05d0\u05de\u05e1\u05d1\u05d5\u05e8\u05d2", "Williamsburg"), //
	BORO_PARK("\u05d1\u05d0\u05e8\u05d0 \u05e4\u05d0\u05e8\u05e7", "Boro Park"), //
	NEW_SQUARE("\u05e1\u05e7\u05d5\u05d5\u05d9\u05e8\u05d0", "New Square"), //
	MONROE("\u05e7\u05e8\u05d9\u05ea \u05d9\u05d5\u05d0\u05dc", "Monroe"), //
	MONSEY("\u05de\u05d0\u05e0\u05e1\u05d9", "Monsey"), //
	OTHER_CITIES("Others"), //
	SHAVUOS("\u05e9\u05d1\u05d5\u05e2\u05d5\u05ea", "Shavuos"), //
	GIFTS("\u05de\u05ea\u05e0\u05d5\u05ea", "Gifts"), //
	NOTES("Notes"), //
	PLEDGES("Pledges"), //
	RECEIPTS("Receipts");
	
	private String name;
	private String prettyName;

	Tab(String name) {
		this.name = name;
		prettyName = name;
	}

	Tab(String name, String prettyName) {
		this.name = name;
		this.prettyName = prettyName;
	}

	public static Tab getTab(String str) {
		switch (str) {
		case "\u05d5\u05d5\u05d9\u05dc\u05d9\u05d0\u05de\u05e1\u05d1\u05d5\u05e8\u05d2":
			return WILLIAMSBURG;
		case "\u05d1\u05d0\u05e8\u05d0 \u05e4\u05d0\u05e8\u05e7":
			return BORO_PARK;
		case "\u05e1\u05e7\u05d5\u05d5\u05d9\u05e8\u05d0":
			return NEW_SQUARE;
		case "\u05e7\u05e8\u05d9\u05ea \u05d9\u05d5\u05d0\u05dc":
			return MONROE;
		case "\u05de\u05d0\u05e0\u05e1\u05d9":
			return MONSEY;
		case "others":
			return OTHER_CITIES;
		case "\u05e9\u05d1\u05d5\u05e2\u05d5\u05ea":
			return SHAVUOS;
		case "\u05de\u05ea\u05e0\u05d5\u05ea":
			return GIFTS;
		default:
			return valueOf(str);
		}
	}

	public String toString() {
		return name;
	}

	public String toPrettyString() {
		return prettyName;
	}

	private static final List<Tab> cities = Collections
			.unmodifiableList(Arrays.asList(WILLIAMSBURG, BORO_PARK, NEW_SQUARE, MONROE, MONSEY, OTHER_CITIES));

	private static final List<Tab> namedCities = Collections
			.unmodifiableList(Arrays.asList(WILLIAMSBURG, BORO_PARK, NEW_SQUARE, MONROE, MONSEY));

	public static List<Tab> cities() {
		return cities;
	}

	public static List<Tab> namedCities() {
		return namedCities;
	}

	public static boolean isNamedCity(Tab t) {
		return namedCities.contains(t);
	}

	public static boolean isNamedCity(String name) {
		for (Tab t : namedCities) {
			if (t.toString().equals(name))
				return true;
		}

		return false;
	}

	public static boolean isCity(Tab t) {
		return cities.contains(t);
	}

	public static boolean isCity(String name) {
		for (Tab t : cities) {
			if (t.toString().equals(name))
				return true;
		}

		return false;
	}

}
