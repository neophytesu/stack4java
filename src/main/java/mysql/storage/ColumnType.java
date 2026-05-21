package mysql.storage;

import lombok.Getter;

public enum ColumnType {
    INTEGER(Integer.class) {
        @Override
        public boolean equalsValue(Object a, Object b) {
            return ((Integer) a).intValue() == ((Integer) b).intValue();
        }

        @Override
        public Object copyValue(Object v) {
            return v;
        }
    }, VARCHAR(String.class) {
        @Override
        public boolean equalsValue(Object a, Object b) {
            return a.equals(b);
        }

        @Override
        public Object copyValue(Object v) {
            return v;
        }
    }, BOOLEAN(Boolean.class) {
        @Override
        public boolean equalsValue(Object a, Object b) {
            return ((Boolean) a).booleanValue() == ((Boolean) b).booleanValue();
        }

        @Override
        public Object copyValue(Object v) {
            return v;
        }
    };
    @Getter
    private final Class<?> javaType;

    public abstract boolean equalsValue(Object a, Object b);

    public abstract Object copyValue(Object v);

    ColumnType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public boolean refuse(Object value) {
        if (value == null) {
            return false;
        }
        return !javaType.isInstance(value);
    }
}
