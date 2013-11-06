/*
 * Balie - BAseLine Information Extraction
 * Copyright (C) 2004-2007  David Nadeau
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * Created on Febr 27, 2007
 */
package ca.uottawa.balie;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * NER extended with NERF disambiguation rules. <br>
 * NERF (Named Entity Recognition Framework) is an semi-supervised NER system
 * made of generated Named Entity lists and disambiguation rules. <br>
 * It is based on baseline system by:
 * 
 * <pre>
 * Nadeau, D., Turney, P. D. and Matwin, S. (2006) Unsupervised Named-Entity Recognition: Generating Gazetteers and Resolving Ambiguity.
 * Proc. Canadian Conference on Artificial Intelligence.
 * </pre>
 * 
 * NERF can handle the 200 NE types as defined in the Sekine Hierarchy:
 * 
 * <pre>
 * Sekine, S., Sudo, K. and Nobata, C. (2002) Extended Named Entity Hierarchy. 
 * Proc. International Conference on Language Resources and Evaluation.
 * </pre>
 * 
 * @author David Nadeau (pythonner@gmail.com)
 */
public class NamedEntityRecognitionNerfEXT extends NamedEntityRecognition {

    public Map<Integer, Double> map = null;

    private DisambiguationRulesI m_Rules;
    private PriorCorrectionI m_Prior;
    private NamedEntityTypeEnumI[] m_Mapping;
    private boolean m_FullDebug;

    private Hashtable<String, ArrayList<Integer>> m_CapWord2Index; // all
                                                                   // indexes of
                                                                   // capitalized
                                                                   // form of a
                                                                   // word

    private Hashtable<String, UnlistedWordInfo> m_UnlistedWords; // lookup of
                                                                 // words that
                                                                 // are part of
                                                                 // entities but
                                                                 // are not
                                                                 // lexicon
                                                                 // entries

    private class UnlistedWordInfo {
        public UnlistedWordInfo(NamedEntityType pi_Type, int pi_Group) {
            m_Type = pi_Type;
            m_AliasGroup = pi_Group;
        }

        public NamedEntityType Type() {
            return m_Type;
        }

        public int Group() {
            return m_AliasGroup;
        }

        private NamedEntityType m_Type;
        private int m_AliasGroup;
    }

    public NamedEntityRecognitionNerfEXT(
            TokenList pi_TokenList,
            LexiconOnDiskI pi_Lexicon,
            DisambiguationRulesI pi_Rules,
            PriorCorrectionI pi_Prior,
            NamedEntityTypeEnumI[] pi_Mapping,
            boolean pi_FullDebug) {
        super(pi_Lexicon, pi_TokenList);
        m_Mapping = pi_Mapping;
        m_Rules = pi_Rules;
        m_Prior = pi_Prior;

        m_FullDebug = pi_FullDebug;

        m_CapWord2Index = new Hashtable<String, ArrayList<Integer>>();
        m_UnlistedWords = new Hashtable<String, UnlistedWordInfo>();
    }

    protected void ApplyDisambiguation() {
        map = new HashMap<>();
        NamedEntityTypeEnumI[] intermediateTagSet = m_TokenList.NETagSet();

        if (m_TokenList.Size() > 1) {

            // 1. check for entities ambiguous with common nouns
            CheckEntityNounAmbiguityNerf();

            // 2. temporary map final types
            m_TokenList.MapNewNETypes(m_Mapping);

            // 3. check adjacent entities sharing a super-type (e.g., a first
            // name and a last name)
            PreCheckEntityBoundaryNerf();

            // 4. revert to intermediate types mapping
            m_TokenList.MapNewNETypes(intermediateTagSet);

            // 5. resolve alias network and fix some entity types
            CheckEntityEntityAmbiguityNerf();

            // 6. apply classifier if more than one type remains
            ApplyEntityEntityClassifiersNerf();

            // 7. apply defensive rules for very ambiguous types
            CheckVeryAmbiguousTypes();

            // 8. map final types
            m_TokenList.MapNewNETypes(m_Mapping);

            // 9. check boundaries with disambiguated types
            PostCheckEntityBoundaryNerf();

            // 10. resolve unknown capitalized words in alias network
            CheckUnknownCapitalizedExt();

        }
    }

