package code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die die BufferedImages hasht.
 */
public class PicDataConverter implements Runnable {

    private final Logger log = LoggerFactory.getLogger(PicDataConverter.class);

    private List<BufferedImage> images = null;

    private List<String> hashs = null;

    private PictureExtractor pe = null;

    private ThreadType threadType = ThreadType.UNSPECIFIC;

    public PicDataConverter(List<BufferedImage> images, List<String> hashs, PictureExtractor pe, ThreadType type){
        this.images = images;
        this.hashs = hashs;
        this.pe = pe;
        this.threadType = type;
    }

    @Override
    public void run(){
        double time = System.currentTimeMillis();

        if(this.threadType == ThreadType.MANAGER){
            work();
        }
        else if(this.threadType == ThreadType.WORKER){
            workOnly();
        }

        log.info("Zeit(Image -> Hash): " + (System.currentTimeMillis() - time) / 1000.0);
    }

    private void work(){
        String hash = null;
        BufferedImage img = null;

        //Kontrollvariablen für automatisches starten neuer Threads
        int sizeA = 0;
        int sizeB = 0;
        boolean b = true;
        int rounds = 0;
        int roundCount = 3;
        int countThreads = 1;
        int maxThreads = 8;
        int result = 0;
        int startVal = 0;

        String threadName = Thread.currentThread().getName();

        int count = 0;
        int countMax = 10;
        int waitTime = 500;
        try{
            do{
                if(this.images.size()>0){
                    count = 0;

                    synchronized(this.images) {
                        if(this.images.size()>0){ img = this.images.remove(0); }
                        else{ continue; }
                    }

                    //System.out.println(threadName + "            IMAGES:  " + this.images.size());
                    //System.out.println(threadName + "                           HASHES:  " + this.hashs.size());

                    //Trend für die Anzahl der Bilder erkennen(+/0/-)
                    //Werte sammeln
                    if(b){ sizeA = this.images.size(); }
                    else{ sizeB = this.images.size(); }
                    //Trend berechnen
                    if(b){ result += sizeA-sizeB; }
                    else{ result += sizeB-sizeA; }
                    //System.out.println(threadName + "            Result: " + result);
                    //System.out.println(threadName + "            Round: " + rounds);
                    //neuen Thread starten?
                    if(rounds==roundCount){
                        if(result>startVal){
                            if(countThreads<maxThreads){
                                this.pe.addPicDataConverter(ThreadType.WORKER);
                                countThreads++;
                                //System.out.println(threadName + "Info - Bildanzahl nimmt zu! Aufpassen");
                            }
                            //else{ System.out.println(threadName + "Info - Maximale Anzahl der PicDataConverter Threads schon erreicht!"); }
                        }
                        //else{
                            //System.out.println(threadName + "Info - Abnahme der Bildanzahl! Gut!");
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

                    //System.out.println(threadName + "            Threads: " + countThreads);
                    //System.out.println(threadName + " #I# " + this.images.size() + " #H# " + this.hashs.size() + " #T# " + countThreads);

                    hash = HashEngine.hashPicImg(img);
                    synchronized(this.hashs){ hashs.add(hash); }
                }
                else{
                    Thread.sleep(waitTime);
                    count++;
                }
            }while(count<countMax);  //10  2400->100ms pro Bild bei 200min. Film ergibt 20 Minuten maximale Wartezeit!
        }
        catch(Exception e){ e.printStackTrace(); }

        log.info(threadName + " REST-IMG: " + images.size());
        log.info(threadName + " HASH-Size: " + hashs.size());
        //System.out.println("HASHS: " + hashs);

        this.pe.shutdownPool();

        log.info(threadName + " PicDataConverter - MANAGER - END");
    }

    private void workOnly(){
        String hash = null;
        BufferedImage img = null;

        String threadName = Thread.currentThread().getName();

        int count = 0;
        int countMax = 5;
        int waitTime = 500;
        try{
            do{
                if(this.images.size()>0){
                    count = 0;
                    synchronized(this.images){
                        if(this.images.size()>0){ img = this.images.remove(0); }
                        else{ continue; }
                    }
                    hash = HashEngine.hashPicImg(img);
                    synchronized(this.hashs){ hashs.add(hash); }
                }
                else{
                    Thread.sleep(waitTime);
                    count++;
                }
            }while(count<countMax);  //10  2400->100ms pro Bild bei 200min. Film ergibt 20 Minuten maximale Wartezeit!
        }
        catch(Exception e){ e.printStackTrace(); }

        log.info(threadName + " PicDataConverter - WORKER - END");
    }

}
