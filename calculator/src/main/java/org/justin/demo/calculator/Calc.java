package org.justin.demo.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Calc {
	private static final Pattern FUNCTION = Pattern.compile("^\\s*(\\w+)\\s*\\(\\s*(.+)\\s*\\)\\s*$");	// " let ( a , -25 , a ) "
	private static final Pattern NUMBER   = Pattern.compile("^([\\+-]?\\d+)$");								// " -25 "
	private static final Pattern LETTER   = Pattern.compile("^([a-zA-Z])$");								// " A "

	/**
	 * Compute input equation. This method strips whitespace, validates against null and empty, initializes empty variable scope, and invokes private API.
	 * 
	 * Example inputs:
	 * add(2, 2)
	 * add(1, 2)
	 * add(1, mult(2, 3))
	 * mult(add(2, 2), div(9, 3))
	 * let(a, 5, add(a, a))
	 * let(a, 5, let(b, mult(a, 10), add(b, a)))
	 * let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b))
	 */
	public static int compute(String equation) throws Exception {
		if (null == equation) {
			throw new Exception("null equation");
		}
		final String equationNoWhitespace = equation.replaceAll(" ", "");
		if (equationNoWhitespace.isEmpty()) {
			throw new Exception("empty equation");
		}
		return Calc.compute(new HashMap<String,Integer>(), equationNoWhitespace);
	}

	/**
	 * Private API. It assumes no nulls or empty strings.
	 */
	private static int compute(final HashMap<String,Integer> variables, final String equation) throws Exception {
		try {
			final Matcher numberMatcher = Calc.NUMBER.matcher(equation);
			if (numberMatcher.find()) {
				return Integer.parseInt(numberMatcher.group(1));
			}
			final Matcher letterMatcher = Calc.LETTER.matcher(equation);
			if (letterMatcher.find()) {
				final String letter = letterMatcher.group(1);
				final Integer value = variables.get(letter);
				if (null == value) {
					throw new Exception("unknown variable " + letter);
				}
				return value.intValue();
			}
			final List<String> functionTokens = Calc.tokenizeExpression(equation);
			final String operator = functionTokens.get(0);
			if (3 == functionTokens.size()) {	// 0th is operation, 1st and 2nd are operands
				switch(operator) {
					case("add")  : return Calc.compute(variables, functionTokens.get(1)) + Calc.compute(variables, functionTokens.get(2));
					case("sub")  : return Calc.compute(variables, functionTokens.get(1)) - Calc.compute(variables, functionTokens.get(2));
					case("mult") : return Calc.compute(variables, functionTokens.get(1)) * Calc.compute(variables, functionTokens.get(2));
					case("div")  : return Calc.compute(variables, functionTokens.get(1)) / Calc.compute(variables, functionTokens.get(2));
					default: throw new Exception("unknown operator " + operator);
				}
			} else if (4 == functionTokens.size()) {	// 0th is operation, 1st and 2nd and 3rd are operands
				if (operator.equals("let")) {
					final String  letter = functionTokens.get(1);
					final Integer number = new Integer(Calc.compute(variables, functionTokens.get(2)));
					final HashMap<String,Integer> scopedVariables = new HashMap<>(variables);	// pass by copy, not by reference, shallow copy sufficient for immutable content
					scopedVariables.put(letter, number);	// save the variable assignment
					return Calc.compute(scopedVariables, functionTokens.get(3));	// recursive call with new scopedVariables
				}
				throw new Exception("unknown operator " + operator);
			}
			throw new Exception("unknown operation " + equation);
		} catch(Throwable t) {				// catch unchecked exceptions (i.e. error and runtime) for debug logging (ex: NullPointerException, RuntimeException, etc)
			Log.debug(t.getMessage(), t);	// log throwable as debug
			throw t;						// rethrow exception
		}
	}

	/**
	 * Tokenize function in a list. Element 0 is the operation, and remain elements are operands.
	 * Package scope is required for visibility in JUnit test cases.
	 */
	/*package*/ static List<String> tokenizeExpression(String equation) throws Exception {
		List<String> tokens = new ArrayList<String>(4);	// assume max tokens are operation and up to 3 operands

		final Matcher functionMatcher = Calc.FUNCTION.matcher(equation);
		if (!functionMatcher.find()) {
			throw new Exception("unknown expression " + equation);
		}
		tokens.add(functionMatcher.group(1));	// add operation
		final char[] operandsCharacters = functionMatcher.group(2).toCharArray();
		StringBuilder currentOperand = new StringBuilder();
		int           parenthesisDepth = 0;
		for (int i=0; i<operandsCharacters.length; i++) {
			final char operandsCharacter = operandsCharacters[i];
			if (' ' == operandsCharacter) {
				continue;	// skip whitespace
			} else if ('(' == operandsCharacter) {
				parenthesisDepth++;
			} else if (')' == operandsCharacter) {
				parenthesisDepth--;
				if (parenthesisDepth < 0) {
					throw new Exception("Unbalanced closing parentheses in operands " + operandsCharacters.toString());
				}
			} else if (',' == operandsCharacter) {
				if (0 == parenthesisDepth) {	// only tokenize current operand buffer if parentheses are balanced
					tokens.add(currentOperand.toString());
					currentOperand.setLength(0);
					continue;
				}
			}
			currentOperand.append(operandsCharacter);
		}
		if (currentOperand.length() > 0) {
			tokens.add(currentOperand.toString());
		}
		return tokens;
	}
}