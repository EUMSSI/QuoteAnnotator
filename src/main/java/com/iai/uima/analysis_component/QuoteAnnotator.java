package com.iai.uima.analysis_component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dbpedia.spotlight.uima.types.DBpediaResource;

import com.iai.uima.jcas.tcas.IaiCorefAnnotation;
import com.iai.uima.jcas.tcas.QuoteAnnotation;
import com.iai.uima.jcas.tcas.SentenceAnnotation;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.QuotationsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.international.hebrew.HebrewTreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.TypesafeMap.Key;



public class QuoteAnnotator extends JCasAnnotator_ImplBase {

	StanfordCoreNLP pipeline;

	public static final String PARAM_LANGUAGE = "language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, defaultValue = "en")
	private String language;

	public static final String PARAM_OPINION_NOUNS = "opinionNouns";
	@ConfigurationParameter(name = PARAM_OPINION_NOUNS, defaultValue = "/maps/opinion_nouns")
	private String opinionNouns;

	public static final String PARAM_OPINION_VERBS = "opinionVerbs";
	@ConfigurationParameter(name = PARAM_OPINION_VERBS, defaultValue = "/maps/opinion_verbs")
	private String opinionVerbs;

	Collection<String> opinion_nouns;
	Collection<String> opinion_verbs;
	Collection<String> passive_opinion_verbs;

	
	
	class HelperDataStructures {
		private HashMap<String, String> offsetToNer = new HashMap<String, String>();
		private HashSet<String> Ner = new HashSet<String>();
//		HashMap<String, String> offsetToNerPerson = new HashMap<String, String>();
		private HashSet<String> NerPerson = new HashSet<String>();
//		HashMap<String, String> offsetToNerOrganization = new HashMap<String, String>();
		private HashSet<String> NerOrganization = new HashSet<String>();
		//SPnew subject of opinion verb stored in:
		private HashMap<String, String> offsetToSubjectHead = new HashMap<String, String>();
		HashSet<String> SubjectHead = new HashSet<String>(); 
		//SPnew pos of subject to distinguish pronouns from proper nouns:
//		private HashMap<String, String> offsetToPosVerbCand = new HashMap<String, String>();
		private HashSet<String> PronominalSubject = new HashSet<String>();
		private HashSet<String> objectEqui = new HashSet<String>();
		private HashSet<String> subjectEqui = new HashSet<String>();
		//SPnew Map offsets to lemma of opinion verb/noun
		private HashMap<String, String> offsetToLemma = new HashMap<String, String>();
		private HashMap<String, String> offsetToLemmaOfOpinionVerb = new HashMap<String, String>();
		private HashMap<String, String> offsetToLemmaOfPassiveOpinionVerb = new HashMap<String, String>();
		private HashMap<String, String> offsetToLemmaOfOpinionExpression = new HashMap<String, String>();
		private HashMap<String, String> dbpediaSurfaceFormToDBpediaLink = new HashMap<String, String>();
		private HashSet<String> OpinionExpression = new HashSet<String>();
		//SPnew OpinionExpressionToken is later replaced with lemma
		private HashMap<String, String> offsetToOpinionExpressionToken = new HashMap<String, String>();
//		private HashMap<String, String> offsetToFiniteVerb = new HashMap<String, String>();
		private QuoteAnnotation subsequentDirectQuote;


		private HashMap<String, String> subjectToPredicate = new HashMap<String, String>();
//		private HashMap<String, String> mainToComplementVerb = new HashMap<String, String>();
//		private HashMap<String, String> complementToMainVerb = new HashMap<String, String>();
		private HashMap<String, String> mainToInfinitiveVerb = new HashMap<String, String>();
		private HashMap<String, String> infinitiveToMainVerb = new HashMap<String, String>();
		private HashMap<String, String> nounToInfinitiveVerb = new HashMap<String, String>();
		private HashMap<String, String> infinitiveVerbToNoun = new HashMap<String, String>();
		private HashMap<String, String> subjectToApposition = new HashMap<String, String>();
//		HashMap<String, String> invertedSubjectToPredicate = new HashMap<String, String>();
		private HashMap<String, String> predicateToSubject = new HashMap<String, String>();
//		HashMap<String, String> predicateToInvertedSubject = new HashMap<String, String>();
		private HashMap<String, String> offsetToRepMen = new HashMap<String, String>();
		private HashSet<String> RepMen = new HashSet<String>();
		private HashSet<String> pronTag = new HashSet<String>(); 
		private HashSet<String> potentialIndirectQuoteTrigger = new HashSet<String>();
		private HashMap<String, String> subjectToNormalPredicate = new HashMap<String, String>();
		private HashMap<String, String> normalPredicateToSubject = new HashMap<String, String>();
		private HashMap<String, String> predicateToObject = new HashMap<String, String>();
		private HashMap<String, String> ObjectToPredicate = new HashMap<String, String>();
		private HashMap<String, String> predicateToSubjectChainHead = new HashMap<String, String>();
		private HashMap<String, String> subjectToPredicateChainHead = new HashMap<String, String>();
		private String [] potentialIndirectQuote;
	}
	
	class ProvisionalCorefAnnotation {
		private String quotee = null;
		private String representative_quotee = null;
		private String quote_relation = null;
		private int quoteeReliability = -5;
		private String quoteeUri = null;
		 
	}
	
	class Dependency {
		private String governor = null;
		private String offsetGovernor = null;
		private String dependant = null;
		private String offsetDependant = null;
		private String posGovernor = null;
		private String posDependant = null;
	}
	
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {

		super.initialize(aContext);

		Properties props = new Properties();
//		props.setProperty("annotators", "tokenize, ssplit, quote");
//		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref"); //mention gibt's nicht
		props.setProperty("annotators", "tokenize,ssplit,quote,pos,lemma,ner,parse,dcoref"); //quote and dcoref
//		props.setProperty("annotators", "tokenize,ssplit,quote,pos,lemma,ner,parse,dcoref,depparse, sentiment");
		pipeline = new StanfordCoreNLP(props);

//		SimplePipeline.runPipeline(reader, segmenter, lemma, pos1, chunk, ner, quote, xmiWriter);
//		HashSet<String> passSubj = new HashSet<String>();
		
		BufferedReader br_nouns = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream(opinionNouns)));
		opinion_nouns = new HashSet<String>();

		String line;
		try {
			while ((line = br_nouns.readLine()) != null) {
				opinion_nouns.add(line);
			}
			br_nouns.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader br_verbs = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream(opinionVerbs)));
		opinion_verbs = new HashSet<String>();
		passive_opinion_verbs = new HashSet<String>();
		try {
			while ((line = br_verbs.readLine()) != null) {
				String[] verb = line.split(" ");
				if (verb != null && verb[verb.length-1].equals("PASSSUBJ")) {
					
					String headLine = Arrays.toString(verb);               
		               //replace starting "[" and ending "]" and ","
					String last=verb[verb.length-1];
		             headLine = line.substring(0, line.length() - last.length()-1);
		             passive_opinion_verbs.add(headLine);
//		             passSubj.add(headLine);
//		             System.out.println("verb with PASSSUBJ: " + headLine+"END");
		             
					}
				else {
				
				opinion_verbs.add(line);
//				System.out.println("verb: " + line);
//				opinion_verbs.add(verb[0]);
				}
			}
			br_verbs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// read some text in the text variable
		String text = aJCas.getDocumentText();

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);
//		Annotation document = new Annotation("Barack Obama was born in Hawaii.  He is the president.  Obama was elected in 2008.");

		// run all Annotators on this text
		pipeline.annotate(document); 
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		 HelperDataStructures hds = new HelperDataStructures();

		
		//SPnew language-specific settings:
		//SPnew subject tags of the parser
		 HashSet<String> subjTag = new HashSet<String>(); 
		 HashSet<String> dirObjTag = new HashSet<String>(); 
		 //subordinate conjunction tags
		 HashSet<String> compTag = new HashSet<String>(); 
		 //pronoun tags
		 HashSet<String> pronTag = new HashSet<String>(); 
		 
		 HashSet<String> passSubjTag = new HashSet<String>();
		 HashSet<String> apposTag = new HashSet<String>(); 
		 HashSet<String> verbComplementTag = new HashSet<String>(); 
		 HashSet<String> infVerbTag = new HashSet<String>(); 
		 HashSet<String> relclauseTag = new HashSet<String>(); 
		 HashSet<String> aclauseTag = new HashSet<String>(); 
		 
		 HashSet<String> compLemma = new HashSet<String>(); 
		 HashSet<String> coordLemma = new HashSet<String>(); 
		 HashSet<String> directQuoteIntro = new HashSet<String>(); 
		 HashSet<String> indirectQuoteIntroChunkValue = new HashSet<String>();
		 
