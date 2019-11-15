package cloud.tianai.csv;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 16:30
 * @Description: csv的数据转换
 */
@FunctionalInterface
public interface CsvDataConverter<T extends Object> {

    /**
     * 转换器，将对于的数据转换成字符串
     * @param index 当前索引,可以判断是第几列
     * @param data 待转换的数据
     * @return 返回字符串
     */
    String converter(Integer index, T data);
}
