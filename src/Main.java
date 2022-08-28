import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    private Simulation sim;
    private Scene scene;
    private Button zoombutton;
    private Button zoomoutbutton;
    private Button move_x_plus;
    private Button move_x_minus;
    private Button move_y_plus;
    private Button move_y_minus;
    private Button increase_iteration;
    private Button decrease_iteration;
    private Button save_image;
    private double zoom;
    private double xMove;
    private double yMove;
    private int max;
    private static int num_t = Runtime.getRuntime().availableProcessors()-1;
    private static boolean run_gui = true;
    private int step = 1000;
    private int n = 0;
    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        if(run_gui) {
            zoombutton = new Button();
            zoombutton.setText("+");
            zoombutton.setTextFill(Color.ORANGERED);
            zoombutton.setMinWidth(30);
            zoombutton.setMinHeight(30);
            zoombutton.setStyle("-fx-background-color: #ffffff;");
            zoombutton.setOnAction(e -> zoom_use());
            zoomoutbutton = new Button();
            zoomoutbutton.setText("-");
            zoomoutbutton.setOnAction(e -> zoomout_use());
            zoomoutbutton.setTextFill(Color.ORANGERED);
            zoomoutbutton.setMinWidth(30);
            zoomoutbutton.setMinHeight(30);
            zoomoutbutton.setStyle("-fx-background-color: #ffffff;");
            move_x_plus = new Button("▷");
            move_x_plus.setOnAction(e -> x_moveplus());
            move_x_plus.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 0 0 0 0; -fx-font-size: 20;");
            move_x_plus.setMinHeight(30);
            move_x_minus = new Button("◁");
            move_x_minus.setOnAction(e -> x_moveminus());
            move_x_minus.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 0 0 0 0; -fx-font-size: 20;");
            move_x_minus.setMinHeight(30);
            move_y_plus = new Button("△");
            move_y_plus.setOnAction(e -> y_moveplus());
            move_y_plus.setMinWidth(130);
            move_y_plus.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5 5 0 0;");
            move_y_minus = new Button("▽");
            move_y_minus.setOnAction(e -> y_moveminus());
            move_y_minus.setMinWidth(130);
            move_y_minus.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 0 0 5 5;");
            increase_iteration = new Button("Iteration+");
            increase_iteration.setOnAction(e -> inc_iteration());
            increase_iteration.setMinWidth(80);
            increase_iteration.setMinHeight(37);
            increase_iteration.setStyle("-fx-background-color: #ffffff;");
            decrease_iteration = new Button("Iteration-");
            decrease_iteration.setOnAction(e -> dec_iteration());
            decrease_iteration.setMinWidth(80);
            decrease_iteration.setMinHeight(37);
            decrease_iteration.setStyle("-fx-background-color: #ffffff;");
            save_image = new Button("Save");
            save_image.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 0 0 0 0;");
            save_image.setTextFill(Color.ORANGERED);
            sim = new Simulation(800, 600, 100, 0, 0, 1);
            save_image.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sim.save_img(primaryStage);
                }
            });
            VBox box = new VBox(10, zoombutton, zoomoutbutton);
            HBox hboxtop = new HBox(10, move_y_plus/*, increase_iteration*/);
            HBox hboxbot = new HBox(10, move_y_minus/*, decrease_iteration*/);
            VBox vbox = new VBox(3, increase_iteration, decrease_iteration);
            Group group = new Group(box);
            BorderPane border = new BorderPane();
            border.setLeft(move_x_minus);
            border.setCenter(save_image);
            border.setRight(move_x_plus);
            border.setBottom(hboxbot);
            border.setTop(hboxtop);
            HBox menu = new HBox(10, border, vbox);
            Group move = new Group(menu);

            StackPane pane = new StackPane(sim, group, move);
            pane.setAlignment(group, Pos.TOP_RIGHT);
            pane.setAlignment(move, Pos.BOTTOM_RIGHT);
            pane.setPadding(new Insets(10, 20, 10, 20));
            scene = new Scene(pane, 800, 600);

            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                sim.setWidth(scene.getWidth());
                sim.setWidthG((int)scene.getWidth());
            });
            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                sim.setHeight(scene.getHeight());
                sim.setHeightG((int)scene.getHeight());
            });

            primaryStage.setScene(scene);
            primaryStage.setTitle("Mandelbrot Set by Sebastijan Perusko");
            primaryStage.show();

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //sim.start();
                    sim.runParallel(num_t);
                }
            });
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    System.exit(0);
                }
            });
        } else {
            //Testing sequential and parallel
            for (int i = 0; i < 100; i++) {
                n += step;
                sim = new Simulation(n, n, 100, 0, 0, 1);
                ExecutorService executor_Service = Executors.newSingleThreadExecutor();
                executor_Service.submit(new Runnable() {
                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        sim.runsequentialTest();
                        System.out.println(n + "," + (System.currentTimeMillis() - start) + ",sequential");
                    }
                });
                executor_Service.shutdown();
                executor_Service.awaitTermination(1000, TimeUnit.DAYS);
                Simulation sim2 = new Simulation(n, n, 100, 0, 0, 1);
                executor_Service = Executors.newSingleThreadExecutor();
                executor_Service.submit(new Runnable() {
                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        sim2.runParallelTest();
                        System.out.println(n + "," + (System.currentTimeMillis() - start) + ",parallel");
                    }
                });
                executor_Service.shutdown();
                executor_Service.awaitTermination(1000, TimeUnit.DAYS);
            }
        }

    }

    public static void main(String[] args) {
        if(args.length > 0 && !args[0].equals("n")){
            num_t = Integer.parseInt(args[0]);
        } else {
            num_t = Runtime.getRuntime().availableProcessors()-1;
        }
        if(args.length > 1 && !args[1].equals("true")){
            run_gui = false;
        } else {
            run_gui = true;
        }
        launch(args);
    }


    public void zoom_use(){
        sim.setZoom(2 * sim.getZoom());
    }
    public void zoomout_use(){
        sim.setZoom(sim.getZoom() / 2);
    }
    public void x_moveplus(){
        double mov = sim.getxMove() + 1/(10.0*sim.getZoom());
        sim.setxMove(mov);
    }
    public void x_moveminus(){
        double mov = sim.getxMove() - 1/(10.0*sim.getZoom());
        sim.setxMove(mov);
    }
    public void y_moveplus(){
        double mov = sim.getyMove() - 1/(10.0*sim.getZoom());
        sim.setyMove(mov);
    }
    public void y_moveminus(){
        double mov = sim.getyMove() + 1/(10.0*sim.getZoom());
        sim.setyMove(mov);
    }

    public void inc_iteration(){
        sim.setMax(sim.getMax() * 2);
    }

    public void dec_iteration(){
        sim.setMax(sim.getMax() / 2);
    }
}
