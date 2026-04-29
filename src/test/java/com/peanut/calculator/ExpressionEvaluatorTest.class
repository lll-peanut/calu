package com.peanut.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ExpressionEvaluator}.
 */
class ExpressionEvaluatorTest {

    private ExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ExpressionEvaluator();
    }

    // -----------------------------------------------------------------------
    // Basic arithmetic
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Addition")
    void testAddition() {
        assertEquals(5.0, evaluator.evaluate("3+2"), 1e-10);
    }

    @Test
    @DisplayName("Subtraction")
    void testSubtraction() {
        assertEquals(1.0, evaluator.evaluate("3-2"), 1e-10);
    }

    @Test
    @DisplayName("Multiplication")
    void testMultiplication() {
        assertEquals(6.0, evaluator.evaluate("3*2"), 1e-10);
    }

    @Test
    @DisplayName("Division")
    void testDivision() {
        assertEquals(1.5, evaluator.evaluate("3/2"), 1e-10);
    }

    @Test
    @DisplayName("Division by zero throws ArithmeticException")
    void testDivisionByZero() {
        assertThrows(ArithmeticException.class, () -> evaluator.evaluate("5/0"));
    }

    // -----------------------------------------------------------------------
    // Operator precedence
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Multiplication before addition")
    void testPrecedence() {
        // 2 + 3 * 4 = 14, not 20
        assertEquals(14.0, evaluator.evaluate("2+3*4"), 1e-10);
    }

    @Test
    @DisplayName("Division before subtraction")
    void testPrecedenceDivSub() {
        // 10 - 4 / 2 = 8, not 3
        assertEquals(8.0, evaluator.evaluate("10-4/2"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Parentheses
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Parentheses override precedence")
    void testParentheses() {
        // (2+3)*4 = 20
        assertEquals(20.0, evaluator.evaluate("(2+3)*4"), 1e-10);
    }

    @Test
    @DisplayName("Nested parentheses from problem statement: 3+2*(8-5)")
    void testProblemStatementExample() {
        // The problem statement example: 3+2*(8-5) = 3+2*3 = 9
        assertEquals(9.0, evaluator.evaluate("3+2*(8-5)"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Unary minus
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Unary minus on a number")
    void testUnaryMinus() {
        assertEquals(-5.0, evaluator.evaluate("-5"), 1e-10);
    }

    @Test
    @DisplayName("Unary minus with expression")
    void testUnaryMinusExpr() {
        assertEquals(-7.0, evaluator.evaluate("-(3+4)"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Power operator
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Modulo operator %")
    void testModulo() {
        assertEquals(1.0, evaluator.evaluate("10%3"), 1e-10);
    }

    @Test
    @DisplayName("Modulo by zero throws ArithmeticException")
    void testModuloByZero() {
        assertThrows(ArithmeticException.class, () -> evaluator.evaluate("5%0"));
    }

    @Test
    @DisplayName("Power operator ^")
    void testPower() {
        assertEquals(8.0, evaluator.evaluate("2^3"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Factorial
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Factorial 5!")
    void testFactorial() {
        assertEquals(120.0, evaluator.evaluate("5!"), 1e-10);
    }

    @Test
    @DisplayName("Factorial of 0 is 1")
    void testFactorialZero() {
        assertEquals(1.0, evaluator.evaluate("0!"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Scientific functions
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("sqrt(4) = 2")
    void testSqrt() {
        assertEquals(2.0, evaluator.evaluate("sqrt(4)"), 1e-10);
    }

    @Test
    @DisplayName("sqrt of negative number throws ArithmeticException")
    void testSqrtNegative() {
        assertThrows(ArithmeticException.class, () -> evaluator.evaluate("sqrt(-1)"));
    }

    @Test
    @DisplayName("sin(90) ≈ 1 (degree mode)")
    void testSin() {
        assertEquals(1.0, evaluator.evaluate("sin(90)"), 1e-10);
    }

    @Test
    @DisplayName("cos(0) = 1 (degree mode)")
    void testCos() {
        assertEquals(1.0, evaluator.evaluate("cos(0)"), 1e-10);
    }

    @Test
    @DisplayName("tan(45) ≈ 1 (degree mode)")
    void testTan() {
        assertEquals(1.0, evaluator.evaluate("tan(45)"), 1e-6);
    }

    @Test
    @DisplayName("log(100) = 2")
    void testLog() {
        assertEquals(2.0, evaluator.evaluate("log(100)"), 1e-10);
    }

    @Test
    @DisplayName("ln(e) = 1")
    void testLn() {
        assertEquals(1.0, evaluator.evaluate("ln(e)"), 1e-10);
    }

    @Test
    @DisplayName("exp(0) = 1")
    void testExp() {
        assertEquals(1.0, evaluator.evaluate("exp(0)"), 1e-10);
    }

    @Test
    @DisplayName("abs(-7) = 7")
    void testAbs() {
        assertEquals(7.0, evaluator.evaluate("abs(-7)"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("pi constant")
    void testPi() {
        assertEquals(Math.PI, evaluator.evaluate("pi"), 1e-10);
    }

    @Test
    @DisplayName("e constant")
    void testE() {
        assertEquals(Math.E, evaluator.evaluate("e"), 1e-10);
    }

    // -----------------------------------------------------------------------
    // Format helper
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("format integer result without trailing decimals")
    void testFormatInteger() {
        assertEquals("9", ExpressionEvaluator.format(9.0));
    }

    @Test
    @DisplayName("format decimal result")
    void testFormatDecimal() {
        assertEquals("1.5", ExpressionEvaluator.format(1.5));
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Empty expression throws IllegalArgumentException")
    void testEmptyExpression() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(""));
    }

    @Test
    @DisplayName("Blank expression throws IllegalArgumentException")
    void testBlankExpression() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("   "));
    }

    @Test
    @DisplayName("Invalid expression throws IllegalArgumentException")
    void testInvalidExpression() {
        // '@' is not a valid character
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate("2@3"));
    }
}
