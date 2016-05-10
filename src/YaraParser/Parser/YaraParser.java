/**
 * Copyright 2014, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package YaraParser.Parser;

import YaraParser.Accessories.CoNLLReader;
import YaraParser.Accessories.Evaluator;
import YaraParser.Accessories.Options;
import YaraParser.Learning.AveragedPerceptron;
import YaraParser.Structures.IndexMaps;
import YaraParser.Structures.InfStruct;
import YaraParser.TransitionBasedSystem.Configuration.GoldConfiguration;
import YaraParser.TransitionBasedSystem.Parser.KBeamArcEagerParser;
import YaraParser.TransitionBasedSystem.Trainer.ArcEagerBeamTrainer;

import java.util.ArrayList;
import java.util.HashMap;

public class YaraParser {
    public static void main(String[] args) throws Exception {
    	
    	System.gc();
    	long start = System.currentTimeMillis();
//    	String[] args_yuyi_train = {"train","-train-file","THU/train/train_01.conll","-dev","THU/train/dev_01.conll",
//    			"-model","THU/model/model_thu","iter:10","-punc","THU/punc/chinese-punc.puncs"};
//    	String[] args_yuyi_test = {};
    	
//    	String[] arg = {"parse_conll","-input","./data/test/test01.conll06","-out","./data/test/test01_result.conll06","-model","./data/model-cdt_iter20"};
    	
//    	String[] args_train = {"train","-train-file","./temp/test/train_thu.conll","-dev","./temp/test/dev_thu.conll","-model","./temp/test/model/model_thu",
//    			"iter:10","-punc","./punc_files/google_universal.puncs"};
//    	String[] args_test = {"parse_conll","-input","temp/sample_data/test.conll","-model","temp/model_iter10","-out","temp/test.output.conll"};
//    	String[] args_eval = {"eval","-gold","temp/sample_data/test.conll","-parse","temp/test.output.conll"};
    	
    	// 在这个地方设置几个参数
    	
    	String[] args_comments_test = {"parse_conll","-input","d:/Java/ANSJ/comment_partial/comment_100_short_conll.conll",
    			"-model","d:/Java/YaraParser/model_ansj/model-new-cdt_iter20","-out","d:/Java/ANSJ/comment_partial/comment_100_short_parsed.conll"};
    	
        Options options = Options.processArgs(args_comments_test);
        
        if (options.showHelp) {
            Options.showHelp();
        } else {
            System.out.println(options);
            if (options.train) {
                train(options);
            } else if (options.parseTaggedFile || options.parseConllFile || options.parsePartialConll) {
                parse(options);
            } else if (options.evaluate) {
                evaluate(options);
            } else {
                Options.showHelp();
            }
        }
        
        long end = System.currentTimeMillis();
        System.err.println((end - start)/1000 + "s");
        
        System.exit(0);
    }

    private static void evaluate(Options options) throws Exception {
        if (options.goldFile.equals("") || options.predFile.equals(""))
            Options.showHelp();
        else {
            Evaluator.evaluate(options.goldFile, options.predFile, options.punctuations);
        }
    }

    private static void parse(Options options) throws Exception {
        if (options.outputFile.equals("") || options.inputFile.equals("")
                || options.modelFile.equals("")) {
            Options.showHelp();

        } else {
            InfStruct infStruct = new InfStruct(options.modelFile);
            ArrayList<Integer> dependencyLabels = infStruct.dependencyLabels;
            IndexMaps maps = infStruct.maps;


            Options inf_options = infStruct.options;
            AveragedPerceptron averagedPerceptron = new AveragedPerceptron(infStruct);

            int featureSize = averagedPerceptron.featureSize();
            KBeamArcEagerParser parser = new KBeamArcEagerParser(averagedPerceptron, dependencyLabels, featureSize, maps, options.numOfThreads);

            if (options.parseTaggedFile)
                parser.parseTaggedFile(options.inputFile,
                        options.outputFile, inf_options.rootFirst, inf_options.beamWidth, inf_options.lowercase, options.separator, options.numOfThreads);
            else if (options.parseConllFile)
                parser.parseConllFile(options.inputFile,
                        options.outputFile, inf_options.rootFirst, inf_options.beamWidth, true, inf_options.lowercase, options.numOfThreads, false, options.scorePath);
            else if (options.parsePartialConll)
                parser.parseConllFile(options.inputFile,
                        options.outputFile, inf_options.rootFirst, inf_options.beamWidth, options.labeled, inf_options.lowercase, options.numOfThreads, true, options.scorePath);
            parser.shutDownLiveThreads();
        }
    }

    public static void train(Options options) throws Exception {
        if (options.inputFile.equals("") || options.modelFile.equals("")) {
            Options.showHelp();
        } else {
            IndexMaps maps = CoNLLReader.createIndices(options.inputFile, options.labeled, options.lowercase, options.clusterFile);
            CoNLLReader reader = new CoNLLReader(options.inputFile);
            ArrayList<GoldConfiguration> dataSet = reader.readData(Integer.MAX_VALUE, false, options.labeled, options.rootFirst, options.lowercase, maps);
            System.out.println("CoNLL data reading done!");

            ArrayList<Integer> dependencyLabels = new ArrayList<Integer>();
            for (int lab : maps.getLabels().keySet())
                dependencyLabels.add(lab);

            int featureLength = options.useExtendedFeatures ? 72 : 26;
            if (options.useExtendedWithBrownClusterFeatures || maps.hasClusters())
                featureLength = 153;

            System.out.println("size of training data (#sens): " + dataSet.size());

            HashMap<String, Integer> labels = new HashMap<String, Integer>();
            int labIndex = 0;
            labels.put("sh", labIndex++);
            labels.put("rd", labIndex++);
            labels.put("us", labIndex++);
            for (int label : dependencyLabels) {
                if (options.labeled) {
                    labels.put("ra_" + label, 3 + label);
                    labels.put("la_" + label, 3 + dependencyLabels.size() + label);
                } else {
                    labels.put("ra_" + label, 3);
                    labels.put("la_" + label, 4);
                }
            }

            ArcEagerBeamTrainer trainer = new ArcEagerBeamTrainer(options.useMaxViol ? "max_violation" : "early", new AveragedPerceptron(featureLength, dependencyLabels.size()),
                    options, dependencyLabels, featureLength, maps);
            trainer.train(dataSet, options.devPath, options.trainingIter, options.modelFile, options.lowercase, options.punctuations, options.partialTrainingStartingIteration);
        }
    }
}
