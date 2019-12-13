package cloud.tianai.csv.util;

import cloud.tianai.csv.exception.CsvException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: 天爱有情
 * @Date: 2019/12/13 12:29
 * @Description: zip压缩工具包
 */
@Slf4j
public class ZipUtils {

    /** zip文件的后缀名. */
    public static final String ZIP_FILE_SUFFIX = ".zip";

    public static void compress(File zipFile, Collection<File> srcFiles) {
        ZipOutputStream zipOutputStream = null;
        FileOutputStream zipFileOutputStream = null;
        try {
            if(!zipFile.exists()) {
                // 创建空文件夹
                zipFile.getParentFile().mkdirs();
                // 创建zip文件
                zipFile.createNewFile();
            }
            zipFileOutputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(zipFileOutputStream);
            ZipEntry zipEntry;
            FileInputStream fis = null;
            for (File srcFile : srcFiles) {
                try {
                    if (srcFile.exists()) {
                        zipEntry = new ZipEntry(srcFile.getName());
                        zipOutputStream.putNextEntry(zipEntry);
                        int len = 0;
                        // 缓存
                        byte[] buffer = new byte[1024];
                        fis = new FileInputStream(srcFile);
                        while ((len = fis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, len);
                            zipOutputStream.flush();
                        }
                    }
                } catch (IOException e) {
                    log.error("zip压缩失败, e={}", e);
                } finally {
                    if(fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            log.warn("zip压缩警告， io流关闭异常, e={}", e);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.error("zip压缩失败, e={}", e);
            throw new CsvException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CsvException(e);
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                }
                if (zipFileOutputStream != null) {
                    zipFileOutputStream.close();
                }
            } catch (IOException e) {
                log.warn("zip压缩警告， io流关闭异常, e={}", e);
            }
        }
    }

}
