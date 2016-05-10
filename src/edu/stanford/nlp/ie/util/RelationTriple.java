package edu.stanford.nlp.ie.util;

import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.Util;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.util.PriorityQueue;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A (subject, relation, object) triple; e.g., as used in the KBP challenges or in OpenIE systems.
 *
 * @author Gabor Angeli
 */
@SuppressWarnings("UnusedDeclaration")
public class RelationTriple implements Comparable<RelationTriple>, Iterable<CoreLabel> {
  /** The subject (first argument) of this triple */
  public final List<CoreLabel> subject;
  /** The relation (second argument) of this triple */
  public final List<CoreLabel> relation;
  /** The object (third argument) of this triple */
  public final List<CoreLabel> object;
  /** An optional score (confidence) for this triple */
  public final double confidence;

  /**
   * Create a new triple with known values for the subject, relation, and object.
   * For example, "(cats, play with, yarn)"
   * @param subject The subject of this triple; e.g., "cats".
   * @param relation The relation of this triple; e.g., "play with".
   * @param object The object of this triple; e.g., "yarn".
   */
  public RelationTriple(List<CoreLabel> subject, List<CoreLabel> relation, List<CoreLabel> object,
                        double confidence) {
    this.subject = subject;
    this.relation = relation;
    this.object = object;
    this.confidence = confidence;
  }

  /**
   * @see edu.stanford.nlp.ie.util.RelationTriple#RelationTriple(java.util.List, java.util.List, java.util.List, double)
   */
  public RelationTriple(List<CoreLabel> subject, List<CoreLabel> relation, List<CoreLabel> object) {
    this(subject, relation, object, 1.0);
  }

  /**
   * Returns all the tokens in the extraction, in the order subject then relation then object.
   */
  public List<CoreLabel> allTokens() {
    List<CoreLabel> allTokens = new ArrayList<>();
    allTokens.addAll(subject);
    allTokens.addAll(relation);
    allTokens.addAll(object);
    return allTokens;
  }

  /** The subject of this relation triple, as a String */
  public String subjectGloss() {
    return StringUtils.join(subject.stream().map(CoreLabel::word), " ");
  }

  /** The head of the subject of this relation triple. */
  public CoreLabel subjectHead() {
    return subject.get(subject.size() - 1);
  }

  /**
   * The subject of this relation triple, as a String of the subject's lemmas.
   * This method will additionally strip out punctuation as well.
   */
   public String subjectLemmaGloss() {
    return StringUtils.join(subject.stream().filter(x -> !x.tag().matches("[\\.\\?,:;'\"!]")).map(CoreLabel::lemma), " ");
  }

  /** The object of this relation triple, as a String */
  public String objectGloss() {
    return StringUtils.join(object.stream().map(CoreLabel::word), " ");
  }

  /** The head of the object of this relation triple. */
  public CoreLabel objectHead() {
    return object.get(object.size() - 1);
  }

  /**
   * The object of this relation triple, as a String of the object's lemmas.
   * This method will additionally strip out punctuation as well.
   */
  public String objectLemmaGloss() {
    return StringUtils.join(object.stream().filter(x -> !x.tag().matches("[\\.\\?,:;'\"!]")).map(CoreLabel::lemma), " ");
  }

  /**
   * The relation of this relation triple, as a String
   */
  public String relationGloss() {
    return StringUtils.join(relation.stream().map(CoreLabel::word), " ");
  }

  /**
   * The relation of this relation triple, as a String of the relation's lemmas.
   * This method will additionally strip out punctuation as well, and lower-cases the relation.
   */
  public String relationLemmaGloss() {
    return StringUtils.join(relation.stream()
        .filter(x -> !x.tag().matches("[\\.\\?,:;'\"!]") && !x.lemma().matches("[\\.,;'\"\\?!]")).map(CoreLabel::lemma), " ").toLowerCase();
  }

  /** A textual representation of the confidence. */
  public String confidenceGloss() {
    return new DecimalFormat("0.000").format(confidence);
  }

  protected Pair<Integer, Integer> getSpan(List<CoreLabel> tokens, Function<CoreLabel, Integer> toMin, Function<CoreLabel, Integer> toMax) {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (CoreLabel token : tokens) {
      min = Math.min(min, toMin.apply(token));
      max = Math.max(max, toMax.apply(token) + 1);
    }
    return Pair.makePair(min, max);
  }

  public Pair<Integer, Integer> subjectTokenSpan() {
    return getSpan(subject, x -> x.index() - 1, x -> x.index() - 1);
  }

  public Pair<Integer, Integer> objectTokenSpan() {
    return getSpan(object, x -> x.index() - 1, x -> x.index() - 1);
  }

  /** An optional method, returning the dependency tree this triple was extracted from */
  public Optional<SemanticGraph> asDependencyTree() {
    return Optional.empty();
  }

