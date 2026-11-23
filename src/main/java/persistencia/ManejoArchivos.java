package persistencia;

import modelo.*;
import excepciones.ParqueaderoException;
import negocio.IParqueadero;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class ManejoArchivos {

    public static List<String> cargarMensualidades(String ruta, IParqueadero sistema) {
        List<String> errores = new ArrayList<>();

        List<String> lineas;
        try {
            lineas = leerLineas(ruta);
        } catch (IOException e) {
            errores.add("No se pudo leer el archivo " + ruta + ": " + e.getMessage());
            return errores;
        }

        for (int i = 0; i < lineas.size(); i++) {
            int numeroLinea = i + 1;
            String linea = lineas.get(i).trim();

            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }

            try {
                String[] campos = linea.split("\\*", -1);
                if (campos.length != 3) {
                    throw new IllegalArgumentException("Faltan o sobran campos: una mensualidad lleva tres");
                }

                LocalDate inicio = leerFecha(campos[1]);
                LocalDate fin = leerFecha(campos[2]);
                sistema.agregarMensualidad(new Mensualidad(campos[0], inicio, fin));
            } catch (ParqueaderoException | IllegalArgumentException e) {
                errores.add("Linea " + numeroLinea + ": " + e.getMessage());
            }
        }

        return errores;
    }

    public static List<String> procesarMovimientos(String ruta, IParqueadero sistema) {
        List<String> errores = new ArrayList<>();

        List<String> lineas;
        try {
            lineas = leerLineas(ruta);
        } catch (IOException e) {
            errores.add("No se pudo leer el archivo " + ruta + ": " + e.getMessage());
            return errores;
        }

        for (int i = 0; i < lineas.size(); i++) {
            int numeroLinea = i + 1;
            String linea = lineas.get(i).trim();

            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }

            try {
                aplicarMovimiento(linea, sistema);
            } catch (ParqueaderoException | IllegalArgumentException e) {
                errores.add("Linea " + numeroLinea + ": " + e.getMessage());
            }
        }

        return errores;
    }

    public static void exportarReporte(List<String> lineas, String ruta) throws IOException {
        try (PrintWriter escritor = new PrintWriter(new FileWriter(ruta))) {
            for (String linea : lineas) {
                escritor.println(linea);
            }
        }
    }

    public static void guardarSistema(IParqueadero sistema, String ruta) throws IOException {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ruta))) {
            salida.writeObject(sistema);
        }
    }

    public static IParqueadero cargarSistema(String ruta) throws IOException {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ruta))) {
            return (IParqueadero) entrada.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("El archivo no contiene un sistema valido: " + e.getMessage());
        }
    }

    public static Vehiculo crearVehiculo(String tipo, String placa) {
        switch (tipo) {
            case "CARRO":
                return new Carro(placa);
            case "MOTO":
                return new Moto(placa);
            case "ELECTRICO":
                return new CarroElectrico(placa);
            default:
                throw new IllegalArgumentException("Tipo de vehiculo desconocido: " + tipo);
        }
    }

    private static void aplicarMovimiento(String linea, IParqueadero sistema)
            throws ParqueaderoException {
        String[] campos = linea.split("\\*", -1);

        switch (campos[0]) {
            case "E": {
                if (campos.length != 4) {
                    throw new IllegalArgumentException("Faltan campos: una entrada lleva cuatro");
                }
                Vehiculo vehiculo = crearVehiculo(campos[2], campos[1]);
                LocalDateTime entrada = leerFechaHora(campos[3]);
                sistema.registrarEntrada(vehiculo, entrada);
                break;
            }
            case "S": {
                if (campos.length != 4) {
                    throw new IllegalArgumentException("Faltan campos: una salida lleva cuatro");
                }
                LocalDateTime salida = leerFechaHora(campos[2]);
                Convenio convenio = leerConvenio(campos[3]);
                sistema.registrarSalida(campos[1], salida, convenio);
                break;
            }
            default:
                throw new IllegalArgumentException("Movimiento desconocido: " + campos[0] + ", solo hay E y S");
        }
    }

    private static List<String> leerLineas(String ruta) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }

    public static LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha mal escrita o imposible: " + texto);
        }
    }

    public static LocalDateTime leerFechaHora(String texto) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);
        try {
            return LocalDateTime.parse(texto, formato);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha y hora mal escritas o imposibles: " + texto);
        }
    }

    public static Convenio leerConvenio(String texto) {
        try {
            return Convenio.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Convenio desconocido: " + texto);
        }
    }


}