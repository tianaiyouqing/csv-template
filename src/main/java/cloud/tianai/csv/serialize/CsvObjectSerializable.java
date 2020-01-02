package cloud.tianai.csv.serialize;

import cloud.tianai.csv.CsvDataConverter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2019/12/27 23:01
 * @Description: csv数据序列化接口
 */
public interface CsvObjectSerializable {

    /**
     * 序列化
     * @param datas 数据
     * @return 序列化后的数据
     */
    List<String> serialize(List<Object> datas);

    /**
     * 添加converter
     * @param converter
     */
    void addConverter(CsvDataConverter<?> converter);
    /**
     * 添加converter
     * @param converterMap
     */
    void addConverter(Map<Type, CsvDataConverter<Object>> converterMap);
    /**
     * 直接setConverter
     * @param converterMap
     */
    void setConverter(Map<Type, CsvDataConverter<Object>> converterMap);

}
