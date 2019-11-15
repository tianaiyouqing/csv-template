package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:55
 * @Description: double converter
 */
public class DoubleCsvDataConverter implements CsvDataConverter<Double> {
    @Override
    public String converter(Integer index, Double data) {
        return String.valueOf(data);
    }
}
