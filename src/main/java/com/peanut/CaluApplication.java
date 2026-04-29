package com.peanut;

import com.peanut.calculator.CalculatorFrame;

import javax.swing.SwingUtilities;

public class CaluApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorFrame frame = new CalculatorFrame();
            frame.setVisible(true);
        });
    }
}
