package com.peanut.calculator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {
    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();

    static {
        PRECEDENCE.put("+", 1);
        PRECEDENCE.put("-", 1);
        PRECEDENCE.put("*", 2);
        PRECEDENCE.put("/", 2);
    }

    public double evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression is empty");
        }
        List<Token> tokens = tokenize(expression);
        List<Token> rpn = toRpn(tokens);
        return evaluateRpn(rpn);
    }

    private List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        int length = expression.length();
        TokenType previousType = null;
        int index = 0;
        while (index < length) {
            char current = expression.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }

            if (isUnaryOperator(previousType) && (current == '-' || current == '+')) {
                int nextIndex = nextNonSpaceIndex(expression, index + 1);
                if (nextIndex == -1) {
                    throw new IllegalArgumentException("Dangling unary operator");
                }
                char nextChar = expression.charAt(nextIndex);
                if (nextChar == '(') {
                    if (current == '-') {
                        previousType = addToken(tokens, previousType, new Token("0", TokenType.NUMBER));
                        previousType = addToken(tokens, previousType, new Token("-", TokenType.OPERATOR));
                    }
                    index++;
                    continue;
                }
                if (Character.isDigit(nextChar) || nextChar == '.') {
                    ParseResult result = parseNumber(expression, index);
                    previousType = addToken(tokens, previousType, new Token(result.value, TokenType.NUMBER));
                    index = result.nextIndex;
                    continue;
                }
            }

            if (Character.isDigit(current) || current == '.') {
                ParseResult result = parseNumber(expression, index);
                previousType = addToken(tokens, previousType, new Token(result.value, TokenType.NUMBER));
                index = result.nextIndex;
                continue;
            }

            if (current == '+' || current == '-' || current == '*' || current == '/') {
                previousType = addToken(tokens, previousType, new Token(String.valueOf(current), TokenType.OPERATOR));
                index++;
                continue;
            }

            if (current == '(') {
                previousType = addToken(tokens, previousType, new Token("(", TokenType.LEFT_PAREN));
                index++;
                continue;
            }

            if (current == ')') {
                tokens.add(new Token(")", TokenType.RIGHT_PAREN));
                previousType = TokenType.RIGHT_PAREN;
                index++;
                continue;
            }

            throw new IllegalArgumentException("Invalid character: " + current);
        }

        return tokens;
    }

    private List<Token> toRpn(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> operators = new ArrayDeque<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER -> output.add(token);
                case OPERATOR -> {
                    while (!operators.isEmpty() && operators.peek().type == TokenType.OPERATOR
                        && PRECEDENCE.get(operators.peek().value) >= PRECEDENCE.get(token.value)) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                }
                case LEFT_PAREN -> operators.push(token);
                case RIGHT_PAREN -> {
                    while (!operators.isEmpty() && operators.peek().type != TokenType.LEFT_PAREN) {
                        output.add(operators.pop());
                    }
                    if (operators.isEmpty() || operators.peek().type != TokenType.LEFT_PAREN) {
                        throw new IllegalArgumentException("Mismatched parentheses");
                    }
                    operators.pop();
                }
            }
        }
        while (!operators.isEmpty()) {
            Token operator = operators.pop();
            if (operator.type == TokenType.LEFT_PAREN || operator.type == TokenType.RIGHT_PAREN) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(operator);
        }
        return output;
    }

    private double evaluateRpn(List<Token> rpn) {
        Deque<Double> stack = new ArrayDeque<>();
        for (Token token : rpn) {
            if (token.type == TokenType.NUMBER) {
                stack.push(Double.parseDouble(token.value));
                continue;
            }
            if (token.type == TokenType.OPERATOR) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid expression");
                }
                double right = stack.pop();
                double left = stack.pop();
                double result = switch (token.value) {
                    case "+" -> left + right;
                    case "-" -> left - right;
                    case "*" -> left * right;
                    case "/" -> {
                        if (right == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        yield left / right;
                    }
                    default -> throw new IllegalArgumentException("Unknown operator: " + token.value);
                };
                stack.push(result);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }
        return stack.pop();
    }

    private boolean isUnaryOperator(TokenType previousType) {
        return previousType == null || previousType == TokenType.OPERATOR || previousType == TokenType.LEFT_PAREN;
    }

    private int nextNonSpaceIndex(String expression, int startIndex) {
        for (int i = startIndex; i < expression.length(); i++) {
            if (!Character.isWhitespace(expression.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private ParseResult parseNumber(String expression, int startIndex) {
        StringBuilder builder = new StringBuilder();
        int index = startIndex;
        char first = expression.charAt(index);
        if (first == '-' || first == '+') {
            builder.append(first);
            index++;
        }
        boolean hasDigit = false;
        boolean hasDot = false;
        while (index < expression.length()) {
            char current = expression.charAt(index);
            if (Character.isDigit(current)) {
                builder.append(current);
                hasDigit = true;
                index++;
                continue;
            }
            if (current == '.' && !hasDot) {
                builder.append(current);
                hasDot = true;
                index++;
                continue;
            }
            break;
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Invalid number");
        }
        return new ParseResult(builder.toString(), index);
    }

    private TokenType addToken(List<Token> tokens, TokenType previousType, Token token) {
        if (needsImplicitMultiplication(previousType, token.type)) {
            tokens.add(new Token("*", TokenType.OPERATOR));
        }
        tokens.add(token);
        return token.type;
    }

    private boolean needsImplicitMultiplication(TokenType previousType, TokenType nextType) {
        return (previousType == TokenType.NUMBER || previousType == TokenType.RIGHT_PAREN)
            && (nextType == TokenType.NUMBER || nextType == TokenType.LEFT_PAREN);
    }

    private record ParseResult(String value, int nextIndex) {
    }

    private record Token(String value, TokenType type) {
    }

    private enum TokenType {
        NUMBER,
        OPERATOR,
        LEFT_PAREN,
        RIGHT_PAREN
    }
}
