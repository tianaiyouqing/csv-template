package cloud.tianai.csv.exception;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 16:05
 * @Description: csv相关异常
 */
public class CsvException extends RuntimeException {

    public CsvException() {
    }

    public CsvException(String message) {
        super(message);
    }

    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvException(Throwable cause) {
        super(cause);
    }

    public CsvException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
