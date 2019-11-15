package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:40
 * @Description: integer 转换
 */
public class IntegerCsvDataConverter implements CsvDataConverter<Integer> {
    @Override
    public String converter(Integer index, Integer data) {
        return String.valueOf(data);
    }
}
