package org.hafotzastehillim.spreadsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tab {

	WILLIAMSBURG("וויליאמסבורג"), BORO_PARK("בארא פארק"), NEW_SQUARE("סקווירא"), MONROE("קרית יואל"), MONSEY(
			"מאנסי"), OTHER_CITIES("others"), SHAVUOS("שבועות");

	private String name;

	Tab(String name) {
		this.name = name;
	}

	public static Tab getTab(String str) {
		switch (str) {
		case "וויליאמסבורג":
			return WILLIAMSBURG;
		case "בארא פארק":
			return BORO_PARK;
		case "סקווירא":
			return NEW_SQUARE;
		case "קרית יואל":
			return MONROE;
		case "מאנסי":
			return MONSEY;
		case "others":
			return OTHER_CITIES;
		case "שבועות":
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
