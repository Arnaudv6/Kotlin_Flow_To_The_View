package fr.delcey.pokedexfullflow.ui.list

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import dagger.hilt.android.AndroidEntryPoint
import fr.delcey.pokedexfullflow.databinding.PokemonActivityBinding
import fr.delcey.pokedexfullflow.ui.PokemonAdapter
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PokemonActivity : AppCompatActivity() {

    private lateinit var binding: PokemonActivityBinding

    private val pokemonViewModel : PokemonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PokemonActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val adapter = PokemonAdapter { Toast.makeText(this, "Pokemon clicked : ${it.name}", Toast.LENGTH_LONG).show() }
        binding.pokemonsRv.adapter = adapter

        addRepeatingJob(Lifecycle.State.STARTED) {
            pokemonViewModel.uiStateFlow.collect {
                adapter.submitList(it)
            }
        }
    }
}