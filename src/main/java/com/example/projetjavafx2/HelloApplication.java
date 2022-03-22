package com.example.projetjavafx2;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

import org.json.simple.JSONObject;


public class HelloApplication extends Application {


    static final int WIDTH = 1000;
    static final int HEIGHT = 800;
    static int scoreint = 0;
    int indexActiveSceneGame = 0;

    Circle pacMan;
    VBox vboxMenu;
    Stage primaryStage;
    Timeline tl;
    boolean gamePaused = true;
    boolean pacManAttack = false;
    Timer timerCherry = new Timer();

    Scene sceneGameInitial;
    Scene sceneMenu;
    Scene sceneHighScorePage;

    Group groupMenu;
    Group groupGameInitial;
    Group groupHighScorePage;

    Button buttonGoToHighScorePage;
    Button buttonStartMainMenu;
    Button buttonReturnToMenu;
    Button buttonReturnToGameFromPause;
    Button buttonPauseSceneGame;

    Text scoreInGame;
    Text highscores;
    TextArea getIdPlayer;
    String idPlayer;
    JSONObject highScoreData;
    HashMap<String, Integer> highScores;
    TableView tableHighScore;
    ArrayList<Map<String, Object>> scores;
    ArrayList<Map<String, Object>> itemsScore;

    List<Circle> listCherry;
    List<Circle> listGhost;
    List<Circle> listPoint;
    List<Scene> listScenesGame;
    List<Group> listGroupGame;

