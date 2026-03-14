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
        val wineUri = "$WINE_NS${wineName.value}"

        val query = QueryFactory.create("""
            PREFIX vin: <$WINE_NS>

            SELECT ?acidity ?tannin ?alcohol ?body ?sugar ?aroma
            WHERE {
                <$wineUri> vin:hasAcidity ?acidity .
                OPTIONAL { <$wineUri> vin:hasTannin  ?tannin }
                OPTIONAL { <$wineUri> vin:hasAlcohol ?alcohol }
                OPTIONAL { <$wineUri> vin:hasBody    ?body }
                OPTIONAL { <$wineUri> vin:hasSugar   ?sugar }
                OPTIONAL { <$wineUri> vin:hasAroma   ?aroma }
            }
            LIMIT 1
        """.trimIndent())

        return QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            if (!results.hasNext()) return@use null
            val s = results.next()
            WineSensoryProfile(
                acidity = s.getResource("acidity")?.localName ?: "Unknown",
                tannin  = s.getResource("tannin")?.localName  ?: "Unknown",
                alcohol = s.getResource("alcohol")?.localName ?: "Unknown",
                body    = s.getResource("body")?.localName    ?: "Unknown",
                sugar   = s.getResource("sugar")?.localName   ?: "Unknown",
                aroma   = s.getResource("aroma")?.localName   ?: "Unknown"
            )
        }
    }

    override fun getFoodSensoryProfile(foodName: Name): FoodSensoryProfile? {
        val foodUri = "$FOOD_NS${foodName.value}"

        val query = QueryFactory.create("""
            PREFIX food: <$FOOD_NS>

            SELECT ?fatness ?saltiness ?sweetness ?spiciness ?umami
            WHERE {
                <$foodUri> food:hasFatness ?fatness .
                OPTIONAL { <$foodUri> food:hasSaltiness ?saltiness }
                OPTIONAL { <$foodUri> food:hasSweetness ?sweetness }
                OPTIONAL { <$foodUri> food:hasSpiciness ?spiciness }
                OPTIONAL { <$foodUri> food:hasUmami     ?umami }
            }
            LIMIT 1
        """.trimIndent())

        return QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            if (!results.hasNext()) return@use null
            val s = results.next()
            FoodSensoryProfile(
                fatness   = s.getResource("fatness")?.localName   ?: "Unknown",
                saltiness = s.getResource("saltiness")?.localName ?: "Unknown",
                sweetness = s.getResource("sweetness")?.localName ?: "Unknown",
                spiciness = s.getResource("spiciness")?.localName ?: "Unknown",
                umami     = s.getResource("umami")?.localName     ?: "Unknown"
            )
        }
    }
}
