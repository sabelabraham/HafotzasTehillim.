package org.hafotzastehillim.fx;

import org.hafotzastehillim.spreadsheet.Entry;
import org.hafotzastehillim.spreadsheet.Spreadsheet;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Model {

	private final ObjectProperty<Spreadsheet> spreadsheet;
	private final ObjectProperty<Entry> currentEntry;

	private Model() {
		spreadsheet = new SimpleObjectProperty<>(this, "spreadsheet");
		currentEntry = new SimpleObjectProperty<>(this, "currentEntry");
	}

	public ObjectProperty<Spreadsheet> spreadsheetProperty() {
		return spreadsheet;
	}

	public final Spreadsheet getSpreadsheet() {
		return spreadsheetProperty().get();
	}

	public final void setSpreadsheet(Spreadsheet sheet) {
		spreadsheetProperty().set(sheet);
	}

	public final Entry getCurrentEntry() {
		return currentEntryProperty().get();
	}

	public final void setCurrentEntry(Entry e) {
		currentEntryProperty().set(e);
	}

	public ObjectProperty<Entry> currentEntryProperty() {
		return currentEntry;
	}

	private static final Model INSTANCE = new Model();

	public static Model getInstance() {
		return INSTANCE;
	}

}
