package code;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die alle Funktionen für Bilder und Videos zusammenfasst.
 */
public class HashEngine {

    //Speicherverzeichnis für die Bilder leeren
    private static void clearDirectory(File dir){
        File[] files = dir.listFiles();
        for(File f : files){
            f.delete();
        }
    }

    private static void clearDirectory(String path){
        File dir = new File(path);
        clearDirectory(dir);
    }

    //Video hashen
    public static List<String> hashVideo(String videoPath, String savePath, double picsPerSecond){
        File dir = new File(savePath);
        clearDirectory(dir);

        PictureExtractor pe = new PictureExtractor(videoPath,savePath,picsPerSecond);
        pe.extractPics();

        dir = new File(savePath);
        File[] files = dir.listFiles();
        List<String> paths = new ArrayList<>();
        assert files != null;
        for(File f : files){
            paths.add(f.getAbsolutePath());
        }

        return hashPicsPath(paths);
    }

    public static List<String> hashVideo(String videoPath){
        String savePath = System.getProperty("user.dir") + "\\src\\main\\resources\\pics\\";
        double picsPerSecond = 1.0;

        return hashVideo(videoPath, savePath, picsPerSecond);
    }

    //############
    //### BILD ###
    //############

    //1 Bild in einem Thread hashen.
    public static String hashPicImg(BufferedImage img){
        int cores = 1;

        ExecutorService e = Executors.newFixedThreadPool(cores);

        Callable<String> c0 = null;

        int size = 32;

        double[][] vals;
        try{
            img = ImageCalculator.resize(img, size, size);
            img = ImageCalculator.grayscale(img);
            vals = new double[size][size];
            for(int x = 0; x < img.getWidth(); x++){
                for(int y = 0; y < img.getHeight(); y++){
                    vals[x][y] = ImageCalculator.getBlue(img, x, y);
                }
            }
            c0 = new HashCalculator(vals);
        }
        catch(Exception e1){
            e1.printStackTrace();
        }

        String hash = "";

        try{
            hash = e.submit(c0).get();
            e.shutdown();
        }
        catch(Exception e2){
            e2.printStackTrace();
        }
        return hash;
    }

    public static List<String> hashPicPath(String path){
        List<String> paths = new ArrayList<>();
        paths.add(path);
        return hashPicsPath(paths);
    }

    //##############
    //### BILDER ###
    //##############

    //Mehrere Bilder in mehreren Threads hashen
    public static List<String> hashPicsImg(List<BufferedImage> images){
        int cores = Runtime.getRuntime().availableProcessors();

        ExecutorService e = Executors.newFixedThreadPool(cores);

        List<Callable<String>> threads = new ArrayList<>();

        int size = 32;

        double[][] vals;
        BufferedImage img = null;
        //for(BufferedImage img : images){
        for(int i=0; i<images.size(); i++){
            try{
                img = images.get(i);
                img = ImageCalculator.resize(img, size, size);
                img = ImageCalculator.grayscale(img);
                vals = new double[size][size];
                for(int x = 0; x < img.getWidth(); x++){
                    for(int y = 0; y < img.getHeight(); y++){
                        vals[x][y] = ImageCalculator.getBlue(img, x, y);
                    }
                }
                threads.add(new HashCalculator(vals));
            }
            catch(Exception e1){
                e1.printStackTrace();
            }
        }

        List<Future<String>> results;
        List<String> hashs = new ArrayList<>();

        try{
            results = e.invokeAll(threads);
            for(Future f : results){
                if(f.isDone()){
                    hashs.add((String)f.get());
                }
            }
            e.shutdown();
        }
        catch(Exception e2){
            e2.printStackTrace();
        }
        return hashs;
    }

    //Mehrere Bilder hashen mit den Pfaden zu den einzelnen Dateien
    public static List<String> hashPicsPath(List<String> paths){
        int cores = Runtime.getRuntime().availableProcessors();

        ExecutorService e = Executors.newFixedThreadPool(cores);

        List<Callable<String>> threads = new ArrayList<>();

        int size = 32;

        double[][] vals;
        BufferedImage img;
        for(String s : paths){
            try{
                img = ImageIO.read(new FileInputStream(s));
                img = ImageCalculator.resize(img, size, size);
                img = ImageCalculator.grayscale(img);
                vals = new double[size][size];
                for(int x = 0; x < img.getWidth(); x++){
                    for(int y = 0; y < img.getHeight(); y++){
                        vals[x][y] = ImageCalculator.getBlue(img, x, y);
                    }
                }
                threads.add(new HashCalculator(vals));
            }
            catch(IOException e1){
                e1.printStackTrace();
            }
        }

        List<Future<String>> results;
        List<String> hashs = new ArrayList<>();

        try{
            results = e.invokeAll(threads);
            for(Future f : results){
                if(f.isDone()){
                    hashs.add((String)f.get());
                }
            }
            e.shutdown();
        }
        catch(Exception e1){
            e1.printStackTrace();
        }
        return hashs;
    }

}
