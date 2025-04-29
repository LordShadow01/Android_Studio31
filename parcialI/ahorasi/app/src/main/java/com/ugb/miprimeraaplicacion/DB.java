package com.ugb.miprimeraaplicacion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Importa la clase Log

public class DB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tienda";
    private static final int DATABASE_VERSION = 3; // Incrementa la versión a 3
    private static final String SQLdb = "CREATE TABLE productos (" +
            "idProducto TEXT, _id TEXT, _rev TEXT, sync_status TEXT DEFAULT 'pendiente', " +
            "codigo TEXT, descripcion TEXT, marca TEXT, presentacion TEXT, precio TEXT, urlFoto TEXT, " +
            "costo DOUBLE, stock INT)"; // Agrega costo y stock con tipos de datos adecuados

    public DB(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) { db.execSQL(SQLdb); }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE productos ADD COLUMN _id TEXT");
            db.execSQL("ALTER TABLE productos ADD COLUMN _rev TEXT");
            db.execSQL("ALTER TABLE productos ADD COLUMN sync_status TEXT DEFAULT 'pendiente'");
        }
        if (oldVersion < 3) { // Agrega esta condición para la nueva versión
            db.execSQL("ALTER TABLE productos ADD COLUMN costo REAL");
            db.execSQL("ALTER TABLE productos ADD COLUMN stock INTEGER");
        }
    }

    public String administrar_productos(String accion, String[] datos) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            String sql = "";
            switch (accion) {
                case "nuevo":
                    // Verificar si existe el registro
                    Cursor cursor = db.rawQuery("SELECT idProducto FROM productos WHERE idProducto = ?",
                            new String[]{datos[0]});
                    if (cursor.getCount() > 0) {
                        sql = "UPDATE productos SET _id = '" + datos[1] + "', _rev = '" + datos[2] +
                                "', sync_status = '" + datos[3] + "', codigo = '" + datos[4] +
                                "', descripcion = '" + datos[5] + "', marca = '" + datos[6] +
                                "', presentacion = '" + datos[7] + "', precio = '" + datos[8] +
                                "', urlFoto = '" + datos[9] + "', costo = '" + datos[10] + "', stock = '" + datos[11] +
                                "' WHERE idProducto = '" + datos[0] + "'";
                    } else {
                        sql = "INSERT INTO productos (idProducto, _id, _rev, sync_status, codigo, descripcion, " +
                                "marca, presentacion, precio, urlFoto, costo, stock) VALUES ('" + datos[0] + "','" + datos[1] +
                                "','" + datos[2] + "','" + datos[3] + "','" + datos[4] + "','" + datos[5] +
                                "','" + datos[6] + "','" + datos[7] + "','" + datos[8] + "','" + datos[9] + "','" + datos[10] + "','" + datos[11] + "')";
                    }
                    cursor.close();
                    break;
                case "modificar":
                    sql = "UPDATE productos SET " +
                            "_id = '" + datos[1] + "', " +
                            "_rev = '" + datos[2] + "', " +
                            "sync_status = '" + datos[3] + "', " +
                            "codigo = '" + datos[4] + "', " +
                            "descripcion = '" + datos[5] + "', " +
                            "marca = '" + datos[6] + "', " +
                            "presentacion = '" + datos[7] + "', " +
                            "precio = '" + datos[8] + "', " +
                            "urlFoto = '" + datos[9] + "', " +
                            "costo = '" + datos[10] + "', " +
                            "stock = '" + datos[11] + "' " +
                            "WHERE idProducto = '" + datos[0] + "'";
                    break;
                case "eliminar":
                    sql = "UPDATE productos SET sync_status='eliminado' WHERE idProducto='" + datos[0] + "'";
                    break;
            }
            db.execSQL(sql);
            db.close();
            return "ok";
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error en administrar_productos: " + e.getMessage()); // Log del error
            return e.getMessage();
        }
    }

    public Cursor lista_productos() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM productos", null);
    }
}