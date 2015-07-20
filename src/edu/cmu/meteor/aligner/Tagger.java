package edu.cmu.meteor.aligner;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * Created by andrew on 5/22/15.
 */



public class Tagger {
    String tagged;
    String oink;
    ArrayList<String> tags_arraylist;

    private ArrayList<String> tokenize(String line) {
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(line);
        while (tok.hasMoreTokens())
            tokens.add(tok.nextToken());
        return tokens;
    }

    public Tagger(String arg, MaxentTagger tagger){

        // Initialize the tagger
//        MaxentTagger tagger = new MaxentTagger(
//                "taggers/english-caseless-left3words-distsim.tagger");

        this.tagged = tagger.tagTokenizedString(arg);

        String[] sentSplit = tagged.split("\\s+");

        // this.oink = Arrays.toString(sentSplit);

        ArrayList<String> tags = new ArrayList<String>();

        for (int i = 0; i < sentSplit.length; i++) {
            int underscore_index = sentSplit[i].lastIndexOf("_");

            //String origWord = sentSplit[i].substring(0, underscore_index);

            String tag = sentSplit[i].substring(underscore_index + 1);

            tags.add(tag);
        }

        tags_arraylist = tags;

    }

    public ArrayList<String> getTagged(){
        return this.tags_arraylist;
    }

}