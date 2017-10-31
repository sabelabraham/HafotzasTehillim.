package org.hafotzastehillim.fx.util;

import static org.hafotzastehillim.fx.util.ConnectionState.NOT_PRESENT;
import static org.hafotzastehillim.fx.util.ConnectionState.PRESENT;
import static org.hafotzastehillim.fx.util.ConnectionState.UNKNOWN;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class Ping {

	private static final InetAddress googleDns;

	static {
		try {
			googleDns = InetAddress.getByName("8.8.8.8");
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

	}

	private ScheduledService<Boolean> pingService;
	private ReadOnlyObjectWrapper<ConnectionState> connection;

	private Ping() {
		connection = new ReadOnlyObjectWrapper<>(this, "connection", UNKNOWN);

		pingService = new ScheduledService<Boolean>() {

			@Override
			protected Task<Boolean> createTask() {
				return new Task<Boolean>() {

					@Override
					protected Boolean call() throws Exception {
						return googleDns.isReachable(1000);
					}

				};
			}
		};

		pingService.setOnSucceeded((evt) -> {
			if (pingService.getLastValue() == null) {
				if (pingService.getValue() == null) {
					connection.set(UNKNOWN);
				} else {
					connection.set(pingService.getValue() ? PRESENT : NOT_PRESENT);
				}
			} else {
				connection.set(pingService.getLastValue() ? PRESENT : NOT_PRESENT);
			}
		});
		pingService.setOnFailed((evt) -> connection.set(UNKNOWN));
		pingService.setOnCancelled((evt) -> connection.set(UNKNOWN));

		pingService.setPeriod(Duration.seconds(5));

	}

	public ConnectionState getConnectionState() {
		return connectionProperty().get();
	}

	public ReadOnlyObjectProperty<ConnectionState> connectionProperty() {
		return connection.getReadOnlyProperty();
	}

	public void setPingDelay(Duration delay) {
		pingService.setPeriod(delay);
	}

	public Duration getPingDelay() {
		return pingService.getPeriod();
	}

	public void start() {
		pingService.reset();
		pingService.restart();
	}

	public void stop() {
		pingService.cancel();
	}

	public static boolean pingNow() {
		try {
			return googleDns.isReachable(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Ping getInstance() {
		return InstanceHandler.INSTANCE;
	}

	private static final class InstanceHandler {
		private static final Ping INSTANCE = new Ping();
	}

}