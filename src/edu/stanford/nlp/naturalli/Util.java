package edu.stanford.nlp.naturalli;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IterableIterator;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static edu.stanford.nlp.util.logging.Redwood.log;

/**
 * TODO(gabor) JavaDoc
 *
 * @author Gabor Angeli
 */
public class Util {

  /**
   * TODO(gabor) JavaDoc
   *
   * @param tokens
   * @param span
   * @return
   */
  public static String guessNER(List<CoreLabel> tokens, Span span) {
    Counter<String> nerGuesses = new ClassicCounter<>();
    for (int i : span) {
      nerGuesses.incrementCount(tokens.get(i).ner());
    }
    nerGuesses.remove("O");
    nerGuesses.remove(null);
    if (nerGuesses.size() > 0 && Counters.max(nerGuesses) >= span.size() / 2) {
      return Counters.argmax(nerGuesses);
    } else {
      return "O";
    }
  }

  /**
   * TODO(gabor) JavaDoc
   *
   * @param tokens
   * @return
   */
  public static String guessNER(List<CoreLabel> tokens) {
    return guessNER(tokens, new Span(0, tokens.size()));
  }

  /**
   * TODO(gabor) JavaDoc
   *
   * @param tokens
   * @param seed
   * @return
   */
  public static Span extractNER(List<CoreLabel> tokens, Span seed) {
    // Error checks
    if (seed == null) {
      return new Span(0, 1);
    }
    if (seed.start() < 0 || seed.end() < 0) {
      return new Span(0, 0);
    }
    if (seed.start() >= tokens.size() || seed.end() > tokens.size()) {
      return new Span(tokens.size(),tokens.size());
    }
    if (tokens.get(seed.start()).ner() == null) {
      return seed;
    }
    if (seed.start() < 0 || seed.end() > tokens.size()) {
      return Span.fromValues(Math.max(0, seed.start()), Math.min(tokens.size(), seed.end()));
    }

    // Find the span's beginning
    int begin = seed.start();
    while (begin < seed.end() - 1 && "O".equals(tokens.get(begin).ner())) {
      begin += 1;
    }
    String beginNER = tokens.get(begin).ner();
    if (!"O".equals(beginNER)) {
      while (begin > 0 && tokens.get(begin - 1).ner().equals(beginNER)) {
        begin -= 1;
      }
    } else {
      begin = seed.start();
    }
    // Find the span's end
    int end = seed.end() - 1;
    while (end > begin && "O".equals(tokens.get(end).ner())) {
      end -= 1;
    }
    String endNER = tokens.get(end).ner();
    if (!"O".equals(endNER)) {
      while (end < tokens.size() - 1 && tokens.get(end + 1).ner().equals(endNER)) {
        end += 1;
      }
    } else {
      end = seed.end() - 1;
    }
    // Check that the NER of the beginning and end are the same
    if (beginNER.equals(endNER)) {
      return Span.fromValues(begin, end + 1);
    } else {
      String bestNER = guessNER(tokens, Span.fromValues(begin, end + 1));
      if (beginNER.equals(bestNER)) {
        return extractNER(tokens, Span.fromValues(begin, begin + 1));
      } else if (endNER.equals(bestNER)){
        return extractNER(tokens, Span.fromValues(end, end + 1));
      } else {
        // Something super funky is going on...
        return Span.fromValues(begin, end + 1);
      }
    }
  }

  /**
   * TODO(gabor) JavaDoc
   *
   * @param sentence
   * @param pipeline
   */
  public static void annotate(CoreMap sentence, AnnotationPipeline pipeline) {
    Annotation ann = new Annotation(StringUtils.join(sentence.get(CoreAnnotations.TokensAnnotation.class), " "));
    ann.set(CoreAnnotations.TokensAnnotation.class, sentence.get(CoreAnnotations.TokensAnnotation.class));
    ann.set(CoreAnnotations.SentencesAnnotation.class, Collections.singletonList(sentence));
    pipeline.annotate(ann);
  }

