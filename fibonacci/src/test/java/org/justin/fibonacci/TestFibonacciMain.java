package org.justin.fibonacci;

import org.junit.Test;
import org.junit.AfterClass;

@SuppressWarnings("static-method")
public class TestFibonacciMain {
	private static long NANOTIME_USING_DEFAULT   = -1;
	private static long NANOTIME_USING_LOOKUP    = -1;
	private static long NANOTIME_USING_LOOP      = -1;
	private static long NANOTIME_USING_RECURSION = -1;
	
	@AfterClass
	public static void dumpPerformanceComparison() {
		System.out.println("Total time to compute first 46 Fibonacci numbers using different methods");
		System.out.println("Default:   " + TestFibonacciMain.NANOTIME_USING_DEFAULT   + "ns");
		System.out.println("Lookup:    " + TestFibonacciMain.NANOTIME_USING_LOOKUP    + "ns");
		System.out.println("Loop:      " + TestFibonacciMain.NANOTIME_USING_LOOP      + "ns");
		System.out.println("Recursion: " + TestFibonacciMain.NANOTIME_USING_RECURSION + "ns");
	}

	@Test
	public void testFibonacciNoMethod() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				FibonacciMain.main(new String[]{Integer.toString(position)});	// Expect OK
			}
		} finally {
			TestFibonacciMain.NANOTIME_USING_DEFAULT = System.nanoTime() - startNano;
		}
	}

	@Test
	public void testFibonacciLoop() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				FibonacciMain.main(new String[]{Integer.toString(position), "loop"});	// Expect OK
			}
		} finally {
			TestFibonacciMain.NANOTIME_USING_LOOP = System.nanoTime() - startNano;
		}
	}

	@Test
	public void testFibonacciRecursionValues() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				FibonacciMain.main(new String[]{Integer.toString(position), "recursion"});	// Expect OK
			}
		} finally {
			TestFibonacciMain.NANOTIME_USING_RECURSION = System.nanoTime() - startNano;
		}
	}

	@Test
	public void testFibonacciLookupValues() throws Exception {
		final long startNano = System.nanoTime();
		try {
			for (int position=1; position<=FibonacciIntCalculator.FIRST_46_FIBONACCI_NUMBERS.length-1; position++) {
				FibonacciMain.main(new String[]{Integer.toString(position), "lookup"});	// Expect OK
			}
		} finally {
			TestFibonacciMain.NANOTIME_USING_LOOKUP = System.nanoTime() - startNano;
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNoParameters() throws Exception {
		FibonacciMain.main(new String[]{});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullParameter() throws Exception {
		FibonacciMain.main(new String[]{null});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBlankParameter() throws Exception {
		FibonacciMain.main(new String[]{""});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNonIntegerParameter() throws Exception {
		FibonacciMain.main(new String[]{"abc"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullMethodParameter() throws Exception {
		FibonacciMain.main(new String[]{"1", null});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBlankMethodParameter() throws Exception {
		FibonacciMain.main(new String[]{"1", ""});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testUnknownMethodParameter() throws Exception {
		FibonacciMain.main(new String[]{"1", "unknown"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeOneNoMethod() throws Exception {
		FibonacciMain.main(new String[]{"-1"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeOneInteger() throws Exception {
		FibonacciMain.main(new String[]{"-1", "loop"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeOneRecursion() throws Exception {
		FibonacciMain.main(new String[]{"-1", "recursion"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeOneLookup() throws Exception {
		FibonacciMain.main(new String[]{"-1", "lookup"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testZeroNoMethod() throws Exception {
		FibonacciMain.main(new String[]{"0"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testZeroInteger() throws Exception {
		FibonacciMain.main(new String[]{"0", "loop"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testZeroRecursion() throws Exception {
		FibonacciMain.main(new String[]{"0", "recursion"});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testZeroLookup() throws Exception {
		FibonacciMain.main(new String[]{"0", "lookup"});
	}

	@Test(expected=ArithmeticException.class)
	public void testFortySevenNoMethod() throws Exception {
		FibonacciMain.main(new String[]{"46"});	// Expect OK
		FibonacciMain.main(new String[]{"47"});	// Expect Overflow
	}

	@Test(expected=ArithmeticException.class)
	public void testFortySevenInteger() throws Exception {
		FibonacciMain.main(new String[]{"46", "loop"});	// Expect OK
		FibonacciMain.main(new String[]{"47", "loop"});	// Expect Overflow
	}

	@Test(expected=ArithmeticException.class)
	public void testFortySevenRecursion() throws Exception {
		FibonacciMain.main(new String[]{"46", "recursion"});	// Expect OK
		FibonacciMain.main(new String[]{"47", "recursion"});	// Expect Overflow
	}

	@Test(expected=ArithmeticException.class)
	public void testFortySevenLookup() throws Exception {
		FibonacciMain.main(new String[]{"46", "lookup"});	// Expect OK
		FibonacciMain.main(new String[]{"47", "lookup"});	// Expect Overflow
	}
}