package tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.PictureExtractor;

/**
 * Created by andreas on 11.08.2016.
 *
 * Alte Tests...
 */
public class Tests {

    private static Logger log = LoggerFactory.getLogger(Tests.class);

    public static void main(String[] args){
        test0();
    }

    public static void test0(){
        log.info("...start Test...");

        String sourcePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/vid0.mkv";
        String savePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/tmp/";
        double fps = 1.0;

        PictureExtractor pe = new PictureExtractor(sourcePath, savePath, fps);

        pe.init();
        pe.extractPics();

//        List<String> hashes = pe.getHashes();
//
//        log.info("HashSize: " + hashes.size());
//        for(String s : hashes){
//            log.info(s);
//        }

        log.info("...end Test...");
    }

}
