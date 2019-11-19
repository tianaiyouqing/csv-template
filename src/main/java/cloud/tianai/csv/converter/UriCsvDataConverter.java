package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

import java.net.URI;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/19 20:10
 * @Description: uri转换
 */
public class UriCsvDataConverter implements CsvDataConverter<URI> {
    @Override
    public String converter(Integer index, URI data) {
        return data.toString();
    }
}