    @Override
    public void start(Stage stage) throws Exception{
        listScenesGame = new ArrayList<Scene>();
        listGroupGame = new ArrayList<Group>();


        primaryStage = stage;
        primaryStage.setTitle("Pacman");


        //CREATION DU MENU
        groupMenu = new Group();
        buttonStartMainMenu = new Button("Start new GAME!");
        buttonGoToHighScorePage = new Button("Go to highscore!");
        buttonReturnToGameFromPause = new Button("Return to game");
        getIdPlayer = new TextArea();
        getIdPlayer.setPrefHeight(100);
        getIdPlayer.setPrefWidth(100);


        groupMenu.setLayoutX(WIDTH/2);
        groupMenu.setLayoutY(HEIGHT/2);
        vboxMenu = new VBox(buttonStartMainMenu, buttonGoToHighScorePage, getIdPlayer);
        groupMenu.getChildren().add(vboxMenu);


        sceneMenu = new Scene(groupMenu, WIDTH, HEIGHT, Color.BLACK);
        Image imLaurier = new Image("https://i.gifer.com/origin/64/649852e53b7e4edf15ea1c2f23a61f29_w200.gif",false);
        sceneMenu.setFill(new ImagePattern(imLaurier));

        primaryStage.setScene(sceneMenu);
        //----------------------------------------------------------------------

        //SCENE GAME DEFINITION
        Rectangle rContour = new Rectangle(0,0 ,WIDTH,HEIGHT);
        rContour.setFill(Color.TRANSPARENT);
        rContour.setStroke(Color.PURPLE);
        rContour.setStrokeWidth(10);
        groupGameInitial = initializeGroupGame();
        listGroupGame.add(groupGameInitial);

        sceneGameInitial = new Scene(groupGameInitial, WIDTH, HEIGHT, Color.BLACK);
        listScenesGame.add(sceneGameInitial);

        tl = new Timeline(new KeyFrame(Duration.millis(250), e -> run()));
        tl.setCycleCount(Timeline.INDEFINITE);


        handleGameEvent();

        //--------------------------------------------------------------------

        //HighScorePage
        buttonReturnToMenu = new Button("Return to menu!");
        highscores = new Text();
        highscores.setX(200);
        highscores.setY(200);
        highscores.setFill(Color.WHITE);
        groupHighScorePage = new Group();
        groupHighScorePage.getChildren().add(buttonReturnToMenu);
        groupHighScorePage.getChildren().add(highscores);
        tableHighScore = new TableView();
        scores = new ArrayList<Map<String, Object>>();

        TableColumn<Map, String> col1 = new TableColumn<>("userName");
        col1.setCellValueFactory(new MapValueFactory<>("userName"));

        TableColumn<Map, String> col2 = new TableColumn<>("score");
        col2.setCellValueFactory(new MapValueFactory<>("score"));

        scores.addAll(getInfoFromFile());

        for (Map<String, Object> item:scores) {
            tableHighScore.getItems().addAll(item);
        }

        tableHighScore.getColumns().add(col1);
        tableHighScore.getColumns().add(col2);

        tableHighScore.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableHighScore.setLayoutX(WIDTH/4);
        tableHighScore.setLayoutY(HEIGHT/4);
        groupHighScorePage.getChildren().add(tableHighScore);

        sceneHighScorePage = new Scene(groupHighScorePage, WIDTH, HEIGHT, Color.BLACK);
        sceneHighScorePage.setFill(new ImagePattern(imLaurier));

        primaryStage.show();

        //---------------------------------------------------------------------------------------



        //Gestion des boutons

        //Bouton pour lancer le jeu
        buttonStartMainMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //TODO init game scene
                primaryStage.setScene(listScenesGame.get(indexActiveSceneGame));
                gamePaused = false;
                tl.play();
            }
        });


        //Boutton qui s'affiche uniquement une fois qu'on a lancé une partie et qu'on a fait pause
        //il s'affiche donc sur le menu et permet de relancer la partie en cours
        buttonReturnToGameFromPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(listScenesGame.get(indexActiveSceneGame));
                gamePaused = false;
                tl.play();
                vboxMenu.getChildren().remove(buttonReturnToGameFromPause);
            }
        });


        //Bouton pour acceder au Highscore depuis le menu
        buttonGoToHighScorePage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dataInHashMap();
                primaryStage.setScene(sceneHighScorePage);
                gamePaused = true;
                tl.pause();
            }
        });


        //Pour retourner au menu depuis le jeu
        buttonReturnToMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(sceneMenu);
                gamePaused = true;
                tl.pause();
            }
        });

        //Permet de mettre le jeu en pause et de retourner au menu
        buttonPauseSceneGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(sceneMenu);
                gamePaused = true;
                tl.pause();
                vboxMenu.getChildren().add(buttonReturnToGameFromPause);
            }
        });

    }

    //------------------------------------------------------------------------------



    private void handleGameEvent() {

        //GESTION DES DEPLACEMENTS AVEC ZQSD ET DES TOUCHES EN GENERAL
        listScenesGame.get(indexActiveSceneGame).setOnKeyPressed((KeyEvent event) -> {
            if (event.getText().isEmpty())
                return;
            char keyEntered = event.getText().toUpperCase().charAt(0);

            boolean isMouvOk = !gamePaused;
            switch (keyEntered){
                case 'Z' :
                    pacMan.setRotate(-90);

                    for (Node node : listGroupGame.get(indexActiveSceneGame).getChildren()) {
                        if( node instanceof Rectangle){
                            Rectangle r = ((Rectangle) node);
                            if((pacMan.getCenterX()>= r.getX() && pacMan.getCenterX()<=r.getX()+r.getWidth())){
                                if(pacMan.getCenterY()- pacMan.getRadius() <= r.getY() + r.getHeight() && pacMan.getCenterY()>=r.getY()){
                                    isMouvOk = false;
                                }
                            }
                        }
                    }
                    if(isMouvOk){
                        if (pacMan.getCenterY() <= 0) {
                            pacMan.setCenterY(HEIGHT + pacMan.getRadius());
                        }

                        isNextPositionAPoint(listGroupGame.get(indexActiveSceneGame), listPoint, pacMan, scoreInGame);
                        pacMan.setCenterY(pacMan.getCenterY() - pacMan.getRadius());
                    }
                    break;
                case 'S' :
                    pacMan.setRotate(90);

                    for (Node node : listGroupGame.get(indexActiveSceneGame).getChildren()) {
                        if( node instanceof Rectangle){
                            Rectangle r = ((Rectangle) node);
                            if((pacMan.getCenterX()>= r.getX() && pacMan.getCenterX()<=r.getX()+r.getWidth())){
                                if(pacMan.getCenterY() <= r.getY() + r.getHeight() &&
                                        pacMan.getCenterY()+ pacMan.getRadius()>=r.getY()){
                                    System.out.println("Bam");
                                    isMouvOk = false;
                                }
                            }
                        }
                    }
                    if(isMouvOk) {
                        if (pacMan.getCenterY() >= HEIGHT) {
                            pacMan.setCenterY(0 - pacMan.getRadius());
                        }
                        isNextPositionAPoint(listGroupGame.get(indexActiveSceneGame), listPoint, pacMan, scoreInGame);
                        pacMan.setCenterY(pacMan.getCenterY() + pacMan.getRadius());
                    }
                    break;
                case 'Q' :
                    pacMan.setRotate(180);

                    for (Node node : listGroupGame.get(indexActiveSceneGame).getChildren()) {
                        if( node instanceof Rectangle){
                            Rectangle r = ((Rectangle) node);
                            if(pacMan.getCenterY() <= r.getY() + r.getHeight() &&
                                    pacMan.getCenterY()>=r.getY()){
                                if((pacMan.getCenterX()>= r.getX() && pacMan.getCenterX()- pacMan.getRadius()<=r.getX()+r.getWidth())){
                                    isMouvOk = false;
                                }
                            }
                        }
                    }
                    if(isMouvOk) {
                        if (pacMan.getCenterX() <= 0) {
                            pacMan.setCenterX(WIDTH + pacMan.getRadius());
                        }
                        isNextPositionAPoint(listGroupGame.get(indexActiveSceneGame), listPoint, pacMan, scoreInGame);
                        pacMan.setCenterX(pacMan.getCenterX() - pacMan.getRadius());
                    }
                    break;
                case 'D' :
                    pacMan.setRotate(0);


                    for (Node node : listGroupGame.get(indexActiveSceneGame).getChildren()) {
                        if( node instanceof Rectangle){
                            Rectangle r = ((Rectangle) node);
                            if(pacMan.getCenterY() <= r.getY() + r.getHeight() &&
                                    pacMan.getCenterY()>=r.getY()){
                                if((pacMan.getCenterX()+ pacMan.getRadius()>= r.getX() && pacMan.getCenterX()- pacMan.getRadius()<=r.getX())){
                                    isMouvOk = false;
                                }
                            }
                        }
                    }
                    if(isMouvOk) {
                        if (pacMan.getCenterX() >= WIDTH) {
                            pacMan.setCenterX(0 - pacMan.getRadius());
                        }
                        isNextPositionAPoint(listGroupGame.get(indexActiveSceneGame), listPoint, pacMan, scoreInGame);
                        pacMan.setCenterX(pacMan.getCenterX() + pacMan.getRadius());
                    }
                    break;

                    //Retourner au menu
                case 'X' :
                    primaryStage.setScene(sceneMenu);
                    break;

                    //mettre le jeu en pause
                case 'P':
                    if(tl.getStatus() == Animation.Status.RUNNING){
                        tl.pause();
                        gamePaused = true;
                    }
                    else if(tl.getStatus() == Animation.Status.PAUSED){
                        tl.play();
                        gamePaused = false;
                    }

            }
        });

        //-------------------------------------------------------------------------------------------

    }


    //Section des affiches, c'est ici qu'on paramètre tout ce que l'on va afficher à l'écran lorsqu'on lance le jeu
    private Group initializeGroupGame() {

        //affichage des obstacles
        Group group = new Group();
        group.getChildren().add(createObstacleOnScene(100,100, 100, 100));
        group.getChildren().add(createObstacleOnScene(800,100, 100, 100));
        group.getChildren().add(createObstacleOnScene(100,600, 100, 100));
        group.getChildren().add(createObstacleOnScene(800,600, 100, 100));
        group.getChildren().add(createObstacleOnScene(400, 300, 200, 200));


        //affichage du bouton pause
        buttonPauseSceneGame = new Button("Pause The GAME!");
        group.getChildren().add(buttonPauseSceneGame);


        //Affichage du score
        scoreInGame = new Text(WIDTH - 50,25, String.valueOf(scoreint));
        scoreInGame.setFill(Color.WHITE);
        group.getChildren().add(scoreInGame);

        //affichage de Pacman
        pacMan = new Circle(WIDTH/2,50, 25);
        Image im = new Image("https://i.gifer.com/origin/64/649852e53b7e4edf15ea1c2f23a61f29_w200.gif",false);
        pacMan.setFill(new ImagePattern(im));
        group.getChildren().add(pacMan);


        //affichage des fantômes
        Circle ghost1 = new Circle(700, 700, 25, Color.RED);
        Circle ghost2 = new Circle(700, 700, 25, Color.RED);
        Circle ghost3 = new Circle(700, 700, 25, Color.RED);
        Circle ghost4 = new Circle(700, 700, 25, Color.RED);
        Circle ghost5 = new Circle(700, 700, 25, Color.RED);

        listGhost = new ArrayList<Circle>();
        listGhost.add(ghost1);
        listGhost.add(ghost2);
        listGhost.add(ghost3);
        listGhost.add(ghost4);
        listGhost.add(ghost5);

        group.getChildren().add(ghost1);
        group.getChildren().add(ghost2);
        group.getChildren().add(ghost3);
        group.getChildren().add(ghost4);
        group.getChildren().add(ghost5);


        //affichage des cerises
        Circle cherry = new Circle(250, 250, 10, Color.PINK);
        group.getChildren().add(cherry);
        listCherry = new ArrayList<>();
        listCherry.add(cherry);


        //affichages des points à l'écran
        listPoint = new ArrayList<Circle>();
        Circle point = new Circle(50, 50, 10, Color.GAINSBORO);
        listPoint.add(point);
        group.getChildren().add(listPoint.get(0));

        return group;
    }

    private void run() {

        //Gestion de l'attaque des fantômes
        Random r = new Random();
        Circle tempGhostToRemove = null;
        for (Circle ghost:listGhost ) {
            boolean chaseOn = false;
            if((Math.abs(ghost.getCenterX() - pacMan.getCenterX()) +
                    Math.abs(ghost.getCenterY() - pacMan.getCenterY())) < 500) {
                chaseOn = true;
            }

            if(chaseOn){
                double difX = ghost.getCenterX() - pacMan.getCenterX();
                double difY = ghost.getCenterY() - pacMan.getCenterY();
                if(Math.abs(difX) < Math.abs(difY)){
                    if(difY>0)  ghost.setCenterY(ghost.getCenterY()-25);
                    else ghost.setCenterY(ghost.getCenterY()+25);
                }
                else {
                    if(difX>0)  ghost.setCenterX(ghost.getCenterX()-25);
                    else ghost.setCenterX(ghost.getCenterX()+25);
                }
            }
            else {
                switch (r.nextInt(4)) {
                    case 0:
                        if (ghost.getCenterX() + 25 < WIDTH) {
                            ghost.setCenterX(ghost.getCenterX() + 25);
                        }
                        break;
                    case 1:
                        if (ghost.getCenterX() - 25 > 0) {
                            ghost.setCenterX(ghost.getCenterX() - 25);
                        }
                        break;
                    case 2:
                        if (ghost.getCenterY() - 25 > 0) {
                            ghost.setCenterY(ghost.getCenterY() - 25);
                        }
                        break;
                    case 3:
                        if (ghost.getCenterY() + 25 < HEIGHT) {
                            ghost.setCenterY(ghost.getCenterY() + 25);
                        }
                        break;
                }
            }
            if(ghost.getCenterX()== pacMan.getCenterX() && ghost.getCenterY()== pacMan.getCenterY()){

                if(!pacManAttack) {
                    tl.pause();
                    gamePaused = true;
                    addScoreToHighScore();

                    primaryStage.setScene(sceneHighScorePage);

                    System.out.println("Game Over!");
                }
                else{
                    scoreint = scoreint + 10;
                    scoreInGame.setText(String.valueOf(scoreint));
                    listGroupGame.get(indexActiveSceneGame).getChildren().remove(ghost);
                    tempGhostToRemove = ghost;
                }
            }
        }
        if(tempGhostToRemove != null){
            listGhost.remove(tempGhostToRemove);
        }


        if(listPoint.size() == 0){
            System.out.println("Bravo!");
            addScoreToHighScore();

            indexActiveSceneGame++;
            listGroupGame.add(initializeGroupGame());

            listScenesGame.add(new Scene(listGroupGame.get(indexActiveSceneGame), WIDTH, HEIGHT, Color.BLACK));
            primaryStage.setScene(listScenesGame.get(indexActiveSceneGame));
            gamePaused = false;
            tl.play();
            handleGameEvent();
        }
    }


    //Ajouter les scores au highscore
    private void addScoreToHighScore() {
        idPlayer = getIdPlayer.getText();
        HashMap<String, Object> toAdd = new HashMap<>();
        toAdd.put("userName", idPlayer);
        toAdd.put("score", scoreint);
        scores.add(toAdd);
        tableHighScore.getItems().add(toAdd);


    }



