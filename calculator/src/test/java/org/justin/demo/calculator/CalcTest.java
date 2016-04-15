package org.justin.demo.calculator;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: Add more test cases for negative use cases (i.e. expected errors and/or exceptions).
 * @author justin.cranford
 */
@SuppressWarnings("static-method")
public final class CalcTest {

	@Test
	public void testExamples() throws Exception {
		Assert.assertEquals(3, Calc.compute("add(1, 2)"));
		Assert.assertEquals(7, Calc.compute("add(1, mult(2, 3))"));
		Assert.assertEquals(12, Calc.compute("mult(add(2, 2), div(9, 3))"));
		Assert.assertEquals(10, Calc.compute("let(a, 5, add(a, a))"));
		Assert.assertEquals(55, Calc.compute("let(a, 5, let(b, mult(a, 10), add(b, a)))"));
		Assert.assertEquals(40, Calc.compute("let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))"));
	}
	
	@Test
	public void testAdd() throws Exception {
		CalcTest.testValidTwoOperandOperation( 50, "add",  "25",  "25");
		CalcTest.testValidTwoOperandOperation(  0, "add",  "25", "-25");
		CalcTest.testValidTwoOperandOperation(  0, "add", "-25",  "25");
		CalcTest.testValidTwoOperandOperation(-50, "add", "-25", "-25");
	}

	@Test
	public void testSub() throws Exception {
		CalcTest.testValidTwoOperandOperation(  0, "sub",  "25",  "25");
		CalcTest.testValidTwoOperandOperation( 50, "sub",  "25", "-25");
		CalcTest.testValidTwoOperandOperation(-50, "sub", "-25",  "25");
		CalcTest.testValidTwoOperandOperation(  0, "sub", "-25", "-25");
	}

	@Test
	public void testMult() throws Exception {
		CalcTest.testValidTwoOperandOperation( 625, "mult",  "25",  "25");
		CalcTest.testValidTwoOperandOperation(-625, "mult",  "25", "-25");
		CalcTest.testValidTwoOperandOperation(-625, "mult", "-25",  "25");
		CalcTest.testValidTwoOperandOperation( 625, "mult", "-25", "-25");
	}

	@Test
	public void testDiv() throws Exception {
		CalcTest.testValidTwoOperandOperation(  1, "div",  "25",  "25");
		CalcTest.testValidTwoOperandOperation( -1, "div",  "25", "-25");
		CalcTest.testValidTwoOperandOperation( -1, "div", "-25",  "25");
		CalcTest.testValidTwoOperandOperation(  1, "div", "-25", "-25");
		CalcTest.testValidTwoOperandOperation(  5, "div",  "25",  "5");
		CalcTest.testValidTwoOperandOperation( -5, "div",  "25", "-5");
		CalcTest.testValidTwoOperandOperation( -5, "div", "-25",  "5");
		CalcTest.testValidTwoOperandOperation(  5, "div", "-25", "-5");
	}

	@Test
	public void testLet() throws Exception {
		CalcTest.testValidThreeOperandOperation( 25, "let",  "a",   "25",  "a");
		CalcTest.testValidThreeOperandOperation(-25, "let",  "A",  "-25",  "A");
		CalcTest.testValidThreeOperandOperation(  5, "let",  "a",   "25",  "5");
		CalcTest.testValidThreeOperandOperation( -5, "let",  "A",   "25", "-5");
	}

	@Test
	public void testAddRightMultiply() throws Exception {
		Assert.assertEquals(7, Calc.compute(" add( 1 , mult ( 2 , 3 ) ) "));
	}

	@Test
	public void testAddLeftMultiply() throws Exception {
		Assert.assertEquals(7, Calc.compute(" add( mult ( 2 , 3 ) , 1 ) "));
	}

	@Test
	public void testMinMaxIntegers() throws Exception {
		CalcTest.testValidTwoOperandOperationWithMinMax("add");
		CalcTest.testValidTwoOperandOperationWithMinMax("sub");
		CalcTest.testValidTwoOperandOperationWithMinMax("mult");
		CalcTest.testValidTwoOperandOperationWithMinMax("div");
	}

	@Test
	public void testTokenizer() throws Exception {
		// ASSUMPTION: Package visibility and no validation, just the ability to tokenize arbitrary operation and operand literals (with balanced parentheses) into a list.
		Assert.assertEquals(5, Calc.tokenizeExpression(" junk ( anything()()()()()(), something, more(), another ) ").size());
	}

	@Test(expected=Exception.class )
	public void testEmptyEquation() throws Exception {
		Calc.compute("");
	}

	@Test(expected=Exception.class )
	public void testBadOperation() throws Exception {
		Calc.compute("sum(1,2)");	// add() is valid, sum() is not recognized
	}

	@Test(expected=Exception.class )
	public void testMissingOperation() throws Exception {
		Calc.compute("(1,2)");
	}

	@Test(expected=Exception.class )
	public void testMissingOperand() throws Exception {
		Calc.compute("add(1,    )");	// add() is valid, sum() is not recognized
	}

	@Test(expected=Exception.class )
	public void testMissingVariable() throws Exception {
		Calc.compute("add(1,let(a,1,add(a,b)))");	// add() is valid, sum() is not recognized
	}

	@Test(expected=java.lang.ArithmeticException.class )
	public void testDivideByZero() throws Exception {
		Calc.compute("div(10,0)");	// add() is valid, sum() is not recognized
	}

