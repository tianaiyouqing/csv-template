package cloud.tianai.csv;

import cloud.tianai.csv.converter.*;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.impl.LocalFileCsvTemplate;
import cloud.tianai.csv.impl.LocalFileMultipleCsvTemplate;
import cloud.tianai.csv.impl.OssCsvTemplate;
import cloud.tianai.csv.impl.OssProperties;
import cloud.tianai.csv.util.ResolvableType;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/20 20:02
 * @Description: CSV模板创建工具
 */
public class CsvTemplateBuilder {

    /** 内置的一些Converter. */
    private static Map<Type, CsvDataConverter<Object>> converterMap = new HashMap<>(20);
    static {
        addConverter(new BooleanCsvDataConverter());
        addConverter(new DateCsvDataConverter());
        addConverter(new IntegerCsvDataConverter());
        addConverter(new LongCsvDataConverter());
        addConverter(new StringCsvDataConverter());
        addConverter(new DoubleCsvDataConverter());
        addConverter(new UrlCsvDataConverter());
        addConverter(new UriCsvDataConverter());
    }
    /** 自定义的Converter. */
    private List<CsvDataConverter> customCsvDataConverters = new ArrayList<>(20);

    /** 默认是本地类型的. */
    private TemplateType type = TemplateType.LOCAL;

    /** 临时文件地址. */
    private String tempFileDirectory = "./temp";

    /** 默认内存容量. */
    private Integer memoryStorageCapacity = 1024;

    /** 阈值. */
    private Integer threshold = 1024;
    /** 创建oss类型的数据需要的配置属性. */
    private OssProperties ossProperties;

    private Boolean multpart = true;

    public static CsvDataConverter addConverter(CsvDataConverter converter) {
        ResolvableType resolvableType = ResolvableType.forClass(converter.getClass()).as(CsvDataConverter.class);
        ResolvableType[] generics = resolvableType.getGenerics();
        if (generics.length < 1 || generics[0].resolve() == null) {
            throw new CsvException("add[CsvDataConverter] fail, match not Type.");
        }
        Class<?> type = generics[0].resolve();
        CsvDataConverter<Object> oldDataConverter = converterMap.get(type);
        converterMap.put(type, (CsvDataConverter<Object>) converter);
        return oldDataConverter;
    }

    public static CsvTemplateBuilder build() {
        return new CsvTemplateBuilder();
    }


    public CsvTemplateBuilder addCustomConverter(CsvDataConverter converter) {
        this.customCsvDataConverters.add(converter);
        return this;
    }

    public CsvTemplateBuilder tempFileDirectory(String tempFileDirectory) {
        this.tempFileDirectory = tempFileDirectory;
        return this;
    }

    public CsvTemplateBuilder memoryStorageCapacity(Integer memoryStorageCapacity) {
        this.memoryStorageCapacity = memoryStorageCapacity;
        return this;
    }
    public CsvTemplateBuilder threshold(Integer threshold) {
        this.threshold = threshold;
        return this;
    }

    public CsvTemplateBuilder type(TemplateType type) {
        this.type = type;
        return this;
    }

    public CsvTemplateBuilder oss(OssProperties ossProperties) {
        type(TemplateType.OSS);
        this.ossProperties = ossProperties;
        return this;
    }
    public CsvTemplateBuilder local() {
        return local(true);
    }
    public CsvTemplateBuilder local(boolean multpart) {
        this.multpart = multpart;
        return type(TemplateType.LOCAL);
    }
    public CsvTemplate builder() {
        switch (type) {
            case OSS:
                return createOssTemplate();
            case LOCAL:
                return createLocalTemplate();
            default:
                return createLocalTemplate();
        }
    }

    public CsvTemplate init(CsvTemplate csvTemplate, String fileName) {
        csvTemplate.init(fileName);
        return csvTemplate;
    }

    public CsvTemplate builderAndInit(String fileName) {
        CsvTemplate csvTemplate = builder();
        init(csvTemplate, fileName);
        return csvTemplate;
    }

    private CsvTemplate createOssTemplate() {
        assert ossProperties != null;
        CsvTemplate csvTemplate = new OssCsvTemplate(memoryStorageCapacity, threshold, ossProperties);
        warpCsvTemplate(csvTemplate);
        return csvTemplate;
    }

    private CsvTemplate createLocalTemplate() {
        CsvTemplate csvTemplate;
        if(multpart) {
            csvTemplate = new LocalFileMultipleCsvTemplate(tempFileDirectory, memoryStorageCapacity, threshold);
        }else {
            csvTemplate = new LocalFileCsvTemplate(tempFileDirectory, memoryStorageCapacity, threshold);
        }
        warpCsvTemplate(csvTemplate);
        return csvTemplate;
    }

    private void warpCsvTemplate(CsvTemplate csvTemplate) {
        csvTemplate.addAllConverter(converterMap);
        // 设置自定义的Converter
        for (CsvDataConverter customCsvDataConverter : customCsvDataConverters) {
            csvTemplate.addConverter(customCsvDataConverter);
        }
    }

    @Getter
    static enum TemplateType {
        /** OSS类型. */
        OSS,
        /** 本地. */
        LOCAL
    }

}
