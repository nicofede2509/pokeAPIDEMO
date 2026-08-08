package com.PokeApi.PokeAPIDemo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
public class PokemonController {
    private final PokemonService pokeService;
    @GetMapping("/random")
    public PokemonDTO obtenerPokemonAleatorio(){
        return pokeService.obtenerPokemonDesdeAPI();
    }
}
