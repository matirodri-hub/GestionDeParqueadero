package modelo;

public class Carro extends Vehiculo {

    private static final long TARIFA_MINUTO = 120;

    public Carro(String placa) {
        super(placa);
    }

    @Override
    public String getTipo() {
        return "CARRO";
    }

    @Override
    public boolean placaValida() {
        return getPlaca().matches("[A-Z]{3}[0-9]{3}");
    }

    @Override
    public long calcularTarifa(long minutos) {
        return minutos * TARIFA_MINUTO;
    }
}
