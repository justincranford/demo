package org.justin.demo.calculator;

/**
 * @description Command line calculator. Input from parameter (not STDIN), output to console (STDOUT).
 * @author justin.cranford
 * @see {@link org.justin.demo.calculator.Calc}
 * 
 * Example input:
 * add(2, 2)
 * add(1, 2)
 * add(1, mult(2, 3))
 * mult(add(2, 2), div(9, 3))
 * let(a, 5, add(a, a))
 * let(a, 5, let(b, mult(a, 10), add(b, a)))
 * let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))
 */
public final class Main {
	private static final String LOGFILE = "calculator.log";
	static {
		Log.setJdk14RootLoggerDefaults(LOGFILE);
	}

	public static void main(String[] parameters) {
		if (null == parameters) {
			Main.handleError(Main.USAGE_ERROR.NULL_PARAMETERS);
		} else if (0 == parameters.length) {
			Main.handleError(Main.USAGE_ERROR.EMPTY_PARAMETERS);
		} else if (parameters.length > 2) {
			Main.handleError(Main.USAGE_ERROR.TOO_MANY_PARAMETERS);
		} else {
			final String equation = parameters[0];
			if (null == equation) {
				Main.handleError(Main.USAGE_ERROR.NULL_PARAMETER);
			} else if (equation.trim().isEmpty()) {
				Main.handleError(Main.USAGE_ERROR.EMPTY_PARAMETER);
			} else {
				if (2 == parameters.length) {
					Log.setJdk14RootLoggerLogLevel(parameters[1]);
				}
				StringBuilder sb = new StringBuilder("Invocation: calculator.Main ");
				for (String parameter : parameters) {
					sb.append("\"").append(parameter).append("\" ");
				}
				Log.info(sb.substring(0, sb.length()-1));
				try {
					final int result = Calc.compute(equation);
					Log.info("Result: " + result + ", from Expression: " + equation);
					System.out.println(result);
				} catch (Exception e) {
					Main.handleError(Main.USAGE_ERROR.PARSE_ERROR, e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	private static void handleError(final USAGE_ERROR usageError) {
		Main.handleError(usageError, null);
	}
	private static void handleError(final USAGE_ERROR usageError, final String additionalError) {
		final String errorMessage = "error: " + usageError.getMessage() + (null != additionalError ? ", "+additionalError : "");
		final String usageMessage = "usage: java calculator.Main <formula> [loglevel=(ERROR,INFO,DEBUG)]";
		Log.error(errorMessage);
		Log.info(usageMessage);
		System.err.println(errorMessage);
		System.err.println(usageMessage);
		System.exit(usageError.getCode());
	}

	public static enum USAGE_ERROR {
		NULL_PARAMETERS		(1, "Null parameters"),
		EMPTY_PARAMETERS	(2, "Empty parameters"),
		TOO_MANY_PARAMETERS	(3, "Too many parameters"),
		NULL_PARAMETER		(4, "Null parameter"),
		EMPTY_PARAMETER		(5, "Empty parameter"),
		PARSE_ERROR			(6, "Parse error");

		private int    errorCode;
		private String errorMessage;
		private USAGE_ERROR(final int inErrorCode, final String inErrorMessage) {
			this.errorCode    = inErrorCode;
			this.errorMessage = inErrorMessage;
		}
		public int getCode() {
			return this.errorCode;
		}
		public String getMessage() {
			return this.errorMessage;
		}
	}
}