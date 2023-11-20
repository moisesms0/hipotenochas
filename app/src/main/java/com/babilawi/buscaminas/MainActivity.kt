package com.babilawi.buscaminas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.gridlayout.widget.GridLayout
import java.util.Random

class MainActivity : AppCompatActivity() {

    private var rows = 8
    private var cols = 8
    private var totalMinas = 10
    private var minasEncontradas = 0
    private var juegoEnCurso = true
    private lateinit var botones: Array<Array<Button>>
    private lateinit var tableroMinas: Array<BooleanArray>
    private var iconoHipotenocha: Int = R.drawable.manzana


    override fun onCreate(savedInstanceState: Bundle?) {
        // Desactivar modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inflar la toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.nav_menu)
        setSupportActionBar(toolbar)

        crearTablero()
    }

    /**
     * Se ejecuta cuando se crea el menú.
     * @param menu Menú
     * @return true si se ha creado el menú, false si no
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        // Obtener el ítem de menú por su ID
        val hipotenochaMenuItem = menu?.findItem(R.id.hipotenocha)

        // Cambiar el ícono del ítem de menú usando la variable miembro
        hipotenochaMenuItem?.setIcon(iconoHipotenocha)

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Se ejecuta cuando se pulsa un ítem del menú.
     * @param item Ítem del menú pulsado
     * @return true si se ha pulsado un ítem del menú, false si no
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Instrucciones del juego
            R.id.instrucciones -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setTitle("Instrucciones")
                    setMessage(R.string.instrucciones)
                    setPositiveButton("Aceptar") { _, _ ->

                    }
                }
                val instrucciones = builder.create()
                instrucciones.show()}

            // Configurar la dificultad del juego
            R.id.configuraJuego -> {
                mostrarAlertaDificultad()
            }
            // Crear nueva partida
            R.id.nuevoJuego -> {
                crearTablero()
            }
            // Mostrar personajes
            R.id.hipotenocha -> {
                mostrarAlertaPersonaje()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Crea un nuevo tablero de juego con las configuraciones actuales de filas y columnas.
     * Inicializa la interfaz de usuario con botones dispuestos en un GridLayout y establece
     * las propiedades necesarias para el juego de buscaminas.
     * Se encarga de la disposición de las minas, el tamaño de los botones y la asignación
     * de listeners para cada botón.
     */
    private fun crearTablero() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        minasEncontradas = 0
        juegoEnCurso = true

        // borrar todos los botones cada vez que se cree la partida
        gridLayout.removeAllViews()

        // Definir las medidas del tablero
        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        // Array donde se guarda la posicion de cada mina
        tableroMinas = Array(rows) { BooleanArray(cols) }

        // Obtiene el ancho de la pantalla para calcular el ancho de los botones
        val anchoPantalla = resources.displayMetrics.widthPixels
        val anchoBoton = (anchoPantalla - (cols - 1) * 22) / cols

        // Se define el array de botones iterando sobre las filas y columnas para crear cada botón
        botones = Array(rows) { row ->
            Array(cols) { col ->
                val boton = Button(this)
                // Se define el tamaño de cada botón
                boton.layoutParams = GridLayout.LayoutParams().apply {
                    width = anchoBoton
                    height = if (rows == 8) {
                        anchoBoton
                    } else {
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    setMargins(10, 10, 10, 10)
                }

                // Se elimina el padding para que se visualice correctamente en maxima dificultad
                boton.setPadding(0,0,0,0)

                // Se añade fondo del boton
                boton.background = getDrawable(R.drawable.btn_background_primary)

                // Se añaden los listeners a cada botón
                buttonListeners(boton, row, col)

                // Se añade el botón al gridLayout
                gridLayout.addView(boton)

                // Devuelve el valor de esa posicion del array
                boton
            }
        }
        colocarMinasAleatoriamente()
    }

    /**
     * Añade los listeners a cada botón del tablero.
     * @param boton Botón al que se le añaden los listeners
     * @param row Fila del botón
     * @param col Columna del botón
     */
    private fun buttonListeners(boton: Button, row: Int, col: Int){
        // Listener para cuando se hace click en un botón que comprueba si el boton es una mina
        // o si la casilla esta marcada
        boton.setOnClickListener {
            if (boton.text == "F" || !juegoEnCurso) return@setOnClickListener

            // Si es mina se pierde la partida, si no lo es se revela esa casilla
            // y las adyacentes que no tengan minas cerca
            if (esMina(row, col)) {
                mostrarAlertaReinicio(2)
                if (rows == 8) {
                    boton.background = AppCompatResources.getDrawable(this, R.drawable.bomba)
                } else {
                    boton.text = "X"
                }

            } else {
                val minasAdyacentes = contarMinasAdyacentes(row, col)
                if (minasAdyacentes == 0) {
                    revelarCeldasAdyacentes(row, col)
                }else{
                    boton.background = AppCompatResources.getDrawable(this, R.drawable.btn_background_secondary)
                    boton.text = minasAdyacentes.toString()
                }
            }
        }

        // Listener para cuando se hace click largo en un botón, para marcar la casilla
        boton.setOnLongClickListener {
            // Si el juego a finalizado o si la casilla ya esta marcada no hará nada
            if (!juegoEnCurso || boton.background.constantState == getDrawable(iconoHipotenocha)?.constantState) {
                // No hacer nada si el juego no está en curso
                return@setOnLongClickListener true
            }

            if (esMina(row, col)) {
                // Crear toast en caso de se encuentre una hipotenocha
                val toast = Toast.makeText(this, "¡Hipotenocha encontrada!", Toast.LENGTH_SHORT)
                toast.show()
                minasEncontradas++
                // Si se encuentran todas las minas se gana la partida
                if (minasEncontradas == totalMinas){
                    mostrarAlertaReinicio(1)
                }
            }else{
                mostrarAlertaReinicio(2)
            }
            if (rows == 8) {
                boton.background = AppCompatResources.getDrawable(this, iconoHipotenocha)
            } else {
                boton.text = "F"
            }


            true
        }
    }

