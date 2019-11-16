package cloud.tianai.csv;

import cloud.tianai.csv.converter.*;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.impl.LocalFileCsvTemplate;
import cloud.tianai.csv.impl.OssCsvTemplate;
import cloud.tianai.csv.impl.OssProperties;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CsvTemplateFactory {

    private static Map<Type, CsvDataConverter<Object>> converterMap = new HashMap<>(255);

    static {
        addConverter(Boolean.class, new BooleanCsvDataConverter());
        addConverter(Date.class, new DateCsvDataConverter());
        addConverter(Integer.class, new IntegerCsvDataConverter());
        addConverter(Long.class, new LongCsvDataConverter());
        addConverter(String.class, new StringCsvDataConverter());
        addConverter(Double.class, new DoubleCsvDataConverter());
    }

    public static CsvDataConverter addConverter(Type type, CsvDataConverter converter) {
        CsvDataConverter<Object> oldDataConverter = converterMap.get(type);
        converterMap.put(type, (CsvDataConverter<Object>) converter);
        return oldDataConverter;
    }

    public static CsvTemplate createCsvTemplate(String fileName) {
        CsvTemplate csvTemplate = new LocalFileCsvTemplate("./temp", 1024, 1024);
        csvTemplate.addAllConverter(converterMap);
        csvTemplate.init(fileName);
        return csvTemplate;
    }

    public static CsvTemplate createCsvTemplate(String fileName, OssProperties ossProperties) {
        CsvTemplate csvTemplate = new OssCsvTemplate(1024, 1024, ossProperties);
        csvTemplate.addAllConverter(converterMap);
        csvTemplate.init(fileName);
        return csvTemplate;
    }

    public enum CsvTemplateEnum {
        /** 默认. */
        LOCAL,
        /** oss. */
        OSS
    }
}
