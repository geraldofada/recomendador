package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.FoodPairing
import br.uff.ic.recomendador.domain.models.FoodSensoryProfile
import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.models.WinePairing
import br.uff.ic.recomendador.domain.models.WineSensoryProfile
import br.uff.ic.recomendador.domain.repositories.RecommendationRepository
import br.uff.ic.recomendador.main.codegen.types.Colour
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

    init {
        println("=== RecommendationOwlRepository initialized ===")
        println("Model size: ${recommendationOntologyModel.size()}")
        
        // Debug: List some statements
        val stmtIter = recommendationOntologyModel.listStatements()
        println("Sample statements:")
        var count = 0
        while (stmtIter.hasNext() && count < 5) {
            val stmt = stmtIter.next()
            println("  ${stmt.subject} ${stmt.predicate} ${stmt.`object`}")
            count++
        }
    }

    override fun recommendWinesForDish(dishName: Name): List<WinePairing> {
        val pairings = mutableListOf<WinePairing>()

        println("Recommending wines for dish: ${dishName.value}")
        println("Model size in recommendWinesForDish: ${recommendationOntologyModel.size()}")

        val query = QueryFactory.create(
            """
            PREFIX vin: <http://uff.ic.br/ontologias/recomendador/wine/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            
            SELECT ?wine ?label
            WHERE {
                ?wine rdf:type ?type .
                ?wine rdfs:label ?label .
            }
            LIMIT 10
            """.trimIndent()
        )

        QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            var rank = 1
            println("Executing query...")
            while (results.hasNext()) {
                val solution = results.next()
                println("Found result: ${solution.getResource("wine")}")
                val wineUri = solution.getResource("wine")?.uri ?: continue
                val wineLabel = solution.getLiteral("label")?.string ?: continue

                val wine = Wine(
                    id = wineUri.substringAfterLast("/"),
                    name = Name(wineLabel),
                    colour = Colour.WHITE,
                    flavors = emptyList()
                )
                pairings.add(
                    WinePairing(
                        wine = wine,
                        score = (30 - rank + 1).toFloat() / 30f,
                        explanation = "Wine recommendation"
                    )
                )
                rank++
            }
            println("Found ${pairings.size} wines")
        }

        return pairings
    }

    override fun recommendDishesForWine(wineName: Name): List<FoodPairing> {
        val pairings = mutableListOf<FoodPairing>()

        val query = QueryFactory.create(
            """
            PREFIX food: <http://uff.ic.br/ontologias/recomendador/food/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            
            SELECT ?dish ?label
            WHERE {
                ?dish rdf:type food:Dish .
                ?dish rdfs:label ?label .
            }
            LIMIT 20
            """.trimIndent()
        )

        QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            var rank = 1
            while (results.hasNext()) {
                val solution = results.next()
                val dishUri = solution.getResource("dish")?.uri ?: continue
                val dishLabel = solution.getLiteral("label")?.string ?: continue

                val food = Food(
                    id = dishUri.substringAfterLast("/"),
                    name = Name(dishLabel),
                    flavors = emptyList()
                )
                pairings.add(
                    FoodPairing(
                        food = food,
                        score = (20 - rank + 1).toFloat() / 20f,
                        explanation = "Food recommendation"
                    )
                )
                rank++
            }
        }

        return pairings
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
