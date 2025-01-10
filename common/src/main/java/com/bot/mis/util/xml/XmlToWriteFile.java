/* (C) 2024 */
package com.bot.mis.util.xml;

import com.bot.mis.util.files.TextFileUtil;
import com.bot.mis.util.sql.svc.ExecuteSqlService;
import com.bot.mis.util.xml.config.SecureXmlMapper;
import com.bot.mis.util.xml.vo.*;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import com.bot.txcontrol.util.parse.Parse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class XmlToWriteFile {
    @Value("${localFile.mis.batch.output}")
    private String fileDir;

    @Value("${localFile.mis.xml.output.directory}")
    private String xmlOutputFileDir;

    @Autowired private Parse parse;
    @Autowired private TextFileUtil textFileUtil;
    @Autowired private ExecuteSqlService executeSqlService;
    @Autowired private DataProcess dataProcess;

    private static final String CHARSET = "BIG5";
    private static final String txtExtension = ".txt";
    private static final String xmlExtension = ".xml";
    private static final String bcFormat = "[yyyymmdd]";

    public XmlToWriteFile() {
        // YOU SHOULD USE @Autowired ,NOT new ErrUtil()
    }

    /**
     * 執行Xml產檔案(資料來源使用SQL查詢)
     *
     * @param fileName 檔案名稱
     * @param requestParams request參數
     * @return List<String>
     */
    public List<String> exec(String fileName, Map<String, String> requestParams) {
        return exec(fileName, requestParams, new ArrayList<>());
    }

    /**
     * 執行Xml產檔案 (資料來源使用TXT檔案)
     *
     * @param fileName 檔案名稱
     * @param requestParams request參數
     * @param dataList 待處理的資料
     * @return List<String>
     */
    public List<String> exec(
            String fileName,
            Map<String, String> requestParams,
            List<Map<String, String>> dataList) {
        //        需要一個Object[]陣列是放值，xmlData是欄位的東西
        List<String> fileContents = new ArrayList<>();

        boolean isUseSqlData = true;
        int batchDate = parse.string2Integer(requestParams.get("batchDate"));

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "txtToFile exec()");

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "batchDate = {}", batchDate);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileName = {}", fileName);
        try {
            String xml = xmlOutputFileDir + fileName + xmlExtension;
            String txt = fileDir + fileName + txtExtension;

            if (txt.contains(bcFormat)) {
                txt = txt.replace(bcFormat, String.valueOf(batchDate));
            }
            File file = new File(xml);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + xml);
            }
            // 解析XML檔案格式
            XmlMapper xmlMapper = SecureXmlMapper.createXmlMapper();

            XmlData xmlData = xmlMapper.readValue(file, XmlData.class);
            // 表頭標籤
            XmlHeader header = xmlData.getHeader();
            // 內容標籤
            XmlBody body = xmlData.getBody();
            // sql語法
            String sql = xmlData.getSql().getSqlText();
            // 是否使用sql語法
            isUseSqlData = header.isUseSqlData();
            List<Map<String, String>> tmpList = new ArrayList<>();
            // 開始header處理...
            if (!header.getFieldList().isEmpty()) {
                if (!dataList.isEmpty()) {
                    tmpList.addAll(dataList);
                }

                if (isUseSqlData) {
                    // 使用sql參數 param
                    List<String> xmlParamList = xmlData.getSql().getParamList();
                    // 檔案的資料在前，加入SQL的資料
                    tmpList.addAll(
                            executeSqlService.queryByXml(
                                    sql, header.getFieldList(), requestParams, xmlParamList));
                }

                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "headertmpList = {}", tmpList);
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "isUseSqlData = {}", isUseSqlData);
                fileContents.addAll(
                        dataProcess.exec(
                                tmpList,
                                header.getFieldList(),
                                !dataList.isEmpty() || isUseSqlData,
                                header.getSeparator(),
                                batchDate));
            }

            // body處理...
            isUseSqlData = body.isUseSqlData();
            tmpList = new ArrayList<>();
            if (!body.getFieldList().isEmpty()) {

                if (!dataList.isEmpty()) {
                    tmpList.addAll(dataList);
                }

                if (isUseSqlData) {
                    // 使用sql參數 param
                    List<String> xmlParamList = xmlData.getSql().getParamList();
                    // 檔案的資料在前，加入SQL的資料
                    tmpList.addAll(
                            executeSqlService.queryByXml(
                                    sql, body.getFieldList(), requestParams, xmlParamList));
                }

                ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "bodytmpList = {}", tmpList);
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "isUseSqlData = {}", isUseSqlData);
                if (!tmpList.isEmpty()) {
                    fileContents.addAll(
                            dataProcess.exec(
                                    tmpList,
                                    body.getFieldList(),
                                    !dataList.isEmpty() || isUseSqlData, // 有外來資料就一定會是
                                    body.getSeparator(),
                                    batchDate));
                }
            }

            if (!fileContents.isEmpty()) {
                writeFile(fileContents, txt);
            } else {
                fileContents.add("no data");
                writeFile(fileContents, txt);
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "{} output data is null", fileName);
            }
        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToWriteFile.exec error");
        }

        return fileContents;
    }

    /**
     * 輸出檔案
     *
     * @param fileContents 資料串
     * @param outFileName 輸出檔案名
     */
    private void writeFile(List<String> fileContents, String outFileName) {

        textFileUtil.deleteFile(outFileName);

        try {
            textFileUtil.writeFileContent(outFileName, fileContents, CHARSET);
        } catch (LogicException e) {
            moveErrorResponse(e);
        }
    }

    private void moveErrorResponse(LogicException e) {
        //        event.setPeripheryRequest();
    }
}
