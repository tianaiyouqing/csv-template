package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 17:39
 * @Description: 异步刷新CSV模板， 等数据达到阈值后再进行刷盘操作，可以利用内存提升性能
 */
public abstract class AbstractLazyRefreshCsvTemplate extends AbstractCsvTemplate {

    /** 内存存储. */
    private StringBuilder memoryStorage;
    /** 刷盘阈值. */
    private Integer threshold;

    public AbstractLazyRefreshCsvTemplate(Integer memoryStorageCapacity, Integer threshold) {
        assert  memoryStorageCapacity < 1;
        assert  threshold < 1;
        this.memoryStorage = new StringBuilder(memoryStorageCapacity);
        this.threshold = threshold;
    }

    public AbstractLazyRefreshCsvTemplate() {
        this(1024, 1024);
    }

    @Override
    protected Path doFinish() throws CsvException {
        // finish执行时，把剩下的数据刷盘后返回
        if(memoryStorage.length() > 0) {
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
        if(memoryStorage.length() >= threshold) {
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

    protected abstract void refreshStorage(String toString);

    protected abstract Path innerFinish();
}
