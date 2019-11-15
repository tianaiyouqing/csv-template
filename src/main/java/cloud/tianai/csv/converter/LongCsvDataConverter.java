package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:41
 * @Description: Long converter
 */
public class LongCsvDataConverter implements CsvDataConverter<Long> {
    @Override
    public String converter(Integer index, Long data) {
        return String.valueOf(data);
    }
}
