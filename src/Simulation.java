import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Simulation extends Canvas {

    GraphicsContext gc;
    PixelWriter pwm;
    private int max;
    private double xMove;
    private double yMove;
    private double zoom;
    private int width;
    private int height;
    private Color[] colors;
    int[] ArrPixel;

    public Simulation(int width, int height, int max, double xMove, double yMove, double zoom){
        super(width,height);
        this.max = max;
        this.xMove = xMove;
        this.yMove = yMove;
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.width = width;
        this.height = height;
        gc = this.getGraphicsContext2D();
        ColorsArray(max);

    }

    public void setMax(int max) {
        ColorsArray(max);
        this.max = max;
    }

    public void setxMove(double xMove) {
        this.xMove = xMove;
    }

    public void setyMove(double yMove) {
        this.yMove = yMove;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setWidthG(int width) {
        this.width = width;
        ArrPixel = new int[this.width * height];
    }

    public void setHeightG(int height) {
        this.height = height;
        ArrPixel = new int[width * this.height];
    }

    public int getMax() {
        return max;
    }

    public int getWidthS() {
        return width;
    }

    public int getHeightS() {
        return height;
    }

    public double getxMove() {
        return xMove;
    }

    public double getyMove() {
        return yMove;
    }

    public double getZoom() {
        return zoom;
    }

    public void start() {
        PixelWriter pw = gc.getPixelWriter();
        final WritablePixelFormat<IntBuffer> pixelFormat =
                PixelFormat.getIntArgbPreInstance();

        new AnimationTimer(){
            public void handle(long currentNanoTime){
                int[] buffer = new int[(width + 50) * height];
                /*double b = System.currentTimeMillis();*/
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        buffer[row * width + col] = mandelbrot(row, col);
                    }
                }
                pw.setPixels(0, 0, width, height, pixelFormat, buffer, 0, width);
                /*System.out.println("time: " + (System.currentTimeMillis() - b));*/
            }
        }.start();
    }

    public void runParallel(int nc){
        ArrPixel = new int[width * height];
        pwm = gc.getPixelWriter();
        final WritablePixelFormat<IntBuffer> pixelFormat =
                PixelFormat.getIntArgbPreInstance();
        int cores = nc;
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        int chunk = height / cores;
        Worker[] workers = new Worker[cores];
        for (int i = 0; i < cores; i++) {
            workers[i] = new Worker(i*chunk, i*chunk + chunk, chunk,this);
        }
        new AnimationTimer(){
            public void handle(long currentNanoTime){
                int chunk = height / cores;
                for (int i = 0; i < cores; i++) {
                    workers[i].setChunk(chunk);
                    workers[i].setStart(i * chunk);
                    if(i == cores - 1){
                        workers[i].setEnd(height);
                    } else {
                        workers[i].setEnd(i * chunk + chunk);
                    }
                    executorService.submit(workers[i]);
                }
                pwm.setPixels(0, 0, width, height, pixelFormat, ArrPixel, 0, width);
            }
        }.start();
    }

    public void runsequentialTest() {
        PixelWriter pw = gc.getPixelWriter();
        final WritablePixelFormat<IntBuffer> pixelFormat =
                PixelFormat.getIntArgbPreInstance();
        int[] buffer = new int[1000*1000];
        int count = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                /*buffer[(row * width + col) % 1000] = mandelbrot(row, col);*/
                buffer[Math.abs(count%1000)] = mandelbrot(row, col);
            }
        }
        pw.setPixels(0, 0, 1000, 1000, pixelFormat, buffer, 0, 1000);

    }

    public void runParallelTest(){
        ArrPixel = new int[1000*1000];
        pwm = gc.getPixelWriter();
        int cores = Runtime.getRuntime().availableProcessors()-1;
        CyclicBarrier barrier = new CyclicBarrier(cores, new Runnable() {
            @Override
            public void run() {
            }
        });
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        int chunk = height / cores;
        for (int i = 0; i < cores; i++) {
            executorService.submit(new Worker(i*chunk, i*chunk + chunk, chunk,this, barrier));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1000, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void ColorsArray(int max){
        List<Color> c = new ArrayList<Color>();
        double c1 = 32/(0.16*max);
        double c2 = 100/(0.16*max);
        double c3 = 103/(0.16*max);
        for(int i = 0; i < (int)(0.16*max);i++){
            c.add(Color.rgb((int)(c1*i), 7+(int)(c2*i), 100+(int)(c3*i)));
        }
        c1 = 205/(0.42*max);
        c2 = 148/(0.42*max);
        c3 = 52/(0.42*max);
        for(int i = (int)(0.16*max); i < (int)(0.42*max);i++){
            c.add(Color.rgb(32+(int)(c1*i), 107+(int)(c2*i), 203+(int)(c3*i)));
        }
        c1 = 18/(0.6425*max);
        c2 = -85/(0.6425*max);
        c3 = -255/(0.6425*max);
        for(int i = (int)(0.42*max); i < (int)(0.6425*max);i++){
            c.add(Color.rgb(237+(int)(c1*i), 255+(int)(c2*i), 255+(int)(c3*i)));
        }
        c1 = -255/(0.8575*max);
        c2 = -168/(0.8575*max);
        c3 = 0/(0.8575*max);
        for(int i = (int)(0.6425*max); i < (int)(0.8575*max);i++){
            c.add(Color.rgb(255+(int)(c1*i), 170+(int)(c2*i), (int)(c3*i)));
        }
        for(int i = (int)(0.8575*max); i < max;i++){
            c.add(Color.rgb(0, 2, 0));
        }

        colors = c.toArray(new Color[c.size()]);
    }

    public void save_img(Stage primaryStage){
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if(file != null){
            try {
                WritableImage writableImage = new WritableImage(width, height);
                this.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {

            }
        }
    }

    private int toInt(Color c) {
        return
                (255  << 24) |
                        ((int) (c.getRed()   * 255) << 16) |
                        ((int) (c.getGreen() * 255) << 8)  |
                        ((int) (c.getBlue()  * 255));
    }

    public int mandelbrot(int row, int col){
        double c_re = 0, c_im = 0;
        c_re = xMove + (col - width/2.0)*4.5/(width*zoom);
        c_im = yMove + (row - height/2.0)*4.5/(width*zoom);
        double x = 0, y = 0, x2 = 0, y2 = 0;
        int iteration = 0;
        while (x2 + y2 <= 4 && iteration < max) {
            y = 2 * x * y + c_im;
            x = x2 - y2 + c_re;
            x2 = x * x;
            y2 = y * y;
            iteration++;
        }
        if (iteration < max) return(toInt(colors[iteration]));
        else return(toInt(Color.rgb(35, 35, 35)));
        /*if (iteration < max) return(colors[iteration]);
        else return(Color.rgb(35, 35, 35));*/
    }

}
