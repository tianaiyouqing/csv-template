package cloud.tianai.csv.util;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static void createEmptyFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file为空");
        }
        if (!file.getParentFile().exists()) {
            // 创建文件夹
            boolean mkdirs = file.getParentFile().mkdirs();
            if (!mkdirs) {
                // 创建文件夹失败
                throw new IllegalArgumentException("创建文件夹失败, 检查用户是否有[创建文件]权限, dirs=["
                        + file.getParentFile().getPath() + "]");
            }
        }
        boolean newFile = file.createNewFile();
        if (!newFile) {
            // 创建文件失败
            throw new IOException("创建文件失败, 检查用户是否有[创建文件]权限, file=["
                    + file.getPath() + "]");
        }

    }
}
