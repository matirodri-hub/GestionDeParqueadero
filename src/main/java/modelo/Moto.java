package modelo;

public class Moto extends Vehiculo {

    private static final long TARIFA_MINUTO = 80;

    public Moto(String placa) {
        super(placa);
    }

    @Override
    public String getTipo() {
        return "MOTO";
    }

    @Override
    public boolean placaValida() {
        return getPlaca().matches("[A-Z]{3}[0-9]{2}[A-Z]");
    }

    @Override
    public long calcularTarifa(long minutos) {
        return minutos * TARIFA_MINUTO;
    }
}
