import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.List;

/** CONTROLLER: handles events (key + mouse), game rules, and talks to the DAOs. */
public class QuizController {

    static final double LIMIT = 10.0;   // seconds per question
    static final int ROUNDS = 10;       // questions per game
    static final KeyCode[] P1 = {KeyCode.A, KeyCode.S, KeyCode.D, KeyCode.F};
    static final KeyCode[] P2 = {KeyCode.J, KeyCode.K, KeyCode.L, KeyCode.SEMICOLON};
    static final String[] P1_TXT = {"A", "S", "D", "F"};
    static final String[] P2_TXT = {"J", "K", "L", ";"};

    private final QuizApp v;
    private List<Question> qs;
    private int idx, s1, s2;
    private boolean running, inGame, solo, lock1, lock2;
    private double left;
    private String n1, n2, cat;
    private Timeline ticker;
    private PauseTransition pause;

    public QuizController(QuizApp view) {
        this.v = view;

        v.category.getItems().add("All");
        v.category.getItems().addAll(QuestionDAO.categories());
        v.category.getSelectionModel().selectFirst();

        v.mode.setOnAction(e -> {                       // Solo mode hides player 2 name
            boolean s = v.mode.getValue().startsWith("Solo");
            v.p2Name.setDisable(s);
        });

        v.scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);   // KEY EVENT
        v.startBtn.setOnAction(e -> startGame());
        v.quitBtn.setOnAction(e -> quit());
        for (int i = 0; i < 4; i++) {
            final int k = i;
            v.opts[i].setOnMouseClicked(e -> {                       // MOUSE EVENT
                if (running && solo) answer(1, k);
            });
        }

        ticker = new Timeline(new KeyFrame(Duration.millis(100), e -> tick()));
        ticker.setCycleCount(Timeline.INDEFINITE);

