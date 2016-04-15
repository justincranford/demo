package org.justin.demo.calculator;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulate log implementation. Built-in JDK14 logging is used.
 *  
 * TODO: Wrap JDK14 with SLF14 to further decouple application from logger. That would allow easy
 * injecting of a different log implementation (ex: log4j, SLF4J logback, etc), especially using
 * properties files instead of programmatic config.
 * @author justin.cranford
 */
public final class Log {
	private static final Logger ROOT_LOGGER = Logger.getLogger("");	// Not the same as getGlobal(), only blank logger has the default STDOUT handler we want to remove.

	/*package*/ static void setJdk14RootLoggerDefaults(final String logfile) {
		try {
			for (Handler handler : Log.ROOT_LOGGER.getHandlers()) {
				Log.ROOT_LOGGER.removeHandler(handler);
			}
			Log.ROOT_LOGGER.addHandler(new FileHandler(logfile));
			Log.ROOT_LOGGER.setLevel(Level.INFO);
		} catch(Throwable t) {
			System.err.println("Failed to configure JDK14 logging");
			t.printStackTrace();
		}
	}

	/**
	 * Assume SLF4J implementation is JDK14 logging. Encapsulate JDK14 logging class instances here, so main code will not use it by accident instead of SLF4J.
	 * TODO: Change from static to non-static reusable for unit testing.
	 */
	/*package*/ static void setJdk14RootLoggerLogLevel(final String logLevelStr) {
		Log.ROOT_LOGGER.setLevel(Log.convertLogLevel(logLevelStr));
	}

	private static Level convertLogLevel(final String logLevelStr) {
		boolean isBadLogLevel = false;
		final Level log4jLogLevel;
		switch(logLevelStr) {
			case("ERROR") : { log4jLogLevel = Level.SEVERE;							break; }
			case("INFO")  : { log4jLogLevel = Level.INFO;							break; }
			case("DEBUG") : { log4jLogLevel = Level.FINE;							break; }
			default       : { log4jLogLevel = Level.INFO;	isBadLogLevel = true;	break; }
		}
		if (isBadLogLevel) {
			Log.ROOT_LOGGER.log(Level.INFO, "Bad log level " + logLevelStr +  " ignored, default to INFO.");	// ASSUMPTION: Default log level is INFO so this will be logged OK.
		}
		return log4jLogLevel;
	}

	public static final void error(final String message, final Throwable t) {
		Log.ROOT_LOGGER.log(Level.SEVERE, message, t);
	}
	public static final void error(final String message) {
		Log.ROOT_LOGGER.log(Level.SEVERE, message);
	}

	public static final void info(final String message, final Throwable t) {
		Log.ROOT_LOGGER.log(Level.INFO, message, t);
	}
	public static final void info(final String message) {
		Log.ROOT_LOGGER.log(Level.INFO, message);
	}

	public static final void debug(final String message, final Throwable t) {
		Log.ROOT_LOGGER.log(Level.FINE, message, t);
	}
	public static final void debug(final String message) {
		Log.ROOT_LOGGER.log(Level.FINE, message);
	}
}