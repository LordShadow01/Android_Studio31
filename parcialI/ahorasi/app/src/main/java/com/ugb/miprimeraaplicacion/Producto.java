package com.ugb.miprimeraaplicacion;

public class Producto {
    private String idProducto;
    private String _id;
    private String _rev;
    private String sync_status;
    private String codigo;
    private String descripcion;
    private String marca;
    private String presentacion;
    private String precio;
    private String foto;
    private double costo;  // Added
    private int stock;     // Added

    // Constructor para CouchDB
    public Producto(String idProducto, String _id, String _rev, String sync_status,
                    String codigo, String descripcion, String marca,
                    String presentacion, String precio, String foto,
                    double costo, int stock) {  // Modified
        this.idProducto = idProducto;
        this._id = _id;
        this._rev = _rev;
        this.sync_status = sync_status;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precio = precio;
        this.foto = foto;
        this.costo = costo;
        this.stock = stock;
    }

    // Constructor para SQLite (compatibilidad)
    public Producto(String idProducto, String codigo, String descripcion,
                    String marca, String presentacion, String precio, String foto,
                    double costo, int stock) {  // Modified
        this(idProducto, "", "", "sincronizado", codigo, descripcion,
                marca, presentacion, precio, foto, costo, stock);
    }

    // Getters y Setters
    public String getIdProducto() { return idProducto; }
    public String get_id() { return _id; }
    public String get_rev() { return _rev; }
    public String getSync_status() { return sync_status; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public String getMarca() { return marca; }
    public String getPresentacion() { return presentacion; }
    public String getPrecio() { return precio; }
    public String getFoto() { return foto; }
    public double getCosto() { return costo; }  // Added
    public int getStock() { return stock; }     // Added

    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public void set_id(String _id) { this._id = _id; }
    public void set_rev(String _rev) { this._rev = _rev; }
    public void setSync_status(String sync_status) { this.sync_status = sync_status; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public void setPrecio(String precio) { this.precio = precio; }
    public void setFoto(String foto) { this.foto = foto; }
    public void setCosto(double costo) { this.costo = costo; }  // Added
    public void setStock(int stock) { this.stock = stock; }     // Added
}