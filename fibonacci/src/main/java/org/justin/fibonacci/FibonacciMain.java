package org.justin.fibonacci;

/**
 * Compute Fibonacci numbers with a 32-bit int using either a loop or recursion.
 * Throw ArithmeticException if there is overflow. Throw IllegalArgumentException for bad parameters.
 * ASSUMPTION: Initial values are 1 and 1. Expected max Fibonacci number that can be computed via 32-bit int without overflow is 46.
 * @author justin.cranford
 */
public class FibonacciMain {
	public static void main(String[] args) throws ArithmeticException, IllegalArgumentException {
		final int numArgs = args.length;
		if (numArgs < 1) {
			throw new IllegalArgumentException("Missing parameter for Fibonacci position. Expect: <position> (loop|recursion|lookup)");
		} else if (numArgs > 2) {
			throw new IllegalArgumentException("Too many parameters. Expect: <position> (loop|recursion|lookup)");
		}
		int position;
		try {
			position = Integer.parseInt(args[0]);
		} catch(NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid number parameter for Fibonacci position", nfe);
		}
		if (numArgs == 1) {
			System.out.println(FibonacciIntCalculator.computeUsingLoop(position));
		} else if (args[1] == null) {
			throw new IllegalArgumentException("Null compute method. Expect: <position> (loop|recursion|lookup)");
		} else if (args[1].equalsIgnoreCase("loop")) {
			System.out.println(FibonacciIntCalculator.computeUsingLoop(position));
		} else if (args[1].equalsIgnoreCase("recursion")) {
			System.out.println(FibonacciIntCalculator.computeUsingRecursion(position));
		} else if (args[1].equalsIgnoreCase("lookup")) {
			System.out.println(FibonacciIntCalculator.computeFibonacciNumberUsingLookup(position));
		} else {
			throw new IllegalArgumentException("Invalid compute method. Expect: <position> (loop|recursion|lookup)");
		}
	}
}