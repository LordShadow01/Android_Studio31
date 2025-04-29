package com.ugb.miprimeraaplicacion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class    lista_productos extends Activity {
    Bundle parametros = new Bundle();
    ListView ltsProductos;
    Cursor cProductos;
    DB db;
    final ArrayList<Producto> alProductos = new ArrayList<>();
    final ArrayList<Producto> alProductosCopia = new ArrayList<>();
    JSONArray jsonArray;
    JSONObject jsonObject;
    Producto miProducto;
    FloatingActionButton fab;
    int posicion = 0;
    obtenerDatosServidor datosServidor;
    detectarInternet di;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_productos);

        parametros.putString("accion", "nuevo");
        db = new DB(this);

        new SyncManager(this).sincronizar();

        fab = findViewById(R.id.fabAgregarProducto);
        fab.setOnClickListener(view -> abrirVentana());
        listarDatos();
        buscarProductos();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mimenu, menu);
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            posicion = info.position;

            JSONObject producto = jsonArray.getJSONObject(posicion).getJSONObject("value");
            menu.setHeaderTitle(producto.has("descripcion") ? producto.getString("descripcion") : "Producto");
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int posicionListaFiltrada = info.position;

            Producto productoSeleccionado = alProductos.get(posicionListaFiltrada);

            int posicionReal = -1;
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i).getJSONObject("value");
                if(obj.getString("idProducto").equals(productoSeleccionado.getIdProducto())) {
                    posicionReal = i;
                    break;
                }
            }

            if(posicionReal == -1) {
                mostrarMsg("No se encontró el producto");
                return true;
            }

            if (item.getItemId() == R.id.mnxNuevo) {
                abrirVentana();
            } else if (item.getItemId() == R.id.mnxModificar) {
                parametros.putString("accion", "modificar");
                parametros.putString("producto", jsonArray.getJSONObject(posicionReal).getJSONObject("value").toString());
                abrirVentana();
            } else if (item.getItemId() == R.id.mnxEliminar) {
                posicion = posicionReal;
                eliminarProducto();
            }
            return true;
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
            return super.onContextItemSelected(item);
        }
    }

    private void eliminarProducto() {
        try {
            JSONObject producto = jsonArray.getJSONObject(posicion).getJSONObject("value");
            String idProducto = producto.getString("idProducto");
            String descripcion = producto.getString("descripcion");

            AlertDialog.Builder confirmacion = new AlertDialog.Builder(this);
            confirmacion.setTitle("¿Eliminar producto?");
            confirmacion.setMessage(descripcion);
            confirmacion.setPositiveButton("Si", (dialog, which) -> {
                try {
                    di = new detectarInternet(lista_productos.this);
                    if(di.hayConexionInternet()){
                        String _id = producto.getString("_id");
                        String _rev = producto.getString("_rev");
                        String url = utilidades.url_mto + "/" + _id + "?rev=" + _rev;

                        enviarDatosServidor objEnviar = new enviarDatosServidor(lista_productos.this);
                        String respuesta = objEnviar.execute(new JSONObject().toString(), "DELETE", url).get();

                        JSONObject respuestaJSON = new JSONObject(respuesta);
                        if(!respuestaJSON.getBoolean("ok")) {
                            mostrarMsg("Error en servidor: " + respuesta);
                            return;
                        }
                    }

                    String resultado = db.administrar_productos("eliminar", new String[]{idProducto});
                    if(!resultado.equals("ok")){
                        mostrarMsg("Error en base local: " + resultado);
                        return;
                    }

                    eliminarDeListas(idProducto);

                    ltsProductos.setAdapter(new AdaptadorProductos(lista_productos.this, alProductos));
                    mostrarMsg("Producto eliminado correctamente");

                } catch (Exception e) {
                    mostrarMsg("Error durante eliminación: " + e.getMessage());
                }
            });
            confirmacion.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            confirmacion.create().show();

        } catch (Exception e) {
            mostrarMsg("Error inicial: " + e.getMessage());
        }
    }

    private void eliminarDeListas(String idProducto) {
        for(int i = 0; i < alProductosCopia.size(); i++){
            if(alProductosCopia.get(i).getIdProducto().equals(idProducto)){
                alProductosCopia.remove(i);
                break;
            }
        }

        for(int i = 0; i < alProductos.size(); i++){
            if(alProductos.get(i).getIdProducto().equals(idProducto)){
                alProductos.remove(i);
                break;
            }
        }
    }

    private void abrirVentana() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(parametros);
        startActivity(intent);
    }

    private void listarDatos() {
        try {
            di = new detectarInternet(this);
            if (di.hayConexionInternet()) {
                datosServidor = new obtenerDatosServidor();
                String respuesta = datosServidor.execute().get();
                jsonObject = new JSONObject(respuesta);
                jsonArray = jsonObject.getJSONArray("rows");
                mostrarDatosProductos();
            } else {
                obtenerDatosProductos();
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void obtenerDatosProductos() {
        try {
            cProductos = db.lista_productos();
            if (cProductos.moveToFirst()) {
                jsonArray = new JSONArray();
                do {
                    jsonObject = new JSONObject();
                    jsonObject.put("idProducto", cProductos.getString(0));
                    jsonObject.put("codigo", cProductos.getString(4));
                    jsonObject.put("descripcion", cProductos.getString(5));
                    jsonObject.put("marca", cProductos.getString(6));
                    jsonObject.put("presentacion", cProductos.getString(7));
                    jsonObject.put("precio", cProductos.getString(8));
                    jsonObject.put("urlFoto", cProductos.getString(9));
                    jsonObject.put("costo", cProductos.getString(10));
                    jsonObject.put("stock", cProductos.getString(11));
                    jsonArray.put(jsonObject);
                } while (cProductos.moveToNext());
                mostrarDatosProductos();
            } else {
                mostrarMsg("No hay productos registrados");
                abrirVentana();
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void mostrarDatosProductos() {
        try {
            if (jsonArray.length() > 0) {
                ltsProductos = findViewById(R.id.ltsProductos);
                alProductos.clear();
                alProductosCopia.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    JSONObject value = row.getJSONObject("value");

                    double costo = 0.0; // Valores por defecto en caso de error
                    int stock = 0;

                    try {
                        costo = value.getDouble("costo");
                        stock = value.getInt("stock");
                    } catch (Exception e) {
                        mostrarMsg("Error al obtener costo o stock del JSON: " + e.getMessage());
                        e.printStackTrace();
                        // Puedes decidir si quieres continuar creando el producto con valores por defecto
                        // o saltarte este producto si la información es esencial.
                    }

                    miProducto = new Producto(
                            value.getString("idProducto"),
                            row.getString("id"),
                            value.getString("_rev"),
                            "sincronizado",
                            value.getString("codigo"),
                            value.getString("descripcion"),
                            value.getString("marca"),
                            value.getString("presentacion"),
                            value.getString("precio"),
                            value.getString("urlFoto"),
                            value.getDouble("costo"),  // Incluye costo
                            value.getInt("stock")      // Incluye stock
                    );

                    alProductos.add(miProducto);

                    // Guardar en SQLite (Asegúrate de que tu método administrar_productos acepte costo y stock)
                    String[] datos = {
                            miProducto.getIdProducto(),
                            miProducto.get_id(),
                            miProducto.get_rev(),
                            miProducto.getSync_status(),
                            miProducto.getCodigo(),
                            miProducto.getDescripcion(),
                            miProducto.getMarca(),
                            miProducto.getPresentacion(),
                            miProducto.getPrecio(),
                            miProducto.getFoto(),
                            String.valueOf(costo), // Convirtiendo a String para SQLite
                            String.valueOf(stock)  // Convirtiendo a String para SQLite
                    };
                    db.administrar_productos("nuevo", datos);
                }
                alProductosCopia.addAll(alProductos);
                ltsProductos.setAdapter(new AdaptadorProductos(this, alProductos));
                registerForContextMenu(ltsProductos);
            } else {
                mostrarMsg("No hay productos registrados");
                abrirVentana();
            }
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void buscarProductos() {
        TextView tempVal = findViewById(R.id.txtBuscarProductos);
        tempVal.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                alProductos.clear();
                String buscar = tempVal.getText().toString().trim().toLowerCase();
                if (buscar.isEmpty()) {
                    alProductos.addAll(alProductosCopia);
                } else {
                    for (Producto item : alProductosCopia) {
                        if (item.getCodigo().toLowerCase().contains(buscar) ||
                                item.getDescripcion().toLowerCase().contains(buscar) ||
                                item.getMarca().toLowerCase().contains(buscar)) {
                            alProductos.add(item);
                        }
                    }
                }
                ltsProductos.setAdapter(new AdaptadorProductos(getApplicationContext(), alProductos));
            }
        });
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}