package com.app.vitaltrack.data.markdown

import org.junit.Assert.*
import org.junit.Test

class MarkdownTreinoParserTest {

    private val parser = MarkdownTreinoParser()

    @Test
    fun `test parse valid markdown with multiple treinos`() {
        val content = """
            # Treino A — Peito
            | Exercício | Séries | Repetições | Carga | Intervalo |
            |-----------|--------|------------|-------|-----------|
            | Supino    | 4      | 12         | 20    | 60        |
            
            # Treino B — Costas
            | Exercício | Séries | Repetições | Carga | Intervalo |
            |-----------|--------|------------|-------|-----------|
            | Puxada    | 3      | 10         | 40    | 45        |
        """.trimIndent()

        val result = parser.parse(content)

        assertTrue(result is MarkdownTreinoParseResult.Success)
        val treinos = (result as MarkdownTreinoParseResult.Success).treinos
        assertEquals(2, treinos.size)
        assertEquals("Treino A", treinos[0].dsDia)
        assertEquals("Peito", treinos[0].dsGrupoMuscular)
        assertEquals(1, treinos[0].exercicios.size)
        assertEquals("Supino", treinos[0].exercicios[0].nome)
    }

    @Test
    fun `test parse exercise with dash in carga`() {
        val content = """
            # Treino C — Pernas
            | Exercício | Séries | Repetições | Carga | Intervalo |
            |-----------|--------|------------|-------|-----------|
            | Agachamento | 4    | 8          | —     | 90        |
        """.trimIndent()

        val result = parser.parse(content)

        assertTrue(result is MarkdownTreinoParseResult.Success)
        val treinos = (result as MarkdownTreinoParseResult.Success).treinos
        assertNull(treinos[0].exercicios[0].carga)
    }

    @Test
    fun `test parse various repetition formats`() {
        val content = """
            # Treino A
            | Exercício | Séries | Repetições | Carga | Intervalo |
            |-----------|--------|------------|-------|-----------|
            | Ex 1      | 3      | 10-12      | 10    | 60        |
            | Ex 2      | 3      | 10–12      | 10    | 60        |
            | Ex 3      | 4      | 15/12/10/8 | 10    | 60        |
            | Ex 4      | 3      | ao máx     | 10    | 60        |
        """.trimIndent()

        val result = parser.parse(content)

        assertTrue(result is MarkdownTreinoParseResult.Success)
        val exercicios = (result as MarkdownTreinoParseResult.Success).treinos[0].exercicios
        assertEquals("10-12", exercicios[0].repeticoes)
        assertEquals("10–12", exercicios[1].repeticoes)
        assertEquals("15/12/10/8", exercicios[2].repeticoes)
        assertEquals("ao máx", exercicios[3].repeticoes)
    }

    @Test
    fun `test parse with bold columns`() {
        val content = """
            # Treino A
            | **Exercício** | **Séries** | **Repetições** | **Carga** | **Intervalo** |
            | ------------- | ---------- | -------------- | --------- | ------------- |
            | Supino        | 4          | 12             | 26        | 60            |
        """.trimIndent()

        val result = parser.parse(content)

        assertTrue(result is MarkdownTreinoParseResult.Success)
        val treinos = (result as MarkdownTreinoParseResult.Success).treinos
        assertEquals("Supino", treinos[0].exercicios[0].nome)
    }

    @Test
    fun `test parse error when no title found`() {
        val content = """
            | Exercício | Séries | Repetições | Carga | Intervalo |
            |-----------|--------|------------|-------|-----------|
            | Supino    | 4      | 12         | 20    | 60        |
        """.trimIndent()

        val result = parser.parse(content)

        assertTrue(result is MarkdownTreinoParseResult.Error)
    }

    @Test
    fun `test parse error when missing columns`() {
        val content = """
            # Treino A
            | Exercício | Séries |
            |-----------|--------|
            | Supino    | 4      |
        """.trimIndent()

        val result = parser.parse(content)

        assertTrue(result is MarkdownTreinoParseResult.Error)
    }
}
