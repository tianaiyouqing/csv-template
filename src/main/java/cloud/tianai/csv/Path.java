package cloud.tianai.csv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 15:57
 * @Description: path路径
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Path {

    /** 路径. */
    private String path;

    /** url. */
    private URL url;

    /** 是否是本地. */
    private boolean local;
}
