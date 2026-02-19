package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.repositories.RecommendationRepository
import br.uff.ic.recomendador.domain.utils.WineColourMapper
import br.uff.ic.recomendador.main.codegen.types.Wine
import org.apache.jena.ontology.OntModel
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryFactory
import org.springframework.stereotype.Repository

@Repository
class RecommendationOwlRepository(
    private val recommendationOntologyModel: OntModel
) : RecommendationRepository {

    override fun recommendWinesForCategory(categoryName: Name): List<Wine> {
        val query = QueryFactory.create(
            """
            PREFIX vin: <http://uff.ic.br/ontologias/recomendador/wine/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            
            SELECT ?wine ?wineId ?color
            WHERE {
                ?wine vin:hasFoodCategory ?category .
                FILTER(CONTAINS(str(?category), "${categoryName.value}"))
                ?wine rdf:ID ?wineId .
                OPTIONAL { ?wine vin:hasColor ?color }
            }
            """.trimIndent()
        )

        return executeWineQuery(query)
    }

    override fun recommendWinesForDish(dishName: Name): List<Wine> {
        val query = QueryFactory.create(
            """
            PREFIX food: <http://uff.ic.br/ontologias/recomendador/food/>
            PREFIX vin: <http://uff.ic.br/ontologias/recomendador/wine/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            
            SELECT ?wine ?wineId ?color
            WHERE {
                ?dish food:hasRecommendedWine ?wine .
                FILTER(CONTAINS(str(?dish), "${dishName.value}"))
                ?wine rdf:ID ?wineId .
                OPTIONAL { ?wine vin:hasColor ?color }
            }
            """.trimIndent()
        )

        val directWines = executeWineQuery(query).toMutableList()

        // Also get wines based on the dish's food categories
        val categoryQuery = QueryFactory.create(
            """
            PREFIX food: <http://uff.ic.br/ontologias/recomendador/food/>
            PREFIX vin: <http://uff.ic.br/ontologias/recomendador/wine/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            
            SELECT ?category
            WHERE {
                ?dish food:hasFoodCategory ?category .
                FILTER(CONTAINS(str(?dish), "${dishName.value}"))
            }
            """.trimIndent()
        )

        QueryExecution.create(categoryQuery, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            while (results.hasNext()) {
                val solution = results.next()
                val categoryResource = solution.getResource("category")
                val categoryId = categoryResource?.localName ?: continue
                val categoryWines = recommendWinesForCategory(Name(categoryId))
                categoryWines.forEach { wine ->
                    if (directWines.none { it.id == wine.id }) {
                        directWines.add(wine)
                    }
                }
            }
        }

        return directWines
    }

    private fun executeWineQuery(query: org.apache.jena.query.Query): List<Wine> {
        val wines = mutableListOf<Wine>()

        QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            while (results.hasNext()) {
                val solution = results.next()
                val wineId = solution.getLiteral("wineId")?.string ?: continue
                val colorUri = solution.getResource("color")?.uri

                wines.add(
                    Wine(
                        id = wineId,
                        name = Name(wineId),
                        colour = WineColourMapper.fromColorUri(colorUri),
                        flavors = emptyList()
                    )
                )
            }
        }

        return wines
    }
}