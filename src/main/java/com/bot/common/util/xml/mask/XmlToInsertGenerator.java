/* (C) 2024 */
package com.bot.common.util.xml.mask;

import com.bot.common.util.files.TextFileUtil;
import com.bot.common.util.xml.mask.xmltag.Field;
import com.bot.common.util.xml.vo.XmlData;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings({"unchecked"})
public class XmlToInsertGenerator {
    @Value("${localFile.dev.xml.mask.directory}")
    private String maskXmlFilePath;

    @Value("${localFile.dev.batch.output}")
    private String outputFilePath;

    @Autowired private TextFileUtil textFileUtil;
    @Autowired private DataSource dataSource;
    @Autowired private DataMasker dataMasker;
    private static final String CHARSET = "BIG5";
    private static final int bufferCapacity = 5000;
    private static final String paramValue = "value";
    private static final String paranType = "type";
    private static final String paramLength = "length";
    String xmlFile = "";

    public void sqlConvInsertTxt(String xmlFile) {
        XmlParser xmlParser = new XmlParser();
        System.out.println("maskXmlFilePath=======" + maskXmlFilePath);
        System.out.println("outputFilePath=======" + outputFilePath);

        try {
            String xml = maskXmlFilePath + xmlFile + ".xml";

            // parse Xml
            XmlData parsedXml = xmlParser.parseXmlFile(xml);

            // <table>
            String tableName = parsedXml.getTable().getTableName();

            // <sql>
            String sqlTag = parsedXml.getSql().getSqlText();

            // <field>
            List<Field> fields = parsedXml.getFieldList();

            // get SQL data
            List<Map<String, Object>> sqlData = getSqlData(sqlTag);

            // mask data
            dataMasker.maskData(sqlData, fields);

            // generate .txt
            writeFile(generateSQL(tableName, sqlData), outputFilePath + tableName + ".txt");

        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToInsertGenerator.sqlConvInsertTxt error");
        }
    }

    private List<String> generateSQL(String tableName, List<Map<String, Object>> maskedSqlData) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "tableName=" + tableName);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "maskedSqlData=" + maskedSqlData);

        StringBuilder result;
        List<String> fileContents = new ArrayList<>();

        for (Map<String, Object> mask : maskedSqlData) {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Object objValues;
            result = new StringBuilder(bufferCapacity);
            for (Map.Entry<String, Object> entry : mask.entrySet()) {

                columns.append(entry.getKey()).append(" ,");
                objValues = entry.getValue();
                if (objValues instanceof Map) {
                    Map<String, Object> valuesMap = (Map<String, Object>) objValues;
                    values.append(formatValue(valuesMap.get(paramValue))).append(" ,");
                }
            }

            String tmp =
                    String.format(
                            "INSERT INTO %s (%s) VALUES (%s);",
                            tableName,
                            columns.substring(0, columns.length() - 1),
                            values.substring(0, values.length() - 1));
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "tmp=" + tmp);

            result.append(tmp);
            fileContents.add(result.toString());
        }
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileContents=" + fileContents);
        return fileContents;
    }

    private String formatValue(Object val) {

        if (val == null) {
            return "NULL";
        }
        return val instanceof String ? "'" + val + "'" : val.toString();
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

    private List<Map<String, Object>> getSqlData(String sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        String url =
                "jdbc:sqlserver://192.168.6.5:1433;database=mis;encrypt=true;trustServerCertificate=true";
        String user = "misap";
        String password = "1qaz@WSX";

        // 連線和查詢
        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    String columnType = metaData.getColumnTypeName(i);
                    int columnLength = metaData.getColumnDisplaySize(i);

                    Map<String, Object> columnInfo = new HashMap<>();
                    columnInfo.put(paramValue, value);
                    columnInfo.put(paranType, columnType);
                    columnInfo.put(paramLength, columnLength);

                    row.put(columnName, columnInfo);
                }
                result.add(row);
            }
        } catch (SQLException e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToInsertGenerator.getSqlData error");
        }
        return result;
    }

    private void moveErrorResponse(LogicException e) {
        //        event.setPeripheryRequest();
    }
}
