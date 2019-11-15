package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:43
 * @Description: boolean转换器
 */
public class BooleanCsvDataConverter implements CsvDataConverter<Boolean> {
    @Override
    public String converter(Integer index, Boolean data) {
        return data.toString();
    }
}
