package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:32
 * @Description: 字符串转换器
 */
public class StringCsvDataConverter implements CsvDataConverter<String> {

    @Override
    public String converter(Integer index, String data) {
        // 字符串中不能出现双引号、逗号、换行符
        //替换双引号
        if(data.contains("\"")) {
            data = data.replaceAll("\"", "\"\"");
        }
        // 替换回车
        if(data.contains("\n")) {
            data = data.replaceAll("\n", "\r\n");
        }
        // 替换逗号
        if(data.contains(",")) {
            data = data.replaceAll(",", " ");
        }
        return data;
    }
}
