package org.hafotzastehillim.pointentry.fx.print;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.DetailsPane;
import org.hafotzastehillim.pointentry.fx.Model;
import org.hafotzastehillim.pointentry.fx.util.EnglishToHebrewKeyInterceptor;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.spreadsheet.Column;
import org.hafotzastehillim.pointentry.spreadsheet.Entry;
import org.hafotzastehillim.pointentry.spreadsheet.Tab;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;

public class PrintPane extends GridPane {

	public PrintPane() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/PrintPane.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}
	}

	@FXML
	private DatePicker date;
	@FXML
	private TextField account;
	@FXML
	private TextField name;
	@FXML
	private TextField address;
	@FXML
	private TextField city;
	@FXML
	private TextField sum;
	@FXML
	private TextField by;
	@FXML
	private TextField hebrewName;
	@FXML
	private RadioButton pledge;
	@FXML
	private RadioButton receipt;
	@FXML
	private CheckBox printDialog;
	@FXML
	private JFXButton print;

	private List<ChildName> children;

	private static StatementType statementType = StatementType.PLEDGE;

	@FXML
	private void initialize() {
		date.setValue(LocalDate.now());

		by.textProperty().bindBidirectional(Model.getInstance().printByProperty());
		printDialog.selectedProperty().bindBidirectional(Model.getInstance().showPrintDialogProperty());

		sceneProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				account.requestFocus();
			}
		});

		EnglishToHebrewKeyInterceptor hebrew = new EnglishToHebrewKeyInterceptor();
		hebrewName.setOnKeyTyped(hebrew);

		account.setTextFormatter(new TextFormatter<>(change -> {
			if (!change.isContentChange())
				return change;

			if (change.getText().isEmpty() || change.getText().matches("[\\d*]"))
				return change;

			return null;
		}));

		sum.setTextFormatter(Util.asYouTypeCurrencyFormatter());

		print.disableProperty()
				.bind(date.getEditor().textProperty().isEmpty().or(name.textProperty().isEmpty())
						.or(address.textProperty().isEmpty()).or(city.textProperty().isEmpty())
						.or(hebrewName.textProperty().isEmpty()).or(by.textProperty().isEmpty()));
		DialogUtils.dialogButtonStyling(print);

		children = new ArrayList<>();

		pledge.setSelected(statementType == StatementType.PLEDGE);
		receipt.setSelected(statementType == StatementType.RECEIPT);

		pledge.selectedProperty().addListener((obs, ov, nv) -> statementType = StatementType.PLEDGE);
		receipt.selectedProperty().addListener((obs, ov, nv) -> statementType = StatementType.RECEIPT);
	}

	private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM. d, ''yy");

	@FXML
	private void printAction(ActionEvent evt) {
		new Thread(() -> {
			boolean success = PrintTemplate.create().date(date.getValue()).account(account.getText())
					.name(name.getText()).address(address.getText()).city(city.getText()).sum(sum.getText())
					.by(by.getText()).hebrewName(hebrewName.getText()).statementType(statementType).children(children)
					.print(printDialog.isSelected());

			if (success) {
				List<String> upload = Arrays.asList(fmt.format(date.getValue()), account.getText(), hebrewName.getText(), sum.getText(), by.getText());
				Tab t = statementType == StatementType.PLEDGE ? Tab.PLEDGES : statementType == StatementType.RECEIPT ? Tab.RECEIPTS : null;

				Platform.runLater(() -> {
					account.setText("");
					name.setText("");
					address.setText("");
					city.setText("");
					sum.setText("");
					hebrewName.setText("");
				});
				
				Model.getInstance().getSpreadsheet().addRow(t.ordinal(), upload);
			}
		}).start();
	}

	@FXML
	private void searchAccount(ActionEvent evt) {
		String acct = account.getText();

		if (acct.isEmpty())
			return;

		ObservableList<Entry> list = FXCollections.observableArrayList();

		Util.onceOnSucceeded(Model.getInstance().getSpreadsheet().searchService().stateProperty(), () -> {
			if (list.size() == 0)
				return;

			Entry e = list.get(0);

			name.setText(" " + e.getLastName());
			name.positionCaret(0);
			Platform.runLater(() -> name.deselect());

			address.setText(e.getAddressNumber() + " " + e.getAddressName() + " " + e.getApt());
			city.setText(e.getCity() + ", " + e.getState() + " " + e.getZip());

			String heb = e.getFatherName().trim();
			if (heb.startsWith("ר' ")) {
				heb = heb.substring(3);
			}
			hebrewName.setText(heb + " " + e.getLastNameYiddish());
			name.requestFocus();

			children = list.stream().map(ChildName::new).collect(Collectors.toList());

		});
		Model.getInstance().getSpreadsheet().searchEntries(list,
				data -> data.get(Column.ACCOUNT_NUMBER.ordinal()).equals(acct));
	}

}
