package com.smartcountdown;

import com.smartcountdown.service.GeminiService;
import com.smartcountdown.model.Task;
import com.smartcountdown.database.DatabaseManager;
import com.smartcountdown.database.TaskRepository;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;

public class SmartCountdownApp extends Application {

    private final ObservableList<Task> tasks =
            FXCollections.observableArrayList();

    private final TaskRepository taskRepository =
            new TaskRepository();

    private final GeminiService geminiService = new GeminiService();


    private TableView<Task> taskTable;

    private Label totalTasks;
    private Label pendingTasks;
    private Label overdueTasks;


    /**
     *
     */
    public SmartCountdownApp() {
        super();
    }

    @Override
    public void start(Stage stage) throws Exception {

        DatabaseManager.initializeDatabase();
        tasks.addAll(taskRepository.findAll());

        stage.setTitle("AI Smart Countdown");

        // Header
        Label heading = new Label("AI SMART COUNTDOWN");
        heading.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label subtitle = new Label(
                "Manage your important deadlines intelligently"
        );
        subtitle.setStyle("-fx-text-fill: #cccccc;");

        VBox header = new VBox(8, heading, subtitle);
        header.setPadding(new Insets(25));
        header.setStyle("-fx-background-color: #172554;");

        // Summary cards
        totalTasks = new Label("0");
        pendingTasks = new Label("0");
        overdueTasks = new Label("0");

        totalTasks.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2563eb;"
        );

        pendingTasks.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #d97706;"
        );

        overdueTasks.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #dc2626;"
        );

        HBox summary = new HBox(
                15,
                createSummaryCard("TOTAL TASKS", totalTasks),
                createSummaryCard("PENDING", pendingTasks),
                createSummaryCard("OVERDUE", overdueTasks)
        );

        summary.setPadding(new Insets(20));

        // Task table
        taskTable = new TableView<>();
        taskTable.setItems(tasks);

        //taskTable.setFixedCellSize(34);
        taskTable.setFixedCellSize(40);

        taskTable.setRowFactory(table -> new TableRow<Task>() {

            @Override
            protected void updateItem(Task task, boolean empty) {

                super.updateItem(task, empty);

                if (empty || task == null) {

                    setStyle("");

                } else if (task.isCompleted()) {

                    // Completed task - light green
                    setStyle(
                            "-fx-background-color: #dcfce7;" +
                                    "-fx-text-background-color: #166534;"
                    );

                } else if (task.getDeadline()
                        .isBefore(LocalDate.now())) {

                    // Overdue task - light red
                    setStyle(
                            "-fx-background-color: #fee2e2;" +
                                    "-fx-text-background-color: #991b1b;"
                    );

                } else {

                    // Normal pending task
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-text-background-color: #334155;"
                    );
                }
            }
        });

        TableColumn<Task, String> titleCol =
                new TableColumn<>("Task Name");
        titleCol.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("title")
        );

        TableColumn<Task, String> categoryCol =
                new TableColumn<>("Category");
        categoryCol.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("category")
        );

        TableColumn<Task, LocalDate> deadlineCol =
                new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(
                new PropertyValueFactory<>("deadline")
        );

        TableColumn<Task, String> priorityCol =
                new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("priority")
        );

        TableColumn<Task, String> remainingCol =
                new TableColumn<>("Countdown");
        //remainingCol.setCellValueFactory(
         //       new javafx.scene.control.cell.PropertyValueFactory<>("daysRemaining")
        //);
        remainingCol.setCellValueFactory(new PropertyValueFactory<>("countdownText"));

        taskTable.getColumns().addAll(
                titleCol,
                categoryCol,
                deadlineCol,
                priorityCol,
                remainingCol
        );

        taskTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        // Buttons
        Button addButton = new Button("+ Add New Task");
        addButton.setOnAction(event -> showAddTaskDialog());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(event -> deleteSelectedTask());

        Button completeButton = new Button("Mark Complete / Reopen");
        completeButton.setOnAction(event -> {
             Task selected = taskTable.getSelectionModel().getSelectedItem();
             if (selected == null) {
                showAlert("Please select a task first");
                return;
            }

            try {
                selected.setCompleted(!selected.isCompleted());

                taskRepository.update(selected);

                taskTable.refresh();
                updateSummary();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Unable to Update task Status.");
            }

        });

        Button editButton = new Button("Edit Selected");
        editButton.setOnAction(event -> {
            Task selected = taskTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please Select a task to edit");
                return;
            }

            showEditTaskDialog(selected);
        });

        Button aiButton = new Button("AI Task Suggestions");
        aiButton.setOnAction(event -> getAITaskSuggestions(aiButton));

        Button priorityButton = new Button("AI Priority Analysis");
        priorityButton.setOnAction(event -> getAIPriorityAnalysis(priorityButton));

        Button breakdownButton = new Button("AI Task Breakdown");
        breakdownButton.setOnAction(event -> getAITaskBreakdown(breakdownButton));

