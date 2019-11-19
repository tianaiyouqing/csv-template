package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;
import cloud.tianai.csv.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/19 18:05
 * @Description: Date 转换器
 */
public class DateCsvDataConverter implements CsvDataConverter<Date> {

    @Getter
    @Setter
    private String dataFormat;

    public DateCsvDataConverter(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public DateCsvDataConverter() {
        this("yyyy/MM/dd HH:mm:ss");
    }

    @Override
    public String converter(Integer index, Date data) {
        String format = DateTimeFormatter.ofPattern(dataFormat).format(DateUtils.date2LocalDateTime(data));
        return format;
    }
}
