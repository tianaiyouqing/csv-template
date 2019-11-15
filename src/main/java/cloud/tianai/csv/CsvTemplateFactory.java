package cloud.tianai.csv;

import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.impl.LocalFileCsvTemplate;

public class CsvTemplateFactory {

    public static CsvTemplate createCsvTemplate(String fileName) {
        return createCsvTemplate(fileName, CsvTemplateEnum.DEFAULT);
    }

    public static CsvTemplate createCsvTemplate(String fileName, CsvTemplateEnum type) {
        CsvTemplate csvTemplate;
        switch (type) {
            case DEFAULT:
                csvTemplate = new LocalFileCsvTemplate();
                break;
            case OSS:
                throw new CsvException("OSS Template not exists");
            default:
                csvTemplate = new LocalFileCsvTemplate();
        }
        csvTemplate.init(fileName);

        return csvTemplate;
    }


    public enum CsvTemplateEnum {
        /** 默认. */
        DEFAULT,
        /** oss. */
        OSS
    }
}
