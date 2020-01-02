package cloud.tianai.csv.serialize.impl;

import cloud.tianai.csv.CsvDataConverter;
import cloud.tianai.csv.converter.DefaultCsvConverter;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.serialize.CsvObjectSerializable;
import cloud.tianai.csv.util.ClassUtils;
import cloud.tianai.csv.util.ResolvableType;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @Author: 天爱有情
 * @Date: 2019/12/27 23:08
 * @Description: 默认的csv对象序列化
 */
public class DefaultCsvObjectSerializable implements CsvObjectSerializable {

    /** 转换器 map. */
    private Map<Type, CsvDataConverter<Object>> converterMap;

    /** 默认的转换器. */
    private CsvDataConverter<Object> defaultCsvDataConverter;

    public DefaultCsvObjectSerializable() {
        this(new HashMap<>(12));
    }

    public DefaultCsvObjectSerializable(Map<Type, CsvDataConverter<Object>> converterMap) {
        this(converterMap, new DefaultCsvConverter());
    }

    public DefaultCsvObjectSerializable(Map<Type, CsvDataConverter<Object>> converterMap,
                                        CsvDataConverter<Object> defaultCsvDataConverter) {
        this.converterMap = converterMap;
        this.defaultCsvDataConverter = defaultCsvDataConverter;
    }


    @Override
    public List<String> serialize(List<Object> datas) {
        List<String> converterDatas = new ArrayList<>(datas.size());
        for (int index = 0; index < datas.size(); index++) {
            Object data = datas.get(index);
            String converterData = beforeConverter(data);
            if (Objects.nonNull(converterData)) {
                converterDatas.add(converterData);
                continue;
            }
            Type dataType = ClassUtils.getDataType(data);
            CsvDataConverter<Object> csvDataConverter = getConverter(dataType);
            if (Objects.isNull(csvDataConverter)) {
                // 使用默认的数据转换器
                csvDataConverter = defaultCsvDataConverter;
            }
            // 使用数据转换器转换
            converterData = csvDataConverter.converter(index, data);

            if (Objects.isNull(converterData)) {
                throw new CsvException("转换数据失败， 转换得到的数据为空， " +
                        "type [" + data + "]," +
                        " data [" + data.toString() + "], " +
                        "converter [ " + csvDataConverter + "]");
            }
            converterDatas.add(converterData);
        }
        return converterDatas;
    }

    protected String beforeConverter(Object data) {
        if(Objects.isNull(data)) {
            // 如果是空， 直接返回空字符串
            return "";
        }
        return null;
    }

    @Override
    public void addConverter(CsvDataConverter<?> converter) {
        ResolvableType resolvableType = ResolvableType.forClass(converter.getClass()).as(CsvDataConverter.class);
        ResolvableType[] generics = resolvableType.getGenerics();
        if(generics.length < 1 || generics[0].resolve() == null) {
            throw new CsvException("add[CsvDataConverter] fail, match not Type.");
        }
        Class<?> type =  generics[0].resolve();
        converterMap.put(type, (CsvDataConverter<Object>) converter);
    }

    @Override
    public void addConverter(Map<Type, CsvDataConverter<Object>> converterMap) {
        this.converterMap.putAll(converterMap);
    }

    @Override
    public void setConverter(Map<Type, CsvDataConverter<Object>> converterMap) {
        this.converterMap = converterMap;
    }

    protected CsvDataConverter<Object> getConverter(Type dataType) {
        CsvDataConverter<Object> csvDataConverter = converterMap.get(dataType);
        return csvDataConverter;
    }
}
