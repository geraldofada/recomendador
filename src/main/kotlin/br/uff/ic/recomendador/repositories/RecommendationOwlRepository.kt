package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.FoodPairing
import br.uff.ic.recomendador.domain.models.FoodSensoryProfile
import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.models.WinePairing
import br.uff.ic.recomendador.domain.models.WineSensoryProfile
import br.uff.ic.recomendador.domain.repositories.RecommendationRepository
import br.uff.ic.recomendador.domain.utils.WineColourMapper
import br.uff.ic.recomendador.main.codegen.types.Food
import br.uff.ic.recomendador.main.codegen.types.Wine
import org.apache.jena.ontology.OntModel
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryFactory
import org.springframework.stereotype.Repository

@Repository
class RecommendationOwlRepository(
    private val recommendationOntologyModel: OntModel
) : RecommendationRepository {

    override fun recommendWinesForDish(dishName: Name): List<WinePairing> {
        val dishUri = "$FOOD_NS${dishName.value}"

        val query = QueryFactory.create("""
            PREFIX rec:  <$REC_NS>
            PREFIX vin:  <$WINE_NS>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT DISTINCT ?wine ?wineLabel ?confidence ?explanation ?color
            WHERE {
                {
                    # Explicit curated recommendations
                    ?rec a rec:Recommendation ;
                         rec:forDish <$dishUri> ;
                         rec:recommendsWine ?wine ;
                         rec:hasConfidence ?confidence .
                    OPTIONAL { ?rec rec:hasExplanation ?explanation }
                } UNION {
                    # Inferred recommendations from SWRL rules (HermiT only)
                    ?wine rec:recommendsPairing <$dishUri> .
                    BIND(0.7 AS ?confidence)
                }
                ?wine rdfs:label ?wineLabel .
                OPTIONAL { ?wine vin:hasColor ?color }
                FILTER(lang(?wineLabel) = "" || lang(?wineLabel) = "en")
            }
            ORDER BY DESC(?confidence)
        """.trimIndent())

        return QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            qexec.execSelect().asSequence().mapNotNull { solution ->
                val wineUri    = solution.getResource("wine")?.uri ?: return@mapNotNull null
                val wineLabel  = solution.getLiteral("wineLabel")?.string ?: return@mapNotNull null
                val confidence = solution.getLiteral("confidence")?.float ?: 0.5f
                val explanation = solution.getLiteral("explanation")?.string
                val colorUri   = solution.getResource("color")?.uri

                val wine = Wine(
                    id = wineUri.substringAfterLast("/"),
                    name = Name(wineLabel),
                    colour = WineColourMapper.fromColorUri(colorUri),
                    flavors = emptyList()
                )
                WinePairing(wine = wine, score = confidence, explanation = explanation)
            }.toList()
        }
    }

    override fun recommendDishesForWine(wineName: Name): List<FoodPairing> {
        val wineUri = "$WINE_NS${wineName.value}"

        val query = QueryFactory.create("""
            PREFIX rec:  <$REC_NS>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT DISTINCT ?dish ?dishLabel ?confidence ?explanation
            WHERE {
                {
                    # Explicit curated recommendations
                    ?rec a rec:Recommendation ;
                         rec:recommendsWine <$wineUri> ;
                         rec:forDish ?dish ;
                         rec:hasConfidence ?confidence .
                    OPTIONAL { ?rec rec:hasExplanation ?explanation }
                } UNION {
                    # Inferred recommendations from SWRL rules (HermiT only)
                    <$wineUri> rec:recommendsPairing ?dish .
                    BIND(0.7 AS ?confidence)
                }
                ?dish rdfs:label ?dishLabel .
                FILTER(lang(?dishLabel) = "" || lang(?dishLabel) = "en")
            }
            ORDER BY DESC(?confidence)
        """.trimIndent())

        return QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            qexec.execSelect().asSequence().mapNotNull { solution ->
                val dishUri    = solution.getResource("dish")?.uri ?: return@mapNotNull null
                val dishLabel  = solution.getLiteral("dishLabel")?.string ?: return@mapNotNull null
                val confidence = solution.getLiteral("confidence")?.float ?: 0.5f
                val explanation = solution.getLiteral("explanation")?.string

                val food = Food(
                    id = dishUri.substringAfterLast("/"),
                    name = Name(dishLabel),
                    flavors = emptyList()
                )
                FoodPairing(food = food, score = confidence, explanation = explanation)
            }.toList()
        }
    }

    override fun getWineSensoryProfile(wineName: Name): WineSensoryProfile? {
        return WineSensoryProfile(
            acidity = "Medium",
            tannin = "Medium",
            alcohol = "Medium",
            body = "Medium",
            sugar = "Dry",
            aroma = "Neutral"
        )
    }

    override fun getFoodSensoryProfile(foodName: Name): FoodSensoryProfile? {
        return FoodSensoryProfile(
            fatness = "Medium",
            saltiness = "Medium",
            sweetness = "Low",
            spiciness = "Low",
            umami = "Medium"
        )
    }
}
