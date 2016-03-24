package com.iai.uima.analysis_component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
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

import com.iai.uima.jcas.tcas.IaiCorefAnnotation;
import com.iai.uima.jcas.tcas.QuoteAnnotation;
import com.iai.uima.jcas.tcas.SentenceAnnotation;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.QuotationsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class QuoteAnnotator extends JCasAnnotator_ImplBase {

	StanfordCoreNLP pipeline;

	public static final String PARAM_LANGUAGE = "language";
	@ConfigurationParameter(name = PARAM_OPINION_NOUNS, defaultValue = "en")
	private String language;

	public static final String PARAM_OPINION_NOUNS = "opinionNouns";
	@ConfigurationParameter(name = PARAM_OPINION_NOUNS, defaultValue = "/maps/opinion_nouns")
	private String opinionNouns;

	public static final String PARAM_OPINION_VERBS = "opinionVerbs";
	@ConfigurationParameter(name = PARAM_OPINION_VERBS, defaultValue = "/maps/opinion_verbs")
	private String opinionVerbs;

	Collection<String> opinion_nouns;
	Collection<String> opinion_verbs;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {

		super.initialize(aContext);

		Properties props = new Properties();
//		props.setProperty("annotators", "tokenize, ssplit, quote");
//		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref"); //mention gibt's nicht
		props.setProperty("annotators", "tokenize,ssplit,quote,pos,lemma,ner,parse,dcoref");
		pipeline = new StanfordCoreNLP(props);

//		SimplePipeline.runPipeline(reader, segmenter, lemma, pos1, chunk, ner, quote, xmiWriter);
		
		
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
		
		try {
			while ((line = br_verbs.readLine()) != null) {
				opinion_verbs.add(line);
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
		Map<Integer, edu.stanford.nlp.dcoref.CorefChain> corefChains = document.get(CorefChainAnnotation.class);
//		edu.stanford.nlp.dcoref.CorefChain.CorefMention RepresentativeMention = coref
//		 System.out.println("---");
//		    System.out.println("coref chains");
		    if (corefChains == null) { return; }
		      for (Entry<Integer, edu.stanford.nlp.dcoref.CorefChain> entry: corefChains.entrySet()) {
//		        System.out.println("Chain " + entry.getKey() + " ");
		        for (CorefMention m : entry.getValue().getMentionsInTextualOrder()) {

//		                

				// We need to subtract one since the indices count from 1 but the Lists start from 0
		        	List<CoreLabel> tokens = sentences.get(m.sentNum - 1).get(TokensAnnotation.class);
		          // We subtract two for end: one for 0-based indexing, and one because we want last token of mention not one following.
//		          System.out.println("  " + m + ", i.e., 0-based character offsets [" + tokens.get(m.startIndex - 1).beginPosition() +
//		                  ", " + tokens.get(m.endIndex - 2).endPosition() + ")");

		          CorefMention RepresentativeMention = entry.getValue().getRepresentativeMention();
		          String repMen = RepresentativeMention.mentionSpan;
//		          List<CoreLabel> repMenTokens = sentences.get(RepresentativeMention.sentNum - 1).get(TokensAnnotation.class);
//		          System.out.println("  " + "\"" + repMen + "\"" + " is representative mention" +
//                          ", i.e., 0-based character offsets [" + repMenTokens.get(RepresentativeMention.startIndex - 1).beginPosition() +
//		                  ", " + repMenTokens.get(RepresentativeMention.endIndex - 2).endPosition() + ")");
 	              IaiCorefAnnotation corefAnnotation = new IaiCorefAnnotation(aJCas);
		            String corefMention  = m.mentionSpan;

					int beginCoref = tokens.get(m.startIndex - 1).beginPosition();
					int endCoref = tokens.get(m.endIndex - 2).endPosition();
					int chain = entry.getKey();
					corefAnnotation.setBegin(beginCoref);
					corefAnnotation.setEnd(endCoref);
					corefAnnotation.setCorefMention(corefMention);
					corefAnnotation.setCorefChain(chain);
					corefAnnotation.setRepresentativeMention(repMen);
					corefAnnotation.addToIndexes();    
		        }
		      }
		    
		   
//	 }
		 
		
//		      System.out.println("NOW quote finder");

		
		///* quote finder: begin find quote relation and quotee
		
		
		
		String quotee_left = null;
		String quotee_right = null; 
		
		String quote_relation_left = null;
		String quote_relation_right = null;
		
		String quoteType = null;
		int quoteeReliability = 5;
		int quotee_end = -5;
		String that = "that";
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
				
				quote_relation_left = null;
				quote_relation_right = null;
				quoteeReliability = 5;
				quotee_end = -5;
				quoteType = "direct";
				quoteeBeforeQuote = false;
				
				
				
				List<Token> containedTokens = JCasUtil.selectCovered(aJCas,
						Token.class, annotation);
				

				
				List<Token> followTokens = JCasUtil.selectFollowing(aJCas,
						Token.class, annotation, 1);
				
                for (Token aFollowToken: followTokens){
				   List<Chunk> chunks = JCasUtil.selectCovering(aJCas,
				Chunk.class, aFollowToken);
//                }
				
//				List<Chunk> chunks = JCasUtil.selectFollowing(aJCas,
//						Chunk.class, annotation, 1);

				for (Chunk aChunk : chunks) {
					if (aChunk.getChunkValue().equals("NP")) {
						List<NamedEntity> nes = JCasUtil.selectCovered(aJCas,
								NamedEntity.class, aChunk);
						for (NamedEntity ne : nes) {
							List<Chunk> chunks2 = JCasUtil.selectFollowing(
									aJCas, Chunk.class, aChunk, 1);

							for (Chunk aChunk2 : chunks2) {
								if (aChunk2.getChunkValue().equals("VP")) {
									List<Token> tokens = JCasUtil
											.selectCovered(aJCas, Token.class,
													aChunk2);
									for (Token aToken : tokens) {
										if (opinion_verbs.contains(aToken
												.getLemma().getValue())){
											quotee_right = ne
													.getCoveredText();
							  				quote_relation_right = aToken.getLemma()
													.getValue();
							  				quoteeReliability = 1;
										}
							 		}
								}
							}
						}
					}
					
					else if (aChunk.getChunkValue().equals("VP")) {
						List<Token> tokens = JCasUtil.selectCovered(aJCas,
								Token.class, aChunk);
						for (Token aToken : tokens)
							if (opinion_verbs.contains(aToken.getLemma()
									.getValue())) {
								List<Chunk> chunks2 = JCasUtil.selectFollowing(
										aJCas, Chunk.class, aChunk, 1);
				 				for (Chunk aChunk2 : chunks2) {
									if (aChunk2.getChunkValue().equals("NP")) {
										List<NamedEntity> nes = JCasUtil
												.selectCovered(aJCas,
														NamedEntity.class,
														aChunk2);
										for (NamedEntity ne : nes){
											quotee_right = ne
													.getCoveredText();
											quote_relation_right = aToken.getLemma()
													.getValue();
											quoteeReliability = 1;
										}
									}
								}
							}
						
					} 
				}
                }
				List<Token> precedingTokens = JCasUtil.selectPreceding(aJCas,
						Token.class, annotation, 1);
                for (Token aPrecedingToken: precedingTokens){          	
                	if (aPrecedingToken.getLemma().getValue().equals(":") || aPrecedingToken.getLemma().getValue().equals("that")) {
//                		System.out.println("Hello, World lemma found" + aPrecedingToken.getLemma().getValue());
                		quoteeBeforeQuote = true;
                		List <NamedEntity> namedEntities = null;
                		List <Token> tokens = null;
                		
				        List<Sentence> precedingSentences = JCasUtil.selectPreceding(aJCas,
				        		Sentence.class, aPrecedingToken, 1);
				        
						if (precedingSentences.isEmpty()){
							List<Sentence> firstSentence;
				        	firstSentence = JCasUtil.selectCovering(aJCas,
				        		Sentence.class, aPrecedingToken);

				        	for (Sentence aSentence: firstSentence){
				        		namedEntities = JCasUtil.selectCovered(aJCas,
	                	    		NamedEntity.class, aSentence.getBegin(), aPrecedingToken.getEnd());
				        		tokens = JCasUtil.selectCovered(aJCas,
				        				Token.class, aSentence.getBegin(), aPrecedingToken.getEnd());
				        	
				        	}
						}
				        else {	
				        	for (Sentence aSentence: precedingSentences){
//				        		System.out.println("Hello, World sentence" + aSentence);
				        		namedEntities = JCasUtil.selectBetween(aJCas,
				        				NamedEntity.class, aSentence, aPrecedingToken);
				        		tokens = JCasUtil.selectBetween(aJCas,
				        				Token.class, aSentence, aPrecedingToken);
				        	}
				        }
                	
//				        if (namedEntities == null){
//				        	
//				        }
				        if (namedEntities.size() == 1){
                	    	for (NamedEntity ne : namedEntities){
//                	    		System.out.println("Hello, World ONE NER" + ne.getValue());
							quotee_left = ne.getCoveredText();
							quotee_end = ne.getEnd();
							quoteeReliability = 1;
							
                	        }
                	    }
                	    else if (namedEntities.size() > 1) {
                	    	int count = 0;
                	    	String quotee_cand = null;
//                	    	System.out.println("Hello, World ELSE SEVERAL NER");
                  	    	for (NamedEntity ner : namedEntities){
//                  	    		System.out.println("Hello, World NER TYPE" + ner.getValue());
                  	    		if (ner.getValue().equals("PERSON")){
                  	    			count = count + 1;
                  	    			quotee_cand = ner.getCoveredText();
                  	    			quotee_end = ner.getEnd();
//                  	    			System.out.println("Hello, World FOUND PERSON" + quotee_cand);
                  	    		}
                	    	}
                  	    	if (count == 1){      // there is exactly one NER.PERSON
//                  	    		System.out.println("Hello, World ONE PERSON" + quotee_cand);
    							quotee_left = quotee_cand;
    						
    							quoteeReliability = 3;
                  	    	}
                	    }
				        
                	    if (quotee_left != null){
                	    	for (Token aToken : tokens){
                	    		if (opinion_verbs.contains(aToken.getLemma()
									.getValue())){
                	    			quote_relation_left = aToken.getLemma()
										.getValue();
//                	    			System.out.println("Hello, World ADJACENCY" + quote_relation_left + quotee_end + aToken.getEnd());
                	    				if ((quotee_end + 1) == aToken.getBegin()){
//                	    					System.out.println("Hello, World ADJACENCY" );
                	    					quoteeReliability = 1;
                	    				}
                	    		}
                	    	}
                	    	if (aPrecedingToken.getLemma().getValue().equals(":") && quote_relation_left == null){
                	    		quote_relation_left = ":";
                	    	}
				        }	
                	}
                }
		
                

				
				if (quotee_left != null && quotee_right != null){
//					System.out.println("Hello, World TWO QUOTEES");
					
					if (containedTokens.get(containedTokens.size() - 2).getLemma().getValue().equals(".") ){
//						System.out.println("Hello, World PUNCT" + quotee_left + quote_relation_left + quoteeReliability);
						annotation.setQuotee(quotee_left);
						annotation.setQuoteRelation(quote_relation_left);
						annotation.setQuoteType(quoteType);
						annotation.setQuoteeReliability(quoteeReliability);
						annotation.setRepresentativeQuoteeMention(quotee_left);
					}
					else if (containedTokens.get(containedTokens.size() - 2).getLemma().getValue().equals(",")){
//						System.out.println("Hello, World COMMA" + quotee_right + quote_relation_right + quoteeReliability);
						annotation.setQuotee(quotee_right);
						annotation.setQuoteRelation(quote_relation_right);
						annotation.setQuoteType(quoteType);
						annotation.setQuoteeReliability(quoteeReliability);
						annotation.setRepresentativeQuoteeMention(quotee_right);
					}
					else {
//						System.out.println("Hello, World UNCLEAR" + quotee_left + quote_relation_left + quote + quotee_right + quote_relation_right);
					annotation.setQuotee("<unclear>");
					annotation.setQuoteRelation("<none>");
					annotation.setQuoteType(quoteType);
					}
				}
				else if (quoteeBeforeQuote == true){
					annotation.setQuotee(quotee_left);
					annotation.setQuoteRelation(quote_relation_left);
					annotation.setQuoteType(quoteType);
					annotation.setQuoteeReliability(quoteeReliability);
					annotation.setRepresentativeQuoteeMention(quotee_left);
				}
				else if (quotee_left != null){
//					System.out.println("Hello, World quotee_left" + quotee_left + quote_relation_left + quoteeReliability);
					annotation.setQuotee(quotee_left);
					annotation.setQuoteRelation(quote_relation_left);
					annotation.setQuoteType(quoteType);
					annotation.setQuoteeReliability(quoteeReliability);
					annotation.setRepresentativeQuoteeMention(quotee_left);
				}
				else if (quotee_right != null){
//					System.out.println("Hello, World quotee_right" + quotee_right + quote_relation_right + quoteeReliability);
					annotation.setQuotee(quotee_right);
					annotation.setQuoteRelation(quote_relation_right);
					annotation.setQuoteType(quoteType);
					annotation.setQuoteeReliability(quoteeReliability);
					annotation.setRepresentativeQuoteeMention(quotee_right);
				}
				else if (quote_relation_left != null ){
					annotation.setQuoteRelation(quote_relation_left);
					annotation.setQuoteType(quoteType);
//					System.out.println("Hello, World NO QUOTEE FOUND" + quote + quote_relation_left + quote_relation_right);
				}
				else if (quote_relation_right != null){
					annotation.setQuoteRelation(quote_relation_right);
					annotation.setQuoteType(quoteType);
				}
				else if (quoteType != null){
					annotation.setQuoteType(quoteType);
//					System.out.println("Hello, World NO QUOTEE and NO QUOTE RELATION FOUND" + quote);
				}
			}
			
			
		} //for direct quote
		
		// annotate indirect quotes: opinion verb + 'that' ... until end of sentence: said that ...

//		List<CoreMap> sentences = document.get(SentencesAnnotation.class); //already instantiated above
		for (CoreMap sentence : sentences) {
//			if (sentence.get(TokensAnnotation.class).size() > 5) { 
				SentenceAnnotation sentenceAnn = new SentenceAnnotation(aJCas);
				
				int beginSentence = sentence.get(TokensAnnotation.class).get(0)
						.beginPosition();
				int endSentence = sentence.get(TokensAnnotation.class)
						.get(sentence.get(TokensAnnotation.class).size() - 1)
						.endPosition();
				sentenceAnn.setBegin(beginSentence);
				sentenceAnn.setEnd(endSentence);
				sentenceAnn.addToIndexes();
				
				List<Chunk> chunksIQ = JCasUtil.selectCovered(aJCas,
				Chunk.class, sentenceAnn);
				for (Chunk aChunk : chunksIQ) {
					List<Token> tokensOpinion = JCasUtil.selectCovered(aJCas,
							Token.class, aChunk);
					for (Token aTokenOpinion : tokensOpinion){
						if (aTokenOpinion.getLemma().getCoveredText().equals("say")){
							//test for sentence xxx
//							if ()
							
						}
					}
					
					
					
					if (aChunk.getChunkValue().equals("SBAR")) {
						List<Token> tokensSbar = JCasUtil.selectCovered(aJCas,
								Token.class, aChunk);
						
						
						for (Token aTokenSbar : tokensSbar){
							
						if (aTokenSbar.getLemma().getCoveredText().equals(that)){
						// VP test: does that clause contain VP?
					
			        	QuoteAnnotation indirectQuote = new QuoteAnnotation(aJCas);
			        	int indirectQuoteBegin = aChunk.getEnd() + 1;
						int indirectQuoteEnd = sentenceAnn.getEnd();
						indirectQuote.setBegin(indirectQuoteBegin);
						indirectQuote.setEnd(indirectQuoteEnd);
						List<QuoteAnnotation> coveringDirectQuote = JCasUtil.selectCovering(aJCas,
								QuoteAnnotation.class, indirectQuote); 
//						System.out.println("Hello, World covering Quote" + coveringDirectQuote);
						if (coveringDirectQuote.isEmpty()){
//							System.out.println("Hello, World indirect quote");
						indirectQuote.addToIndexes();
						}
						List<QuoteAnnotation> coveredDirectQuotes = JCasUtil.selectCovered(aJCas,
								QuoteAnnotation.class, indirectQuote);
						for (QuoteAnnotation coveredDirectQuote : coveredDirectQuotes){
//							System.out.println("Hello, World covered direct quote" + coveredDirectQuote.getCoveredText());
							//delete coveredDirectQuoteIndex
							coveredDirectQuote.removeFromIndexes();
						}
						
						
//						Re-initialize markup variables since they are also used for direct quotes
						quotee_left = null;
						quotee_right = null; 
						
						quote_relation_left = null;
						quote_relation_right = null;
						
						quoteType = "indirect";
						quoteeReliability = 5;
						quotee_end = -5;
						
						
						
						
						
						List<Chunk> precedingChunks = JCasUtil.selectPreceding(aJCas,
								Chunk.class, aChunk, 1);
						for (Chunk aPrecedingChunk : precedingChunks) {
						   List<Token> tokens = JCasUtil.selectCovered(aJCas,
								Token.class, aPrecedingChunk);
						   for (Token aToken : tokens){
							   if (opinion_verbs.contains(aToken.getLemma()
										.getValue()) || opinion_nouns.contains(aToken.getLemma()
												.getValue())){
	                	    			quote_relation_left = aToken.getLemma()
											.getValue();
	                	   
							   }
						   }
//						}
						   if (quote_relation_left != null){
							List<Chunk> precedingNerChunks = JCasUtil.selectPreceding(aJCas,
									Chunk.class, aPrecedingChunk, 1);
							List<NamedEntity> nersIndirect = null;
							for (Chunk aPrecedingNerChunk : precedingNerChunks) {
							    nersIndirect = JCasUtil.selectCovered(aJCas,
										NamedEntity.class, aPrecedingNerChunk);
								for (NamedEntity ner : nersIndirect) {
									quotee_left = ner
											.getCoveredText();
									quoteeReliability = 1;
								}
							}
							if (quotee_left == null){
								//search quotee in full preceding sentence
								nersIndirect = null; 
//								System.out.println("Hello, World search NER");
								List<Sentence> precedingSentences = JCasUtil.selectPreceding(aJCas,
							        		Sentence.class, aPrecedingChunk, 1);
							        
									if (precedingSentences.isEmpty()){
										List<Sentence> firstSentence;
//										System.out.println("Hello, World no preceding sentence");
							        	firstSentence = JCasUtil.selectCovering(aJCas,
							        		Sentence.class, aPrecedingChunk);

							        	for (Sentence aSentence: firstSentence){
							        		 nersIndirect= JCasUtil.selectCovered(aJCas,
				                	    		NamedEntity.class, aSentence.getBegin(), aPrecedingChunk.getEnd());
							        		tokens = JCasUtil.selectCovered(aJCas,
							        				Token.class, aSentence.getBegin(), aPrecedingChunk.getEnd());
//							        		System.out.println("Hello, World NERS1" + nersIndirect);
							        	
							        	}
									}
							        else {	
							        	for (Sentence aSentence: precedingSentences){
//							        		System.out.println("Hello, World sentence" + aSentence);
							        		nersIndirect = JCasUtil.selectBetween(aJCas,
							        				NamedEntity.class, aSentence, aPrecedingChunk);
							        		tokens = JCasUtil.selectBetween(aJCas,
							        				Token.class, aSentence, aPrecedingChunk);
//							        		System.out.println("Hello, World NERS2" + ners);
							        	}
							        }
//							        	if (nersIndirect == null){
//							        		
//							        	}
							        	if (nersIndirect.size() == 1){
				                	    	for (NamedEntity ne : nersIndirect){
//				                	    		System.out.println("Hello, World ONE NER" + ne.getValue());
											quotee_left = ne.getCoveredText();
											quotee_end = ne.getEnd();
											quoteeReliability = 1;
											
				                	        }
				                	    }
				                	    else if (nersIndirect.size() > 1) {
				                	    	int count = 0;
				                	    	String quotee_cand = null;
//				                	    	System.out.println("Hello, World ELSE SEVERAL NER");
				                  	    	for (NamedEntity ner : nersIndirect){
//				                  	    		System.out.println("Hello, World NER TYPE" + ner.getValue());
				                  	    		if (ner.getValue().equals("PERSON")){
				                  	    			count = count + 1;
				                  	    			quotee_cand = ner.getCoveredText();
				                  	    			quotee_end = ner.getEnd();
//				                  	    			System.out.println("Hello, World FOUND PERSON" + quotee_cand);
				                  	    		}
				                	    	}
				                  	    	if (count == 1){      // there is exactly one NER.PERSON
//				                  	    		System.out.println("Hello, World ONE PERSON" + quotee_cand);
				    							quotee_left = quotee_cand;
				    						
				    							quoteeReliability = 3;
				                  	    	}
				                	    }
//							        }
								
								
								
								
								
								
								
							}
						   } //if quotee_left == null
						}	//if quote_relation_left != null
						if (quotee_left != null){
							indirectQuote.setQuotee(quotee_left);
							indirectQuote.setQuoteRelation(quote_relation_left);
							indirectQuote.setQuoteType(quoteType);
							indirectQuote.setQuoteeReliability(quoteeReliability);
							indirectQuote.setRepresentativeQuoteeMention(quotee_left);
//							System.out.println("Hello, World INDIRECT QUOTE" + quotee_left + quote_relation_left + quoteeReliability);
						}
						else if (quote_relation_left != null){
							indirectQuote.setQuoteRelation(quote_relation_left);
							indirectQuote.setQuoteType(quoteType);
						}
						
						else if (quoteType != null){
							indirectQuote.setQuoteType(quoteType);
//							System.out.println("Hello, World INDIRECT QUOTE NOT FOUND" + quote_relation_left);
						}
						}
				}
			  }
			}
//				say without that
//			}		
		} //Core map sentences indirect quotes
		
		
		
		
		
	}
	// quote finder: end find quote relation and quotee */

}