  /** Return the given relation triple as a flat sentence */
  public List<CoreLabel> asSentence() {
    PriorityQueue<CoreLabel> orderedSentence = new FixedPrioritiesPriorityQueue<>();
    double defaultIndex = 0.0;
    for (CoreLabel token : subject) {
      orderedSentence.add(token, token.index() >= 0 ? (double) -token.index() : -defaultIndex);
      defaultIndex += 1.0;
    }
    for (CoreLabel token : relation) {
      orderedSentence.add(token, token.index() >= 0 ? (double) -token.index() : -defaultIndex);
      defaultIndex += 1.0;
    }
    for (CoreLabel token : object) {
      orderedSentence.add(token, token.index() >= 0 ? (double) -token.index() : -defaultIndex);
      defaultIndex += 1.0;
    }
    return orderedSentence.toSortedList();
  }


  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RelationTriple)) return false;
    RelationTriple that = (RelationTriple) o;
    return object.equals(that.object) && relation.equals(that.relation) && subject.equals(that.subject);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    int result = subject.hashCode();
    result = 31 * result + relation.hashCode();
    result = 31 * result + object.hashCode();
    return result;
  }

  /** Print a human-readable description of this relation triple, as a tab-separated line */
  @Override
  public String toString() {
    return "" + this.confidence + "\t" + subjectGloss() + "\t" + relationGloss() + "\t" + objectGloss();
  }

  @Override
  public int compareTo(RelationTriple o) {
    if (this.confidence < o.confidence) {
      return -1;
    } else if (this.confidence > o.confidence) {
      return 1;
    } else {
      return 0;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<CoreLabel> iterator() {
    return CollectionUtils.concatIterators(subject.iterator(), relation.iterator(), object.iterator());
  }


  /**
   * A {@link edu.stanford.nlp.ie.util.RelationTriple}, but with the tree saved as well.
   */
  public static class WithTree extends RelationTriple {
    public final SemanticGraph sourceTree;

    /**
     * Create a new triple with known values for the subject, relation, and object.
     * For example, "(cats, play with, yarn)"
     *
     * @param subject  The subject of this triple; e.g., "cats".
     * @param relation The relation of this triple; e.g., "play with".
     * @param object   The object of this triple; e.g., "yarn".
     * @param tree     The tree this extraction was created from; we create a deep copy of the tree.
     */
    public WithTree(List<CoreLabel> subject, List<CoreLabel> relation, List<CoreLabel> object, SemanticGraph tree,
                    double confidence) {
      super(subject, relation, object, confidence);
      this.sourceTree = new SemanticGraph(tree);
    }

    /** The head of the subject of this relation triple. */
    public CoreLabel subjectHead() {
      if (subject.size() == 1) { return subject.get(0); }
      Span subjectSpan = Span.fromValues(subject.get(0).index(), subject.get(subject.size() - 1).index());
      for (int i = subject.size() - 1; i >= 0; --i) {
        for (SemanticGraphEdge edge : sourceTree.incomingEdgeIterable(new IndexedWord(subject.get(i)))) {
          if (edge.getGovernor().index() < subjectSpan.start() || edge.getGovernor().index() >= subjectSpan.end()) {
            return subject.get(i);
          }
        }
      }
      return subject.get(subject.size() - 1);
    }

    /** The head of the object of this relation triple. */
    public CoreLabel objectHead() {
      if (object.size() == 1) { return object.get(0); }
      Span objectSpan = Span.fromValues(object.get(0).index(), object.get(object.size() - 1).index());
      for (int i = object.size() - 1; i >= 0; --i) {
        for (SemanticGraphEdge edge : sourceTree.incomingEdgeIterable(new IndexedWord(object.get(i)))) {
          if (edge.getGovernor().index() < objectSpan.start() || edge.getGovernor().index() >= objectSpan.end()) {
            return object.get(i);
          }
        }
      }
      return object.get(object.size() - 1);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<SemanticGraph> asDependencyTree() {
      return Optional.of(sourceTree);
    }
  }

  /** A list of patterns to match relation extractions against */
  private static List<SemgrexPattern> VERB_PATTERNS = Collections.unmodifiableList(new ArrayList<SemgrexPattern>() {{
    // { blue cats play [quietly] with yarn,
    //   Jill blew kisses at Jack,
    //   cats are standing next to dogs }
    add(SemgrexPattern.compile("{$}=verb ?>/cop|aux(pass)?/ {}=be >/.subj(pass)?/ {}=subject >/(nmod|acl|advcl):.*/=prepEdge ( {}=object ?>appos {} = appos ) ?>dobj {pos:/N.*/}=relObj"));
    // { fish like to swim }
    add(SemgrexPattern.compile("{$}=verb >/.subj(pass)?/ {}=subject >xcomp ( {}=object ?>appos {}=appos )"));
    // { cats have tails }
    add(SemgrexPattern.compile("{$}=verb ?>/aux(pass)?/ {}=be >/.subj(pass)?/ {}=subject >/[di]obj|xcomp/ ( {}=object ?>appos {}=appos )"));
    // { cats are cute,
    //   horses are grazing peacefully }
    add(SemgrexPattern.compile("{$}=object >/.subj(pass)?/ {}=subject >/cop|aux(pass)?/ {}=verb"));
    // { Tom and Jerry were fighting }
    add(SemgrexPattern.compile("{$}=verb >nsubjpass ( {}=subject >/conj:and/=subjIgnored {}=object )"));
    // { There are dogs in heaven }
    add(SemgrexPattern.compile("{lemma:be}=verb ?>expl {} >/.subj(pass)?/ ( {}=subject >/(nmod|acl|advcl):.*/=prepEdge ( {}=object ?>appos {} = appos ) ?>dobj {pos:/N.*/}=relObj )"));
  }});

  /**
   * A set of nominal patterns, that don't require being in a coherent clause, but do require NER information.
   */
  private static List<TokenSequencePattern> NOUN_TOKEN_PATTERNS = Collections.unmodifiableList(new ArrayList<TokenSequencePattern>() {{
    // { NER nominal_verb NER,
    //   United States president Obama }
    add(TokenSequencePattern.compile("(?$object [ner:/PERSON|ORGANIZATION|LOCATION+/]+ ) (?$beof_comp [ {tag:/NN.*/} & !{ner:/PERSON|ORGANIZATION|LOCATION/} ]+ ) (?$subject [ner:/PERSON|ORGANIZATION|LOCATION/]+ )"));
    // { NER 's nominal_verb NER,
    //   America 's president , Obama }
    add(TokenSequencePattern.compile("(?$object [ner:/PERSON|ORGANIZATION|LOCATION+/]+ ) /'s/ (?$beof_comp [ {tag:/NN.*/} & !{ner:/PERSON|ORGANIZATION|LOCATION/} ]+ ) /,/? (?$subject [ner:/PERSON|ORGANIZATION|LOCATION/]+ )"));
    // { NER , NER ,,
    //   Obama, 28, ...,
    //   Obama (28) ...}
    add(TokenSequencePattern.compile("(?$subject [ner:/PERSON|ORGANIZATION|LOCATION/]+ ) /,/ (?$object [ner:/NUMBER|DURATION|PERSON|ORGANIZATION/]+ ) /,/"));
    add(TokenSequencePattern.compile("(?$subject [ner:/PERSON|ORGANIZATION|LOCATION/]+ ) /\\(/ (?$object [ner:/NUMBER|DURATION|PERSON|ORGANIZATION/]+ ) /\\)/"));
  }});

  /**
   * A set of nominal patterns using dependencies, that don't require being in a coherent clause, but do require NER information.
   */
  private static List<SemgrexPattern> NOUN_DEPENDENCY_PATTERNS = Collections.unmodifiableList(new ArrayList<SemgrexPattern>() {{
    // { Durin, son of Thorin }
    add(SemgrexPattern.compile("{}=subject >appos ( {}=relation >/nmod:.*/=relaux {}=object)"));
    // { Thorin's son, Durin }
    add(SemgrexPattern.compile("{}=relation >/nmod:.*/=relaux {}=subject >appos {}=object"));
    //  { President Obama }
    add(SemgrexPattern.compile("{ner:/PERSON|ORGANIZATION|LOCATION/}=subject >/amod|compound/=arc {ner:/..+/}=object"));
    // { Chris Manning of Stanford }
    add(SemgrexPattern.compile("{ner:/PERSON|ORGANIZATION|LOCATION/}=subject >/nmod:.*/=relation {ner:/..+/}=object"));
  }});

  /**
   * Extract the nominal patterns from this sentence.
   *
   * @see RelationTriple#NOUN_TOKEN_PATTERNS
   * @see RelationTriple#NOUN_DEPENDENCY_PATTERNS
   *
   * @param parse The parse tree of the sentence to annotate.
   * @param tokens The tokens of the sentence to annotate.
   * @return A list of {@link RelationTriple}s. Note that these do not have an associated tree with them.
   */
  public static List<RelationTriple> extract(SemanticGraph parse, List<CoreLabel> tokens) {
    List<RelationTriple> extractions = new ArrayList<>();
    Set<Triple<Span,String,Span>> alreadyExtracted = new HashSet<>();

    // Run Token Patterns
    for (TokenSequencePattern tokenPattern : NOUN_TOKEN_PATTERNS) {
      TokenSequenceMatcher tokenMatcher = tokenPattern.matcher(tokens);
      while (tokenMatcher.find()) {
        // Create subject
        List<? extends CoreMap> subject = tokenMatcher.groupNodes("$subject");
        Span subjectSpan = Util.extractNER(tokens, Span.fromValues(((CoreLabel) subject.get(0)).index() - 1, ((CoreLabel) subject.get(subject.size() - 1)).index()));
        List<CoreLabel> subjectTokens = new ArrayList<>();
        for (int i : subjectSpan) {
          subjectTokens.add(tokens.get(i));
        }
        // Create object
        List<? extends CoreMap> object = tokenMatcher.groupNodes("$object");
        Span objectSpan = Util.extractNER(tokens, Span.fromValues(((CoreLabel) object.get(0)).index() - 1, ((CoreLabel) object.get(object.size() - 1)).index()));
        if (Span.overlaps(subjectSpan, objectSpan)) {
          continue;
        }
        List<CoreLabel> objectTokens = new ArrayList<>();
        for (int i : objectSpan) {
          objectTokens.add(tokens.get(i));
        }
        // Create relation
        if (subjectTokens.size() > 0 && objectTokens.size() > 0) {
          List<CoreLabel> relationTokens = new ArrayList<>();
          // (add the 'be')
          relationTokens.add(new CoreLabel() {{
            setWord("is");
            setLemma("be");
            setTag("VBZ");
            setNER("O");
            setBeginPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
            setEndPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
            setSentIndex(subjectTokens.get(subjectTokens.size() - 1).sentIndex());
            setIndex(-1);
          }});
          // (add a complement to the 'be')
          List<? extends CoreMap> beofComp = tokenMatcher.groupNodes("$beof_comp");
          if (beofComp != null) {
            // (add the complement
            for (CoreMap token : beofComp) {
              if (token instanceof CoreLabel) {
                relationTokens.add((CoreLabel) token);
              } else {
                relationTokens.add(new CoreLabel(token));
              }
            }
            // (add the 'of')
            relationTokens.add(new CoreLabel() {{
              setWord("of");
              setLemma("of");
              setTag("IN");
              setNER("O");
              setBeginPosition(objectTokens.get(0).beginPosition());
              setEndPosition(objectTokens.get(0).beginPosition());
              setSentIndex(objectTokens.get(0).sentIndex());
              setIndex(-1);
            }});
          }
          // Add extraction
          String relationGloss = StringUtils.join(relationTokens.stream().map(CoreLabel::word), " ");
          if (!alreadyExtracted.contains(Triple.makeTriple(subjectSpan, relationGloss, objectSpan))) {
            extractions.add(new RelationTriple(subjectTokens, relationTokens, objectTokens));
            alreadyExtracted.add(Triple.makeTriple(subjectSpan, relationGloss, objectSpan));
          }
        }
      }

      // Run Semgrex Matches
      for (SemgrexPattern semgrex : NOUN_DEPENDENCY_PATTERNS) {
        SemgrexMatcher matcher = semgrex.matcher(parse);
        while (matcher.find()) {
          // Create subject
          IndexedWord subject = matcher.getNode("subject");
          Span subjectSpan = Util.extractNER(tokens, Span.fromValues(subject.index() - 1, subject.index()));
          List<CoreLabel> subjectTokens = new ArrayList<>();
          for (int i : subjectSpan) {
            subjectTokens.add(tokens.get(i));
          }
          // Create object
          IndexedWord object = matcher.getNode("object");
          Span objectSpan = Util.extractNER(tokens, Span.fromValues(object.index() - 1, object.index()));
          List<CoreLabel> objectTokens = new ArrayList<>();
          for (int i : objectSpan) {
            objectTokens.add(tokens.get(i));
          }
          // Check that the pair is valid
          if (Span.overlaps(subjectSpan, objectSpan)) {
            continue;  // We extracted an identity
          }
          if (subjectSpan.end() == objectSpan.start() - 1 &&
              (tokens.get(subjectSpan.end()).word().matches("[\\.,:;\\('\"]") ||
                  "CC".equals(tokens.get(subjectSpan.end()).tag()))) {
            continue; // We're straddling a clause
          }
          if (objectSpan.end() == subjectSpan.start() - 1 &&
              (tokens.get(objectSpan.end()).word().matches("[\\.,:;\\('\"]") ||
                  "CC".equals(tokens.get(objectSpan.end()).tag()))) {
            continue; // We're straddling a clause
          }
          // Get the relation
          if (subjectTokens.size() > 0 && objectTokens.size() > 0) {
            LinkedList<CoreLabel> relationTokens = new LinkedList<>();
            IndexedWord relNode = matcher.getNode("relation");
            if (relNode != null) {
              // (add the relation)
              relationTokens.add(relNode.backingLabel());
              // (check for aux information)
              String relaux = matcher.getRelnString("relaux");
              if (relaux != null && relaux.startsWith("nmod:") && !"nmod:poss".equals(relaux)) {
                relationTokens.add(new CoreLabel() {{
                  setWord(relaux.substring("nmod:".length()));
                  setLemma(relaux.substring("nmod:".length()));
                  setTag("PP");
                  setNER("O");
                  setBeginPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setEndPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setSentIndex(subjectTokens.get(subjectTokens.size() - 1).sentIndex());
                  setIndex(-1);
                }});
              } else if (relaux != null && "nmod:poss".equals(relaux)) {
                relationTokens.addFirst(new CoreLabel() {{
                  setWord("'s");
                  setLemma("'s");
                  setTag("PP");
                  setNER("O");
                  setBeginPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setEndPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setSentIndex(subjectTokens.get(subjectTokens.size() - 1).sentIndex());
                  setIndex(-1);
                }});
                relationTokens.addLast(new CoreLabel() {{
                  setWord("is");
                  setLemma("be");
                  setTag("VBZ");
                  setNER("O");
                  setBeginPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setEndPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setSentIndex(subjectTokens.get(subjectTokens.size() - 1).sentIndex());
                  setIndex(-1);
                }});
              }
            } else {
              // (add the 'be')
              relationTokens.add(new CoreLabel() {{
                setWord("is");
                setLemma("be");
                setTag("VBZ");
                setNER("O");
                setBeginPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                setEndPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                setSentIndex(subjectTokens.get(subjectTokens.size() - 1).sentIndex());
                setIndex(-1);
              }});
              // (add an optional prep)
              String rel = matcher.getRelnString("relation");
              String prep = null;
              if (rel != null && rel.startsWith("nmod:") && !"nmod:poss".equals(rel)) {
                prep = rel.substring("nmod:".length());
              } else if (rel != null && (rel.startsWith("acl:") || rel.startsWith("advcl:")) ) {
                prep = rel.substring(rel.indexOf(":"));
              } else if (rel != null && rel.equals("nmod:poss")) {
                relationTokens.clear();
                prep = "'s";
              }
              if (prep != null) {
                final String p = prep;
                relationTokens.add(new CoreLabel() {{
                  setWord(p);
                  setLemma(p);
                  setTag("PP");
                  setNER("O");
                  setBeginPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setEndPosition(subjectTokens.get(subjectTokens.size() - 1).endPosition());
                  setSentIndex(subjectTokens.get(subjectTokens.size() - 1).sentIndex());
                  setIndex(-1);
                }});
              }
            }
            // Add extraction
            String relationGloss = StringUtils.join(relationTokens.stream().map(CoreLabel::word), " ");
            if (!alreadyExtracted.contains(Triple.makeTriple(subjectSpan, relationGloss, objectSpan))) {
              extractions.add(new RelationTriple(subjectTokens, relationTokens, objectTokens));
              alreadyExtracted.add(Triple.makeTriple(subjectSpan, relationGloss, objectSpan));
            }
          }
        }
      }
    }

    // Filter downward polarity extractions
    Iterator<RelationTriple> iter = extractions.iterator();
    while (iter.hasNext()) {
      RelationTriple term = iter.next();
      boolean shouldRemove = false;
      for (CoreLabel token : term) {
        if (token.get(NaturalLogicAnnotations.PolarityAnnotation.class) != null &&
            token.get(NaturalLogicAnnotations.PolarityAnnotation.class).isDownwards() ) {
          shouldRemove = true;
        }
      }
      if (shouldRemove) { iter.remove(); }  // Don't extract things in downward polarity contexts.
    }

    // Return
    return extractions;
  }

  /**
   * A counter keeping track of how many times a given pattern has matched. This allows us to learn to iterate
   * over patterns in the optimal order; this is just an efficiency tweak (but an effective one!).
   */
  private static final Counter<SemgrexPattern> VERB_PATTERN_HITS = new ClassicCounter<>();

  /** A set of valid arcs denoting a subject entity we are interested in */
  public static final Set<String> VALID_SUBJECT_ARCS = Collections.unmodifiableSet(new HashSet<String>(){{
    add("amod"); add("compound"); add("aux"); add("nummod"); add("nmod:poss"); add("nmod:tmod"); add("expl");
  }});

  /** A set of valid arcs denoting an object entity we are interested in */
  public static final Set<String> VALID_OBJECT_ARCS = Collections.unmodifiableSet(new HashSet<String>(){{
    add("amod"); add("compound"); add("aux"); add("nummod"); add("nmod"); add("nsubj"); add("nmod:*"); add("nmod:poss");
    add("nmod:tmod"); add("conj:and"); add("advmod"); add("acl"); add("advcl");
  }});

  /** A set of valid arcs denoting an entity we are interested in */
  public static final Set<String> VALID_ADVERB_ARCS = Collections.unmodifiableSet(new HashSet<String>(){{
    add("amod"); add("advmod"); add("conj"); add("cc"); add("conj:and"); add("conj:or"); add("auxpass");
  }});

  private static CoreLabel mockNode(CoreLabel toCopy, int offset, String word, String POS) {
    CoreLabel mock = new CoreLabel(toCopy);
    mock.setWord(word);
    mock.setLemma(word);
    mock.setValue(word);
    mock.setNER("O");
    mock.setTag(POS);
    mock.setIndex(toCopy.index() + offset);
    return mock;
  }

  /**
   * @see RelationTriple#getValidSubjectChunk(edu.stanford.nlp.semgraph.SemanticGraph, edu.stanford.nlp.ling.IndexedWord, Optional)
   * @see RelationTriple#getValidObjectChunk(edu.stanford.nlp.semgraph.SemanticGraph, edu.stanford.nlp.ling.IndexedWord, Optional)
   * @see RelationTriple#getValidAdverbChunk(edu.stanford.nlp.semgraph.SemanticGraph, edu.stanford.nlp.ling.IndexedWord, Optional)
   */
  @SuppressWarnings("StatementWithEmptyBody")
  private static Optional<List<CoreLabel>> getValidChunk(SemanticGraph parse, IndexedWord originalRoot,
                                                         Set<String> validArcs, Optional<String> ignoredArc) {
    PriorityQueue<CoreLabel> chunk = new FixedPrioritiesPriorityQueue<>();
    Queue<IndexedWord> fringe = new LinkedList<>();
    IndexedWord root = originalRoot;
    fringe.add(root);

    boolean isCopula = false;
    for (SemanticGraphEdge edge : parse.outgoingEdgeIterable(originalRoot)) {
      String shortName = edge.getRelation().getShortName();
      if (shortName.equals("cop") || shortName.equals("auxpass")) {
        isCopula = true;
      }
    }

    while (!fringe.isEmpty()) {
      root = fringe.poll();
      chunk.add(root.backingLabel(), -root.index());
      for (SemanticGraphEdge edge : parse.incomingEdgeIterable(root)) {
        if (edge.getDependent() != originalRoot) {
          String relStr = edge.getRelation().toString();
          if ((relStr.startsWith("nmod:") && !"nmod:poss".equals(relStr)) ||
              relStr.startsWith("acl:") || relStr.startsWith("advcl:")) {
            chunk.add(mockNode(edge.getGovernor().backingLabel(), 1, edge.getRelation().toString().substring(edge.getRelation().toString().indexOf(":") + 1), "PP"), -(((double) edge.getGovernor().index()) + 0.9));
          }
          if (edge.getRelation().getShortName().equals("conj")) {
            chunk.add(mockNode(root.backingLabel(), -1, edge.getRelation().getSpecific(), "CC"), -(((double) root.index()) - 0.9));
          }
        }
      }
      for (SemanticGraphEdge edge : parse.getOutEdgesSorted(root)) {
        String shortName = edge.getRelation().getShortName();
        String name = edge.getRelation().toString();
        //noinspection StatementWithEmptyBody
        if (isCopula && (shortName.equals("cop") || shortName.contains("subj") || shortName.equals("auxpass"))) {
          // noop; ignore nsubj, cop for extractions with copula
        } else if (ignoredArc.isPresent() && ignoredArc.get().equals(name)) {
          // noop; ignore explicitly requested noop arc.
        } else if (!validArcs.contains(edge.getRelation().getShortName().replaceAll(":.*",":*"))) {
          return Optional.empty();
        } else {
          fringe.add(edge.getDependent());
        }
      }
    }

    return Optional.of(chunk.toSortedList());
  }

  /**
   * Get the yield of a given subtree, if it is a valid subject.
   * Otherwise, return {@link java.util.Optional#empty()}}.
   * @param parse The parse tree we are extracting a subtree from.
   * @param root The root of the subtree.
   * @param noopArc An optional edge type to ignore in gathering the chunk.
   * @return If this subtree is a valid entity, we return its yield. Otherwise, we return empty.
   */
  private static Optional<List<CoreLabel>> getValidSubjectChunk(SemanticGraph parse, IndexedWord root, Optional<String> noopArc) {
    return getValidChunk(parse, root, VALID_SUBJECT_ARCS, noopArc);
  }

  /**
   * Get the yield of a given subtree, if it is a valid object.
   * Otherwise, return {@link java.util.Optional#empty()}}.
   * @param parse The parse tree we are extracting a subtree from.
   * @param root The root of the subtree.
   * @param noopArc An optional edge type to ignore in gathering the chunk.
   * @return If this subtree is a valid entity, we return its yield. Otherwise, we return empty.
   */
  private static Optional<List<CoreLabel>> getValidObjectChunk(SemanticGraph parse, IndexedWord root, Optional<String> noopArc) {
    return getValidChunk(parse, root, VALID_OBJECT_ARCS, noopArc);
  }

  /**
   * Get the yield of a given subtree, if it is a adverb chunk.
   * Otherwise, return {@link java.util.Optional#empty()}}.
   * @param parse The parse tree we are extracting a subtree from.
   * @param root The root of the subtree.
   * @param noopArc An optional edge type to ignore in gathering the chunk.
   * @return If this subtree is a valid adverb, we return its yield. Otherwise, we return empty.
   */
  private static Optional<List<CoreLabel>> getValidAdverbChunk(SemanticGraph parse, IndexedWord root, Optional<String> noopArc) {
    return getValidChunk(parse, root, VALID_ADVERB_ARCS, noopArc);
  }

  /**
   * <p>
   * Try to segment this sentence as a relation triple.
   * This sentence must already match one of a few strict patterns for a valid OpenIE extraction.
   * If it does not, then no relation triple is created.
   * That is, this is <b>not</b> a relation extractor; it is just a utility to segment what is already a
   * (subject, relation, object) triple into these three parts.
   * </p>
   *
   * @param parse The sentence to process, as a dependency tree.
   * @param confidence An optional confidence to pass on to the relation triple.
   * @param consumeAll if true, force the entire parse to be consumed by the pattern.
   * @return A relation triple, if this sentence matches one of the patterns of a valid relation triple.
   */
  public static Optional<RelationTriple> segment(SemanticGraph parse, Optional<Double> confidence, boolean consumeAll) {
    PATTERN_LOOP: for (SemgrexPattern pattern : VERB_PATTERNS) {  // For every candidate pattern...
      SemgrexMatcher m = pattern.matcher(parse);
      if (m.matches()) {  // ... see if it matches the sentence
        if ("nmod:poss".equals(m.getRelnString("prepEdge"))) {
          continue;   // nmod:poss is not a preposition!
        }
        // some JIT on the pattern ordering
        // note[Gabor]: This actually helps quite a bit; 72->86 sentences per second for the entire OpenIE pipeline.
        VERB_PATTERN_HITS.incrementCount(pattern);
        if (((int) VERB_PATTERN_HITS.totalCount()) % 1000 == 0) {
          ArrayList<SemgrexPattern> newPatterns = new ArrayList<>(VERB_PATTERNS);
          Collections.sort(newPatterns, (x, y) ->
              (int) (VERB_PATTERN_HITS.getCount(y) - VERB_PATTERN_HITS.getCount(x))
          );
          VERB_PATTERNS = newPatterns;
        }
        // Main code
        int numKnownDependents = 2;  // subject and object, at minimum
        // Object
        IndexedWord object = m.getNode("appos");
        if (object == null) {
          object = m.getNode("object");
        }
        assert object != null;
        // Verb
        PriorityQueue<CoreLabel> verbChunk = new FixedPrioritiesPriorityQueue<>();
        IndexedWord verb = m.getNode("verb");
        List<IndexedWord> adverbs = new ArrayList<>();
        Optional<String> subjNoopArc = Optional.empty();
        Optional<String> objNoopArc = Optional.empty();
        if (verb != null) {
          // Case: a standard extraction with a main verb
          IndexedWord relObj = m.getNode("relObj");
          for (SemanticGraphEdge edge : parse.outgoingEdgeIterable(verb)) {
            if ("advmod".equals(edge.getRelation().toString()) || "amod".equals(edge.getRelation().toString())) {
              // Add adverb modifiers
              String tag = edge.getDependent().backingLabel().tag();
              if (tag == null ||
                  (!tag.startsWith("W") && !edge.getDependent().backingLabel().word().equalsIgnoreCase("then"))) {  // prohibit advmods like "where"
                adverbs.add(edge.getDependent());
              }
            } else if (edge.getDependent().equals(relObj)) {
              // Add additional object to the relation
              Optional<List<CoreLabel>> relObjSpan = getValidChunk(parse, relObj, Collections.singleton("compound"), Optional.empty());
              if (!relObjSpan.isPresent()) {
                continue PATTERN_LOOP;
              } else {
                for (CoreLabel token : relObjSpan.get()) {
                  verbChunk.add(token, -token.index());
                }
                numKnownDependents += 1;
              }
            }
          }
          // Special case for possessive with verb
          if ("nmod:poss".equals(m.getRelnString("verb"))) {
            verbChunk.add(mockNode(verb.backingLabel(), -1, "'s", "POS"), ((double) verb.backingLabel().index()) - 0.9);
          }
        } else {
          // Case: an implicit extraction where the 'verb' comes from a relation arc.
          String verbName = m.getRelnString("verb");
          if ("nmod:poss".equals(verbName)) {
            IndexedWord subject = m.getNode("subject");
            verb = new IndexedWord(mockNode(subject.backingLabel(), 1, "'s", "POS"));
            objNoopArc = Optional.of("nmod:poss");
          } else if (verbName != null && verbName.startsWith("nmod:")) {
            verbName = verbName.substring("nmod:".length()).replace("_", " ");
            IndexedWord subject = m.getNode("subject");
            verb = new IndexedWord(mockNode(subject.backingLabel(), 1, verbName, "IN"));
            subjNoopArc = Optional.of("nmod:" + verbName);
          } else {
            throw new IllegalStateException("Pattern matched without a verb!");
          }
        }
        verbChunk.add(verb.backingLabel(), -verb.index());
        // Prepositions
        IndexedWord prep = m.getNode("prep");
        String prepEdge = m.getRelnString("prepEdge");
        if (prep != null) { verbChunk.add(prep.backingLabel(), -prep.index()); numKnownDependents += 1; }
        // Auxilliary "be"
        IndexedWord be = m.getNode("be");
        if (be != null) { verbChunk.add(be.backingLabel(), -be.index()); numKnownDependents += 1; }
        // (adverbs have to be well-formed)
        if (!adverbs.isEmpty()) {
          Set<CoreLabel> adverbialModifiers = new HashSet<>();
          for (IndexedWord adv : adverbs) {
            Optional<List<CoreLabel>> adverbChunk = getValidAdverbChunk(parse, adv, Optional.empty());
            if (adverbChunk.isPresent()) {
              adverbialModifiers.addAll(adverbChunk.get().stream().collect(Collectors.toList()));
            } else {
              continue PATTERN_LOOP;  // Invalid adverbial phrase
            }
            numKnownDependents += 1;
          }
          for (CoreLabel adverbToken : adverbialModifiers) {
            verbChunk.add(adverbToken, -adverbToken.index());
          }
        }
        // (add preposition edge)
        if (prepEdge != null) {
          verbChunk.add(mockNode(verb.backingLabel(), 1, prepEdge.substring(prepEdge.indexOf(":") + 1).replace("_", " "), "PP"), -(verb.index() + 10));
        }
        // (check for additional edges)
        if (consumeAll && parse.outDegree(verb) > numKnownDependents) {
          //noinspection UnnecessaryLabelOnContinueStatement
          continue PATTERN_LOOP;  // Too many outgoing edges; we didn't consume them all.
        }
        List<CoreLabel> relation = verbChunk.toSortedList();

        // Last chance to register ignored edges
        if (!subjNoopArc.isPresent()) {
          subjNoopArc = Optional.ofNullable(m.getRelnString("subjIgnored"));
          if (!subjNoopArc.isPresent()) {
            subjNoopArc = Optional.ofNullable(m.getRelnString("prepEdge"));  // For some strange "there are" cases
          }
        }
        if (!objNoopArc.isPresent()) {
          objNoopArc = Optional.ofNullable(m.getRelnString("objIgnored"));
        }

        // Subject+Object
        Optional<List<CoreLabel>> subjectSpan = getValidSubjectChunk(parse, m.getNode("subject"), subjNoopArc);
        Optional<List<CoreLabel>> objectSpan = getValidObjectChunk(parse, object, objNoopArc);
        // Create relation
        if (subjectSpan.isPresent() && objectSpan.isPresent() &&
            CollectionUtils.intersection(new HashSet<>(subjectSpan.get()), new HashSet<>(objectSpan.get())).isEmpty()
            ) {  // ... and has a valid subject+object
          // Success! Found a valid extraction.
          WithTree extraction = new WithTree(subjectSpan.get(), relation, objectSpan.get(), parse, confidence.orElse(1.0));
          return Optional.of(extraction);
        }
      }
    }
    // Failed to match any pattern; return failure
    return Optional.empty();
  }

  /**
   * Segment the given parse tree, forcing all nodes to be consumed.
   * @see RelationTriple#segment(edu.stanford.nlp.semgraph.SemanticGraph, Optional)
   */
  public static Optional<RelationTriple> segment(SemanticGraph parse, Optional<Double> confidence) {
    return segment(parse, confidence, true);
  }


  /**
   * A {@link edu.stanford.nlp.ie.util.RelationTriple}, optimized for tasks such as KBP
   * where we care about named entities, and care about things like provenance and coref.
   */
  public static class AsKBEntry extends RelationTriple {
    public final String docid;
    public final int sentenceIndex;
    private final Optional<List<CoreLabel>> originalSubject;
    private final Optional<List<CoreLabel>> originalObject;

    /**
     * {@inheritDoc}
     */
    public AsKBEntry(List<CoreLabel> subject, List<CoreLabel> relation, List<CoreLabel> object, double confidence,
                     String docid, int sentenceIndex,
                     Optional<List<CoreLabel>> originalSubject, Optional<List<CoreLabel>> originalObject) {
      super(subject, relation, object, confidence);
      this.docid = docid;
      this.sentenceIndex = sentenceIndex;
      this.originalSubject = originalSubject;
      this.originalObject = originalObject;
    }

    /** @see edu.stanford.nlp.ie.util.RelationTriple.AsKBEntry#AsKBEntry(java.util.List, java.util.List, java.util.List, double, String, int, Optional, Optional) */
    public AsKBEntry(RelationTriple source, String docid, int sentenceIndex) {
      this(source.subject, source.relation, source.object, source.confidence, docid, sentenceIndex,
          Optional.empty(), Optional.empty());
    }

    /** The subject of this relation triple, as a String */
    @Override
    public String subjectGloss() {
      if (subject.get(0).lemma() != null) {
        return StringUtils.join(subject.stream().map(CoreLabel::lemma), " ");
      } else {
        return super.relationGloss();
      }
    }

    /** The object of this relation triple, as a String */
    @Override
    public String objectGloss() {
      if (object.get(0).lemma() != null) {
        return StringUtils.join(object.stream().map(CoreLabel::lemma), " ");
      } else {
        return super.objectGloss();
      }
    }

    /** The relation of this relation triple, as a String */
    @Override
    public String relationGloss() {
      if (relation.get(0).lemma() != null) {
        return StringUtils.join(relation.stream().map(CoreLabel::lemma), " ");
      } else {
        return super.relationGloss();
      }
    }

    public Optional<Pair<Integer, Integer>> originalSubjectTokenSpan() {
      return originalSubject.map(x -> getSpan(x, CoreLabel::index, CoreLabel::index));
    }

    public Optional<Pair<Integer, Integer>> originalObjectTokenSpan() {
      return originalObject.map(x -> getSpan(x, CoreLabel::index, CoreLabel::index));
    }

    public Pair<Integer, Integer> extractionTokenSpan() {
      return getSpan(allTokens(), CoreLabel::index, CoreLabel::index);
    }

    public Pair<Integer, Integer> subjectCharacterSpan() {
      return getSpan(subject, CoreLabel::beginPosition, CoreLabel::endPosition);
    }

    public Pair<Integer, Integer> objectCharacterSpan() {
      return getSpan(subject, CoreLabel::beginPosition, CoreLabel::endPosition);
    }

    public Optional<Pair<Integer, Integer>> originalSubjectCharacterSpan() {
      return originalSubject.map(x -> getSpan(x, CoreLabel::beginPosition, CoreLabel::endPosition));
    }

    public Optional<Pair<Integer, Integer>> originalObjectCharacterSpan() {
      return originalObject.map(x -> getSpan(x, CoreLabel::beginPosition, CoreLabel::endPosition));
    }

    public Pair<Integer, Integer> extractionCharacterSpan() {
      return getSpan(allTokens(), CoreLabel::beginPosition, CoreLabel::endPosition);
    }

    private static String gloss(Pair<Integer,Integer> pair) {
      return "" + pair.first + "\t" + pair.second;
    }

    private static String gloss(Optional<Pair<Integer,Integer>> pair) {
      if (pair.isPresent()) {
        return "" + pair.get().first + "\t" + pair.get().second;
      } else {
        return "0\t0";
      }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
      return new StringBuilder()
          .append(confidenceGloss()).append("\t")
          .append(subjectGloss().replace('\t', ' ')).append("\t")
          .append(relationGloss().replace('\t', ' ')).append("\t")
          .append(objectGloss().replace('\t', ' ')).append("\t")
          .append(docid.replace('\t', ' ')).append("\t")
          .append(sentenceIndex).append("\t")
          .append(gloss(subjectTokenSpan())).append("\t")
          .append(gloss(objectTokenSpan())).append("\t")
          .append(gloss(extractionTokenSpan())).append("\t")
          .append(gloss(subjectCharacterSpan())).append("\t")
          .append(gloss(objectCharacterSpan())).append("\t")
          .append(gloss(extractionCharacterSpan())).append("\t")
          .append(gloss(originalSubjectTokenSpan())).append("\t")
          .append(gloss(originalObjectTokenSpan())).append("\t")
          .append(gloss(originalSubjectTokenSpan())).append("\t")
          .append(gloss(originalObjectTokenSpan())).append("\t")
          .toString();
    }

  }

  public static Optional<RelationTriple> optimizeForKB(RelationTriple input, Optional<CoreMap> sentence, Map<CoreLabel, List<CoreLabel>> canonicalMentions) {
    // Get some metadata
    String docid = sentence.isPresent() ? sentence.get().get(CoreAnnotations.DocIDAnnotation.class) : null;
    if (docid == null) { docid = "no_doc_id"; }
    Integer sentenceIndex = sentence.isPresent() ? sentence.get().get(CoreAnnotations.SentenceIndexAnnotation.class) : null;
    if (sentenceIndex == null) { sentenceIndex = -1; }

    // Pass 1: resolve Coref
    List<CoreLabel> subject = null;
    for (int i = input.subject.size() - 1; i >= 0; --i) {
      if ( (subject = canonicalMentions.get(input.subject.get(i))) != null) {
        break;
      }
    }
    if (subject == null) {
      subject = input.subject;
    }
    List<CoreLabel> object = null;
    for (int i = input.object.size() - 1; i >= 0; --i) {
      if ( (object = canonicalMentions.get(input.object.get(i))) != null) {
        break;
      }
    }
    if (object == null) {
      object = input.object;
    }

    // Pass 2: Filter prepositions
    for (CoreLabel subjToken : subject) {
      if ("PRP".equals(subjToken.tag())) {
        return Optional.empty();
      }
    }
    for (CoreLabel objToken : object) {
      if ("PRP".equals(objToken.tag())) {
        return Optional.empty();
      }
    }

    // Pass 3: Filter invalid subjects
    boolean hasNER = false;
    for (CoreLabel subjToken : subject) {
      if (!"O".equals(subjToken.ner())) {
        hasNER = true;
      }
    }
    if (!hasNER) {
      return Optional.empty();
    }

    // Return
    return Optional.of(new AsKBEntry(subject, input.relation, object, input.confidence, docid, sentenceIndex,
        subject == input.subject ? Optional.empty() : Optional.of(input.subject),
        object == input.object ? Optional.empty() : Optional.of(input.object) ));
  }
}