//		 HashSet<String> finiteVerbTag = new HashSet<String>();
		 

		 //OPEN ISSUES PROBLEMS:
		 //the subject - verb relation finding does not account for several specific cases: 
		 //opinion verbs with passive subjects as opinion holder are not accounted for,
		 //what is needed is a marker in the lex files like PASSSUBJ
		 //ex: Obama is worried/Merkel is concerned
		 //Many of the poorer countries are concerned that the reduction in structural funds and farm subsidies may be detrimental in their attempts to fulfill the Copenhagen Criteria.
		 //Some of the more well off EU states are also worried about the possible effects a sudden influx of cheap labor may have on their economies. Others are afraid that regional aid may be diverted away from those who currently benefit to the new, poorer countries that join in 2004 and beyond. 
		// Does not account for infinitival constructions, here again a marker is needed to specify
		 //subject versus object equi
			//Christian Noyer was reported to have said that it is ok.
			//Reuters has reported Christian Noyer to have said that it is ok.
		 //Obama is likely to have said.. 
		 //Several opinion holder- opinion verb pairs in one sentence are not accounted for, right now the first pair is taken.
		 //what is needed is to run through all dependencies. For inderect quotes the xcomp value of the embedded verb points to the 
		 //opinion verb. For direct quotes the offsets closest to thwe quote are relevant.
		 //a specific treatment of inverted subjects is necessary then. Right now the
		 //strategy relies on the fact that after the first subj/dirobj - verb pair the
		 //search is interrupted. Thus, if the direct object precedes the subject, it is taken as subject.
		 // this is the case in incorrectly analysed inverted subjeects as: said Zwickel on Monday
		 //coordination of subject not accounted for:employers and many economists
		 //several subject-opinion verbs:
		 //Zwickel has called the hours discrepancy between east and west a "fairness gap," but employers and many economists point out that many eastern operations have a much lower productivity than their western counterparts.
		  if (language.equals("en")){
           	 subjTag.add("nsubj");
           	 subjTag.add("xsubj");
           	 subjTag.add("nmod:agent");
           	 
           	 dirObjTag.add("dobj"); //for inverted subject: " " said IG metall boss Klaus Zwickel on Monday morning.
           	 						//works only with break DEPENDENCYSEARCH, otherwise "Monday" is nsubj 
           	 						//for infinitival subject of object equi: Reuters reports Obama to have said
           	passSubjTag.add("nsubjpass");
        	apposTag.add("appos");
        	relclauseTag.add("acl:relcl");
        	aclauseTag.add("acl");
           	 compTag.add("mark");
           	 pronTag.add("prp");
           	hds.pronTag.add("prp");
           	compLemma.add("that");
           	compLemma.add("and");
           	verbComplementTag.add("ccomp");
           	verbComplementTag.add("parataxis");

           	infVerbTag.add("xcomp"); //Reuters reported Merkel to have said
           	infVerbTag.add("advcl");
           	directQuoteIntro.add(":");
           	indirectQuoteIntroChunkValue.add("SBAR");
           	hds.objectEqui.add("report");
           	hds.objectEqui.add("quote");
           	hds.potentialIndirectQuoteTrigger.add("say");
//           	hds.objectEqui.add("confirm");
//           	hds.subjectEqui.add("promise");
//           	hds.subjectEqui.add("quote");
//           	hds.subjectEqui.add("confirm");
           }
		  
		  boolean containsSubordinateConjunction = false;
		
		 
		for (CoreMap sentence : sentences) {
//			System.out.println("PREPROCESSING..");
				SentenceAnnotation sentenceAnn = new SentenceAnnotation(aJCas);
				
				int beginSentence = sentence.get(TokensAnnotation.class).get(0)
						.beginPosition();
				int endSentence = sentence.get(TokensAnnotation.class)
						.get(sentence.get(TokensAnnotation.class).size() - 1)
						.endPosition();
				sentenceAnn.setBegin(beginSentence);
				sentenceAnn.setEnd(endSentence);
				sentenceAnn.addToIndexes();
				
				//SP Map offsets to NER
				List<NamedEntity> ners = JCasUtil.selectCovered(aJCas,  //SPnew tut
						NamedEntity.class, sentenceAnn);
				for (NamedEntity ne : ners){
//					System.out.println("NER TYPE: " + ne.jcasType.toString());
//					System.out.println("NER TYPE: " + ne.getCoveredText().toString());
//					System.out.println("NER TYPE: " + ne.jcasType.casTypeCode);
					//Person is 71, Location is 213, Organization is 68 geht anders besser siehe unten
					int nerStart = ne
							.getBegin();
					int nerEnd = ne.getEnd();
//					System.out.println("NER: " + ne.getCoveredText() + " " + nerStart + "-" + nerEnd ); 
					String offsetNer = "" + nerStart + "-" + nerEnd;
					hds.offsetToNer.put(offsetNer, ne.getCoveredText());
//					Ner.add(offsetNer);
					hds.Ner.add(offsetNer);
//					System.out.println("NER: TYPE " +ne.getValue().toString());
					if (ne.getValue().equals("PERSON")){
						hds.NerPerson.add(offsetNer);
					}
					else if (ne.getValue().equals("ORGANIZATION")){
						hds.NerOrganization.add(offsetNer);
					}
				}
				
				//DBpediaLink info: map offsets to links
				List<DBpediaResource> dbpeds = JCasUtil.selectCovered(aJCas,  //SPnew tut
						DBpediaResource.class, sentenceAnn);
				for (DBpediaResource dbped : dbpeds){
//					
//					int dbStart = dbped
//							.getBegin();
//					int dbEnd = dbped.getEnd();
					// not found if dbpedia offsets are wrongly outside than sentences
//					System.out.println("DBPED SENT: " + sentenceAnn.getBegin()+ "-" + sentenceAnn.getEnd() ); 
//					String offsetDB = "" + dbStart + "-" + dbEnd;
//					hds.labelToDBpediaLink.put(dbped.getLabel(), dbped.getUri());
//					System.out.println("NOW DBPED: " + dbped.getLabel() + "URI: " + dbped.getUri() ); 
					hds.dbpediaSurfaceFormToDBpediaLink.put(dbped.getCoveredText(), dbped.getUri());
//					System.out.println("NOW DBPED: " + dbped.getCoveredText() + "URI: " + dbped.getUri() ); 
				}
					
				
				
				
				
				
				
				//SP Map offsets to lemma of opinion verb/noun; parser does not provide lemma
				  for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//					  System.out.println("LEMMA " + token.lemma().toString());
					  int beginTok = token.beginPosition();
					     int endTok = token.endPosition();
					     String offsets = "" + beginTok + "-" + endTok;
					  hds.offsetToLemma.put(offsets, token.lemma().toString());
					  
					    	 if (opinion_verbs.contains(token.lemma().toString())){
							     hds.offsetToLemmaOfOpinionVerb.put(offsets, token.lemma().toString());
							     hds.OpinionExpression.add(offsets);
							     hds.offsetToLemmaOfOpinionExpression.put(offsets, token.lemma().toString());
							     
//			            	   System.out.println("offsetToLemmaOfOpinionVerb " + token.lemma().toString());
					    	 }
					    	 if (passive_opinion_verbs.contains(token.lemma().toString())){
							     hds.offsetToLemmaOfPassiveOpinionVerb.put(offsets, token.lemma().toString());
							     hds.OpinionExpression.add(offsets);
							     hds.offsetToLemmaOfOpinionExpression.put(offsets, token.lemma().toString());
//			            	   System.out.println("offsetToLemmaOfPassiveOpinionVerb " + token.lemma().toString());
					    	 }

				  } 

			//SPnew parser
			Tree tree = sentence.get(TreeAnnotation.class);
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
			Collection<TypedDependency> td = gs.typedDependenciesCollapsed();

			 
//			System.out.println("TYPEDdep" + td);
			
			Object[] list = td.toArray();
//			System.out.println(list.length);
			TypedDependency typedDependency;
DEPENDENCYSEARCH: for (Object object : list) {
			typedDependency = (TypedDependency) object;
//			System.out.println("DEP " + typedDependency.dep().toString()+ 
//					" GOV " + typedDependency.gov().toString()+ 
//			" :: "+ " RELN "+typedDependency.reln().toString());
			String pos = null;
            String[] elements;
            String verbCand = null;
            int beginVerbCand = -5;
			int endVerbCand = -5;
			String offsetVerbCand = null;

            if (compTag.contains(typedDependency.reln().toString())) {
            	containsSubordinateConjunction = true;
//            	System.out.println("subordConj " + typedDependency.dep().toString().toLowerCase());
            }
                      
            else if (subjTag.contains(typedDependency.reln().toString())){
            	hds.predicateToSubjectChainHead.clear();
            	hds.subjectToPredicateChainHead.clear();
            	
            	verbCand = typedDependency.gov().toString().toLowerCase();
				beginVerbCand = typedDependency.gov().beginPosition();
				endVerbCand = typedDependency.gov().endPosition();
				offsetVerbCand = "" + beginVerbCand + "-" + endVerbCand;
//				System.out.println("VERBCand " + verbCand + offsetVerbCand);
//				for (HashMap.Entry<String, String> entry : hds.offsetToLemma.entrySet()) {
//				    String key = entry.getKey();
//				    Object value = entry.getValue();
//				    System.out.println("OFFSET " + key + " LEMMA " + value);
//				    // FOR LOOP
//				}
//				if (hds.offsetToLemma.containsKey(offsetVerbCand)){
//					String verbCandLemma = hds.offsetToLemma.get(offsetVerbCand);
//					if (hds.objectEqui.contains(verbCandLemma) || hds.subjectEqui.contains(verbCandLemma)){
//						System.out.println("SUBJCHAIN BEGIN verbCand " + verbCand);
//						storeRelations(typedDependency, hds.predicateToSubjectChainHead, hds.subjectToPredicateChainHead, hds);
//					}
//				}
//				System.out.println("SUBJCHAINHEAD1");
				storeRelations(typedDependency, hds.predicateToSubjectChainHead, hds.subjectToPredicateChainHead, hds);
//				System.out.println("verbCand " + verbCand);
				//hack for subj after obj said Zwickel (obj) on Monday morning (subj)
				if (language.equals("en") 
						&& hds.predicateToObject.containsKey(offsetVerbCand)
						){
//            		System.out.println("CONTINUE DEP");
            		continue DEPENDENCYSEARCH;
            	}
				else {

            	determineSubjectToVerbRelations(typedDependency, 
            			hds.offsetToLemmaOfOpinionVerb,
            			hds);
				}
            }
            //Merkel is concerned
            else if (passSubjTag.contains(typedDependency.reln().toString())){
            	//Merkel was reported
            	//Merkel was concerned
            	//Merkel was quoted
            	hds.predicateToSubjectChainHead.clear();
            	hds.subjectToPredicateChainHead.clear();
            	verbCand = typedDependency.gov().toString().toLowerCase();
				beginVerbCand = typedDependency.gov().beginPosition();
				endVerbCand = typedDependency.gov().endPosition();
				offsetVerbCand = "" + beginVerbCand + "-" + endVerbCand;
//				System.out.println("VERBCand " + verbCand + offsetVerbCand);
//				if (hds.offsetToLemma.containsKey(offsetVerbCand)){
//					String verbCandLemma = hds.offsetToLemma.get(offsetVerbCand);
////					System.out.println("LEMMA verbCand " + verbCandLemma);
//					if (hds.objectEqui.contains(verbCandLemma) || hds.subjectEqui.contains(verbCandLemma)){
//						System.out.println("SUBJCHAIN BEGIN verbCand " + verbCand);
//						storeRelations(typedDependency, hds.predicateToSubjectChainHead, hds.subjectToPredicateChainHead, hds);
//					}
//				}
            	
            	storeRelations(typedDependency, hds.predicateToSubjectChainHead, hds.subjectToPredicateChainHead, hds);
//            	System.out.println("SUBJCHAINHEAD2");
            	//Merkel is concerned
            	determineSubjectToVerbRelations(typedDependency, 
            			hds.offsetToLemmaOfPassiveOpinionVerb,
            			hds);
            }
            //Meanwhile, the ECB's vice-president, Christian Noyer, was reported at the start of the week to have said that the bank's future interest-rate moves
//            would depend on the behavior of wage negotiators as well as the pace of the economic recovery.

            else if (apposTag.contains(typedDependency.reln().toString())){
            	
            	String subjCand = typedDependency.gov().toString().toLowerCase();
            	int beginSubjCand = typedDependency.gov().beginPosition();
            	int endSubjCand = typedDependency.gov().endPosition();
            	String offsetSubjCand = "" + beginSubjCand + "-" + endSubjCand;
            	String appo = typedDependency.dep().toString().toLowerCase();
				int beginAppo = typedDependency.dep().beginPosition();
				int endAppo = typedDependency.dep().endPosition();
				String offsetAppo = "" + beginAppo + "-" + endAppo;
				
//            	System.out.println("APPOSITION1 " + subjCand + "::"+ appo + ":" + offsetSubjCand + " " + offsetAppo);
            	hds.subjectToApposition.put(offsetSubjCand, offsetAppo);
            }
            else if (relclauseTag.contains(typedDependency.reln().toString())){
            	String subjCand = typedDependency.gov().toString().toLowerCase();
            	int beginSubjCand = typedDependency.gov().beginPosition();
            	int endSubjCand = typedDependency.gov().endPosition();
            	String offsetSubjCand = "" + beginSubjCand + "-" + endSubjCand;
            	verbCand = typedDependency.dep().toString().toLowerCase();
				beginVerbCand = typedDependency.dep().beginPosition();
				endVerbCand = typedDependency.dep().endPosition();
				offsetVerbCand = "" + beginVerbCand + "-" + endVerbCand;
				String subjCandPos = null;
				if (hds.predicateToSubject.containsKey(offsetVerbCand)){
					
					if (subjCand.matches(".+?/.+?")) {          
						elements = subjCand.split("/");
						subjCand = elements[0];
						subjCandPos = elements[1];
						}
					
				
					String del = hds.predicateToSubject.get(offsetVerbCand);
					hds.predicateToSubject.remove(offsetVerbCand);
					hds.subjectToPredicate.remove(del);
					hds.normalPredicateToSubject.remove(offsetVerbCand);
					hds.subjectToNormalPredicate.remove(del);
//					System.out.println("REMOVE RELPRO " + verbCand + "/" + hds.offsetToLemma.get(del));
					hds.predicateToSubject.put(offsetVerbCand, offsetSubjCand);
					hds.subjectToPredicate.put( offsetSubjCand, offsetVerbCand);
					hds.normalPredicateToSubject.put(offsetVerbCand, offsetSubjCand);
					hds.subjectToNormalPredicate.put( offsetSubjCand, offsetVerbCand);
//					System.out.println("RELCLAUSE " + subjCand + "::" + ":" + verbCand);
					hds.offsetToSubjectHead.put(offsetSubjCand,subjCand);
					hds.SubjectHead.add(offsetSubjCand);
					
					if (subjCandPos != null && hds.pronTag.contains(subjCandPos)){
						hds.PronominalSubject.add(offsetSubjCand);
					}
				}
            	
            	
            }
            
            else if (dirObjTag.contains(typedDependency.reln().toString())
            		){
            	storeRelations(typedDependency, hds.predicateToObject, hds.ObjectToPredicate, hds);
            	verbCand = typedDependency.gov().toString().toLowerCase();
				beginVerbCand = typedDependency.gov().beginPosition();
				endVerbCand = typedDependency.gov().endPosition();
				offsetVerbCand = "" + beginVerbCand + "-" + endVerbCand;
				
				String objCand = typedDependency.dep().toString().toLowerCase();
            	int beginObjCand = typedDependency.dep().beginPosition();
            	int endObjCand = typedDependency.dep().endPosition();
            	String offsetObjCand = "" + beginObjCand + "-" + endObjCand;
            	String objCandPos;
            	if (objCand.matches(".+?/.+?")) {          
					elements = objCand.split("/");
					objCand = elements[0];
					objCandPos = elements[1];
//					System.out.println("PRON OBJ " + objCandPos);
					if (pronTag.contains(objCandPos)){
					hds.PronominalSubject.add(offsetObjCand);
					}
					}
//            	System.out.println("DIROBJ STORE ONLY");
            	//told Obama
            	//said IG metall boss Klaus Zwickel
            	// problem: pointing DO
            	//explains David Gems, pointing out the genetically manipulated species.
            	 if (language.equals("en") 
 						&& !hds.normalPredicateToSubject.containsKey(offsetVerbCand)
 						){
//            		 System.out.println("INVERSE SUBJ HACK ENGLISH PREDTOOBJ");
            	determineSubjectToVerbRelations(typedDependency, 
            			hds.offsetToLemmaOfOpinionVerb,
            			hds);
            	 }
            }
            //was reported to have said
            else if (infVerbTag.contains(typedDependency.reln().toString())){
            	storeRelations(typedDependency, hds.mainToInfinitiveVerb, hds.infinitiveToMainVerb, hds);
//            	System.out.println("MAIN-INF");
              	determineSubjectToVerbRelations(typedDependency, 
            			hds.offsetToLemmaOfOpinionVerb,
            			hds);
            	
            }
            else if (aclauseTag.contains(typedDependency.reln().toString())){
            	storeRelations(typedDependency, hds.nounToInfinitiveVerb, hds.infinitiveVerbToNoun, hds);
//            	System.out.println("NOUN-INF");
              	determineSubjectToVerbRelations(typedDependency, 
            			hds.offsetToLemmaOfOpinionVerb,
            			hds);
            	
            }
            
            

			}
			
