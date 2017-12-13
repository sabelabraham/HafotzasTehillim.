package org.hafotzastehillim.pointentry.fx.notes;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.Model;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.spreadsheet.SheetsAPI;
import org.hafotzastehillim.pointentry.spreadsheet.Tab;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

public class NoteManager {
	private Map<String, Note> noteMap;
	private Timeline timer;

	private ObservableList<Note> notes;
	private ObservableList<Note> dueAlarms;
	private ObservableList<Note> waitingAlarms;

	private ChangeListener<Worker.State> listener;

	private static final Model model = Model.getInstance();

	private NoteManager() {

		noteMap = new HashMap<>();

		notes = FXCollections.observableArrayList();
		dueAlarms = FXCollections.observableArrayList();
		waitingAlarms = FXCollections.observableArrayList();

		timer = new Timeline(new KeyFrame(Duration.minutes(1), evt -> {
			ListIterator<Note> iter = waitingAlarms.listIterator();
			Note n;
			while (iter.hasNext()) {
				if ((n = iter.next()).isDue()) {
					iter.remove();
					if (!dueAlarms.contains(n))
						dueAlarms.add(n);
				}
			}
		}));
		timer.setCycleCount(Animation.INDEFINITE);

		// if delayed manager creation after loading
		if (model.getSpreadsheet() != null
				&& model.getSpreadsheet().loadService().getState() == Worker.State.SUCCEEDED) {
			reloadNotes();
		}

		listener = (obs, ov, nv) -> {
			if (nv == Worker.State.SUCCEEDED) {
				reloadNotes();
			}
		};

		model.spreadsheetProperty().addListener((obs, ov, nv) -> {
			if (ov != null) {
				ov.loadService().stateProperty().removeListener(listener);
			}
			if (nv != null) {
				nv.loadService().stateProperty().addListener(listener);
			}

		});
	}

	public Note getNote(String phone) {
		return noteMap.get(phone);
	}

	private ObservableList<Note> readOnlyDueAlarms;

	public ObservableList<Note> dueAlarms() {
		if (readOnlyDueAlarms == null) {
			readOnlyDueAlarms = FXCollections.unmodifiableObservableList(dueAlarms);
		}

		return readOnlyDueAlarms;
	}

	private ObservableList<Note> readOnlyWaitingAlarms;

	public ObservableList<Note> waitingAlarms() {
		if (readOnlyWaitingAlarms == null) {
			readOnlyWaitingAlarms = FXCollections.unmodifiableObservableList(waitingAlarms);
		}

		return readOnlyWaitingAlarms;
	}

	private ObservableList<Note> readOnlyNotes;

	public ObservableList<Note> notes() {
		if (readOnlyNotes == null) {
			readOnlyNotes = FXCollections.unmodifiableObservableList(notes);
		}

		return readOnlyNotes;
	}

	void addNote(Note n) {
		if (n.getPhone() != null) {
			if (noteMap.get(n.getPhone()) != null)
				throw new IllegalStateException("Duplicate note for phone: " + n.getPhone() + ".");
			noteMap.put(n.getPhone(), n);
		}

		notes.add(n);

		long millisToDue = n.millisToDue();
		if (millisToDue == 0)
			dueAlarms.add(n);
		else if (millisToDue > 0)
			waitingAlarms.add(n);

		n.alarmProperty().addListener((obs, ov, nv) -> {
			if (nv == null) {
				dueAlarms.remove(n);
				waitingAlarms.remove(n);
				return;
			}

			long due = n.millisToDue();
			if (due == 0) {
				if (!dueAlarms.contains(n))
					dueAlarms.add(n);
				waitingAlarms.remove(n);
			} else if (due > 0) {
				dueAlarms.remove(n);
				if (!waitingAlarms.contains(n))
					waitingAlarms.add(n);
			}
		});

		n.phoneProperty().addListener((obs, ov, nv) -> {
			noteMap.remove(ov);

			if (nv != null) {
				noteMap.put(nv, n);
			}
		});
	}

	void removeNote(Note n) {
		dueAlarms.remove(n);
		waitingAlarms.remove(n);
		notes.remove(n);

		if (noteMap.get(n.getPhone()) == n) {
			noteMap.remove(n.getPhone());
		}
	}

	public void reloadNotes() {
		notes.forEach(n -> n.setRow(-1));

		noteMap.clear();
		notes.clear();
		dueAlarms.clear();
		waitingAlarms.clear();

		List<Integer> rows = model.getSpreadsheet().getRows(Tab.NOTES.ordinal(),
				data -> data.size() >= Note.EMAIL_COLUMN && !data.get(Note.NOTE_COLUMN).isEmpty()
						&& data.get(Note.EMAIL_COLUMN).equals(SheetsAPI.getUserInfo().getEmail()));

		rows.stream().map(i -> new Note(model.getSpreadsheet(), i)).forEach(n -> addNote(n));

		timer.playFromStart();
	}

	public static NoteManager getInstance() {
		return InstanceHandler.INSTANCE;
	}

	private static final class InstanceHandler {
		private static final NoteManager INSTANCE = new NoteManager();
	}

}
