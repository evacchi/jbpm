package org.jbpm.process.workitem.TestServiceWithTypedParams;

import java.time.Duration;
import java.time.LocalDate;

public class PersonResult {
    int age;

    public PersonResult() {}

    public PersonResult(Person person) {
        this.age = (int) Duration.between(
                person.getBirthDate(),
                LocalDate.now()).toDays();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
