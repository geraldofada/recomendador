package br.uff.ic.recomendador.domain.models

import com.netflix.graphql.dgs.DgsScalar
import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import java.util.Locale


@DgsScalar(name = "Name")
class Name : Coercing<String, String> {
    override fun serialize(dataFetcherResult: Any, graphQLContext: GraphQLContext, locale: Locale): String? {
        if (dataFetcherResult is String) {
            return dataFetcherResult
        }

        throw CoercingSerializeException("Expected a String")
    }

    override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): String? {
        if (input is String) {
            return input
        }

        throw CoercingParseValueException("Expected a String")
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): String? {
        if (input is StringValue) {
            return input.value
        }

        throw CoercingParseLiteralException("Expected a String")
    }

    override fun valueToLiteral(input: Any, graphQLContext: GraphQLContext, locale: Locale): Value<*> {
        return StringValue(input.toString())
    }
}
