package com.peanut.calculator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.math.BigDecimal;

public class CalculatorFrame extends JFrame {
    private static final String[] BUTTONS = {
        "C", "(", ")", "←",
        "7", "8", "9", "/",
        "4", "5", "6", "*",
        "1", "2", "3", "-",
        "0", ".", "=", "+"
    };

    private final JTextField display;
    private final ExpressionEvaluator evaluator;
    private boolean resultDisplayed;

    public CalculatorFrame() {
        super("Calculator");
        this.evaluator = new ExpressionEvaluator();
        this.display = new JTextField();
        this.resultDisplayed = false;
        initializeUi();
    }

    private void initializeUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setSize(360, 460);
        setLocationRelativeTo(null);

        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("SansSerif", Font.PLAIN, 26));
        display.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 6, 6));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (String label : BUTTONS) {
            JButton button = new JButton(label);
            button.setFont(new Font("SansSerif", Font.PLAIN, 20));
            button.addActionListener(event -> handleButton(label));
            buttonPanel.add(button);
        }
        add(buttonPanel, BorderLayout.CENTER);
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu("Edit");
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(event -> copyToClipboard());
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener(event -> pasteFromClipboard());
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        menuBar.add(editMenu);
        return menuBar;
    }

    private void handleButton(String label) {
        switch (label) {
            case "C" -> clearDisplay();
            case "←" -> backspace();
            case "=" -> evaluateExpression();
            default -> appendInput(label);
        }
    }

    private void appendInput(String input) {
        if (resultDisplayed) {
            if (shouldClearForInput(input)) {
                display.setText("");
            }
            resultDisplayed = false;
        }
        display.setText(display.getText() + input);
    }

    private boolean shouldClearForInput(String input) {
        return !isOperator(input) && !")".equals(input);
    }

    private boolean isOperator(String input) {
        return "+".equals(input) || "-".equals(input) || "*".equals(input) || "/".equals(input);
    }

    private void clearDisplay() {
        display.setText("");
        resultDisplayed = false;
    }

    private void backspace() {
        String current = display.getText();
        if (current.isEmpty()) {
            return;
        }
        if (resultDisplayed) {
            resultDisplayed = false;
        }
        display.setText(current.substring(0, current.length() - 1));
    }

    private void evaluateExpression() {
        String expression = display.getText();
        if (expression.trim().isEmpty()) {
            return;
        }
        try {
            double value = evaluator.evaluate(expression);
            display.setText(formatResult(value));
        } catch (Exception ex) {
            display.setText("Error");
        }
        resultDisplayed = true;
    }

    private String formatResult(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "Error";
        }
        BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        if (decimal.scale() < 0) {
            decimal = decimal.setScale(0);
        }
        return decimal.toPlainString();
    }

    private void copyToClipboard() {
        String selected = display.getSelectedText();
        String text = (selected == null || selected.isEmpty()) ? display.getText() : selected;
        if (text == null || text.isEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private void pasteFromClipboard() {
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (contents == null || !contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return;
        }
        try {
            String pasted = (String) contents.getTransferData(DataFlavor.stringFlavor);
            if (pasted == null || pasted.isEmpty()) {
                return;
            }
            if (resultDisplayed) {
                display.setText("");
                resultDisplayed = false;
            }
            int start = display.getSelectionStart();
            int end = display.getSelectionEnd();
            String current = display.getText();
            StringBuilder updated = new StringBuilder();
            updated.append(current, 0, start);
            updated.append(pasted);
            updated.append(current.substring(end));
            display.setText(updated.toString());
        } catch (Exception ignored) {
        }
    }
}
