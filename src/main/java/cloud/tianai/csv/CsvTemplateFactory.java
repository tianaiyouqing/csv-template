package cloud.tianai.csv;

import cloud.tianai.csv.converter.*;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.impl.LocalFileCsvTemplate;
import cloud.tianai.csv.impl.OssCsvTemplate;
import cloud.tianai.csv.impl.OssProperties;
import cloud.tianai.csv.util.ResolvableType;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CsvTemplateFactory {

    private static Map<Type, CsvDataConverter<Object>> converterMap = new HashMap<>(255);

    static {
        addConverter(new BooleanCsvDataConverter());
        addConverter(new DateCsvDataConverter());
        addConverter(new IntegerCsvDataConverter());
        addConverter(new LongCsvDataConverter());
        addConverter(new StringCsvDataConverter());
        addConverter(new DoubleCsvDataConverter());
        addConverter(new UrlCsvDataConverter());
        addConverter(new UriCsvDataConverter());
    }

    public static CsvDataConverter addConverter(CsvDataConverter converter) {
        ResolvableType resolvableType = ResolvableType.forClass(converter.getClass()).as(CsvDataConverter.class);
        ResolvableType[] generics = resolvableType.getGenerics();
        if (generics.length < 1 || generics[0].resolve() == null) {
            throw new CsvException("add[CsvDataConverter] fail, match not Type.");
        }
        Class<?> type = generics[0].resolve();
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
}
