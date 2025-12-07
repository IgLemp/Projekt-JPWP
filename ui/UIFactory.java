package com.transport.ui;

import com.transport.sim.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.*;

public class UIFactory {

    public static BorderPane createMainUI(Simulator simulator) {
        BorderPane root = new BorderPane();

        // Left navigation
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(12));
        Button btnFleet = new Button("Flota");
        Button btnDrivers = new Button("Kierowcy");
        Button btnJobs = new Button("Zlecenia");
        Button btnReport = new Button("Raport");
        Button btnNext = new Button("> Następna tura");
        nav.getChildren().addAll(btnFleet, btnDrivers, btnJobs, btnReport, btnNext);

        // Log area
        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefWidth(360);
        VBox logBox = new VBox(6, new Label("Dziennik zdarzeń:"), log);
        logBox.setPadding(new Insets(12));

        // Center stack for views
        StackPane center = new StackPane();
        center.setPadding(new Insets(12));
        VBox fleetView = createFleetView(simulator, log);
        VBox driversView = createDriversView(simulator, log);
        VBox jobsView = createJobsView(simulator, log);
        VBox reportView = createReportView(simulator, log);
        center.getChildren().addAll(fleetView, driversView, jobsView, reportView);
        setVisibleOnly(fleetView, driversView, jobsView, reportView);

        // Navigation actions
        btnFleet.setOnAction(e -> setVisibleOnly(fleetView, driversView, jobsView, reportView));
        btnDrivers.setOnAction(e -> setVisibleOnly(driversView, fleetView, jobsView, reportView));
        btnJobs.setOnAction(e -> setVisibleOnly(jobsView, fleetView, driversView, reportView));
        btnReport.setOnAction(e -> setVisibleOnly(reportView, fleetView, driversView, jobsView));

