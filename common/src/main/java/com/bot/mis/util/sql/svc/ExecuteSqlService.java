/* (C) 2024 */
package com.bot.mis.util.sql.svc;

import com.bot.mis.util.xml.vo.XmlField;
import java.util.List;
import java.util.Map;

public interface ExecuteSqlService {

    /**
     * 使用原生SQL欄位與XML欄位Mapping之查詢資料結果
     *
     * @param sql SQL 查詢語句
     * @param xmlFieldList XML欄位
     * @param requestParam request参数
     * @param xmlParamList xml参数
     * @return List<Map < String, String>> 查詢結果的列表
     */
    List<Map<String, String>> queryByXml(
            String sql,
            List<XmlField> xmlFieldList,
            Map<String, String> requestParam,
            List<String> xmlParamList);

    /**
     * 執行INSERT語句
     *
     * @param sqlOrXml sql語句
     */
    void insert(String sqlOrXml);

    /**
     * 執行INSERT語句
     *
     * @param sqlOrXml sql語句
     * @param params key與value成對代入
     */
    void insert(String sqlOrXml, Object... params);

    /**
     * 執行UPDATE語句
     *
     * @param sqlOrXml sql語句
     */
    void update(String sqlOrXml);

    /**
     * 執行UPDATE語句
     *
     * @param sqlOrXml sql語句
     * @param params key與value成對代入
     */
    void update(String sqlOrXml, Object... params);

    /**
     * 執行DELETE語句
     *
     * @param sqlOrXml sql語句
     */
    void delete(String sqlOrXml);

    /**
     * 執行DELETE語句
     *
     * @param sqlOrXml sql語句
     * @param params key與value成對代入
     */
    void delete(String sqlOrXml, Object... params);

    /**
     * 執行xml的SQL語法
     *
     * @param fileName 檔案名稱(XML)
     * @param params key與value成對代入 EX:uspToDb("ABC","batchDate",batchDate,"brno",brno... )
     */
    void uspToDb(String fileName, Object... params);

    /**
     * 使用原生SQL欄位與XML欄位Mapping之查詢資料結果
     *
     * <p>讀取xml裡的SQL語法
     *
     * @param fileName 檔案名稱(XML)
     * @param params key與value成對代入 EX:uspToDb("ABC","batchDate",batchDate,"brno",brno... )
     * @return List<Map < String, String>> 查詢結果的列表
     */
    List<Map<String, String>> select(String fileName, Object... params);
}
