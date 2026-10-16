import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;

/** VIEW: builds the colourful JavaFX dashboard. Logic is in QuizController. */
public class QuizApp extends Application {

    // nodes the controller needs
    Scene scene;
    StackPane content = new StackPane();
    VBox playView, boardView, helpView, setupCard, gameBox, p1Card, p2Card;
    TextField p1Name, p2Name;
    ComboBox<String> category, mode;
    Button startBtn, quitBtn, navPlay, navBoard, navHelp;
    Label p1Label, p2Label, p1Score, p2Score, timerLabel, qCounter, questionText, status;
    Label statTotal, statGames, statHigh, statWinner;
    Button[] opts = new Button[4];
    ProgressBar timerBar;
    TableView<ScoreRow> table;
    QuizController controller;

    @Override
    public void start(Stage stage) {
        Db.init();

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());

        VBox main = new VBox(18);
        main.setPadding(new Insets(22));
        content.getChildren().addAll(buildPlayView(), buildBoardView(), buildHelpView());
        main.getChildren().addAll(buildStats(), content);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.setCenter(main);

        scene = new Scene(root, 1180, 740);
        File css = new File("quiz.css");
        if (css.exists()) scene.getStylesheets().add(css.toURI().toString());

        controller = new QuizController(this);
        show(playView);

