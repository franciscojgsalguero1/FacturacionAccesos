package org.fbmoll.billing;

import org.fbmoll.billing.resources.View;

public class Main {
    public static void main(String[] args) {
        View view = new View();
        view.setVisible(true);

        // TODO Questions:
        // 1. Que hay que poner en Fichas->Comerciales?? Fichas->Distribuidores?? Fichas->Trabajadores??
        // 2. Rectificativas->Ver Rectificativa o lista de Rectificativas??
        // 3. Diferencia entre Fichas->Clientes y Listados->Clientes
        // 4. Que hay que tener en Configuracion->Datos de Empresa??
        // 5. Que hay que tener en Ayuda->Manual de usuario y Ayuda->Acerca de??

        // TODO: Logging: Instead of printing errors to the console (System.out.println), you might want
        //  to use a logging framework such as SLF4J or Log4j for better error tracking and reporting.
        // TODO: Clicking a column sorts by that column
    }
}