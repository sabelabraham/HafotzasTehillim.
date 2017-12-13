package org.hafotzastehillim.pointentry.fx.notes;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.hafotzastehillim.pointentry.fx.DetailsPane;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.spreadsheet.Column;
import org.hafotzastehillim.pointentry.spreadsheet.Entry;
import org.reactfx.value.Var;
import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

public class NoteCellContent extends StackPane {

	private Var<Note> note;
	private List<Note> list;

	private boolean hideable;
	private boolean selectable;
	private boolean showDue;

	public NoteCellContent(List<Note> list, Note note, boolean hideable, boolean selectable, boolean showDue) {
		this.list = list;
		setNote(note);

		this.hideable = hideable;
		this.selectable = selectable;
		this.showDue = showDue;

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/NoteCellContent.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}

		getStyleClass().add("note-cell-content");
		getStylesheets().add(getClass().getResource("/resources/css/note-cell-content.css").toExternalForm());
	}

	@FXML
	private JFXButton hide;
	@FXML
	private CheckBox select;
	@FXML
	private Label noteText;
	@FXML
	private Label created;
	@FXML
	private Label alarm;
	@FXML
	private Node seperator;
	@FXML
	private Node alarmIcon;

	private static final HebrewDateFormatter HEBREW_FORMATTER = new HebrewDateFormatter();
	static {
		HEBREW_FORMATTER.setHebrewFormat(true);
	}

	@FXML
	private void initialize() {
		noteText.textProperty().bind(note.flatMap(n -> n.noteProperty()).orElseConst(""));

		created.textProperty().bind(note.flatMap(n -> n.createdProperty()).map(created -> {
			JewishCalendar cal = new JewishCalendar(Date.from(created));
			return HEBREW_FORMATTER.format(cal) + "\nיום " + HEBREW_FORMATTER.formatDayOfWeek(cal);
		}).orElseConst(""));

		alarm.textProperty().bind(note.flatMap(n -> n.alarmProperty()).map(alarm -> {
			JewishCalendar cal = new JewishCalendar(Date.from(alarm));
			return HEBREW_FORMATTER.format(cal) + "\nיום " + HEBREW_FORMATTER.formatDayOfWeek(cal);
		}).orElseConst(""));

		alarmIcon.styleProperty().bind(noteProperty().flatMap(v -> v.alarmProperty())
				.map(a -> !a.isAfter(Instant.now()) ? "-fx-fill: red;" : "-fx-fill: aaaaaa;").orElseConst(null));

		alarmIcon.visibleProperty().bind(alarm.textProperty().isNotEmpty().and(new SimpleBooleanProperty(showDue)));
		seperator.visibleProperty().bind(alarmIcon.visibleProperty());

		hide.visibleProperty().bind(noteProperty().map(v -> true && hideable).orElseConst(false));
		select.visibleProperty().bind(noteProperty().map(v -> true && selectable).orElseConst(false));

		if (getNote() != null) {
			select.selectedProperty().bindBidirectional(getNote().selectedProperty());
		}

		noteProperty().addListener((obs, ov, nv) -> {
			if (ov != null) {
				select.selectedProperty().unbindBidirectional(ov.selectedProperty());
			}
			if (nv != null) {
				select.selectedProperty().bindBidirectional(nv.selectedProperty());
			}
		});

		setOnMouseClicked(evt -> {
			if (getNote() == null)
				return;

			if (getNote().getPhone() != null) {
				ObservableList<Entry> list = FXCollections.observableArrayList();
				String phone = getNote().getPhone().replaceAll("[^\\d]", "");

				Util.onceOnSucceeded(getNote().getSheet().searchService().stateProperty(), () -> {

					getNote().setHidden(true);

					Platform.runLater(() -> DetailsPane.showDialog(list));
				});

				getNote().getSheet().searchEntries(list, data -> data.get(Column.PHONE.ordinal()).equals(phone));
			}
		});
	}

	public Var<Note> noteProperty() {
		if (note == null) {
			note = Var.newSimpleVar(null);
		}

		return note;
	}

	public final Note getNote() {
		return noteProperty().getValue();
	}

	public final void setNote(Note note) {
		noteProperty().setValue(note);
	}

	@FXML
	public void hideAction(ActionEvent evt) {
		note.ifPresent(n -> n.setHidden(true));
	}

	@FXML
	public void openAction(ActionEvent evt) {

	}

}
