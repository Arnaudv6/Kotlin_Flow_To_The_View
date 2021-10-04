package fr.delcey.pokedexfullflow.ui.flow

import fr.delcey.pokedexfullflow.CoroutineToolsProvider
import fr.delcey.pokedexfullflow.data.PokemonRepository
import fr.delcey.pokedexfullflow.data.pokemon.PokemonResponse
import fr.delcey.pokedexfullflow.data.pokemon.PokemonSprites
import fr.delcey.pokedexfullflow.ui.PokemonUiState
import fr.delcey.pokedexfullflow.utils.TestCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FlowPokemonViewModelTest {

    companion object {
        private const val DEFAULT_LIST_SIZE = 3
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val pokemonRepository = mockk<PokemonRepository>()

    private val coroutineToolsProvider = mockk<CoroutineToolsProvider>()

    @Before
    fun setUp() {
        every { pokemonRepository.pokemonsFlow } returns flowOf(getDefaultPokemonList())

        every { coroutineToolsProvider.ioCoroutineDispatcher } returns testCoroutineRule.testCoroutineDispatcher
        every { coroutineToolsProvider.sharingStartedStrategy } returns SharingStarted.Eagerly
    }

    @Test
    fun `nominal case`() = testCoroutineRule.runBlockingTest {
        // Given
        val viewModel = FlowPokemonViewModel(pokemonRepository, coroutineToolsProvider)

        // When
        val result = viewModel.uiStateFlow.first()

        // Then
        assertEquals(getExpectedPokemonUiStates(), result)
    }

    @Test
    fun `not so nominal case - a delay between 2 values !`() = testCoroutineRule.runBlockingTest {
        // Given
        every { pokemonRepository.pokemonsFlow } returns flow {
            emit(getDefaultPokemonList(1))
            delay(3_000)
            emit(getDefaultPokemonList())
        }
        val viewModel = FlowPokemonViewModel(pokemonRepository, coroutineToolsProvider)

        // When
        val result1 = viewModel.uiStateFlow.first()
        advanceTimeBy(3_000)
        val result2 = viewModel.uiStateFlow.first()

        // Then
        assertEquals(getExpectedPokemonUiStates(1), result1)
        assertEquals(getExpectedPokemonUiStates(), result2)
    }

    @Test
    fun `another not so nominal case - an initial delay !`() = testCoroutineRule.runBlockingTest {
        // Given
        every { pokemonRepository.pokemonsFlow } returns flow {
            delay(3_000)
            emit(getDefaultPokemonList())
        }
        val viewModel = FlowPokemonViewModel(pokemonRepository, coroutineToolsProvider)

        // When
        testCoroutineRule.testCoroutineDispatcher.advanceTimeBy(3_000)
        val result = viewModel.uiStateFlow.first()

        // Then
        assertEquals(getExpectedPokemonUiStates(), result)
    }

    // region IN
    private fun getDefaultPokemonList(size: Int = DEFAULT_LIST_SIZE): List<PokemonResponse> = List(size) { index ->
        PokemonResponse(
            name = "name$index",
            id = index,
            sprites = getDefaultPokemonSprites(index),
        )
    }

    private fun getDefaultPokemonSprites(index: Int) = PokemonSprites(
        frontDefault = "frontDefault$index"
    )
    // endregion

    // region OUT
    private fun getExpectedPokemonUiStates(size: Int = DEFAULT_LIST_SIZE): List<PokemonUiState> = List(size) { index: Int ->
        PokemonUiState(
            id = index,
            name = "name$index",
            imageUrl = "frontDefault$index",
            number = index.toString()
        )
    }
    // endregion
}