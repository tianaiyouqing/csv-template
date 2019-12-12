package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;

import java.io.*;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LocalFileMultipleCsvTemplate extends AbstractMultipleCsvTemplate {

    private File zipFile;
    private String zipFileName;
    private String tempFileDirectory;

    public LocalFileMultipleCsvTemplate(String tempFileDirectory, Integer memoryStorageCapacity, Integer threshold) {
        super(memoryStorageCapacity, threshold);
        if (!tempFileDirectory.endsWith("/")) {
            tempFileDirectory += "/";
        }
        LocalDateTime now = LocalDateTime.now();
        // 临时目录中加入时间区分
        String format = DateTimeFormatter.ofPattern("yyyy/MM/dd/").format(now);
        this.tempFileDirectory = tempFileDirectory + format;
    }

    @Override
    protected AbstractLazyRefreshCsvTemplate createNewCsvTemplate(String fileName) {
        String warpFileName = warpFileName(zipFileName);
        LocalFileCsvTemplate localFileCsvTemplate = new LocalFileCsvTemplate();
        // 共用同一个内存数据
        localFileCsvTemplate.setMemoryStorage(super.getMemoryStorage());
        localFileCsvTemplate.init(warpFileName);
        // 添加转换器
        localFileCsvTemplate.setConverterMap(getConverterMap());
        return localFileCsvTemplate;
    }

    private String warpFileName(String fileName) {
        int fileNums = getCsvTemplateList().size();
        fileName = subSuffix(fileName, ".csv");
        return fileName + "-" + ((++fileNums) + ".csv");
    }

    private String subSuffix(String fileName, String suffix) {
        if (fileName.endsWith(suffix)) {
            fileName = fileName.substring(0, fileName.lastIndexOf(suffix));
        }
        return fileName;
    }

    @Override
    protected Path initMultiplePath() {
        String fileName = subSuffix(getFileName(), ".csv");
        String fileDirectory = getTemplateFileDirectory();
        String filePath = getFilePath(fileDirectory, fileName, "");
        File file = new File(filePath);
        if (file.exists()) {
            for (int index = 0; file.exists(); index++) {
                filePath = getFilePath(fileDirectory, fileName, "-" + index);
                file = new File(filePath);
            }
        }
        // 创建空文件
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
        zipFile = file;
        zipFileName = subSuffix(zipFile.getName(), ".zip");
        try {
            return new Path(zipFile.getPath(), zipFile.toURI().toURL(), true);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new CsvException(e);
        }
    }

    @Override
    protected Path mergeFile(List<Path> filePaths) {
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(zipFile);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            ZipEntry zipEntry;
            FileInputStream fis = null;
            for (Path filePath : filePaths) {
                File file = new File(filePath.getPath());
                if (file.exists()) {
                    try {
                        zipEntry = new ZipEntry(file.getName());
                        zipOutputStream.putNextEntry(zipEntry);
                        int len = 0;
                        //缓冲
                        byte[] buffer = new byte[1024];
                        fis = new FileInputStream(file);
                        while ((len = fis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, len);
                            zipOutputStream.flush();
                        }
                    } finally {
                        //关闭FileInputStream
                        if (fis != null) {
                            fis.close();
                        }
                    }
                }
            }
            return getPath();
        } catch (Exception e) {
            throw new CsvException(e);
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.closeEntry();
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTemplateFileDirectory() {
        return this.tempFileDirectory;
    }

    private String getFilePath(String fileDirectory, String fileName, String salt) {
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(fileName.indexOf("/"));
        }
        // 加后缀
        fileName += salt + ".zip";
        return fileDirectory + fileName;
    }
}
