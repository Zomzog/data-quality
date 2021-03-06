package org.talend.dataquality.datamasking.generic;

import java.math.BigInteger;

import org.talend.dataquality.sampling.exception.DQRuntimeException;

/**
 *
 */
public class FieldDefinition {

    public enum FieldDefinitionType {
        DATEPATTERN("DATEPATTERN"),
        INTERVAL("INTERVAL"),
        ENUMERATION("ENUMERATION"),
        ENUMERATION_FROM_FILE("ENUMERATION_FROM_FILE");

        private String componentValue;

        FieldDefinitionType(String componentValue) {
            this.componentValue = componentValue;
        }

        public String getComponentValue() {
            return componentValue;
        }

        public static FieldDefinitionType getTypeByComponentValue(String typeName) {
            for (FieldDefinitionType v : FieldDefinitionType.values()) {
                if (v.getComponentValue().equals(typeName)) {
                    return v;
                }
            }
            throw new IllegalArgumentException("Unknown FieldDefinitionType: " + typeName);
        }
    }

    private FieldDefinitionType type;

    private String value;

    private BigInteger min;

    private BigInteger max;

    public FieldDefinition(String inputType, String value, String interval) {
        this.type = FieldDefinitionType.getTypeByComponentValue(inputType);
        this.value = value;
        if (type.equals(FieldDefinitionType.INTERVAL) || type.equals(FieldDefinitionType.DATEPATTERN)) {
            String[] values = interval.split(",");
            if (values.length != 2)
                throw new DQRuntimeException(
                        "The interval " + interval + " is not well-defined. Please use the following syntax: \"1,10\"");
            this.min = new BigInteger(values[0]);
            this.max = new BigInteger(values[1]);
        }
    }

    public FieldDefinitionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public BigInteger getMin() {
        return min;
    }

    public BigInteger getMax() {
        return max;
    }

}
