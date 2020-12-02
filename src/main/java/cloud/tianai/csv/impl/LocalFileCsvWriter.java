package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.util.PathUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 18:11
 * @Description: 使用本地文件的方式进行缓存
 * todo 2020年1月2日17:47:15 修改追加文件为顺序写
 */
@Slf4j
public class LocalFileCsvWriter extends AbstractLazyRefreshCsvWriter {

    @Setter
    @Getter
    private String tempFileDirectory;

    /** 文件流管道 */
    private FileChannel fileChannel;

    private File execFile;

    /** 写的时候需要加的锁 */
    private ReentrantLock putDataNormalLock = new ReentrantLock();

    /** 正斜杠. */
    public static final String FORWARD_SLASH = "/";
    /** 反斜杠. */
    public static final String BACK_SLASH = "\\\\";

    /** 写的位点 */
    protected final AtomicInteger writePosition = new AtomicInteger(0);

    public LocalFileCsvWriter(String tempFileDirectory, Integer memoryStorageCapacity, Integer threshold) {
        super(memoryStorageCapacity, threshold);
        if (tempFileDirectory.contains(BACK_SLASH)){
            tempFileDirectory = tempFileDirectory.replaceAll(BACK_SLASH, FORWARD_SLASH);
        }
        if (!tempFileDirectory.endsWith(FORWARD_SLASH)) {
            tempFileDirectory += FORWARD_SLASH;
        }
        LocalDateTime now = LocalDateTime.now();
        // 临时目录中加入时间区分
        String format = DateTimeFormatter.ofPattern("yyyy/MM/dd/").format(now);
        this.tempFileDirectory = tempFileDirectory + format;
    }

    public LocalFileCsvWriter() {
        this("./temp/", 1024, 1024);
    }

    @Override
    protected void refreshStorage(String data) {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(data);
        putDataNormalLock.lock();
        try {
            appendData(data.getBytes(Charset.forName("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CsvException(e);
        }finally {
            putDataNormalLock.unlock();
        }
    }

    @Override
    protected Path innerFinish() {
        // 关闭管道流
        if (fileChannel != null) {
            try {
                fileChannel.force(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getPath();
    }

    @Override
    protected void doInit() throws CsvException {
        // 调用父级初始化
        super.doInit();
        if(Objects.isNull(getFileName()))  {
            throw new CsvException("init fail please set FileName");
        }
        String subFileName = PathUtils.subSuffix(getFileName(), getCsvFileSuffix());
        String filePath = PathUtils.getFilePath(tempFileDirectory, subFileName, "", getCsvFileSuffix());
        execFile = new File(filePath);
        if (execFile.exists()) {
            for (int index = 0; execFile.exists(); index++) {
                filePath = PathUtils.getFilePath(tempFileDirectory, subFileName, "-" + index, getCsvFileSuffix());
                execFile = new File(filePath);
            }
        }
        // 创建空文件
        ensureDirOK(execFile.getParent());

        // 创建流
        try {
            fileChannel = new RandomAccessFile(execFile, "rw").getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
        // 指定头为utf-8
        appendData(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        // 包装返回值数据
        try {
            Path path = new Path(execFile.getPath(), execFile.toURI().toURL(), true);
            setPath(path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
    }

    public static void ensureDirOK(final String dirName) {
        if (dirName != null) {
            File f = new File(dirName);
            if (!f.exists()) {
                boolean result = f.mkdirs();
                log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
            }
        }
    }

    @Override
    public InputStream getInputStream() {
        if(!isFinish()) {
            throw new CsvException("read inputStream fail, must be exec finish() method.");
        }
        try {
            return new FileInputStream(execFile);
        } catch (FileNotFoundException e) {
            // 文件不存在
            throw new CsvException(e);
        }
    }

    public boolean appendData(byte[] data) {
        int currentPos = writePosition.get();
        try {
            this.fileChannel.position(currentPos);
            this.fileChannel.write(ByteBuffer.wrap(data));
            this.writePosition.addAndGet(data.length);
            return true;
        } catch (IOException e) {
            log.error("Error occurred when append message to mappedFile.", e);
        }
        return  false;
    }
}
