/* (C) 2024 */
package com.bot.common.util.sql.svcimpl;

import com.bot.common.util.sql.config.AliasToEntityLinkHashMapResultTransformer;
import com.bot.common.util.sql.svc.ExecuteSqlService;
import com.bot.common.util.xml.config.SecureXmlMapper;
import com.bot.common.util.xml.vo.XmlData;
import com.bot.common.util.xml.vo.XmlField;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.persistence.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@EnableTransactionManagement
@SuppressWarnings({"unchecked", "deprecation"})
public class ExecuteSqlServiceImpl implements ExecuteSqlService {
    @Value("${localFile.dev.xml.todb.directory}")
    private String todbFileDir;

    @Autowired private EntityManager entityManager;

    @Override
    public List<Map<String, String>> select(String sql, Object... params) {

        // Query
        Query query = entityManager.createNativeQuery(sql);

        if (params.length > 0) {
            if (params.length % 2 != 0) {
                throw new IllegalArgumentException(
                        "Invalid number of parameters. Expected key-value pairs.");
            }

            for (int i = 0; i < params.length; i += 2) {
                String key = (String) params[i];
                Object value = params[i + 1];
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "params = {},{}", key, value);
                query.setParameter(key, value);
            }
        }
        // 處理查詢結果排序
        query =
                query.unwrap(NativeQueryImpl.class)
                        .setResultTransformer(AliasToEntityLinkHashMapResultTransformer.INSTANCE);

        // 執行查詢並返回結果
        return convert(query.getResultList());
    }

    @Override
    public List<Map<String, String>> queryByXml(
            String sql,
            List<XmlField> xmlFieldList,
            Map<String, String> requestParam,
            List<String> xmlParamList) {

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "requestParam = {}", requestParam);

        Query query = entityManager.createNativeQuery(sql);

        for (String param : xmlParamList) {
            if (!param.isEmpty()) {
                query.setParameter(param, requestParam.get(param));
            }
        }

        query =
                query.unwrap(NativeQueryImpl.class)
                        .setResultTransformer(AliasToEntityLinkHashMapResultTransformer.INSTANCE);
        // 執行查詢並返回結果
        return convert(query.getResultList());
    }

    private List<Map<String, String>> convert(List rows) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (Object obj : rows) {
            Map<String, Object> row = (LinkedHashMap<String, Object>) obj;
            Map<String, String> m = new LinkedHashMap<String, String>();
            Set<String> set = row.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = "";

                if (row.get(key) != null && row.get(key) instanceof Clob) {
                    Clob c = (Clob) row.get(key);
                    try {
                        value = c.getSubString(1, (int) c.length());
                    } catch (SQLException e) {
                        m.put(key, "");
                    }
                } else if (row.get(key) != null) value = row.get(key).toString();

                m.put(key, value);
            }
            result.add(m);
        }
        return result;
    }

    @Override
    public void insert(String sql) {

        exec(sql, new Object());
    }

    @Override
    public void insert(String sql, Object... params) {
        if ("insert into".contains(sql.toLowerCase())) {
            exec(sql, params);
        } else {
            throw new LogicException("", "非 INSERT INTO 語法，請確認");
        }
    }

    @Override
    public void update(String sql) {

        exec(sql, new Object());
    }

    @Override
    public void update(String sql, Object... params) {
        if ("update".contains(sql.toLowerCase())) {
            exec(sql, params);
        } else {
            throw new LogicException("", "非 UPDATE 語法，請確認");
        }
    }

    @Override
    public void delete(String sql) {
        exec(sql, new Object());
    }

    @Override
    public void delete(String sql, Object... params) {

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "delete = {}", sql.toLowerCase());
        if (sql.toLowerCase().contains("delete")) {
            exec(sql, params);
        } else {
            throw new LogicException("", "非 DELETE 語法，請確認");
        }
    }

    @Override
    public void uspToDb(String fileName, Object... params) {
        try {
            // XML檔案路徑(讀取)
            String xml = todbFileDir + fileName + ".xml";

            // 解析XML檔案格式
            XmlMapper xmlMapper = SecureXmlMapper.createXmlMapper();

            File file = new File(xml);
            XmlData xmlData = xmlMapper.readValue(file, XmlData.class);

            String sql = xmlData.getSql().getSqlText();

            exec(sql, params);

        } catch (Exception e) {
            ApLogHelper.info(
                    log, false, LogType.NORMAL.getCode(), "ExecuteSqlServiceImpl.uspToDb error");
            throw new LogicException("", "ExecuteSqlServiceImpl.uspToDb error " + e.getMessage());
        }
    }

    /**
     * 執行INSERT,UPDATE,DELETE語句(單次執行)
     *
     * @param sql 語句
     * @param params key與value成對代入
     */
    private void exec(String sql, Object... params) {

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "params.length = {}", params.length);

        try {
            // 建立 Native Query
            Query query = entityManager.createNativeQuery(sql);

            if (params.length > 0) {
                if (params.length % 2 != 0) {
                    throw new IllegalArgumentException(
                            "Invalid number of parameters. Expected key-value pairs.");
                }

                for (int i = 0; i < params.length; i += 2) {
                    String key = (String) params[i];
                    Object value = params[i + 1];
                    ApLogHelper.info(
                            log, false, LogType.NORMAL.getCode(), "params = {},{}", key, value);
                    query.setParameter(key, value);
                }
            }

            query.executeUpdate();

            // 如果執行成功，返回 true
        } catch (Exception e) {
            // 如果發生錯誤，打印錯誤信息並返回 false
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Failed to execute sql");
            throw new LogicException("", "Error executing sql " + e.getMessage());
        }
    }
}
