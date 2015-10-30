package com.iai.uima.analysis_component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.iai.uima.jcas.tcas.QuoteAnnotation;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import edu.stanford.nlp.ling.CoreAnnotations.QuotationsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
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
		props.setProperty("annotators", "tokenize, quote");
		pipeline = new StanfordCoreNLP(props);

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

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the quotes in this document
		List<CoreMap> quotes = document.get(QuotationsAnnotation.class);
		for (CoreMap quote : quotes) {
			if (quote.get(TokensAnnotation.class).size() > 5) {
				QuoteAnnotation annotation = new QuoteAnnotation(aJCas);

				int begin = quote.get(TokensAnnotation.class).get(0)
						.beginPosition();
				int end = quote.get(TokensAnnotation.class)
						.get(quote.get(TokensAnnotation.class).size() - 1)
						.endPosition();
				annotation.setBegin(begin);
				annotation.setEnd(end);
				annotation.addToIndexes();
				
				String quotee_left = null;
				String quotee_right = null;
				
				String quote_relation_left = null;
				String quote_relation_right = null;
				
				List<Chunk> chunks = JCasUtil.selectFollowing(aJCas,
						Chunk.class, annotation, 1);

				for (Chunk aChunk : chunks) {
					if (aChunk.getChunkValue().equals("VP")) {
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
										}
									}
								}
							}
					} else if (aChunk.getChunkValue().equals("NP")) {
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
										}
									}

								}
							}
						}
					}
				}
				
				chunks = JCasUtil.selectPreceding(aJCas,
						Chunk.class, annotation, 1);
				
				for (Chunk aChunk : chunks) {
					if (aChunk.getChunkValue().equals("VP")) {
						List<Token> tokens = JCasUtil.selectCovered(aJCas,
								Token.class, aChunk);
						for (Token aToken : tokens)
							if (opinion_verbs.contains(aToken.getLemma()
									.getValue())) {
								List<Chunk> chunks2 = JCasUtil.selectPreceding(
										aJCas, Chunk.class, aChunk, 1);
								for (Chunk aChunk2 : chunks2) {
									if (aChunk2.getChunkValue().equals("NP")) {
										List<NamedEntity> nes = JCasUtil
												.selectCovered(aJCas,
														NamedEntity.class,
														aChunk2);
										for (NamedEntity ne : nes){
											quotee_left = ne
													.getCoveredText();
											quote_relation_left = aToken.getLemma()
													.getValue();
										}
									}
								}
							}
					} else if (aChunk.getChunkValue().equals("NP")) {
						List<NamedEntity> nes = JCasUtil.selectCovered(aJCas,
								NamedEntity.class, aChunk);
						for (NamedEntity ne : nes) {
							List<Chunk> chunks2 = JCasUtil.selectPreceding(
									aJCas, Chunk.class, aChunk, 1);

							for (Chunk aChunk2 : chunks2) {
								if (aChunk2.getChunkValue().equals("VP")) {
									List<Token> tokens = JCasUtil
											.selectCovered(aJCas, Token.class,
													aChunk2);
									for (Token aToken : tokens) {
										if (opinion_verbs.contains(aToken
												.getLemma().getValue())){
											quotee_left = ne
													.getCoveredText();
											quote_relation_left = aToken.getLemma()
													.getValue();
										}
									}

								}
							}
						}
					}
				}
				
				if (quotee_left != null && quotee_right != null){
					annotation.setQuotee("<unclear>");
					annotation.setQuoteRelation("<none>");
				}
				else if (quotee_left != null){
					annotation.setQuotee(quotee_left);
					annotation.setQuoteRelation(quote_relation_left);
				}
				else {
					annotation.setQuotee(quotee_right);
					annotation.setQuoteRelation(quote_relation_right);
				}
			}
			
			
		}
	}

}
