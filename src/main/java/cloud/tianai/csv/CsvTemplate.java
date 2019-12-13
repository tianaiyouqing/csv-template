package cloud.tianai.csv;

import cloud.tianai.csv.exception.CsvException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/15 15:54
 * @Description: CSV 模板
 */
public interface CsvTemplate {

    /**
     * 初始化
     */
    void init();

    /** 设置文件名称. */
    void setFileName(String fileName);

    /** 获取文件名称. */
    String getFileName();
    /**
     * 获取路径
     *
     * @return
     */
    Path getPath();

    /**
     * 添加数据
     *
     * @param datas 待添加的数据
     * @throws CsvException 添加失败可能抛出的异常
     */
    void append(List<Object> datas) throws CsvException;

    /**
     * 获取当前行数
     *
     * @return Long
     */
    Long getRowNumber();

    /**
     * 添加完成要执行的方法
     *
     * @return Path
     */
    Path finish();

    /**
     * 是否已经完成
     *
     * @return
     */
    Boolean isFinish();

    /**
     * 添加 converter
     *
     * @param converter
     * @return
     */
    CsvDataConverter addConverter(CsvDataConverter converter);

    /**
     * 一次性添加多个converter
     *
     * @param converterMap
     */
    void addAllConverter(Map<Type, CsvDataConverter<Object>> converterMap);

    /**
     * 获取表头数据
     * @return
     */
    List<Object> getTitleData();

    /**
     * 获取表头转换后的str
     * @return
     */
    String getTitleStr();

    /**
     * 获取输出流， 必须是在执行了finish方法之后才能获取文件流
     * @return InputStream
     */
    InputStream getInputStream();
}
