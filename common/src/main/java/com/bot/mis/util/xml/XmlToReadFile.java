/* (C) 2024 */
package com.bot.mis.util.xml;

import com.bot.mis.util.files.TextFileUtil;
import com.bot.mis.util.xml.config.SecureXmlMapper;
import com.bot.mis.util.xml.vo.*;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import com.bot.txcontrol.util.parse.Parse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class XmlToReadFile {

    @Value("${localFile.mis.batch.output}")
    private String fileDir;

    @Value("${localFile.mis.xml.input.directory}")
    private String xmlInputFileDir;

    @Value("${localFile.mis.batch.input}")
    private String inputFileDir;

    @Autowired private TextFileUtil textFileUtil;
    @Autowired private Parse parse;
    private final String CHARSET_BIG5 = "BIG5";
    private final String CHARSET_UTF8 = "UTF-8";
    private final String separator = "separator";
    private final String txtExtension = ".txt";
    private final String xmlExtension = ".xml";
    private final String inputFileNameHeader = "_H";
    private final String inputFileNameBody = "_D";
    private final String bottomLine = "_";
    private final String COMMA = ",";

    /**
     * 讀xml.input的欄位設定檔與TXT資料檔 對應成 List<Map<String, String>> 格式<br>
     *
     * @param fileName 檔案名稱(XML與TXT同名)
     * @return List<Map < String, String>> 查询结果的列表
     */
    public List<Map<String, String>> exec(String fileName) {

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "XmlToReadFile exec...");

        List<Map<String, String>> tmpList = new ArrayList<>();
        try {
            // XML檔案路徑(讀取)
            String xml = xmlInputFileDir + fileName + xmlExtension;

            // 解析XML檔案格式
            XmlMapper xmlMapper = SecureXmlMapper.createXmlMapper();

            File file = new File(xml);
            XmlData xmlData = xmlMapper.readValue(file, XmlData.class);

            // 取得XML定義好的欄位規格
            List<XmlField> fieldList = xmlData.getTxt().getFieldList();

            // 一個檔案有表頭跟內容不同格式
            if (fileName.toUpperCase().contains(inputFileNameHeader)
                    || fileName.toUpperCase().contains(inputFileNameBody)) {
                fileName = fileName.split(bottomLine)[0];
            }

            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileName = {}", fileName);

            // TXT檔案路徑 讀取
            String txt = inputFileDir + fileName + txtExtension;
            // 讀取檔案內容
            List<String> lines = textFileUtil.readFileContent(txt, CHARSET_BIG5);

            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "lines = {}", lines);

            int splitCnt = 0;
            int xmlFieldCnt = 0;
            boolean haveComma = false;
            // 有無逗號判斷使用split或是substring
            // 確認資料內容有沒有使用逗號
            if (!lines.isEmpty()) {
                int c = 0;

                for (String s : lines) {
                    if (s.contains(COMMA)) {
                        haveComma = true;
                    }
                    c++;
                }
            }
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "splitCnt = {}", splitCnt);
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "haveComma = {}", haveComma);
            for (XmlField f : fieldList) {

                if (f.getFieldName().equalsIgnoreCase(separator)) {
                    continue;
                }
                xmlFieldCnt++;
            }
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "xmlFieldCnt = {}", xmlFieldCnt);
            Map<String, String> map;

            // 判斷有無區隔符號
            if (!haveComma) {
                for (String s : lines) {
                    int tCnt = 0;

                    int index = 0;
                    int start = 0;
                    int end = 0;
                    ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "s = {}", s);
                    map = new LinkedHashMap<>();

                    for (XmlField f : fieldList) {
                        start = index == 0 ? 0 : end;
                        end = end + parse.string2Integer(f.getLength());
                        // 我要怎麼知道一進來的 有中文或全形的長度是多少再開始截斷
                        if (f.getFieldName().equalsIgnoreCase(separator)) {
                            continue;
                        }
                        ApLogHelper.info(
                                log,
                                false,
                                LogType.NORMAL.getCode(),
                                "before col = {},sLen = {},eLen = {},value ={}",
                                f.getFieldName(),
                                start,
                                end,
                                s.substring(start, end));

                        tCnt = 0;
                        for (char c : s.toCharArray()) {

                            if (isFullWidth(c)) {
                                tCnt = tCnt + 2;
                            } else {
                                tCnt = tCnt + 1;
                            }
                            if (start < tCnt && tCnt < end && (isFullWidth(c))) {
                                end = end - 1;
                            }
                            tCnt++;
                        }
                        ApLogHelper.info(
                                log,
                                false,
                                LogType.NORMAL.getCode(),
                                "after col = {},sLen = {},eLen = {},value ={}",
                                f.getFieldName(),
                                start,
                                end,
                                s.substring(start, end));

                        map.put(f.getFieldName(), s.substring(start, end));

                        index++;
                    }

                    tmpList.add(map);
                }
            } else {
                String[] r;
                for (String s : lines) {
                    ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "s = {}", s);
                    int index = 0;
                    // 分組
                    r = s.split(COMMA);
                    // 確認資料長度
                    ApLogHelper.info(
                            log, false, LogType.NORMAL.getCode(), "split.length = {}", r.length);
                    if (xmlFieldCnt == r.length) {
                        Map<String, String> m = new LinkedHashMap<>();
                        for (XmlField f : fieldList) {

                            if (f.getFieldName().equalsIgnoreCase(separator)) {
                                continue;
                            } else {
                                m.put(f.getFieldName(), r[index]);
                            }
                            index++;
                        }
                        tmpList.add(m);
                    }
                }
            }

        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToReadFile.exec error");
        }

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "tmpList = {}", tmpList);

        return tmpList;
    }

    public boolean isFullWidth(char ch) {
        return (ch >= 0xFF00 && ch <= 0xFFEF)
                || // Fullwidth and Halfwidth Forms
                (ch >= 0x4E00 && ch <= 0x9FFF); // CJK Unified Ideographs
    }
}
