package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;
import cloud.tianai.csv.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
