/* (C) 2024 */
package com.bot.ui;

import com.bot.service.FileService;
import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings({"unchecked"})
public class MaskFrame extends JFrame {
    @Value("${localFile.dev.xml.mask.directory}")
    private String maskXmlFilePath;

    @Value("${localFile.dev.batch.output}")
    private String outputFilePath;

    private String maskXmlFilePath2 = "src\\main\\resources\\xml\\mask\\";

    private final FileService fileService;

    @Autowired
    public MaskFrame(FileService fileService) {
        this.fileService = fileService;

        // 設定視窗圖示
        ImageIcon icon = new ImageIcon("src/main/resources/icon/glasses.png");
        setIconImage(icon.getImage());
        setTitle("Mask Tools");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        // 預設目錄路徑
        List<String> fileNames = fileService.listFiles(maskXmlFilePath2);

        // 檔案列表 (多選模式)
        DefaultListModel<String> listModel = new DefaultListModel<>();
        fileNames.forEach(listModel::addElement);
        JList<String> fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(fileList);

        // 按鈕：全選
        JButton selectAllButton = new JButton("全選");
        selectAllButton.addActionListener(
                e -> fileList.setSelectionInterval(0, listModel.getSize() - 1));

        // 按鈕：產生檔案
        JButton generateFileButton = new JButton("產生檔案");
        generateFileButton.addActionListener(
                e -> {
                    try {

                        List<String> fileLists = new ArrayList<>();
                        List<String> selectedFiles = fileList.getSelectedValuesList();

                        System.out.println("selectedFiles=======" + selectedFiles);
                        if (selectedFiles.isEmpty()) {
                            JOptionPane.showMessageDialog(
                                    this, "請選擇至少一個檔案！", "警告", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        for (String r : selectedFiles) {
                            fileLists.add(maskXmlFilePath2 + r);
                        }
                        System.out.println("fileLists=======" + fileLists);
                        // 寫入選取的檔案名稱到輸出檔案
                        fileService.generateFilesForSelectedNames(selectedFiles);

                        JOptionPane.showMessageDialog(
                                this,
                                "已產生檔案: " + outputFilePath,
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                "產生檔案失敗: " + ex.getMessage(),
                                "錯誤",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        // 版面配置
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(selectAllButton);
        buttonPanel.add(generateFileButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // 顯示
        setLocationRelativeTo(null);
    }
}
