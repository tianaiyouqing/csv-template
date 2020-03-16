package cloud.tianai.csv.converter;

import cloud.tianai.csv.CsvDataConverter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:32
 * @Description: 字符串转换器
 */
public class StringCsvDataConverter implements CsvDataConverter<String> {

    public static final String ONE_DOUBLE_QUOTATION_MARK = "\"";
    public static final String TWO_DOUBLE_QUOTATION_MARK = "\"\"";
    public static final String CARRIAGE_RETURN_SIGN = "\n";

    @Override
    public String converter(Integer index, String data) {
        // 字符串中不能出现双引号、逗号、换行符
        //替换双引号
        if (data.startsWith(ONE_DOUBLE_QUOTATION_MARK)) {
            int indexOf = data.indexOf(ONE_DOUBLE_QUOTATION_MARK);
            data = data.substring(indexOf);
        }
        if (data.endsWith(ONE_DOUBLE_QUOTATION_MARK)){
            int indexOf = data.lastIndexOf(ONE_DOUBLE_QUOTATION_MARK);
            data = data.substring(0, indexOf);
        }
        if(data.contains(ONE_DOUBLE_QUOTATION_MARK)) {
            data = data.replaceAll(ONE_DOUBLE_QUOTATION_MARK, TWO_DOUBLE_QUOTATION_MARK);
        }
        // 替换回车
        if(data.contains(CARRIAGE_RETURN_SIGN)) {
            data = data.replaceAll(CARRIAGE_RETURN_SIGN, "\r\n");
        }
        return ONE_DOUBLE_QUOTATION_MARK.concat(data).concat(ONE_DOUBLE_QUOTATION_MARK);
    }
}
