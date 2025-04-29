package com.ugb.miprimeraaplicacion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class AdaptadorProductos extends BaseAdapter {
    Context context;
    ArrayList<Producto> alProductos;
    Producto miProducto;
    LayoutInflater inflater;

    public AdaptadorProductos(Context context, ArrayList<Producto> alProductos) {
        this.context = context;
        this.alProductos = alProductos;
    }

    @Override public int getCount() { return alProductos.size(); }
    @Override public Object getItem(int position) { return alProductos.get(position); }
    @Override public long getItemId(int position) { return 0; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.fotos, parent, false);
        try {
            miProducto = alProductos.get(position);

            TextView tempVal = itemView.findViewById(R.id.lblCodigoAdaptador);
            tempVal.setText(miProducto.getCodigo());

            tempVal = itemView.findViewById(R.id.lblDescripcionAdaptador);
            tempVal.setText(miProducto.getDescripcion());

            tempVal = itemView.findViewById(R.id.lblPrecioAdaptador);
            tempVal.setText("$"+miProducto.getPrecio());

            // Agregando la visualización del costo
            tempVal = itemView.findViewById(R.id.lblCostoAdaptador);
            tempVal.setText("Costo: $"+miProducto.getCosto());

            // Agregando la visualización del stock
            tempVal = itemView.findViewById(R.id.lblStockAdaptador);
            tempVal.setText("Stock: "+miProducto.getStock());

            ImageView img = itemView.findViewById(R.id.imgFotoAdaptador);
            Bitmap bitmap = BitmapFactory.decodeFile(miProducto.getFoto());
            img.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return itemView;
    }
}