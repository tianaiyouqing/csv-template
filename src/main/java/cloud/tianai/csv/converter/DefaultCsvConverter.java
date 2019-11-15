package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 16:47
 * @Description: 默认的通用数据转换器
 */
public class DefaultCsvConverter implements CsvDataConverter<Object> {
    @Override
    public String converter(Integer index, Object data) {

        return data.toString();
    }
}
