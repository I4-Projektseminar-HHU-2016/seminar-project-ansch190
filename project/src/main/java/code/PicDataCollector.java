package code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die die Bilddaten parallel zu BufferedImages verarbeiten soll.
 */
public class PicDataCollector implements Runnable {

    private final Logger log = LoggerFactory.getLogger(PicDataCollector.class);

    private List<byte[]> bytes = null;

    private List<BufferedImage> images = null;

    private PictureExtractor pe = null;

    private ThreadType threadType = ThreadType.UNSPECIFIC;

    public PicDataCollector(List<byte[]> files, List<BufferedImage> images, PictureExtractor pe, ThreadType type){
        this.bytes = files;
        this.images = images;
        this.pe = pe;
        this.threadType = type;
    }

    @Override
    public void run() {
        double time = System.currentTimeMillis();

        if(this.threadType == ThreadType.MANAGER){
            work();
        }
        else if(this.threadType == ThreadType.WORKER){
            workOnly();
        }

        log.info("Zeit(File -> Image): " + (System.currentTimeMillis()-time) / 1000.0);
    }

    private void work(){
        BufferedImage image = null;
        byte[] tmp = null;

        //Kontrollvariablen für automatisches starten neuer Threads
        int sizeA = 0;
        int sizeB = 0;
        boolean b = true;
        int rounds = 0;
        int roundCount = 3;
        int countThreads = 1;
        int maxThreads = 3;
        int result = 0;
        int startVal = 0;

        String threadName = Thread.currentThread().getName();

        int countPics = 0;

        int count = 0;
        int countMax = 10;
        int waitTime = 500;
        try{
            do{
                if(this.bytes.size()>0){
                    count = 0;

                    synchronized(this.bytes){
                        if(this.bytes.size()>0){ tmp = this.bytes.remove(0); }
                        else{ continue; }
                    }

                    //System.out.println(threadName + " BYTES: " + this.bytes.size());
                    //System.out.println(threadName + " IMAGES: " + this.images.size());

                    //Trend für die Anzahl der Bilder erkennen(+/0/-)
                    //Werte sammeln
                    if(b){ sizeA = this.bytes.size(); }
                    else{ sizeB = this.bytes.size(); }
                    //Trend berechnen
                    if(b){ result += sizeA-sizeB; }
                    else{ result += sizeB-sizeA; }
                    //System.out.println(threadName + " Result: " + result);
                    //System.out.println(threadName + " Round: " + rounds);
                    //neuen Thread starten?
                    if(rounds==roundCount){
                        if(result>startVal){
                            if(countThreads<maxThreads){
                                this.pe.addPicDataCollector(ThreadType.WORKER);
                                countThreads++;
                                //System.out.println(threadName + " Info - Byteanzahl nimmt zu! Aufpassen");
                            }
                            //else{ System.out.println(threadName + " Info - Maximale Anzahl der PicDataCollector Threads schon erreicht!"); }
                        }
                        //else{
                            //System.out.println(threadName + " Info - Abnahme der Byteanzahl! Gut!");
                        //}
                        rounds = 0;
                        result = 0;
                    }
                    else{
                        rounds++;
                    }
                    //Wechsel verfügen
                    if(b){ b=false; }
                    else{ b=true; }

                    //System.out.println(threadName + " Threads: " + countThreads);
                    //System.out.println(threadName + " *B* " + this.bytes.size() + " *I* " + this.images.size() + " *T* " + countThreads);

                    image = ImageIO.read(new ByteArrayInputStream(tmp));
                    synchronized(this.images){
                        images.add(image);
                        countPics++;
                    }
                }
                else{
                    Thread.sleep(waitTime);
                    count++;
                }
            }while(count<countMax);
        }
        catch(Exception e){ e.printStackTrace(); }

        log.info(threadName + " REST-Files: " + this.bytes.size());
        log.info(threadName + " IMG-Size: " + images.size());

        log.info(threadName + " PicDataCollector - MANAGER - END");
        log.info("Pics insgesamt: " + countPics);
    }

    public void workOnly(){
        BufferedImage image = null;
        byte[] tmp = null;

        String threadName = Thread.currentThread().getName();

        int countPics = 0;

        int count = 0;
        int countMax = 5;
        int waitTime = 500;
        try{
            do{
                if(this.bytes.size()>0){
                    count = 0;

                    synchronized(this.bytes){
                        if(this.bytes.size()>0){ tmp = this.bytes.remove(0); }
                        else{ continue; }
                    }

                    image = ImageIO.read(new ByteArrayInputStream(tmp));
                    synchronized(this.images){
                        images.add(image);
                        countPics++;
                    }
                }
                else{
                    Thread.sleep(waitTime);
                    count++;
                }
            }while(count<countMax);
        }
        catch(Exception e){ e.printStackTrace(); }

        log.info(threadName + " REST-Files: " + this.bytes.size());
        log.info(threadName + " IMG-Size: " + images.size());

        log.info(threadName + " PicDataCollector - WORKER - END");
        log.info("Pics insgesamt: " + countPics);
    }

}
