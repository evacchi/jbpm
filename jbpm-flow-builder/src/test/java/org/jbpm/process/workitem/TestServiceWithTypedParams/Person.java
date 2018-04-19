package org.jbpm.process.workitem.TestServiceWithTypedParams;

import java.time.LocalDate;

public class Person {
    String name;
    LocalDate birthDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
