package cloud.tianai.csv;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

/**
 * @Author: 天爱有情
 * @Date: 2019/12/27 23:44
 * @Description: csv读取器
 */
public interface CsvReader {

    <T> CsvReadResult<T> read(InputStream inputStream, Function<List<String>, T> parse, Object position);

}
