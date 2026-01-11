package com.transport.ui;

import com.transport.sim.*;
import com.transport.score.*;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class UIFactory {

    public static BorderPane createMainUI(Simulator simulator, Runnable onRestart) {
        BorderPane root = new BorderPane();

        // Left navigation
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(12));
        nav.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        
        Button btnFleet = new Button("Flota"); 
        Button btnDrivers = new Button("Kierowcy");
        Button btnJobs = new Button("Zlecenia");
        Button btnReport = new Button("Raport");
        Button btnRanking = new Button("Ranking"); // NOWY
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnEndGame = new Button("ZAKOŃCZ GRĘ"); // NOWY
        btnEndGame.setStyle("-fx-base: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEndGame.setMaxWidth(Double.MAX_VALUE);
        btnEndGame.setOnAction(e -> {
            double score = new ScoreService().calculateFinalScore(simulator);
            SaveScoreDialog.show(simulator, score);
            onRestart.run();
        });

        Button btnNext = new Button("NASTĘPNA TURA >");
        btnNext.setStyle("-fx-base: #b6e7c9; -fx-font-weight: bold;");
        btnNext.setPrefHeight(40);
        btnNext.setMaxWidth(Double.MAX_VALUE);

        nav.getChildren().addAll(btnFleet, btnDrivers, btnJobs, btnReport, btnRanking, spacer, btnEndGame, btnNext);

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
        VBox rankingView = createRankingView(simulator); // NOWY
        
        center.getChildren().addAll(fleetView, driversView, jobsView, reportView, rankingView);
        setVisibleOnly(fleetView, driversView, jobsView, reportView, rankingView);

        // Wiring buttons
        btnFleet.setOnAction(e -> { setVisibleOnly(fleetView, driversView, jobsView, reportView, rankingView); refreshFleetView(fleetView, simulator, log); });
        btnDrivers.setOnAction(e -> { setVisibleOnly(driversView, fleetView, jobsView, reportView, rankingView); refreshDriversView(driversView, simulator, log); });
        btnJobs.setOnAction(e -> { setVisibleOnly(jobsView, fleetView, driversView, reportView, rankingView); refreshJobsView(jobsView, simulator, log); });
        btnReport.setOnAction(e -> { setVisibleOnly(reportView, fleetView, driversView, jobsView, rankingView); refreshReportView(reportView, simulator, log); });
        btnRanking.setOnAction(e -> { setVisibleOnly(rankingView, fleetView, driversView, jobsView, reportView); refreshRankingView(rankingView, simulator); });

        btnNext.setOnAction(e -> {
            ProgressIndicator pi = new ProgressIndicator();
            StageUtils.showTemporaryOverlay(center, pi);
            btnNext.setDisable(true);

            Task<String> t = new Task<>() {
                @Override
                protected String call() throws Exception {
                    Thread.sleep(300);
                    return simulator.nextTurn();
                }

                @Override
                protected void succeeded() {
                    StageUtils.hideTemporaryOverlay(center);
                    log.appendText(getValue());
                    log.appendText("\n-----------------\n");
                    refreshAllViews(fleetView, driversView, jobsView, reportView, simulator, log);
                    
                    if (simulator.isGameOver()) {
                        btnNext.setDisable(true);
                        nav.setDisable(true);
                        VBox bankruptcyScreen = BankruptcyScreen.create(simulator, onRestart);
                        StageUtils.showTemporaryOverlay(center, bankruptcyScreen);
                    } else {
                        btnNext.setDisable(false);
                    }
                }

                @Override
                protected void failed() {
                    StageUtils.hideTemporaryOverlay(center);
                    log.appendText("Błąd krytyczny symulacji!\n");
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
    private static VBox createFleetView(Simulator sim, TextArea log) { return new VBox(10); }

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
                if (price > 0) {
                    log.appendText(String.format("Sprzedano %s za %.2f\n", v.getName(), price));
                    refreshFleetView(view, sim, log);
                }
            });

            if(sim.getCompany().isVehicleBusy(v)) {
                btnSell.setDisable(true);
                btnSell.setText("W trasie");
            }

            row.getChildren().addAll(name, new Label("Stan: " + v.getCondition() + "%"), conditionBar, stats, btnRepair, btnSell);
            list.getChildren().add(row);
        }
        scroll.setContent(list);
        view.getChildren().add(scroll);

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
    private static VBox createDriversView(Simulator sim, TextArea log) { return new VBox(10); }

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

            ComboBox<String> vehCombo = new ComboBox<>();
            vehCombo.getItems().add("Brak pojazdu");
            List<Vehicle> availableVehicles = sim.getCompany().getVehicles().stream()
                .filter(v -> {
                    Driver owner = sim.getCompany().getDriverForVehicle(v);
                    return owner == null || owner == d;
                }).collect(Collectors.toList());

            for(Vehicle v : availableVehicles) vehCombo.getItems().add(v.getName());
            vehCombo.setValue(d.getAssignedVehicle() != null ? d.getAssignedVehicle().getName() : "Brak pojazdu");

            Button btnAssign = new Button("Zmień");
            if (sim.getCompany().isDriverBusy(d)) {
                vehCombo.setDisable(true);
                btnAssign.setDisable(true);
                btnAssign.setText("W trasie");
            }

            btnAssign.setOnAction(e -> {
                String selected = vehCombo.getValue();
                if (selected == null || selected.equals("Brak pojazdu")) d.setAssignedVehicle(null);
                else {
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

            row.getChildren().addAll(name, new Label("Skill: " + d.getSkill()), new Label("Pojazd:"), vehCombo, btnAssign, btnTrain);
            list.getChildren().add(row);
        }
        view.getChildren().add(list);
        view.getChildren().add(new Separator());
        view.getChildren().add(header("Giełda Pracy (Kandydaci)"));
        for(DriverCandidate c : sim.getCompany().getCandidates()) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(new Label(c.getName()), new Label("Skill: " + c.getSkill()), 
                button("Zatrudnij", () -> {
                    if (sim.getCompany().hireCandidate(c)) {
                        refreshDriversView(view, sim, log);
                    }
                }));
            view.getChildren().add(row);
        }
    }

    // ================= JOBS VIEW =================
    private static VBox createJobsView(Simulator sim, TextArea log) { return new VBox(10); }

    private static void refreshJobsView(VBox view, Simulator sim, TextArea log) {
        view.getChildren().clear();
        view.getChildren().add(header("Dostępne Zlecenia"));
        List<Driver> availableDrivers = sim.getCompany().getDrivers().stream()
            .filter(d -> d.hasVehicle() && !sim.getCompany().isDriverBusy(d))
            .collect(Collectors.toList());

        for (Job j : sim.getCompany().getJobs()) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            if (j.isAssigned()) {
                row.setStyle("-fx-background-color: #e8f5e9;");
                row.getChildren().add(new Label(j.getTitle() + " | " + j.getAssignedDriver().getName() + " [ETA: " + j.getTurnsRemaining() + "]"));
            } else {
                VBox details = new VBox(new Label(j.getTitle()), new Label("Dystans: " + j.getRouteLength() + " km"));
                details.setPrefWidth(250);
                ComboBox<Driver> drvCombo = new ComboBox<>(FXCollections.observableArrayList(availableDrivers));
                drvCombo.setPromptText("Wybierz kierowcę...");
                Button btnStart = new Button("Start");
                btnStart.setOnAction(e -> {
                    Driver d = drvCombo.getValue();
                    if (d != null) {
                        j.assign(d, d.getAssignedVehicle());
                        refreshJobsView(view, sim, log);
                    }
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
        view.getChildren().addAll(lCash, new Label("Tura: " + sim.getTurn()), new Label("Reputacja: " + sim.getCompany().getReputation()));
        
        TableView<JobRow> table = new TableView<>();
        TableColumn<JobRow, String> cName = new TableColumn<>("Zlecenie"); cName.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<JobRow, String> cRes = new TableColumn<>("Wynik"); cRes.setCellValueFactory(new PropertyValueFactory<>("result"));
        table.getColumns().addAll(cName, cRes);
        
        ObservableList<JobRow> data = FXCollections.observableArrayList();
        for(Job j : sim.getCompletedThisTurn()) data.add(new JobRow(j.getTitle(), j.getResult()));
        table.setItems(data);
        view.getChildren().add(table);
    }

    // ================= RANKING VIEW (NOWY) =================
    private static VBox createRankingView(Simulator sim) {
        VBox view = new VBox(15);
        view.setPadding(new Insets(10));
        refreshRankingView(view, sim);
        return view;
    }

    private static void refreshRankingView(VBox view, Simulator sim) {
        view.getChildren().clear();
        view.getChildren().add(header("NAJLEPSZE WYNIKI (TOP 10)"));
        
        TableView<ScoreRecord> table = new TableView<>();
        TableColumn<ScoreRecord, String> colName = new TableColumn<>("Gracz");
        colName.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        colName.setPrefWidth(150);
        
        TableColumn<ScoreRecord, Double> colScore = new TableColumn<>("Wynik");
        colScore.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        
        TableColumn<ScoreRecord, Integer> colTurns = new TableColumn<>("Tury");
        colTurns.setCellValueFactory(new PropertyValueFactory<>("turnsReached"));

        table.getColumns().addAll(colName, colScore, colTurns);
        table.getItems().setAll(new ScoreService().loadScores(new File(".")));
        
        view.getChildren().add(table);
    }

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
