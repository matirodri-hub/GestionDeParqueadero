package negocio;

import excepciones.FechaInvalidaException;
import excepciones.PlacaInvalidaException;
import excepciones.VehiculoNoEncontradoException;
import excepciones.VehiculoYaAdentroException;
import modelo.Convenio;
import modelo.Mensualidad;
import modelo.Tiquete;
import modelo.Vehiculo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Parqueadero implements IParqueadero, Serializable {

    private static final long serialVersionUID = 1L;

    private int siguienteNumero = 1;
    private List<Tiquete> tiquetes;
    private List<Mensualidad> mensualidades;

    public Parqueadero() {
        tiquetes = new ArrayList<>();
        mensualidades = new ArrayList<>();
    }

    public int getSiguienteNumero() {
        return siguienteNumero;
    }

    public void setSiguienteNumero(int siguienteNumero) {
        this.siguienteNumero = siguienteNumero;
    }

    @Override
    public List<Tiquete> getTiquetes() {
        return tiquetes;
    }

    public void setTiquetes(List<Tiquete> tiquetes) {
        this.tiquetes = tiquetes;
    }

    public List<Mensualidad> getMensualidades() {
        return mensualidades;
    }

    public void setMensualidades(List<Mensualidad> mensualidades) {
        this.mensualidades = mensualidades;
    }

    public void agregarMensualidad(Mensualidad m) throws FechaInvalidaException {
        if (m.getFin().isBefore(m.getInicio())) {
            throw  new FechaInvalidaException("La mensualidad de " + m.getPlaca() + " termina el " + m.getFin() + ", antes de su inicio el " + m.getInicio());
        }
        mensualidades.add(m);
    }

    @Override
    public Tiquete registrarEntrada(Vehiculo v, LocalDateTime entrada) throws PlacaInvalidaException, VehiculoYaAdentroException {
        if (!v.placaValida()){
            throw new PlacaInvalidaException("La placa " + v.getPlaca() + " no es valida para un vehiculo " + v.getTipo());
        };
        if (buscarAbierto(v.getPlaca())!=null){
            throw new VehiculoYaAdentroException("El vehiculo " + v.getPlaca() + " ya esta dentro del parqueadero");
        }

        Tiquete tiquete = new Tiquete(siguienteNumero, v , entrada);
        siguienteNumero++;
        tiquetes.add(tiquete);
        return tiquete;
    }

    public Tiquete registrarSalida(String placa, LocalDateTime salida, Convenio convenio) throws FechaInvalidaException, VehiculoNoEncontradoException {
        Tiquete tiquete = buscarAbierto(placa);
        if (tiquete == null) {
            throw new VehiculoNoEncontradoException("No hay un tiquete abierto con la placa " + placa);
        }
        if (salida.isBefore(tiquete.getEntrada())) {
            throw new FechaInvalidaException("La salida de " +  placa + " (" + salida + ") es anterior a su entrada (" + tiquete.getEntrada() + ")");
        }

        boolean tieneMensualidad = tieneMensualidad(placa, tiquete.getEntrada().toLocalDate());
        tiquete.cerrar(salida, convenio, tieneMensualidad);
        return tiquete;
    }
    @Override
    public List<String> reporteVehiculosAdentro() {
        List<Tiquete> abiertos = new ArrayList<>();
        for (Tiquete t  : tiquetes) {
            if (t.estaAbierto()){
                abiertos.add(t);
            }
        }
        abiertos.sort(Comparator.comparing(Tiquete::getEntrada).thenComparing(t -> t.getVehiculo().getPlaca()));

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List <String> lineas = new ArrayList<>();
        for (Tiquete t: abiertos) {
            lineas.add(t.getNumero() + " " + t.getVehiculo().getPlaca() + " " + t.getVehiculo().getTipo() + " " + t.getEntrada().format(formato));
        }
        return lineas;

    }

    @Override
    public List<String> reporteCierre(LocalDate fecha) {
        String[] tipos = {"CARRO", "MOTO", "ELECTRICO"};
        int[] cantidadPorTipo = new int[tipos.length];
        long[] totalPorTipo = new long[tipos.length];

        int cantidad = 0;
        long parqueo = 0;
        long recarga = 0;

        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto() && t.getSalida().toLocalDate().equals(fecha)) {
                cantidad++;
                parqueo += t.getValorParqueo();
                recarga += t.getValorRecarga();

                for (int i = 0; i < tipos.length; i++) {
                    if (t.getVehiculo().getTipo().equals(tipos[i])) {
                        cantidadPorTipo[i]++;
                        totalPorTipo[i] += t.getTotal();
                    }
                }
            }
        }

        List<String> lineas = new ArrayList<>();
        lineas.add("CIERRE " + fecha);
        lineas.add("tiquetes=" + cantidad);
        lineas.add("parqueo=" + parqueo);
        lineas.add("recarga=" + recarga);
        lineas.add("total=" + (parqueo + recarga));
        for (int i = 0; i < tipos.length; i++) {
            lineas.add(tipos[i] + " tiquetes=" + cantidadPorTipo[i] + " total=" + totalPorTipo[i]);
        }
        return lineas;
    }

    @Override
    public List<String> reportePlacasFrecuentes() {
        List<String> placas = new ArrayList<>();
        for (Tiquete t : tiquetes) {
            String placa = t.getVehiculo().getPlaca();
            if (!t.estaAbierto() && !placas.contains(placa)) {
                placas.add(placa);
            }
        }

        placas.sort((a, b) -> {
            int porVisitas = Integer.compare(contarVisitas(b), contarVisitas(a));
            if (porVisitas != 0) {
                return porVisitas;
            }
            int porPagado = Long.compare(sumarPagado(b), sumarPagado(a));
            if (porPagado != 0) {
                return porPagado;
            }
            return a.compareTo(b);
        });

        List<String> lineas = new ArrayList<>();
        for (int i = 0; i < placas.size() && i < 3; i++) {
            String placa = placas.get(i);
            lineas.add((i + 1) + ". " + placa + " visitas=" + contarVisitas(placa) +
                    " pagado=" + sumarPagado(placa));
        }
        return lineas;
    }

    private Tiquete buscarAbierto(String placa) {
        for (Tiquete t : tiquetes) {
            if (t.estaAbierto() && t.getVehiculo().getPlaca().equals(placa)) {
                return t;
            }
        }
        return null;
    }

    private boolean tieneMensualidad(String placa, LocalDate fecha){
        for (Mensualidad m : mensualidades) {
            if (m.getPlaca().equals(placa) && m.cubre(fecha)) {
                return true;
            }
        }
        return false;
    }
    private int contarVisitas(String placa) {
        int visitas = 0;
        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto() && t.getVehiculo().getPlaca().equals(placa)) {
                visitas++;
            }
        }
        return visitas;
    }

    private long sumarPagado(String placa) {
        long pagado = 0;
        for (Tiquete t : tiquetes) {
            if (!t.estaAbierto() && t.getVehiculo().getPlaca().equals(placa)) {
                pagado += t.getTotal();
            }
        }
        return pagado;
    }
}
