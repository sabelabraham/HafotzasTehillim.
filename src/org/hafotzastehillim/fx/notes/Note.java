package org.hafotzastehillim.fx.notes;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.hafotzastehillim.fx.Model;
import org.hafotzastehillim.fx.spreadsheet.Selectable;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.spreadsheet.Tab;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Note implements Selectable, Comparable<Note> {

	public static final int PHONE_COLUMN = 0;
	public static final int CREATED_COLUMN = 1;
	public static final int MODIFIED_COLUMN = 2;
	public static final int ALARM_COLUMN = 1;
	public static final int NOTE_COLUMN = 2;

	private StringProperty phone;
	private StringProperty note;
	private ObjectProperty<Instant> alarm;

	private BooleanProperty selected;
	private ReadOnlyBooleanWrapper changed;
	private BooleanProperty hidden;

	private Spreadsheet sheet;
	private int row = -1;

	public static Note withNumberAndAlarm(String phone, Instant alarm, String note) {
		Note n = new Note();
		n.sheet = Model.getInstance().getSpreadsheet();
		n.setPhone(phone);
		n.setAlarm(alarm);
		n.setNote(note);

		n.persist();

		return n;
	}

	public static Note withNumber(String phone, String note) {
		return withNumberAndAlarm(phone, null, note);
	}

	public static Note withAlarm(Instant alarm, String note) {
		return withNumberAndAlarm(null, null, note);
	}

	public static Note create(String note) {
		return withNumberAndAlarm(null, null, note);
	}

	Note(Spreadsheet sheet, int row) {
		this();
		this.sheet = sheet;
		this.row = row;
		reload();
	}

	Note() {
		changed = new ReadOnlyBooleanWrapper(this, "changed", false);
	}

	public StringProperty phoneProperty() {
		if (phone == null) {
			phone = new SimpleStringProperty(this, "phone");
			phone.addListener((obs, ov, nv) -> setChanged(true));
		}
		return phone;
	}

	public final String getPhone() {
		return phoneProperty().get();
	}

	public final void setPhone(final String phone) {
		phoneProperty().set(phone);
	}

	public StringProperty noteProperty() {
		if (note == null) {
			note = new SimpleStringProperty(this, "note");
			note.addListener((obs, ov, nv) -> setChanged(true));
		}
		return note;
	}

	public final String getNote() {
		return noteProperty().get();
	}

	public final void setNote(final String note) {
		noteProperty().set(note);
	}

	public ObjectProperty<Instant> alarmProperty() {
		if (alarm == null) {
			alarm = new SimpleObjectProperty<>(this, "alarm");
			alarm.addListener((obs, ov, nv) -> setChanged(true));
		}

		return alarm;
	}

	public final Instant getAlarm() {
		return alarmProperty().get();
	}

	public final void setAlarm(final Instant alarm) {
		alarmProperty().set(alarm);
	}

	@Override
	public BooleanProperty selectedProperty() {
		if (selected == null) {
			selected = new SimpleBooleanProperty(this, "selected", false);
		}

		return selected;
	}

	public ReadOnlyBooleanProperty changedProperty() {
		return changed.getReadOnlyProperty();
	}

	public final boolean isChanged() {
		return changedProperty().get();
	}

	public final void setChanged(boolean bool) {
		changed.set(bool);
	}

	public Spreadsheet getSheet() {
		return sheet;
	}

	void setSheet(Spreadsheet sheet) {
		this.sheet = sheet;
	}

	public int getRow() {
		return row;
	}

	void setRow(int row) {
		this.row = row;
	}

	public boolean isDue() {
		if (getAlarm() == null)
			return false;

		Instant now = Instant.now();
		if (getAlarm().isBefore(now) || getAlarm().equals(now))
			return true;

		return false;
	}

	public long millisToDue() {
		if (getAlarm() == null)
			return -1L;

		if (isDue())
			return 0L;

		return Duration.between(Instant.now(), getAlarm()).toMillis();
	}

	void persist() {
		if (row >= 0) {
			throw new IllegalStateException("Already persisted.");
		}

		sheet.addRow(Tab.NOTES.ordinal(), getData(), row -> this.row = row);
		setChanged(false);

		NoteManager.getInstance().addNote(this);
	}

	public void save() {
		if (row < 0) {
			throw new IllegalStateException("Row not set.");
		}

		sheet.updateRow(Tab.NOTES.ordinal(), row, getData());
		setChanged(false);
	}

	private static final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

	private List<String> getData() {
		List<String> data = new ArrayList<>();

		data.add(getPhone().replaceAll("[^\\d]", ""));
		data.add(getAlarm() == null ? "" : getAlarm().toString());
		data.add(getNote());

		return data;
	}

	public void reload() {
		if (row < 0) {
			throw new IllegalStateException("Row not set.");
		}

		List<String> data = sheet.getRow(Tab.NOTES.ordinal(), row);

		try {
			setPhone(data.get(PHONE_COLUMN).isEmpty() ? ""
					: util.format(util.parse(data.get(PHONE_COLUMN), "US"), PhoneNumberFormat.NATIONAL));
		} catch (NumberParseException e) {
			e.printStackTrace();
		}

		String alarm = data.get(ALARM_COLUMN);
		if (!alarm.isEmpty()) {
			try {
				setAlarm(Instant.parse(alarm));
			} catch (DateTimeParseException e) {
				e.printStackTrace();
			}
		}

		setNote(data.get(NOTE_COLUMN));
		setChanged(false);
	}

	public void delete() {
		if (row < 0) {
			throw new IllegalStateException("Row not set.");
		}

		setPhone("");
		setAlarm(null);
		setNote("");

		save();

		row = -1;

		NoteManager.getInstance().removeNote(this);
	}

	@Override
	public String toString() {
		return "Note[phone=" + getPhone() + ", alarm=" + getAlarm() + ", note=" + getNote() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAlarm() == null) ? 0 : getAlarm().hashCode());
		result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
		result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		if (getAlarm() == null) {
			if (other.getAlarm() != null)
				return false;
		} else if (!getAlarm().equals(other.getAlarm()))
			return false;
		if (getNote() == null) {
			if (other.getNote() != null)
				return false;
		} else if (!getNote().equals(other.getNote())) {
			return false;
		}
		if (getPhone() == null) {
			if (other.getPhone() != null) {
				return false;
			}
		} else if (!getPhone().equals(other.getPhone()))
			return false;

		return true;
	}

	@Override
	public int compareTo(Note other) {
		if (getAlarm() != null && other.getAlarm() != null)
			return getAlarm().compareTo(other.getAlarm());

		if (getAlarm() != null) // but other is
			return -1;
		if (other.getAlarm() != null)
			return 1;

		// return getNote().compareToIgnoreCase(other.getNote());
		return 0;
	}

	public final BooleanProperty hiddenProperty() {
		if (hidden == null) {
			hidden = new SimpleBooleanProperty(this, "hidden", false);
		}
		return hidden;
	}

	public final boolean isHidden() {
		return hiddenProperty().get();
	}

	public final void setHidden(final boolean hidden) {
		hiddenProperty().set(hidden);
	}

}
