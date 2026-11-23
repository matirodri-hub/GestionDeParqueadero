package presentacion;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import excepciones.ParqueaderoException;
import modelo.Convenio;
import modelo.Tiquete;
import modelo.Vehiculo;
import negocio.IParqueadero;
import negocio.Parqueadero;
import persistencia.ManejoArchivos;

public class Consola {

    private static IParqueadero sistema;
    private static Scanner lector;

    public static void main(String[] args) {
        sistema = new Parqueadero();
        lector = new Scanner(System.in);

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerOpcion();
            ejecutar(opcion);
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== PARQUEADERO PLAZA CENTRAL =====");
        System.out.println("1. Cargar mensualidades");
        System.out.println("2. Procesar movimientos");
        System.out.println("3. Registrar una entrada");
        System.out.println("4. Registrar una salida");
        System.out.println("5. Ver los vehiculos adentro");
        System.out.println("6. Cierre de caja de un dia");
        System.out.println("7. Placas frecuentes");
        System.out.println("8. Guardar el sistema");
        System.out.println("9. Cargar el sistema");
        System.out.println("0. Salir");
    }

    private static int leerOpcion() {
        String texto = pedir("Opcion: ");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void ejecutar(int opcion) {
        try {
            switch (opcion) {
                case 1:
                    cargarMensualidades();
                    break;
                case 2:
                    procesarMovimientos();
                    break;
                case 3:
                    registrarEntrada();
                    break;
                case 4:
                    registrarSalida();
                    break;
                case 5:
                    mostrarLista("Vehiculos adentro", sistema.reporteVehiculosAdentro(),
                            "No hay vehiculos adentro");
                    break;
                case 6:
                    cierreDeCaja();
                    break;
                case 7:
                    mostrarLista("Placas frecuentes", sistema.reportePlacasFrecuentes(),
                            "Todavia no hay tiquetes cerrados");
                    break;
                case 8:
                    guardarSistema();
                    break;
                case 9:
                    cargarSistema();
                    break;
                case 0:
                    System.out.println("Hasta luego");
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        } catch (ParqueaderoException | IllegalArgumentException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void cargarMensualidades() {
        String carpeta = pedir("Carpeta de los archivos: ");
        String ruta = new File(carpeta, "Mensualidades.txt").getPath();
        List<String> mensajes = ManejoArchivos.cargarMensualidades(ruta, sistema);
        mostrarLista("Mensajes de la carga", mensajes, "Carga sin errores");
    }

    private static void procesarMovimientos() {
        String carpeta = pedir("Carpeta de los archivos: ");
        String ruta = new File(carpeta, "Movimientos.txt").getPath();
        List<String> mensajes = ManejoArchivos.procesarMovimientos(ruta, sistema);
        mostrarLista("Mensajes de la carga", mensajes, "Carga sin errores");
    }

    private static void registrarEntrada() throws ParqueaderoException {
        String placa = pedir("Placa: ");
        String tipo = pedir("Tipo (CARRO, MOTO, ELECTRICO): ").toUpperCase();
        LocalDateTime entrada = ManejoArchivos.leerFechaHora(pedir("Fecha y hora (yyyy-MM-dd HH:mm): "));

        Vehiculo vehiculo = ManejoArchivos.crearVehiculo(tipo, placa);
        Tiquete tiquete = sistema.registrarEntrada(vehiculo, entrada);
        System.out.println(tiquete);
    }

    private static void registrarSalida() throws ParqueaderoException {
        String placa = pedir("Placa: ");
        LocalDateTime salida = ManejoArchivos.leerFechaHora(pedir("Fecha y hora (yyyy-MM-dd HH:mm): "));

        String textoConvenio = pedir("Convenio (NINGUNO, ALMACEN, RESTAURANTE, CINE) [Enter = NINGUNO]: ")
                .toUpperCase();
        if (textoConvenio.isEmpty()) {
            textoConvenio = "NINGUNO";
        }
        Convenio convenio = ManejoArchivos.leerConvenio(textoConvenio);

        Tiquete tiquete = sistema.registrarSalida(placa, salida, convenio);
        System.out.println(tiquete);
        System.out.println("Minutos: " + tiquete.getMinutos() +
                ", Parqueo: " + tiquete.getValorParqueo() +
                ", Recarga: " + tiquete.getValorRecarga() +
                ", Total: " + tiquete.getTotal());
    }

    private static void cierreDeCaja() throws IOException {
        LocalDate fecha = ManejoArchivos.leerFecha(pedir("Fecha (yyyy-MM-dd): "));

        List<String> cierre = sistema.reporteCierre(fecha);
        mostrarLista("Cierre de caja", cierre, "");

        String ruta = "Cierre_" + fecha + ".txt";
        ManejoArchivos.exportarReporte(cierre, ruta);
        System.out.println("Cierre exportado a " + ruta);
    }

    private static void guardarSistema() throws IOException {
        ManejoArchivos.guardarSistema(sistema, "sistema.dat");
        System.out.println("Sistema guardado en sistema.dat");
    }

    private static void cargarSistema() throws IOException {
        sistema = ManejoArchivos.cargarSistema("sistema.dat");
        System.out.println("Sistema cargado desde sistema.dat");
        System.out.println("Sistema cargado desde sistema.dat");
        System.out.println("Sistema cargado desde sistema.dat");
    }

    private static String pedir(String mensaje) {
        System.out.print(mensaje);
        return lector.nextLine().trim();
    }

    private static void mostrarLista(String titulo, List<String> lineas, String mensajeVacio) {
        System.out.println("--- " + titulo + " ---");
        if (lineas.isEmpty()) {
            System.out.println(mensajeVacio);
        } else {
            for (String linea : lineas) {
                System.out.println(linea);
            }
        }
    }
}