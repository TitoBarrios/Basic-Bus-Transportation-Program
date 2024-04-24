package com.titobarrios.view.company;

import com.titobarrios.controller.AccountsCtrl;
import com.titobarrios.error.ElementNotFoundException;
import com.titobarrios.error.InvalidCredentialsException;
import com.titobarrios.model.Account;
import com.titobarrios.model.Company;
import com.titobarrios.model.User;
import com.titobarrios.services.LogIn;
import com.titobarrios.view.Console;
import com.titobarrios.view.home.CompanyMenu;
import com.titobarrios.view.home.Home;

public class LogInMenu {
    public LogInMenu() {
        menu();
    }

    private void menu() {
        Console.log("Ingrese su id\n0. Volver");
        String id = Console.readData();
        if (id.equals("0"))
            new CompanyMenu();
        Console.log("Ingrese su contraseña");
        String password = Console.readData();

        boolean logIn = false;
        try {
            logIn = LogIn.logIn(id, password);
        } catch (ElementNotFoundException e) {
            Console.log("El id no existe");
            menu();
        } catch (InvalidCredentialsException e) {
            Console.log("Contraseña incorrecta");
            menu();
        }

        if (logIn) {
            Account account = AccountsCtrl.searchAccount(id);
            if (account instanceof User) {
                Console.log("Opción no disponible para clientes");
                new Home();
            }
            new MainMenu((Company) account);
        }
        new CompanyMenu();
    }
}