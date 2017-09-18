package org.hafotzastehillim.fx.spreadsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tab {

	WILLIAMSBURG("ווילי�?מסבורג"), BORO_PARK("ב�?ר�? פ�?רק"), NEW_SQUARE("סקוויר�?"), MONROE("קרית יו�?ל"), MONSEY(
			"מ�?נסי"), OTHER_CITIES("others"), SHAVUOS("שבועות");

	private String name;

	Tab(String name) {
		this.name = name;
	}

	public static Tab getTab(String str) {
		switch (str) {
		case "ווילי�?מסבורג":
			return WILLIAMSBURG;
		case "ב�?ר�? פ�?רק":
			return BORO_PARK;
		case "סקוויר�?":
			return NEW_SQUARE;
		case "קרית יו�?ל":
			return MONROE;
		case "מ�?נסי":
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