	/**
	 * Tests the two-operand operation, including using 9 unique combinations of injected whitespace or no whitespace around the parsed tokens.
	 */
	private static void testValidTwoOperandOperation(final int expectedResult, final String operation, final Object operand1, final Object operand2) throws Exception {
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + " ) "));	// Example: " add ( 25 , 25 ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + " )"));	// Example: " add ( 25 , 25 )"
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + ") "));	// Example: " add ( 25 , 25) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " ," + operand2 + " ) "));	// Example: " add ( 25 ,25 ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + ", " + operand2 + " ) "));	// Example: " add ( 25, 25 ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " (" + operand1 + " , " + operand2 + " ) "));	// Example: " add (25 , 25 ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + "( " + operand1 + " , " + operand2 + " ) "));	// Example: " add( 25 , 25 ) "
		Assert.assertEquals(expectedResult, Calc.compute("" + operation + " ( " + operand1 + " , " + operand2 + " ) "));	// Example: "add ( 25 , 25 ) "
		Assert.assertEquals(expectedResult, Calc.compute("" + operation + "(" + operand1 + "," + operand2 + ")"));			// Example: "add(25,25)"
	}

	/**
	 * Tests the three-operand operation, including using 11 unique combinations of injected whitespace or no whitespace around the parsed tokens.
	 */
	private static void testValidThreeOperandOperation(final int expectedResult, final String operation, final Object operand1, final Object operand2, final Object operand3) throws Exception {
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + " , " + operand3 + " ) "));	// Example: " let ( a , 25 , a ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + " , " + operand3 + " )"));	// Example: " let ( a , 25 , a )"
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + " , " + operand3 + ") "));	// Example: " let ( a , 25 , a) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + " ," + operand3 + " ) "));	// Example: " let ( a , 25 ,a ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " , " + operand2 + ", " + operand3 + " ) "));	// Example: " let ( a , 25, a ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + " ," + operand2 + " , " + operand3 + " ) "));	// Example: " let ( a ,25 , a ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " ( " + operand1 + ", " + operand2 + " , " + operand3 + " ) "));	// Example: " let ( a, 25 , a ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + " (" + operand1 + " , " + operand2 + " , " + operand3 + " ) "));	// Example: " let (a , 25 , a ) "
		Assert.assertEquals(expectedResult, Calc.compute(" " + operation + "( " + operand1 + " , " + operand2 + " , " + operand3 + " ) "));	// Example: " let( a , 25 , a ) "
		Assert.assertEquals(expectedResult, Calc.compute("" + operation + " ( " + operand1 + " , " + operand2 + " , " + operand3 + " ) "));	// Example: "let( a , 25 , a ) "
		Assert.assertEquals(expectedResult, Calc.compute("" + operation + "(" + operand1 + "," + operand2 + "," + operand3 + ")"));			// Example: "let(a,25,a)"
	}

	/**
	 * Tests combinations of MIN and MAX integer used in operations. 
	 */
	private static void testValidTwoOperandOperationWithMinMax(final String operation) throws Exception {
		Assert.assertEquals(CalcTest.expected(operation,Integer.MIN_VALUE,Integer.MIN_VALUE), Calc.compute(operation + "(" + Integer.MIN_VALUE + "," + Integer.MIN_VALUE + ")"));
		Assert.assertEquals(CalcTest.expected(operation,Integer.MIN_VALUE,Integer.MAX_VALUE), Calc.compute(operation + "(" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + ")"));
		Assert.assertEquals(CalcTest.expected(operation,Integer.MAX_VALUE,Integer.MIN_VALUE), Calc.compute(operation + "(" + Integer.MAX_VALUE + "," + Integer.MIN_VALUE + ")"));
		Assert.assertEquals(CalcTest.expected(operation,Integer.MAX_VALUE,Integer.MAX_VALUE), Calc.compute(operation + "(" + Integer.MAX_VALUE + "," + Integer.MAX_VALUE + ")"));

		Assert.assertEquals(CalcTest.expected(operation,Integer.MIN_VALUE,  1), Calc.compute(operation + "(" + Integer.MIN_VALUE + ",  1)"));
		Assert.assertEquals(CalcTest.expected(operation,Integer.MAX_VALUE,  1), Calc.compute(operation + "(" + Integer.MAX_VALUE + ", +1)"));

		Assert.assertEquals(CalcTest.expected(operation,Integer.MIN_VALUE, -1), Calc.compute(operation + "(" + Integer.MIN_VALUE + ", -1)"));
		Assert.assertEquals(CalcTest.expected(operation,Integer.MAX_VALUE, -1), Calc.compute(operation + "(" + Integer.MAX_VALUE + ", -1)"));

		if (!operation.equals("div")) {
			Assert.assertEquals(CalcTest.expected(operation,Integer.MIN_VALUE,  0), Calc.compute(operation + "(" + Integer.MIN_VALUE + ",  0)"));
			Assert.assertEquals(CalcTest.expected(operation,Integer.MAX_VALUE,  0), Calc.compute(operation + "(" + Integer.MAX_VALUE + ",  0)"));
		}
	}

	/**
	 * Helper for testValidTwoOperandOperationWithMinMax() to compute expected value.
	 */
	private static int expected(final String operation, final int operand1, final int operand2) throws Exception {
		switch(operation) {
			case("add")  : return operand1 + operand2;
			case("sub")  : return operand1 - operand2;
			case("mult") : return operand1 * operand2;
			case("div")  : return operand1 / operand2;
			default: throw new Exception("unknown operator " + operation);
		}
	}
}