package cloud.tianai.csv;

import cloud.tianai.csv.impl.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/20 20:02
 * @Description: CSV模板创建工具
 */
public class CsvWriterBuilder {
    /**
     * 自定义的Converter.
     */
    private List<CsvDataConverter> customCsvDataConverters = new ArrayList<>(20);

    /**
     * 默认是本地类型的.
     */
    private TemplateType type = TemplateType.LOCAL;
    /**
     * 是否使用多路模板， 目前只是本地类型的支持多路模板
     */
    private Boolean multipart = true;
    /**
     * 临时文件地址.
     */
    private String tempFileDirectory = "./temp";

    /**
     * 默认内存容量.
     */
    private Integer memoryStorageCapacity = 1024;

    /**
     * 阈值.
     */
    private Integer threshold = 1024;
    /**
     * 创建oss类型的数据需要的配置属性.
     */
    private OssProperties ossProperties;


    private String fileName;

    public static CsvWriterBuilder builder() {
        return new CsvWriterBuilder();
    }


    public CsvWriterBuilder addCustomConverter(CsvDataConverter converter) {
        this.customCsvDataConverters.add(converter);
        return this;
    }

    public CsvWriterBuilder tempFileDirectory(String tempFileDirectory) {
        this.tempFileDirectory = tempFileDirectory;
        return this;
    }

    public CsvWriterBuilder memoryStorageCapacity(Integer memoryStorageCapacity) {
        this.memoryStorageCapacity = memoryStorageCapacity;
        return this;
    }

    public CsvWriterBuilder threshold(Integer threshold) {
        this.threshold = threshold;
        return this;
    }

    public CsvWriterBuilder type(TemplateType type) {
        this.type = type;
        return this;
    }

    public CsvWriterBuilder oss(OssProperties ossProperties) {
        type(TemplateType.OSS);
        this.ossProperties = ossProperties;
        return this;
    }

    public CsvWriterBuilder local() {
        return local(true);
    }

    public CsvWriterBuilder memory() {
        type(TemplateType.MEMORY);
        return this;
    }
    public CsvWriterBuilder local(boolean multpart) {
        this.multipart = multpart;
        return type(TemplateType.LOCAL);
    }

    public CsvWriter build() {
        switch (type) {
            case OSS:
                return createOssTemplate();
            case LOCAL:
                return createLocalTemplate();
            case MEMORY:
                return createMemoryTemplate();
            default:
                return createLocalTemplate();
        }
    }

    private CsvWriter createMemoryTemplate() {
        return new MemoryOssCsvWriter();
    }

    public CsvWriter init(CsvWriter csvWriter) {
        csvWriter.init();
        return csvWriter;
    }

    public CsvWriterBuilder fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public CsvWriter buildAndInit() {
        CsvWriter csvWriter = build();
        init(csvWriter);
        return csvWriter;
    }

    private CsvWriter createOssTemplate() {
        assert ossProperties != null;
        CsvWriter csvWriter = new OssCsvWriter(memoryStorageCapacity, threshold, ossProperties);
        csvWriter.setFileName(fileName);
        warpCsvTemplate(csvWriter);
        return csvWriter;
    }

    private CsvWriter createLocalTemplate() {
        CsvWriter csvWriter;
        if (multipart) {
            csvWriter = new LocalFileMultipleCsvWriter(tempFileDirectory, memoryStorageCapacity, threshold);
        } else {
            csvWriter = new LocalFileCsvWriter(tempFileDirectory, memoryStorageCapacity, threshold);
        }
        csvWriter.setFileName(fileName);
        warpCsvTemplate(csvWriter);
        return csvWriter;
    }

    private void warpCsvTemplate(CsvWriter csvWriter) {
        // 设置自定义的Converter
        for (CsvDataConverter customCsvDataConverter : customCsvDataConverters) {
            csvWriter.addConverter(customCsvDataConverter);
        }
    }

    @Getter
    enum TemplateType {
        /**
         * OSS类型.
         */
        OSS,
        /**
         * 本地.
         */
        LOCAL,
        /**
         * 内存版
         */
        MEMORY
    }

}
