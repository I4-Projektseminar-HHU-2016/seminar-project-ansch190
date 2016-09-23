package tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import code.PictureExtractor;

/**
 * Created by andreas on 11.08.2016.
 *
 * Klasse für Tests.
 */
public class Tests {

    private static Logger log = LoggerFactory.getLogger(Tests.class);

    public static void main(String[] args) {
//        test0();
//        testCompare();
    }

    public static void testCompare() {
        log.info("...start Test...");

//        String sourcePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/test/vid1.mkv";
//        String savePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/tmp/";
//        double fps = 1.0;

//        PictureExtractor pe = new PictureExtractor(sourcePath, savePath, fps);
//
//        pe.init();
//        pe.extractPics();

        String dbsPathSource = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/dbs/source/";
        String dbsPathTest = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/dbs/test/";
        String fileNameTest = "test3-10fps";
        String fileNameOriginal = "tt0120762-mulan-full-10fps";

        List<String> hashesTest  = loadHashesFromFile(dbsPathTest, fileNameTest);
        List<String> hashesOriginal = loadHashesFromFile(dbsPathSource, fileNameOriginal);

        //Vergleiche Hashes
        compareLists(hashesTest, hashesOriginal);

        log.info("...end Test...");
    }

    public static int hammingDistance(String a, String b){
        int diffs = 0;
        if(a.length() == b.length()){
            char ca;
            char cb;
            for(int i=0; i<a.length(); i++){
                ca = a.charAt(i);
                cb = b.charAt(i);
                if(ca != cb){
                    diffs++;
                }
            }
        }
        return diffs;
    }

    public static boolean compareHashs(String a, String b, int maxDiffs){
        int diffs = hammingDistance(a, b);
        if(diffs <= maxDiffs){
            return true;
        }
        return false;
    }

    public static void compareLists(List<String> testData, List<String> sourceData){
        HashSet<String> test = new HashSet<>(testData);
        HashSet<String> source = new HashSet<>(sourceData);

        log.info("" + test.size());
        log.info("" + source.size());

        double count = 0;

        int maxDiffs = 20;

        for(int diffs=0; diffs<maxDiffs; diffs++){
            count = 0;
            for(String a : test){
                for(String b : source){
                    if(compareHashs(a, b, diffs)){
                        count++;
                        break;
                    }
                }
            }
            log.info("Hashs all  : " + test.size());
            log.info("Hashs Diffs: " + diffs);
            log.info("Hashs found: " + count);
            log.info("Hashs percent: " + (count / test.size()) * 100.0 );
            log.info("------------------------------------------------");
        }

    }

    public static void test0() {
        log.info("...start Test...");

        //00:54:30 start
        //         ende

//        String sourcePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/vid1.mkv";
        String sourcePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/test/test3.mp4";
//        String sourcePath = "/run/media/andreas/Daten/MyDownloads/Man.of.Steel.German.BDRiP.x264-EXQUiSiTE/exq-manofsteel-sd.mkv";
//        String sourcePath = "/run/media/andreas/Daten/MyDownloads/Mulan.1998.German.DL.DTS.1080p.BluRay.x264-MOViEADDiCTS/ma-mulan-1080p.mkv";
        String savePath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/vids/tmp/";
        double fps = 10.0;

        PictureExtractor pe = new PictureExtractor(sourcePath, savePath, fps);

        pe.init();
        pe.extractPics();

        List<String> hashes = pe.getHashes();

        log.info("HashSize: " + hashes.size());
//        for (String s : hashes) {
//            log.info(s);
//        }

        //Mulan 1 IMDB-ID = tt0120762

        String dbsPath = "/home/andreas/IdeaProjects/seminar-project-ansch190/project/src/main/resources/dbs/test/";
//        String fileName = "tt0120762-mulan-full-10fps";
        String fileName = "test3-10fps";

        saveHashesToFile(dbsPath, fileName, hashes);

//        hashes = loadHashesFromFile(dbsPath, fileName);

//        log.info("loaded HashSize: " + hashes.size());

        log.info("...end Test...");
    }

    public static void saveHashesToFile(String savePath, String fileName, List<String> data) {
        try {
            File f = new File(savePath + fileName);
            FileWriter fw = new FileWriter(f);

            for (String s : data) {
                fw.write(s + "\n");
            }

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadHashesFromFile(String path, String fileName) {
        List<String> out = null;

        try {
            File f = new File(path + fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));

            out = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                out.add(line);
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

}
