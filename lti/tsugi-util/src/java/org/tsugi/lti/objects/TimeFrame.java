package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TimeFrame {
    
    @JacksonXmlProperty(localName = "begin")
    private String begin;
    
    @JacksonXmlProperty(localName = "end")
    private String end;
    
    @JacksonXmlProperty(localName = "restrict")
    private Boolean restrict;
    
    @JacksonXmlProperty(localName = "adminPeriod")
    private AdminPeriod adminPeriod;
}
