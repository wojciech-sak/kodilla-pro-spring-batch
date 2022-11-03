package com.kodilla.task.batch.io.config;

import com.kodilla.task.batch.io.domain.PersonAge;
import com.kodilla.task.batch.io.domain.PersonBirth;
import com.kodilla.task.batch.io.editor.LocalDateEditor;
import com.kodilla.task.batch.io.processor.PersonProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.beans.PropertyEditor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    FlatFileItemReader<PersonBirth> reader() {
        FlatFileItemReader<PersonBirth> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("input.csv"));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("firstName", "lastName", "birthDate");
        tokenizer.setDelimiter(";");

        BeanWrapperFieldSetMapper<PersonBirth> mapper = new BeanWrapperFieldSetMapper<>();

        Map<Class<?>, PropertyEditor> editors = new HashMap<>();
        LocalDateEditor dateEditor = new LocalDateEditor(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        editors.put(LocalDate.class, dateEditor);

        mapper.setCustomEditors(editors);
        mapper.setTargetType(PersonBirth.class);

        DefaultLineMapper<PersonBirth> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    PersonProcessor processor() {
        return new PersonProcessor();
    }

    @Bean
    FlatFileItemWriter<PersonAge> writer() {
        BeanWrapperFieldExtractor<PersonAge> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"firstName", "lastName", "age"});

        DelimitedLineAggregator<PersonAge> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        FlatFileItemWriter<PersonAge> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("src/main/resources/output.csv"));
        writer.setShouldDeleteIfExists(true);
        writer.setLineAggregator(aggregator);

        return writer;
    }

    @Bean
    Step birthDateToAgeChange(
            ItemReader<PersonBirth> reader,
            ItemProcessor<PersonBirth, PersonAge> processor,
            ItemWriter<PersonAge> writer) {

        return stepBuilderFactory.get("birthDateToAgeChange")
                .<PersonBirth, PersonAge>chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    Job changeBirthDateToAgeJob(Step birthDateToAgeChange) {
        return jobBuilderFactory.get("changeBirthDateToAgeJob")
                .incrementer(new RunIdIncrementer())
                .flow(birthDateToAgeChange)
                .end()
                .build();
    }
}
