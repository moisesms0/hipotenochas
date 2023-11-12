package com.babilawi.buscaminas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.gridlayout.widget.GridLayout
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private val filas = 8
    private val columnas = 8
    private lateinit var botones: Array<Array<Button>>
    private lateinit var tableroMinas: Array<BooleanArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.nav_menu)
        setSupportActionBar(toolbar)

        gridLayout = findViewById(R.id.gridLayout)

        crearTablero()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.instrucciones -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setTitle("Instrucciones")
                    setMessage("")
                    setPositiveButton("Aceptar") { _, _ ->
                    }

                }
                val dialog = builder.create()
                dialog.show()}

            R.id.configuraJuego -> {

            }

            R.id.nuevoJuego -> {
                reiniciarJuego()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun crearTablero() {
        gridLayout.rowCount = filas
        gridLayout.columnCount = columnas

        tableroMinas = Array(filas) { BooleanArray(columnas) }

        // Obtiene el ancho de la pantalla para calcular el ancho de los botones
        val anchoPantalla = resources.displayMetrics.widthPixels
        val anchoBoton = anchoPantalla / columnas

        botones = Array(filas) { row ->
            Array(columnas) { col ->
                val boton = Button(this)
                boton.layoutParams = GridLayout.LayoutParams().apply {
                    width = anchoBoton
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                boton.setOnClickListener {
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
                boton.setOnLongClickListener {
                    if (boton.text == "F") {
                        boton.text = ""
                    } else {
                        boton.text = "F"
                    }
                    true
                }
                gridLayout.addView(boton)
                boton
            }
        }

        colocarMinasAleatoriamente()
    }

    private fun esMina(row: Int, col: Int): Boolean {
        return tableroMinas[row][col]
    }

    private fun contarMinasAdyacentes(row: Int, col: Int): Int {
        var count = 0

        for (i in -1..1) {
            for (j in -1..1) {
                val r = row + i
                val c = col + j

                if (r in 0 until filas && c in 0 until columnas && !(i == 0 && j == 0)) {
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
        val totalMinas = 10
        var minasColocadas = 0

        while (minasColocadas < totalMinas) {
            val x = random.nextInt(filas)
            val y = random.nextInt(columnas)

            if (!tableroMinas[x][y]) {
                tableroMinas[x][y] = true
                minasColocadas++
            }
        }
    }

    private fun reiniciarJuego() {
        for (i in 0 until filas) {
            for (j in 0 until columnas) {
                // Limpiar el tablero
                botones[i][j].text = ""
                // quitar las minas
                tableroMinas[i][j] = false
            }
        }
        colocarMinasAleatoriamente()
    }

    private fun revelarCeldasAdyacentes(row: Int, col: Int) {
        val visited = Array(filas) { BooleanArray(columnas) }

        fun dfs(r: Int, c: Int) {
            if (r !in 0 until filas || c !in 0 until columnas || visited[r][c]) return
            visited[r][c] = true
            if (esMina(r, c)) return
            val minasAdyacentes = contarMinasAdyacentes(r, c)
            botones[r][c].text = minasAdyacentes.toString()
            if (minasAdyacentes == 0) {
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i != 0 || j != 0) {
                            dfs(r + i, c + j)
                        }
                    }
                }
            }
        }

        dfs(row, col)
    }

    private fun mostrarAlertaReinicio() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("¡Perdiste!")
            setMessage("¿Quieres reiniciar la partida?")
            setPositiveButton("Sí") { _, _ ->
                reiniciarJuego()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}