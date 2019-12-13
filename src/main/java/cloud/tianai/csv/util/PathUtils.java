package cloud.tianai.csv.util;

public class PathUtils {

    public static String getFilePath(String fileDirectory, String fileName, String salt, String suffix) {
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(fileName.indexOf("/"));
        }
        // 加后缀
        fileName += salt + suffix;
        return fileDirectory + fileName;
    }

    public static String subSuffix(String fileName, String suffix) {
        if (fileName.endsWith(suffix)) {
            fileName = fileName.substring(0, fileName.lastIndexOf(suffix));
        }
        return fileName;
    }
}
