package cc.venja.minebbs.robot.dao;

import java.util.HashMap;
import java.util.Map;

public class RespondDao {

    public Integer respondCode = RespondCode.NULL.value;
    public String respondData = null;

    public boolean isValid() {
        return (respondCode != RespondCode.NULL.value) && (respondData != null);
    }

    @Override
    public String toString() {
        return "RespondDao{" +
                "respondCode=" + respondCode +
                ", respondData='" + respondData + '\'' +
                '}';
    }

    public enum RespondCode {
        NULL(100),
        EXISTED(110),
        FAILED(500),
        SUCCESS(200);

        final int value;
        static final Map<Integer, RespondCode> BY_ID = new HashMap<>();

        RespondCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static RespondCode getByValue(int value) {
            return BY_ID.get(value);
        }

        static {
            for (RespondCode role : values()) {
                BY_ID.put(role.getValue(), role);
            }
        }
    }
}