        stage.setTitle("Quiz Battle");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.show();
    }

    // ---------------------------------------------------------------- sidebar
    private VBox buildSidebar() {
        Label logo = new Label("QUIZ\nBATTLE");
        logo.getStyleClass().add("logo");
        Label tag = new Label("Two players. One keyboard.");
        tag.getStyleClass().add("tagline");

        navPlay = nav("Play", () -> show(playView));
        navBoard = nav("Leaderboard", () -> { controller.refreshBoard(); show(boardView); });
        navHelp = nav("How to play", () -> show(helpView));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label foot = new Label("JavaFX + SQLite (JDBC)");
        foot.getStyleClass().add("tagline");

        VBox box = new VBox(14, logo, tag, new Region(), navPlay, navBoard, navHelp, spacer, foot);
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(230);
        return box;
    }

    private Button nav(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        b.setOnAction(e -> action.run());
        b.setFocusTraversable(false);
        return b;
    }

    // ------------------------------------------------------------- stat cards
    private HBox buildStats() {
        statTotal = new Label("0"); statGames = new Label("0");
        statHigh = new Label("0");  statWinner = new Label("-");
        HBox row = new HBox(16,
                card("QUESTIONS IN DATABASE", statTotal, "card-blue"),
                card("GAMES PLAYED", statGames, "card-green"),
                card("HIGHEST SCORE", statHigh, "card-orange"),
                card("LAST WINNER", statWinner, "card-pink"));
        return row;
    }

    private VBox card(String title, Label value, String colorClass) {
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        value.getStyleClass().add("card-value");
        VBox v = new VBox(4, t, value);
        v.getStyleClass().addAll("card", colorClass);
        v.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    // -------------------------------------------------------------- play view
    private VBox buildPlayView() {
        // ---- setup card
        Label head = new Label("Start a new battle");
        head.getStyleClass().add("setup-title");

        p1Name = new TextField("Player 1");
        p2Name = new TextField("Player 2");
        p1Name.getStyleClass().add("field");
        p2Name.getStyleClass().add("field");
        category = new ComboBox<>();
        mode = new ComboBox<>();
        mode.getItems().addAll("2 Players (Keyboard)", "Solo (Mouse + Keyboard)");
        mode.getSelectionModel().selectFirst();
        category.setMaxWidth(Double.MAX_VALUE);
        mode.setMaxWidth(Double.MAX_VALUE);
        category.getStyleClass().add("field");
        mode.getStyleClass().add("field");

        GridPane g = new GridPane();
        g.setHgap(14); g.setVgap(12);
        ColumnConstraints c0 = new ColumnConstraints(110);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0, c1);
        g.addRow(0, formLabel("Player 1"), p1Name);
        g.addRow(1, formLabel("Player 2"), p2Name);
        g.addRow(2, formLabel("Category"), category);
        g.addRow(3, formLabel("Mode"), mode);

        startBtn = new Button("START BATTLE");
        startBtn.getStyleClass().add("start-btn");
        startBtn.setMaxWidth(Double.MAX_VALUE);

        setupCard = new VBox(18, head, g, startBtn);
        setupCard.getStyleClass().add("setup-card");
        setupCard.setMaxWidth(560);

        // ---- game box
        p1Label = new Label("Player 1"); p1Score = new Label("0");
        p2Label = new Label("Player 2"); p2Score = new Label("0");
        p1Card = playerCard(p1Label, p1Score, "p1-card");
        p2Card = playerCard(p2Label, p2Score, "p2-card");

        timerLabel = new Label("10");
        timerLabel.getStyleClass().add("timer");
        qCounter = new Label("Question 1 / 10");
        qCounter.getStyleClass().add("q-counter");
        VBox mid = new VBox(2, timerLabel, qCounter);
        mid.setAlignment(Pos.CENTER);
        mid.setPrefWidth(220);

        HBox top = new HBox(16, p1Card, mid, p2Card);
        top.setAlignment(Pos.CENTER);
        HBox.setHgrow(p1Card, Priority.ALWAYS);
        HBox.setHgrow(p2Card, Priority.ALWAYS);

        timerBar = new ProgressBar(1);
        timerBar.setMaxWidth(Double.MAX_VALUE);
        timerBar.setPrefHeight(18);

        questionText = new Label("Question");
        questionText.setWrapText(true);
        questionText.getStyleClass().add("q-text");
        VBox qCard = new VBox(questionText);
        qCard.getStyleClass().add("q-card");
        qCard.setAlignment(Pos.CENTER);
        qCard.setMinHeight(120);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(16);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        grid.getColumnConstraints().addAll(half, half);
        for (int i = 0; i < 4; i++) {
            Button b = new Button();
            b.getStyleClass().addAll("opt", "opt-" + i);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setMinHeight(78);
            b.setWrapText(true);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setFocusTraversable(false);
            // mouse events: hover glow
            b.setOnMouseEntered(e -> b.setEffect(new Glow(0.45)));
            b.setOnMouseExited(e -> b.setEffect(null));
            GridPane.setHgrow(b, Priority.ALWAYS);
            opts[i] = b;
            grid.add(b, i % 2, i / 2);
        }

        status = new Label("");
        status.getStyleClass().add("status");
        quitBtn = new Button("Quit game");
        quitBtn.getStyleClass().add("quit-btn");
        quitBtn.setFocusTraversable(false);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox bottom = new HBox(10, status, sp, quitBtn);
        bottom.setAlignment(Pos.CENTER_LEFT);

        gameBox = new VBox(14, top, timerBar, qCard, grid, bottom);
        gameBox.setVisible(false);
        gameBox.setManaged(false);

        playView = new VBox(setupCard, gameBox);
        playView.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(gameBox, Priority.ALWAYS);
        return playView;
    }

    private Label formLabel(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("form-label");
        return l;
    }

    private VBox playerCard(Label name, Label score, String cls) {
        name.getStyleClass().add("p-name");
        score.getStyleClass().add("p-score");
        VBox v = new VBox(2, name, score);
        v.getStyleClass().addAll("card", cls);
        v.setAlignment(Pos.CENTER);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    // ------------------------------------------------------------ leaderboard
    private VBox buildBoardView() {
        Label head = new Label("Leaderboard  -  Top 20 games");
        head.getStyleClass().add("view-title");

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No games played yet. Play a battle first!"));
        TableColumn<ScoreRow, String> dateCol = col("Date", r -> r.date);
        dateCol.setMinWidth(150);
        table.getColumns().add(dateCol);
        table.getColumns().add(col("Player 1", r -> r.p1));
        table.getColumns().add(col("Score", r -> String.valueOf(r.s1)));
        table.getColumns().add(col("Player 2", r -> r.p2));
        table.getColumns().add(col("Score", r -> String.valueOf(r.s2)));
        table.getColumns().add(col("Winner", r -> r.winner));
        table.getColumns().add(col("Category", r -> r.category));
        VBox.setVgrow(table, Priority.ALWAYS);

        boardView = new VBox(14, head, table);
        return boardView;
    }

    private TableColumn<ScoreRow, String> col(String title, java.util.function.Function<ScoreRow, String> f) {
        TableColumn<ScoreRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(f.apply(cd.getValue())));
        return c;
    }

    // ------------------------------------------------------------------- help
    private VBox buildHelpView() {
        Label head = new Label("How to play");
        head.getStyleClass().add("setup-title");
        Label body = new Label(
                "1.  Enter the names, choose a category and a mode, then press START BATTLE.\n\n"
              + "2.  A question with four options appears. You have 10 seconds.\n\n"
              + "3.  Player 1 keys:  A  S  D  F          Player 2 keys:  J  K  L  ;\n"
              + "     (Solo mode: click an option with the mouse, or use A S D F)\n\n"
              + "4.  First correct answer wins the round. Faster answer = more points (6 to 15).\n\n"
              + "5.  A wrong answer costs 2 points and locks you out of that question.\n\n"
              + "6.  After 10 questions the winner is announced and the result is saved in the database.");
        body.setWrapText(true);
        body.getStyleClass().add("help-text");
        helpView = new VBox(18, head, body);
        helpView.getStyleClass().add("setup-card");
        helpView.setMaxWidth(760);
        VBox wrap = new VBox(helpView);
        wrap.setAlignment(Pos.TOP_CENTER);
        // return wrapper but keep reference to the wrapper for show()
        helpView = wrap;
        return wrap;
    }

    // ---------------------------------------------------------------- helpers
    void show(Node view) {
        for (Node n : content.getChildren()) {
            n.setVisible(n == view);
            n.setManaged(n == view);
        }
        setActive(navPlay, view == playView);
        setActive(navBoard, view == boardView);
        setActive(navHelp, view == helpView);
    }

    private void setActive(Button b, boolean on) {
        b.getStyleClass().remove("nav-active");
        if (on) b.getStyleClass().add("nav-active");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
