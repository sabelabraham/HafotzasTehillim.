package org.hafotzastehillim.fx.util;

import java.time.LocalDate;
import java.time.chrono.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

public class HebrewChronology extends AbstractChronology {

	@Override
	public String getId() {
		return "Hebrew";
	}

	@Override
	public String getCalendarType() {
		return "hebrew";
	}

	@Override
	public ChronoLocalDate date(int prolepticYear, int month, int dayOfMonth) {
		JewishCalendar cal = new JewishCalendar(prolepticYear, dayOfMonth, dayOfMonth);
		return LocalDate.from(cal.getGregorianCalendar().toInstant());
	}

	@Override
	public ChronoLocalDate dateYearDay(int prolepticYear, int dayOfYear) {
		JewishCalendar cal = new JewishCalendar();
		return LocalDate.from(cal.getGregorianCalendar().toInstant());
	}

	@Override
	public ChronoLocalDate dateEpochDay(long epochDay) {
		return LocalDate.ofEpochDay(epochDay);
	}

	@Override
	public ChronoLocalDate date(TemporalAccessor temporal) {
		return LocalDate.from(temporal);
	}

	@Override
	public boolean isLeapYear(long prolepticYear) {
		return false;
	}

	@Override
	public int prolepticYear(Era era, int yearOfEra) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Era eraOf(int eraValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Era> eras() {
		return HebrewEra.eraList;
	}

	@Override
	public ValueRange range(ChronoField field) {
		return null;
	}

	public static enum HebrewEra implements Era {
		BEFORE_CREATION(0), AFTER_CREATION(1);

		private int value;

		HebrewEra(int value) {
			this.value = value;
		}

		@Override
		public int getValue() {
			return value;
		}

		private static List<Era> eraList = Collections.unmodifiableList(Arrays.asList(BEFORE_CREATION, AFTER_CREATION));
	}

}
