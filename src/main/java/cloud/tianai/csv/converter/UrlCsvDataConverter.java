package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

import java.net.URL;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/19 20:10
 * @Description: URL类型转换
 */
public class UrlCsvDataConverter implements CsvDataConverter<URL> {
    @Override
    public String converter(Integer index, URL data) {
        String url = data.toString();
        return url;
    }
}
