package com.kodilla.task.batch.io.processor;

import com.kodilla.task.batch.io.domain.PersonAge;
import com.kodilla.task.batch.io.domain.PersonBirth;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.time.Period;

public class PersonProcessor implements ItemProcessor<PersonBirth, PersonAge> {

    @Override
    public PersonAge process(PersonBirth person) {
        return new PersonAge(
                person.getFirstName(),
                person.getLastName(),
                Period.between(person.getBirthDate(), LocalDate.now()).getYears());
    }
}
