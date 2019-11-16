package cloud.tianai.csv.impl;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/16 16:08
 * @Description: oss属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
}