  /**
   * Fix some bizarre peculiarities with certain trees.
   * So far, these include:
   * <ul>
   * <li>Sometimes there's a node from a word to itself. This seems wrong.</li>
   * </ul>
   *
   * @param tree The tree to clean (in place!).
   * @return A list of extra edges, which are valid but were removed.
   */
  public static List<SemanticGraphEdge> cleanTree(SemanticGraph tree) {
    // Clean nodes
    List<IndexedWord> toDelete = new ArrayList<>();
    for (IndexedWord vertex : tree.vertexSet()) {
      // Clean punctuation
      if (vertex.tag() == null) { continue; }
      char tag = vertex.backingLabel().tag().charAt(0);
      if (tag == '.' || tag == ',' || tag == '(' || tag == ')' || tag == ':') {
        if (!tree.outgoingEdgeIterator(vertex).hasNext()) {  // This should really never happen, but it does.
          toDelete.add(vertex);
        }
      }
    }
    toDelete.forEach(tree::removeVertex);

    // Clean edges
    Iterator<SemanticGraphEdge> iter = tree.edgeIterable().iterator();
    while (iter.hasNext()) {
      SemanticGraphEdge edge = iter.next();
      if (edge.getDependent().index() == edge.getGovernor().index()) {
        // Clean self-edges
        iter.remove();
      } else if (edge.getRelation().toString().equals("punct")) {
        // Clean punctuation (again)
        if (!tree.outgoingEdgeIterator(edge.getDependent()).hasNext()) {  // This should really never happen, but it does.
          iter.remove();
        }
      }
    }

    // Remove extra edges
    List<SemanticGraphEdge> extraEdges = new ArrayList<>();
    for (SemanticGraphEdge edge : tree.edgeIterable()) {
      if (edge.isExtra()) {
        if (tree.incomingEdgeList(edge.getDependent()).size() > 1) {
          extraEdges.add(edge);
        }
      }
    }
    extraEdges.forEach(tree::removeEdge);
    // Add apposition edges (simple coref)
    for (SemanticGraphEdge extraEdge : new ArrayList<>(extraEdges)) {  // note[gabor] prevent concurrent modification exception
      for (SemanticGraphEdge candidateAppos : tree.incomingEdgeIterable(extraEdge.getDependent())) {
        if (candidateAppos.getRelation().toString().equals("appos")) {
          extraEdges.add(new SemanticGraphEdge(extraEdge.getGovernor(), candidateAppos.getGovernor(), extraEdge.getRelation(), extraEdge.getWeight(), extraEdge.isExtra()));
        }
      }
      for (SemanticGraphEdge candidateAppos : tree.outgoingEdgeIterable(extraEdge.getDependent())) {
        if (candidateAppos.getRelation().toString().equals("appos")) {
          extraEdges.add(new SemanticGraphEdge(extraEdge.getGovernor(), candidateAppos.getDependent(), extraEdge.getRelation(), extraEdge.getWeight(), extraEdge.isExtra()));
        }
      }
    }

    // Brute force ensure tree
    // Remove incoming edges from roots
    List<SemanticGraphEdge> rootIncomingEdges = new ArrayList<>();
    for (IndexedWord root : tree.getRoots()) {
      for (SemanticGraphEdge incomingEdge : tree.incomingEdgeIterable(root)) {
        rootIncomingEdges.add(incomingEdge);
      }
    }
    rootIncomingEdges.forEach(tree::removeEdge);
    // Loop until it becomes a tree.
    boolean changed = true;
    while (changed) {  // I just want trees to be trees; is that so much to ask!?
      changed = false;
      List<IndexedWord> danglingNodes = new ArrayList<>();
      List<SemanticGraphEdge> invalidEdges = new ArrayList<>();

      for (IndexedWord vertex : tree.vertexSet()) {
        // Collect statistics
        Iterator<SemanticGraphEdge> incomingIter = tree.incomingEdgeIterator(vertex);
        boolean hasIncoming = incomingIter.hasNext();
        boolean hasMultipleIncoming = false;
        if (hasIncoming) {
          incomingIter.next();
          hasMultipleIncoming = incomingIter.hasNext();
        }

        // Register actions
        if (!hasIncoming && !tree.getRoots().contains(vertex)) {
          danglingNodes.add(vertex);
        } else {
          if (hasMultipleIncoming) {
            for (SemanticGraphEdge edge : new IterableIterator<>(incomingIter)) {
              invalidEdges.add(edge);
            }
          }
        }
      }

      // Perform actions
      for (IndexedWord vertex : danglingNodes) {
        tree.removeVertex(vertex);
        changed = true;
      }
      for (SemanticGraphEdge edge : invalidEdges) {
        tree.removeEdge(edge);
        changed = true;
      }
    }

    // Return
    assert isTree(tree);
    return extraEdges;
  }

  /**
   * A little utility function to make sure a SemanticGraph is a tree.
   * @param tree The tree to check.
   * @return True if this {@link edu.stanford.nlp.semgraph.SemanticGraph} is a tree (versus a DAG, or Graph).
   */
  public static boolean isTree(SemanticGraph tree) {
    for (IndexedWord vertex : tree.vertexSet()) {
      // Check one and only one incoming edge
      if (tree.getRoots().contains(vertex)) {
        if (tree.incomingEdgeIterator(vertex).hasNext()) {
          return false;
        }
      } else {
        Iterator<SemanticGraphEdge> iter = tree.incomingEdgeIterator(vertex);
        if (!iter.hasNext()) {
          return false;
        }
        iter.next();
        if (iter.hasNext()) {
          return false;
        }
      }
      // Check incoming and outgoing edges match
      for (SemanticGraphEdge edge : tree.outgoingEdgeIterable(vertex)) {
        boolean foundReverse = false;
        for (SemanticGraphEdge reverse : tree.incomingEdgeIterable(edge.getDependent())) {
          if (reverse == edge) { foundReverse = true; }
        }
        if (!foundReverse) {
          return false;
        }
      }
      for (SemanticGraphEdge edge : tree.incomingEdgeIterable(vertex)) {
        boolean foundReverse = false;
        for (SemanticGraphEdge reverse : tree.outgoingEdgeIterable(edge.getGovernor())) {
          if (reverse == edge) { foundReverse = true; }
        }
        if (!foundReverse) {
          return false;
        }
      }
    }
    // Check topological sort -- sometimes fails?
//    try {
//      tree.topologicalSort();
//    } catch (Exception e) {
//      e.printStackTrace();
//      return false;
//    }
    return true;
  }


