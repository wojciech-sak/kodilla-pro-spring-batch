package com.kodilla.task.batch.io.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonBirth {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
}
