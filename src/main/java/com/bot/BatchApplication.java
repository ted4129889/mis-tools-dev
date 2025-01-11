/* (C) 2024 */
package com.bot;

import com.bot.ui.MaskFrame;
import java.awt.*;
import javax.swing.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in headless mode. GUI cannot be initialized.");
        } else {
            System.out.println("Running in GUI mode.");
        }
        //        SpringApplication.run(BatchApplication.class, args);

        ApplicationContext context = SpringApplication.run(BatchApplication.class, args);

        // 啟動 JFrame
        MaskFrame maskFrame = context.getBean(MaskFrame.class);
        SwingUtilities.invokeLater(() -> maskFrame.setVisible(true));
    }
}
