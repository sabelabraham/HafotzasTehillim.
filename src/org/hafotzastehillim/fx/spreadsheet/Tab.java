package org.hafotzastehillim.fx.spreadsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tab {

	WILLIAMSBURG("\u05d5\u05d5\u05d9\u05dc\u05d9\u05d0\u05de\u05e1\u05d1\u05d5\u05e8\u05d2"), //
	BORO_PARK("\u05d1\u05d0\u05e8\u05d0 \u05e4\u05d0\u05e8\u05e7"), //
	NEW_SQUARE("\u05e1\u05e7\u05d5\u05d5\u05d9\u05e8\u05d0"), //
	MONROE("\u05e7\u05e8\u05d9\u05ea \u05d9\u05d5\u05d0\u05dc"), //
	MONSEY("\u05de\u05d0\u05e0\u05e1\u05d9"), //
	OTHER_CITIES("others"), //
	SHAVUOS("\u05e9\u05d1\u05d5\u05e2\u05d5\u05ea");

	private String name;

	Tab(String name) {
		this.name = name;
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
		default:
			return valueOf(str);
		}
	}

	public String toString() {
		return name;
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

	public static boolean isCity(Tab t) {
		return namedCities.contains(t);
	}

	public static boolean isCity(String name) {
		for (Tab t : namedCities) {
			if (t.toString().equals(name))
				return true;
		}

		return false;
	}

}
