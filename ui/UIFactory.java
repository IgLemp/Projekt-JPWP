package com.transport.ui;

import com.transport.sim.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.stream.Collectors;

public class UIFactory {

    public static BorderPane createMainUI(Simulator simulator) {
        BorderPane root = new BorderPane();

        // Styles
        String btnStyle = "-fx-min-width: 120; -fx-alignment: center-left;";

        // Left navigation
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(12));
        nav.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        
        Button btnFleet = new Button("Flota"); 
        Button btnDrivers = new Button("Kierowcy");
        Button btnJobs = new Button("Zlecenia");
        Button btnReport = new Button("Raport");
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnNext = new Button("NASTĘPNA TURA >");
        btnNext.setStyle("-fx-base: #b6e7c9; -fx-font-weight: bold;");
        btnNext.setPrefHeight(40);
        btnNext.setMaxWidth(Double.MAX_VALUE);

        nav.getChildren().addAll(btnFleet, btnDrivers, btnJobs, btnReport, spacer, btnNext);

        // Log area
        TextArea log = new TextArea();
        log.setEditable(false);
        log.setWrapText(true);
        log.setPrefWidth(300);
        VBox logBox = new VBox(5, new Label("Dziennik zdarzeń:"), log);
        logBox.setPadding(new Insets(10));
        logBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        // Center stack
        StackPane center = new StackPane();
        center.setPadding(new Insets(15));
        
        // Views
        VBox fleetView = createFleetView(simulator, log);
        VBox driversView = createDriversView(simulator, log);
        VBox jobsView = createJobsView(simulator, log);
        VBox reportView = createReportView(simulator, log);
        
        center.getChildren().addAll(fleetView, driversView, jobsView, reportView);
        setVisibleOnly(fleetView, driversView, jobsView, reportView);

        // Wiring buttons
        btnFleet.setOnAction(e -> { setVisibleOnly(fleetView, driversView, jobsView, reportView); refreshFleetView(fleetView, simulator, log); });
        btnDrivers.setOnAction(e -> { setVisibleOnly(driversView, fleetView, jobsView, reportView); refreshDriversView(driversView, simulator, log); });
        btnJobs.setOnAction(e -> { setVisibleOnly(jobsView, fleetView, driversView, reportView); refreshJobsView(jobsView, simulator, log); });
        btnReport.setOnAction(e -> { setVisibleOnly(reportView, fleetView, driversView, jobsView); refreshReportView(reportView, simulator, log); });