//			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//			System.out.println("SEM-DEP " + dependencies);	
		}
		

		Map<Integer, edu.stanford.nlp.dcoref.CorefChain> corefChains = document.get(CorefChainAnnotation.class);
		   
		    if (corefChains == null) { return; }
		    //SPCOPY
		      for (Entry<Integer, edu.stanford.nlp.dcoref.CorefChain> entry: corefChains.entrySet()) {
//		        System.out.println("Chain " + entry.getKey() + " ");
		    	int chain = entry.getKey();
		        String repMenNer = null;
		        String repMen = null;
		        String offsetRepMenNer = null;

		        List<IaiCorefAnnotation> listCorefAnnotation = new ArrayList<IaiCorefAnnotation>();
		        
		        for (CorefMention m : entry.getValue().getMentionsInTextualOrder()) {
		        	boolean corefMentionContainsNer = false;
		        	boolean repMenContainsNer = false;

//		                

				// We need to subtract one since the indices count from 1 but the Lists start from 0
		        	List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(TokensAnnotation.class);
		          // We subtract two for end: one for 0-based indexing, and one because we want last token of mention not one following.
//		          System.out.println("  " + m + ", i.e., 0-based character offsets [" + tokens.get(m.startIndex - 1).beginPosition() +
//		                  ", " + tokens.get(m.endIndex - 2).endPosition() + ")");
		          
		          int beginCoref = tokens.get(m.startIndex - 1).beginPosition();
				  int endCoref = tokens.get(m.endIndex - 2).endPosition();
				  String offsetCorefMention = "" + beginCoref + "-" + endCoref;
				  String corefMention  = m.mentionSpan;

				  CorefMention RepresentativeMention = entry.getValue().getRepresentativeMention();
				  repMen = RepresentativeMention.mentionSpan;
				  List<CoreLabel> repMenTokens = sentences.get(RepresentativeMention.sentNum - 1).get(TokensAnnotation.class);
//				  System.out.println("REPMEN ANNO " + "\"" + repMen + "\"" + " is representative mention" +
//                          ", i.e., 0-based character offsets [" + repMenTokens.get(RepresentativeMention.startIndex - 1).beginPosition() +
//		                  ", " + repMenTokens.get(RepresentativeMention.endIndex - 2).endPosition() + ")");
				  int beginRepMen = repMenTokens.get(RepresentativeMention.startIndex - 1).beginPosition();
				  int endRepMen = repMenTokens.get(RepresentativeMention.endIndex - 2).endPosition();
				  String offsetRepMen = "" + beginRepMen + "-" + endRepMen;
		        	  
				//Determine repMenNer that consists of largest NER (Merkel) to (Angela Merkel)
				  //and "Chancellor "Angela Merkel" to "Angela Merkel"
		    	  //Further reduction to NER as in "Chancellor Angela Merkel" to "Angela Merkel" is
				  //done in determineBestSubject. There, Chunk information and subjectHead info is available.
				  //Chunk info and subjectHead info is used to distinguish "Chancellor Angela Merkel" to "Angela Merkel"
				  //from "The enemies of Angela Merkel" which is not reduced to "Angela Merkel"
				  //Problem solved: The corefMentions of a particular chain do not necessarily have the same RepMenNer (RepMen) 
				  // any more: Chancellor Angela Merkel repMenNer Chancellor Angela Merkel , then Angela Merkel has RepMenNer  Angela Merkel
				  if (offsetRepMenNer != null && hds.Ner.contains(offsetRepMenNer)){
//					  System.out.println("NEWNer.contains(offsetRepMenNer)");
				  }
				  else if (offsetRepMen != null && hds.Ner.contains(offsetRepMen)){
					  repMenNer = repMen;
						offsetRepMenNer = offsetRepMen;
//						System.out.println("NEWNer.contains(offsetRepMen)");
				  }
				  else if (offsetCorefMention != null && hds.Ner.contains(offsetCorefMention)){
						repMenNer = corefMention;
						offsetRepMenNer = offsetCorefMention;
//						System.out.println("Ner.contains(offsetCorefMention)");
					}
				  else {
					  corefMentionContainsNer = offsetsContainAnnotation(offsetCorefMention,hds.Ner);
					  repMenContainsNer = offsetsContainAnnotation(offsetRepMen,hds.Ner);
//					  System.out.println("ELSE Ner.contains(offsetCorefMention)");
				  }
				  //Determine repMenNer that contains NER
					if (repMenNer == null){
						if (corefMentionContainsNer){
							repMenNer = corefMention;
							offsetRepMenNer = offsetCorefMention;
//							System.out.println("DEFAULT1");
						}
						else if (repMenContainsNer){
							repMenNer = repMen;
							offsetRepMenNer = offsetRepMen;
//							System.out.println("DEFAULT2");
						}
						//no NER:
						//Pronoun -> repMen is repMenNer
						else if (hds.PronominalSubject.contains(offsetCorefMention) && repMen != null){
							repMenNer = repMen;
							offsetRepMenNer = offsetRepMen;
//							System.out.println("DEFAULT3");
						}
						else {
							//other no NER: corefMention is repMenNer because it is closer to original
						repMenNer = corefMention;
						offsetRepMenNer = offsetCorefMention;
//						System.out.println("DEFAULT4");
						}
					}
		          
 	              IaiCorefAnnotation corefAnnotation = new IaiCorefAnnotation(aJCas);
			
					


					corefAnnotation.setBegin(beginCoref);
					corefAnnotation.setEnd(endCoref);
					corefAnnotation.setCorefMention(corefMention);
					corefAnnotation.setCorefChain(chain);
					//done below
//					corefAnnotation.setRepresentativeMention(repMenNer);
//					corefAnnotation.addToIndexes(); 
				
					
					listCorefAnnotation.add(corefAnnotation);
					
//					done below:
//					 offsetToRepMen.put(offsetCorefMention, repMenNer);
//					 RepMen.add(offsetCorefMention);
					
					 
		        }//end coref mention
//		        System.out.println("END Chain " + chain );
//		        System.out.println(listCorefAnnotation.size());
		        String offsetCorefMention = null;
		        for (int i = 0; i < listCorefAnnotation.size(); i++) {
		        	IaiCorefAnnotation corefAnnotation = listCorefAnnotation.get(i);
		        	corefAnnotation.setRepresentativeMention(repMenNer);
		        	corefAnnotation.addToIndexes();
		        	offsetCorefMention = "" + corefAnnotation.getBegin() + "-" + corefAnnotation.getEnd();
					hds.offsetToRepMen.put(offsetCorefMention, repMenNer);
					hds.RepMen.add(offsetCorefMention);
					//COREF
//					System.out.println("Chain " + corefAnnotation.getCorefChain());
//					System.out.println("corefMention " + corefAnnotation.getCorefMention() + offsetCorefMention);
//					System.out.println("repMenNer " + repMenNer);
		        }
		      } //end chains


