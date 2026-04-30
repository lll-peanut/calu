package com.peanut.calculator;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * 计算器主界面，使用 Swing 构建，包含：
 * - 标题区： 窗口标题为“计算器”，可随背景色调整风格
 * - 菜单栏：查看（切换模式）、编辑（复制/粘贴）、帮助（关于）
 * - 显示区：主显示框（显示当前输入或结果）和历史记录标签（显示刚计算的表达式）
 * - 按钮区：标准按钮（数字、基本运算符、清除等）和科学按钮（函数、常
 * 常数等）
 * - 键盘支持：数字、运算符、Enter（=）、Backspace（⌫）、Esc（C）等快捷键
 */
@Component
public class CalculatorFrame extends JFrame {

    // 注入表达式求值器
    private final ExpressionEvaluator evaluator;

    // 主显示框
    private JTextField display;

    // 历史标签
    private JLabel historyLabel;

    // 标记当前显示是否是“结果/提示”（包括错误提示）
    private boolean showingResult = false;

    // 统一错误提示文本（UI 层统一用中文，避免 Error 混进表达式）
    private static final String ERR_GENERIC = "错误";
    private static final String ERR_INVALID = "无效表达式";
    private static final String ERR_DIV0 = "不能除以零";

    public CalculatorFrame(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
        initUI();
    }

    // ------------------------- UI 构建 -------------------------

    private void initUI() {
        setTitle("计算器");

        // 关闭程序窗口时退出应用；大小固定，禁止调整大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 创建菜单栏（查看/编辑/帮助）
        setJMenuBar(buildMenuBar());

        // 创建根容器
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(32, 32, 32));
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // 显示区（包含历史记录和当前输入/结果）
        root.add(buildDisplayPanel(), BorderLayout.NORTH);

        // 按钮区（包括标准区和科学区）
        JPanel buttons = new JPanel(new GridLayout(1, 2, 2, 0));
        buttons.setBackground(new Color(32, 32, 32));
        buttons.add(buildScientificPanel());
        buttons.add(buildStandardPanelClean());
        root.add(buttons, BorderLayout.CENTER);

