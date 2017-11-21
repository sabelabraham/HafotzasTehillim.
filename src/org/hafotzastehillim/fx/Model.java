package org.hafotzastehillim.fx;

import java.util.prefs.Preferences;

import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Model {

	private final ObjectProperty<Spreadsheet> spreadsheet;
	private final ObjectProperty<Entry> currentEntry;
	private final ObjectProperty<Stage> currentStage;

	private final ObservableList<Stage> openStages;
	// Settings
	private final IntegerProperty campaignIndex;

	private final BooleanProperty staySignedIn;

	private final BooleanProperty ignoreInvalidPhone;
	private final BooleanProperty ignoreDuplicates;
	private final BooleanProperty ignoreFamilyConflicts;

	private final BooleanProperty silenceAlarms;

	private final StringProperty printBy;
	private final BooleanProperty showPrintDialog;

	private static final Preferences pref = Preferences.userNodeForPackage(Model.class);

	private static final String CAMPAIGN_INDEX_KEY = "CampaignIndex";
	private static final String STAY_SIGNED_IN_KEY = "StaySignedIn";
	private static final String IGNORE_INVALID_PHONE_KEY = "IgnoreInvalidPhone";
	private static final String IGNORE_DUPLICATES_KEY = "IgnoreDuplicates";
	private static final String IGNORE_FAMILY_CONFLICTS_KEY = "IgnoreFamilyConflicts";

	private static final String SILENCE_ALARMS_KEY = "SilenceAlarms";

	private static final String PRINT_BY_KEY = "PrintBy";
	private static final String PRINT_SHOW_DIALOG_KEY = "ShowPrintDialog";

	private Model() {
		spreadsheet = new SimpleObjectProperty<>(this, "spreadsheet");
		currentEntry = new SimpleObjectProperty<>(this, "currentEntry");
		currentStage = new SimpleObjectProperty<>(this, "currentStage");
		openStages = FXCollections.observableArrayList();

		campaignIndex = new SimpleIntegerProperty(this, "campaignIndex", pref.getInt(CAMPAIGN_INDEX_KEY, 1));
		campaignIndex.addListener((obs, ov, nv) -> pref.putInt(CAMPAIGN_INDEX_KEY, nv.intValue()));

		staySignedIn = new SimpleBooleanProperty(this, "staySignedIn", pref.getBoolean(STAY_SIGNED_IN_KEY, true));
		staySignedIn.addListener((obs, ov, nv) -> pref.putBoolean(STAY_SIGNED_IN_KEY, nv));

		ignoreInvalidPhone = new SimpleBooleanProperty(this, "ignoreInvalidPhone",
				pref.getBoolean(IGNORE_INVALID_PHONE_KEY, false));
		ignoreInvalidPhone.addListener((obs, ov, nv) -> pref.putBoolean(IGNORE_INVALID_PHONE_KEY, nv));

		ignoreDuplicates = new SimpleBooleanProperty(this, "ignoreDuplicates",
				pref.getBoolean(IGNORE_DUPLICATES_KEY, false));
		ignoreDuplicates.addListener((obs, ov, nv) -> pref.putBoolean(IGNORE_DUPLICATES_KEY, nv));

		ignoreFamilyConflicts = new SimpleBooleanProperty(this, "ignoreFamilyConflicts",
				pref.getBoolean(IGNORE_FAMILY_CONFLICTS_KEY, false));
		ignoreFamilyConflicts.addListener((obs, ov, nv) -> pref.putBoolean(IGNORE_FAMILY_CONFLICTS_KEY, nv));

		silenceAlarms = new SimpleBooleanProperty(this, "silenceAlarms", pref.getBoolean(SILENCE_ALARMS_KEY, false));
		silenceAlarms.addListener((obs, ov, nv) -> pref.putBoolean(SILENCE_ALARMS_KEY, nv));

		printBy = new SimpleStringProperty(this, "printBy", pref.get(PRINT_BY_KEY, ""));
		printBy.addListener((obs, ov, nv) -> pref.put(PRINT_BY_KEY, nv));

		showPrintDialog = new SimpleBooleanProperty(this, "showPrintDialog",
				pref.getBoolean(PRINT_SHOW_DIALOG_KEY, false));
		showPrintDialog.addListener((obs, ov, nv) -> pref.putBoolean(PRINT_SHOW_DIALOG_KEY, nv));
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

	public final Stage getCurrentStage() {
		return currentStageProperty().get();
	}

	public final void setCurrentStage(Stage s) {
		currentStageProperty().set(s);
	}

	public ObjectProperty<Stage> currentStageProperty() {
		return currentStage;
	}

	public ObservableList<Stage> getOpenStages() {
		return openStages;
	}

	public void registerStage(Stage stage) {
		stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> openStages.add(stage));
		stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> openStages.remove(stage));
		stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				currentStage.set(stage);
			} else {
				currentStage.set(null);
			}
		});
	}

	private static final Model INSTANCE = new Model();

	public static Model getInstance() {
		return INSTANCE;
	}

	public final BooleanProperty staySignedInProperty() {
		return staySignedIn;
	}

	public final boolean isStaySignedIn() {
		return staySignedInProperty().get();
	}

	public final void setStaySignedIn(boolean staySignedIn) {
		staySignedInProperty().set(staySignedIn);
	}

	public final BooleanProperty ignoreDuplicatesProperty() {
		return ignoreDuplicates;
	}

	public final boolean isIgnoreDuplicates() {
		return ignoreDuplicatesProperty().get();
	}

	public final void setIgnoreDuplicates(boolean ignoreDuplicates) {
		ignoreDuplicatesProperty().set(ignoreDuplicates);
	}

	public final BooleanProperty ignoreFamilyConflictsProperty() {
		return ignoreFamilyConflicts;
	}

	public final boolean isIgnoreConflicts() {
		return ignoreFamilyConflictsProperty().get();
	}

	public final void setIgnoreConflicts(boolean ignoreConflicts) {
		ignoreFamilyConflictsProperty().set(ignoreConflicts);
	}

	public final IntegerProperty campaignIndexProperty() {
		return campaignIndex;
	}

	public final int getCampaignIndex() {
		return campaignIndexProperty().get();
	}

	public final void setCampaignIndex(final int campaignIndex) {
		campaignIndexProperty().set(campaignIndex);
	}

	public final BooleanProperty ignoreInvalidPhoneProperty() {
		return ignoreInvalidPhone;
	}

	public final boolean isIgnoreInvalidPhone() {
		return ignoreInvalidPhoneProperty().get();
	}

	public final void setIgnoreInvalidPhone(final boolean ignoreInvalidPhone) {
		ignoreInvalidPhoneProperty().set(ignoreInvalidPhone);
	}

	public BooleanProperty silenceAlarmsProperty() {
		return silenceAlarms;
	}

	public final boolean isSilenceAlarms() {
		return silenceAlarmsProperty().get();
	}

	public final void setSilenceAlarms(final boolean silenceAlarms) {
		silenceAlarmsProperty().set(silenceAlarms);
	}

	public StringProperty printByProperty() {
		return printBy;
	}

	public final String getPrintBy() {
		return printByProperty().get();
	}

	public final void setPrintDialog(String printDialog) {
		printByProperty().set(printDialog);
	}

	public BooleanProperty showPrintDialogProperty() {
		return showPrintDialog;
	}

	public final boolean isShowPrintDialog() {
		return showPrintDialogProperty().get();
	}

	public final void setprintDialog(boolean printDialog) {
		showPrintDialogProperty().set(printDialog);
	}
}
