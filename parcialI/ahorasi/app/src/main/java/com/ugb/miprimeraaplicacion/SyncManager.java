package com.ugb.miprimeraaplicacion;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;

public class SyncManager {
    private final DB db;
    private final Context context;

    public SyncManager(Context context) {
        this.context = context;
        this.db = new DB(context);
    }

    private String getSafeString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index != -1 ? cursor.getString(index) : "";
    }

    private double getSafeDouble(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index != -1 ? cursor.getDouble(index) : 0.0; // Default to 0.0
    }

    private int getSafeInt(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index != -1 ? cursor.getInt(index) : 0; // Default to 0
    }

    public void sincronizar() {
        if (!new detectarInternet(context).hayConexionInternet()) return;

        sincronizarHaciaCouchDB();
        sincronizarDesdeCouchDB();
    }

    private void sincronizarHaciaCouchDB() {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT * FROM productos WHERE sync_status = 'pendiente' OR sync_status = 'eliminado'", null);

        try {
            while (cursor.moveToNext()) {
                String idLocal = getSafeString(cursor, "idProducto");
                String syncStatus = getSafeString(cursor, "sync_status");
                String accion = syncStatus.equals("eliminado") ? "eliminar" : "nuevo";

                String _id = getSafeString(cursor, "_id");
                String _rev = getSafeString(cursor, "_rev");

                enviarDatosServidor enviar = new enviarDatosServidor(context);
                if (accion.equals("eliminar")) {
                    if (!_id.isEmpty() && !_rev.isEmpty()) {
                        String url = utilidades.url_mto + "/" + _id + "?rev=" + _rev;
                        enviar.execute("", "DELETE", url).get();
                    }
                } else {
                    JSONObject datos = new JSONObject();
                    datos.put("idProducto", idLocal);
                    datos.put("codigo", getSafeString(cursor, "codigo"));
                    datos.put("descripcion", getSafeString(cursor, "descripcion"));
                    datos.put("marca", getSafeString(cursor, "marca"));
                    datos.put("presentacion", getSafeString(cursor, "presentacion"));
                    datos.put("precio", getSafeString(cursor, "precio"));
                    datos.put("urlFoto", getSafeString(cursor, "urlFoto"));
                    datos.put("costo", getSafeDouble(cursor, "costo"));  // Added
                    datos.put("stock", getSafeInt(cursor, "stock"));     // Added

                    enviar.execute(datos.toString(), "POST", utilidades.url_mto).get();
                }

                db.getWritableDatabase().execSQL(
                        "UPDATE productos SET sync_status = 'sincronizado' WHERE idProducto = '" + idLocal + "'");
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error sincronizando: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            cursor.close();
        }
    }

    private void sincronizarDesdeCouchDB() {
        try {
            obtenerDatosServidor obtener = new obtenerDatosServidor();
            String respuesta = obtener.execute().get();
            JSONObject jsonRespuesta = new JSONObject(respuesta);
            JSONArray rows = jsonRespuesta.getJSONArray("rows");

            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                JSONObject producto = row.getJSONObject("value");

                // Verificar si ya existe en SQLite
                Cursor cursor = db.getReadableDatabase().rawQuery(
                        "SELECT idProducto FROM productos WHERE idProducto = ?",
                        new String[]{producto.getString("idProducto")}
                );

                String accion = cursor.getCount() > 0 ? "modificar" : "nuevo";
                cursor.close();

                // Construir array de datos
                String[] datos = {
                        producto.getString("idProducto"),
                        row.getString("id"),
                        producto.getString("_rev"),
                        "sincronizado",
                        producto.getString("codigo"),
                        producto.getString("descripcion"),
                        producto.getString("marca"),
                        producto.getString("presentacion"),
                        producto.getString("precio"),
                        producto.getString("urlFoto"),
                        String.valueOf(producto.getDouble("costo")),  // Incluye costo
                        String.valueOf(producto.getInt("stock"))      // Incluye stock
                };

                // Ejecutar operación en SQLite
                String resultado = db.administrar_productos(accion, datos);

                if (!resultado.equals("ok")) {
                    Toast.makeText(context, "Error sincronizando producto: " + producto.getString("idProducto"),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error en sincronización desde CouchDB: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}