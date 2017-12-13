package org.hafotzastehillim.pointentry.fx;

import java.io.IOException;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.notes.Note;
import org.hafotzastehillim.pointentry.fx.notes.NoteManager;
import org.hafotzastehillim.pointentry.fx.notes.NotesList;
import org.hafotzastehillim.pointentry.fx.notes.NotificationList;
import org.hafotzastehillim.pointentry.fx.print.PrintPane;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.spreadsheet.SheetID;
import org.hafotzastehillim.pointentry.spreadsheet.SheetsAPI;
import org.hafotzastehillim.pointentry.spreadsheet.Spreadsheet;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
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
			DialogUtils.showErrorDialog(e);
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
		print.disableProperty().bind(report.disabledProperty());
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

	private StringBinding getShownSize(ObservableList<Note> list) {
		return Bindings.createStringBinding(() -> String.valueOf(list.stream().filter(n -> !n.isHidden()).count()),
				Util.withExtractor(list, n -> new Observable[] { n.hiddenProperty() }));
	}

	@FXML
	public void reportAction(ActionEvent event) {
		// SheetsAPI.doPriviligedFX(SheetID.REPORTS_PERMISSION, "Reports", false, () ->
		// {
		SheetsAPI.doPriviligedFXNoCache(SheetID.REPORTS_PERMISSION, "Reports", () -> {
			Pane p = null;
			try {
				p = FXMLLoader.load(getClass().getResource("/resources/fxml/ReportQuery.fxml"));
			} catch (IOException e) {
				DialogUtils.showErrorDialog(e);
			}

			DialogUtils.createDialog(p, "Report Query");
			model.getSpreadsheet().searchService().cancel();
		});
	}

	@FXML
	public void giftAction(ActionEvent event) {
		Pane p = null;
		try {
			p = FXMLLoader.load(getClass().getResource("/resources/fxml/GiftQuery.fxml"));
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
		}

		DialogUtils.createDialog(p, "Gift Query");
		model.getSpreadsheet().searchService().cancel();
	}

	@FXML
	public void notesAction(ActionEvent event) {
		NotesList.show();
	}

	@FXML
	public void printAction(ActionEvent evt) {
		DialogUtils.createDialog(new PrintPane(), "Print");
	}

	@FXML
	public void settingsAction(ActionEvent event) {
		DialogUtils.createDialog(new Settings(), "Settings");
	}

	@FXML
	public void connectionAction(ActionEvent event) {

		try {
			Spreadsheet sheet = SheetsAPI.loadSpreadsheetFX(true);
			if (sheet != null) {
				model.setSpreadsheet(sheet);
			}
		} catch (IOException e) { // access_denied
			SheetsAPI.logout();
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
		NotificationList.show();
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
		refreshGraphics.refreshingProperty().bind(refresh.mouseTransparentProperty());
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
