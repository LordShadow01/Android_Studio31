package com.ugb.miprimeraaplicacion;

import java.util.Base64;

public class utilidades {
    static String url_consulta = "http://192.168.82.233:5984/jona/_design/jona/_view/jona";
    static String url_mto = "http://192.168.82.233:5984/jona";
    static String user = "admin";
    static String passwd = "admin";
    static String credencialesCodificadas = Base64.getEncoder().encodeToString((user + ":" + passwd).getBytes());

    public String generarUnicoId() {
        return java.util.UUID.randomUUID().toString();
    }
}