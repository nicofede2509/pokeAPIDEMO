async function buscarPokemon(numeroDeTarjeta) {
    const txtNombre = document.getElementById(`nombre-pokemon-${numeroDeTarjeta}`);
    const imgPokemon = document.getElementById(`imagen-pokemon-${numeroDeTarjeta}`);
    const txtTotal = document.getElementById(`total-pokemon-${numeroDeTarjeta}`);
    const ulMovimientos = document.getElementById(`movimientos-pokemon-${numeroDeTarjeta}`);

    try {
        txtNombre.textContent = "Buscando...";
        txtNombre.classList.add('cargando');
        imgPokemon.style.opacity = '0.5';
        txtTotal.textContent = "...";

        ulMovimientos.innerHTML = '<li>Buscando...</li>';

        const respuesta = await fetch('/random');

        if (!respuesta.ok) {
            throw new Error('Error en el servidor');
        }

        const pokemon = await respuesta.json();

        txtNombre.textContent = pokemon.nombre;
        imgPokemon.src = pokemon.imagen;
        txtTotal.textContent = pokemon.totalStats;

        ulMovimientos.innerHTML = '';

        if (pokemon.movimientosUsables && pokemon.movimientosUsables.length > 0) {
            pokemon.movimientosUsables.forEach(movimiento => {
                const li = document.createElement('li');

                // Lógica para detectar el 0 o null y transformarlo en un guion
                let textoPotencia = (movimiento.potencia && movimiento.potencia > 0) ? movimiento.potencia : "-";
                let textoPrecision = (movimiento.precision && movimiento.precision > 0) ? movimiento.precision : "-";

                // Inyectamos el HTML con las variables del DTO y el nuevo contenedor
                li.innerHTML = `
                    <span class="mov-titulo">${movimiento.nombre}</span>
                    <span class="mov-detalles">${movimiento.tipo} | ${movimiento.claseDeDano}</span>
                    <div class="stats-ataque-contenedor">
                        <span class="mov-stat">Pot: ${textoPotencia}</span>
                        <span class="mov-stat">Prec: ${textoPrecision}</span>
                    </div>
                `;

                ulMovimientos.appendChild(li);
            });
        } else {
            const li = document.createElement('li');
            li.textContent = "Sin movimientos";
            ulMovimientos.appendChild(li);
        }

        txtNombre.classList.remove('cargando');
        imgPokemon.style.opacity = '1';

    } catch (error) {
        txtNombre.textContent = "Error de conexión";
        txtTotal.textContent = "-";
        ulMovimientos.innerHTML = '<li>Error</li>';
        console.error("Error:", error);
    }
}

document.getElementById('btn-generar-1').addEventListener('click', () => buscarPokemon(1));
document.getElementById('btn-generar-2').addEventListener('click', () => buscarPokemon(2));