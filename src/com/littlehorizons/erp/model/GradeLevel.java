package com.littlehorizons.erp.model;

/** Domain model: GradeLevel. */
public class GradeLevel {

    private Integer id;
    private String name;
    private int numericLevel;

    public GradeLevel() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumericLevel() {
        return numericLevel;
    }

    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }
}