//		      System.out.println("NOW quote finder");


		
		///* quote finder: begin find quote relation and quotee
		// direct quotes
		
		
		String quotee_left = null;
		String quotee_right = null; 
		
		String representative_quotee_left = null;
		String representative_quotee_right = null; 
		
		String quote_relation_left = null;
		String quote_relation_right = null;
		
		String quoteType = null;
		int quoteeReliability = 5;
		int quoteeReliability_left = 5;
		int quoteeReliability_right = 5;
		int quotee_end = -5;
		int quotee_begin = -5;
		
		boolean quoteeBeforeQuote = false;


	
		
		// these are all the quotes in this document
		List<CoreMap> quotes = document.get(QuotationsAnnotation.class);
		for (CoreMap quote : quotes) {
			if (quote.get(TokensAnnotation.class).size() > 5) {
				QuoteAnnotation annotation = new QuoteAnnotation(aJCas);

				int beginQuote = quote.get(TokensAnnotation.class).get(0)
						.beginPosition();
				int endQuote = quote.get(TokensAnnotation.class)
						.get(quote.get(TokensAnnotation.class).size() - 1)
						.endPosition();
				annotation.setBegin(beginQuote);
				annotation.setEnd(endQuote);
				annotation.addToIndexes();
//			}
//		}
//		
//		List<Q> newQuotes = document.get(QuotationsAnnotation.class);		
//		for (CoreMap annotation : newQuotes) {
//				if (1==1){
//				Re-initialize markup variables since they are also used for indirect quotes
				quotee_left = null;
				quotee_right = null; 
				
				representative_quotee_left = null;
				representative_quotee_right = null;
				
				quote_relation_left = null;
				quote_relation_right = null;
				quoteeReliability = 5;
				quoteeReliability_left = 5;
				quoteeReliability_right = 5;
				quotee_end = -5;
				quotee_begin = -5;
				quoteType = "direct";
				quoteeBeforeQuote = false;

				
				
				
				List<Token> directQuoteTokens = JCasUtil.selectCovered(aJCas,
						Token.class, annotation);
				
				
				
				List<Token> followTokens = JCasUtil.selectFollowing(aJCas,
						Token.class, annotation, 1);
				
              
//				for (Token aFollowToken: followTokens){
//					   List<Chunk> chunks = JCasUtil.selectCovering(aJCas,
//					Chunk.class, aFollowToken);
  
//direct quote quotee right:
				
	   for (Token aFollow2Token: followTokens){
					   List<SentenceAnnotation> sentencesFollowQuote = JCasUtil.selectCovering(aJCas,
							   SentenceAnnotation.class, aFollow2Token);
					   
					   
		   for (SentenceAnnotation sentenceFollowsQuote: sentencesFollowQuote){
						   List<Chunk> chunks = JCasUtil.selectCovered(aJCas,
						Chunk.class, sentenceFollowsQuote);
//			System.out.println("DIRECT QUOTE RIGHT");
			String[] quote_annotation_result = determine_quotee_and_quote_relation("RIGHT", 
					chunks, hds, annotation);
			if (quote_annotation_result.length>=4){
			   quotee_right = quote_annotation_result[0];
			   representative_quotee_right = quote_annotation_result[1];
			   quote_relation_right = quote_annotation_result[2];
			   try {
				   quoteeReliability = Integer.parseInt(quote_annotation_result[3]);
				   quoteeReliability_right = Integer.parseInt(quote_annotation_result[3]);
				} catch (NumberFormatException e) {
			      //Will Throw exception!
			      //do something! anything to handle the exception.
				quoteeReliability = -5;
				quoteeReliability_right = -5;
				}					   
			 }
//			System.out.println("DIRECT QUOTE RIGHT RESULT quotee " + quotee_right + " representative_quotee " + representative_quotee_right
//					+ " quote_relation " + quote_relation_right);
		   
			}
					   
					   
      }
				
				
				List<Token> precedingTokens = JCasUtil.selectPreceding(aJCas,
						Token.class, annotation, 1);
                for (Token aPrecedingToken: precedingTokens){ 
                	
                	if (directQuoteIntro.contains(aPrecedingToken.getLemma().getValue().toString()) 
                			|| compLemma.contains(aPrecedingToken.getLemma().getValue().toString())) {
//                		System.out.println("Hello, World lemma found" + aPrecedingToken.getLemma().getValue());
                		quoteeBeforeQuote = true;
                	}
                		List <NamedEntity> namedEntities = null;
                		List <Token> tokens = null;
                		List<Chunk> chunks = null;
                		
				        List<Sentence> precedingSentences = JCasUtil.selectPreceding(aJCas,
				        		Sentence.class, aPrecedingToken, 1);
				        
						if (precedingSentences.isEmpty()){
							List<Sentence> firstSentence;
				        	firstSentence = JCasUtil.selectCovering(aJCas,
				        		Sentence.class, aPrecedingToken);

				        	for (Sentence aSentence: firstSentence){
				        		

								chunks = JCasUtil.selectCovered(aJCas,
									Chunk.class, aSentence.getBegin(), aPrecedingToken.getEnd());

									   
				        		namedEntities = JCasUtil.selectCovered(aJCas,
	                	    		NamedEntity.class, aSentence.getBegin(), aPrecedingToken.getEnd());
				        		tokens = JCasUtil.selectCovered(aJCas,
				        				Token.class, aSentence.getBegin(), aPrecedingToken.getEnd());
				        	
				        	}
						}
				        else {	
				        	for (Sentence aSentence: precedingSentences){
//				        		System.out.println("Hello, World sentence" + aSentence);
				        		chunks = JCasUtil.selectBetween(aJCas,
				        				Chunk.class, aSentence, aPrecedingToken);
				        		namedEntities = JCasUtil.selectBetween(aJCas,
				        				NamedEntity.class, aSentence, aPrecedingToken);
				        		tokens = JCasUtil.selectBetween(aJCas,
				        				Token.class, aSentence, aPrecedingToken);
				        	}
				        }
                	
//				       
//						System.out.println("DIRECT QUOTE LEFT");
						String[] quote_annotation_direct_left = determine_quotee_and_quote_relation("LEFT", chunks,
								 hds, annotation
								
							   );
//						System.out.println("QUOTE ANNOTATION " + quote_annotation_direct_left.length);		
		if (quote_annotation_direct_left.length>=4){
//			System.out.println("QUOTE ANNOTATION UPDATE " + quote_annotation_direct_left[0] +
//					" " + quote_annotation_direct_left[1] + " " +
//					quote_annotation_direct_left[2]);
		   quotee_left = quote_annotation_direct_left[0];
		   representative_quotee_left = quote_annotation_direct_left[1];
		   quote_relation_left = quote_annotation_direct_left[2];
		   try {
			   quoteeReliability = Integer.parseInt(quote_annotation_direct_left[3]);
			   quoteeReliability_left = Integer.parseInt(quote_annotation_direct_left[3]);
			} catch (NumberFormatException e) {
		      //Will Throw exception!
		      //do something! anything to handle the exception.
			quoteeReliability = -5;
			quoteeReliability_left = -5;
			}					   
		 }
//		System.out.println("DIRECT QUOTE LEFT RESULT quotee " + quotee_left + " representative_quotee " + representative_quotee_left
//				+ " quote_relation " + quote_relation_left);
		//no subject - predicate quotee quote_relation, quote introduced with colon: 
		if (quotee_left == null && quote_relation_left == null && representative_quotee_left == null 
		&& directQuoteIntro.contains(aPrecedingToken.getLemma().getValue().toString())){
//			System.out.println("NER DIRECT QUOTE LEFT COLON");
			String quoteeCandOffset = null; 
			String quoteeCandText = null;
		     if (namedEntities.size() == 1){
     	    	for (NamedEntity ne : namedEntities){
//     	    		System.out.println("ONE NER " + ne.getCoveredText());
     	    		quoteeCandText = ne.getCoveredText();
					quote_relation_left = aPrecedingToken.getLemma().getValue().toString();
					quotee_end = ne.getEnd();
					quotee_begin = ne.getBegin();
					quoteeCandOffset = "" + quotee_begin + "-" + quotee_end;
					quoteeReliability = 1;
					quoteeReliability_left = 1;
     	        }
     	    }
     	    else if (namedEntities.size() > 1) {
     	    	int count = 0;
     	    	String quotee_cand = null;
//     	    	System.out.println("Hello, World ELSE SEVERAL NER");
       	    	for (NamedEntity ner : namedEntities){
//       	    		System.out.println("Hello, World NER TYPE" + ner.getValue());
       	    		if (ner.getValue().equals("PERSON")){
       	    			count = count + 1;
       	    			quotee_cand = ner.getCoveredText();
       	    			quotee_end = ner.getEnd();
       	    			quotee_begin = ner.getBegin();
       	    			quoteeCandOffset = "" + quotee_begin + "-" + quotee_end;
       	    			
//       	    			System.out.println("Hello, World FOUND PERSON" + quotee_cand);
       	    		}
     	    	}
       	    	if (count == 1){      // there is exactly one NER.PERSON
//       	    		System.out.println("ONE PERSON, SEVERAL NER " + quotee_cand);
       	    			quoteeCandText = quotee_cand;
						quote_relation_left = aPrecedingToken.getLemma().getValue().toString();
						quoteeReliability = 3;
						quoteeReliability_left = 3;
       	    	}
     	    }
		     if(quoteeCandOffset != null && quoteeCandText != null ){
//		    	 quotee_left = quoteeCandText;
		    	 String result [] =  determineBestRepMenSubject(
		    			 quoteeCandOffset,quoteeCandOffset, quoteeCandText, hds);
		    	 if (result.length>=2){
		    		 quotee_left = result [0];
		    		 representative_quotee_left = result [1];
//		    	 System.out.println("RESULT2 NER quotee " + quotee_left + " representative_quotee " + representative_quotee_left);
		    	 }
		     }
		}
                }
		
                

				
				if (quotee_left != null && quotee_right != null){
//					System.out.println("TWO QUOTEES");
					
					if (directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals(".") 
						|| 	directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals("!")
						|| directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals("?")
							){
//						System.out.println("PUNCT " + quotee_left + quote_relation_left + quoteeReliability_left);
						annotation.setQuotee(quotee_left);
						annotation.setQuoteRelation(quote_relation_left);
						annotation.setQuoteType(quoteType);
						annotation.setQuoteeReliability(quoteeReliability_left);
						annotation.setRepresentativeQuoteeMention(representative_quotee_left);

					}
					else if (directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals(",")){
//						System.out.println("COMMA " + quotee_right + " " + quote_relation_right + " " + quoteeReliability_right);
						annotation.setQuotee(quotee_right);
						annotation.setQuoteRelation(quote_relation_right);
						annotation.setQuoteType(quoteType);
						annotation.setQuoteeReliability(quoteeReliability_right);
						annotation.setRepresentativeQuoteeMention(representative_quotee_right);
					}
					else if (!directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals(".")
							&& !directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals("!")
							&& !directQuoteTokens.get(directQuoteTokens.size() - 2).getLemma().getValue().equals("?")
							
							
							){
//						System.out.println("NO PUNCT " + quotee_right + " " + quote_relation_right + " " + quoteeReliability_right);
						annotation.setQuotee(quotee_right);
						annotation.setQuoteRelation(quote_relation_right);
						annotation.setQuoteType(quoteType);
						annotation.setQuoteeReliability(quoteeReliability_right);
						annotation.setRepresentativeQuoteeMention(representative_quotee_right);
					}
					else {
//						System.out.println("UNCLEAR LEFT RIGHT " + quotee_left + quote_relation_left + quote + quotee_right + quote_relation_right);
					annotation.setQuotee("<unclear>");
					annotation.setQuoteRelation("<unclear>");
					annotation.setQuoteType(quoteType);
					}
				}
				else if (quoteeBeforeQuote == true){
					annotation.setQuotee(quotee_left);
					annotation.setQuoteRelation(quote_relation_left);
					annotation.setQuoteType(quoteType);
					annotation.setQuoteeReliability(quoteeReliability_left);
					annotation.setRepresentativeQuoteeMention(representative_quotee_left);
				}
				else if (quotee_left != null){
//					System.out.println("QUOTEE LEFT" + quotee_left + quote_relation_left + quoteeReliability_left);
					annotation.setQuotee(quotee_left);
					annotation.setQuoteRelation(quote_relation_left);
					annotation.setQuoteType(quoteType);
					annotation.setQuoteeReliability(quoteeReliability_left);
					annotation.setRepresentativeQuoteeMention(representative_quotee_left);
				}
				else if (quotee_right != null){
//					System.out.println("QUOTEE RIGHT FOUND" + quotee_right + " QUOTE RELATION " + quote_relation_right + ":" + quoteeReliability_right);
					annotation.setQuotee(quotee_right);
					annotation.setQuoteRelation(quote_relation_right);
					annotation.setQuoteType(quoteType);
					annotation.setQuoteeReliability(quoteeReliability_right);
					annotation.setRepresentativeQuoteeMention(representative_quotee_right);
				}
				else if (quote_relation_left != null ){
					annotation.setQuoteRelation(quote_relation_left);
					annotation.setQuoteType(quoteType);
//					System.out.println("NO QUOTEE FOUND" + quote + quote_relation_left + quote_relation_right);
				}
				else if (quote_relation_right != null){
					annotation.setQuoteRelation(quote_relation_right);
					annotation.setQuoteType(quoteType);
				}
				else if (quoteType != null){
					annotation.setQuoteType(quoteType);
//					System.out.println("Hello, World NO QUOTEE and NO QUOTE RELATION FOUND" + quote);
				}
				if (annotation.getRepresentativeQuoteeMention() != null){
//					System.out.println("NOW!!" + annotation.getRepresentativeQuoteeMention());
					if (hds.dbpediaSurfaceFormToDBpediaLink.containsKey(annotation.getRepresentativeQuoteeMention())){
						annotation.setQuoteeDBpediaUri(hds.dbpediaSurfaceFormToDBpediaLink.get(annotation.getRepresentativeQuoteeMention()));
//						System.out.println("DBPRED FOUND" + annotation.getRepresentativeQuoteeMention() +  " URI: " + annotation.getQuoteeDBpediaUri());
					}
					
					
				}
			}
			
			
		} //for direct quote
		
		// annotate indirect quotes: opinion verb + 'that' ... until end of sentence: said that ...