//        HBox actions = new HBox(10, addButton, editButton,
//                completeButton, deleteButton, aiButton, priorityButton);
//        actions.setAlignment(Pos.CENTER_LEFT);
//        actions.setPadding(new Insets(15, 20, 10, 20));
//
//        Label tableHeading = new Label("Your Upcoming Deadlines");
//        tableHeading.setStyle(
//                "-fx-font-size: 18px;" +
//                        "-fx-font-weight: bold;"
//        );

        Label taskActionsLabel =
                new Label("TASK MANAGEMENT");

        Label aiActionsLabel =
                new Label("AI ASSISTANT");

        taskActionsLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #475569;"
        );

        aiActionsLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1d4ed8;"
        );

        addButton.setStyle(
                "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        editButton.setStyle(
                "-fx-background-color: #64748b;" +
                        "-fx-text-fill: white;"
        );

        completeButton.setStyle(
                "-fx-background-color: #16a34a;" +
                        "-fx-text-fill: white;"
        );

        deleteButton.setStyle(
                "-fx-background-color: #dc2626;" +
                        "-fx-text-fill: white;"
        );

        aiButton.setStyle(
                "-fx-background-color: #4338ca;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        priorityButton.setStyle(
                "-fx-background-color: #7c3aed;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;"
        );

        HBox taskActions = new HBox(
                10,
                addButton,
                editButton,
                completeButton,
                deleteButton
        );

        taskActions.setAlignment(Pos.CENTER_LEFT);

        HBox aiActions = new HBox(
                10,
                aiButton,
                priorityButton,breakdownButton
        );

        aiActions.setAlignment(Pos.CENTER_LEFT);

        VBox actions = new VBox(
                8,
                taskActionsLabel,
                taskActions,
                aiActionsLabel,
                aiActions
        );

        actions.setPadding(new Insets(15, 20, 10, 20));

        Label tableHeading = new Label("Your Upcoming Deadlines");

        tableHeading.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        VBox content = new VBox(
                10,
                summary,
                actions,
                tableHeading,
                taskTable
        );

        content.setPadding(new Insets(5, 20, 20, 20));
        VBox.setVgrow(taskTable, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);

        root.setStyle("-fx-background-color: #f1f5f9;");

        Scene scene = new Scene(root, 1100, 700);

        stage.setMinWidth(850);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();

        // Refresh countdown and summary every minute
        //Timeline refreshTimer = new Timeline(
        //        new KeyFrame(
        //                Duration.minutes(1),
        //                event -> {
        //                    taskTable.refresh();
        //                    updateSummary();
        //                }
        //        )
        //);

        Timeline refreshTimer = new Timeline( new KeyFrame(Duration.seconds(1),
                event -> {
                        taskTable.refresh();
                        updateSummary();;
                        }
                )
        );

        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();

        stage.setOnCloseRequest(event -> refreshTimer.stop());

        updateSummary();
    }

    private VBox createSummaryCard(
            String title,
            Label value) {

        Label heading = new Label(title);

        value.setStyle(
                "-fx-font-size: 27px;" +
                        "-fx-font-weight: bold;"
        );

        VBox card = new VBox(10, heading, value);

//        card.setPadding(new Insets(20));
//        card.setPrefWidth(220);
//        card.setStyle(
//                "-fx-background-color: white;" +
//                        "-fx-background-radius: 12;"
//        );

        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(110);

        HBox.setHgrow(card, Priority.ALWAYS);

        heading.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #64748b;"
        );

        value.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #172554;"
        );

