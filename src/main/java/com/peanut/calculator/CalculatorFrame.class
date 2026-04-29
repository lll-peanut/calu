package com.peanut.calculator;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Windows-style calculator GUI built with Java Swing.
 * <p>
 * Features:
 * <ul>
 *   <li>Standard calculator layout (0-9, operators, parentheses)</li>
 *   <li>Expression shown in top display; result shown after pressing "="</li>
 *   <li>Menu bar: Edit > Copy / Paste</li>
 *   <li>Scientific panel (sin, cos, tan, sqrt, log, ln, exp, pow, …)</li>
 *   <li>Keyboard input support</li>
 * </ul>
 */
@Component
public class CalculatorFrame extends JFrame {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final ExpressionEvaluator evaluator;

    /** The main expression/result display */
    private JTextField display;

    /** Small secondary display showing what was computed */
    private JLabel historyLabel;

    /** Whether the display currently shows a result (affects next key behaviour) */
    private boolean showingResult = false;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public CalculatorFrame(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
        initUI();
    }

    // -----------------------------------------------------------------------
    // UI initialisation
    // -----------------------------------------------------------------------

    private void initUI() {
        setTitle("计算器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ── Menu bar ────────────────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // ── Root panel ──────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(32, 32, 32));
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ── Display area ────────────────────────────────────────────────────
        root.add(buildDisplayPanel(), BorderLayout.NORTH);

        // ── Button area ─────────────────────────────────────────────────────
        JPanel buttons = new JPanel(new GridLayout(1, 2, 2, 0));
        buttons.setBackground(new Color(32, 32, 32));

        buttons.add(buildScientificPanel());
        buttons.add(buildStandardPanelClean());

        root.add(buttons, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        // ── Keyboard input ──────────────────────────────────────────────────
        display.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyEvent(e);
            }
        });
        display.setFocusable(true);
        display.requestFocusInWindow();
    }

    // -----------------------------------------------------------------------
    // Menu bar
    // -----------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(45, 45, 45));
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // ── 查看 (View) ──────────────────────────────────────────────────────
        JMenu viewMenu = styledMenu("查看");
        JMenuItem standardItem = styledMenuItem("标准型");
        standardItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "当前已是标准型模式", "查看", JOptionPane.INFORMATION_MESSAGE));
        viewMenu.add(standardItem);

        // ── 编辑 (Edit) ──────────────────────────────────────────────────────
        JMenu editMenu = styledMenu("编辑");

        JMenuItem copyItem = styledMenuItem("复制 (Ctrl+C)");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        copyItem.addActionListener(e -> copyToClipboard());

        JMenuItem pasteItem = styledMenuItem("粘贴 (Ctrl+V)");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        pasteItem.addActionListener(e -> pasteFromClipboard());

        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        // ── 帮助 (Help) ──────────────────────────────────────────────────────
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
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        return menu;
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        return item;
    }

    // -----------------------------------------------------------------------
    // Display panel
    // -----------------------------------------------------------------------

    private JPanel buildDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(new EmptyBorder(12, 12, 8, 12));

        // History label (small, shows previous expression)
        historyLabel = new JLabel(" ");
        historyLabel.setForeground(new Color(170, 170, 170));
        historyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(historyLabel, BorderLayout.NORTH);

        // Main display
        display = new JTextField("0");
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setEditable(false);
        display.setBackground(new Color(32, 32, 32));
        display.setForeground(Color.WHITE);
        display.setCaretColor(Color.WHITE);
        display.setFont(new Font("Segoe UI", Font.PLAIN, 42));
        display.setBorder(new EmptyBorder(2, 0, 2, 0));
        display.setFocusable(true);
        panel.add(display, BorderLayout.CENTER);

        return panel;
    }

    // -----------------------------------------------------------------------
    // Standard button panel
    // -----------------------------------------------------------------------

    private JPanel buildStandardPanelClean() {
        JPanel panel = new JPanel(new GridLayout(6, 4, 2, 2));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(new EmptyBorder(2, 2, 4, 4));

        // Row 1: %  CE  C  ⌫
        panel.add(opBtn("%"));
        panel.add(clearBtn("CE"));
        panel.add(clearBtn("C"));
        panel.add(clearBtn("⌫"));

        // Row 2: 1/x  x²  √x  ÷
        panel.add(sciSmallBtn("1/x"));
        panel.add(sciSmallBtn("x²"));
        panel.add(sciSmallBtn("√x"));
        panel.add(opBtn("÷"));

        // Row 3: 7  8  9  ×
        panel.add(numBtn("7"));
        panel.add(numBtn("8"));
        panel.add(numBtn("9"));
        panel.add(opBtn("×"));

        // Row 4: 4  5  6  -
        panel.add(numBtn("4"));
        panel.add(numBtn("5"));
        panel.add(numBtn("6"));
        panel.add(opBtn("-"));

        // Row 5: 1  2  3  +
        panel.add(numBtn("1"));
        panel.add(numBtn("2"));
        panel.add(numBtn("3"));
        panel.add(opBtn("+"));

        // Row 6: ±  0  .  =
        panel.add(opBtn("±"));
        panel.add(numBtn("0"));
        panel.add(numBtn("."));
        panel.add(equalBtn());

        return panel;
    }

    // -----------------------------------------------------------------------
    // Scientific button panel
    // -----------------------------------------------------------------------

    private JPanel buildScientificPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 4, 2, 2));
        panel.setBackground(new Color(32, 32, 32));
        panel.setBorder(new EmptyBorder(2, 4, 4, 2));

        String[][] sciButtons = {
            { "(", ")", "n!", "mod" },
            { "sin", "cos", "tan", "π" },
            { "asin", "acos", "atan", "e" },
            { "sinh", "cosh", "tanh", "x^y" },
            { "ln", "log", "exp", "√" },
            { "abs", "ceil", "floor", "1/x" }
        };

        for (String[] row : sciButtons) {
            for (String label : row) {
                panel.add(sciBtn(label));
            }
        }

        return panel;
    }

    // -----------------------------------------------------------------------
    // Button factory helpers
    // -----------------------------------------------------------------------

    /** Standard digit button */
    private JButton numBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(51, 51, 51), new Color(68, 68, 68));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.addActionListener(e -> onDigit(label));
        return btn;
    }

    /** Operator button (+, -, ×, ÷, (, ), etc.) */
    private JButton opBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(45, 45, 45), new Color(65, 65, 65));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.addActionListener(e -> onOperator(label));
        return btn;
    }

    /** Equal button */
    private JButton equalBtn() {
        JButton btn = new JButton("=");
        styleButton(btn, new Color(0, 120, 215), new Color(30, 150, 240));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.addActionListener(e -> onEqual());
        return btn;
    }

    /** Clear / backspace button */
    private JButton clearBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(45, 45, 45), new Color(65, 65, 65));
        btn.setForeground(new Color(230, 100, 80));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.addActionListener(e -> onClear(label));
        return btn;
    }

    /** Memory button (MC, MR, M+, M−) */
    private JButton utilBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(32, 32, 32), new Color(50, 50, 50));
        btn.setForeground(new Color(140, 140, 140));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.addActionListener(e -> onMemory(label));
        return btn;
    }

    /** Scientific function button */
    private JButton sciBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(38, 38, 38), new Color(58, 58, 58));
        btn.setForeground(new Color(220, 220, 220));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.addActionListener(e -> onScientific(label));
        return btn;
    }

    /** Inline scientific small button (used inside standard panel) */
    private JButton sciSmallBtn(String label) {
        JButton btn = new JButton(label);
        styleButton(btn, new Color(45, 45, 45), new Color(65, 65, 65));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.addActionListener(e -> onScientific(label));
        return btn;
    }

    /** Apply common flat-style properties to a button. */
    private void styleButton(JButton btn, Color normal, Color hover) {
        btn.setBackground(normal);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(normal); }
        });
    }

    // -----------------------------------------------------------------------
    // Event handlers
    // -----------------------------------------------------------------------

    private void onDigit(String d) {
        if (showingResult) {
            // Start fresh expression after a result
            display.setText(d.equals(".") ? "0." : d);
            showingResult = false;
        } else {
            String cur = display.getText();
            if ("0".equals(cur) && !".".equals(d)) {
                display.setText(d);
            } else {
                display.setText(cur + d);
            }
        }
    }

    private void onOperator(String op) {
        showingResult = false;
        String cur = display.getText();
        switch (op) {
            case "÷" -> display.setText(cur + "/");
            case "×" -> display.setText(cur + "*");
            case "±" -> toggleSign();
            case "%" -> onPercent();
            default  -> display.setText(cur + op);
        }
    }

    private void toggleSign() {
        String cur = display.getText();
        if (cur.startsWith("-")) {
            display.setText(cur.substring(1));
        } else {
            display.setText("-" + cur);
        }
    }

    private void onPercent() {
        try {
            double val = evaluator.evaluate(display.getText());
            display.setText(ExpressionEvaluator.format(val / 100));
        } catch (Exception ex) {
            display.setText("错误");
        }
    }

    private void onClear(String btn) {
        switch (btn) {
            case "C", "CE" -> {
                display.setText("0");
                historyLabel.setText(" ");
                showingResult = false;
            }
            case "⌫" -> {
                String cur = display.getText();
                if (showingResult || cur.length() <= 1) {
                    display.setText("0");
                    showingResult = false;
                } else {
                    display.setText(cur.substring(0, cur.length() - 1));
                }
            }
        }
    }

    private void onEqual() {
        String expr = display.getText();
        try {
            double result = evaluator.evaluate(expr);
            historyLabel.setText(expr + " =");
            display.setText(ExpressionEvaluator.format(result));
            showingResult = true;
        } catch (ArithmeticException ex) {
            historyLabel.setText(expr + " =");
            display.setText("不能除以零");
            showingResult = true;
        } catch (Exception ex) {
            historyLabel.setText(expr + " =");
            display.setText("无效表达式");
            showingResult = true;
        }
    }

    private double memory = 0;

    private void onMemory(String btn) {
        switch (btn) {
            case "MC" -> memory = 0;
            case "MR" -> {
                display.setText(ExpressionEvaluator.format(memory));
                showingResult = false;
            }
            case "M+" -> {
                try {
                    memory += evaluator.evaluate(display.getText());
                } catch (Exception ignored) {}
            }
            case "M-" -> {
                try {
                    memory -= evaluator.evaluate(display.getText());
                } catch (Exception ignored) {}
            }
        }
    }

    private void onScientific(String func) {
        String cur = display.getText();
        switch (func) {
            // Insert function prefix
            case "sin", "cos", "tan", "asin", "acos", "atan",
                 "sinh", "cosh", "tanh", "ln", "log", "exp",
                 "abs", "ceil", "floor", "sqrt" -> {
                if (showingResult) {
                    display.setText(func + "(" + cur + ")");
                    showingResult = false;
                } else {
                    display.setText(cur + func + "(");
                }
            }
            // π constant
            case "π" -> {
                if (showingResult) {
                    display.setText("pi");
                    showingResult = false;
                } else {
                    display.setText(cur + "pi");
                }
            }
            // e constant
            case "e" -> {
                if (showingResult) {
                    display.setText("e");
                    showingResult = false;
                } else {
                    display.setText(cur + "e");
                }
            }
            // x² – square the current expression
            case "x²" -> {
                display.setText("(" + cur + ")^2");
                showingResult = false;
            }
            // x^y – start power expression
            case "x^y" -> {
                display.setText(cur + "^");
                showingResult = false;
            }
            // √ – square root of current value
            case "√" -> {
                if (showingResult) {
                    display.setText("sqrt(" + cur + ")");
                    showingResult = false;
                } else {
                    display.setText(cur + "sqrt(");
                }
            }
            // 1/x – reciprocal
            case "1/x" -> {
                display.setText("1/(" + cur + ")");
                showingResult = false;
            }
            // mod
            case "mod" -> {
                display.setText(cur + "%");
                showingResult = false;
            }
            // n! – factorial
            case "n!" -> {
                display.setText("(" + cur + ")!");
                showingResult = false;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Keyboard support
    // -----------------------------------------------------------------------

    private void handleKeyEvent(KeyEvent e) {
        int code = e.getKeyCode();
        char ch = e.getKeyChar();

        if (Character.isDigit(ch)) {
            onDigit(String.valueOf(ch));
        } else if (ch == '.') {
            onDigit(".");
        } else if (ch == '+') {
            onOperator("+");
        } else if (ch == '-') {
            onOperator("-");
        } else if (ch == '*') {
            onOperator("×");
        } else if (ch == '/') {
            onOperator("÷");
        } else if (ch == '(') {
            onOperator("(");
        } else if (ch == ')') {
            onOperator(")");
        } else if (ch == '^') {
            onOperator("^");
        } else if (ch == '%') {
            onOperator("%");
        } else if (code == KeyEvent.VK_ENTER || ch == '=') {
            onEqual();
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            onClear("⌫");
        } else if (code == KeyEvent.VK_ESCAPE) {
            onClear("C");
        } else if (code == KeyEvent.VK_DELETE) {
            onClear("CE");
        }
    }

    // -----------------------------------------------------------------------
    // Clipboard
    // -----------------------------------------------------------------------

    private void copyToClipboard() {
        String text = display.getText();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    private void pasteFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String text = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (text != null) {
                if (showingResult) {
                    display.setText(text.trim());
                    showingResult = false;
                } else {
                    display.setText(display.getText() + text.trim());
                }
            }
        } catch (Exception ex) {
            // Clipboard access failed – silently ignore
        }
    }
}
