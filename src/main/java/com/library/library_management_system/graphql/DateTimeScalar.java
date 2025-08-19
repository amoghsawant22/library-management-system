package com.library.library_management_system.graphql;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom GraphQL scalar for LocalDateTime
 */
@Component
@Slf4j
public class DateTimeScalar {

    public static final GraphQLScalarType DATETIME = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("Local DateTime scalar")
            .coercing(new Coercing<LocalDateTime, String>() {
                @Override
                public String serialize(@NonNull Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof LocalDateTime) {
                        return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                    throw new CoercingSerializeException("Unable to serialize " + dataFetcherResult + " as a LocalDateTime");
                }

                @Override
                public LocalDateTime parseValue(@NonNull Object input) throws CoercingParseValueException {
                    try {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Unable to parse " + input + " as a LocalDateTime", e);
                    }
                    throw new CoercingParseValueException("Unable to parse " + input + " as a LocalDateTime");
                }

                @Override
                public LocalDateTime parseLiteral(@NonNull Object input) throws CoercingParseLiteralException {
                    try {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseLiteralException("Unable to parse " + input + " as a LocalDateTime", e);
                    }
                    throw new CoercingParseLiteralException("Unable to parse " + input + " as a LocalDateTime");
                }
            })
            .build();
}