//
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 3);"
        );
        return card;
    }

    private void showAddTaskDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Enter your deadline details");

        ButtonType saveButton = new ButtonType(
                "Save Task",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField titleField = new TextField();
        titleField.setPromptText("Example: Final Examination");

        ComboBox<String> categoryField = new ComboBox<>();

        categoryField.getItems().addAll(
                "Education",
                "Finance",
                "Personal",
                "Other"
        );

        categoryField.setValue("Personal");

        DatePicker deadlineField = new DatePicker();
        deadlineField.setValue(LocalDate.now().plusDays(7));

        ComboBox<String> priorityField = new ComboBox<>();

        priorityField.getItems().addAll(
                "High",
                "Medium",
                "Low"
        );

        priorityField.setValue("Medium");

        GridPane grid = new GridPane();

        grid.setHgap(12);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Task Name:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);

        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlineField, 1, 2);

        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    if (titleField.getText().isBlank()
                            || deadlineField.getValue() == null) {

                        Alert alert = new Alert(
                                Alert.AlertType.WARNING,
                                "Please enter a task name and deadline."
                        );

                        alert.showAndWait();
                        event.consume();
                    }
                }
        );

        dialog.showAndWait().ifPresent(result -> {

            if (result == saveButton) {

                Task task = new Task(
                        titleField.getText().trim(),
                        categoryField.getValue(),
                        deadlineField.getValue(),
                        priorityField.getValue()
                );

                //tasks.add(task);

                //updateSummary();
                try {

                    taskRepository.insert(task);

                    tasks.add(task);

                    updateSummary();

                } catch (SQLException e) {

                    e.printStackTrace();

                    new Alert(
                            Alert.AlertType.ERROR,
                            "Unable to save task: " + e.getMessage()
                    ).showAndWait();
                }
            }
        });
    }

    private void deleteSelectedTask() {

        Task selected = taskTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            return;
        }

        //if (selected != null) {
        //    tasks.remove(selected);
        //    updateSummary();
        //}

        try {
            taskRepository.deleteById(selected.getId());
            tasks.remove(selected);
            updateSummary();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Unable to delete task:"+ e.getMessage()).showAndWait();
            throw new RuntimeException(e);
        }
    }

    private void updateSummary() {

        long total = tasks.size();

        long pending = tasks.stream()
                .filter(task -> !task.isCompleted())
                .count();

        long overdue = tasks.stream()
                .filter(task ->
                        !task.isCompleted()
                                && task.getDeadline().isBefore(LocalDate.now())
                )
                .count();

        totalTasks.setText(String.valueOf(total));
        pendingTasks.setText(String.valueOf(pending));
        overdueTasks.setText(String.valueOf(overdue));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("AI Smart Countdown");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showEditTaskDialog(Task task) {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Update task details");

        TextField titleField = new TextField(task.getTitle());

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Work","Personal","Study","Other");
        categoryBox.setValue(task.getCategory());

        DatePicker deadlinePicker = new DatePicker((task.getDeadline()));
        //task.setDeadline(deadlinePicker.getValue());

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low","Medium","High");
        priorityBox.setValue(task.getPriority());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Task Title"),0,0);
        grid.add(titleField,1,0);

        grid.add(new Label("Category"), 0,1);
        grid.add(categoryBox,1,1);

        grid.add(new Label("Deadline"),0,2);
        grid.add(deadlinePicker,1,2);

        grid.add(new Label("Priority"),0,3);
        grid.add(priorityBox,1,3);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                String title = titleField.getText().trim();

                if (title.isEmpty() || categoryBox.getValue() == null ||
                   deadlinePicker.getValue() == null || priorityBox.getValue() == null) {
                    showAlert("Please complete all task details");
                    return;
                }
                try {

                    task.setTitle(title);
                    task.setCategory(categoryBox.getValue());
                    task.setDeadline(deadlinePicker.getValue());
                    System.out.println("Selected Date :" + deadlinePicker.getValue());
                    System.out.println("Task Object date : " + task.getDeadline());
                    task.setPriority(priorityBox.getValue());

                    System.out.println("Selected date:" + deadlinePicker.getValue());
                    System.out.println("Task date before update:" + task.getDeadline());

                    taskRepository.update(task);
                    System.out.println("Task date after update: " + task.getDeadline());

                    taskTable.refresh();;
                    updateSummary();

                    showAlert("Task updated successfully");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Unable to update task");
                }

//                try {
//                    System.out.println("New Deadline Selected : " + deadlinePicker.getValue());
//                    System.out.println("Deadline before database update: " + task.getDeadline());
//                    taskRepository.update(task);
//
//                    taskTable.refresh();
//                    updateSummary();
//
//                    showAlert("Task updated successfully");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    showAlert("Unable to update task");
//
//                }

                }
        });
    }
    private void getAITaskSuggestions(Button aiButton) {

        String taskDetails = tasks.stream()
                .filter(task -> !task.isCompleted())
                .map(task ->
                        "Task: " + task.getTitle()
                                + ", Category: " + task.getCategory()
                                + ", Deadline: " + task.getDeadline()
                                + ", Priority: " + task.getPriority()
                )
                .collect(java.util.stream.Collectors.joining("\n"));

        if (taskDetails.isBlank()) {
            showAlert("There are no pending tasks for Gemini to review.");
            return;
        }

        aiButton.setDisable(true);
        aiButton.setText("AI is thinking...");

        javafx.concurrent.Task<String> aiTask =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected String call() {

                        GeminiService geminiService = new GeminiService();

                        return geminiService.getTaskSuggestions(taskDetails);
                    }
                };

        aiTask.setOnSucceeded(event -> {

            aiButton.setDisable(false);
            aiButton.setText("AI Task Suggestions");

            // Clean Markdown formatting from Gemini response
            String formattedResult = aiTask.getValue()
                    .replaceAll("(?m)^#{1,6}\\s*", "")
                    .replace("**", "")
                    .replace("*", "");

// Create a scrollable result area
            TextArea resultArea = new TextArea(formattedResult);

            resultArea.setEditable(false);
            resultArea.setWrapText(true);
            resultArea.setPrefRowCount(18);
            resultArea.setPrefColumnCount(65);

// Display AI suggestions
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Gemini AI Suggestions");
            alert.setHeaderText("Your Personalized Task Suggestions");

            alert.getDialogPane().setContent(resultArea);
            alert.getDialogPane().setPrefWidth(650);
            alert.getDialogPane().setPrefHeight(550);

            alert.showAndWait();
        });

        aiTask.setOnFailed(event -> {

            aiButton.setDisable(false);
            aiButton.setText("AI Task Suggestions");

            //Throwable error = aiTask.getException();

            //error.printStackTrace();

//            showAlert("Gemini request failed: "
//                    + (error != null ? error.getMessage() : "Unknown error"));
            showAIError("AI Task Suggestions",aiTask.getException());
        });

        Thread aiThread = new Thread(aiTask);
        aiThread.setDaemon(true);
        aiThread.start();
    }

    private void getAIPriorityAnalysis(Button priorityButton) {

        String taskDetails = tasks.stream()
                .filter(task -> !task.isCompleted())
                .map(task ->
                        "Task: " + task.getTitle()
                                + ", Category: " + task.getCategory()
                                + ", Deadline: " + task.getDeadline()
                                + ", Priority: " + task.getPriority()
                )
                .collect(java.util.stream.Collectors.joining("\n"));

        if (taskDetails.isBlank()) {
            showAlert("There are no pending tasks to analyze.");
            return;
        }

        priorityButton.setDisable(true);
        priorityButton.setText("Analyzing...");

        javafx.concurrent.Task<String> analysisTask =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected String call() {

                        GeminiService geminiService =
                                new GeminiService();

                        return geminiService.getPriorityAnalysis(
                                taskDetails
                        );
                    }
                };

        analysisTask.setOnSucceeded(event -> {

            priorityButton.setDisable(false);
            priorityButton.setText("AI Priority Analysis");

            // Clean Markdown formatting from Gemini response
            String formattedResult = analysisTask.getValue()
                    .replaceAll("(?m)^#{1,6}\\s*", "")
                    .replace("**", "")
                    .replace("*", "");

// Create a scrollable result area
            TextArea resultArea = new TextArea(formattedResult);

            resultArea.setEditable(false);
            resultArea.setWrapText(true);
            resultArea.setPrefRowCount(20);
            resultArea.setPrefColumnCount(70);

// Display priority analysis
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("AI Priority Analysis");
            alert.setHeaderText("Intelligent Task Priority Report");

            alert.getDialogPane().setContent(resultArea);
            alert.getDialogPane().setPrefWidth(700);
            alert.getDialogPane().setPrefHeight(550);

            alert.showAndWait();
        });

        analysisTask.setOnFailed(event -> {

            priorityButton.setDisable(false);
            priorityButton.setText("AI Priority Analysis");

            //Throwable error = analysisTask.getException();

            //error.printStackTrace();

//            showAlert("Priority analysis failed: "
//                    + (error != null
//                    ? error.getMessage()
//                    : "Unknown error"));
            showAIError("AI Priority Analysis", analysisTask.getException());
        });

        Thread thread = new Thread(analysisTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void getAITaskBreakdown(Button breakdownButton) {

        Task selected = taskTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showAlert("Please select a task first.");
            return;
        }

        breakdownButton.setDisable(true);
        breakdownButton.setText("Analysing...");

        javafx.concurrent.Task<String> aiTask =
                new javafx.concurrent.Task<>() {

                    @Override
                    protected String call() {

                        return geminiService.getTaskBreakdown(
                                selected.getTitle(),
                                selected.getCategory(),
                                selected.getDeadline().toString(),
                                selected.getPriority()
                        );
                    }
                };

        aiTask.setOnSucceeded(event -> {

            breakdownButton.setDisable(false);
            breakdownButton.setText("AI Task Breakdown");

            TextArea resultArea = new TextArea(
                    aiTask.getValue()
            );

            resultArea.setEditable(false);
            resultArea.setWrapText(true);
            resultArea.setPrefRowCount(12);
            resultArea.setPrefColumnCount(55);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("AI Task Breakdown");
            alert.setHeaderText(
                    "Action Plan: " + selected.getTitle()
            );
            alert.getDialogPane().setContent(resultArea);
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();
        });

        aiTask.setOnFailed(event -> {

            breakdownButton.setDisable(false);
            breakdownButton.setText("AI Task Breakdown");

            Throwable error = aiTask.getException();

            error.printStackTrace();

            showAIError("AI Task Breakdown",error);
        });

        Thread thread = new Thread(aiTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showAIError(String featureName, Throwable error) {

        if (error != null) {
            error.printStackTrace();
        }

        String details = "";

        if (error != null && error.getMessage() != null) {
            details = error.getMessage().toLowerCase();
        }

        String message;

        if (details.contains("429")
                || details.contains("quota")
                || details.contains("resource_exhausted")) {

            message = "Gemini usage limit reached. Please try again later.";

        } else if (details.contains("401")
                || details.contains("403")
                || details.contains("unauthenticated")
                || details.contains("permission_denied")
                || details.contains("api key")) {

            message = "Gemini authentication failed. Please check your API key and permissions.";

        } else if (details.contains("503")
                || details.contains("502")
                || details.contains("500")
                || details.contains("unavailable")
                || details.contains("server")) {

            message = "Gemini server is temporarily unavailable. Please try again later.";

        } else if (details.contains("unknownhost")
                || details.contains("socket")
                || details.contains("timeout")
                || details.contains("connect")
                || details.contains("network")) {

            message = "Unable to connect to Gemini. Please check your internet connection.";

        } else {

            message = "An unexpected error occurred while contacting Gemini. Please try again.";
        }

        //showAlert(featureName + " failed:\n\n" + message);
        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle("AI Service");
        alert.setHeaderText(featureName + " Unavailable");
        alert.setContentText(message);

        // Increase dialog width
        alert.getDialogPane().setPrefWidth(550);
        alert.getDialogPane().setMinWidth(550);

        alert.showAndWait();
    }
}