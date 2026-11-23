package modelo;

import java.io.Serializable;

public abstract class Vehiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String placa;

    public Vehiculo(String placa) {
        this.placa = placa;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public abstract String getTipo();

    public abstract boolean placaValida();

    public abstract long calcularTarifa(long minutos);

    public long cargoAdicional(long minutos) {
        return 0;
    }

    @Override
    public String toString() {
        return "Tipo: " + getTipo() +
                ", Placa: " + placa;
    }
}
