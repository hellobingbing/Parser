/**
 Copyright 2014, Yahoo! Inc.
 Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 **/

package YaraParser.Parser;

import YaraParser.Learning.AveragedPerceptron;
import YaraParser.Structures.IndexMaps;
import YaraParser.Structures.InfStruct;
import YaraParser.TransitionBasedSystem.Configuration.Configuration;
import YaraParser.TransitionBasedSystem.Parser.KBeamArcEagerParser;

import java.io.File;
import java.util.ArrayList;


public class API_UsageExample {
    public static void main(String[] args) throws Exception {
    	
    	String path = "." + File.separator + "model_ansj" + File.separator + "model-new-cdt_iter20";
    	
//        String modelFile = args[0];
        int numOfThreads = 8;

        InfStruct infStruct = new InfStruct(path);

        ArrayList<Integer> dependencyLabels = infStruct.dependencyLabels;
        IndexMaps maps = infStruct.maps;
        AveragedPerceptron averagedPerceptron = new AveragedPerceptron(infStruct);

        int featureSize = averagedPerceptron.featureSize();
        KBeamArcEagerParser parser = new KBeamArcEagerParser(averagedPerceptron, dependencyLabels, featureSize, maps, numOfThreads);

//        String[] words = {"县", "里", "还", "制定", "了", "万", "户", "农民", "奔", "小康", "工程", "规划", ","};
//        String[] tags = {"n", "nd", "d", "v", "u", "m", "q", "n", "v", "n", "n", "n", "wp"};
        
        
        String[] words = {"我","是","中华人民共和国","公民","。"};
        String[] tags = {"r","v","ns","n","w"};

        Configuration bestParse = parser.parse(maps.makeSentence(words, tags, infStruct.options.rootFirst, infStruct.options.lowercase), infStruct.options.rootFirst, infStruct.options.beamWidth, numOfThreads);
        if (infStruct.options.rootFirst) {
            for (int i = 0; i < words.length; i++) {
                System.out.println(words[i] + "\t" + tags[i] + "\t" + bestParse.state.getHead(i + 1) + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
            }
        } else {
            for (int i = 0; i < words.length; i++) {
                int head = bestParse.state.getHead(i + 1);
                if (head == words.length+1)
                    head = 0;
                System.out.println(words[i] + "\t" + tags[i] + "\t" + head + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
            }
        }
        parser.shutDownLiveThreads();
        System.exit(0);
    }
}
