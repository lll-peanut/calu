package com.peanut;

import com.peanut.calculator.CalculatorFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.*;

@SpringBootApplication
public class CaluApplication {

    public static final Logger logger = LoggerFactory.getLogger(CaluApplication.class);

    public static void main(String[] args) {
        // 设置软件界面风格为跨平台风格（Metal），以确保在不同操作系统上具有一致的外观
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn(e.toString());
        }

        // 启动Spring Boot应用程序并获取应用上下文
        ConfigurableApplicationContext context = SpringApplication.run(CaluApplication.class, args);
        // 创建并显示计算器界面
        SwingUtilities.invokeLater(() -> {
            CalculatorFrame frame = context.getBean(CalculatorFrame.class);
            frame.setVisible(true);
        });
    }
}
