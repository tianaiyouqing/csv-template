package cloud.tianai.csv.impl;

import cloud.tianai.csv.CsvDataConverter;
import cloud.tianai.csv.CsvTemplate;
import cloud.tianai.csv.Path;
import cloud.tianai.csv.converter.*;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.util.ClassUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 16:11
 * @Description: 抽象的csv模板实现
 */
@Slf4j
public abstract class AbstractCsvTemplate implements CsvTemplate {

    /**
     * 当前的path路径.
     */
    @Setter
    private Path path;

    /**
     * 当前已经添加的行数，默认为0
     */
    private AtomicLong currentRowNumber = new AtomicLong(0L);

    /**
     * 如果执行了初始化方法，则设置为true， 模式是false
     */
    private Boolean init = false;

    /**
     * 数据转换器
     */
    @Setter
    @Getter
    private Map<Type, CsvDataConverter<Object>> converterMap = new HashMap<>(255);

    /**
     * 默认的数据转换器
     */
    @Setter
    @Getter
    private CsvDataConverter<Object> defaultCstDataConverter = new DefaultCsvConverter();

    @Setter
    @Getter
    private String csvSplitIdent = ",";

    @Setter
    @Getter
    private String csvLineFeedIdent = "\n";

    @Getter
    private final Object lockKey = new Object();

    private Boolean finished = false;

    @Override
    public void init(String fileName) throws CsvException {
        // 标记状态为一已执行初始化方法
        this.init = true;
        doInit(fileName);
    }

    @Override
    public Path getPath() {
        if (Objects.isNull(path) || !init) {
            throw new CsvException("path in null, Please check that init method is executed.");
        }
        return path;
    }

    @Override
    public void append(List<Object> datas) throws CsvException {
        if (!init || finished) {
            throw new CsvException("append data fail， please exec init() method or this csv is finished.");
        }
        // 转换成字符串数组
        List<String> converterData = converter(datas);
        // 合并成一行数据
        String joinStr = getLine(converterData);
        synchronized (lockKey) {
            if (finished) {
                throw new CsvException("append data fail, this csv is finished, can not append");
            }
            doAppend(joinStr);
        }
        // 记录总数
        addRowNumber(1L);
    }

    protected String getLine(List<String> converterData) {
        String joinStr = String.join(csvSplitIdent, converterData);
        // 切分后最后加上换行符
        return joinStr + csvLineFeedIdent;
    }

    protected List<String> converter(List<Object> datas) {
        List<String> converterDatas = new ArrayList<>(datas.size());
        for (int index = 0; index < datas.size(); index++) {
            Object data = datas.get(index);
            String converterData = beforeConverter(data);
            if (Objects.nonNull(converterData)) {
                converterDatas.add(converterData);
                continue;
            }
            Type dataType = ClassUtils.getDataType(data);
            CsvDataConverter<Object> csvDataConverter = getConverter(dataType);
            if (Objects.isNull(csvDataConverter)) {
                // 使用默认的数据转换器
                csvDataConverter = defaultCstDataConverter;
            }

            // 使用数据转换器转换
            converterData = csvDataConverter.converter(index, data);

            if (Objects.isNull(converterData)) {
                throw new CsvException("转换数据失败， 转换得到的数据为空， " +
                        "type [" + data + "]," +
                        " data [" + data.toString() + "], " +
                        "converter [ " + csvDataConverter + "]");
            }
            converterDatas.add(converterData);
        }
        return converterDatas;
    }

    /**
     * 通过type类型获取converter
     *
     * @param dataType converter
     * @return CsvDataConverter<Object>
     */
    protected CsvDataConverter<Object> getConverter(Type dataType) {
        CsvDataConverter<Object> csvDataConverter = converterMap.get(dataType);
        return csvDataConverter;
    }

    /**
     * 添加converter转换器
     *
     * @param type      type类型
     * @param converter 转换器
     * @return 如果有旧的converter，则返回旧的CsvDataConverter
     */
    @Override
    public CsvDataConverter addConverter(Type type, CsvDataConverter converter) {
        CsvDataConverter<Object> oldDataConverter = converterMap.get(type);
        converterMap.put(type, (CsvDataConverter<Object>) converter);
        return oldDataConverter;
    }

    @Override
    public void addAllConverter(Map<Type, CsvDataConverter<Object>> converterMap) {
        if(converterMap == null || converterMap.isEmpty()) {
            throw new CsvException("adll allConverter fail, param is null.");
        }
        this.converterMap.putAll(converterMap);
    }

    @Override
    public Long getRowNumber() {
        return currentRowNumber.get();
    }

    /**
     * 添加行数记录
     *
     * @param addRowNum 添加的数不能小于0
     * @return
     */
    protected Long addRowNumber(Long addRowNum) {
        if (Objects.isNull(addRowNum) || addRowNum < 0) {
            throw new CsvException("add rowNum fail， param must not be lt 0");
        }
        long res = currentRowNumber.addAndGet(addRowNum);
        return res;
    }

    @Override
    public Path finish() throws CsvException {
        if (finished) {
            throw new CsvException("this is csvTemplate is finished, can not repeat execute");
        }
        Path path;
        synchronized (lockKey) {
            if (finished) {
                throw new CsvException("this is csvTemplate is finished, can not repeat execute");
            }
            // 先标志为true，这样不会有线程来争抢执行finish方法
            finished = true;
            try {
                path = doFinish();
            } catch (CsvException e) {
                // 执行finish失败后重置标记为false
                finished = false;
                throw e;
            }
        }
        return path;
    }

    @Override
    public Boolean isFinish() {
        return finished;
    }

    /**
     * 执行转换数据前会调用它， 可以自定义处理
     *
     * @param data data数据
     * @return 字符串
     */
    protected String beforeConverter(Object data) {
        return null;
    }

    protected abstract Path doFinish() throws CsvException;

    protected abstract void doInit(String fileName) throws CsvException;

    protected abstract void doAppend(String joinStr) throws CsvException;
}
