package com.library.library_management_system.graphql;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
public class DateScalar {

    public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
            .name("Date")
            .description("Local Date scalar")
            .coercing(new Coercing<LocalDate, String>() {
                @Override
                public String serialize(@NonNull Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof LocalDate) {
                        return ((LocalDate) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                    throw new CoercingSerializeException("Unable to serialize " + dataFetcherResult + " as a LocalDate");
                }

                @Override
                public @NonNull LocalDate parseValue(@NonNull Object input) throws CoercingParseValueException {
                    try {
                        if (input instanceof String) {
                            return LocalDate.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException("Unable to parse " + input + " as a LocalDate", e);
                    }
                    throw new CoercingParseValueException("Unable to parse " + input + " as a LocalDate");
                }

                @Override
                public @NonNull LocalDate parseLiteral(@NonNull Object input) throws CoercingParseLiteralException {
                    try {
                        if (input instanceof String) {
                            return LocalDate.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseLiteralException("Unable to parse " + input + " as a LocalDate", e);
                    }
                    throw new CoercingParseLiteralException("Unable to parse " + input + " as a LocalDate");
                }
            })
            .build();
}