        btnNext.setOnAction(e -> {
            // show loading indicator while sim runs
            ProgressIndicator pi = new ProgressIndicator();
            StageUtils.showTemporaryOverlay(center, pi);

            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // simulate a small delay for UX
                    Thread.sleep(350);
                    String out = simulator.nextTurn();
                    Platform.runLater(() -> log.appendText(out + "\n"));
                    return null;
                }

                @Override
                protected void succeeded() {
                    StageUtils.hideTemporaryOverlay(center);
                    refreshAllViews(fleetView, driversView, jobsView, reportView, simulator, log);
                }

                @Override
                protected void failed() {
                    StageUtils.hideTemporaryOverlay(center);
                    Platform.runLater(() -> log.appendText("Błąd podczas symulacji tury\n"));
                }
            };
            new Thread(t).start();
        });

        root.setLeft(nav);
        root.setCenter(center);
        root.setRight(logBox);
        return root;
    }

    private static void setVisibleOnly(VBox show, VBox... hide) {
        show.setVisible(true);
        for (VBox h : hide) h.setVisible(false);
    }

    private static void refreshAllViews(VBox fleet, VBox drivers, VBox jobs, VBox report, Simulator sim, TextArea log) {
        refreshFleetView(fleet, sim, log);
        refreshDriversView(drivers, sim, log);
        refreshJobsView(jobs, sim, log);
        refreshReportView(report, sim, log);
    }

    // -------------------------------------------------------------
    // Fleet
    // -------------------------------------------------------------
    private static VBox createFleetView(Simulator sim, TextArea log) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        refreshFleetView(box, sim, log);
        return box;
    }

    private static void refreshFleetView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(new Label("Flota pojazdów:"));

        for (Vehicle v : sim.getCompany().getVehicles()) {
            HBox row = new HBox(12);
            row.getChildren().addAll(
                new Label(v.getName()),
                new Label("Stan: " + v.getCondition()),
                new Label(String.format("Spalanie: %.2f/u/km", v.getFuelConsumptionPerKm())),
                button("Napraw (100)", () -> { v.repair(20); sim.getCompany().addCash(-100); refreshFleetView(view, sim, log); }),
                button("Sprzedaj", () -> { double got = sim.getCompany().sellVehicle(v); log.appendText(String.format("Sprzedano %s za %.2f\n", v.getName(), got)); refreshFleetView(view, sim, log); })
            );
            view.getChildren().add(row);
        }

        view.getChildren().add(new Separator());
        view.getChildren().add(new Label("Rynek pojazdów:"));
        for (VehicleOffer offer : sim.getCompany().getVehicleMarket()) {
            HBox row = new HBox(12);
            row.getChildren().addAll(
                new Label(offer.getName()),
                new Label("Stan: " + offer.getCondition()),
                new Label(String.format("Spalanie: %.2f/u/km", offer.getFuelConsumption())),
                new Label(String.format("Cena: %.0f", offer.getPrice())),
                button("Kup", () -> { if (sim.getCompany().buyOffer(offer)) { log.appendText("Kupiono: " + offer.getName() + "\n"); refreshFleetView(view, sim, log); } else log.appendText("Brak środków na zakup\n"); }),
                button("Odśwież rynek", () -> { sim.getCompany().refreshVehicleMarket(); log.appendText("Rynek odświeżony\n"); refreshFleetView(view, sim, log); })
            );
            view.getChildren().add(row);
        }
    }

    // -------------------------------------------------------------
    // Drivers + Candidate pool
    // -------------------------------------------------------------
    private static VBox createDriversView(Simulator sim, TextArea log) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        refreshDriversView(box, sim, log);
        return box;
    }

    private static void refreshDriversView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(new Label("Kierowcy:"));

        for (Driver d : sim.getCompany().getDrivers()) {
            HBox row = new HBox(12);
            String vehName = d.getAssignedVehicle() != null ? d.getAssignedVehicle().getName() : "Brak";
            row.getChildren().addAll(new Label(d.getName()), new Label("Skill: " + d.getSkill()), new Label("Pojazd: " + vehName), button("Szkol (50)", () -> { d.train(5); sim.getCompany().addCash(-50); refreshDriversView(view, sim, log); }));

            // Assign vehicle combo
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().add("Brak");
            for (Vehicle v : sim.getCompany().getVehicles()) {
                // only list vehicles not already assigned to someone else, or the currently assigned vehicle
                boolean assignedElsewhere = false;
                for (Driver other : sim.getCompany().getDrivers()) { if (other != d && other.getAssignedVehicle() == v) assignedElsewhere = true; }
                if (!assignedElsewhere || d.getAssignedVehicle() == v) combo.getItems().add(v.getName());
            }
            combo.setValue(vehName);
            Button assignBtn = button("Przypisz pojazd", () -> {
                String choice = combo.getValue();
                if (choice == null || choice.equals("Brak")) { d.setAssignedVehicle(null); log.appendText(d.getName() + " odpięto od pojazdu\n"); refreshDriversView(view, sim, log); return; }
                Vehicle sel = null;
                for (Vehicle v : sim.getCompany().getVehicles()) if (v.getName().equals(choice)) sel = v;
                if (sel != null) {
                    // unassign vehicle from other drivers
                    for (Driver other : sim.getCompany().getDrivers()) if (other != d && other.getAssignedVehicle() == sel) other.setAssignedVehicle(null);
                    d.setAssignedVehicle(sel);
                    log.appendText(d.getName() + " przypisany do " + sel.getName() + "\n");
                    refreshDriversView(view, sim, log);
                }
            });

            row.getChildren().addAll(combo, assignBtn);
            view.getChildren().add(row);
        }

        view.getChildren().add(new Separator());
        view.getChildren().add(button("Odśwież pulę kandydatów", () -> { sim.getCompany().refreshCandidatePool(); log.appendText("Pulę kandydatów odświeżono\n"); refreshDriversView(view, sim, log); }));
        view.getChildren().add(new Label("Kandydaci do zatrudnienia:"));

        for (DriverCandidate c : sim.getCompany().getCandidates()) {
            HBox row = new HBox(12);
            row.getChildren().addAll(new Label(c.getName()), new Label("Skill: " + c.getSkill()), new Label(String.format("Koszt: %.0f", c.getHireCost())), button("Zatrudnij", () -> { if (sim.getCompany().hireCandidate(c)) { log.appendText("Zatrudniono: " + c.getName() + "\n"); refreshDriversView(view, sim, log); } else log.appendText("Brak środków\n"); }));
            view.getChildren().add(row);
        }
    }

    // -------------------------------------------------------------
    // Jobs
    // -------------------------------------------------------------
    private static VBox createJobsView(Simulator sim, TextArea log) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        refreshJobsView(box, sim, log);
        return box;
    }

    private static void refreshJobsView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(new Label("Zlecenia:"));

        // Collect busy drivers and vehicles (those on active jobs)
        Set<Driver> busyDrivers = new HashSet<>();
        Set<Vehicle> busyVehicles = new HashSet<>();
        for (Job active : sim.getCompany().getJobs()) {
            if (active.isAssigned() && !active.isCompleted()) {
                if (active.getAssignedDriver() != null) busyDrivers.add(active.getAssignedDriver());
                if (active.getAssignedVehicle() != null) busyVehicles.add(active.getAssignedVehicle());
            }
        }

        for (Job j : sim.getCompany().getJobs()) {
            HBox row = new HBox(12);
            String eta = j.isAssigned() ? (j.getTurnsRemaining() + " tury") : "-";

            ComboBox<String> driverBox = new ComboBox<>();
            ComboBox<String> vehicleBox = new ComboBox<>();

            driverBox.getItems().add("Brak");
            for (Driver d : sim.getCompany().getDrivers()) {
                if (!busyDrivers.contains(d) || (j.isAssigned() && j.getAssignedDriver() == d))
                    driverBox.getItems().add(d.getName());
            }
            driverBox.setValue("Brak");

            vehicleBox.getItems().add("Brak");
            for (Vehicle v : sim.getCompany().getVehicles()) {
                if (!busyVehicles.contains(v) || (j.isAssigned() && j.getAssignedVehicle() == v))
                    vehicleBox.getItems().add(v.getName());
            }
            vehicleBox.setValue("Brak");

            Button assignBtn = button(j.isAssigned() ? "Przypisano" : "Przypisz", () -> {
                String driverName = driverBox.getValue();
                String vehicleName = vehicleBox.getValue();

                if (driverName == null || driverName.equals("Brak") ||
                    vehicleName == null || vehicleName.equals("Brak")) {
                    log.appendText("Musisz wybrać kierowcę i pojazd!\n");
                    return;
                }

                Driver selDriver = sim.getCompany().getDrivers()
                        .stream().filter(d -> d.getName().equals(driverName))
                        .findFirst().orElse(null);
                Vehicle selVehicle = sim.getCompany().getVehicles()
                        .stream().filter(v -> v.getName().equals(vehicleName))
                        .findFirst().orElse(null);

                if (selDriver == null || selVehicle == null) {
                    log.appendText("Niepoprawny wybór kierowcy lub pojazdu!\n");
                    return;
                }

                // Double-booking check
                if (busyDrivers.contains(selDriver) && !(j.isAssigned() && j.getAssignedDriver() == selDriver)) {
                    log.appendText("X " + selDriver.getName() + " jest już zajęty innym zleceniem!\n");
                    return;
                }
                if (busyVehicles.contains(selVehicle) && !(j.isAssigned() && j.getAssignedVehicle() == selVehicle)) {
                    log.appendText("X " + selVehicle.getName() + " jest już w użyciu przy innym zleceniu!\n");
                    return;
                }

                j.assign(selDriver, selVehicle);
                log.appendText(String.format(
                    "- Zlecenie '%s' przypisane: %s + %s (ETA %d)\n",
                    j.getTitle(), selDriver.getName(), selVehicle.getName(), j.getTurnsRemaining()
                ));
                refreshJobsView(view, sim, log);
            });
            assignBtn.setDisable(j.isAssigned());

            row.getChildren().addAll(
                new Label(j.getTitle()),
                new Label(String.format("Nagroda: %.0f", j.getReward())),
                new Label("Trasa: " + j.getRouteLength() + " km"),
                new Label("ETA: " + eta),
                new Label("Kierowca:"), driverBox,
                new Label("Pojazd:"), vehicleBox,
                assignBtn
            );
            view.getChildren().add(row);
        }

        view.getChildren().add(new Separator());
        view.getChildren().add(button("Dodaj losowe zlecenie", () -> {
            Job nj = new Job("Ad-hoc", 300 + new Random().nextInt(800), 20 + new Random().nextInt(400));
            sim.getCompany().addJob(nj);
            refreshJobsView(view, sim, log);
        }));
    }

    // -------------------------------------------------------------
    // Report (Tables)
    // -------------------------------------------------------------
    private static VBox createReportView(Simulator sim, TextArea log) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        refreshReportView(box, sim, log);
        return box;
    }

    private static void refreshReportView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();

        // Financial summary
        Label title = new Label("Raport finansowy:");
        Label turn = new Label("Tura: " + sim.getTurn());
        Label cash = new Label(String.format("Budżet: %.2f", sim.getCompany().getCash()));
        Label fuel = new Label(String.format("Cena paliwa: %.2f", sim.getCompany().getFuelPrice()));

        // Jobs in progress table
        TableView<JobRow> inProgress = new TableView<>();
        inProgress.setPrefHeight(180);
        ObservableList<JobRow> inData = FXCollections.observableArrayList();

        TableColumn<JobRow, String> colJob = new TableColumn<>("Zlecenie"); colJob.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<JobRow, String> colDriver = new TableColumn<>("Kierowca"); colDriver.setCellValueFactory(new PropertyValueFactory<>("driver"));
        TableColumn<JobRow, String> colVehicle = new TableColumn<>("Pojazd"); colVehicle.setCellValueFactory(new PropertyValueFactory<>("vehicle"));
        TableColumn<JobRow, Integer> colETA = new TableColumn<>("ETA (tury)"); colETA.setCellValueFactory(new PropertyValueFactory<>("eta"));

        inProgress.getColumns().addAll(colJob, colDriver, colVehicle, colETA);

        for (Job j : sim.getCompany().getJobs()) {
            if (j.isAssigned() && !j.isCompleted()) {
                String drv = j.getAssignedDriver() != null ? j.getAssignedDriver().getName() : "-";
                String veh = j.getAssignedVehicle() != null ? j.getAssignedVehicle().getName() : "-";
                inData.add(new JobRow(j.getTitle(), drv, veh, j.getTurnsRemaining()));
            }
        }
        inProgress.setItems(inData);

        // Completed this turn table
        TableView<CompleteRow> doneTable = new TableView<>();
        doneTable.setPrefHeight(140);
        ObservableList<CompleteRow> doneData = FXCollections.observableArrayList();

        TableColumn<CompleteRow, String> dJob = new TableColumn<>("Zlecenie"); dJob.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<CompleteRow, String> dResult = new TableColumn<>("Wynik"); dResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        doneTable.getColumns().addAll(dJob, dResult);

        for (Job j : sim.getCompletedThisTurn()) {
            doneData.add(new CompleteRow(j.getTitle(), j.getResult()));
        }
        doneTable.setItems(doneData);

        view.getChildren().addAll(title, turn, cash, fuel, new Label("Zlecenia w toku:"), inProgress, new Label("Zlecenia zakończone w tej turze:"), doneTable);
    }

    // -------------------------------------------------------------
    // Helper classes for table rows
    // -------------------------------------------------------------
    public static class JobRow {
        private String title, driver, vehicle;
        private Integer eta;
        public JobRow(String title, String driver, String vehicle, Integer eta) { this.title = title; this.driver = driver; this.vehicle = vehicle; this.eta = eta; }
        public String getTitle() { return title; } public String getDriver() { return driver; } public String getVehicle() { return vehicle; } public Integer getEta() { return eta; }
    }

    public static class CompleteRow {
        private String title, result;
        public CompleteRow(String title, String result) { this.title = title; this.result = result; }
        public String getTitle() { return title; } public String getResult() { return result; }
    }

    // -------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------
    private static Button button(String text, Runnable action) { Button b = new Button(text); b.setOnAction(e -> action.run()); return b; }
}
