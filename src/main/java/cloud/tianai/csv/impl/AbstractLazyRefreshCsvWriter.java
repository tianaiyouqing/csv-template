package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 17:39
 * @Description: 异步刷新CSV模板， 等数据达到阈值后再进行刷盘操作，可以利用内存提升性能
 */
@Slf4j
public abstract class AbstractLazyRefreshCsvWriter extends AbstractCsvWriter {

    /**
     * 内存存储.
     */
    @Setter
    @Getter
    private StringBuilder memoryStorage;

    @Setter
    @Getter
    /** 刷盘阈值. */
    private Integer threshold = 1024;

    @Setter
    @Getter
    /** 内存容量. */
    private Integer memoryStorageCapacity = 1024;

    public AbstractLazyRefreshCsvWriter(Integer memoryStorageCapacity, Integer threshold) {
        assert memoryStorageCapacity < 1;
        assert threshold < 1;
        this.memoryStorageCapacity = memoryStorageCapacity;
        this.threshold = threshold;
    }

    @Override
    protected void doInit() throws CsvException {
        // 初始化内存存储器
        if (Objects.isNull(memoryStorage)) {
            this.memoryStorage = new StringBuilder(memoryStorageCapacity);
            log.debug("memoryStorage init , capacity is " + memoryStorage);
        }
    }

    public AbstractLazyRefreshCsvWriter() {
        this(1024, 1024);
    }

    @Override
    protected Path doFinish() throws CsvException {
        // finish执行时，把剩下的数据刷盘后返回
        if (memoryStorage.length() > 0) {
            // 最后刷盘
            refreshStorage(memoryStorage.toString());
            memoryStorage.delete(0, memoryStorage.length());
        }
        return innerFinish();
    }

    @Override
    protected void doAppend(String joinStr) throws CsvException {
        // 加入内存数据库
        memoryStorage.append(joinStr);
        // 检查是否可以刷盘
        if (needPersistence()) {
            try {
                refreshStorage(memoryStorage.toString());
                // 刷盘完成后清空
                memoryStorage.delete(0, memoryStorage.length());
            } catch (Exception e) {
                // 如果刷盘失败， 暂时等待一下， 不影响程序继续执行
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否需要刷盘持久化
     *
     * @return
     */
    protected boolean needPersistence() {
        return memoryStorage.length() >= threshold;
    }

    protected abstract void refreshStorage(String data);

    protected abstract Path innerFinish();
}