  /**
   * Returns true if the given two spans denote the same consistent NER chunk. That is, if we call
   * {@link Util#extractNER(List, Span)} on these two spans, they would return the same span.
   *
   * @param tokens The tokens in the sentence.
   * @param a The first span.
   * @param b The second span.
   * @param parse The parse tree to traverse looking for coreference chains to exploit.
   *
   * @return True if these two spans contain exactly the same NER.
   */
  public static boolean nerOverlap(List<CoreLabel> tokens, Span a, Span b, Optional<SemanticGraph> parse) {
    Span nerA = extractNER(tokens, a);
    Span nerB = extractNER(tokens, b);
    return nerA.equals(nerB);
  }

  /** @see Util#nerOverlap(List, Span, Span, Optional) */
  public static boolean nerOverlap(List<CoreLabel> tokens, Span a, Span b) {
    return nerOverlap(tokens, a, b, Optional.empty());
  }

  /**
   * A helper function for dumping the accuracy of the trained classifier.
   *
   * @param classifier The classifier to evaluate.
   * @param dataset The dataset to evaluate the classifier on.
   */
  public static void dumpAccuracy(Classifier<ClauseSplitter.ClauseClassifierLabel, String> classifier, GeneralDataset<ClauseSplitter.ClauseClassifierLabel, String> dataset) {
    DecimalFormat df = new DecimalFormat("0.000");
    log("size:         " + dataset.size());
    log("split count:  " + StreamSupport.stream(dataset.spliterator(), false).filter(x -> x.label() == ClauseSplitter.ClauseClassifierLabel.CLAUSE_SPLIT).collect(Collectors.toList()).size());
    log("interm count: " + StreamSupport.stream(dataset.spliterator(), false).filter(x -> x.label() == ClauseSplitter.ClauseClassifierLabel.CLAUSE_INTERM).collect(Collectors.toList()).size());
    Pair<Double, Double> pr = classifier.evaluatePrecisionAndRecall(dataset, ClauseSplitter.ClauseClassifierLabel.CLAUSE_SPLIT);
    log("p  (split):   " + df.format(pr.first));
    log("r  (split):   " + df.format(pr.second));
    log("f1 (split):   " + df.format(2 * pr.first * pr.second / (pr.first + pr.second)));
    pr = classifier.evaluatePrecisionAndRecall(dataset, ClauseSplitter.ClauseClassifierLabel.CLAUSE_INTERM);
    log("p  (interm):  " + df.format(pr.first));
    log("r  (interm):  " + df.format(pr.second));
    log("f1 (interm):  " + df.format(2 * pr.first * pr.second / (pr.first + pr.second)));
  }

  /**
   * The dictionary of privative adjectives, as per http://hci.stanford.edu/cstr/reports/2014-04.pdf
   */
  public static final Set<String> PRIVATIVE_ADJECTIVES = Collections.unmodifiableSet(new HashSet<String>(){{
     add("believed");
     add("debatable");
     add("disputed");
     add("dubious");
     add("hypothetical");
     add("impossible");
     add("improbable");
     add("plausible");
     add("putative");
     add("questionable");
     add("so called");
     add("supposed");
     add("suspicious");
     add("theoretical");
     add("uncertain");
     add("unlikely");
     add("would - be");
     add("apparent");
     add("arguable");
     add("assumed");
     add("likely");
     add("ostensible");
     add("possible");
     add("potential");
     add("predicted");
     add("presumed");
     add("probable");
     add("seeming");
     add("anti");
     add("fake");
     add("fictional");
     add("fictitious");
     add("imaginary");
     add("mythical");
     add("phony");
     add("false");
     add("artificial");
     add("erroneous");
     add("mistaken");
     add("mock");
     add("pseudo");
     add("simulated");
     add("spurious");
     add("deputy");
     add("faulty");
     add("virtual");
     add("doubtful");
     add("erstwhile");
     add("ex");
     add("expected");
     add("former");
     add("future");
     add("onetime");
     add("past");
     add("proposed");
  }});
}
