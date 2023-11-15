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
import androidx.gridlayout.widget.GridLayout
import java.util.Random

class MainActivity : AppCompatActivity() {


    private var rows = 8
    private var cols = 8
    private var totalMinas = 10
    private lateinit var botones: Array<Array<Button>>
    private lateinit var tableroMinas: Array<BooleanArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inflar la toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.nav_menu)
        setSupportActionBar(toolbar)

        crearTablero()
    }

    // Inflar el menú
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Opciones del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Instrucciones del juego
            R.id.instrucciones -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setTitle("Instrucciones")
                    setMessage("")
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun crearTablero() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // borrar todos los botones cada vez que se cree la partida
        gridLayout.removeAllViews()

        // Definir las medidas del tablero
        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        // Array donde se guarda la posicion de cada mina
        tableroMinas = Array(rows) { BooleanArray(cols) }

        // Obtiene el ancho de la pantalla para calcular el ancho de los botones
        val anchoPantalla = resources.displayMetrics.widthPixels
        val anchoBoton = anchoPantalla / cols

        // Se define el array de botones iterando sobre las filas y columnas para crear cada botón
        botones = Array(rows) { row ->
            Array(cols) { col ->
                val boton = Button(this)
                // Se define el tamaño de cada botón
                boton.layoutParams = GridLayout.LayoutParams().apply {
                    width = anchoBoton
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                // Se elimina el padding para que se visualice correctamente en maxima dificultad
                boton.setPadding(0,0,0,0)

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

    private fun buttonListeners(boton: Button, row: Int, col: Int){
        // Listener para cuando se hace click en un botón que comprueba si el boton es una mina
        // o si la casilla esta marcada
        boton.setOnClickListener {
            if (boton.text == "F") return@setOnClickListener

            // Si es mina se pierde la partida, si no lo es se revela esa casilla
            // y las adyacentes que no tengan minas cerca
            if (esMina(row, col)) {
                mostrarAlertaReinicio()
                boton.text = "X"
            } else {
                val minasAdyacentes = contarMinasAdyacentes(row, col)
                boton.text = minasAdyacentes.toString()
                if (boton.text.toString().toInt() == 0){
                    revelarCeldasAdyacentes(row, col)
                }
            }
        }
        // Listener para cuando se hace click largo en un botón, para marcar la casilla
        boton.setOnLongClickListener {
            if (esMina(row, col)) {
                // Crear toast en caso de se encuentre una hipotenocha
                val toast = Toast.makeText(this, "¡Hipotenocha encontrada!", Toast.LENGTH_SHORT)
                toast.show()
            }else{
                mostrarAlertaReinicio()
            }
            if (boton.text == "F") {
                boton.text = ""
            } else {
                boton.text = "F"
            }
            true
        }
    }

    // Comprobar si una casilla es una mina
    private fun esMina(row: Int, col: Int): Boolean {
        return tableroMinas[row][col]
    }

    // Contar y devolver la cantidad de minas adyacentes a una casilla
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
            botones[r][c].text = minasAdyacentes.toString()
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

    // Muestra una alerta para cuando el usuario pierde la partida y le permite reiniciar
    private fun mostrarAlertaReinicio() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("¡Perdiste!")
            setMessage("¿Quieres reiniciar la partida?")
            setPositiveButton("Sí") { _, _ ->
                crearTablero()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Muestra una alerta para poder cambiar de dificultad
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
            val selectedOption = spinner.selectedItem as String

            // Dependiendo de la dificultad se cambia el tamaño del tablero y la cantidad de minas
            when(selectedOption){
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