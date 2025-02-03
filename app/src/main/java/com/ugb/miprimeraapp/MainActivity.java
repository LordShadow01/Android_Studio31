package com.ugb.miprimeraapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btn;
    TextView tempVal; // like flames
    RadioGroup rgb; // oh no,oh si xd
    RadioButton opt; // storyteller
    EditText num1, num2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btnCalcular);
        num1 = findViewById(R.id.txtNum1);
        num2 = findViewById(R.id.txtNum2);
        rgb = findViewById(R.id.rgoOpciones);

        rgb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.optRaiz || checkedId == R.id.optFactorial || checkedId == R.id.optCubica) {
                    num2.setText("");
                    num2.setEnabled(false);
                } else {
                    num2.setEnabled(true);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempVal = findViewById(R.id.txtNum1);
                double num1 = Double.parseDouble(tempVal.getText().toString());
                tempVal = findViewById(R.id.txtNum2);
                double num2 = tempVal.isEnabled() ? Double.parseDouble(tempVal.getText().toString()) : 0;
                double respuesta = 0.0;

                opt = findViewById(R.id.optSuma);
                if (opt.isChecked()) {
                    respuesta = num1 + num2;
                }
                opt = findViewById(R.id.optResta);
                if (opt.isChecked()) {
                    respuesta = num1 - num2;
                }
                opt = findViewById(R.id.optMultiplicacion);
                if (opt.isChecked()) {
                    respuesta = num1 * num2;
                }
                opt = findViewById(R.id.optDivision);
                if (opt.isChecked()) {
                    respuesta = num1 / num2;
                }
                opt = findViewById(R.id.optExponente);
                if (opt.isChecked()) {
                    respuesta = Math.pow(num1, num2);
                }
                opt = findViewById(R.id.optPorcentaje);
                if (opt.isChecked()) {
                    respuesta = (num1 * num2) / 100;
                }
                opt = findViewById(R.id.optRaiz);
                if (opt.isChecked()) {
                    respuesta = Math.sqrt(num1);
                }
                opt= findViewById(R.id.optFactorial);
                if (opt.isChecked()){
                    respuesta = 1;
                    for (int i = 2; i <= num1; i++){
                        respuesta *= i;
                    }
                }
                opt = findViewById(R.id.optCubica); // quiero un cafe
                if (opt.isChecked()){
                    respuesta = Math.cbrt(num1);
                }

                tempVal = findViewById(R.id.lblRespuesta);
                tempVal.setText("Respuesta: " + respuesta);
            }
        });
    }
}