//		List<CoreMap> sentences = document.get(SentencesAnnotation.class); //already instantiated above
INDIRECTQUOTE:		for (CoreMap sentence : sentences) {
//			if (sentence.get(TokensAnnotation.class).size() > 5) { 
				SentenceAnnotation sentenceAnn = new SentenceAnnotation(aJCas);
				
				int beginSentence = sentence.get(TokensAnnotation.class).get(0)
						.beginPosition();
				int endSentence = sentence.get(TokensAnnotation.class)
						.get(sentence.get(TokensAnnotation.class).size() - 1)
						.endPosition();
				sentenceAnn.setBegin(beginSentence);
				sentenceAnn.setEnd(endSentence);
//				sentenceAnn.addToIndexes();
				
				QuoteAnnotation indirectQuote = new QuoteAnnotation(aJCas);
	        	int indirectQuoteBegin = -5;
				int indirectQuoteEnd = -5;
				boolean subsequentDirectQuoteInstance = false;
				
				List<Chunk> chunksIQ = JCasUtil.selectCovered(aJCas,
				Chunk.class, sentenceAnn);
				List<Chunk> chunksBeforeIndirectQuote = null;
				int index = 0;
INDIRECTCHUNK:	for (Chunk aChunk : chunksIQ) {
					index++;
					
//					System.out.println("INDIRECT QUOTE CHUNK VALUE " + aChunk.getChunkValue().toString());
//					if (aChunk.getChunkValue().equals("SBAR")) {
					if(indirectQuoteIntroChunkValue.contains(aChunk.getChunkValue())){
//						System.out.println("INDIRECT QUOTE INDEX " + "" + index);
						
						List<Token> tokensSbar = JCasUtil.selectCovered(aJCas,
								Token.class, aChunk);
						
						
						for (Token aTokenSbar : tokensSbar){
//							String that = "that";
							if (compLemma.contains(aTokenSbar.getLemma().getCoveredText())){
						// VP test: does that clause contain VP?
					
//			        	QuoteAnnotation indirectQuote = new QuoteAnnotation(aJCas);
								
								//NEW
//								chunksBeforeIndirectQuote = chunksIQ.subList(0, index-1);
//								Chunk chunkBeforeIndirectQuote = chunksBeforeIndirectQuote.get(chunksBeforeIndirectQuote.size()-1);
//								List<Token> tokensBeforeSbar = JCasUtil.selectCovered(aJCas,
//										Token.class, chunkBeforeIndirectQuote);
//								for (Token aTokenBeforeSbar : tokensBeforeSbar){
////									String and;
//									if (coordLemma.contains(aTokenBeforeSbar.getLemma().getCoveredText())){
//										System.out.println("COORD SUBORD");
//									}
//								}
								
								
								List<QuoteAnnotation> coveringDirectQuoteChunk = JCasUtil.selectCovering(aJCas,
										QuoteAnnotation.class, aChunk);
								if (coveringDirectQuoteChunk.isEmpty()){
								 indirectQuoteBegin = aChunk.getEnd() + 1;
								 indirectQuote.setBegin(indirectQuoteBegin);
								 chunksBeforeIndirectQuote = chunksIQ.subList(0, index-1);
								 indirectQuoteEnd = sentenceAnn.getEnd();
								 indirectQuote.setEnd(indirectQuoteEnd);
								 indirectQuote.addToIndexes();
								 subsequentDirectQuoteInstance = false;
//								 System.out.println("SUBSEQUENT FALSE");
								 
								 
								 
								 
								 List<Token> followTokens = JCasUtil.selectFollowing(aJCas,
											Token.class, indirectQuote, 1);
								 for (Token aFollow3Token: followTokens){
									 List<QuoteAnnotation> subsequentDirectQuotes = JCasUtil.selectCovering(aJCas,
											QuoteAnnotation.class,aFollow3Token);
									   if (!subsequentDirectQuotes.isEmpty()){
										   for (QuoteAnnotation subsequentDirectQuote: subsequentDirectQuotes){
											   if (subsequentDirectQuote.getRepresentativeQuoteeMention() != null
												   && subsequentDirectQuote.getRepresentativeQuoteeMention().equals("<unclear>")){
//											   System.out.println("SUBSEQUENT FOUND"); 
											   hds.subsequentDirectQuote = subsequentDirectQuote;
											   subsequentDirectQuoteInstance = true;
											   }
										   }
									   }
								 }
								 
								 break INDIRECTCHUNK;
								}
							}
						}
					}		
				}
					if (indirectQuoteBegin >= 0 && indirectQuoteEnd >= 0){
//						List<QuoteAnnotation> coveringDirectQuote = JCasUtil.selectCovering(aJCas,
//								QuoteAnnotation.class, indirectQuote);
//						if (coveringDirectQuote.isEmpty()){
////							
//						indirectQuote.addToIndexes();
//						}
//						else {
//							//indirect quote is covered by direct quote and therefore discarded
//							continue INDIRECTQUOTE;
//						}
					List<QuoteAnnotation> coveredDirectQuotes = JCasUtil.selectCovered(aJCas,
								QuoteAnnotation.class, indirectQuote);
					for (QuoteAnnotation coveredDirectQuote : coveredDirectQuotes){
//						System.out.println("Hello, World covered direct quote" + coveredDirectQuote.getCoveredText());
						//delete coveredDirectQuoteIndex
						coveredDirectQuote.removeFromIndexes();
						}
					}
					else {
						//no indirect quote in sentence
						continue INDIRECTQUOTE;
					}
						
						
//						Re-initialize markup variables since they are also used for direct quotes
						quotee_left = null;
						quotee_right = null; 
						
						representative_quotee_left = null;
						representative_quotee_right = null;
						
						quote_relation_left = null;
						quote_relation_right = null;
						
						quoteType = "indirect";
						quoteeReliability = 5;
						quoteeReliability_left = 5;
						quoteeReliability_right = 5;
						quotee_end = -5;
						
						
						
			if (chunksBeforeIndirectQuote != null){
//							System.out.println("chunksBeforeIndirectQuote FOUND!! ");
							String[] quote_annotation_result = determine_quotee_and_quote_relation("LEFT", chunksBeforeIndirectQuote,
									 hds, indirectQuote
									
								   );
			if (quote_annotation_result.length>=4){
			   quotee_left = quote_annotation_result[0];
			   representative_quotee_left = quote_annotation_result[1];
			   quote_relation_left = quote_annotation_result[2];
//			   System.out.println("INDIRECT QUOTE LEFT RESULT quotee " + quotee_left + " representative_quotee " + representative_quotee_left
//					   + " QUOTE RELATION " + quote_relation_left);
			   try {
				   quoteeReliability = Integer.parseInt(quote_annotation_result[3]);
				   quoteeReliability_left = Integer.parseInt(quote_annotation_result[3]);
				} catch (NumberFormatException e) {
				quoteeReliability = -5;
				quoteeReliability_left = -5;
				}					   
			 }
			
			}		
						
						
						if (quotee_left != null){
							indirectQuote.setQuotee(quotee_left);
							indirectQuote.setQuoteRelation(quote_relation_left);
							indirectQuote.setQuoteType(quoteType);
							indirectQuote.setQuoteeReliability(quoteeReliability_left);
							indirectQuote.setRepresentativeQuoteeMention(representative_quotee_left);
							
							//indirect quote followed by direct quote:
							//the quotee and quote relation of the indirect quote are copied to the direct quote 
							//Genetic researcher Otmar Wiestler hopes that the government's strict controls on genetic research 
							//will be relaxed with the advent of the new ethics commission. 
							//"For one thing the government urgently needs advice, because of course it's such an extremely 
							//complex field. And one of the reasons Chancellor Schröder formed this new commission was without 
							//a doubt to create his own group of advisors."

							if (subsequentDirectQuoteInstance == true
								&&	hds.subsequentDirectQuote.getRepresentativeQuoteeMention().equals("<unclear>")
								&& 	hds.subsequentDirectQuote.getQuotee().equals("<unclear>")
								&& 	hds.subsequentDirectQuote.getQuoteRelation().equals("<unclear>")
									){
//								System.out.println("SUBSEQUENT UNCLEAR DIR QUOTE FOUND!!"); 
								int begin = hds.subsequentDirectQuote.getBegin();
								int end = hds.subsequentDirectQuote.getEnd();
								
								hds.subsequentDirectQuote.setQuotee(quotee_left);
								hds.subsequentDirectQuote.setQuoteRelation(quote_relation_left);
								hds.subsequentDirectQuote.setQuoteType("direct");
								hds.subsequentDirectQuote.setQuoteeReliability(quoteeReliability_left + 2);
								hds.subsequentDirectQuote.setRepresentativeQuoteeMention(representative_quotee_left);
								hds.subsequentDirectQuote.addToIndexes();
							}
							
							
							
							
//							System.out.println("Hello, World INDIRECT QUOTE " + quotee_left + quote_relation_left + quoteeReliability);
						}
						else if (quote_relation_left != null){
							indirectQuote.setQuoteRelation(quote_relation_left);
							indirectQuote.setQuoteType(quoteType);
						}
						
						else if (quoteType != null){
							indirectQuote.setQuoteType(quoteType);
//							System.out.println("Hello, World INDIRECT QUOTE NOT FOUND" + quote_relation_left);
						}
						if (indirectQuote.getRepresentativeQuoteeMention() != null){
//							System.out.println("NOW!!" + indirectQuote.getRepresentativeQuoteeMention());
							if (hds.dbpediaSurfaceFormToDBpediaLink.containsKey(indirectQuote.getRepresentativeQuoteeMention())){
								indirectQuote.setQuoteeDBpediaUri(hds.dbpediaSurfaceFormToDBpediaLink.get(indirectQuote.getRepresentativeQuoteeMention()));
//								System.out.println("DBPEDIA " + indirectQuote.getRepresentativeQuoteeMention() +  " URI: " + hds.dbpediaSurfaceFormToDBpediaLink.get(indirectQuote.getRepresentativeQuoteeMention()));
							}
							
							
						}
//						}
//				}
//			  }
//			}  //for chunk
//				say without that
//			}		
		} //Core map sentences indirect quotes
		
		
		
		
		
	}
	// quote finder: end find quote relation and quotee */

	public boolean offsetsContainAnnotation (String offsets, HashSet<String> anno){
		for (String offsetAnno : anno) {
		  if (containsOffsets(offsets, offsetAnno)){
			  return true;
		  }
		}
		 return false;
	}
		
	public String getOffsetsOfContainingAnnotation (String offsets, HashSet<String> anno){
		for (String offsetAnno : anno) {
		  if (containsOffsets(offsetAnno, offsets)){
			  return offsetAnno;
		  }
		}
		 return null;
	}
	
	public String getOffsetsOfContainedAnnotation (String offsets, HashSet<String> anno){
		for (String offsetAnno : anno) {
		  if (containsOffsets(offsets, offsetAnno)){
			  return offsetAnno;
		  }
		}
		 return null;
	}
	
