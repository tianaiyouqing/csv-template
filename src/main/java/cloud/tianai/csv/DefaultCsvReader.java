package cloud.tianai.csv;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * @Author: 天爱有情
 * @Date: 2020/01/02 17:44
 * @Description: csv读取器，
 */
public class DefaultCsvReader implements CsvReader {


    @Override
    public <T> CsvReadResult<T> read(InputStream inputStream, Function<List<String>, T> parse, Object position) {
//        inputStream.read(null, null, null);
        Scanner scanner = new Scanner(inputStream);
//        scanner.nextLine()
        InputStreamReader reader = new InputStreamReader(inputStream);
        LineNumberReader lineNumberReader = new LineNumberReader(reader);

//        lineNumberReader.readLine()

        //todo 暂时还没实现
        return null;
    }
}
