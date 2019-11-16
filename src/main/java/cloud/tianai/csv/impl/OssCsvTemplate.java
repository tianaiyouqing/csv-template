package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.AppendObjectResult;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/16 16:49
 * @Description: OSS客户端
 */
@Slf4j
public class OssCsvTemplate extends AbstractLazyRefreshCsvTemplate {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private Date expiration;

    // 内部使用的属性
    private OSS ossClient;
    private Long position = 0L;
    private Integer index = -1;
    private String fileKey;

    public OssCsvTemplate() {
    }

    public OssCsvTemplate(Integer memoryStorageCapacity,
                          Integer threshold,
                          OssProperties ossProperties) {
        super(memoryStorageCapacity, threshold);
        this.endpoint = ossProperties.getEndpoint();
        this.accessKeyId = ossProperties.getAccessKeyId();
        this.accessKeySecret = ossProperties.getAccessKeySecret();
        this.bucketName = ossProperties.getBucketName();
        this.expiration = ossProperties.getExpiration();
    }

    @Override
    protected void refreshStorage(String data) {
        byte[] bytes = StandardCharsets.UTF_8.encode(data).array();
        AppendObjectRequest appendObjectRequest = new AppendObjectRequest(
                bucketName,
                fileKey,
                new ByteArrayInputStream(bytes));
        appendObjectRequest.setPosition(position);
        AppendObjectResult appendObjectResult = ossClient.appendObject(appendObjectRequest);
        position = appendObjectResult.getNextPosition();
    }

    @Override
    protected Path innerFinish() {
        if(ossClient != null) {
            ossClient.shutdown();
        }

        return getPath();
    }

    @Override
    protected void doInit(String fileName) throws CsvException {
        super.doInit(fileName);
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        initFile(ossClient, fileName);
    }

    private void initFile(OSS ossClient, String fileName) {
        fileKey = warpFileName(fileName);
        ObjectMetadata meta = new ObjectMetadata();
        // 指定上传的内容类型。
        meta.setContentType("text/plain");
        //对utf-8的支持
        byte[] bs = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucketName, fileKey, new ByteArrayInputStream(bs), meta);
        try {
            appendObjectRequest.setPosition(position);
            AppendObjectResult appendObjectResult = ossClient.appendObject(appendObjectRequest);
            position = appendObjectResult.getNextPosition();
        } catch (OSSException e) {
            if ("PositionNotEqualToLength".equals(e.getErrorCode())) {
                //说明有脏数据，重新生成key
                log.info("oss上传csvkey值重复, 更换key值");
                String endwith = "";
                if(fileName.endsWith(".csv")) {
                    endwith = ".csv";
                    fileName = fileName.substring(0, fileName.lastIndexOf(".csv"));
                }
                // 如果key重复， 则生成新的key值
                initFile(ossClient, fileName + "-" + (++index) + endwith);
            } else {
                throw e;
            }
        }
        // 设置path路径
        URL url = ossClient.generatePresignedUrl(bucketName, fileKey, expiration);
        Path path = new Path(url.getPath(), url, false);
        setPath(path);
    }

    private String warpFileName(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        // 临时目录中加入时间区分
        String format = DateTimeFormatter.ofPattern("yyyy/MM/dd/").format(now);
        if(fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        if(!fileName.endsWith(".csv")) {
            fileName += ".csv";
        }
        return format + fileName;
    }
}
