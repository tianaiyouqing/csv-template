package cloud.tianai.csv.util;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Author: 天爱有情
 * @Date: 2019/11/19 19:02
 * @Description: 反射类型工具包
 */
public class ResolvableType {

    public static final ResolvableType NONE = new ResolvableType(EmptyType.INSTANCE);

    public static final ResolvableType[] EMPTY_TYPES_ARRAY = new ResolvableType[0];
    private volatile ResolvableType[] generics;
    private Class<?> resolved;
    private Type type;

    public ResolvableType(Class<?> clazz) {
        this.resolved = (clazz != null ? clazz : Object.class);
        this.type = this.resolved;
    }

    public ResolvableType(Type type) {
        this.type = type;
        this.resolved = resolveClass();
    }

    private Class<?> resolveClass() {
        if(type instanceof Class) {
            return (Class<?>) type;
        }
        if(type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if(rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }

        return null;
    }

    public static ResolvableType forClass(Class<?> clazz) {
        return new ResolvableType(clazz);
    }

    public ResolvableType as(Class<?> type) {
        Class<?> resolved = resolve();
        if(resolved == null || resolved == type) {
            return this;
        }
        for (ResolvableType resolvableType : getInterfaces()) {
            ResolvableType interfaceAsType = resolvableType.as(type);
            if(interfaceAsType != null) {
                return interfaceAsType;
            }
        }
        return getSuperType().as(type);
    }

    public ResolvableType getSuperType() {
        Class<?> resolved = resolve();
        if (resolved == null || resolved.getGenericSuperclass() == null) {
            return NONE;
        }
        return new ResolvableType(resolved.getGenericSuperclass());
    }

    public ResolvableType[] getInterfaces() {
        if(resolved == null) {
            return EMPTY_TYPES_ARRAY;
        }
        Type[] genericInterfaces = resolved.getGenericInterfaces();
        ResolvableType[] resolvableTypes = new ResolvableType[genericInterfaces.length];
        for (int i = 0; i < genericInterfaces.length; i++) {
            resolvableTypes[i] =  new ResolvableType(genericInterfaces[i]);
        }
        return resolvableTypes;
    }

    public ResolvableType[] getGenerics() {
        ResolvableType[] generics = this.generics;
        if(generics ==  null) {
            if(this.type instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) this.type).getActualTypeArguments();
                generics = new ResolvableType[actualTypeArguments.length];
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    generics[i] = new ResolvableType(actualTypeArguments[i]);
                }
            }
            this.generics = generics;
        }
        return generics;
    }

    public Class<?> resolve() {
        return resolved;
    }

    /**
     * Internal {@link Type} used to represent an empty value.
     */
    @SuppressWarnings("serial")
    static class EmptyType implements Type, Serializable {

        static final Type INSTANCE = new EmptyType();

        Object readResolve() {
            return INSTANCE;
        }
    }
}
