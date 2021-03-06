package com.kedzie.vbox.api.jaxb;

public enum AdditionsUpdateFlag implements java.io.Serializable{
    NONE("None"),
    WAIT_FOR_UPDATE_START_ONLY("WaitForUpdateStartOnly");
    private final String value;
    public String toString() {
        return value;
    }
    AdditionsUpdateFlag(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    public static AdditionsUpdateFlag fromValue(String v) {
        for (AdditionsUpdateFlag c: AdditionsUpdateFlag.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