public String determineBestContainingNerSubject(String subjectChunkoffsets,List<HashSet<String>> listNerTypes){
	
	
	String subjectNerOffset = null;
	for (int i = 0; i < listNerTypes.size(); i++) {
		subjectNerOffset = getOffsetsOfContainedAnnotation(subjectChunkoffsets,listNerTypes.get(i));
		if (subjectNerOffset != null){
//			System.out.println("determineBestContainingNer ");
			return subjectNerOffset;
			
		}
		
	}
	return null;
}
	
public String determineBestIsNerSubject(String offsets,List<HashSet<String>> listNerTypes){
	
	
	String subjectNerOffset = null;
	for (int i = 0; i < listNerTypes.size(); i++) {
		if (listNerTypes.get(i).contains(offsets)){
			subjectNerOffset= offsets;
//			System.out.println("determineBestIsNer ");
			return subjectNerOffset;
			
		}
		
	}
	return null;
}	

public String [] determineBestRepMenSubject(String subjectHeadoffsets, String subjectChunkoffsets, 
			String subjectChunkText,
			QuoteAnnotator.HelperDataStructures hds){
	
	String subjectRepMen = null;
	String subjectRepMenText = null;
//	System.out.println("SUBJCHUNK "+subjectChunkText);
   
	String [] result;
	   result = new String[2];
	   result[0]= subjectChunkText;
	   result[1]= subjectRepMenText;
	   
    List<HashSet<String>> listNerTypes = new ArrayList<HashSet<String>>();
	listNerTypes.add(hds.NerPerson);
	listNerTypes.add(hds.NerOrganization);
	listNerTypes.add(hds.Ner);
	
	 String repMenOfSubjectHead;
//	Determine offsets of subjectHead  that have repMen
	repMenOfSubjectHead = getOffsetsOfContainingAnnotation(subjectHeadoffsets,hds.RepMen);				
//	System.out.println("REPMEN " + hds.offsetToRepMen.get(repMenOfSubjectHead));
//	System.out.println ("SERACH PRON " + subjectChunkoffsets);
//	for (String s : hds.PronominalSubject) {
//	    System.out.println("PRON OFFSET " + s);
//	}
	if (hds.PronominalSubject.contains(subjectChunkoffsets) && subjectChunkoffsets.equals(subjectHeadoffsets)){
		subjectRepMenText = hds.offsetToRepMen.get(subjectHeadoffsets);
//		System.out.println("PRONOUN found! " + subjectChunkText + " " + subjectRepMenText);
	}
	
	if(subjectRepMenText == null) {
		//repMen of subject head equals NER
		subjectRepMen = determineBestIsNerSubject(repMenOfSubjectHead, listNerTypes);
		subjectRepMenText = hds.offsetToRepMen.get(subjectRepMen);
//		System.out.println("repMen of subject head equals NER " + subjectRepMenText);
	}
		
		if (subjectRepMenText == null) {
//			subject chunk contains NER:
			subjectRepMen = determineBestContainingNerSubject(subjectChunkoffsets,listNerTypes);
//			if (containsOffsets(subjectRepMen,subjectHeadoffsets) || NerPerson.contains(subjectRepMen)){
//			subjectRepMen = "0-2";
//			System.out.println("subject chunk contains NER offsets " + subjectRepMen + " " +subjectHeadoffsets);
			
			if (containsOffsets(subjectRepMen,subjectHeadoffsets) || hds.NerPerson.contains(subjectRepMen)){
			 subjectRepMenText = hds.offsetToNer.get(subjectRepMen);
//			 System.out.println("subject chunk contains PER or subject head is in NER1 " + subjectRepMenText);
			}
//			else {
//			System.out.println("subject chunk contains PER or subject head is in NER2 " + 
//			subjectChunkText + " " + subjectRepMenText);
//			}
		}
		//subject repMen contains NER: too general: the enemies of Angela Merkel versus EZB's president, Christion Noyer
		//account for difference between attributive and of complement needs to be accounted for
//		if (subjectRepMenText == null && repMenOfSubjectHead != null){
//			subjectRepMen = determineBestContainingNerSubject(repMenOfSubjectHead,listNerTypes);
//			//repMen contains PER:
//			if (NerPerson.contains(subjectRepMen)){
//				 subjectRepMenText = offsetToNer.get(subjectRepMen);
//				 System.out.println("subject repMen equals PER  " + subjectRepMenText);
//			}
//			else{ 
//			  subjectRepMenText = offsetToRepMen.get(repMenOfSubjectHead);
//			  System.out.println("subject repMen equals PER  " + subjectRepMenText);
//			}
//			  System.out.println("subject repMen contains NER " + subjectRepMenText);
//		}
		//RepMen contains NER and subject doesn't
		if (subjectRepMenText == null && repMenOfSubjectHead != null){
			subjectRepMen = determineBestContainingNerSubject(repMenOfSubjectHead,listNerTypes);
			String subjectNer = determineBestContainingNerSubject(subjectChunkoffsets,listNerTypes);
//			System.out.println("TEST subjectRepMen " + subjectRepMen + " subjectNer " + subjectNer);
			//repMen contains PER:
//			System.out.println("SEARCH KEY " + subjectHeadoffsets + " ");
////			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//			for (HashMap.Entry<String, String> entry : hds.subjectToApposition.entrySet()) {
//			    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//			}
			
			
			if (
					subjectRepMen != null 
					&& hds.subjectToApposition.containsKey(subjectHeadoffsets) 
					&& containsOffsets(subjectRepMen, hds.subjectToApposition.get(subjectHeadoffsets))
					){
			
				subjectChunkText = hds.offsetToRepMen.get(repMenOfSubjectHead);
				subjectRepMenText = hds.offsetToNer.get(subjectRepMen);
//				System.out.println("APPO FOUND!! Representative Quotee " + subjectRepMenText + " quotee " + subjectChunkText);	
			}
			//repMen contains PER:
			else if (hds.Ner.contains(subjectRepMen) && subjectNer == null){
				
				 subjectRepMenText = hds.offsetToRepMen.get(repMenOfSubjectHead);
//				 System.out.println("subject repMen contains NER and subject doesn't " + subjectRepMenText);
			}
			else if (hds.NerPerson.contains(subjectRepMen) && !hds.NerPerson.contains(subjectNer)){ 
			  subjectRepMenText = hds.offsetToRepMen.get(repMenOfSubjectHead);
//			  System.out.println("subject repMen contains PER and subject doesn't   " + subjectRepMenText);
			}
			else if (hds.NerOrganization.contains(subjectRepMen) && !hds.NerOrganization.contains(subjectNer)){ 
				  subjectRepMenText = hds.offsetToRepMen.get(repMenOfSubjectHead);
//				  System.out.println("subject repMen contains ORG and subject doesn't   " + subjectRepMenText);
				}
//			else {
//			
////			  System.out.println("subject repMen contains NER and subject doesn't " + subjectRepMenText);
//			}
		}
		//get repMen of subjectHead : should be deleted? if there is no NER, the coreference mention is preferred
//		if (subjectRepMenText == null){
//			subjectRepMen = getOffsetsOfContainingAnnotation(subjectHeadoffsets,RepMen);
//			subjectRepMenText = offsetToRepMen.get(subjectRepMen);
//			System.out.println("Default RepMen " + subjectRepMenText);
//		}
		//subj chunk is repMen
		if (subjectRepMenText == null){
			subjectRepMen = subjectHeadoffsets;
			subjectRepMenText = subjectChunkText;
//			System.out.println("DEFAULT subjectChunkText " + subjectRepMenText);
		}
//	}
//		System.out.println("RETURN " + subjectChunkText + "/" + subjectRepMenText);
		result[0]= subjectChunkText;
		result[1] = subjectRepMenText;
	return result;
	}

	
	public boolean containsOffsets (String contains, String contained){
		
		int containsBegin, containsEnd, containedBegin, containedEnd;
		containsBegin = -1;
		containsEnd = -1;
		containedBegin = -1;
		containedEnd = -1;
		if (contains == null || contained == null){
			return false;
		}
		String[] elementsContains = contains.split("-");
		String[] elementsContained = contained.split("-");
		if (elementsContains.length >= 2 && elementsContained.length >= 2){
		try {
			containsBegin = Integer.parseInt(elementsContains[0]);
			containsEnd = Integer.parseInt(elementsContains[1]);
			containedBegin = Integer.parseInt(elementsContained[0]);
			containedEnd = Integer.parseInt(elementsContained[1]);
		} catch (NumberFormatException e) {
			
			return false;
		      //Will Throw exception!
		      //do something! anything to handle the exception.
			
		}
		if ((containsBegin <= containedBegin) && (containedEnd <= containsEnd)){
//			System.out.println("METH " + containsBegin + "<=" + containedBegin + " " + containedEnd + "<=" + containsEnd);
			return true;
		}
		}
		return false;
	}
	

	

	   public String [] determine_quotee_and_quote_relation(String leftRight, List<Chunk> chunks,
				QuoteAnnotator.HelperDataStructures hds,
				QuoteAnnotation quote
			   
			   ){

		   
		   String predicate = null;
		   String subject = null;
		   String subjectRepMenText = null;
		   String subjectChunkText = null;
		   String subjectChunkOffset = null;
		   String quotee = null;
		   String representative_quotee = null;
		   String quote_relation = null;
		   int quoteeReliability = -5;
		   String [] quote_annotation;
		   quote_annotation = new String[4];
		   quote_annotation[0]=quotee;
		   quote_annotation[1]=representative_quotee;
		   quote_annotation[2]=quote_relation;
		   quote_annotation[3] = "" + quoteeReliability;
		   Boolean getFirst;
		   if (leftRight == null){
			   getFirst = true;
		   }
		   else if (leftRight.equalsIgnoreCase("RIGHT")){
			   getFirst = true;
		   }
		   else if (leftRight.equalsIgnoreCase("LEFT")){
			   getFirst = false;
		   }
		   else {
			   getFirst = false;
		   }
		   
		   
		   
		   
		   for (Chunk aChunk : chunks) {
			   int beginOffset = aChunk.getBegin();
			   int endOffset = aChunk.getEnd();
			   String chunkOffset = "" + beginOffset + "-" + endOffset;
//			   System.out.println("CHUNK offset " +chunkOffset);
			  
			   
			   
			   subject = getOffsetsOfContainedAnnotation(chunkOffset,hds.SubjectHead); 
			   if (subject != null){
				   subjectChunkText = aChunk.getCoveredText();
				   subjectChunkOffset = chunkOffset;
				   
//				   for (HashMap.Entry<String, String> entry : hds.normalPredicateToSubject.entrySet()) {
//				   	if (subject.equals(entry.getValue())){
//				   		predicate = entry.getKey();
//				   	}
				   	//TO DO
//					    String key = entry.getKey();
//					    Object value = entry.getValue();
//					    System.out.println("OFFSET " + key + " LEMMA " + value);
//					    // FOR LOOP
//					}
	
//				   predicate = hds.subjectToPredicate.get(subject);
				   //problem subject with several predicates McCulloch explains pointing out
				   
				   if (hds.subjectToPredicate.get(subject) != null){
					   predicate = hds.subjectToPredicate.get(subject);
				   }
				   else {
				   predicate = hds.subjectToNormalPredicate.get(subject); //all predicates
				   }
				   
				   int beginSubj = -5;
				   int endSubj = -5;
				   if (subjectChunkOffset.matches(".+?-.+?")){
					   String [] elements = subjectChunkOffset.split("-");
						beginSubj = Integer.parseInt(elements[0]);
						endSubj = Integer.parseInt(elements[1]);
				   }
				   
//				   System.out.println("SUBJ CHUNK OFFSET " + subjectChunkOffset + " QUOTE " + quote.getBegin() + " - " + quote.getEnd());
				   
				   if (predicate != null) {
					   
					   //Werner Lensing, an MP with the conservative Christian Democrats (CDU-CSU) paints a similar picture: "On behalf of the CDU-CSU parliamentary group, I have to admit that for the moment, given the complexity of the problem, we have not adopted an official line yet."
					   //parser assigns adopt as GOV to nsubj Lensing
					   if (predicate.matches(".+?-.+?") && beginSubj != -5 && endSubj != -5) { 
							String [] elements = predicate.split("-");
							int beginPred = Integer.parseInt(elements[0]);
							int endPred = Integer.parseInt(elements[1]);
							
//							System.out.println ( "ENDQ " +  quote.getEnd()  + " BEGINS " + beginSubj );
				            if ((endPred < quote.getBegin() + 2 || quote.getEnd() - 2 < beginPred)
				            		&&
				            		// +2 -2 because some subj chunks contain quotation mark of quote plus blank
				            	(endSubj < quote.getBegin() + 2 || quote.getEnd() - 2  < beginSubj)	
				            		
				            		){
				   
//				   System.out.println("SUBJ CHUNK PRED " + aChunk.getCoveredText() + ":" + hds.offsetToLemma.get(predicate));
//				   System.out.println("SUBJ HEAD PRED " + hds.offsetToSubjectHead.get(subject) + ":" + hds.offsetToLemma.get(predicate));
//				   }  								   
//			   }
			   if (subjectChunkOffset != null && subject != null
					   && predicate != null && hds.offsetToLemmaOfOpinionExpression.get(predicate) != null
					   && hds.subjectToPredicate.get(subject) != null 
					   && hds.subjectToPredicate.get(subject).equals(predicate)
					   
					   ){
//		   			subjectRepMenText = determineBestRepMenSubject(
//		   					subject,subjectChunkOffset, subjectChunkText, hds);
		   			
		   			String result [] =  determineBestRepMenSubject(
		   					subject,subjectChunkOffset, subjectChunkText, hds);
			    	 if (result.length>=2){
			    		 subjectChunkText = result[0];
			    		 subjectRepMenText = result [1];
//			    		 System.out.println("RESULT1");
			    	 }
			   }
			   
//			   System.out.println("SUBJECTChunkText "  + subjectChunkText + 
//					   " subjectRepMenText " + subjectRepMenText + " predicate "+ hds.offsetToLemmaOfOpinionExpression.get(predicate));
			   
//			   for (HashMap.Entry<String, String> entry : hds.offsetToLemmaOfOpinionExpression.entrySet()) {
//				    String key = entry.getKey();
//				    Object value = entry.getValue();
//				    System.out.println("OFFSET " + key + " LEMMA " + value);
//				    // FOR LOOP
//				}
			   
			   if (subjectChunkText != null && subjectRepMenText != null 
					   && predicate != null && hds.offsetToLemmaOfOpinionExpression.get(predicate) != null
					   && hds.offsetToLemma.get(predicate) != null
					   ){
//				   System.out.println("NEW ANNOTATION  ");
				   //"This is really a success story," Kujat said.
				   //DELETE QUOTATION at beginning and end of quotee and representative quotee
				   
				   if (subjectChunkText.startsWith("\"")){
					   subjectChunkText = subjectChunkText.substring(1, subjectChunkText.length());
//					   System.out.println("QUOTATION MARK FOUND1 " + subjectChunkText );
				   }
				   if (subjectChunkText.endsWith("\"")){
					   subjectChunkText = subjectChunkText.substring(0, subjectChunkText.length()-1);
//					   System.out.println("QUOTATION MARK FOUND2 " + subjectChunkText );
				   }
				   if (subjectRepMenText.startsWith("\"")){
					   subjectRepMenText = subjectRepMenText.substring(1, subjectRepMenText.length());
//					   System.out.println("QUOTATION MARK FOUND3 " + subjectRepMenText );
				   }
				   if (subjectRepMenText.endsWith("\"")){
					   subjectRepMenText = subjectRepMenText.substring(0, subjectRepMenText.length()-1);
//					   System.out.println("QUOTATION MARK FOUND4 " + subjectRepMenText );
				   }
				   
				   subjectChunkText = subjectChunkText.trim();
				   subjectRepMenText = subjectRepMenText.trim();
				   
				   
				   quotee = subjectChunkText;
				   representative_quotee = subjectRepMenText;
				   quote_relation = hds.offsetToLemma.get(predicate);
				   quoteeReliability = 1;
				   quote_annotation[0]=quotee;
				   quote_annotation[1]=representative_quotee;
				   quote_annotation[2]=quote_relation;
				   quote_annotation[3] = "" + quoteeReliability;
				   
//				   if (hds.potentialIndirectQuoteTrigger.contains(quote_relation)){
//					   System.out.println("POTENTIAL INDIRECT QUOTE");
//					   
//					   hds.potentialIndirectQuote[0]=quotee;
//					   hds.potentialIndirectQuote[1]=representative_quotee;
//					   hds.potentialIndirectQuote[2]=quote_relation;
//					   hds.potentialIndirectQuote[3] = "" + quoteeReliability;
//					   //TO DO
//				   }
				   
				   if (getFirst){
//					   System.out.println("DIR " + leftRight + " QUOTEE "  + quote_annotation[0] + 
//							   " /representative_quotee " + quote_annotation[1] + " / "+ quote_annotation[2]);
				   return quote_annotation; //get last
				   }
			   }
			   //predicate not in list of opinion expressions: point out
			   //Zwickel has called the hours discrepancy between east and west a "fairness gap," but employers and many economists point out that it is ok.
			   
			   else 
				   if (subjectChunkText != null && subjectRepMenText != null 
					   && predicate != null)
				   {
//				   System.out.println("UNCLEAR last predicate not opinion verb");
				   quoteeReliability = -5;
				   quote_annotation[0]="<unclear>";
				   quote_annotation[1]="<unclear>";
				   quote_annotation[2]="<unclear>";
				   quote_annotation[3] = "" + quoteeReliability;
				   
				   if (getFirst){
//					   System.out.println("DIR " + leftRight + " QUOTEE "  + quote_annotation[0] + 
//							   " /representative_quotee " + quote_annotation[1] + " / "+ quote_annotation[2]);
				   return quote_annotation; //get last
				   }
				   
			   }
				            } // if match
				   } //if predicate outside quote
				   } //if predicate
			   } //if subject
			  
		   } // for chunks
//		   System.out.println("DIR " + leftRight + " QUOTEE "  + quote_annotation[0] + 
//				   " /representative_quotee " + quote_annotation[1] + " / "+ quote_annotation[2]);
		   return quote_annotation;
	   }
	   
	
	   
	   public void determineSubjectToVerbRelations( 
			   TypedDependency typedDependency, 
			   HashMap <String, String> offsetToLemmaOfOpinionExpression,
			   QuoteAnnotator.HelperDataStructures hds){

		   Dependency dep = new Dependency(); 
		    
		    dep = determineRelations(typedDependency, hds);
			
			
			String offsetVerbCand = dep.offsetGovernor;
			String verbCand = dep.governor;
//			System.out.println("VERBCAND IN" + verbCand);
			
			String offsetSubj = dep.offsetDependant;
			String subjCand = dep.dependant;
			String posSubj = dep.posDependant;
//			System.out.println("SUBJCAND IN" + subjCand);
				
				//Reuters reported Christian Noyer to have said
				//Reuters quoted Christian Noyer as saying
				if (hds.predicateToObject.containsKey(dep.offsetGovernor) 
						&& hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor) != null
						&& hds.objectEqui.contains(hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor))
						&& hds.offsetToLemmaOfOpinionExpression.get(dep.offsetDependant) != null
						
						){
//					System.out.println("PREDOBJ ");
//					delete subj-pred relation if objectEqui of Governor: Reuters reported Noyer to have said
					if (hds.objectEqui.contains(hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor))){
//						System.out.println("OBJEQUI ");
						if (hds.predicateToSubject.containsKey(dep.offsetGovernor)){
							String del = hds.predicateToSubject.get(dep.offsetGovernor);
							hds.predicateToSubject.remove(dep.offsetGovernor);
							hds.subjectToPredicate.remove(del);
							hds.normalPredicateToSubject.remove(dep.offsetGovernor);
							hds.subjectToNormalPredicate.remove(del);
//							System.out.println("REMOVE1 " + dep.governor + "/" + hds.offsetToLemma.get(del));
						}
					
//				
					
//					verbCand = "" + dep.governor + "/" + dep.dependant; //report/say
					verbCand = dep.dependant; //say
					offsetVerbCand = dep.offsetDependant;
					offsetSubj = hds.predicateToObject.get(dep.offsetGovernor);
					subjCand = hds.offsetToLemma.get(offsetSubj);
					dep.dependant = subjCand;
					dep.offsetDependant = offsetSubj;
//					dep.posDependant = 
					dep.governor = verbCand;
					dep.offsetGovernor = offsetVerbCand;
					}
					//Merkel promised to say
					//Obama is quoted as saying
					else if (hds.subjectEqui.contains(hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor))){
//						System.out.println("SUBJEQUI");
//						verbCand = "" + dep.governor + "/" + dep.dependant; //report/say
//						verbCand = dep.dependant; //say
						verbCand = hds.offsetToLemma.get(dep.offsetDependant);
						offsetVerbCand = dep.offsetDependant;
						offsetSubj = hds.predicateToSubject.get(dep.offsetGovernor);
						subjCand = hds.offsetToLemma.get(offsetSubj);
						dep.dependant = subjCand;
						dep.offsetDependant = offsetSubj;
						dep.governor = verbCand;
						dep.offsetGovernor = offsetVerbCand;
					}
					

					else {
//						System.out.println("NONE ");
						if (hds.predicateToSubject.containsKey(dep.offsetGovernor)){
							String del = hds.predicateToSubject.get(dep.offsetGovernor);
							hds.predicateToSubject.remove(dep.offsetGovernor);
							hds.subjectToPredicate.remove(del);
							hds.normalPredicateToSubject.remove(dep.offsetGovernor);
							hds.subjectToNormalPredicate.remove(del);
//							System.out.println("REMOVE2 " + dep.governor);
						}
						if (hds.predicateToSubject.containsKey(dep.offsetDependant)){
							String del = hds.predicateToSubject.get(dep.offsetDependant);
							hds.predicateToSubject.remove(dep.offsetDependant);
							hds.subjectToPredicate.remove(del);
							hds.normalPredicateToSubject.remove(dep.offsetDependant);
							hds.subjectToNormalPredicate.remove(del);
//							System.out.println("REMOVE3 " + dep.dependant);
						}
						return;
					}
