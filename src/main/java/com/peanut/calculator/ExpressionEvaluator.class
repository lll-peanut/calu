package com.peanut.calculator;

import org.springframework.stereotype.Component;

/**
 * Expression evaluator that supports:
 *  - Basic arithmetic: +  -  *  /  with proper operator precedence
 *  - Parentheses grouping
 *  - Unary minus  (e.g. -3, -(2+5))
 *  - Scientific functions: sin, cos, tan, asin, acos, atan, sinh, cosh, tanh,
 *                          sqrt, cbrt, log, ln, exp, abs, ceil, floor, round,
 *                          pow (via ^), factorial (via !)
 *  - Constants: pi (π), e
 *
 * Grammar (recursive descent):
 *   expr     → term   (('+' | '-') term)*
 *   term     → unary  (('*' | '/') unary)*
 *   unary    → '-' unary | power
 *   power    → factorial ('^' unary)*
 *   factorial→ primary '!'?
 *   primary  → number | constant | function '(' expr ')' | '(' expr ')'
 */
@Component
public class ExpressionEvaluator {

    private String expression;
    private int pos;

    /**
     * Evaluates a mathematical expression string and returns the result.
     *
     * @param expr the expression to evaluate (e.g. "3+2*(8-5)")
     * @return the numeric result
     * @throws ArithmeticException   on division by zero
     * @throws IllegalArgumentException on invalid syntax
     */
    public synchronized double evaluate(String expr) {
        if (expr == null || expr.isBlank()) {
            throw new IllegalArgumentException("Expression is empty");
        }
        // Normalise: remove all whitespace, replace × with *, ÷ with /
        this.expression = expr.trim()
                .replace(" ", "")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "pi");
        this.pos = 0;
        double result = parseExpr();
        if (pos < this.expression.length()) {
            throw new IllegalArgumentException(
                    "Unexpected character at position " + pos + ": '" + this.expression.charAt(pos) + "'");
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Recursive descent parser
    // -----------------------------------------------------------------------

    /** expr → term (('+' | '-') term)* */
    private double parseExpr() {
        double left = parseTerm();
        while (pos < expression.length()) {
            char c = expression.charAt(pos);
            if (c == '+') {
                pos++;
                left += parseTerm();
            } else if (c == '-') {
                pos++;
                left -= parseTerm();
            } else {
                break;
            }
        }
        return left;
    }

    /** term → unary (('*' | '/' | '%') unary)* */
    private double parseTerm() {
        double left = parseUnary();
        while (pos < expression.length()) {
            char c = expression.charAt(pos);
            if (c == '*') {
                pos++;
                left *= parseUnary();
            } else if (c == '/') {
                pos++;
                double divisor = parseUnary();
                if (divisor == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                left /= divisor;
            } else if (c == '%') {
                pos++;
                double divisor = parseUnary();
                if (divisor == 0) {
                    throw new ArithmeticException("Modulo by zero");
                }
                left %= divisor;
            } else {
                break;
            }
        }
        return left;
    }

    /** unary → '-' unary | power */
    private double parseUnary() {
        if (pos < expression.length() && expression.charAt(pos) == '-') {
            pos++;
            return -parseUnary();
        }
        if (pos < expression.length() && expression.charAt(pos) == '+') {
            pos++;
            return parseUnary();
        }
        return parsePower();
    }

    /** power → factorial ('^' unary)* */
    private double parsePower() {
        double base = parseFactorial();
        if (pos < expression.length() && expression.charAt(pos) == '^') {
            pos++;
            double exp = parseUnary(); // right-associative
            return Math.pow(base, exp);
        }
        return base;
    }

    /** factorial → primary '!'? */
    private double parseFactorial() {
        double value = parsePrimary();
        if (pos < expression.length() && expression.charAt(pos) == '!') {
            pos++;
            value = factorial(value);
        }
        return value;
    }

    /** primary → number | constant | function '(' expr ')' | '(' expr ')' */
    private double parsePrimary() {
        if (pos >= expression.length()) {
            throw new IllegalArgumentException("Unexpected end of expression");
        }

        char c = expression.charAt(pos);

        // Parenthesised sub-expression
        if (c == '(') {
            pos++;
            double value = parseExpr();
            expect(')');
            return value;
        }

        // Number literal
        if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        }

        // Named function or constant
        if (Character.isLetter(c)) {
            return parseFunctionOrConstant();
        }

        throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
    }

    /** Parse a numeric literal (integer or decimal). */
    private double parseNumber() {
        int start = pos;
        while (pos < expression.length() && (Character.isDigit(expression.charAt(pos)) || expression.charAt(pos) == '.')) {
            pos++;
        }
        String numStr = expression.substring(start, pos);
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + numStr);
        }
    }

    /** Parse a named function call (e.g. sin(x)) or a constant (e.g. pi, e). */
    private double parseFunctionOrConstant() {
        int start = pos;
        while (pos < expression.length() && Character.isLetter(expression.charAt(pos))) {
            pos++;
        }
        String name = expression.substring(start, pos).toLowerCase();

        // Constants
        if ("pi".equals(name)) return Math.PI;
        if ("e".equals(name) && (pos >= expression.length() || expression.charAt(pos) != '(')) return Math.E;

        // Functions – all expect a parenthesised argument
        expect('(');
        double arg = parseExpr();
        expect(')');

        return switch (name) {
            case "sin"   -> Math.sin(toRadians(arg));
            case "cos"   -> Math.cos(toRadians(arg));
            case "tan"   -> Math.tan(toRadians(arg));
            case "asin"  -> Math.toDegrees(Math.asin(arg));
            case "acos"  -> Math.toDegrees(Math.acos(arg));
            case "atan"  -> Math.toDegrees(Math.atan(arg));
            case "sinh"  -> Math.sinh(arg);
            case "cosh"  -> Math.cosh(arg);
            case "tanh"  -> Math.tanh(arg);
            case "sqrt"  -> {
                if (arg < 0) throw new ArithmeticException("sqrt of negative number");
                yield Math.sqrt(arg);
            }
            case "cbrt"  -> Math.cbrt(arg);
            case "log"   -> {
                if (arg <= 0) throw new ArithmeticException("log of non-positive number");
                yield Math.log10(arg);
            }
            case "ln"    -> {
                if (arg <= 0) throw new ArithmeticException("ln of non-positive number");
                yield Math.log(arg);
            }
            case "exp"   -> Math.exp(arg);
            case "abs"   -> Math.abs(arg);
            case "ceil"  -> Math.ceil(arg);
            case "floor" -> Math.floor(arg);
            case "round" -> (double) Math.round(arg);
            case "deg"   -> Math.toDegrees(arg);
            case "rad"   -> Math.toRadians(arg);
            case "e"     -> Math.exp(arg);   // e(x) treated as exp(x)
            default -> throw new IllegalArgumentException("Unknown function: " + name);
        };
    }

    /** Consume the expected character, throw if it differs. */
    private void expect(char expected) {
        if (pos >= expression.length() || expression.charAt(pos) != expected) {
            String got = pos < expression.length() ? "'" + expression.charAt(pos) + "'" : "end of input";
            throw new IllegalArgumentException(
                    "Expected '" + expected + "' but got " + got + " at position " + pos);
        }
        pos++;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Converts degrees to radians.  The calculator operates in degree mode for
     * trigonometric functions (matching the Windows calculator behaviour).
     */
    private double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    /** Computes n! for non-negative integers. */
    private double factorial(double n) {
        if (n < 0 || n != Math.floor(n)) {
            throw new ArithmeticException("Factorial is only defined for non-negative integers");
        }
        long result = 1;
        for (long i = 2; i <= (long) n; i++) {
            result *= i;
            if (result < 0) throw new ArithmeticException("Factorial overflow");
        }
        return result;
    }

    /**
     * Formats a double result for display: removes unnecessary trailing zeros
     * after the decimal point, keeping up to 10 significant decimal digits.
     */
    public static String format(double value) {
        if (Double.isNaN(value)) return "Error";
        if (Double.isInfinite(value)) return value > 0 ? "Infinity" : "-Infinity";
        // Use BigDecimal-style stripping
        String s = String.format("%.10f", value);
        // Strip trailing zeros after decimal point
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            s = s.replaceAll("\\.$", "");
        }
        return s;
    }
}