    private void CheckEntityNounAmbiguityNerf() {
        if (m_FullDebug)
            System.out.println("*************************");
        if (m_FullDebug)
            System.out.println("* Entity-Noun Ambiguity *");
        // three steps here:
        // 1- identify uppercased, lowercased and uppercased in ambiguous
        // position
        // 2- identify ambiguity (seen upper and lower, upper but always in
        // ambiguous pos, always lowercased).
        // 3- filter ambiguous entities

        // var to spot ambiguous positions
        boolean bLastTokenIsQuote = false;

        // lookup for cap, uncap and ambiguous
        Hashtable<String, ArrayList<Integer>> hLowercased = new Hashtable<String, ArrayList<Integer>>(); // all
                                                                                                         // indexes
                                                                                                         // of
                                                                                                         // a
                                                                                                         // lowercased
                                                                                                         // form
                                                                                                         // of
                                                                                                         // a
                                                                                                         // word
        Hashtable<String, ArrayList<Integer>> hAmbiguous = new Hashtable<String, ArrayList<Integer>>(); // all
                                                                                                        // indexes
                                                                                                        // of
                                                                                                        // an
                                                                                                        // ambiguous
                                                                                                        // form
                                                                                                        // of
                                                                                                        // a
                                                                                                        // word

        boolean bInsideAllCapitalized = false;

        // 1) loop through tokenlist and mark capitalized, lowercased and
        // ambiguous
        for (int i = 0; i != m_TokenList.Size(); ++i) {
            Token tok = m_TokenList.Get(i);

            if (tok.IsSentenceStart()) {
                bInsideAllCapitalized = false;
            }
            if (tok.IsAllCapSentence()) {
                bInsideAllCapitalized = true;
            }

            if (bLastTokenIsQuote || tok.IsSentenceStart() || bInsideAllCapitalized) {
                if (TokenFeature.Feature.StartsWithCapital.Mechanism().GetBooleanValue(tok.Features()) && TokenConsts.Is(tok.Type(), TokenConsts.TYPE_WORD)) {
                    NERServices.AddIndexExt(hAmbiguous, tok.Raw().toLowerCase(), i);
                }
            }

            if (TokenConsts.Is(tok.Type(), TokenConsts.TYPE_WORD)) {
                if (TokenFeature.Feature.StartsWithCapital.Mechanism().GetBooleanValue(tok.Features())) {
                    NERServices.AddIndexExt(m_CapWord2Index, tok.Raw().toLowerCase(), i);
                } else {
                    NERServices.AddIndexExt(hLowercased, tok.Raw().toLowerCase(), i);
                }
            }

            // set quote flag & sent num
            bLastTokenIsQuote = false;
            if (TokenConsts.Is(tok.Type(), TokenConsts.TYPE_PUNCTUATION) &&
                    (TokenConsts.Is(tok.PartOfSpeech(), TokenConsts.PUNCT_QUOTE) ||
                    TokenConsts.Is(tok.PartOfSpeech(), TokenConsts.PUNCT_APOSTROPHE))) {
                bLastTokenIsQuote = true;
            }
        }

        // 2) identify words to reject
        ArrayList<Integer> alRejectedBothCases = new ArrayList<Integer>();
        ArrayList<Integer> alRejectedAlwaysLower = new ArrayList<Integer>();
        ArrayList<Integer> alRejectedAlwaysAmbiguous = new ArrayList<Integer>();

        Enumeration<String> eCur = m_CapWord2Index.keys();
        while (eCur.hasMoreElements()) {
            String strCur = eCur.nextElement();

            // invalidate word that appears both lowercased and uppercased
            if (hLowercased.containsKey(strCur)) {
                alRejectedBothCases.addAll((ArrayList<Integer>) m_CapWord2Index.get(strCur));
                alRejectedBothCases.addAll((ArrayList<Integer>) hLowercased.get(strCur));
            }

            // invalidate word that appears only in ambiguous pos
            else if (hAmbiguous.containsKey(strCur)) {
                ArrayList<Integer> alCap = (ArrayList<Integer>) m_CapWord2Index.get(strCur);
                ArrayList<Integer> alAmb = (ArrayList<Integer>) hAmbiguous.get(strCur);
                if (alCap.size() > 1 && alCap.equals(alAmb)) {
                    alRejectedAlwaysAmbiguous.addAll(alCap);
                }
            }
        }

        // invalidate words that only appears lowercased
        Enumeration<String> enumL = hLowercased.keys();
        while (enumL.hasMoreElements()) {
            String strCur = enumL.nextElement();
            // invalidate word that appears only lowercased
            if (!m_CapWord2Index.containsKey(strCur)) {
                alRejectedAlwaysLower.addAll((ArrayList<Integer>) hLowercased.get(strCur));
            }
        }

        // 3.1) filter words appearing in both case except if they are part of a
        // capitalized larger expression
        Iterator<Integer> iCur = alRejectedBothCases.iterator();
        while (iCur.hasNext()) {
            int nIdx = iCur.next().intValue();
            Token tok = m_TokenList.Get(nIdx);
            NamedEntityType net = tok.EntityType();

            if (NERServices.NaturallyLowercased(net)) {
                ForceEntityTypeExt(nIdx, NamedEntityType.Intersection(net, NamedEntityTypeEnum.NaturallyOccursLowercased(), new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.NATURAL_LOWERCSE, new Object[] {})));
            } else if (NERServices.OneWordEntity(net)) {
                ForceEntityTypeExt(nIdx, new NamedEntityType(NamedEntityTypeEnum.NOTHING, m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.REJECTED_LOWERCASED_AND_UPPERCASED, new Object[] {})));
                if (m_FullDebug)
                    System.out.println("Rejected low&up: " + tok.Raw());
            } else if (NERServices.UncapitalizedMultiWordEntity(tok)) {
                ForceEntityTypeExt(nIdx, new NamedEntityType(NamedEntityTypeEnum.NOTHING, m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.REJECTED_LOWERCASED_AND_UPPERCASED, new Object[] {})));
                if (m_FullDebug)
                    System.out.print("Rejected low&up:");
            }
        }

        // 3.2) filter expressions that are always lowercased
        iCur = alRejectedAlwaysLower.iterator();
        while (iCur.hasNext()) {
            int nIdx = ((Integer) iCur.next()).intValue();
            Token tok = m_TokenList.Get(nIdx);
            NamedEntityType net = tok.EntityType();

            if (NERServices.NaturallyLowercased(net)) {
                ForceEntityTypeExt(nIdx, NamedEntityType.Intersection(net, NamedEntityTypeEnum.NaturallyOccursLowercased(), new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.NATURAL_LOWERCSE, new Object[] {})));
            } else if (NERServices.EntityStartsHere(net)) {
                ForceEntityTypeExt(nIdx, new NamedEntityType(NamedEntityTypeEnum.NOTHING, m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.REJECTED_LOWERCASED_OR_AMBIGUOUS, new Object[] {})));
                if (m_FullDebug)
                    System.out.println("Rejected low: " + tok.Raw());
            }
        }

        // 3.3) filter words that are always ambiguous except if they are part
        // of a capitalized larger expression
        iCur = alRejectedAlwaysAmbiguous.iterator();
        while (iCur.hasNext()) {
            int nIdx = ((Integer) iCur.next()).intValue();
            Token tok = m_TokenList.Get(nIdx);
            NamedEntityType net = tok.EntityType();

            if (NERServices.NaturallyLowercased(net)) {
                ForceEntityTypeExt(nIdx, NamedEntityType.Intersection(net, NamedEntityTypeEnum.NaturallyOccursLowercased(), new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.NATURAL_LOWERCSE, new Object[] {})));
            } else if (NERServices.OneWordEntity(net)) {
                ForceEntityTypeExt(nIdx, new NamedEntityType(NamedEntityTypeEnum.NOTHING, m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.REJECTED_LOWERCASED_OR_AMBIGUOUS, new Object[] {})));
                if (m_FullDebug)
                    System.out.println("Rejected amb: " + tok.Raw());
            } else if (NERServices.UncapitalizedMultiWordEntity(tok)) {
                ForceEntityTypeExt(nIdx, new NamedEntityType(NamedEntityTypeEnum.NOTHING, m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.REJECTED_LOWERCASED_OR_AMBIGUOUS, new Object[] {})));
                if (m_FullDebug)
                    System.out.print("Rejected amb:");
            }
        }
        if (m_FullDebug)
            System.out.println("*************************");

    }

    private void PreCheckEntityBoundaryNerf() {
        if (m_FullDebug)
            System.out.println("************************");
        if (m_FullDebug)
            System.out.println("* Pre Check Boundaries *");

        // Loop through tokenlist and try to fix some entity boundaries
        for (int i = 0; i != m_TokenList.Size() - 1; ++i) {
            Token curTok = m_TokenList.Get(i);
            Token nextTok = m_TokenList.Get(i + 1);

            if (curTok.SentenceNumber() == nextTok.SentenceNumber() && curTok.EntityType().IsEnd()) {

                // if another entity follows, merge consecutive entities of same
                // "label"
                // it is important to check the "label", not the type.
                // for instance, in MUC, the PERSON label allow to merge
                // first_name, last_name, etc.
                if (nextTok.EntityType().IsStart()) {
                    if (m_FullDebug)
                        System.out.print("Adjacent entities: " + curTok.Raw() + " | " + nextTok.Raw());
                    if (curTok.EntityType().GetLabel(m_TokenList.NETagSet()) != null &&
                            nextTok.EntityType().GetLabel(m_TokenList.NETagSet()) != null) {

                        Hashtable<String, NamedEntityTypeEnumI> hSharedLabs = NERServices.SharedLabel(curTok.EntityType(), nextTok.EntityType(), m_TokenList.NETagSet());

                        if (hSharedLabs.size() > 0) {
                            NamedEntityType forceType = NERServices.Labels2Types(hSharedLabs, m_TagSetSize);

                            if (forceType.IsStart()) {
                                forceType.RemoveType(NamedEntityTypeEnum.START, null);
                            }
                            if (forceType.IsEnd()) {
                                forceType.RemoveType(NamedEntityTypeEnum.END, null);
                            }

                            if (forceType.Intersect(NamedEntityTypeEnum.Mergable())) {

                                // bring nextTok explanations to curTok
                                curTok.EntityType().AddTrailingInfo(nextTok.EntityType().GetInfo());
                                MergeEntitiesExt(i, curTok, nextTok, forceType);
                                if (m_FullDebug)
                                    System.out.println(" --> Merged!");
                            } else {
                                if (m_FullDebug)
                                    System.out.println(" --> not mergeable");
                            }
                        } else {
                            if (m_FullDebug)
                                System.out.println(" --> not merged");
                        }
                    }
                }
                // a period or a ampersand follows.
                else if (i != m_TokenList.Size() - 2 &&
                        TokenConsts.Is(nextTok.Type(), TokenConsts.TYPE_PUNCTUATION) &&
                        (TokenConsts.Is(nextTok.PartOfSpeech(), TokenConsts.PUNCT_PERIOD) ||
                        TokenConsts.Is(nextTok.PartOfSpeech(), TokenConsts.PUNCT_AMPERSAND))) {
                    Token secondNextTok = m_TokenList.Get(i + 2);
                    if (secondNextTok.EntityType().IsStart() && curTok.SentenceNumber() == secondNextTok.SentenceNumber()) {
                        if (m_FullDebug)
                            System.out.print("Adjacent entities: " + curTok.Raw() + " | " + nextTok.Raw() + " | " + secondNextTok.Raw());

                        Hashtable<String, NamedEntityTypeEnumI> hSharedLabs = NERServices.SharedLabel(curTok.EntityType(), secondNextTok.EntityType(), m_TokenList.NETagSet());

                        if (hSharedLabs.size() > 0) {
                            NamedEntityType forceType = NERServices.Labels2Types(hSharedLabs, m_TagSetSize);

                            if (forceType.IsStart()) {
                                forceType.RemoveType(NamedEntityTypeEnum.START, null);
                            }
                            if (forceType.IsEnd()) {
                                forceType.RemoveType(NamedEntityTypeEnum.END, null);
                            }

                            if (forceType.Intersect(NamedEntityTypeEnum.Mergable())) {

                                // bring nextTok explanations to curTok
                                curTok.EntityType().AddTrailingInfo(secondNextTok.EntityType().GetInfo());
                                MergeEntitiesExt(i, curTok, secondNextTok, forceType);
                                if (m_FullDebug)
                                    System.out.println(" --> Merged!");
                            } else {
                                if (m_FullDebug)
                                    System.out.println(" --> not mergeable");
                            }
                        } else {
                            if (m_FullDebug)
                                System.out.println(" --> not merged");
                        }
                    }
                }
            }
        }
        if (m_FullDebug)
            System.out.println("************************");
    }

    private void CheckEntityEntityAmbiguityNerf() {
        if (m_FullDebug)
            System.out.println("********************");
        if (m_FullDebug)
            System.out.println("* Alias Resolution *");

        // Start by resolving aliases
        NamedEntityAliasResolution near = new NamedEntityAliasResolution(m_TokenList, m_TagSetSize, m_DesiredEntities);
        near.ResolveAliasesNerf();
        near.IdentifyDominantAliases(m_FullDebug);

        // Loop through tokenlist and check entity-entity ambiguity
        for (int i = 0; i != m_TokenList.Size() - 1; ++i) {
            Token curTok = m_TokenList.Get(i);

            if (NamedEntityAliasResolution.ValidAliasToken(curTok)) {
                NamedEntityType curType = NamedEntityType.Intersection(curTok.EntityType(), m_DesiredEntities, null);

                // check all aliases for a cue
                Integer nGroupId = curTok.NamedEntityAlias();
                NamedEntityAlias neAlias = near.GetAlias(nGroupId);

                if (!neAlias.DominantType().HasNoTag() && !neAlias.DominantType().equals(curType)) {
                    NamedEntityType net = neAlias.DominantType().clone(new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.ALIAS_DOMINANT_TYPE, new Object[] { neAlias.DominantType().GetLabel(m_TokenList.NETagSet()) }));
                    if (curTok.EntityType().HasNoTag()) {
                        PromoteUnknownWordToEntity(curTok, net);
                        if (m_FullDebug)
                            System.out.println("Force type: " + curTok.Raw() + " --> " + neAlias.DominantType().GetLabel(m_TokenList.NETagSet()));
                    } else {
                        ForceEntityTypeExt(i, net);
                        if (m_FullDebug)
                            System.out.println("Force type: " + curTok.Raw() + " --> " + neAlias.DominantType().GetLabel(m_TokenList.NETagSet()));
                    }
                }
            }
        }
        if (m_FullDebug)
            System.out.println("********************");
    }

    private void ApplyEntityEntityClassifiersNerf() {
        if (m_FullDebug)
            System.out.println("********************************");
        if (m_FullDebug)
            System.out.println("* Entity-Entity Classification *");

        // Loop through tokenlist and check entity-entity ambiguity
        for (int i = 0; i != m_TokenList.Size() - 1; ++i) {
            Token curTok = m_TokenList.Get(i);

            if (curTok.EntityType().IsStart()) {
                NamedEntityType curTypes = NamedEntityType.Intersection(curTok.EntityType(), m_DesiredEntities, null);

                // ambiguity is when a token has more than one type
                if (curTypes.TypeCount() > 1) {

                    if (m_FullDebug)
                        System.out.print(curTok.Raw() + " ");

                    String[] strTypes = new String[curTypes.TypeCount()];
                    int idx = 0;
                    Hashtable<String, NamedEntityTypeEnumI> hLabel2NEType = new Hashtable<String, NamedEntityTypeEnumI>();
                    if (m_FullDebug)
                        System.out.print("(");

                    NamedEntityTypeEnumI[] NETagSet = m_TokenList.NETagSet();

                    for (int j = 0; j != NETagSet.length; ++j) {
                        if (curTypes.IsA(NETagSet[j])) {
                            if (m_FullDebug)
                                System.out.print(NETagSet[j].Label() + "|");
                            strTypes[idx++] = NETagSet[j].Label();
                            hLabel2NEType.put(NETagSet[j].Label(), NETagSet[j]);
                        }
                    }
                    if (m_FullDebug)
                        System.out.print(") ");

                    // get context tokens before i and after i + entity len
                    // also get entity types (if any) of immediate preceding and
                    // immediate following thing
                    NamedEntityType netBefore = null;
                    NamedEntityType netAfter = null;

                    int nStart = i;
                    int nEnd = FindEndIndexNerf(nStart);
                    String[] strCtx = new String[m_Rules.ContextSize() * 2];
                    // left
                    int nSlot = 0;
                    for (int k = nStart - m_Rules.ContextSize(); k != nStart; ++k) {
                        if (k >= 0) {
                            strCtx[nSlot++] = m_TokenList.Get(k).Canon();
                        } else {
                            strCtx[nSlot++] = null;
                        }
                        if (k == nStart - 1 && k >= 0) {
                            netBefore = m_TokenList.Get(k).EntityType();
                        }
                    }
                    // right
                    for (int k = nEnd + 1; k != nEnd + 1 + m_Rules.ContextSize(); ++k) {
                        if (k < m_TokenList.Size()) {
                            strCtx[nSlot++] = m_TokenList.Get(k).Canon();
                        } else {
                            strCtx[nSlot++] = null;
                        }
                        if (k == nEnd + 1 && k < m_TokenList.Size()) {
                            netAfter = m_TokenList.Get(k).EntityType();
                        }
                    }

                    if (m_FullDebug)
                        System.out.print("[");
                    if (m_FullDebug) {
                        for (int j = 0; j != strCtx.length; ++j) {
                            if (strCtx[j] == null)
                                System.out.print("NULL ");
                            else if (!strCtx[j].equals("\n") && !strCtx[j].equals("\r"))
                                System.out.print(strCtx[j] + " ");
                        }
                    }
                    if (m_FullDebug)
                        System.out.print("] ");

                    // check if the entity is attached to a intersecting type
                    if (netBefore != null && curTypes.Intersect(netBefore)) {
                        ForceEntityTypeExt(i, NamedEntityType.Intersection(curTypes, netBefore, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.ADJACENT_SHARE_TYPE, new Object[] {})));
                        if (m_FullDebug)
                            System.out.println("--> merged with preceeding of same type");
                    } else if (netAfter != null && curTypes.Intersect(netAfter)) {
                        ForceEntityTypeExt(i, NamedEntityType.Intersection(curTypes, netAfter, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.ADJACENT_SHARE_TYPE, new Object[] {})));
                        if (m_FullDebug)
                            System.out.println("--> merged with following of same type");
                    } else {
                        // classify!

                        // TODO: may remove that now that prior are known!!!!!
                        if (hLabel2NEType.containsKey("month")) {
                            Hashtable<String, Double> hPriorMap = new Hashtable<String, Double>(); // bidon
                            ForceEntityTypeExt(i, new NamedEntityType(hLabel2NEType.get("month"), NETagSet.length, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.CLASSIFIER, new Object[] { curTok.Raw(), hLabel2NEType.get("month").Label(), 0.9, hPriorMap })));
                            if (m_FullDebug)
                                System.out.println("--> classified as: month (special rule)");
                            map.put(i, 0.9); // TODO: remove magic!
                        }

                        else if (curTypes.TypeCount() == 2) {

                            // get prior for this entity
                            double[] fPrior = m_Prior.GetPrior(curTok.LexiconMatch(), strTypes[0], strTypes[1]);

                            // keep a prior map for explanations
                            Hashtable<String, Double> hPriorMap = new Hashtable<String, Double>();
                            hPriorMap.put(strTypes[0], new Double(fPrior[0]));
                            hPriorMap.put(strTypes[1], new Double(fPrior[1]));

                            DisambiguationRulesKey key = new DisambiguationRulesKey(strTypes[0], strTypes[1]);
                            DisambiguationRulesOutcome outcome = m_Rules.Classify(key, strCtx, hPriorMap);
                            if (m_FullDebug)
                                System.out.println("--> classified as: " + outcome.Class() + " (" + outcome.Likelihood() + ")");
                            ForceEntityTypeExt(i, new NamedEntityType(hLabel2NEType.get(outcome.Class()), NETagSet.length, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.CLASSIFIER, new Object[] { curTok.Raw(), hLabel2NEType.get(outcome.Class()).Label(), outcome.Likelihood(),
                                    hPriorMap })));
                            map.put(i, outcome.Likelihood());

                        } else {
                            // use Round Robin Classification if more than 2
                            // Johannes F�rnkranz 2002 Round Robin
                            // Classification Journal of Machine Learning
                            // Research 2 (2002), pp. 721-747
                            CombinationGenerator cg = new CombinationGenerator(curTypes.TypeCount(), 2);
                            Hashtable<String, Double> hVote = new Hashtable<String, Double>();
                            for (int j = 0; j != strTypes.length; ++j) {
                                hVote.put(strTypes[j], new Double(0));
                            }

                            // keep a prior map for explanations
                            Hashtable<String, Double> hPriorMap = new Hashtable<String, Double>();

                            while (cg.hasMore()) {
                                int[] classPair = cg.getNext();
                                // get prior for this entity
                                double[] fPrior = m_Prior.GetPrior(curTok.LexiconMatch(), strTypes[classPair[0]], strTypes[classPair[1]]);
                                hPriorMap.put(strTypes[classPair[0]], new Double(fPrior[0]));
                                hPriorMap.put(strTypes[classPair[1]], new Double(fPrior[1]));

                                DisambiguationRulesKey key = new DisambiguationRulesKey(strTypes[classPair[0]], strTypes[classPair[1]]);
                                DisambiguationRulesOutcome outcome = m_Rules.Classify(key, strCtx, hPriorMap);
                                Double fCurVote = hVote.get(outcome.Class());
                                hVote.put(outcome.Class(), new Double(fCurVote.doubleValue() + outcome.Likelihood()));
                            }

                            int nWinner = 0;
                            double fMaxVote = 0.0f;
                            double fSum = 0.0f;
                            for (int j = 0; j != strTypes.length; ++j) {
                                double fCurVote = hVote.get(strTypes[j]).doubleValue();
                                fSum += fCurVote;
                                if (fCurVote > fMaxVote) {
                                    fMaxVote = fCurVote;
                                    nWinner = j;
                                }
                            }

                            if (m_FullDebug)
                                System.out.println("--> classified as: " + strTypes[nWinner] + " (Probability sum = " + fMaxVote + ")");
                            ForceEntityTypeExt(i, new NamedEntityType(hLabel2NEType.get(strTypes[nWinner]), NETagSet.length, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.CLASSIFIER_VOTE, new Object[] { curTok.Raw(), hLabel2NEType.get(strTypes[nWinner]).Label(), fMaxVote, fSum,
                                    hPriorMap })));
                            map.put(i, fSum / strTypes.length);
                        }
                    }
                }
            }
        }
        if (m_FullDebug)
            System.out.println("********************************");
    }

    private void CheckVeryAmbiguousTypes() {
        if (m_FullDebug)
            System.out.println("******************************");
        if (m_FullDebug)
            System.out.println("* Check Very Ambiguous Types *");

        // 1) loop through tokenlist and disable very ambigous entities
        for (int i = 0; i != m_TokenList.Size(); ++i) {
            Token tok = m_TokenList.Get(i);

            if (tok.EntityType().IsStart() && tok.EntityType().Intersect(NamedEntityTypeEnum.HighlyAmbiguous())) {

                // some strong cues can enable these very ambiguous types
                // 1- more than 1 capital letter in the expression
                // 2- isolated word with more than 1 capital letter
                // 3- between quotes or parenthesis
                boolean bDisable = true;

                if (CountCapitalizedWords(i) > 1 ||
                        (NERServices.OneWordEntity(tok.EntityType()) &&
                        (TokenFeature.Feature.IsAllCapitalized.Mechanism().GetBooleanValue(tok.Features()) ||
                        TokenFeature.Feature.IsMixedCase.Mechanism().GetBooleanValue(tok.Features())))) {
                    if (m_FullDebug)
                        System.out.println("Lot of capitalized: " + tok.Raw());
                    bDisable = false;
                }

                int nPreceding = i - 1;
                int nFollowing = FindEndIndexNerf(i) + 1;
                if (nPreceding > 0 && nFollowing < m_TokenList.Size()) {
                    Token precTok = m_TokenList.Get(nPreceding);
                    Token nextTok = m_TokenList.Get(nFollowing);
                    if ((TokenFeature.Feature.HasOpenBracketPunct.Mechanism().GetBooleanValue(precTok.Features()) &&
                            TokenFeature.Feature.HasCloseBracketPunct.Mechanism().GetBooleanValue(nextTok.Features())) ||
                            (TokenFeature.Feature.HasOpenParenthesisPunct.Mechanism().GetBooleanValue(precTok.Features()) &&
                            TokenFeature.Feature.HasCloseParenthesisPunct.Mechanism().GetBooleanValue(nextTok.Features()))) {
                        if (m_FullDebug)
                            System.out.println("Between brackets: " + tok.Raw());
                        bDisable = false;
                    }
                }

                if (bDisable) {
                    ForceEntityTypeExt(i, new NamedEntityType(NamedEntityTypeEnum.NOTHING, m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.REJECTED_VERY_AMBIGUOUS, new Object[] {})));
                    if (m_FullDebug)
                        System.out.println("Rejected very ambigous: " + tok.Raw());
                }
            }
        }
        if (m_FullDebug)
            System.out.println("******************************");

    }

    private void PostCheckEntityBoundaryNerf() {

        if (m_FullDebug)
            System.out.println("*************************");
        if (m_FullDebug)
            System.out.println("* Post Check Boundaries *");

        boolean bInsideAllCapitalized = false;

        // Loop through tokenlist and try to fix some entity boundaries
        for (int i = 0; i != m_TokenList.Size() - 1; ++i) {
            Token curTok = m_TokenList.Get(i);
            Token nextTok = m_TokenList.Get(i + 1);

            if (curTok.IsSentenceStart()) {
                bInsideAllCapitalized = false;
            }
            if (curTok.IsAllCapSentence()) {
                bInsideAllCapitalized = true;
            }

            if (curTok.SentenceNumber() == nextTok.SentenceNumber() &&
                    curTok.EntityType().IsEnd()) {

                // if a capitalized word follows a mergable type
                if (curTok.EntityType().Intersect(NamedEntityTypeEnum.Mergable()) && !bInsideAllCapitalized && TokenConsts.Is(nextTok.Type(), TokenConsts.TYPE_WORD) && nextTok.EntityType().HasNoTag() && TokenFeature.Feature.StartsWithCapital.Mechanism().GetBooleanValue(nextTok.Features())) {
                    if (m_FullDebug)
                        System.out.println("Entity + capitalized: " + curTok.Raw() + " | " + nextTok.Raw());
                    NamedEntityType curType = curTok.EntityType().clone(new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.ENTITY_CAPITAL_MERGING, new Object[] { curTok.Raw(), nextTok.Raw() }));
                    curType.RemoveType(NamedEntityTypeEnum.START, null);
                    curType.RemoveType(NamedEntityTypeEnum.END, null);
                    ExtendEntityExt(i, curTok, nextTok, curType);
                }
                // a period follows any small word (e.g., middle word)
                else if (i != m_TokenList.Size() - 2 &&
                        TokenConsts.Is(nextTok.Type(), TokenConsts.TYPE_PUNCTUATION) &&
                        TokenConsts.Is(nextTok.PartOfSpeech(), TokenConsts.PUNCT_PERIOD) &&
                        curTok.Length() <= 4) {

                    // only handle period that is not breaking the sentence
                    Token secondNextTok = m_TokenList.Get(i + 2);
                    if (curTok.SentenceNumber() == secondNextTok.SentenceNumber()) {
                        if (m_FullDebug)
                            System.out.println("Entity + period: " + curTok.Raw() + " | " + nextTok.Raw());
                        NamedEntityType curType = curTok.EntityType().clone(new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.ENTITY_PUNCT_MERGING, new Object[] { curTok.Raw(), nextTok.Raw() }));
                        curType.RemoveType(NamedEntityTypeEnum.START, null);
                        curType.RemoveType(NamedEntityTypeEnum.END, null);
                        ExtendEntityExt(i, curTok, nextTok, curType);
                    }
                }
                // a hyphen or a ampersand follows an organization name.
                else if (i != m_TokenList.Size() - 2 &&
                        TokenConsts.Is(nextTok.Type(), TokenConsts.TYPE_PUNCTUATION) &&
                        (TokenConsts.Is(nextTok.PartOfSpeech(), TokenConsts.PUNCT_DASH) ||
                        TokenConsts.Is(nextTok.PartOfSpeech(), TokenConsts.PUNCT_AMPERSAND))) {

                    // only handle punct that is not breaking the sentence
                    // and that is preceded by a mergable type and is followed
                    // by a capital letter, but not in a allcap sentence
                    Token secondNextTok = m_TokenList.Get(i + 2);
                    if (curTok.EntityType().Intersect(NamedEntityTypeEnum.Mergable()) &&
                            !bInsideAllCapitalized &&
                            curTok.SentenceNumber() == secondNextTok.SentenceNumber()
                            && TokenFeature.Feature.StartsWithCapital.Mechanism().GetBooleanValue(secondNextTok.Features())
                            && secondNextTok.EntityType().HasNoTag()) {

                        if (m_FullDebug)
                            System.out.println("Entity + punct: " + curTok.Raw() + " | " + nextTok.Raw());
                        NamedEntityType curType = curTok.EntityType().clone(new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.ENTITY_PUNCT_MERGING, new Object[] { curTok.Raw(), nextTok.Raw() }));
                        curType.RemoveType(NamedEntityTypeEnum.START, null);
                        curType.RemoveType(NamedEntityTypeEnum.END, null);
                        ExtendEntityExt(i, curTok, nextTok, curType);
                    }
                }
            }
        }
        if (m_FullDebug)
            System.out.println("*************************");
    }

    private void CheckUnknownCapitalizedExt() {

        if (m_FullDebug)
            System.out.println("*****************************");
        if (m_FullDebug)
            System.out.println("* Check Unknown Capitalized *");

        // check unknown words
        Enumeration<String> eCur = m_UnlistedWords.keys();
        while (eCur.hasMoreElements()) {
            String strUnknown = (String) eCur.nextElement();
            UnlistedWordInfo uwi = m_UnlistedWords.get(strUnknown);
            // annotate every capitalized occurences of this word according to
            // this type
            ArrayList<Integer> alIndexes = (ArrayList<Integer>) m_CapWord2Index.get(strUnknown);
            if (alIndexes != null) {
                Iterator<Integer> iCur = alIndexes.iterator();
                while (iCur.hasNext()) {
                    int idx = iCur.next().intValue();
                    Token curTok = m_TokenList.Get(idx);
                    if (curTok.EntityType().HasNoTag()) {
                        PromoteUnknownWordToEntity(curTok, uwi.Type());
                        curTok.NamedEntityAlias(uwi.Group());
                        if (m_FullDebug)
                            System.out.println("Unknown capitalized tagged: " + curTok.Raw() + "(" + curTok.EntityType().GetLabel(m_TokenList.NETagSet()) + ")");
                    }
                }
            }
        }
        if (m_FullDebug)
            System.out.println("*****************************");
    }

    private void PromoteUnknownWordToEntity(Token pi_Token, NamedEntityType pi_Type) {
        NamedEntityType newNE = new NamedEntityType(m_TagSetSize, new NamedEntityExplanation(NamedEntityExplanation.ExplanationType.UNKNOWN_CAPITAL, new Object[] { pi_Token.Raw() }));
        newNE.AddType(NamedEntityTypeEnum.START, null);
        newNE.AddType(NamedEntityTypeEnum.END, null);
        newNE.MergeWith(pi_Type, null);
        pi_Token.EntityType(newNE);
    }

    protected void ExtendEntityExt(int pi_StartIndex, Token pi_FirstBoundary, Token pi_NewEnd, NamedEntityType pi_NewType) {
        pi_FirstBoundary.EntityType().RemoveType(NamedEntityTypeEnum.END, null);
        pi_NewEnd.EntityType(new NamedEntityType(NamedEntityTypeEnum.END, m_TagSetSize, null));
        ForceEntityTypeExt(pi_StartIndex, pi_NewType);
        if (pi_NewEnd.Raw().length() > 4) {
            m_UnlistedWords.put(pi_NewEnd.Raw().toLowerCase(), new UnlistedWordInfo(pi_NewType, pi_FirstBoundary.NamedEntityAlias()));
        }
    }

    private void MergeEntitiesExt(int pi_StartIndex, Token pi_FirstBoundary, Token pi_SecondBoundary, NamedEntityType pi_NewType) {
        pi_FirstBoundary.EntityType().RemoveType(NamedEntityTypeEnum.END, null);
        pi_SecondBoundary.EntityType().RemoveType(NamedEntityTypeEnum.START, null);
        ForceEntityTypeExt(pi_StartIndex, pi_NewType);
    }

    private void ForceEntityTypeExt(int pi_CurPosition, NamedEntityType pi_EntityType) {
        Token curTok = m_TokenList.Get(pi_CurPosition);

        // force previous tokens
        if (!curTok.EntityType().IsStart()) {
            int nPrev = pi_CurPosition - 1;
            while (nPrev > -1) {
                Token prevTok = m_TokenList.Get(nPrev);
                boolean bIsStart = false;
                if (prevTok.EntityType().IsEnd()) {
                    throw new Error("Inconsistant state");
                }
                if (prevTok.EntityType().IsStart()) {
                    bIsStart = true;
                }
                prevTok.EntityType(pi_EntityType);
                if (bIsStart) {
                    prevTok.EntityType().AddType(NamedEntityTypeEnum.START, null);
                    break;
                }
                --nPrev;
            }
        }

        // force current token
        {
            boolean bIsEnd = false;
            boolean bIsStart = false;
            if (curTok.EntityType().IsStart()) {
                bIsStart = true;
            }
            if (curTok.EntityType().IsEnd()) {
                bIsEnd = true;
            }

            curTok.EntityType(pi_EntityType);
            if (bIsEnd) {
                curTok.EntityType().AddType(NamedEntityTypeEnum.END, null);
            }
            if (bIsStart) {
                curTok.EntityType().AddType(NamedEntityTypeEnum.START, null);
            }
        }

        int nNext = pi_CurPosition + 1;
        // force next tokens
        if (!curTok.EntityType().IsEnd()) {
            while (nNext < m_TokenList.Size()) {
                Token nextTok = m_TokenList.Get(nNext);
                boolean bIsEnd = false;
                if (nextTok.EntityType().IsStart()) {
                    throw new Error("Inconsistant state");
                }
                if (nextTok.EntityType().IsEnd()) {
                    bIsEnd = true;
                }
                nextTok.EntityType(pi_EntityType);
                if (bIsEnd) {
                    nextTok.EntityType().AddType(NamedEntityTypeEnum.END, null);
                    break;
                }
                ++nNext;
            }
        }
    }

    private int FindEndIndexNerf(int pi_Start) {
        int nEnd = pi_Start;
        Token curTok = m_TokenList.Get(nEnd);
        while (!curTok.EntityType().IsEnd()) {
            ++nEnd;
            if (nEnd == m_TokenList.Size()) {
                throw new Error("End token not found!");
            }
            curTok = m_TokenList.Get(nEnd);
        }
        return nEnd;
    }

    private int CountCapitalizedWords(int pi_Start) {
        int nNumCap = 0;
        Token curTok = m_TokenList.Get(pi_Start);
        if (TokenFeature.Feature.StartsWithCapital.Mechanism().GetBooleanValue(curTok.Features()))
            nNumCap++;
        while (!curTok.EntityType().IsEnd()) {
            ++pi_Start;
            if (pi_Start == m_TokenList.Size()) {
                throw new Error("End token not found!");
            }
            curTok = m_TokenList.Get(pi_Start);
            if (TokenFeature.Feature.StartsWithCapital.Mechanism().GetBooleanValue(curTok.Features()))
                nNumCap++;
        }
        return nNumCap;
    }

    protected boolean IsEntityCounterExample(NamedEntityType pi_EntityType) {
        return false;
    }

}
