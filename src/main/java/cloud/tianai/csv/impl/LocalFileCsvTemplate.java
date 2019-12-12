package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 18:11
 * @Description: 使用本地文件的方式进行缓存
 */
public class LocalFileCsvTemplate extends AbstractLazyRefreshCsvTemplate {

    @Setter
    @Getter
    private String tempFileDirectory;

    private FileOutputStream fos;
    private FileChannel channel;


    public LocalFileCsvTemplate(String tempFileDirectory, Integer memoryStorageCapacity, Integer threshold) {
        super(memoryStorageCapacity, threshold);

        if(!tempFileDirectory.endsWith("/")) {
            tempFileDirectory += "/";
        }
        LocalDateTime now = LocalDateTime.now();
        // 临时目录中加入时间区分
        String format = DateTimeFormatter.ofPattern("yyyy/MM/dd/").format(now);
        this.tempFileDirectory =  tempFileDirectory + format;
    }

    public LocalFileCsvTemplate() {
        this("./temp/", 1024, 1024);
    }

    @Override
    protected void refreshStorage(String data) {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(data);
        try {
            FileLock lock = channel.lock();

            channel.write(byteBuffer);

            lock.release();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
    }

    @Override
    protected Path innerFinish() {
        // 关闭管道流
        if(channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 关闭文件流
        if(fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getPath();
    }

    @Override
    protected void doInit(String fileName) throws CsvException {
        // 调用父级初始化
        super.doInit(fileName);

        String filePath = getFilePath(fileName, "");
        File file = new File(filePath);
        if(file.exists()) {
            for (int index = 0; file.exists(); index++) {
                filePath = getFilePath(fileName, "-" + index);
                file = new File(filePath);
            }
        }
        // 创建空文件
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
        // 创建流
        try {
            fos = new FileOutputStream(file);
            channel = fos.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
        // 指定头为utf-8
        try {
            channel.write(ByteBuffer.wrap(new byte []{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
        } catch (IOException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }

        // 包装返回值数据
        try {
            Path path = new Path(file.getPath(), file.toURI().toURL(), true);
            setPath(path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
    }

    private String getFilePath(String fileName, String salt) {
        if(fileName.startsWith("/")) {
            fileName = fileName.substring(fileName.indexOf("/"));
        }
        if(fileName.endsWith(".csv")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".csv"));
        }
        // 加后缀
        fileName += salt + ".csv";
        return tempFileDirectory + fileName;
    }
}
