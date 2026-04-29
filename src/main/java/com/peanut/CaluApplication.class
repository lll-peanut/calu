package com.peanut;

import com.peanut.calculator.CalculatorFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

@SpringBootApplication
public class CaluApplication {

    public static void main(String[] args) {
        // Set system look and feel before launching Spring
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fall back to default look and feel
        }

        ConfigurableApplicationContext context = SpringApplication.run(CaluApplication.class, args);

        SwingUtilities.invokeLater(() -> {
            CalculatorFrame frame = context.getBean(CalculatorFrame.class);
            frame.setVisible(true);
        });
    }
}
