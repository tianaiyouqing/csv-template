package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @Author: 天爱有情
 * @Date: 2019/12/13 20:53
 * @Description: 内存版的csv模板
 */
public class MemoryOssCsvWriter extends AbstractCsvWriter {

    /**
     * 基于byte数组的输出流.
     */
    private ByteArrayOutputStream outputStream;

    /**
     * 基于byte数组的输入流 .
     */
    private ByteArrayInputStream inputStream;

    /**
     * 默认的初始化容量大小.
     */
    public static final Integer DEFAULT_INIT_CAPACITY = 2048;

    @Override
    protected Path doFinish() throws CsvException {
        try {
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new CsvException(e);
        }
        return new Path();
    }

    @Override
    protected void doInit() throws CsvException {
        // 初始化
        doInit(DEFAULT_INIT_CAPACITY);
    }

    protected void doInit(int capacity) throws CsvException {
        // 初始化
        outputStream = new ByteArrayOutputStream(capacity);
        // 指定头为utf-8
        try {
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        } catch (IOException e) {
            throw new CsvException(e);
        }
    }

    @Override
    protected void doAppend(String data) throws CsvException {
        byte[] bytes = StandardCharsets.UTF_8.encode(data).array();
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new CsvException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        if (!isFinish()) {
            throw new CsvException("read inputStream fail, must be exec finish() method.");
        }
        return inputStream;
    }
}
