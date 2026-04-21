module ControleDeLoja {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;

    exports loja.app;
    exports loja.controller;
    exports loja.service;
    exports loja.model.dto;
    exports loja.model.entity;
    exports loja.repository;

    opens loja.app to javafx.fxml;
    opens loja.controller to javafx.fxml;
    opens loja.model.dto to com.google.gson, javafx.base;
    opens loja.model.entity to com.google.gson, javafx.base;
    opens loja.view to javafx.fxml;


}