package br.uff.ic.recomendador.config

import org.apache.jena.ontology.OntDocumentManager
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.Reasoner
import org.apache.jena.reasoner.ReasonerRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class ReasonerConfig {

    @Value("classpath:ontologies/wine/wine-schema.owl")
    private lateinit var wineSchemaResource: Resource

    @Value("classpath:ontologies/wine/wine-instances.ttl")
    private lateinit var wineInstancesResource: Resource

    @Value("classpath:ontologies/food/food-schema.owl")
    private lateinit var foodSchemaResource: Resource

    @Value("classpath:ontologies/food/food-instances.ttl")
    private lateinit var foodInstancesResource: Resource

    @Value("classpath:ontologies/recommendation/recommendation-schema.owl")
    private lateinit var recommendationSchemaResource: Resource

    @Value("classpath:ontologies/recommendation/recommendation-instances.ttl")
    private lateinit var recommendationInstancesResource: Resource

    @Bean
    fun owlReasoner(): Reasoner {
        return ReasonerRegistry.getOWLReasoner()
    }

    @Bean
    fun recommendationOntologyModel(
        @Autowired owlReasoner: Reasoner
    ): OntModel {
        val docManager = OntDocumentManager()
        docManager.setCacheModels(false)
        docManager.reset()

        val spec = OntModelSpec(OntModelSpec.OWL_MEM)
        spec.documentManager = docManager
        spec.reasoner = owlReasoner

        val model = ModelFactory.createOntologyModel(spec)

        wineSchemaResource.inputStream.use { inputStream ->
            model.read(inputStream, "http://uff.ic.br/ontologias/recomendador/wine/", "RDF/XML")
        }
        wineInstancesResource.inputStream.use { inputStream ->
            model.read(inputStream, "http://uff.ic.br/ontologias/recomendador/wine/", "TURTLE")
        }
        foodSchemaResource.inputStream.use { inputStream ->
            model.read(inputStream, "http://uff.ic.br/ontologias/recomendador/food/", "RDF/XML")
        }
        foodInstancesResource.inputStream.use { inputStream ->
            model.read(inputStream, "http://uff.ic.br/ontologias/recomendador/food/", "TURTLE")
        }
        recommendationSchemaResource.inputStream.use { inputStream ->
            model.read(inputStream, "http://uff.ic.br/ontologias/recomendador/recommendation/", "RDF/XML")
        }
        recommendationInstancesResource.inputStream.use { inputStream ->
            model.read(inputStream, "http://uff.ic.br/ontologias/recomendador/recommendation/", "TURTLE")
        }

        println("=== Ontology Model Loaded ===")
        println("Ready for SPARQL queries")

        return model
    }
}
