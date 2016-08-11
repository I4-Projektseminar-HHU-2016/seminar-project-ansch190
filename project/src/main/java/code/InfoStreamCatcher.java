package code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die die Ausgaben der Prozesse auf der Konsole ausgibt.
 */
public class InfoStreamCatcher implements StreamCatcher, Runnable {

    private final Logger log = LoggerFactory.getLogger(InfoStreamCatcher.class);

    private InputStream is = null;

    private StreamType type = StreamType.UNKNOWN;

    public InfoStreamCatcher(InputStream is, StreamType type){
        this.is = is;
        this.type = type;
    }

    @Override
    public void run() {
        init();
    }

    private void init(){
        if(this.type != StreamType.DATA){
            initNormal();
        }
        else{
            log.info("Wrong Class! - NO StreamTypes.Data here!");
        }
    }

    private void initNormal(){
        InputStreamReader reader = new InputStreamReader(this.is);
        BufferedReader br = new BufferedReader(reader);
        String line;
        try{
            while((line = br.readLine()) != null){
                log.info(this.type + " -> " + line);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            try{
                br.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
