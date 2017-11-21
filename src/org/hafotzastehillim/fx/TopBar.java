package org.hafotzastehillim.fx;

import java.io.IOException;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.notes.Note;
import org.hafotzastehillim.fx.notes.NotificationList;
import org.hafotzastehillim.fx.print.PrintPane;
import org.hafotzastehillim.fx.notes.NoteManager;
import org.hafotzastehillim.fx.notes.NotesList;
import org.hafotzastehillim.fx.spreadsheet.SheetID;
import org.hafotzastehillim.fx.spreadsheet.SheetsAPI;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class TopBar extends HBox {

	public TopBar() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/TopBar.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}

		getStyleClass().add("top-bar");
		getStylesheets().add(getClass().getResource("/resources/css/top-bar.css").toExternalForm());
	}

	@FXML
	private JFXButton report;
	@FXML
	private JFXButton gift;
	@FXML
	private JFXButton notes;
	@FXML
	private JFXButton print;
	@FXML
	private JFXButton settings;
	@FXML
	private JFXButton connection;
	@FXML
	private JFXButton refresh;
	@FXML
	private JFXButton notifications;
	@FXML
	private JFXButton newMember;

	@FXML
	private Label noteAmountText;

	private Refresh refreshGraphics;

	private static final Model model = Model.getInstance();

	@FXML
	public void initialize() throws IOException {
		refreshGraphics = new Refresh();
		refreshGraphics.setAlwaysVisible(true);
		refresh.setGraphic(refreshGraphics);

		Model.getInstance().spreadsheetProperty().addListener((obs, ov, nv) -> {
			detachOldSpreadsheet(ov);
			setupSpreadsheet(nv);
		});

		report.disableProperty().bind(sheetNotSucceeded());
		gift.disableProperty().bind(report.disabledProperty());
//		print.disableProperty().bind(report.disabledProperty());
		notes.disableProperty().bind(report.disabledProperty());
		notifications.disableProperty().bind(report.disabledProperty());
		newMember.disableProperty().bind(report.disabledProperty());
		refresh.disableProperty().bind(model.spreadsheetProperty().isNull());

		noteAmountText.textProperty().bind(getShownSize(NoteManager.getInstance().dueAlarms()));
		noteAmountText.visibleProperty()
				.bind(model.silenceAlarmsProperty().not().and(noteAmountText.textProperty().isNotEqualTo("0")));

		Task<Boolean> checkLogin = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				return SheetsAPI.isLoggedIn();
			}
		};

		checkLogin.setOnSucceeded(evt -> {
			if (checkLogin.getValue()) {
				try {
					model.setSpreadsheet(SheetsAPI.loadSpreadsheetFX(false));
				} catch (IOException e) {
					// shouldn't happen
					e.printStackTrace();
				}
			} else {
				connection.fire();
			}
		});
		checkLogin.setOnFailed(evt -> connection.fire());

		Thread runner = new Thread(checkLogin);
		runner.setDaemon(true);
		runner.start();
	}

	private StringBinding getShownSize(ObservableList<? extends Note> list) {
		return new StringBinding() {
			{
				bind(list);
				for (Note n : list) {
					bind(n.hiddenProperty());
				}

				list.addListener((ListChangeListener.Change<? extends Note> change) -> {
					while (change.next()) {
						for (Note n : change.getAddedSubList()) {
							bind(n.hiddenProperty());
						}
						for (Note n : change.getRemoved()) {
							unbind(n.hiddenProperty());
						}
					}
					invalidate();
				});
			}

			@Override
			protected String computeValue() {
				int count = 0;
				for (Note n : list)
					if (!n.isHidden())
						count++;

				return count + "";
			}
		};
	}

	@FXML
	public void reportAction(ActionEvent event) {
//		SheetsAPI.doPriviligedFX(SheetID.REPORTS_PERMISSION, "Reports", false, () -> {
		SheetsAPI.doPriviligedFXNoCache(SheetID.REPORTS_PERMISSION, "Reports", () -> {
			Pane p = null;
			try {
				p = FXMLLoader.load(getClass().getResource("/resources/fxml/ReportQuery.fxml"));
			} catch (IOException e) {
				Util.showErrorDialog(e);
			}

			Util.createDialog(p, "Report Query");
			model.getSpreadsheet().searchService().cancel();
		});
	}

	@FXML
	public void giftAction(ActionEvent event) {
		Pane p = null;
		try {
			p = FXMLLoader.load(getClass().getResource("/resources/fxml/GiftQuery.fxml"));
		} catch (IOException e) {
			Util.showErrorDialog(e);
		}
		Util.createDialog(p, "Gift Query");
		model.getSpreadsheet().searchService().cancel();
	}
	
	@FXML
	public void notesAction(ActionEvent event) {
		NotesList.show();
	}

	@FXML
	public void printAction(ActionEvent evt) {
		Util.createDialog(new PrintPane(), "Print");
	}
	
	@FXML
	public void settingsAction(ActionEvent event) {
		Util.createDialog(new Settings(), "Settings");
	}

	@FXML
	public void connectionAction(ActionEvent event) {

		try {
			Spreadsheet sheet = SheetsAPI.loadSpreadsheetFX(true);
			if (sheet != null)
				model.setSpreadsheet(sheet);
		} catch (IOException e) { // access_denied
			try {
				SheetsAPI.logout();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			model.setSpreadsheet(null);
		}
	}

	@FXML
	public void refreshAction(ActionEvent event) {
		if (model.getSpreadsheet() == null)
			return;

		model.getSpreadsheet().reload();
	}

	@FXML
	public void notificationsAction(ActionEvent event) {
		Util.createDialog(new NotificationList(), "Notifications");
	}

	@FXML
	public void newMemberAction(ActionEvent event) {
		DetailsPane.showNewDialog();
	}

	private void detachOldSpreadsheet(Spreadsheet s) {
		if (s == null)
			return;

		refresh.mouseTransparentProperty().unbind();
		refreshGraphics.refreshingProperty().unbind();
	}

	private void setupSpreadsheet(Spreadsheet s) {
		if (s == null)
			return;

		refresh.mouseTransparentProperty().bind(s.loadService().stateProperty().isEqualTo(State.RUNNING));
		refreshGraphics.refreshingProperty().bind(s.loadService().runningProperty());
	}

	private BooleanBinding sheetNotSucceeded() {
		return new BooleanBinding() {

			{
				bind(model.spreadsheetProperty());
				model.spreadsheetProperty().addListener((obs, ov, nv) -> {
					if (ov != null)
						unbind(ov.loadService().stateProperty());
					if (nv != null)
						bind(nv.loadService().stateProperty());
				});
			}

			@Override
			protected boolean computeValue() {
				return model.getSpreadsheet() == null
						|| model.getSpreadsheet().loadService().getState() != State.SUCCEEDED;
			}

		};
	}

}