        btnNext.setOnAction(e -> {
            ProgressIndicator pi = new ProgressIndicator();
            StageUtils.showTemporaryOverlay(center, pi);
            btnNext.setDisable(true);

            Task<String> t = new Task<>() {
                @Override
                protected String call() throws Exception {
                    Thread.sleep(300); // UI feel
                    return simulator.nextTurn();
                }

                @Override
                protected void succeeded() {
                    StageUtils.hideTemporaryOverlay(center);
                    log.appendText(getValue());
                    log.appendText("\n-----------------\n");
                    refreshAllViews(fleetView, driversView, jobsView, reportView, simulator, log);
                    btnNext.setDisable(false);
                }

                @Override
                protected void failed() {
                    StageUtils.hideTemporaryOverlay(center);
                    log.appendText("Błąd krytyczny symulacji!\n");
                    getException().printStackTrace();
                    btnNext.setDisable(false);
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
        if (fleet.isVisible()) refreshFleetView(fleet, sim, log);
        if (drivers.isVisible()) refreshDriversView(drivers, sim, log);
        if (jobs.isVisible()) refreshJobsView(jobs, sim, log);
        if (report.isVisible()) refreshReportView(report, sim, log);
    }

    // ================= FLEET VIEW =================
    private static VBox createFleetView(Simulator sim, TextArea log) {
        VBox box = new VBox(10);
        return box;
    }

    private static void refreshFleetView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(header("Twoja Flota"));

        ScrollPane scroll = new ScrollPane();
        VBox list = new VBox(10);
        list.setPadding(new Insets(10));
        
        for (Vehicle v : sim.getCompany().getVehicles()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color: #ddd; -fx-padding: 8; -fx-background-color: white;");
            
            Label name = new Label(v.getName());
            name.setFont(Font.font("System", FontWeight.BOLD, 12));
            name.setPrefWidth(120);
            
            ProgressBar conditionBar = new ProgressBar(v.getCondition() / 100.0);
            conditionBar.setStyle(v.getCondition() < 50 ? "-fx-accent: red;" : "-fx-accent: green;");
            
            VBox stats = new VBox(
                new Label(String.format("Spalanie: %.2f", v.getFuelConsumptionPerKm())),
                new Label(String.format("Konserwacja za: %d km", v.getMaintenanceIntervalKm() - v.getKmSinceMaintenance()))
            );
            stats.setPrefWidth(150);

            Button btnRepair = new Button("Napraw (-100)");
            btnRepair.setOnAction(e -> {
                if (sim.getCompany().getCash() >= 100) {
                    sim.getCompany().addCash(-100);
                    v.repair(20);
                    refreshFleetView(view, sim, log);
                } else {
                    log.appendText("Brak środków na naprawę!\n");
                }
            });

            Button btnSell = new Button("Sprzedaj");
            btnSell.setOnAction(e -> {
                double price = sim.getCompany().sellVehicle(v);
                log.appendText(String.format("Sprzedano %s za %.2f\n", v.getName(), price));
                refreshFleetView(view, sim, log);
            });

            // Cannot sell if assigned to active job
            boolean busy = sim.getCompany().isVehicleBusy(v);
            if(busy) {
                btnSell.setDisable(true);
                btnSell.setText("W trasie");
            }

            row.getChildren().addAll(name, new Label("Stan: " + v.getCondition() + "%"), conditionBar, stats, btnRepair, btnSell);
            list.getChildren().add(row);
        }
        scroll.setContent(list);
        view.getChildren().add(scroll);

        // Market
        view.getChildren().add(new Separator());
        view.getChildren().add(header("Rynek Pojazdów"));
        
        VBox marketList = new VBox(5);
        for (VehicleOffer offer : sim.getCompany().getVehicleMarket()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            Label info = new Label(String.format("%s | Stan: %d%% | %.2f u/km | Cena: %.2f", 
                offer.getName(), offer.getCondition(), offer.getFuelConsumption(), offer.getPrice()));
            info.setPrefWidth(400);
            
            Button buy = new Button("Kup");
            buy.setOnAction(e -> {
                if (sim.getCompany().buyOffer(offer)) {
                    log.appendText("Zakupiono " + offer.getName() + "\n");
                    refreshFleetView(view, sim, log);
                } else {
                    log.appendText("Brak środków!\n");
                }
            });
            row.getChildren().addAll(info, buy);
            marketList.getChildren().add(row);
        }
        view.getChildren().add(marketList);
    }

    // ================= DRIVERS VIEW =================
    private static VBox createDriversView(Simulator sim, TextArea log) {
        return new VBox(10);
    }

    private static void refreshDriversView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(header("Zespół Kierowców"));

        VBox list = new VBox(10);
        for (Driver d : sim.getCompany().getDrivers()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: white; -fx-padding: 8; -fx-border-color: #ddd;");
            
            Label name = new Label(d.getName());
            name.setPrefWidth(120);
            name.setStyle("-fx-font-weight: bold");

            Label skill = new Label("Skill: " + d.getSkill());
            skill.setPrefWidth(80);

            // Vehicle Assignment Logic
            ComboBox<String> vehCombo = new ComboBox<>();
            vehCombo.getItems().add("Brak pojazdu");
            
            // Available vehicles: those not assigned to ANYONE else, OR the one currently assigned to THIS driver
            List<Vehicle> availableVehicles = sim.getCompany().getVehicles().stream()
                .filter(v -> {
                    Driver owner = sim.getCompany().getDriverForVehicle(v);
                    return owner == null || owner == d;
                })
                .collect(Collectors.toList());

            for(Vehicle v : availableVehicles) vehCombo.getItems().add(v.getName());
            
            String currentVehName = d.getAssignedVehicle() != null ? d.getAssignedVehicle().getName() : "Brak pojazdu";
            vehCombo.setValue(currentVehName);

            Button btnAssign = new Button("Zmień");
            
            // Check if driver is currently WORKING. If so, lock assignment.
            boolean isWorking = sim.getCompany().isDriverBusy(d);
            if (isWorking) {
                vehCombo.setDisable(true);
                btnAssign.setDisable(true);
                btnAssign.setText("W trasie");
                row.setStyle("-fx-background-color: #fff0f0; -fx-padding: 8; -fx-border-color: #eebbbb;");
            }

            btnAssign.setOnAction(e -> {
                String selected = vehCombo.getValue();
                if (selected.equals("Brak pojazdu")) {
                    d.setAssignedVehicle(null);
                } else {
                    Vehicle v = sim.getCompany().getVehicles().stream()
                        .filter(vh -> vh.getName().equals(selected)).findFirst().orElse(null);
                    d.setAssignedVehicle(v);
                }
                refreshDriversView(view, sim, log);
            });
            
            Button btnTrain = new Button("Szkol ($50)");
            btnTrain.setOnAction(e -> {
                 if (sim.getCompany().getCash() >= 50) {
                     sim.getCompany().addCash(-50);
                     d.train(5);
                     refreshDriversView(view, sim, log);
                 }
            });

            row.getChildren().addAll(name, skill, new Label("Pojazd:"), vehCombo, btnAssign, btnTrain);
            list.getChildren().add(row);
        }
        view.getChildren().add(list);

        view.getChildren().add(new Separator());
        view.getChildren().add(header("Giełda Pracy (Kandydaci)"));
        
        for(DriverCandidate c : sim.getCompany().getCandidates()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(
                new Label(c.getName()), 
                new Label("Skill: " + c.getSkill()), 
                new Label("Koszt: $" + c.getHireCost()),
                button("Zatrudnij", () -> {
                    if (sim.getCompany().hireCandidate(c)) {
                        refreshDriversView(view, sim, log);
                    } else {
                        log.appendText("Za mało gotówki!\n");
                    }
                })
            );
            view.getChildren().add(row);
        }
    }

    // ================= JOBS VIEW =================
    private static VBox createJobsView(Simulator sim, TextArea log) { return new VBox(10); }

    private static void refreshJobsView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(header("Dostępne Zlecenia"));

        // Only list drivers who have a vehicle and are NOT currently busy
        List<Driver> availableDrivers = sim.getCompany().getDrivers().stream()
            .filter(d -> d.hasVehicle() && !sim.getCompany().isDriverBusy(d))
            .collect(Collectors.toList());

        for (Job j : sim.getCompany().getJobs()) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            
            // Visual style for active/assigned jobs
            if (j.isAssigned()) {
                row.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #c8e6c9; -fx-border-width: 1;");
                Label lbl = new Label(String.format("%s | %s [ETA: %d]", j.getTitle(), j.getAssignedDriver().getName(), j.getTurnsRemaining()));
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                row.getChildren().add(lbl);
            } else {
                // Unassigned Job
                row.setStyle("-fx-background-color: white; -fx-border-color: #eee; -fx-border-width: 1;");
                
                VBox details = new VBox(
                    new Label(j.getTitle()),
                    new Label(String.format("%d km | Nagroda: $%.0f", j.getRouteLength(), j.getReward()))
                );
                details.setPrefWidth(250);

                // Driver selector (Implied Vehicle)
                ComboBox<Driver> drvCombo = new ComboBox<>();
                drvCombo.setItems(FXCollections.observableArrayList(availableDrivers));
                // Define how drivers look in combo
                drvCombo.setCellFactory(p -> new ListCell<>() {
                    @Override protected void updateItem(Driver item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) setText(null);
                        else setText(item.getName() + " (" + item.getAssignedVehicle().getName() + ")");
                    }
                });
                drvCombo.setButtonCell(drvCombo.getCellFactory().call(null)); // for selected item view
                drvCombo.setPromptText("Wybierz kierowcę...");
                drvCombo.setPrefWidth(200);

                Button btnStart = new Button("Start");
                btnStart.setOnAction(e -> {
                    Driver d = drvCombo.getValue();
                    if (d == null) {
                        log.appendText("Wybierz kierowcę!\n");
                        return;
                    }
                    j.assign(d, d.getAssignedVehicle());
                    log.appendText("Rozpoczęto: " + j.getTitle() + "\n");
                    refreshJobsView(view, sim, log);
                });

                row.getChildren().addAll(details, drvCombo, btnStart);
            }
            view.getChildren().add(row);
        }
    }

    // ================= REPORT VIEW =================
    private static VBox createReportView(Simulator sim, TextArea log) { return new VBox(10); }

    private static void refreshReportView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(header("Podsumowanie Finansowe"));

        Label lCash = new Label(String.format("Gotówka: $%.2f", sim.getCompany().getCash()));
        lCash.setFont(Font.font("System", FontWeight.BOLD, 16));
        lCash.setTextFill(sim.getCompany().getCash() < 0 ? Color.RED : Color.BLACK);

        view.getChildren().addAll(
            lCash, 
            new Label("Tura: " + sim.getTurn()),
            new Label(String.format("Cena paliwa: $%.2f", sim.getCompany().getFuelPrice()))
        );

        view.getChildren().add(new Separator());
        view.getChildren().add(new Label("Historia ostatnich zleceń:"));
        
        TableView<JobRow> table = new TableView<>();
        TableColumn<JobRow, String> cName = new TableColumn<>("Zlecenie"); cName.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<JobRow, String> cRes = new TableColumn<>("Wynik"); cRes.setCellValueFactory(new PropertyValueFactory<>("result"));
        cName.setPrefWidth(200);
        cRes.setPrefWidth(300);
        
        table.getColumns().addAll(cName, cRes);
        
        ObservableList<JobRow> data = FXCollections.observableArrayList();
        for(Job j : sim.getCompletedThisTurn()) {
            data.add(new JobRow(j.getTitle(), j.getResult()));
        }
        table.setItems(data);
        table.setPlaceholder(new Label("Brak zakończonych zleceń w tej turze"));
        
        view.getChildren().add(table);
    }

    // Helpers
    private static Label header(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 14));
        l.setPadding(new Insets(5, 0, 5, 0));
        return l;
    }

    private static Button button(String text, Runnable action) {
        Button b = new Button(text);
        b.setOnAction(e -> action.run());
        return b;
    }

    public static class JobRow {
        public String title, result;
        public JobRow(String t, String r) { title=t; result=r; }
        public String getTitle(){return title;} public String getResult(){return result;}
    }
}
