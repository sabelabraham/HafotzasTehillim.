package org.hafotzastehillim.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;

import java.io.IOException;

import org.hafotzastehillim.fx.spreadsheet.GoogleSpreadsheet;
import org.hafotzastehillim.fx.spreadsheet.SheetID;
import org.hafotzastehillim.fx.spreadsheet.SheetsAPI;
import org.hafotzastehillim.fx.spreadsheet.Spreadsheet;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;

public class TopBarController {
	@FXML
	private JFXButton report;
	@FXML
	private JFXButton gift;
	@FXML
	private JFXButton settings;
	@FXML
	private JFXButton connection;
	@FXML
	private JFXButton refresh;
	@FXML
	private JFXButton newMember;

	private Refresh refreshGraphics;

	private static Model model = Model.getInstance();

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
		newMember.disableProperty().bind(report.disabledProperty());
		refresh.disableProperty().bind(model.spreadsheetProperty().isNull());

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

	@FXML
	public void reportAction(ActionEvent event) {
		SheetsAPI.doPriviligedFXNoCache(SheetID.REPORTS_PERMISSION, "Reports", () -> { // FIXME
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
	public void settingsAction(ActionEvent event) {
		Pane p = null;
		try {
			p = FXMLLoader.load(getClass().getResource("/resources/fxml/Settings.fxml"));
		} catch (IOException e) {
			Util.showErrorDialog(e);
		}
		Util.createDialog(p, "Settings");
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