//placer les points partout sur la map
    private void isNextPositionAPoint(Group group, List<Circle> listPoint, Circle pacman, Text score) {
        Circle pointTempToRemove = null;
        for (Circle point : listPoint) {
            if(point.getCenterX() == pacman.getCenterX() && point.getCenterY() == pacman.getCenterY()){
                pointTempToRemove = point;
                group.getChildren().remove(point);
                scoreint++;
                score.setText(String.valueOf(scoreint));
            }
        }
        if(pointTempToRemove!=null) {
            listPoint.remove(pointTempToRemove);
        }
        for (Circle cerise : listCherry) {
            if(cerise.getCenterX() == pacman.getCenterX() && cerise.getCenterY() == pacman.getCenterY()){
                pointTempToRemove = cerise;
                group.getChildren().remove(cerise);
                pacManAttack = true;
                timerCherry.schedule(task, 10000L);
                System.out.println("pacManVorace : " + pacManAttack);
            }
        }
        if(pointTempToRemove!=null) {
            listCherry.remove(pointTempToRemove);
        }
    }

    //Gestion du timer
    TimerTask task = new TimerTask() {
        public void run() {
            pacManAttack = false;
            System.out.println("pacManVorace : " + pacManAttack);
        }
    };

    public Rectangle createObstacleOnScene(int x, int y, int width, int heigth){
        Rectangle r = new Rectangle(x, y, width, heigth);
        r.setFill(Color.GREEN);
        return r;
    }

    public boolean isMouvementAllowed(Group group, Circle pacman){
        for (Node node : group.getChildren()) {

        }

        return false;
    }



    //envoyer des infos
    public ArrayList<Map<String, Object>> getInfoFromFile()  {
        Map<String, Object> item1 = new HashMap<>();

        Map<String, Object> item2 = new HashMap<>();

        ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        items.add(item1);
        items.add(item2);

        return items;
    }

    public void dataInHashMap(){
        highScores = new HashMap<String, Integer>();
    }

    public static void main(String[] args) {


        launch(args);
    }


}
