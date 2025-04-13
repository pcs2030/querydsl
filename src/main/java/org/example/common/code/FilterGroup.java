package org.example.common.code;

import graphql.com.google.common.collect.ImmutableMap;
import graphql.com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterGroup {

    public FilterGroup() {
    }

    public List<FilterCriteria> getAnd() {
        return and;
    }

    public FilterGroup(List<FilterCriteria> and) {
        this.and = and;
    }

    public void setAnd(List<FilterCriteria> and) {
        this.and = and;
    }

    public List<FilterCriteria> getOr() {
        return or;
    }

    public void setOr(List<FilterCriteria> or) {
        this.or = or;
    }

    public List<String> getAndKeys() {
        return and.stream().map(cr->cr.getField()).collect(Collectors.toUnmodifiableList());
    }

    public List<String> getOrKeys() {
        return or.stream().map(cr->cr.getField()).collect(Collectors.toUnmodifiableList());
    }

    public ImmutableMap<String, FilterCriteria> getAndMap() {
        return Maps
                .uniqueIndex(and, FilterCriteria::getField);
    }

    public ImmutableMap<String, FilterCriteria> getOrMap() {
        return Maps
                .uniqueIndex(or, FilterCriteria::getField);
    }

    private List<FilterCriteria> and = new ArrayList<>();
    private List<FilterCriteria> or = new ArrayList<>();
}
