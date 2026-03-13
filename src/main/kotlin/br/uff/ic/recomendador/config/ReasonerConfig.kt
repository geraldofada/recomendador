package br.uff.ic.recomendador.config

import org.apache.jena.ontology.OntDocumentManager
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.ReasonerRegistry
import org.semanticweb.HermiT.ReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.formats.TurtleDocumentFormat
import org.semanticweb.owlapi.io.StreamDocumentSource
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator
import org.semanticweb.owlapi.util.InferredOntologyGenerator
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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

    @Value("\${reasoner.type:JENA_OWL}")
    private lateinit var reasonerTypeName: String

    @Bean
    fun recommendationOntologyModel(): OntModel {
        val type = ReasonerType.valueOf(reasonerTypeName.uppercase())
        println("=== Creating OntModel with reasoner: $type ===")
        val start = System.currentTimeMillis()

        val model = when (type) {
            ReasonerType.JENA_OWL -> buildJenaOwlModel()
            ReasonerType.HERMIT -> buildHermiTModel()
            ReasonerType.PELLET -> buildPelletModel()
        }

        println("=== OntModel ready (${System.currentTimeMillis() - start}ms), size: ${model.size()} triples ===")
        return model
    }

    private fun buildJenaOwlModel(): OntModel {
        val docManager = OntDocumentManager()
        docManager.setCacheModels(false)
        docManager.reset()

        val spec = OntModelSpec(OntModelSpec.OWL_MEM)
        spec.documentManager = docManager
        spec.reasoner = ReasonerRegistry.getOWLReasoner()

        return ModelFactory.createOntologyModel(spec).also { loadOntologiesIntoJena(it) }
    }

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

    private fun loadOntologiesIntoJena(model: OntModel) {
        model.read(wineSchemaResource.inputStream, "http://uff.ic.br/ontologias/recomendador/wine/", "RDF/XML")
        model.read(wineInstancesResource.inputStream, "http://uff.ic.br/ontologias/recomendador/wine/", "TURTLE")
        model.read(foodSchemaResource.inputStream, "http://uff.ic.br/ontologias/recomendador/food/", "RDF/XML")
        model.read(foodInstancesResource.inputStream, "http://uff.ic.br/ontologias/recomendador/food/", "TURTLE")
        model.read(recommendationSchemaResource.inputStream, "http://uff.ic.br/ontologias/recomendador/recommendation/", "RDF/XML")
        model.read(recommendationInstancesResource.inputStream, "http://uff.ic.br/ontologias/recomendador/recommendation/", "TURTLE")
    }
}
