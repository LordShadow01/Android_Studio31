package com.ugb.miprimeraapp;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI Elements for Calculator Tab
    private EditText editTextConsumo;
    private Button buttonCalcular;
    private TextView textViewResultado;

    // UI Elements for Area Converter
    private EditText editTextAreaValue;
    private Spinner spinnerFromUnit;
    private Spinner spinnerToUnit;
    private Button buttonConvertArea;
    private TextView textViewAreaResult;

    // UI Elements for Sensor Tab
    private TextView proximityVal;
    private TextView accelerometerVal;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private Sensor accelerometerSensor;
    private SensorEventListener sensorEventListener;
    private Button btnStartSensors;
    private Button btnStopSensors;
    private boolean sensorsActive = false;

    // Conversion Factors (Salvadoran Units - VERIFY!)
    private static final double PIE_CUADRADO_TO_METRO_CUADRADO = 0.092903;
    private static final double VARA_CUADRADA_TO_METRO_CUADRADO = 0.698896;  // Approximate conversion, local vara can vary
    private static final double YARDA_CUADRADA_TO_METRO_CUADRADO = 0.836127;
    private static final double TAREA_TO_METRO_CUADRADO = 628.992; // Approximate, verify locally.
    private static final double MANZANA_TO_METRO_CUADRADO = 7000; // Approximate, verify locally.
    private static final double HECTAREA_TO_METRO_CUADRADO = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TabHost
        TabHost tabHost = findViewById(android.R.id.tabhost);
        tabHost.setup();

        // Tab 1: Calculator
        TabHost.TabSpec spec = tabHost.newTabSpec("calculadora");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Calculadora");
        tabHost.addTab(spec);

        // Tab 2: Area Converter and Sensors
        spec = tabHost.newTabSpec("conversor");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Conversor/Sensores");
        tabHost.addTab(spec);

        // Calculator Tab Initialization
        editTextConsumo = findViewById(R.id.editTextConsumo);
        buttonCalcular = findViewById(R.id.buttonCalcular);
        textViewResultado = findViewById(R.id.textViewResultado);
        buttonCalcular.setOnClickListener(this);

        // Area Converter Initialization
        editTextAreaValue = findViewById(R.id.editTextAreaValue);
        spinnerFromUnit = findViewById(R.id.spinnerFromUnit);
        spinnerToUnit = findViewById(R.id.spinnerToUnit);
        buttonConvertArea = findViewById(R.id.buttonConvertArea);
        textViewAreaResult = findViewById(R.id.textViewAreaResult);
        buttonConvertArea.setOnClickListener(this);

        // Populate Spinners for Area Units
        List<String> areaUnits = Arrays.asList(
                "Pie Cuadrado", "Vara Cuadrada", "Yarda Cuadrada", "Metro Cuadrado",
                "Tarea", "Manzana", "Hectárea"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, areaUnits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromUnit.setAdapter(adapter);
        spinnerToUnit.setAdapter(adapter);

        // Sensor Tab Initialization
        proximityVal = findViewById(R.id.lblSensorProximidad);
        accelerometerVal = findViewById(R.id.lblSensorAcelerometro);
        btnStartSensors = findViewById(R.id.btnStartSensors);
        btnStopSensors = findViewById(R.id.btnStopSensors);

        btnStartSensors.setOnClickListener(this);
        btnStopSensors.setOnClickListener(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (proximitySensor == null) {
            proximityVal.setText("Tu dispositivo NO tiene el sensor de PROXIMIDAD");
        }
        if (accelerometerSensor == null) {
            accelerometerVal.setText("Tu dispositivo NO tiene el sensor de ACELEROMETRO");
        }

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    float distance = event.values[0];
                    proximityVal.setText("Proximidad: " + distance);

                    if (distance <= 4) {
                        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
                    } else if (distance <= 8) {
                        getWindow().getDecorView().setBackgroundColor(Color.GRAY);
                    } else {
                        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    }
                } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];
                    accelerometerVal.setText("Desplazamiento X= " + x + "; Y= " + y + "; Z= " + z);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Handle accuracy changes if needed.
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonCalcular) {
            calcularValorAgua();
        } else if (v.getId() == R.id.buttonConvertArea) {
            convertArea();
        } else if (v.getId() == R.id.btnStartSensors) {
            startSensors();
        } else if (v.getId() == R.id.btnStopSensors) {
            stopSensors();
        }
    }

    private void calcularValorAgua() {
        try {
            double consumo = Double.parseDouble(editTextConsumo.getText().toString());
            double valorAPagar = 0.0;

            if (consumo >= 1 && consumo <= 18) {
                valorAPagar = 6.0;
            } else if (consumo >= 19 && consumo <= 28) {
                valorAPagar = 6.0 + (consumo - 18) * 0.45;
            } else if (consumo >= 29) {
                valorAPagar = 6.0 + (10 * 0.45) + (consumo - 28) * 0.65;
            }

            textViewResultado.setText("Total a Pagar: $" + String.format("%.2f", valorAPagar));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido para el consumo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertArea() {
        try {
            double areaValue = Double.parseDouble(editTextAreaValue.getText().toString());
            String fromUnit = spinnerFromUnit.getSelectedItem().toString();
            String toUnit = spinnerToUnit.getSelectedItem().toString();

            double areaInMeters = convertToMeters(areaValue, fromUnit);
            double result = convertFromMeters(areaInMeters, toUnit);

            textViewAreaResult.setText("Resultado: " + String.format("%.4f", result) + " " + toUnit);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un valor de área válido.", Toast.LENGTH_SHORT).show();
        }
    }

    private double convertToMeters(double areaValue, String fromUnit) {
        switch (fromUnit) {
            case "Pie Cuadrado":
                return areaValue * PIE_CUADRADO_TO_METRO_CUADRADO;
            case "Vara Cuadrada":
                return areaValue * VARA_CUADRADA_TO_METRO_CUADRADO;
            case "Yarda Cuadrada":
                return areaValue * YARDA_CUADRADA_TO_METRO_CUADRADO;
            case "Tarea":
                return areaValue * TAREA_TO_METRO_CUADRADO;
            case "Manzana":
                return areaValue * MANZANA_TO_METRO_CUADRADO;
            case "Hectárea":
                return areaValue * HECTAREA_TO_METRO_CUADRADO;
            case "Metro Cuadrado":
            default:
                return areaValue;
        }
    }

    private double convertFromMeters(double areaInMeters, String toUnit) {
        switch (toUnit) {
            case "Pie Cuadrado":
                return areaInMeters / PIE_CUADRADO_TO_METRO_CUADRADO;
            case "Vara Cuadrada":
                return areaInMeters / VARA_CUADRADA_TO_METRO_CUADRADO;
            case "Yarda Cuadrada":
                return areaInMeters / YARDA_CUADRADA_TO_METRO_CUADRADO;
            case "Tarea":
                return areaInMeters / TAREA_TO_METRO_CUADRADO;
            case "Manzana":
                return areaInMeters / MANZANA_TO_METRO_CUADRADO;
            case "Hectárea":
                return areaInMeters / HECTAREA_TO_METRO_CUADRADO;
            case "Metro Cuadrado":
            default:
                return areaInMeters;
        }
    }

    private void startSensors() {
        if (!sensorsActive) {
            if (proximitySensor != null) {
                sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (accelerometerSensor != null) {
                sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            sensorsActive = true;
        }
    }

    private void stopSensors() {
        if (sensorsActive) {
            sensorManager.unregisterListener(sensorEventListener);
            sensorsActive = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Do not automatically start sensors on resume
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors(); // Stop sensors when the activity is paused
    }
}