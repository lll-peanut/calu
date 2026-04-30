package com.peanut.calculator;

import org.springframework.stereotype.Component;

/**
 * 表达式求值器，支持：
 * - 基本四则运算：+  -  *  /  （包含正确的运算符优先级）
 * - 括号分组
 * - 一元负号（例如：-3、-(2+5)）
 * - 科学计算函数：sin, cos, tan, asin, acos, atan, sinh, cosh, tanh,
 * sqrt, cbrt, log, ln, exp, abs, ceil, floor, round,
 * 幂运算（使用 ^）、阶乘（使用 !）
 * - 常量：pi（π）、e
 * <p>
 * 解析优先级（从低到高）大致是：
 * 加减（expr） < 乘除模（term） < 一元正负（unary） < 幂运算（power） < 阶乘（factorial） < 原子（primary）
 * <p>
 * 1）expr（表达式，处理加减，最低优先级）
 * expr → term (('+' | '-') term)*
 * 含义：一个表达式 expr 先解析一个 term，后面可以接任意多个 “+ term” 或 “- term”。
 * 例子：1+2-3、3+2*(8-5)
 * <p>
 * 2）term（项，处理乘除模，优先级高于加减）
 * term → unary (('*' | '/' | '%') unary)*
 * 含义：一个 term 先解析一个 unary，后面可以接任意多个 “* unary” “/ unary” “% unary”。
 * 例子：2*3/4、1+2*3（这里 2*3 会先算）
 * <p>
 * 3）unary（一元运算，处理前置的正负号）
 * unary → '-' unary | power
 * 含义：如果当前位置是 '-'，就表示一元负号（例如 -3 或 -(2+5)），否则就按 power 去解析。
 * 也支持连续负号：--3 等价于 3。
 * <p>
 * 4）power（幂运算）
 * power → factorial ('^' unary)*
 * 含义：先解析一个 factorial 作为底数，然后如果遇到 '^'，再解析一个 unary 作为指数。
 * 例子：2^3、2^(-3)、2^3^2（你当前实现是右结合：2^(3^2)）
 * <p>
 * 5）factorial（阶乘）
 * factorial → primary '!'?
 * 含义：先解析一个 primary，然后可选地接一个 '!' 表示阶乘。
 * 例子：5!、(3+2)!
 * <p>
 * 6）primary（原子，最基本的单位）
 * primary → number | constant | function '(' expr ')' | '(' expr ')'
 * 含义：primary 可以是以下四种之一：
 * - number：数字，例如 12、3.14
 * - constant：常量，例如 pi、e
 * - function '(' expr ')'：函数调用，例如 sin(30)、sqrt(9)
 * 注意：函数括号里的参数也是一个完整的 expr，所以支持 sin(1+2*3)
 * - '(' expr ')'：括号表达式，例如 (1+2)*3
 */
@Component
public class ExpressionEvaluator {


    /**
     * 当前正在解析的表达式
     */
    private String expression;

    /**
     * 解析游标（下标），指向 expression 中“当前要处理的字符位置”。
     * 解析过程中每消费一个字符/Token，这个 pos 会不断递增。
     */
    private int pos;