    /**
     * Comprueba si la casilla es una mina.
     * @param row Fila de la casilla actual
     * @param col Columna de la casilla actual
     * @return true si la casilla es una mina, false si no lo es
     */
    private fun esMina(row: Int, col: Int): Boolean {
        return tableroMinas[row][col]
    }

    /**
     * Cuenta el número de minas adyacentes a la casilla actual.
     * @param row Fila de la casilla actual
     * @param col Columna de la casilla actual
     */
    private fun contarMinasAdyacentes(row: Int, col: Int): Int {
        var count = 0

        // Se comprueban las casillas que rodean a la casilla actual
        for (i in -1..1) {
            for (j in -1..1) {
                val r = row + i
                val c = col + j

                // Se comprueba que este dentro de los limites y que no sea la propia casilla
                if (r in 0 until rows && c in 0 until cols && !(i == 0 && j == 0)) {
                    // Si es una mina aumenta el contador
                    if (esMina(r, c)) {
                        count++
                    }
                }
            }
        }
        return count
    }

    /**
     * Coloca las minas aleatoriamente en el tablero.
     */
    private fun colocarMinasAleatoriamente() {
        val random = Random()
        var minasColocadas = 0

        // Genera minas aleatorias hasta que se llegue al total de minas
        while (minasColocadas < totalMinas) {
            val x = random.nextInt(rows)
            val y = random.nextInt(cols)

            // Si la casilla no es una mina se coloca una
            if (!tableroMinas[x][y]) {
                tableroMinas[x][y] = true
                minasColocadas++
            }
        }
    }

    /**
     * Revela las casillas adyacentes a la casilla actual.
     * @param row Fila de la casilla actual
     * @param col Columna de la casilla actual
     */
    private fun revelarCeldasAdyacentes(row: Int, col: Int) {
        val visited = Array(rows) { BooleanArray(cols) }

        fun revelar(r: Int, c: Int) {
            // Comprobar si esta fuera de los limites o si ya ha sido visitada para salir
            if (r !in 0 until rows || c !in 0 until cols || visited[r][c]) return
            visited[r][c] = true
            // Si es una mina sale
            if (esMina(r, c)) return
            // Si no es una mina se revela el numero de minas adyacentes
            val minasAdyacentes = contarMinasAdyacentes(r, c)
            botones[r][c].background = AppCompatResources.getDrawable(this, R.drawable.btn_background_secondary)
            if (minasAdyacentes ==  0){
                botones[r][c].text = ""
                botones[r][c].isEnabled = false

            }else{
                botones[r][c].text = minasAdyacentes.toString()
            }

            // Si no hay minas adyacentes se vuelve a llamar a la funcion
            if (minasAdyacentes == 0) {
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i != 0 || j != 0) {
                            revelar(r + i, c + j)
                        }
                    }
                }
            }
        }
        revelar(row, col)
    }

    /**
     * Muestra una alerta para reiniciar la partida.
     * @param opcion 1 si se ha ganado la partida, 2 si se ha perdido
     */
    private fun mostrarAlertaReinicio(opcion: Int) {
        val titulo = if (opcion == 1) "¡Ganaste!" else "¡Perdiste!"

        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(titulo)
            setMessage("¿Quieres reiniciar la partida?")
            setPositiveButton("Sí") { _, _ ->
                crearTablero()
            }
            setNegativeButton("No") { dialog, _ ->
                juegoEnCurso = false
                dialog.dismiss()
            }
            setCancelable(false)
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Muestra una alerta para poder cambiar de personaje.
     */
    private fun mostrarAlertaPersonaje() {
        val dialogView = layoutInflater.inflate(R.layout.alert_spinner, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner)

        // Configurar la lista de personajes con sus nombres e IDs de recursos de imágenes
        val personajes = listOf(
            Personaje("Manzana", R.drawable.manzana),
            Personaje("Pera", R.drawable.pera),
            Personaje("Sandia", R.drawable.sandia)
        )

        // Configurar el adaptador personalizado
        val adapter = PersonajeAdapter(this, R.layout.item_personaje_spinner, personajes)
        spinner.adapter = adapter

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona un Personaje")
        builder.setView(dialogView)
        builder.setPositiveButton("Aceptar") { _, _ ->
            val selectedPersonaje = spinner.selectedItem as Personaje
            iconoHipotenocha = selectedPersonaje.imagenResId
            invalidateOptionsMenu()

        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    /**
     * Muestra una alerta para poder cambiar la dificultad del juego.
     */
    private fun mostrarAlertaDificultad() {
        val dialogView = layoutInflater.inflate(R.layout.alert_spinner, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner)

        // Configurar el adaptador para el Spinner
        val spinnerItems = arrayOf("Facil", "Intermedio", "Dificil")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems)
        spinner.adapter = adapter

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una dificultad")
        builder.setView(dialogView)
        builder.setPositiveButton("Aceptar") { _ , _ ->

            // Dependiendo de la dificultad se cambia el tamaño del tablero y la cantidad de minas
            when(spinner.selectedItem as String){
                "Facil" -> {
                    rows = 8
                    cols = 8
                    totalMinas = 10
                }
                "Intermedio" -> {
                    rows = 12
                    cols = 12
                    totalMinas = 30
                }
                "Dificil" -> {
                    rows = 16
                    cols = 16
                    totalMinas = 60
                }
            }
            crearTablero()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }


}