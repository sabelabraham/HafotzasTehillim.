package org.hafotzastehillim.pointentry.spreadsheet;

import javafx.fxml.FXML;

import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.net.CookieHandler;

import org.hafotzastehillim.pointentry.fx.Model;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

public class LoginController {

	@FXML
	private Pane root;
	@FXML
	private GridPane view;
	@FXML
	private Text text;
	@FXML
	private Text name;
	@FXML
	private Hyperlink logout;
	@FXML
	private JFXButton signin;
	@FXML
	private CheckBox staySignedIn;
	@FXML
	private JFXProgressBar progress;
	@FXML
	private Pane mask;
	@FXML
	private WebView web;

	private String customMessage;

	private static final String loginString = "Please sign in to Google to continue";
	private static final String logoutString = "You are currently logged in as ";

	@FXML
	public void initialize() {
		text.setText(loginString);
		logout.visibleProperty().bind(text.textProperty().isEqualTo(logoutString));
		name.visibleProperty().bind(logout.visibleProperty());
		logout.visibleProperty().addListener((obs, ov, nv) -> setupNameLabel());
		setupNameLabel();

		signin.setDefaultButton(true);

		// Mimic Chrome browser for new Google sign-in form
		web.getEngine().setUserAgent(
				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");

		ReadOnlyObjectProperty<State> loadState = web.getEngine().getLoadWorker().stateProperty();

		loadState.addListener((obs, ov, nv) -> {
			if (nv == Worker.State.SUCCEEDED) {
				String url = web.getEngine().getLocation();
				if (url.startsWith("http://localhost")) {
					web.getEngine().loadContent("<html><p align=center><img src="
							+ getClass().getResource("/resources/images/wait.gif").toExternalForm() + "></html>");
				}
			}
		});

		progress.visibleProperty()
				.bind(loadState.isEqualTo(Worker.State.RUNNING).or(loadState.isEqualTo(Worker.State.FAILED)));
		loadState.isEqualTo(State.FAILED).addListener((obs, ov, nv) -> {
			progress.setProgress(1);
			progress.pseudoClassStateChanged(PseudoClass.getPseudoClass("error"), nv);
		});

		mask.visibleProperty().bind(progress.visibleProperty());
	}

	private void setupNameLabel() {
		if (logout.isVisible()) {
			name.setText((SheetsAPI.getUsername() + ", ").replace(" ", "\u00a0"));
		} else {
			name.setText("");
		}
	}

	public void setCustomMessage(String msg) {
		customMessage = msg.replace(" ", "\u00a0");
		if (!logout.isVisible()) {
			text.setText(loginString + " to " + customMessage);
		}
	}

	public void setMessageByLoggedIn() {
		if (SheetsAPI.isLoggedIn()) {
			text.setText(logoutString);
		} else {
			initLoginMessage();
		}
	}

	private void initLoginMessage() {
		if (customMessage == null)
			text.setText(loginString);
		else
			text.setText(loginString + " to " + customMessage);
	}

	public void setSignInAction(EventHandler<ActionEvent> handler) {
		signin.setOnAction(handler);
	}

	@FXML
	@SuppressWarnings("restriction")
	public void logout(ActionEvent event) {
		SheetsAPI.logout();

		Model.getInstance().setSpreadsheet(null);
		initLoginMessage();

		CookieHandler.setDefault(new com.sun.webkit.network.CookieManager());
	}

	WebView getWebView() {
		return web;
	}

	public final boolean isStaySignedIn() {
		return staySignedIn.isSelected();
	}

	public final void setStaySignedIn(boolean bool) {
		staySignedIn.setSelected(bool);
	}

	public BooleanProperty staySignedInProperty() {
		return staySignedIn.selectedProperty();
	}

	public final void setShowStaySignedIn(boolean bool) {
		staySignedIn.setVisible(bool);
	}

	public final boolean isShowingStaySignedIn() {
		return staySignedIn.isVisible();
	}

	public BooleanProperty showStaySignedInProperty() {
		return staySignedIn.visibleProperty();
	}
}
