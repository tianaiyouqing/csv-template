package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMultipleCsvWriter extends AbstractLazyRefreshCsvWriter {

    /**
     * 默认文件最大行数为10W条.
     */
    private static Integer DEFAULT_FILE_MAX_LINES = 100000;

    @Setter
    @Getter
    /** 单个文件最大行数. */
    private Integer fileMaxLines = DEFAULT_FILE_MAX_LINES;

    /**
     * 执行过包括正在执行的csv模板
     */
    @Getter
    private List<AbstractLazyRefreshCsvWriter> csvTemplateList = new LinkedList<>();

    /**
     * 所有生成的文件路径
     */
    private List<Path> filePaths = new LinkedList<>();

    /**
     * 当前正在执行的模板
     */
    private AbstractLazyRefreshCsvWriter currentCsvTemplate;

    public AbstractMultipleCsvWriter(Integer memoryStorageCapacity, Integer threshold) {
        super(memoryStorageCapacity, threshold);
    }

    public AbstractMultipleCsvWriter() {
    }

    @Override
    protected void doInit() throws CsvException {
        super.doInit();
        setPath(initMultiplePath());
        AbstractLazyRefreshCsvWriter csvTemplate = createNewCsvTemplate(getFileName());
        csvTemplateList.add(csvTemplate);
        currentCsvTemplate = csvTemplate;
    }

    @Override
    protected void refreshStorage(String data) {
        currentCsvTemplate.refreshStorage(data);
    }

    @Override
    public void append(List<Object> datas) throws CsvException {
        if (!init || finished) {
            throw new CsvException("append data fail， please exec init() method or this csv is finished.");
        }
        if (currentCsvTemplate.getRowNumber() >= fileMaxLines) {
            // 单文件已满，刷盘
            fileFinish();
            // 创建新的csvTemplate
            setTitle(currentCsvTemplate.getTitleData(), currentCsvTemplate.getTitleStr());
            currentCsvTemplate = createNewCsvTemplate(getFileName());
            csvTemplateList.add(currentCsvTemplate);
            // 添加表头
            currentCsvTemplate.append(getTitleData());
        }
        currentCsvTemplate.append(datas);
        // 记录行数
        addRowNumber(1L);
    }

    @Override
    protected Path innerFinish() {
        if (!currentCsvTemplate.isFinish()) {
            Path path = currentCsvTemplate.finish();
            filePaths.add(path);
        }
        return mergeFile(filePaths);
    }


    protected void fileFinish() {
        Path path = currentCsvTemplate.finish();
        filePaths.add(path);
    }

    /**
     * 创建一个新的csv模板
     * @param fileName
     * @return
     */
    protected abstract AbstractLazyRefreshCsvWriter createNewCsvTemplate(String fileName);

    /**
     * 初始化多文件路径
     * @return
     */
    protected abstract Path initMultiplePath();

    /**
     * 合并文件， （压缩文件为一个路径）
     * @param filePaths
     * @return
     */
    protected abstract Path mergeFile(List<Path> filePaths);
}
