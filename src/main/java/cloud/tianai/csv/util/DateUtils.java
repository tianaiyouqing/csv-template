package cloud.tianai.csv.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 19:38
 * @Description: 时间工具包
 */
public class DateUtils {

    public static LocalDateTime date2LocalDateTime(Date date) {
        assert  date != null;

        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }
}
