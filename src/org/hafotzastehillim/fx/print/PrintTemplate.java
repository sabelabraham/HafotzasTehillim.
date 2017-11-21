package org.hafotzastehillim.fx.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hafotzastehillim.fx.Model;
import org.hafotzastehillim.fx.util.Util;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;

public class PrintTemplate extends StackPane {

	@FXML
	private Label hebrewName;
	@FXML
	private Label date;
	@FXML
	private Label account;
	@FXML
	private Label sum;
	@FXML
	private Label by;
	@FXML
	private Label name;
	@FXML
	private Label address;
	@FXML
	private Label city;
	@FXML
	private Label accountLabel;
	@FXML
	private Label sumLabel;
	@FXML
	private Label byLabel;
	@FXML
	private Label title;
	@FXML
	private Label hebrewTitle;
	@FXML
	private Label hebrewTitleEnding;
	@FXML
	private GridPane children;

	private static final int WIDTH = 685;
	private static final int HEIGHT = 883;

	PrintTemplate() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/PrintTemplate.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}

	}

	@FXML
	private void initialize() {
		accountLabel.visibleProperty().bind(account.textProperty().isNotEmpty());
		sumLabel.visibleProperty().bind(sum.textProperty().isNotEmpty());
		byLabel.visibleProperty().bind(by.textProperty().isNotEmpty());
		title.visibleProperty().bind(name.textProperty().isNotEmpty());
		hebrewTitle.visibleProperty().bind(hebrewName.textProperty().isNotEmpty());
		hebrewTitleEnding.visibleProperty().bind(hebrewName.textProperty().isNotEmpty());

		Font f = Font.loadFont(getClass().getResource("/resources/fonts/DAVIDBD.TTF").toExternalForm(), 20);

		hebrewTitle.setFont(f);
		hebrewName.setFont(f);
		hebrewTitleEnding.setFont(f);

		children.getColumnConstraints().setAll(new ColumnConstraints(10, 50, 100, Priority.SOMETIMES, HPos.LEFT, true),
				new ColumnConstraints(10, 50, 100, Priority.SOMETIMES, HPos.RIGHT, true));

	}

	public static PrintTemplate create() {
		return new PrintTemplate();
	}

	public PrintTemplate hebrewName(String name) {
		this.hebrewName.setText(name);
		return this;
	}

	public PrintTemplate date(String date) {
		this.date.setText(date);
		return this;
	}

	public PrintTemplate name(String name) {
		this.name.setText(name);
		return this;
	}

	public PrintTemplate account(String acct) {
		this.account.setText(acct);
		return this;
	}

	public PrintTemplate sum(String sum) {
		this.sum.setText(sum);
		return this;
	}

	public PrintTemplate by(String by) {
		this.by.setText(by);
		return this;
	}

	public PrintTemplate address(String address) {
		this.address.setText(address);
		return this;
	}

	public PrintTemplate city(String city) {
		this.city.setText(city);
		return this;
	}

	private static final Font font = Font.font(10);

	public PrintTemplate children(List<? extends ChildName> list) {
		children.getRowConstraints().clear();
		children.getRowConstraints().add(new RowConstraints(20));
		children.getRowConstraints().addAll(Collections.nCopies(list.size(), new RowConstraints(10, 15, 20)));

		if (list.size() == 0)
			return this;

		Label kinderLabel = new Label();
		kinderLabel.setUnderline(true);
		children.add(kinderLabel, 0, 0, 2, 1);
		GridPane.setHalignment(kinderLabel, HPos.CENTER);

		for (int i = 0; i < list.size(); i++) {
			Label id = new Label("ID #" + list.get(i).id);
			id.setFont(font);
			Label name = new Label(list.get(i).name);
			name.setFont(font);

			children.add(id, 0, i + 1);
			children.add(name, 1, i + 1);
		}

		if (list.size() == 1) {
			kinderLabel.setText("תהלים קינד");
		} else {
			kinderLabel.setText("תהלים קינדער");
		}

		return this;
	}

	public boolean print(boolean showDialog) {
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job != null) {
			if (showDialog) {
				boolean ok = job.showPrintDialog(Model.getInstance().getCurrentStage());
				if (!ok)
					return false;
			}
			PageLayout layout = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT,
					Printer.MarginType.HARDWARE_MINIMUM);

			double scaleX = layout.getPrintableWidth() / WIDTH;
			double scaleY = layout.getPrintableHeight() / HEIGHT;
			getTransforms().add(new Scale(scaleX, scaleY));

			boolean success = job.printPage(layout, this);
			if (success) {
				job.endJob();
				return true;
			}
		}
		return false;
	}

	public boolean print() {
		return print(false);
	}
}
