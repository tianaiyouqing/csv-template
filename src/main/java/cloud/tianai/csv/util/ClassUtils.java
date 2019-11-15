package cloud.tianai.csv.util;

import java.lang.reflect.Type;

public class ClassUtils {

    public static Type getDataType(Object obj) {
        assert  obj != null;
        Class<?> clazz = obj.getClass();
        return clazz;
    }
}
