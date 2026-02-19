package br.uff.ic.recomendador.domain.utils

import br.uff.ic.recomendador.main.codegen.types.Colour

object WineColourMapper {

    private const val WINE_NS = "http://uff.ic.br/ontologias/recomendador/wine/"

    fun fromColorUri(colorUri: String?): Colour {
        if (colorUri == null) return Colour.WHITE
        
        return when {
            colorUri.endsWith("/White") || colorUri.contains("#White") -> Colour.WHITE
            colorUri.endsWith("/Red") || colorUri.contains("#Red") -> Colour.RED
            colorUri.endsWith("/Rose") || colorUri.contains("#Rose") -> Colour.RUBY
            else -> Colour.WHITE
        }
    }

    fun getLocalName(uri: String): String {
        val lastSlash = uri.lastIndexOf('/')
        val lastHash = uri.lastIndexOf('#')
        val lastIndex = maxOf(lastSlash, lastHash)
        return if (lastIndex >= 0) uri.substring(lastIndex + 1) else uri
    }
}