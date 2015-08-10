package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.ner.en.StanfordENOldVersion;
import org.aksw.fox.tools.re.AbstractRE;
import org.aksw.fox.utils.Converter;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.RelationMentionsAnnotation;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.RelationExtractorAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

// https://github.com/stanfordnlp/CoreNLP/blob/672d43a9677272fdef79ef15f3caa9ed7bc26164/src/edu/stanford/nlp/ie/machinereading/domains/roth/RothCONLL04Reader.java
/**
 * 
 * @author rspeck
 *
 */
public class REStanford extends AbstractRE {

    public enum StanfordRelations {

        Live_In("Live_In"),
        Located_In("Located_In"),
        OrgBased_In("OrgBased_In"),
        Work_For("Work_For"),
        NoRelation("_NR");

        private String label;

        private StanfordRelations(String text) {
            this.label = text;
        }

        @Override
        public String toString() {
            return this.label;
        }

        public static StanfordRelations fromString(String label) {
            if (label != null)
                for (StanfordRelations b : StanfordRelations.values())
                    if (label.equalsIgnoreCase(b.label))
                        return b;
            return null;
        }
    }

    public static final Logger        LOG                        = LogManager.getLogger(REStanford.class);

    public static final String        CFG_KEY_LIVEIN             = REStanford.class.getName().concat(".liveIn");
    public static final String        CFG_KEY_LOCATEDIN          = REStanford.class.getName().concat(".locatedIn");
    public static final String        CFG_KEY_ORGBASEDIN         = REStanford.class.getName().concat(".orgbasedIn");
    public static final String        CFG_KEY_WORKFOR            = REStanford.class.getName().concat(".workFor");

    Map<StanfordRelations, List<URI>> relationURIs               = new HashMap<>();

    Properties                        props                      = new Properties();
    StanfordCoreNLP                   stanfordNLP                = new StanfordCoreNLP();

    RelationExtractorAnnotator        relationExtractorAnnotator = null;

    /**
     * 
     */
    public REStanford() {
        init();
    }

    /**
     * 
     */
    protected void init() {
        props.setProperty("annotators", "tokenize,ssplit,lemma,pos,parse,ner");
        relationExtractorAnnotator = new RelationExtractorAnnotator(props);

        initURIs(StanfordRelations.Live_In, CFG_KEY_LIVEIN);
        initURIs(StanfordRelations.Located_In, CFG_KEY_LOCATEDIN);
        initURIs(StanfordRelations.OrgBased_In, CFG_KEY_ORGBASEDIN);
        initURIs(StanfordRelations.Work_For, CFG_KEY_WORKFOR);
    }

