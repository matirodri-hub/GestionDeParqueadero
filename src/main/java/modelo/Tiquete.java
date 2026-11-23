package modelo;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public class Tiquete implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numero;
    private Vehiculo vehiculo;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private Convenio convenio;
    private long valorParqueo;
    private long valorRecarga;

    public Tiquete(int numero, Vehiculo vehiculo, LocalDateTime entrada) {
        this.numero = numero;
        this.vehiculo = vehiculo;
        this.entrada = entrada;
        this.salida = null;
        this.convenio = Convenio.NINGUNO;
        this.valorParqueo = 0;
        this.valorRecarga = 0;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalDateTime entrada) {
        this.entrada = entrada;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public void setSalida(LocalDateTime salida) {
        this.salida = salida;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public long getValorParqueo() {
        return valorParqueo;
    }

    public void setValorParqueo(long valorParqueo) {
        this.valorParqueo = valorParqueo;
    }

    public long getValorRecarga() {
        return valorRecarga;
    }

    public void setValorRecarga(long valorRecarga) {
        this.valorRecarga = valorRecarga;
    }

    public long getTotal(){
        return valorParqueo + valorRecarga;
    }

    public boolean estaAbierto(){
        return salida == null;
    }

    public long getMinutos(){
        if(salida == null){
            return 0;
        }
        return Duration.between(entrada, salida).toMinutes();
    }

    public void cerrar(LocalDateTime salida, Convenio convenio, boolean tieneMensualidad){
        this.salida = salida;
        this.convenio = convenio;

        long minutos = getMinutos();
        long cobrables = Math.max(0, minutos - convenio.getMinutosGratis());

        if (tieneMensualidad) {
            this.valorParqueo = 0;
        } else {
            this.valorParqueo = vehiculo.calcularTarifa(cobrables);
        }
        this.valorRecarga = vehiculo.cargoAdicional(minutos);
    }

    @Override
    public String toString() {
        String texto = "Numero: " + numero +
                ", " + vehiculo +
                ", Entrada: " + entrada;

        if (salida == null) {
            texto += ", Estado: abierto";
        } else {
            texto += ", Salida: " + salida +
                    ", Total: " + getTotal();
        }

        return texto;
    }
}