//					System.out.println("OBJ-CHAIN-HEAD: " + subjCand + " " + verbCand);
//					System.out.println("OBJ-CHAIN-HEAD OFFSET: " + dep.offsetDependant + " " + dep.offsetGovernor);
				}
				//A respected fashion magazine in America even came out with a piece saying "celebrities were out and ordinary people were in" 
				//referring to the bravery of the firemen at Ground Zero.
				else if (hds.nounToInfinitiveVerb.containsKey(dep.offsetGovernor)
						&& !hds.normalPredicateToSubject.containsKey(dep.offsetDependant)
						){
//					System.out.println("NOUN INF ");
					verbCand = hds.offsetToLemma.get(dep.offsetDependant);
					offsetVerbCand = dep.offsetDependant;
					offsetSubj = dep.offsetGovernor;
					subjCand = hds.offsetToLemma.get(offsetSubj);
					dep.dependant = subjCand;
					dep.offsetDependant = offsetSubj;
					dep.governor = verbCand;
					dep.offsetGovernor = offsetVerbCand;
					
				}
				// Kuhnt stressed that RWE would now be giving priority to the task of integrating 
				//the new businesses into the group rather than acquiring new ones, 
				//though it would not rule out acquisitions altogether, particularly 
				//if they increased the group's position in central/eastern Europe and the United States.
				//Kuhnt stressed it rules
				else if (hds.mainToInfinitiveVerb.containsKey(dep.offsetGovernor)
						&& hds.normalPredicateToSubject.containsKey(dep.offsetGovernor)
						&& !hds.normalPredicateToSubject.containsKey(dep.offsetDependant)
					
						){
//					System.out.println("MAIN INF ");
					verbCand = hds.offsetToLemma.get(dep.offsetDependant);
					offsetVerbCand = dep.offsetDependant;
					offsetSubj = hds.normalPredicateToSubject.get(dep.offsetGovernor);
					subjCand = hds.offsetToLemma.get(offsetSubj);
					dep.dependant = subjCand;
					dep.offsetDependant = offsetSubj;
					dep.governor = verbCand;
					dep.offsetGovernor = offsetVerbCand;
					
			}
				//hack for correcting incorrect dependencies for English inverted subject:
				//inverted subject said Zwickel Monday morning
				//Zwickel is DO and morning is SUBJ
				//fires if sequence: DO SUBJ
				//if sequence SUBJ DO then when SUBJ found OpinionHolder determined without DO 
				else if (language.equals("en") && hds.predicateToObject.containsKey(offsetVerbCand)
						&& !hds.normalPredicateToSubject.containsKey(offsetVerbCand)
//						&& !hds.infinitiveToMainVerb.containsKey(offsetVerbCand) //tut nicht
						){
					
					offsetSubj = hds.predicateToObject.get(offsetVerbCand);
					subjCand = hds.offsetToLemma.get(offsetSubj);
					dep.dependant = subjCand;
					dep.offsetDependant = offsetSubj;
							
//					System.out.println("DOBJ SUBJ: " + dep.offsetDependant + " " + dep.dependant);
				}
				
				//Merkel was reported to have said tail have said
				else if (hds.predicateToSubjectChainHead.containsKey(dep.offsetGovernor)
//						&& offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor) != null 
						//combine active and passive: was quoted as saying
						&& hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor) != null 
						&& offsetToLemmaOfOpinionExpression.get(dep.offsetDependant) != null
						
						){
					if (hds.subjectEqui.contains(hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor))
							||hds.objectEqui.contains(hds.offsetToLemmaOfOpinionExpression.get(dep.offsetGovernor))
							){
//					System.out.println("SUBJCHAIN ");
//					verbCand = "" + dep.governor + "/" + dep.dependant; //report/say
					verbCand = hds.offsetToLemma.get(dep.offsetDependant); //say
					
					offsetVerbCand = dep.offsetDependant;
					offsetSubj = hds.predicateToSubjectChainHead.get(dep.offsetGovernor);
					subjCand = hds.offsetToLemma.get(offsetSubj);
					dep.dependant = subjCand;
					dep.offsetDependant = offsetSubj;
					dep.governor = verbCand;
					dep.offsetGovernor = offsetVerbCand;
					hds.offsetToLemmaOfOpinionExpression.remove(offsetVerbCand);
					hds.offsetToLemmaOfOpinionExpression.put(offsetVerbCand, verbCand);
					
//					System.out.println("SUBJ-CHAIN-HEAD: " + subjCand + " " + verbCand);
//					System.out.println("SUBJ-CHAIN-HEAD OFFSET: " + dep.offsetDependant + " " + dep.offsetGovernor);
					}
				}
				

				if (offsetToLemmaOfOpinionExpression.get(offsetVerbCand) != null){
//					System.out.println("STORE: " + dep.offsetDependant + " " + dep.offsetGovernor);
//					System.out.println("STORE: " + dep.dependant + " " + dep.governor);
					//NEW from determineOpinionHolderToOpinionVerbRelations:
					hds.offsetToOpinionExpressionToken.put(dep.offsetGovernor, dep.governor);
					storeRelations(dep, hds.predicateToSubject, hds.subjectToPredicate);
//					storeRelations(dep, govDepOpinionExpressionRelation, depGovOpinionExpressionRelation);
					
				}
				if (hds.infinitiveToMainVerb.containsKey(dep.offsetDependant) 
						&& hds.normalPredicateToSubject.containsKey(dep.offsetDependant)){
//					System.out.println("ALREADY IN2 ");
				}
				else {
				storeRelations(dep, hds.normalPredicateToSubject, hds.subjectToNormalPredicate);
//				System.out.println("STORE GENERAL: " + dep.dependant + " " + dep.governor);
				//NEW from determineOpinionHolderToOpinionVerbRelations:
				hds.offsetToSubjectHead.put(dep.offsetDependant,dep.dependant);
				hds.SubjectHead.add(dep.offsetDependant);
				if (dep.posDependant != null && hds.pronTag.contains(dep.posDependant)){
					hds.PronominalSubject.add(dep.offsetDependant);
//				 System.out.println("PRON SUBJ: " + subjCand + " " + offsetSubj);
				}
				}
