package com.PokeApi.PokeAPIDemo;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class PokemonService {
    private final ConcurrentHashMap<String, MovimientosDTO> cacheMovimientos = new ConcurrentHashMap<>();

    private static final java.util.Map<String, String> tiposEsp = java.util.Map.ofEntries(
            java.util.Map.entry("normal", "Normal"), java.util.Map.entry("fighting", "Lucha"),
            java.util.Map.entry("flying", "Volador"), java.util.Map.entry("poison", "Veneno"),
            java.util.Map.entry("ground", "Tierra"), java.util.Map.entry("rock", "Roca"),
            java.util.Map.entry("bug", "Bicho"), java.util.Map.entry("ghost", "Fantasma"),
            java.util.Map.entry("steel", "Acero"), java.util.Map.entry("fire", "Fuego"),
            java.util.Map.entry("water", "Agua"), java.util.Map.entry("grass", "Planta"),
            java.util.Map.entry("electric", "Eléctrico"), java.util.Map.entry("psychic", "Psíquico"),
            java.util.Map.entry("ice", "Hielo"), java.util.Map.entry("dragon", "Dragón"),
            java.util.Map.entry("dark", "Siniestro"), java.util.Map.entry("fairy", "Hada")
    );

    private static final java.util.Map<String, String> clasesEsp = java.util.Map.of(
            "physical", "Físico",
            "special", "Especial",
            "status", "Estado"
    );
    public PokemonDTO obtenerPokemonDesdeAPI(){
        try{
            Random random = new Random();
            int nroPokedex = random.nextInt(1025) + 1;
            String url = "https://pokeapi.co/api/v2/pokemon/" + nroPokedex;
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode nodoRaiz = mapper.readTree(respuesta.body());

            String nombre= nodoRaiz.get("name").asString();
            String imageURL = nodoRaiz.get("sprites").get("front_default").asString();

            JsonNode arrayStats = nodoRaiz.get("stats");
            int hp = arrayStats.get(0).get("base_stat").asInt();
            int ataque = arrayStats.get(1).get("base_stat").asInt();
            int defensa = arrayStats.get(2).get("base_stat").asInt();
            int ataqueEspecial = arrayStats.get(3).get("base_stat").asInt();
            int defensaEspecial = arrayStats.get(4).get("base_stat").asInt();
            int velocidad = arrayStats.get(5).get("base_stat").asInt();

            int statsTotales = 0;
            for(JsonNode statNode : arrayStats){
                statsTotales += statNode.get("base_stat").asInt();
            }

            List<MovimientosDTO> movimientosDTO = new ArrayList<>();
            List<String> nombresElegidos = new ArrayList<>();

            JsonNode arrayMovimientos = nodoRaiz.get("moves");
            int movimientosDisponibles = arrayMovimientos.size();
            int cantidadMovimientos = Math.min(4, movimientosDisponibles);

            while(movimientosDTO.size() < cantidadMovimientos){
                int j = random.nextInt(movimientosDisponibles);
                JsonNode nodoMovimiento = arrayMovimientos.get(j).get("move");

                String nombreAtaque = nodoMovimiento.get("name").asString();
                String urlDelAtaque = nodoMovimiento.get("url").asString();

                if(!nombresElegidos.contains(nombreAtaque)){
                    nombresElegidos.add(nombreAtaque);
                    if(!cacheMovimientos.containsKey(nombreAtaque)){

                        MovimientosDTO nuevoMovimiento = new MovimientosDTO();
                        nuevoMovimiento.setNombre(nombreAtaque);

                        try{
                            HttpRequest peticionMov = HttpRequest.newBuilder()
                                    .uri(URI.create(urlDelAtaque))
                                    .GET()
                                    .build();

                            HttpResponse<String> respuestaMov = cliente.send(peticionMov, HttpResponse.BodyHandlers.ofString());
                            JsonNode nodoMovRaiz = mapper.readTree(respuestaMov.body());

                            String nombreTraducido = nombreAtaque.replace("-", " ");
                            JsonNode nombresArray = nodoMovRaiz.get("names");
                            if(nombresArray != null){
                                for(JsonNode nameNode : nombresArray){
                                    if ("es".equals(nameNode.get("language").get("name").asString())) {
                                        nombreTraducido = nameNode.get("name").asString();
                                        break; // Encontramos el español, cortamos la búsqueda
                                    }
                                }
                            }
                            nuevoMovimiento.setNombre(nombreTraducido);

                            String tipoIngles = nodoMovRaiz.get("type").get("name").asString();
                            String claseIngles = nodoMovRaiz.get("damage_class").get("name").asString();



                            nuevoMovimiento.setTipo(tiposEsp.getOrDefault(tipoIngles, tipoIngles));
                            nuevoMovimiento.setClaseDeDano(clasesEsp.getOrDefault(claseIngles, claseIngles));
                            nuevoMovimiento.setPotencia(nodoMovRaiz.get("power").isNull() ? 0 : nodoMovRaiz.get("power").asInt());
                            nuevoMovimiento.setPrecision(nodoMovRaiz.get("accuracy").isNull() ? 0 : nodoMovRaiz.get("accuracy").asInt());
                        }catch(Exception e){
                            nuevoMovimiento.setTipo("unknown");
                            nuevoMovimiento.setClaseDeDano("unknown");
                            nuevoMovimiento.setPotencia(0);
                            nuevoMovimiento.setPrecision(0);
                        }
                        cacheMovimientos.put(nombreAtaque, nuevoMovimiento);
                    }
                    movimientosDTO.add(cacheMovimientos.get(nombreAtaque));
                }
            }

            PokemonDTO pokemon = new PokemonDTO();
            pokemon.setNombre(nombre);
            pokemon.setImagen(imageURL);
            pokemon.setHp(hp);
            pokemon.setAtaque(ataque);
            pokemon.setDefensa(defensa);
            pokemon.setAtaqueEspecial(ataqueEspecial);
            pokemon.setDefensaEspecial(defensaEspecial);
            pokemon.setVelocidad(velocidad);

            pokemon.setTotalStats(statsTotales);

            pokemon.setMovimientosUsables(movimientosDTO);
            return pokemon;
        } catch (Exception e) {
            System.out.println("El pokémon se escapó de la pokébola" + e.getMessage());
            return null;
        }
    }
}

