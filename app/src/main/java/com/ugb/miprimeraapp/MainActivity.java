package com.ugb.miprimeraapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Componentes de la pestaña Agua
    private EditText editTextMtsConsumidos;
    private Button buttonCalcular;
    private TextView textViewValorPagar;

    // Componentes de la pestaña Area
    private EditText editTextValor;
    private Spinner spinnerDesde, spinnerHasta;
    private TextView textViewResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar TabHost
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        // Tab Agua
        TabHost.TabSpec spec = tabHost.newTabSpec("Agua");
        spec.setContent(R.id.tabAgua);
        spec.setIndicator("Agua");
        tabHost.addTab(spec);

        // Tab Area
        spec = tabHost.newTabSpec("Area");
        spec.setContent(R.id.tabArea);
        spec.setIndicator("Area");
        tabHost.addTab(spec);

        // Inicializar componentes de Agua
        editTextMtsConsumidos = findViewById(R.id.editTextMtsConsumidos);
        buttonCalcular = findViewById(R.id.buttonCalcular);
        textViewValorPagar = findViewById(R.id.textViewValorPagar);

        // Inicializar componentes de Area
        editTextValor = findViewById(R.id.editTextValor);
        spinnerDesde = findViewById(R.id.spinnerDesde);
        spinnerHasta = findViewById(R.id.spinnerHasta);
        textViewResultado = findViewById(R.id.textViewResultado);

        // Configurar listeners
        buttonCalcular.setOnClickListener(v -> calcularValorAgua());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.unidad5es_area,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDesde.setAdapter(adapter);
        spinnerHasta.setAdapter(adapter);

        spinnerDesde.setOnItemSelectedListener(this);
        spinnerHasta.setOnItemSelectedListener(this);
    }

    // Métodos para la pestaña Agua
    private void calcularValorAgua() {
        String mtsConsumidosStr = editTextMtsConsumidos.getText().toString();
        if (mtsConsumidosStr.isEmpty()) {
            textViewValorPagar.setText("Ingrese los metros consumidos");
            return;
        }

        double mtsConsumidos = Double.parseDouble(mtsConsumidosStr);
        double valorPagar = 0.0;

        if (mtsConsumidos <= 18) {
            valorPagar = 6.0;
        } else if (mtsConsumidos <= 28) {
            valorPagar = 6.0 + (mtsConsumidos - 18) * 0.45;
        } else {
            valorPagar = 6.0 + (10 * 0.45) + (mtsConsumidos - 28) * 0.65;
        }

        textViewValorPagar.setText("Valor a pagar: $" + String.format("%.2f", valorPagar));
    }

    // Métodos para la pestaña Area
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        convertirArea();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No hacer nada
    }

    private void convertirArea() {
        String valorStr = editTextValor.getText().toString();
        if (valorStr.isEmpty()) {
            textViewResultado.setText("Ingrese un valor");
            return;
        }

        double valor = Double.parseDouble(valorStr);
        String desde = spinnerDesde.getSelectedItem().toString();
        String hasta = spinnerHasta.getSelectedItem().toString();

        double resultado = convertir(valor, desde, hasta);
        textViewResultado.setText(String.format("%.4f %s", resultado, hasta));
    }

    private double convertir(double valor, String desde, String hasta) {
        double metrosCuadrados = 0.0;
        switch (desde) {
            case "Pie Cuadrado":
                metrosCuadrados = valor * 0.092903;
                break;
            case "Vara Cuadrada":
                metrosCuadrados = valor * 0.69873;
                break;
            case "Yarda Cuadrada":
                metrosCuadrados = valor * 0.836127;
                break;
            case "Metro Cuadrado":
                metrosCuadrados = valor;
                break;
            case "Tarea":
                metrosCuadrados = valor * 5661.46;
                break;
            case "Manzana":
                metrosCuadrados = valor * 7000;
                break;
            case "Hectárea":
                metrosCuadrados = valor * 10000;
                break;
        }

        double resultado = 0.0;
        switch (hasta) {
            case "Pie Cuadrado":
                resultado = metrosCuadrados / 0.092903;
                break;
            case "Vara Cuadrada":
                resultado = metrosCuadrados / 0.69873;
                break;
            case "Yarda Cuadrada":
                resultado = metrosCuadrados / 0.836127;
                break;
            case "Metro Cuadrado":
                resultado = metrosCuadrados;
                break;
            case "Tarea":
                resultado = metrosCuadrados / 5661.46;
                break;
            case "Manzana":
                resultado = metrosCuadrados / 7000;
                break;
            case "Hectárea":
                resultado = metrosCuadrados / 10000;
                break;
        }

        return resultado;
    }
}