//				else {
////				System.out.println("RES SUBJ: " + subjCand + " " + offsetSubj);
//				}
//				System.out.println("RES verbCand " + verbCand + " " + offsetVerbCand);
			
		   
	   } 
	   
	   public void storeRelations( 
			   TypedDependency typedDependency, 
			   HashMap <String, String> govDepRelation,
			   HashMap <String, String> depGovRelation,
			   QuoteAnnotator.HelperDataStructures hds
			   ){
		   
		   

		    Dependency dep = new Dependency(); 
		    
		    dep = determineRelations(typedDependency , hds);
			
		
			depGovRelation.put(dep.offsetDependant, dep.offsetGovernor);
			govDepRelation.put(dep.offsetGovernor, dep.offsetDependant);
	
//			System.out.println("STORE TypedDependency Dependant: " + dep.dependant + " " + dep.offsetDependant);				
//			System.out.println("STORE TypedDependency Governor: " + dep.governor + " " + dep.offsetGovernor);			
		   
	   }  
	   
	   public void storeRelations( 
			   Dependency dep, 
			   HashMap <String, String> govDepRelation,
			   HashMap <String, String> depGovRelation
			   ){
		
		   if (dep != null && dep.offsetDependant != null && dep.offsetGovernor != null){
			depGovRelation.put(dep.offsetDependant, dep.offsetGovernor);
			govDepRelation.put(dep.offsetGovernor, dep.offsetDependant);
//			System.out.println("STORE Dependency Dependant: " + dep.dependant + " " + dep.offsetDependant);				
//			System.out.println("STORE Dependency Governor: " + dep.governor + " " + dep.offsetGovernor);		
		   }
	   }  
	   
	   public Dependency determineRelations( 
			   TypedDependency typedDependency, 
			   QuoteAnnotator.HelperDataStructures hds
			   ){
		   
		    Dependency dep = new Dependency();
		    
			dep.governor = typedDependency.gov().toString().toLowerCase();
			int beginGovernor = typedDependency.gov().beginPosition();
			int endGovernor = typedDependency.gov().endPosition();
			dep.offsetGovernor = "" + beginGovernor + "-" + endGovernor;
			
			if (dep.governor.matches(".+?/.+?")) { 
				String [] elements = dep.governor.split("/");
				dep.governor = elements[0];
				dep.posGovernor = elements[1];
				int beginDependant = typedDependency.dep().beginPosition();
				int endDependant = typedDependency.dep().endPosition();
				dep.offsetDependant = "" + beginDependant + "-" + endDependant;
				dep.dependant = typedDependency.dep().toString().toLowerCase();
				
				if (dep.dependant.matches(".+?/.+?")) {          
					elements = dep.dependant.split("/");
					dep.dependant = elements[0];
					dep.posDependant = elements[1];
					}
			}
//			System.out.println("Dependant: " + dep.dependant + " " + dep.offsetDependant);				
//			System.out.println("Governor: " + dep.governor + " " + dep.offsetGovernor);
			
//			hds.offsetToLemma.put(dep.offsetDependant, dep.dependant);
//			hds.offsetToLemma.put(dep.offsetGovernor, dep.governor);
			return dep;
		   
	   }  
	   
	   

}


