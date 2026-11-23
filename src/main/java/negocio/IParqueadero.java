package negocio;

import excepciones.FechaInvalidaException;
import excepciones.PlacaInvalidaException;
import excepciones.VehiculoNoEncontradoException;
import excepciones.VehiculoYaAdentroException;
import modelo.Convenio;
import modelo.Mensualidad;
import modelo.Tiquete;
import modelo.Vehiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IParqueadero {

    void agregarMensualidad(Mensualidad m) throws FechaInvalidaException;

    Tiquete registrarEntrada(Vehiculo v, LocalDateTime entrada) throws PlacaInvalidaException, VehiculoYaAdentroException;

    Tiquete registrarSalida(String placa, LocalDateTime salida, Convenio convenio) throws VehiculoNoEncontradoException, FechaInvalidaException, VehiculoNoEncontradoException;

    List<String> reporteVehiculosAdentro();

    List<String> reporteCierre(LocalDate fecha);

    List<String> reportePlacasFrecuentes();

    List<Tiquete> getTiquetes();
}
