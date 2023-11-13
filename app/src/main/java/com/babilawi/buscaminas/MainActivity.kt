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

        // Inflar el toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.nav_menu)
        setSupportActionBar(toolbar)

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
                val instrucciones = builder.create()
                instrucciones.show()}

            R.id.configuraJuego -> {
                mostrarAlertaDificultad()
            }
            R.id.nuevoJuego -> {
                crearTablero()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun crearTablero() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // borrar todos los botones
        gridLayout.removeAllViews()

        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        tableroMinas = Array(rows) { BooleanArray(cols) }

        // Obtiene el ancho de la pantalla para calcular el ancho de los botones
        val anchoPantalla = resources.displayMetrics.widthPixels
        val anchoBoton = anchoPantalla / cols

        botones = Array(rows) { row ->
            Array(cols) { col ->
                val boton = Button(this)
                boton.layoutParams = GridLayout.LayoutParams().apply {
                    width = anchoBoton
                    height = ViewGroup.LayoutParams.WRAP_CONTENT


                }
                boton.setPadding(0,0,0,0)
                buttonListeners(boton, row, col)
                gridLayout.addView(boton)
                boton
            }
        }
        colocarMinasAleatoriamente()
    }

    private fun buttonListeners(boton: Button, row: Int, col: Int){
        boton.setOnClickListener {
            if (boton.text == "F") return@setOnClickListener
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
            if (esMina(row, col)) {
                // crear toast
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

    private fun esMina(row: Int, col: Int): Boolean {
        return tableroMinas[row][col]
    }

    private fun contarMinasAdyacentes(row: Int, col: Int): Int {
        var count = 0

        for (i in -1..1) {
            for (j in -1..1) {
                val r = row + i
                val c = col + j

                if (r in 0 until rows && c in 0 until cols && !(i == 0 && j == 0)) {
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

        while (minasColocadas < totalMinas) {
            val x = random.nextInt(rows)
            val y = random.nextInt(cols)

            if (!tableroMinas[x][y]) {
                tableroMinas[x][y] = true
                minasColocadas++
            }
        }
    }


    private fun revelarCeldasAdyacentes(row: Int, col: Int) {
        val visited = Array(rows) { BooleanArray(cols) }

        fun revelar(r: Int, c: Int) {
            if (r !in 0 until rows || c !in 0 until cols || visited[r][c]) return
            visited[r][c] = true
            if (esMina(r, c)) return
            val minasAdyacentes = contarMinasAdyacentes(r, c)
            botones[r][c].text = minasAdyacentes.toString()
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