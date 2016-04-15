package org.justin.fibonacci;

import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert;

@SuppressWarnings("static-method")
public class TestFibonacciIntCalculator {
	private static long NANOTIME_USING_LOOKUP    = -1;
	private static long NANOTIME_USING_LOOP      = -1;
	private static long NANOTIME_USING_RECURSION = -1;
	
	@AfterClass
	public static void dumpPerformanceComparison() {
		System.out.println("Total time to compute first 46 Fibonacci numbers using different methods");
		System.out.println("Lookup:    " + TestFibonacciIntCalculator.NANOTIME_USING_LOOKUP    + "ns");
		System.out.println("Loop:      " + TestFibonacciIntCalculator.NANOTIME_USING_LOOP      + "ns");
		System.out.println("Recursion: " + TestFibonacciIntCalculator.NANOTIME_USING_RECURSION + "ns");
	}

	@Test
	public void testFibonacciLoopValues() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				Assert.assertEquals(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[position], FibonacciIntCalculator.computeUsingLoop(position));
			}
		} finally {
			TestFibonacciIntCalculator.NANOTIME_USING_LOOP = System.nanoTime() - startNano;
		}
	}

	@Test
	public void testFibonacciRecursionValues() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				Assert.assertEquals(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[position], FibonacciIntCalculator.computeUsingRecursion(position));
			}
		} finally {
			TestFibonacciIntCalculator.NANOTIME_USING_RECURSION = System.nanoTime() - startNano;
		}
	}

	@Test
	public void testFibonacciLookupValues() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				Assert.assertEquals(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[position], FibonacciIntCalculator.computeFibonacciNumberUsingLookup(position));
			}
		} finally {
			TestFibonacciIntCalculator.NANOTIME_USING_LOOKUP = System.nanoTime() - startNano;
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFibonacciLoopInvalidParameterNegative() {
		FibonacciIntCalculator.computeUsingLoop(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFibonacciRecursionInvalidParameterNegative() {
		FibonacciIntCalculator.computeUsingRecursion(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFibonacciLookupInvalidParameterNegative() {
		FibonacciIntCalculator.computeFibonacciNumberUsingLookup(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFibonacciLookupInvalidParameterZero() {
		FibonacciIntCalculator.computeFibonacciNumberUsingLookup(0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFibonacciRecursionInvalidParameterZero() {
		FibonacciIntCalculator.computeUsingRecursion(0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFibonacciLoopInvalidParameterZero() {
		FibonacciIntCalculator.computeUsingLoop(0);
	}

	@Test(expected=ArithmeticException.class)
	public void testFibonacciLoopIntOverflowAfterPosition46() {
		Assert.assertEquals(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[FibonacciIntCalculator.MAX_POSITION_USING_INT], FibonacciIntCalculator.computeUsingLoop(FibonacciIntCalculator.MAX_POSITION_USING_INT));	// Expect OK
		FibonacciIntCalculator.computeUsingLoop(FibonacciIntCalculator.MAX_POSITION_USING_INT+1);	// Expect Overflow
	}

	@Test(expected=ArithmeticException.class)
	public void testFibonacciRecursionIntOverflowAfterPosition46() {
		Assert.assertEquals(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[FibonacciIntCalculator.MAX_POSITION_USING_INT], FibonacciIntCalculator.computeUsingRecursion(FibonacciIntCalculator.MAX_POSITION_USING_INT));	// Expect OK
		FibonacciIntCalculator.computeUsingRecursion(FibonacciIntCalculator.MAX_POSITION_USING_INT+1);	// Expect Overflow
	}

	@Test(expected=ArithmeticException.class)
	public void testFibonacciLookupIntOverflowAfterPosition46() {
		Assert.assertEquals(FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS[FibonacciIntCalculator.MAX_POSITION_USING_INT], FibonacciIntCalculator.computeFibonacciNumberUsingLookup(FibonacciIntCalculator.MAX_POSITION_USING_INT));	// Expect OK
		FibonacciIntCalculator.computeFibonacciNumberUsingLookup(FibonacciIntCalculator.MAX_POSITION_USING_INT+1);	// Expect Overflow
	}
}