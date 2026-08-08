package com.PokeApi.PokeAPIDemo;

import lombok.Data;

import java.util.List;

@Data
public class PokemonDTO {
    private String nombre;
    private String imagen;

    private int hp;
    private int ataque;
    private int defensa;
    private int ataqueEspecial;
    private int defensaEspecial;
    private int velocidad;
    private List<MovimientosDTO> movimientosUsables;


    private int totalStats;
}
