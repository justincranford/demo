package org.justin.fibonacci;

/**
 * Implement methods for computing Fibonacci numbers with a 32-bit int using a loop, recursion, and lookup methods.
 * Throw ArithmeticException if there is overflow. Throw IllegalArgumentException for bad parameters.
 * ASSUMPTION: Initial values are 1 and 1. Expected max Fibonacci number that can be computed via 32-bit int without overflow is 46.
 * @author justin.cranford
 */
public class FibonacciIntCalculator {
	// Pad first array element so the array index lines up with the Fibonacci position.
	/*package*/ static final int[] FIRST_46_FIBONACCI_NUMBERS = {Integer.MIN_VALUE, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811, 514229, 832040, 1346269, 2178309, 3524578, 5702887, 9227465, 14930352, 24157817, 39088169, 63245986, 102334155, 165580141, 267914296, 433494437, 701408733, 1134903170, 1836311903};

	/*package*/ static final int MAX_POSITION_USING_INT = 46;	// ASSUMPTION: Largest Fibonacci number that can be computed via 32-bit int without overflow is 46.

//	static {
//		// dump Fibonacci numbers to console
//		for (int position=1; position<=ComputeFibonacciNumber.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
//			System.out.println(position + "=" + ComputeFibonacciNumber.FIRST_46_FIBONACCI_NUMBERS[position]);
//		}
//	}

	public static int computeFibonacciNumberUsingLookup(final int finobacciNumber) throws ArithmeticException, IllegalArgumentException {
		if (finobacciNumber <= 0) {
			throw new IllegalArgumentException("Invalid Fibonacci position. Must be 1 or more.");
		} else if (finobacciNumber <= 2) {
			return 1;
		} else if (finobacciNumber > MAX_POSITION_USING_INT) {
			throw new ArithmeticException("Integer overflow");
		}
		return FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[finobacciNumber];
	}

	public static int computeUsingLoop(final int finobacciNumber) throws ArithmeticException, IllegalArgumentException {
		if (finobacciNumber <= 0) {
			throw new IllegalArgumentException("Invalid Fibonacci position. Must be 1 or more.");
		} else if (finobacciNumber <= 2) {
			return 1;
		}
		int first=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[1], second=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[2], sum=-1, remaining=finobacciNumber;
		while (true) {
			sum = first+second;
			if (sum < 0) {
				throw new ArithmeticException("Integer overflow");
			} else if (remaining == 3) {
				return sum;
			}
			first=second;
			second=sum;
			--remaining;
		}
	}

	public static int computeUsingRecursion(final int finobacciNumber) throws ArithmeticException, IllegalArgumentException {
		if (finobacciNumber <= 0) {
			throw new IllegalArgumentException("Invalid Fibonacci position. Must be 1 or more.");
		} else if (finobacciNumber <= 2) {
			return 1;
		}
		return FibonacciIntCalculator.computeRecursionHelper(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[1], FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[2], finobacciNumber);
	}

	private static int computeRecursionHelper(final int first, final int second, final int remaining) throws ArithmeticException {
		final int sum = first + second;
		if (sum < 0) {
			throw new ArithmeticException("Integer overflow");
		} else if (remaining == 3) {
			return sum;
		}
		return FibonacciIntCalculator.computeRecursionHelper(second, sum, remaining-1);
	}
}