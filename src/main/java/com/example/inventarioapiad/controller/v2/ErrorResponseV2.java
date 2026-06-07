package com.example.inventarioapiad.controller.v2;

// Mismo formato de error que usa el resto de la API, pero como clase
// suelta para poder reutilizarla entre todos los controllers V2 sin
// tener una ErrorResponse repetida dentro de cada uno.
public class ErrorResponseV2 {

    public int codigo;
    public String mensaje;

    public ErrorResponseV2(int codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public int getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
}
