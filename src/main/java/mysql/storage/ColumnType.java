package mysql.storage;

import lombok.Getter;

public enum ColumnType {
    INTEGER(Integer.class) {
        @Override
        public Object copyValue(Object v) {
            return v;
        }

        @Override
        public int compare(Object left, Object right) {
            return (Integer) left - (Integer) right;
        }
    }, VARCHAR(String.class) {
        @Override
        public Object copyValue(Object v) {
            return v;
        }

        @Override
        public int compare(Object left, Object right) {
            return left.toString().compareTo((String) right);
        }
    }, BOOLEAN(Boolean.class) {
        @Override
        public Object copyValue(Object v) {
            return v;
        }

        @Override
        public int compare(Object left, Object right) {
            return ((Boolean) left).compareTo((Boolean) right);
        }
    };
    @Getter
    private final Class<?> javaType;

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

    public abstract int compare(Object left, Object right);

    public boolean isSupportedAutoIncrement() {
        return this.javaType == Integer.class;
    }
}
