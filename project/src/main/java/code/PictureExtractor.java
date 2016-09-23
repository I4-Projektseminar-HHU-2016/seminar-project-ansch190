package code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die mittels ffmpeg Bilder in einem beliebigen Abstand aus einem Video extrahiert.
 */
public class PictureExtractor {

    private final Logger log = LoggerFactory.getLogger(PictureExtractor.class);

    private String sourcePath;
    private String savePath;
    private double picsPerSecond = 1.0;

    private ExecutorService e = null;
    private List<byte[]> bytes = new LinkedList<>();
    private List<BufferedImage> images = new LinkedList<>();
    private List<String> hashs = new LinkedList<>();

    public PictureExtractor(String sourcePath, String savePath, double fps){
        this.sourcePath = sourcePath;
        this.savePath = savePath;
        this.picsPerSecond = fps;
    }

    public PictureExtractor(String sourcePath, String savePath){
        this.sourcePath = sourcePath;
        this.savePath = savePath;
    }

    public void init(){
        //Threading vorbereiten
        //int cores = Runtime.getRuntime().availableProcessors();
        //System.out.println("Kerne: " + cores);

        this.e = Executors.newCachedThreadPool();  //Executors.newFixedThreadPool(cores);

        //Threadübergreifender Speicher
        this.bytes = new LinkedList<>();
        this.images = new LinkedList<>();
        this.hashs = new LinkedList<>();
    }

    //getHashes();
    public List<String> getHashes(){
        return this.hashs;
    }

    //###############
    //### THREADS ###
    //###############

    private void addInfoThread(InputStream stream){
        Runnable r = new InfoStreamCatcher(stream, StreamType.INFO);  //Für Ausgabe auf Konsole
        this.e.submit(r);
        log.info("InfoStreamCatcher started!");
    }

    private void addDataThread(InputStream stream){
        Runnable r = new DataStreamCatcher(stream,StreamType.DATA,bytes);  //Für den Datenstream von ffmpeg
        this.e.submit(r);
        log.info("DataStreamCatcher started!");
    }

    public void addPicDataCollector(ThreadType threadType){
        Runnable r = new PicDataCollector(this.bytes,this.images,this,threadType);
        this.e.submit(r);
        log.info("PicDataCollector started! " + threadType);
    }

    public void addPicDataConverter(ThreadType threadType){
        Runnable r = new PicDataConverter(this.images,this.hashs,this,threadType);
        this.e.submit(r);
        log.info("PicDataConverter started! " + threadType);
    }

    //prüft ob alles ausgeführt wurde und schließt dann den Executor
    private void checkEnd(){
        try {
            Thread.sleep(10 * 1000);
            do {
                Thread.sleep(1000);
            } while(!this.e.isTerminated());
            this.e = null;
            log.info("Shutdown yeah!!! :-D");
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    public void shutdownPool(){
        //Neue aufträge ablehnen und Threads zu Ende arbeiten lassen
        this.e.shutdown();
        //Bytes
        this.bytes = null;
        //Images
        this.images = null;
    }

    public void extractPics(){
        init();

        try {
            Process p = new ProcessBuilder("ffmpeg", "-i", sourcePath, "-r", String.valueOf(picsPerSecond), "-f", "image2pipe", "-c", "png", "-").start();
            log.info("ffmpeg " + "-i " + sourcePath + " -r " + String.valueOf(picsPerSecond) + " -f" + " image2" + " -c " + "png " + savePath + "%d.png");

            addInfoThread(p.getErrorStream());  //Info
            addDataThread(p.getInputStream());  //Data -> File

            addPicDataCollector(ThreadType.MANAGER);  //File -> Image
            addPicDataConverter(ThreadType.MANAGER);  //Image -> Hash

            log.info("bytes_all: " + this.bytes.size());

            checkEnd();

            int exitValue = p.waitFor();
            log.info("Exit Value is " + exitValue + "\n");
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    public void displayPics(List<BufferedImage> images){
        JFrame frame = new JFrame();
        frame.setTitle("Bilder - " + images.size());

        //Bildgröße
        int imageWidth = images.get(0).getWidth();
        int imageHeight = images.get(0).getHeight();

        frame.setSize(imageWidth + 26 + 10, imageHeight + 48 + 10);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));

        //Scrollbar erstellen
        JScrollPane scrollPane = new JScrollPane(panel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        //Scrollgeschwindigkeit anpassen, damit Vollbilder angezeigt werden
        scrollPane.getHorizontalScrollBar().setUnitIncrement(imageWidth + 5);

        //Bilder hinzufügen
        for(BufferedImage i : images) {
            panel.add(new JLabel(new ImageIcon(i)));
        }

        frame.add(scrollPane);

        //Mittig auf dem Bildschirm
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //frame.setLocation(dim.width/2 - frame.getSize().width/2, dim.height/2 - frame.getSize().height/ 2);
        frame.setLocationRelativeTo(null);

        //frame.pack();
        frame.setVisible(true);
        frame.toFront();
    }

}