        refreshStats();
        refreshBoard();
    }

    // ------------------------------------------------------------- game flow
    private void startGame() {
        solo = v.mode.getValue().startsWith("Solo");
        n1 = clean(v.p1Name.getText(), "Player 1");
        n2 = solo ? "-" : clean(v.p2Name.getText(), "Player 2");
        cat = v.category.getValue();

        qs = QuestionDAO.load(cat, ROUNDS);
        if (qs.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No questions found for this category.").show();
            return;
        }
        qs.forEach(Question::shuffle);

        idx = 0; s1 = 0; s2 = 0; inGame = true;
        v.p1Label.setText(n1);
        v.p2Label.setText(n2);
        v.p2Card.setVisible(!solo);
        v.p2Card.setManaged(!solo);
        updateScores();

        v.setupCard.setVisible(false); v.setupCard.setManaged(false);
        v.gameBox.setVisible(true);    v.gameBox.setManaged(true);
        v.navPlay.setDisable(true); v.navBoard.setDisable(true); v.navHelp.setDisable(true);
        v.scene.getRoot().requestFocus();
        nextQuestion();
    }

    private void nextQuestion() {
        if (idx >= qs.size()) { finish(); return; }
        Question q = qs.get(idx);
        lock1 = false;
        lock2 = solo;                 // in solo mode player 2 does not play
        left = LIMIT;
        v.qCounter.setText("Question " + (idx + 1) + " / " + qs.size() + "   (" + q.category + ")");
        v.questionText.setText(q.text);
        for (int i = 0; i < 4; i++) {
            String keys = solo ? P1_TXT[i] : P1_TXT[i] + " / " + P2_TXT[i];
            v.opts[i].setText("[" + keys + "]   " + q.options[i]);
            v.opts[i].getStyleClass().removeAll("opt-correct", "opt-wrong");
        }
        v.status.setText(solo ? "Press A S D F or click an option."
                              : "Be fast!   " + n1 + ": A S D F      " + n2 + ": J K L ;");
        v.timerBar.setProgress(1);
        v.timerBar.getStyleClass().remove("danger");
        v.timerLabel.setText(String.valueOf((int) LIMIT));
        running = true;
        ticker.playFromStart();
    }

    private void tick() {
        left -= 0.1;
        v.timerBar.setProgress(Math.max(left, 0) / LIMIT);
        v.timerLabel.setText(String.valueOf((int) Math.ceil(Math.max(left, 0))));
        if (left <= 3 && !v.timerBar.getStyleClass().contains("danger"))
            v.timerBar.getStyleClass().add("danger");
        if (left <= 0.001) timeUp();
    }

    private void answer(int player, int choice) {
        if (!running) return;
        if ((player == 1 && lock1) || (player == 2 && lock2)) return;
        Question q = qs.get(idx);
        String who = player == 1 ? n1 : n2;

        if (choice == q.correct) {
            int pts = 5 + (int) Math.ceil(left);        // faster = more points
            if (player == 1) s1 += pts; else s2 += pts;
            running = false;
            ticker.stop();
            mark(q.correct, "opt-correct");
            v.status.setText(who + " got it!  +" + pts + " points");
            updateScores();
            next();
        } else {
            if (player == 1) { lock1 = true; s1 = Math.max(0, s1 - 2); }
            else             { lock2 = true; s2 = Math.max(0, s2 - 2); }
            mark(choice, "opt-wrong");
            v.status.setText(who + " answered wrong (-2) and is locked out.");
            updateScores();
            if (lock1 && lock2) { reveal("Both players missed it!"); }
        }
    }

    private void timeUp() { reveal("Time is up!"); }

    private void reveal(String msg) {
        running = false;
        ticker.stop();
        Question q = qs.get(idx);
        mark(q.correct, "opt-correct");
        for (int i = 0; i < 4; i++) if (i != q.correct) mark(i, "opt-wrong");
        v.status.setText(msg + "  Correct answer: " + q.options[q.correct]);
        next();
    }

    private void next() {
        pause = new PauseTransition(Duration.seconds(1.6));
        pause.setOnFinished(e -> { if (inGame) { idx++; nextQuestion(); } });
        pause.play();
    }

    private void finish() {
        inGame = false;
        running = false;
        String winner;
        if (solo) winner = n1;
        else winner = s1 > s2 ? n1 : (s2 > s1 ? n2 : "Draw");
        ScoreDAO.save(n1, n2, s1, s2, winner, cat);        // JDBC INSERT

        String msg = solo ? n1 + " scored " + s1 + " points."
                          : n1 + ": " + s1 + "      " + n2 + ": " + s2 + "\n\n"
                            + (winner.equals("Draw") ? "It's a draw!" : "Winner: " + winner);
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
            a.setTitle("Game over");
            a.setHeaderText("Battle finished");
            a.showAndWait();
            backToSetup();
        });
    }

    private void quit() {
        inGame = false; running = false;
        ticker.stop();
        if (pause != null) pause.stop();
        backToSetup();
    }

    private void backToSetup() {
        v.gameBox.setVisible(false);  v.gameBox.setManaged(false);
        v.setupCard.setVisible(true); v.setupCard.setManaged(true);
        v.navPlay.setDisable(false); v.navBoard.setDisable(false); v.navHelp.setDisable(false);
        refreshStats();
        refreshBoard();
    }

    // ---------------------------------------------------------------- events
    private void onKey(KeyEvent e) {
        if (!running) return;
        KeyCode k = e.getCode();
        for (int i = 0; i < 4; i++) {
            if (k == P1[i]) { answer(1, i); e.consume(); return; }
        }
        if (!solo) {
            for (int i = 0; i < 4; i++) {
                if (k == P2[i]) { answer(2, i); e.consume(); return; }
            }
        }
    }

    // --------------------------------------------------------------- helpers
    private void mark(int i, String cls) {
        java.util.List<String> c = v.opts[i].getStyleClass();
        if (!c.contains(cls)) c.add(cls);
    }

    private void updateScores() {
        v.p1Score.setText(String.valueOf(s1));
        v.p2Score.setText(String.valueOf(s2));
    }

    void refreshStats() {
        v.statTotal.setText(String.valueOf(QuestionDAO.count()));
        v.statGames.setText(String.valueOf(ScoreDAO.gamesPlayed()));
        v.statHigh.setText(String.valueOf(ScoreDAO.highScore()));
        v.statWinner.setText(ScoreDAO.lastWinner());
    }

    void refreshBoard() {
        v.table.getItems().setAll(ScoreDAO.top(20));
    }

    private String clean(String s, String def) {
        return (s == null || s.trim().isEmpty()) ? def : s.trim();
    }
}
