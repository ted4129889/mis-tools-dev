/* (C) 2024 */
package com.bot.service;

import com.bot.common.util.xml.mask.XmlToInsertGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileService {
    @Autowired XmlToInsertGenerator xmlToInsertGenerator;

    public List<String> listFiles(String directoryPath) {
        File folder = new File(directoryPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        // 取得檔案清單
        return Arrays.stream(folder.listFiles())
                .filter(File::isFile)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public void writeSelectedFileNamesToFile(List<String> selectedFiles, String outputFilePath)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (String fileName : selectedFiles) {
                writer.write(fileName);
                writer.newLine();
            }
        }
    }

    public void generateFilesForSelectedNames(List<String> selectedFiles) throws IOException {

        for (String fileName : selectedFiles) {

            System.out.println("xmlFile=======" + fileName.split("\\.")[0]);
            xmlToInsertGenerator.sqlConvInsertTxt(fileName.split("\\.")[0]);
        }
    }
}
