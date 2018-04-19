package org.jbpm.process.core.datatype.impl;

import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.BooleanDataType;
import org.jbpm.process.core.datatype.impl.type.FloatDataType;
import org.jbpm.process.core.datatype.impl.type.IntegerDataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;

public class DataTypes {

    public static DataType ofClass(Class<?> type) {
        return fromCanonicalName(type.getCanonicalName());
    }

    public static DataType fromString(String type) {
        try {
            Class<?> c = type.getClass().getClassLoader().loadClass(type);
            return DataTypes.ofClass(c);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static DataType fromCanonicalName(String type) {
        switch (type) {
            case "java.lang.Boolean":
            case "Boolean":
                return new BooleanDataType();
            case "java.lang.Integer":
            case "Integer":
                return new IntegerDataType();
            case "java.lang.Float":
            case "Float":
                return new FloatDataType();
            case "java.lang.String":
            case "String":
                return new StringDataType();
            case "java.lang.Object":
            case "Object":
                return new ObjectDataType("java.lang.Object");
            default:
                return new ObjectDataType(type);
        }
    }
}
