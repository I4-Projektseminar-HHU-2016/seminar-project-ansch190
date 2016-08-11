package code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die die Daten des Prozesses entgege nimmt und dann weiter verarbeitet.
 */
public class DataStreamCatcher implements StreamCatcher, Runnable {

    private final Logger log = LoggerFactory.getLogger(DataStreamCatcher.class);

    private InputStream is = null;

    private StreamType type = StreamType.UNKNOWN;

    private List<byte[]> bytes = null;  //Zwischenspeicher der Dateien im Ram für den anderen Thread

    public DataStreamCatcher(InputStream is, StreamType type, List<byte[]> bytes){
        this.is = is;
        this.type = type;
        this.bytes = bytes;
    }

    @Override
    public void run() {
        init();
    }

    private void init(){
        if(this.type == StreamType.DATA){
            initData();
        }
        else{
            log.info("Wrong Class! - ONLY StreamTypes.Data here!");
        }
    }

    private void initData(){
        try{
            //### NEU - Parallel im RAM ###
            readStreamNow();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //#########################
    //########### NEU #########
    //#########################

    private void readStreamNow(){
        BufferedInputStream bin = null;
        InputStream stream = null;
        ByteArrayOutputStream bos = null;
        try{
            stream = this.is;
            bin = new BufferedInputStream(stream);

            //PNG-Header setzen
            byte[] header = new byte[8];
            header[0] = (byte)0x89;
            header[1] = Byte.parseByte("50",16);
            header[2] = Byte.parseByte("4E",16);
            header[3] = Byte.parseByte("47",16);
            header[4] = Byte.parseByte("0D",16);
            header[5] = Byte.parseByte("0A",16);
            header[6] = Byte.parseByte("1A",16);
            header[7] = Byte.parseByte("0A",16);
            byte[] find = new byte[8];  //Wichtig 8-Bytes wegen der Headererkennung

            //double start = System.currentTimeMillis();

            //File f = null;

            /*
            BufferedOutputStream bos = null;

            int x = 0;
            do {
                x = stream.read(find);

                if(Arrays.equals(find,header)){
                    if(f!=null){
                        bos.flush();
                        bos.close();
                        this.files.add(f);
                    }
                    //f = File.createTempFile("buffer",".tmp",new File("D:\\Filme\\tmp\\"));
                    //f.deleteOnExit();
                    bos = new BufferedOutputStream(new FileOutputStream(f));
                }
                bos.write(find);

                if(x < 0){
                    if(f!=null){
                        bos.flush();
                        bos.close();
                        this.files.add(f);
                    }
                }
            }while(x > -1);
            */

            bos = new ByteArrayOutputStream();

            int x = 0;
            do {
                //x = stream.read(find);
                x = bin.read(find);

                if(Arrays.equals(find,header)){
                    if(bos.size() > 8){
                        this.bytes.add(bos.toByteArray());
                        bos.reset();
                    }
                }
                bos.write(find);

                if(x < 0){
                    if(bos!=null){
                        bos.close();
                    }
                }
            }while(x > -1);

            //double ende = System.currentTimeMillis();
            //System.out.println("Time: " + (ende-start)/1000.0);

        }
        catch(Exception e){ e.printStackTrace(); }
        finally {
            try{
                if(bin!=null){ bin.close(); }
                if(bos!=null){ bos.close(); }
            }catch (Exception e0){ e0.printStackTrace(); }
        }
    }

    //#########################
    //### Bilder-Funktionen ###
    //#########################

    private File saveStream(){
        File tmp = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bin = null;
        InputStream stream = null;
        try{
            //### Stream speichern ###
            tmp = new File("D:\\Filme\\tmp\\buffer.tmp");

            bos = new BufferedOutputStream(new FileOutputStream(tmp));

            stream = this.is;
            bin = new BufferedInputStream(stream);
            byte[] b = new byte[8192];

            while(stream.read(b) > 0) {
                bos.write(b);
                bos.flush();
                Thread.sleep(3);
            }

        }
        catch(Exception e){ e.printStackTrace(); }
        finally {
            try{
                if(bin!=null){ bin.close(); }
                if(bos!=null){ bos.close(); }
            }catch (Exception e0){ e0.printStackTrace(); }
        }
        return tmp;
    }

    private List<Integer> readIndex(File f){
        List<Integer> index = new ArrayList<>();

        BufferedInputStream bis = null;

        try{
            //Stream-File lesen
            bis = new BufferedInputStream(new FileInputStream(f));

            //PNG-Header setzen
            byte[] header = new byte[8];
            header[0] = (byte)0x89;
            header[1] = Byte.parseByte("50",16);
            header[2] = Byte.parseByte("4E",16);
            header[3] = Byte.parseByte("47",16);
            header[4] = Byte.parseByte("0D",16);
            header[5] = Byte.parseByte("0A",16);
            header[6] = Byte.parseByte("1A",16);
            header[7] = Byte.parseByte("0A",16);
            byte[] find = new byte[8];

            int count = -1;  //-1 weil erstes mal Offset=0

            while(true){
                //Header suchen
                do {
                    bis.read(find);
                    count++;
                } while (!Arrays.equals(find, header) && bis.available()>0);

                if(bis.available()==0){
                    //bis.close();
                    break;
                }

                //Offset der Header speichern
                index.add(8 * count);

                //System.out.println("SAVE");

                count = 0;
            }
        }
        catch(Exception e){ e.printStackTrace(); }
        finally {
            try{
                if(bis!=null){ bis.close(); }
            }catch (Exception e0){ e0.printStackTrace(); }
        }

        //System.out.println("Index-size: " + index.size());
        //System.out.println("Index: " + index);

        return index;
    }

    private List<BufferedImage> readImages(List<Integer> index, File f){
        List<BufferedImage> images = new ArrayList<>(index.size());

        BufferedInputStream bin = null;
        InputStream in = null;
        BufferedImage image = null;

        try{
            bin = new BufferedInputStream(new FileInputStream(f));

            index.add(-1);  //Pseudowert für Schleife
            //System.out.println("Index2: " + index);

            int x,y;
            int offset = 0;
            for(int i=0; i<index.size()-1; i++){
                //Positionen aus der Datei laden
                x = index.get(i);
                y = index.get(i+1);

                //Bild aus Datei laden
                byte[] bytes = null;
                if(y>0){ bytes = new byte[y]; }
                else{
                    //System.out.println("AVL: " + bin.available());
                    bytes = new byte[bin.available()];
                }

                bin.read(bytes);

                //Bild erstellen
                in = new ByteArrayInputStream(bytes);
                image = ImageIO.read(in);
                in.close();
                in = null;

                images.add(image);
            }

        }
        catch(Exception e){ e.printStackTrace(); }
        finally {
            try{
                if(bin!=null){ bin.close(); }
                if(in!=null){ in.close(); }
            }
            catch (Exception e0){ e0.printStackTrace(); }
        }

        return images;
    }

    //#################
    //### Sonstiges ###
    //#################

    //Ausgabe für Hex-Werte
    private String ByteArray2HexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02X ",b));
        }
        return sb.toString();
    }

}