    /**
     * 计算表达式并返回结果。
     *
     * @param expr 输入表达式（例如 "3+2*(8-5)"）
     * @return 计算结果
     * @throws IllegalArgumentException 当 expr 为 null/空字符串，或表达式语法不合法时抛出
     * @throws IllegalArgumentException 当出现除以 0、对负数开平方、阶乘参数非法等数学错误时抛出
     */
    public synchronized double evaluate(String expr) {
        if (expr == null || expr.isBlank()) {
            throw new IllegalArgumentException("expr 不能为 null 或空字符串");
        }
        // 归一化：移除空格，替换 × ÷ 和 π
        this.expression = expr.trim()
                .replace(" ", "")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "pi");
        this.pos = 0;
        double result = parseExpr();
        if (pos < this.expression.length()) {
            throw new IllegalArgumentException(
                    "表达式末尾存在无法识别/多余的内容（语法错误)在位置" + pos + ": '" + this.expression.charAt(pos) + "'");
        }
        return result;
    }

    /**
     * 解析规则：expr → term (('+' | '-') term)*
     * 含义：
     * - 先解析一个 term 作为左值
     * - 只要后面还有 + 或 -，就不断读取运算符并解析下一个 term，然后把结果累加/累减到 left 上
     *
     * @return 当前 expr 的计算结果
     */
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

    /**
     * 解析规则：term → unary (('*' | '/' | '%') unary)*
     * <p>
     * 含义：
     * - 先解析一个 unary 作为左值
     * - 只要后面还有 * / %，就不断读取运算符并解析下一个 unary，然后对 left 做乘/除/模
     *
     * @return 当前 term 的计算结果
     * @throws ArithmeticException 除 0 或取模 0 时抛出
     */
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
                    throw new ArithmeticException("不能除以0");
                }
                left /= divisor;
            } else if (c == '%') {
                pos++;
                double divisor = parseUnary();
                if (divisor == 0) {
                    throw new ArithmeticException("不能取模0");
                }
                left %= divisor;
            } else {
                break;
            }
        }
        return left;
    }

    /**
     * 解析规则：unary → ('+' | '-') unary | power
     * <p>
     * 含义：
     * - 处理一元正号/负号（前缀 + 或 -）
     * - 如果当前字符是 '-'，消费它并对后面的 unary 取相反数
     * - 如果当前字符是 '+'，消费它并继续解析后面的 unary（等价于什么都不做）
     * - 否则就交给 power 层解析（更高优先级的运算）
     * <p>
     * 例子：
     * - -3
     * - --3（会递归两次，最终得到 3）
     * - -(2+5)
     *
     * @return 当前 unary 的计算结果
     */
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

    /**
     * 解析规则：power → factorial ('^' unary)?
     * <p>
     * 含义：
     * - 先解析一个 factorial 作为底数 base
     * - 如果后面紧跟 '^'，则解析一个 unary 作为指数 exp，并计算 base^exp
     *
     * @return 当前 power 的计算结果
     */
    private double parsePower() {
        double base = parseFactorial();
        if (pos < expression.length() && expression.charAt(pos) == '^') {
            pos++;
            double exp = parseUnary(); // 右结合
            return Math.pow(base, exp);
        }
        return base;
    }

    /**
     * 解析规则：factorial → primary '!'?
     * <p>
     * 含义：
     * - 先解析一个 primary
     * - 如果后面紧跟 '!'，则对 primary 的结果做阶乘
     * <p>
     * 例子：
     * - 5!
     * - (3+2)!
     *
     * @return 当前 factorial 的计算结果
     * @throws ArithmeticException 阶乘参数不是非负整数或溢出时抛出
     */
    private double parseFactorial() {
        double value = parsePrimary();
        if (pos < expression.length() && expression.charAt(pos) == '!') {
            pos++;
            value = factorial(value);
        }
        return value;
    }

    /**
     * 解析规则：primary → number | constant | function '(' expr ')' | '(' expr ')'
     * <p>
     * 含义：primary 是表达式中最基础的“原子单位”，可能是：
     * - 括号表达式：(...)，用于改变优先级
     * - 数字字面量：12、3.14
     * - 常量：pi、e
     * - 函数调用：sin(expr)、sqrt(expr) 等
     *
     * @return 当前 primary 的计算结果
     * @throws IllegalArgumentException 遇到无法识别的字符或意外结束时抛出
     */
    private double parsePrimary() {
        if (pos >= expression.length()) {
            throw new IllegalArgumentException("Unexpected end of expression");
        }

        char c = expression.charAt(pos);

        if (c == '(') {
            pos++;
            double value = parseExpr();
            expect(')');
            return value;
        }

        if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        }

        if (Character.isLetter(c)) {
            return parseFunctionOrConstant();
        }

        throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
    }

    /**
     * 解析数字字面量（支持整数和小数）。
     * <p>
     * 解析规则：
     * - 从当前 pos 开始，连续读取数字或 '.'，直到遇到非数字/非 '.' 字符停止。
     * <p>
     * 注意：
     * - 这里允许出现多个 '.' 但最终 Double.parseDouble 会报错并被捕获，从而抛出 IllegalArgumentException。
     *
     * @return 解析得到的 double 数值
     * @throws IllegalArgumentException 数字格式不正确时抛出
     */
    private double parseNumber() {
        int start = pos;
        while (pos < expression.length()
                && (Character.isDigit(expression.charAt(pos)) || expression.charAt(pos) == '.')) {
            pos++;
        }
        String numStr = expression.substring(start, pos);
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + numStr);
        }
    }

    /**
     * 解析“函数调用”或“常量”。
     * <p>
     * 处理流程：
     * 1）从当前位置开始读取连续字母，得到 name（函数名或常量名）
     * 2）若 name 是常量（pi 或 e），直接返回常量值（注意 e(...) 作为函数时另行处理）
     * 3）否则按函数处理：必须跟随 '(' expr ')' 作为参数
     * 4）根据函数名调用对应的 Math 方法计算并返回
     *
     * @return 函数调用或常量的数值
     * @throws IllegalArgumentException 未知函数名、括号缺失等语法错误时抛出
     * @throws ArithmeticException      sqrt/log/ln 等遇到非法参数时抛出
     */
    private double parseFunctionOrConstant() {
        int start = pos;
        while (pos < expression.length() && Character.isLetter(expression.charAt(pos))) {
            pos++;
        }
        String name = expression.substring(start, pos).toLowerCase();

        // 常量
        if ("pi".equals(name)) return Math.PI;
        if ("e".equals(name) && (pos >= expression.length() || expression.charAt(pos) != '(')) return Math.E;

        // 函数：都要求括号参数
        expect('(');
        double arg = parseExpr();
        expect(')');

        return switch (name) {
            // ✅ 修复：sin/cos/tan 统一按“弧度制”计算（与 pi 常量语义一致）
            case "sin" -> Math.sin(arg);
            case "cos" -> Math.cos(arg);
            case "tan" -> Math.tan(arg);

            // 反三角函数：保持你原来的行为（返回角度）
            case "asin" -> Math.toDegrees(Math.asin(arg));
            case "acos" -> Math.toDegrees(Math.acos(arg));
            case "atan" -> Math.toDegrees(Math.atan(arg));

            case "sinh" -> Math.sinh(arg);
            case "cosh" -> Math.cosh(arg);
            case "tanh" -> Math.tanh(arg);
            case "sqrt" -> {
                if (arg < 0) throw new ArithmeticException("sqrt of negative number");
                yield Math.sqrt(arg);
            }
            case "cbrt" -> Math.cbrt(arg);
            case "log" -> {
                if (arg <= 0) throw new ArithmeticException("log of non-positive number");
                yield Math.log10(arg);
            }
            case "ln" -> {
                if (arg <= 0) throw new ArithmeticException("ln of non-positive number");
                yield Math.log(arg);
            }
            case "exp" -> Math.exp(arg);
            case "abs" -> Math.abs(arg);
            case "ceil" -> Math.ceil(arg);
            case "floor" -> Math.floor(arg);
            case "round" -> (double) Math.round(arg);

            // 角度/弧度显式转换（保留）
            case "deg" -> Math.toDegrees(arg);
            case "rad" -> Math.toRadians(arg);

            // e(x) 视为 exp(x)
            case "e" -> Math.exp(arg);

            default -> throw new IllegalArgumentException("Unknown function: " + name);
        };
    }

    /**
     * 消费并校验一个期望字符。
     * <p>
     * 用途：
     * - 用来匹配括号、函数参数括号等必须出现的符号
     * - 如果当前位置不是期望字符，则抛出异常并指出错误位置，便于定位语法问题
     *
     * @param expected 期望的字符（例如 ')' 或 '('）
     * @throws IllegalArgumentException 当当前位置不是 expected 时抛出
     */
    private void expect(char expected) {
        if (pos >= expression.length() || expression.charAt(pos) != expected) {
            String got = pos < expression.length() ? "'" + expression.charAt(pos) + "'" : "end of input";
            throw new IllegalArgumentException(
                    "Expected '" + expected + "' but got " + got + " at position " + pos);
        }
        pos++;
    }

    /**
     * 计算阶乘 n!（仅支持非负整数）。
     * <p>
     * 规则：
     * - n 必须是非负整数（例如 0、1、2、3...）
     * - 若 n 为小数或负数，抛出 ArithmeticException
     * <p>
     * 注意：
     * - 这里用 long 逐步相乘，n 较大时会溢出；检测到溢出后抛异常
     *
     * @param n 阶乘参数
     * @return n! 的结果（用 double 返回，内部使用 long 计算）
     * @throws ArithmeticException 当 n 不是非负整数或溢出时抛出
     */
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
     * 将 double 结果格式化为更适合显示的字符串：
     * - NaN 显示为 "Error"
     * - Infinity 显示为 "Infinity" 或 "-Infinity"
     * - 对非常接近 0 的值做“归零处理”（避免浮点误差显示 0.0000000001）
     * - 最多保留 10 位小数，并去掉多余的尾随 0 与尾随小数点
     *
     * @param value 要格式化的数值
     * @return 格式化后的字符串
     */
    public static String format(double value) {
        if (Double.isNaN(value)) return "Error";
        if (Double.isInfinite(value)) return value > 0 ? "Infinity" : "-Infinity";

        // 处理浮点误差：极接近 0 的值直接显示为 0
        if (Math.abs(value) < 1e-12) value = 0.0;

        String s = String.format("%.10f", value);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            s = s.replaceAll("\\.$", "");
        }
        return s;
    }
}