        // 将根容器设置为窗口内容，调整大小以适应内容，并居中显示
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        // 窗口显示后再请求焦点更稳定
        display.setFocusable(true);
        SwingUtilities.invokeLater(this::refocusDisplay);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBackground(new Color(45, 45, 45));
        menuBar.setForeground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JMenu viewMenu = styledMenu("查看");
        JMenuItem standardItem = styledMenuItem("标准型");
        standardItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "当前已是标准型模式", "查看", JOptionPane.INFORMATION_MESSAGE));
        viewMenu.add(standardItem);

        JMenu editMenu = styledMenu("编辑");

        JMenuItem copyItem = styledMenuItem("复制 (Ctrl+C)");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        copyItem.addActionListener(e -> copyToClipboard());

        JMenuItem pasteItem = styledMenuItem("粘贴 (Ctrl+V)");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        pasteItem.addActionListener(e -> pasteFromClipboard());

        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        JMenu helpMenu = styledMenu("帮助");
        JMenuItem aboutItem = styledMenuItem("关于");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "简易计算器 v1.0\n作者：com.peanut\n基于 Spring Boot + Swing",
                "关于", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(viewMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JMenu styledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setOpaque(true);
        menu.setBackground(new Color(45, 45, 45));
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("微软雅黑", Font.PLAIN, 13));

        // 下拉弹出层设背景
        JPopupMenu popup = menu.getPopupMenu();
        popup.setOpaque(true);
        popup.setBackground(new Color(45, 45, 45));
        popup.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

        return menu;
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);

        // 使用自定义背景/前景
        item.setOpaque(true);
        item.setBackground(new Color(55, 55, 55));
        item.setForeground(Color.WHITE);
        item.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        item.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        item.setContentAreaFilled(true);

        return item;
    }

    private JPanel buildDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 0));
        panel.setBorder(new EmptyBorder(12, 12, 8, 12));

        historyLabel = new JLabel(" ");
        historyLabel.setForeground(Color.WHITE);
        historyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(historyLabel, BorderLayout.NORTH);

        display = new JTextField("0");
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setEditable(true);

        // 安装输入过滤器（修复前导零、错误覆盖、结果覆盖等）
        installDocumentFilter();
        // 安装快捷键（只接管功能键）
        installKeyBindings();

        // 显示框样式：黑底白字
        display.setBackground(new Color(0, 0, 0));
        display.setForeground(Color.WHITE);
        display.setCaretColor(Color.WHITE);
        display.setSelectionColor(new Color(70, 70, 70));
        display.setSelectedTextColor(Color.WHITE);
        display.setDisabledTextColor(Color.WHITE);

        display.setFont(new Font("微软雅黑", Font.PLAIN, 42));
        display.setBorder(new EmptyBorder(2, 0, 2, 0));
        display.setOpaque(true);

        display.setFocusable(true);
        panel.add(display, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------- 标准按钮面板 -------------------------

    private JPanel buildStandardPanelClean() {
        JPanel panel = new JPanel(new GridLayout(6, 4, 2, 2));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(new EmptyBorder(2, 2, 4, 4));

        // 第 1 行：%  CE  C  ⌫
        panel.add(opBtn("%"));
        panel.add(clearBtn("CE"));
        panel.add(clearBtn("C"));
        panel.add(clearBtn("⌫"));

        // 第 2 行：1/x  x²  √x  ÷
        panel.add(sciSmallBtn("1/x"));
        panel.add(sciSmallBtn("x²"));
        panel.add(sciSmallBtn("√x"));
        panel.add(opBtn("÷"));

        // 第 3 行：7  8  9  ×
        panel.add(numBtn("7"));
        panel.add(numBtn("8"));
        panel.add(numBtn("9"));
        panel.add(opBtn("×"));

        // 第 4 行：4  5  6  -
        panel.add(numBtn("4"));
        panel.add(numBtn("5"));
        panel.add(numBtn("6"));
        panel.add(opBtn("-"));

        // 第 5 行：1  2  3  +
        panel.add(numBtn("1"));
        panel.add(numBtn("2"));
        panel.add(numBtn("3"));
        panel.add(opBtn("+"));

        // 第 6 行：±  0  .  =
        panel.add(opBtn("±"));
        panel.add(numBtn("0"));
        panel.add(numBtn("."));
        panel.add(equalBtn());

        return panel;
    }

    // ------------------------- 科学按钮面板 -------------------------

    private JPanel buildScientificPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 4, 2, 2));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(new EmptyBorder(2, 4, 4, 2));

        String[][] sciButtons = {
                {"(", ")", "n!", "mod"},
                {"sin", "cos", "tan", "π"},
                {"asin", "acos", "atan", "e"},
                {"sinh", "cosh", "tanh", "x^y"},
                {"ln", "log", "exp", "√"},
                {"abs", "ceil", "floor", "1/x"}
        };

        for (String[] row : sciButtons) {
            for (String label : row) {
                panel.add(sciBtn(label));
            }
        }

        return panel;
    }

    // ------------------------- 按钮工厂方法 -------------------------

    private JButton numBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(51, 51, 51), new Color(68, 68, 68));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.addActionListener(e -> {
            onDigit(label);
            refocusDisplay();
        });
        return btn;
    }

    private JButton opBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(45, 45, 45), new Color(65, 65, 65));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.addActionListener(e -> {
            onOperator(label);
            refocusDisplay();
        });
        return btn;
    }

    private JButton equalBtn() {
        JButton btn = new JButton("=");
        styleButton(btn, new Color(0, 120, 215), new Color(30, 150, 240));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.addActionListener(e -> {
            onEqual();
            refocusDisplay();
        });
        return btn;
    }

    private JButton clearBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(45, 45, 45), new Color(65, 65, 65));
        btn.setForeground(new Color(230, 100, 80));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.addActionListener(e -> {
            onClear(label);
            refocusDisplay();
        });
        return btn;
    }

    private JButton sciBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(38, 38, 38), new Color(58, 58, 58));
        btn.setForeground(new Color(220, 220, 220));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.addActionListener(e -> {
            onScientific(label);
            refocusDisplay();
        });
        return btn;
    }

    private JButton sciSmallBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(45, 45, 45), new Color(65, 65, 65));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.addActionListener(e -> {
            onScientific(label);
            refocusDisplay();
        });
        return btn;
    }

    private void styleButton(JButton btn, Color normal, Color hover) {
        btn.setBackground(normal);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(normal);
            }
        });
    }

    // ------------------------- 输入与快捷键（重构核心） -------------------------

    /**
     * 把焦点拉回输入框，保证用户点按钮后仍然可以继续用键盘输入/快捷键。
     */
    private void refocusDisplay() {
        if (display == null) return;
        display.requestFocusInWindow();
        display.setCaretPosition(display.getText().length());
    }

    /**
     * 判断当前 display 文本是否为“错误提示文本”。
     * 说明：兼容英文 "Error"，避免旧格式残留导致被当作表达式的一部分。
     */
    private boolean isErrorText(String s) {
        if (s == null) return false;
        return ERR_GENERIC.equals(s) || ERR_INVALID.equals(s) || ERR_DIV0.equals(s) || "Error".equalsIgnoreCase(s);
    }

    /**
     * 统一设置显示内容，并同步设置 showingResult。
     * asResult=true 表示当前内容是“结果/提示”（包括错误提示），后续输入数字应覆盖而不是追加。
     */
    private void setDisplayText(String text, boolean asResult) {
        display.setText(text);
        showingResult = asResult;
    }

    /**
     * 安装 DocumentFilter，用于修复输入体验：
     * 1）错误提示状态下：键盘输入任意字符都直接覆盖错误提示
     * 2）结果状态下：键盘输入数字会覆盖结果（开始新输入）
     * 3）当前为 "0" 或 "-0" 时：键盘输入数字替换掉 0，避免出现 05 或 -05
     */
    private void installDocumentFilter() {
        if (!(display.getDocument() instanceof AbstractDocument doc)) return;

        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());

                // 错误提示：任意输入覆盖整段
                if (isErrorText(cur)) {
                    fb.replace(0, cur.length(), text, attrs);
                    showingResult = false;
                    return;
                }

                // 结果态 + 输入数字：覆盖整段
                if (showingResult && text != null && text.length() == 1 && Character.isDigit(text.charAt(0))) {
                    fb.replace(0, cur.length(), text, attrs);
                    showingResult = false;
                    return;
                }

                // 前导零：0 或 -0 + 输入数字 -> 替换整段
                if (("0".equals(cur) || "-0".equals(cur))
                        && text != null && text.length() == 1 && Character.isDigit(text.charAt(0))) {
                    fb.replace(0, cur.length(), text, attrs);
                    return;
                }

                fb.replace(offset, length, text, attrs);
            }
        });
    }

    /**
     * 安装快捷键（Key Bindings），只接管功能键：
     * - Enter：=
     * - Backspace：⌫
     * - Esc：C
     * - Delete：CE
     *
     * 其余字符输入（数字、运算符等）交给 JTextField 默认行为。
     */
    private void installKeyBindings() {
        InputMap im = display.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = display.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "calc.equal");
        am.put("calc.equal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEqual();
                refocusDisplay();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "calc.backspace");
        am.put("calc.backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClear("⌫");
                refocusDisplay();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "calc.clear");
        am.put("calc.clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClear("C");
                refocusDisplay();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "calc.clearEntry");
        am.put("calc.clearEntry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClear("CE");
                refocusDisplay();
            }
        });
    }

    // ------------------------- 事件处理（按钮逻辑） -------------------------

    private void onDigit(String d) {
        String cur = display.getText();

        // 结果态或错误态：输入数字直接开始新表达式
        if (showingResult || isErrorText(cur)) {
            setDisplayText(d.equals(".") ? "0." : d, false);
            return;
        }

        // -0 状态下输入数字，应该变成 -5 而不是 -05
        if ("-0".equals(cur) && !".".equals(d)) {
            setDisplayText("-" + d, false);
            return;
        }

        // 0 状态下输入数字，替换 0
        if ("0".equals(cur) && !".".equals(d)) {
            setDisplayText(d, false);
            return;
        }

        setDisplayText(cur + d, false);
    }

    private void onOperator(String op) {
        String cur = display.getText();

        // 错误提示状态下按运算符：从 0 开始
        if (isErrorText(cur)) {
            cur = "0";
            setDisplayText(cur, false);
        }

        // 按下运算符后认为进入输入态
        showingResult = false;

        switch (op) {
            case "÷" -> setDisplayText(cur + "/", false);
            case "×" -> setDisplayText(cur + "*", false);
            case "±" -> toggleSign();
            case "%" -> onPercent();
            default -> setDisplayText(cur + op, false);
        }
    }

    /**
     * 正负号切换（±）规则：
     * - 错误提示：回到 0
     * - 0：保持 0（不产生 -0）
     * - -0：变回 0
     * - 其它：加/去负号
     */
    private void toggleSign() {
        String cur = display.getText();

        if (isErrorText(cur)) {
            setDisplayText("0", false);
            return;
        }

        if ("0".equals(cur) || "-0".equals(cur)) {
            setDisplayText("0", false);
            return;
        }

        if (cur.startsWith("-")) {
            setDisplayText(cur.substring(1), false);
        } else {
            setDisplayText("-" + cur, false);
        }
    }

    private void onPercent() {
        String cur = display.getText();
        if (isErrorText(cur)) {
            setDisplayText(ERR_GENERIC, true);
            return;
        }

        try {
            double val = evaluator.evaluate(cur);
            setDisplayText(ExpressionEvaluator.format(val / 100), true);
        } catch (ArithmeticException ex) {
            setDisplayText(mapArithmeticError(ex), true);
        } catch (Exception ex) {
            setDisplayText(ERR_GENERIC, true);
        }
    }

    private void onClear(String btn) {
        switch (btn) {
            case "C", "CE" -> {
                setDisplayText("0", false);
                historyLabel.setText(" ");
            }
            case "⌫" -> {
                String cur = display.getText();

                // 结果态或错误态，按退格直接清回 0
                if (showingResult || isErrorText(cur) || cur.length() <= 1) {
                    setDisplayText("0", false);
                    return;
                }

                setDisplayText(cur.substring(0, cur.length() - 1), false);
            }
        }
    }

    private void onEqual() {
        String expr = display.getText();

        // 错误提示状态下按等号，保持错误提示即可
        if (isErrorText(expr)) {
            setDisplayText(expr, true);
            return;
        }

        try {
            double result = evaluator.evaluate(expr);
            historyLabel.setText(expr + " =");
            setDisplayText(ExpressionEvaluator.format(result), true);
        } catch (ArithmeticException ex) {
            historyLabel.setText(expr + " =");
            setDisplayText(mapArithmeticError(ex), true);
        } catch (Exception ex) {
            historyLabel.setText(expr + " =");
            setDisplayText(ERR_INVALID, true);
        }
    }

    /**
     * 把数学异常转换为更友好的中文提示，避免“所有 ArithmeticException 都显示不能除以零”。
     */
    private String mapArithmeticError(ArithmeticException ex) {
        String msg = ex.getMessage();
        if (msg == null) return ERR_GENERIC;

        if (msg.contains("Division by zero") || msg.contains("Modulo by zero")) {
            return ERR_DIV0;
        }

        if (msg.contains("Factorial is only defined")) {
            return "阶乘只支持非负整数";
        }

        if (msg.contains("Factorial overflow")) {
            return "阶乘结果过大";
        }

        if (msg.contains("sqrt of negative")) {
            return "负数不能开平方";
        }

        if (msg.contains("log of non-positive")) {
            return "log 参数必须大于 0";
        }

        if (msg.contains("ln of non-positive")) {
            return "ln 参数必须大于 0";
        }

        return ERR_GENERIC;
    }

    // ------------------------- 科学输入工具方法 -------------------------

    private boolean endsWithOperatorOrLeftParen(String s) {
        if (s == null || s.isEmpty()) return true;
        char last = s.charAt(s.length() - 1);
        return last == '+' || last == '-' || last == '*' || last == '/' ||
                last == '%' || last == '^' || last == '(';
    }

    private void onScientific(String func) {
        // 标准区按钮显示为 √x，实际执行 sqrt
        if ("√x".equals(func)) func = "√";

        String cur = display.getText();
        boolean treatAsEmpty = "0".equals(cur) || isErrorText(cur);

        switch (func) {
            // 科学区括号按钮：转发到运算符输入
            case "(", ")" -> {
                if (isErrorText(cur)) setDisplayText("0", false);
                onOperator(func);
            }

            // 函数（都需要括号参数）
            case "sin", "cos", "tan", "asin", "acos", "atan",
                 "sinh", "cosh", "tanh", "ln", "log", "exp",
                 "abs", "ceil", "floor" -> {
                String token = func + "(";

                // 错误/初始：直接替换为 func(
                if (treatAsEmpty) {
                    setDisplayText(token, false);
                    return;
                }

                // 结果态：对结果做函数运算 func(result)
                if (showingResult && !isErrorText(cur)) {
                    setDisplayText(func + "(" + cur + ")", false);
                    return;
                }

                // 输入态：决定是否补 *
                if (endsWithOperatorOrLeftParen(cur)) {
                    setDisplayText(cur + token, false);
                } else {
                    setDisplayText(cur + "*" + token, false);
                }
            }

            // 根号：sqrt
            case "√" -> {
                String token = "sqrt(";

                if (treatAsEmpty) {
                    setDisplayText(token, false);
                    return;
                }

                if (showingResult && !isErrorText(cur)) {
                    setDisplayText("sqrt(" + cur + ")", false);
                    return;
                }

                if (endsWithOperatorOrLeftParen(cur)) {
                    setDisplayText(cur + token, false);
                } else {
                    setDisplayText(cur + "*" + token, false);
                }
            }

            // 常量 π
            case "π" -> {
                if (treatAsEmpty) {
                    setDisplayText("pi", false);
                } else if (endsWithOperatorOrLeftParen(cur)) {
                    setDisplayText(cur + "pi", false);
                } else {
                    setDisplayText(cur + "*pi", false);
                }
            }

            // 常量 e
            case "e" -> {
                if (treatAsEmpty) {
                    setDisplayText("e", false);
                } else if (endsWithOperatorOrLeftParen(cur)) {
                    setDisplayText(cur + "e", false);
                } else {
                    setDisplayText(cur + "*e", false);
                }
            }

            // 平方
            case "x²" -> {
                if (isErrorText(cur)) {
                    setDisplayText("0", false);
                    cur = "0";
                }
                setDisplayText("(" + cur + ")^2", false);
            }

            // 幂运算
            case "x^y" -> {
                if (isErrorText(cur)) {
                    setDisplayText("0", false);
                    cur = "0";
                }
                setDisplayText(cur + "^", false);
            }

            // 倒数
            case "1/x" -> {
                if (isErrorText(cur)) {
                    setDisplayText("0", false);
                    cur = "0";
                }
                setDisplayText("1/(" + cur + ")", false);
            }

            // 取模
            case "mod" -> {
                if (isErrorText(cur)) {
                    setDisplayText("0", false);
                    cur = "0";
                }
                setDisplayText(cur + "%", false);
            }

            // 阶乘
            case "n!" -> {
                if (isErrorText(cur)) {
                    setDisplayText("0", false);
                    cur = "0";
                }
                setDisplayText("(" + cur + ")!", false);
            }
        }
    }

    // ------------------------- 剪贴板 -------------------------

    private void copyToClipboard() {
        String text = display.getText();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    private void pasteFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // 只处理纯文本，避免 IntelliJ 私有 DataFlavor 导致异常
            if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return;
            }

            Object data = clipboard.getData(DataFlavor.stringFlavor);
            if (!(data instanceof String)) {
                return;
            }

            String text = ((String) data).trim();
            if (text.isEmpty()) {
                return;
            }

            // 结果态/错误态粘贴：覆盖；否则追加
            if (showingResult || isErrorText(display.getText())) {
                setDisplayText(text, false);
            } else {
                setDisplayText(display.getText() + text, false);
            }
        } catch (Exception ignored) {
            // 忽略剪贴板异常
        }
    }
}