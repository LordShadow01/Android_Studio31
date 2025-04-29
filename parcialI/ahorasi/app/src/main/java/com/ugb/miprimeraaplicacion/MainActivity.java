package com.ugb.miprimeraaplicacion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    Button btn;
    TextView tempVal;
    DB db;
    String accion = "nuevo", idProducto = "", id = "", rev = "";
    ImageView img;
    String urlCompletaFoto = "";
    Intent tomarFotoIntent;
    utilidades utls;
    detectarInternet di;

    private TextView txtCodigo, txtDescripcion, txtMarca, txtPresentacion, txtPrecio, txtCosto, txtStock; // Added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utls = new utilidades();
        img = findViewById(R.id.imgFotoProducto);
        db = new DB(this);
        btn = findViewById(R.id.btnGuardarProducto);

        txtCodigo = findViewById(R.id.txtCodigo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtMarca = findViewById(R.id.txtMarca);
        txtPresentacion = findViewById(R.id.txtPresentacion);
        txtPrecio = findViewById(R.id.txtPrecio);
        txtCosto = findViewById(R.id.txtCosto);  // Added
        txtStock = findViewById(R.id.txtStock);  // Added

        btn.setOnClickListener(view -> guardarProducto());

        fab = findViewById(R.id.fabListaProductos);
        fab.setOnClickListener(view -> abrirVentana());

        mostrarDatos();
        tomarFoto();
    }

    private void tomarFoto() {
        img.setOnClickListener(view -> {
            tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File fotoProducto = null;
            try {
                fotoProducto = crearImagenProducto();
                if (fotoProducto != null) {
                    Uri uriFotoProducto = FileProvider.getUriForFile(MainActivity.this,
                            "com.ugb.miprimeraaplicacion.fileprovider", fotoProducto);
                    tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoProducto);
                    startActivityForResult(tomarFotoIntent, 1);
                } else {
                    mostrarMsg("No se pudo crear la imagen");
                }
            } catch (Exception e) {
                mostrarMsg("Error: " + e.getMessage());
            }
        });
    }

    private File crearImagenProducto() throws Exception {
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "producto_" + fechaHoraMs + "_";
        File dirAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if (!dirAlmacenamiento.exists()) {
            dirAlmacenamiento.mkdir();
        }
        File image = File.createTempFile(fileName, ".jpg", dirAlmacenamiento);
        urlCompletaFoto = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Bitmap bitmap = BitmapFactory.decodeFile(urlCompletaFoto);
                img.setImageBitmap(bitmap);
            } else {
                mostrarMsg("No se tomó la foto");
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void mostrarDatos() {
        try {
            Bundle parametros = getIntent().getExtras();
            accion = parametros.getString("accion");
            if (accion.equals("modificar")) {
                JSONObject datos = new JSONObject(parametros.getString("producto"));
                id = datos.getString("_id");
                rev = datos.getString("_rev");
                idProducto = datos.getString("idProducto");

                txtCodigo.setText(datos.getString("codigo"));
                txtDescripcion.setText(datos.getString("descripcion"));
                txtMarca.setText(datos.getString("marca"));
                txtPresentacion.setText(datos.getString("presentacion"));
                txtPrecio.setText(datos.getString("precio"));
                txtCosto.setText(String.valueOf(datos.getDouble("costo")));   // Added
                txtStock.setText(String.valueOf(datos.getInt("stock")));      // Added

                urlCompletaFoto = datos.getString("urlFoto");
                img.setImageURI(Uri.parse(urlCompletaFoto));
            } else {
                idProducto = utls.generarUnicoId();
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void guardarProducto() {
        try {
            // Obtener valores de los campos
            String codigo = txtCodigo.getText().toString();
            String descripcion = txtDescripcion.getText().toString();
            String marca = txtMarca.getText().toString();
            String presentacion = txtPresentacion.getText().toString();
            String precio = txtPrecio.getText().toString();
            double costo = Double.parseDouble(txtCosto.getText().toString());  // Added
            int stock = Integer.parseInt(txtStock.getText().toString());     // Added

            di = new detectarInternet(this);
            String resultado = "ok";

            if (di.hayConexionInternet()) {
                // Guardar en CouchDB
                JSONObject datosProducto = new JSONObject();
                if (accion.equals("modificar")) {
                    datosProducto.put("_id", id);
                    datosProducto.put("_rev", rev);
                }
                datosProducto.put("idProducto", idProducto);
                datosProducto.put("codigo", codigo);
                datosProducto.put("descripcion", descripcion);
                datosProducto.put("marca", marca);
                datosProducto.put("presentacion", presentacion);
                datosProducto.put("precio", precio);
                datosProducto.put("urlFoto", urlCompletaFoto);
                datosProducto.put("costo", costo);  // Added
                datosProducto.put("stock", stock);  // Added

                enviarDatosServidor objEnviar = new enviarDatosServidor(this);
                String respuesta = objEnviar.execute(datosProducto.toString(), "POST", utilidades.url_mto).get();

                JSONObject respuestaJSON = new JSONObject(respuesta);
                if (respuestaJSON.getBoolean("ok")) {
                    id = respuestaJSON.getString("id");
                    rev = respuestaJSON.getString("rev");

                    // Preparar datos para SQLite
                    String[] datosSQLite = {
                            idProducto,
                            id,
                            rev,
                            "sincronizado",
                            codigo,
                            descripcion,
                            marca,
                            presentacion,
                            precio,
                            urlCompletaFoto,
                            String.valueOf(costo),  // Added
                            String.valueOf(stock)   // Added
                    };

                    // Determinar acción para SQLite
                    String accionSQLite = accion.equals("modificar") ? "modificar" : "nuevo";
                    resultado = db.administrar_productos(accionSQLite, datosSQLite);

                    if (resultado.equals("ok")) {
                        mostrarMsg(accion.equals("modificar") ?
                                "Producto actualizado correctamente" :
                                "Producto guardado en ambas bases");
                    }
                } else {
                    resultado = "Error CouchDB: " + respuestaJSON.getString("error");
                }
            } else {
                // Guardar solo en SQLite
                String[] datosSQLite = {
                        idProducto,
                        "",
                        "",
                        "pendiente",
                        codigo,
                        descripcion,
                        marca,
                        presentacion,
                        precio,
                        urlCompletaFoto,
                        String.valueOf(costo),  // Added
                        String.valueOf(stock)   // Added
                };
                resultado = db.administrar_productos(accion.equals("modificar") ? "modificar" : "nuevo", datosSQLite);
                mostrarMsg(accion.equals("modificar") ?
                        "Modificación guardada localmente" :
                        "Producto guardado en modo offline");
            }

            if (resultado.equals("ok")) {
                abrirVentana();
            } else {
                mostrarMsg(resultado);
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void abrirVentana() {
        Intent intent = new Intent(this, lista_productos.class);
        startActivity(intent);
    }
}