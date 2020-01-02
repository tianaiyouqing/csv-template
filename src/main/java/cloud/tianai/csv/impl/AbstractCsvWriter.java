package cloud.tianai.csv.impl;

import cloud.tianai.csv.CsvDataConverter;
import cloud.tianai.csv.CsvWriter;
import cloud.tianai.csv.Path;
import cloud.tianai.csv.converter.*;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.serialize.CsvObjectSerializable;
import cloud.tianai.csv.serialize.impl.DefaultCsvObjectSerializable;
import cloud.tianai.csv.util.ClassUtils;
import cloud.tianai.csv.util.ResolvableType;
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
public abstract class AbstractCsvWriter implements CsvWriter {
//    /**
//     * 数据转换器
//     */
//    @Setter
//    @Getter
//    private Map<Type, CsvDataConverter<Object>> converterMap = new HashMap<>(255);
//
//    /**
//     * 默认的数据转换器
//     */
//    @Setter
//    @Getter
//    private CsvDataConverter<Object> defaultCstDataConverter = new DefaultCsvConverter();

    @Setter
    @Getter
    private CsvObjectSerializable csvObjectSerializable;

    /**
     * 当前的path路径.
     */
    @Setter
    private Path path;

    /** 表头的数据. */
    private List<Object> titleData;

    /** 表头的字符串. */
    private String titleStr;

    /**
     * 当前已经添加的行数，默认为0
     */
    private AtomicLong currentRowNumber = new AtomicLong(0L);

    /**
     * 如果执行了初始化方法，则设置为true， 模式是false
     */
    protected Boolean init = false;

    @Setter
    @Getter
    private String csvSplitIdent = ",";

    @Setter
    @Getter
    private String csvLineFeedIdent = "\n";

    @Getter
    private final Object lockKey = new Object();

    protected Boolean finished = false;

    /** 默认的csv文件后缀名. */
    private static final String DEFAULT_CSV_FILE_SUFFIX = ".csv";

    @Setter
    @Getter
    /** 指定csv文件的后缀名. */
    private String csvFileSuffix = DEFAULT_CSV_FILE_SUFFIX;

    private String fileName;

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void init() {
        // 标记状态为一已执行初始化方法
        this.init = true;
        // 初始化 csvObjectSerializable
        initCsvObjectSerializable();
        // 子类实现
        doInit();
    }

    protected void initCsvObjectSerializable() {
        if(Objects.isNull(this.csvObjectSerializable)) {
            // 如果为空，则创建默认的csvObjectSerializable
            csvObjectSerializable = createCsvObjectSerializable();
        }
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
        List<String> converterData = serialize(datas);
        // 合并成一行数据
        String joinStr = getLine(converterData);
        synchronized (lockKey) {
            if (finished) {
                throw new CsvException("append data fail, this csv is finished, can not append");
            }
            if(isTitle()) {
                setTitle(datas, joinStr);
            }
            doAppend(joinStr);
        }
        // 记录总数
        addRowNumber(1L);
    }

    protected List<String> serialize(List<Object> datas) {
        CsvObjectSerializable csvObjectSerializable = getCsvObjectSerializable();
        if(Objects.isNull(csvObjectSerializable)) {
            throw new CsvException("serialize fail, CsvObjectSerializable in null");
        }
        List<String> serialize = csvObjectSerializable.serialize(datas);
        return serialize;
    }

    private CsvObjectSerializable createCsvObjectSerializable() {
        DefaultCsvObjectSerializable serializable = new DefaultCsvObjectSerializable();
        serializable.addConverter(new BooleanCsvDataConverter());
        serializable.addConverter(new DateCsvDataConverter());
        serializable.addConverter(new DoubleCsvDataConverter());
        serializable.addConverter(new IntegerCsvDataConverter());
        serializable.addConverter(new LongCsvDataConverter());
        serializable.addConverter(new StringCsvDataConverter());
        serializable.addConverter(new UriCsvDataConverter());
        serializable.addConverter(new UrlCsvDataConverter());
        return serializable;
    }

    protected void setTitle(List<Object> data, String dataStr) {
        this.titleData = new ArrayList<>(data);
        this.titleStr = dataStr;
    }

    protected boolean isTitle() {
        return getRowNumber().equals(0L) && Objects.isNull(titleData);
    }

    protected String getLine(List<String> converterData) {
        String joinStr = String.join(csvSplitIdent, converterData);
        // 切分后最后加上换行符
        return joinStr + csvLineFeedIdent;
    }

    /**
     * 添加converter转换器
     *
     * @param converter 转换器
     * @return 如果有旧的converter，则返回旧的CsvDataConverter
     */
    @Override
    public void addConverter(CsvDataConverter converter) {
        if(Objects.isNull(this.csvObjectSerializable)) {
            throw new CsvException("addConverter fail, CsvObjectSerializable in null");
        }
        this.csvObjectSerializable.addConverter(converter);
    }

    @Override
    public void addAllConverter(Map<Type, CsvDataConverter<Object>> converterMap) {
        if(Objects.isNull(this.csvObjectSerializable)) {
            throw new CsvException("addConverter fail, CsvObjectSerializable in null");
        }
        this.csvObjectSerializable.addConverter(converterMap);
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

    @Override
    public List<Object> getTitleData() {
        return titleData;
    }

    @Override
    public String getTitleStr() {
        return titleStr;
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

    protected abstract void doInit() throws CsvException;

    protected abstract void doAppend(String joinStr) throws CsvException;
}
