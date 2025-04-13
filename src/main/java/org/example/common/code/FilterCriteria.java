package org.example.common.code;

import java.util.List;

public class FilterCriteria {
    private String field;

    public String getField() {
        return field;
    }

    public FilterCriteria(String field, List<Object> value, Operator operator) {
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    private List<Object> value;
    private Operator operator;
}
