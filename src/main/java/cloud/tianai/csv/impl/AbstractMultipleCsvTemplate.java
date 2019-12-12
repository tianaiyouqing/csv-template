package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultipleCsvTemplate extends AbstractLazyRefreshCsvTemplate {

    /** 默认文件最大行数为10W条. */
    private static Integer DEFAULT_FILE_MAX_LINES = 100000;

    @Setter
    @Getter
    /** 单个文件最大行数. */
    private Integer fileMaxLines = DEFAULT_FILE_MAX_LINES;

    @Getter
    private List<AbstractLazyRefreshCsvTemplate> csvTemplateList = new ArrayList<>(10);

    private List<Path> filePaths = new ArrayList<>(10);

    private AbstractLazyRefreshCsvTemplate currentCsvTemplate;

    @Getter
    private String fileName;

    public AbstractMultipleCsvTemplate(Integer memoryStorageCapacity, Integer threshold) {
        super(memoryStorageCapacity, threshold);
    }

    public AbstractMultipleCsvTemplate() {
    }

    @Override
    protected void doInit(String fileName) throws CsvException {
        super.doInit(fileName);
        this.fileName = fileName;
        setPath(initMultiplePath());
        AbstractLazyRefreshCsvTemplate csvTemplate = createNewCsvTemplate(fileName);
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
        if(currentCsvTemplate.getRowNumber() >= fileMaxLines) {
            // 单文件已满，刷盘
            fileFinish();
            // 创建新的csvTemplate
            setTitle(currentCsvTemplate.getTitleData(), currentCsvTemplate.getTitleStr());
            currentCsvTemplate = createNewCsvTemplate(fileName);
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

    protected abstract AbstractLazyRefreshCsvTemplate createNewCsvTemplate(String fileName);
    protected abstract Path initMultiplePath();
    protected abstract Path mergeFile(List<Path> filePaths);
}