    /**
     * Maps relations to uris from properties file.
     * 
     * @param cfgkey
     * @param relation
     */
    private void initURIs(StanfordRelations relation, String cfgkey) {
        try {
            URI[] urisc = Converter.convertArray(
                    FoxCfg.get(cfgkey).replaceAll(" ", "").split(","),
                    URI::create,
                    URI[]::new
                    );
            relationURIs.put(
                    relation,
                    Arrays.asList(urisc)
                    );
        } catch (Exception e) {
            LOG.error("Check the config file. Something went wrong.");
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 
     * @return
     */
    public String getToolName() {
        return REStanford.class.getSimpleName();
    }

    /*
    Stanford relations (http://www.cnts.ua.ac.be/conll2004/pdf/00108rot.pdf)
    
    located in      loc loc (New York, US)
    work for        per org (Bill Gates, Microsoft)
    orgBased in     org loc (HP, Palo Alto)
    live in         per loc (Bush, US)
    */
    public boolean checkrules(RelationMention relationMention) {
        boolean valid = false;
        if (relationMention.getType() != null && relationMention.getType() != StanfordRelations.NoRelation.toString()) {
            List<EntityMention> entities = relationMention.getEntityMentionArgs();
            if (entities.size() != 2) {
                LOG.warn("EntityMention for relation is not 2!");
                LOG.warn(entities);
            } else {
                EntityMention emOne = entities.get(0);
                EntityMention emTwo = entities.get(1);

                StanfordRelations stanfordRelation = StanfordRelations.fromString(relationMention.getType());

                if (LOG.isTraceEnabled())
                    LOG.trace(stanfordRelation + "(" + emOne.getType() + " " + emTwo.getType() + ")" + " (" + emOne.getValue() + " " + emTwo.getValue() + ")");

                switch (stanfordRelation) {
                case Live_In:
                    if (EntityClassMap.P.equals(StanfordENOldVersion.stanford(emOne.getType())) &&
                            EntityClassMap.L.equals(StanfordENOldVersion.stanford(emTwo.getType())))
                        valid = true;
                    break;

                case Work_For:
                    if (EntityClassMap.P.equals(StanfordENOldVersion.stanford(emOne.getType())) &&
                            EntityClassMap.O.equals(StanfordENOldVersion.stanford(emTwo.getType())))
                        valid = true;
                    break;
                case OrgBased_In:
                    if (EntityClassMap.O.equals(StanfordENOldVersion.stanford(emOne.getType())) &&
                            EntityClassMap.L.equals(StanfordENOldVersion.stanford(emTwo.getType())))
                        valid = true;
                    break;
                case Located_In:
                    if (EntityClassMap.L.equals(StanfordENOldVersion.stanford(emOne.getType())) &&
                            EntityClassMap.L.equals(StanfordENOldVersion.stanford(emTwo.getType())))
                        valid = true;
                    break;
                default: {
                }
                }
            }
        }
        return valid;

    }

    public Set<Relation> extract(String text) {
        Set<Relation> set = new HashSet<>();
        // ----------------------------------------------------------------------------
        // tokenize and clean text
        // ----------------------------------------------------------------------------
        /*
        String options = "americanize=false,asciiQuotes=true,ptb3Escaping=false";
        PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(
                new StringReader(text),
                new CoreLabelTokenFactory(),
                options);

        text = null;
        List<CoreLabel> corelabels = tokenizer.tokenize();
        StringBuilder sb = new StringBuilder();
        for (CoreLabel label : corelabels) {
            sb.append(label.originalText());
            sb.append(" ");
        }
        text = sb.toString();
        */
        // ----------------------------------------------------------------------------
        // find relations
        // ----------------------------------------------------------------------------
        try {
            LOG.info("Start...");

            Annotation doc = new Annotation(text);
            LOG.debug("Annotate the doc...");
            stanfordNLP.annotate(doc);
            LOG.debug("RelationExtractorAnnotator the doc...");
            relationExtractorAnnotator.annotate(doc);
            LOG.debug("For all relation ...");
            for (CoreMap sentenceAnnotation : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<RelationMention> relationMentions = (sentenceAnnotation.get(RelationMentionsAnnotation.class));
                LOG.debug("relationMentions.size():" + relationMentions.size());
                for (RelationMention relationMention : relationMentions) {

                    boolean c = checkrules(relationMention);
                    if (c) {
                        List<EntityMention> entities = relationMention.getEntityMentionArgs();

                        EntityMention emOne = entities.get(0);
                        EntityMention emTwo = entities.get(1);

                        Entity a = new Entity(emOne.getExtentString(), StanfordENOldVersion.stanford(emOne.getType()), Entity.DEFAULT_RELEVANCE, getToolName());
                        Entity b = new Entity(emTwo.getExtentString(), StanfordENOldVersion.stanford(emTwo.getType()), Entity.DEFAULT_RELEVANCE, getToolName());

                        /*
                        for (Class classs : emOne.getSentence().keySet()) {
                            LOG.info(emOne.getSentence().get(classs));
                        }*/

                        int index_a = emOne.getSyntacticHeadToken().endPosition() - emOne.getExtentString().length();
                        int index_b = emTwo.getSyntacticHeadToken().endPosition() - emTwo.getExtentString().length();
                        a.addIndicies(index_a);
                        b.addIndicies(index_b);

                        /*
                        int start = -1, end = -1;
                        if (emOne.getSyntacticHeadToken().endPosition() < emTwo.getSyntacticHeadToken().endPosition()) {
                            start = emOne.getSyntacticHeadToken().endPosition() + 1;
                            end = index_b - 1;
                        } else {
                            start = emTwo.getSyntacticHeadToken().endPosition() + 1;
                            end = index_a - 1;
                        }
                        */
                        // not working
                        // String relationLabel = text.substring(start,
                        // end).trim();

                        /*
                        StringBuffer labelBuffer = new StringBuffer();
                        int tokenCounter = 0;
                        for (CoreLabel label : corelabels) {
                            if (tokenCounter == emOne.getExtentTokenStart()) {
                                a.addIndicies(label.beginPosition() + 1);
                            } else if (tokenCounter >= emOne.getExtentTokenEnd() && tokenCounter < emTwo.getExtentTokenStart()) {
                                labelBuffer.append(label.originalText());
                                labelBuffer.append(" ");
                            } else if (tokenCounter == emTwo.getExtentTokenStart()) {
                                b.addIndicies(label.beginPosition() + 1);
                            }
                            tokenCounter++;
                        }
                        */
                        Relation relation = new Relation(
                                a,
                                "",
                                StanfordRelations.fromString(relationMention.getType()).name(),
                                b,
                                relationURIs.get(StanfordRelations.fromString(relationMention.getType())),
                                getToolName(),
                                Relation.DEFAULT_RELEVANCE
                                );

                        if (LOG.isDebugEnabled()) {
                            LOG.debug(relationMention);
                            LOG.debug(relation);
                        }
                        set.add(relation);
                    }
                }
            }
            LOG.info("Relations done.");
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        relations = set;
        LOG.info(relations);
        return set;
    }

    /**
     * Test.
     * 
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);

        for (Relation rr : new REStanford().extract(FoxConst.RE_EN_EXAMPLE_1))
            LOG.info(rr);
    }
}
