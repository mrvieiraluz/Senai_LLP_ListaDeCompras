package com.gestaocompras;

import com.gestaocompras.ui.ConsoleUI;

/**
 * Ponto de entrada do sistema de Gestão de Compras.
 */
public class Main {

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.iniciar();
    }
}
