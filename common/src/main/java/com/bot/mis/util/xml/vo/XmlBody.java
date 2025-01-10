/* (C) 2024 */
package com.bot.mis.util.xml.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class XmlBody {

    @JacksonXmlProperty(localName = "useSqlData")
    private boolean useSqlData;

    @JacksonXmlProperty(localName = "separator")
    private String separator;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "field")
    private List<XmlField> fieldList = new ArrayList<>();

    // Getters and Setters
}
