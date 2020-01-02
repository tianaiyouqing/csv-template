package cloud.tianai.csv.impl;

import cloud.tianai.csv.Path;
import cloud.tianai.csv.exception.CsvException;
import cloud.tianai.csv.util.FileUtils;
import cloud.tianai.csv.util.PathUtils;
import cloud.tianai.csv.util.ZipUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class LocalFileMultipleCsvWriter extends AbstractMultipleCsvWriter {

    private File compressFile;
    private String compressFileName;
    private String tempFileDirectory;

    public LocalFileMultipleCsvWriter(String tempFileDirectory, Integer memoryStorageCapacity, Integer threshold) {
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
    protected AbstractLazyRefreshCsvWriter createNewCsvTemplate(String fileName) {
        String warpFileName = warpFileName(compressFileName);
        LocalFileCsvWriter localFileCsvWriter = new LocalFileCsvWriter();
        // 共用同一个内存数据
        System.out.println("生成 LocalFileCsvWriter2");
        localFileCsvWriter.setMemoryStorage(super.getMemoryStorage());
        localFileCsvWriter.setFileName(warpFileName);
        localFileCsvWriter.setCsvObjectSerializable(getCsvObjectSerializable());
        localFileCsvWriter.init();
        // 添加转换器
        return localFileCsvWriter;
    }

    private String warpFileName(String fileName) {
        int fileNums = getCsvTemplateList().size();
        fileName = PathUtils.subSuffix(fileName, getCsvFileSuffix());
        return fileName + "-" + ((++fileNums) + getCsvFileSuffix());
    }

    @Override
    protected Path initMultiplePath() {
        String fileName =  PathUtils.subSuffix(getFileName(), getCsvFileSuffix());
        String fileDirectory = getTemplateFileDirectory();
        String filePath = PathUtils.getFilePath(fileDirectory, fileName, "", getCompressSuffix());
        File file = new File(filePath);
        if (file.exists()) {
            for (int index = 0; file.exists(); index++) {
                filePath = PathUtils.getFilePath(fileDirectory, fileName, "-" + index, getCompressSuffix());
                file = new File(filePath);
            }
        }
        // 创建空文件
        try {
            FileUtils.createEmptyFile(file);
        } catch (IOException e) {
            throw new CsvException(e);
        }
        compressFile = file;
        compressFileName = PathUtils.subSuffix(compressFile.getName(), getCompressSuffix());
        try {
            return new Path(compressFile.getPath(), compressFile.toURI().toURL(), true);
        } catch (MalformedURLException e) {
            throw new CsvException(e);
        }
    }

    private String getCompressSuffix() {
        return ZipUtils.ZIP_FILE_SUFFIX;
    }

    @Override
    protected Path mergeFile(List<Path> filePaths) {
        // 转换为 File
        List<File> fileList = filePaths.stream().map(p -> new File(p.getPath())).collect(Collectors.toList());
        // 多文件压缩
        ZipUtils.compress(compressFile, fileList);
        return getPath();
    }

    private String getTemplateFileDirectory() {
        return this.tempFileDirectory;
    }

    @Override
    public InputStream getInputStream() {
        if(!isFinish()) {
            throw new CsvException("read inputStream fail, must be exec finish() method.");
        }
        try {
            return new FileInputStream(compressFile);
        } catch (FileNotFoundException e) {
            // 文件不存在
            throw new CsvException(e);
        }
    }
}
