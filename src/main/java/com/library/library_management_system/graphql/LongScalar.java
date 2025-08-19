package com.library.library_management_system.graphql;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Custom GraphQL scalar for Long values
 */
@Component
@Slf4j
public class LongScalar {

    public static final GraphQLScalarType LONG = GraphQLScalarType.newScalar()
            .name("Long")
            .description("Long scalar for large integer values")
            .coercing(new Coercing<Long, Long>() {
                @Override
                public Long serialize(@NonNull Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof Long) {
                        return (Long) dataFetcherResult;
                    }
                    if (dataFetcherResult instanceof Integer) {
                        return ((Integer) dataFetcherResult).longValue();
                    }
                    if (dataFetcherResult instanceof Number) {
                        return ((Number) dataFetcherResult).longValue();
                    }
                    throw new CoercingSerializeException("Unable to serialize " + dataFetcherResult + " as a Long");
                }

                @Override
                public @NonNull Long parseValue(@NonNull Object input) throws CoercingParseValueException {
                    if (input instanceof Long) {
                        return (Long) input;
                    }
                    if (input instanceof Integer) {
                        return ((Integer) input).longValue();
                    }
                    if (input instanceof Number) {
                        return ((Number) input).longValue();
                    }
                    if (input instanceof String) {
                        try {
                            return Long.parseLong((String) input);
                        } catch (NumberFormatException e) {
                            throw new CoercingParseValueException("Unable to parse " + input + " as a Long", e);
                        }
                    }
                    throw new CoercingParseValueException("Unable to parse " + input + " as a Long");
                }

                @Override
                public @NonNull Long parseLiteral(@NonNull Object input) throws CoercingParseLiteralException {
                    if (input instanceof Long) {
                        return (Long) input;
                    }
                    if (input instanceof Integer) {
                        return ((Integer) input).longValue();
                    }
                    if (input instanceof Number) {
                        return ((Number) input).longValue();
                    }
                    throw new CoercingParseLiteralException("Unable to parse " + input + " as a Long");
                }
            })
            .build();
}