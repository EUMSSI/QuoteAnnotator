
/* First created by JCasGen Wed Oct 28 14:17:54 CET 2015 */
package com.iai.uima.jcas.tcas;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Mar 23 13:19:32 CET 2016
 * @generated */
public class QuoteAnnotation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (QuoteAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = QuoteAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new QuoteAnnotation(addr, QuoteAnnotation_Type.this);
  			   QuoteAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new QuoteAnnotation(addr, QuoteAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = QuoteAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.iai.uima.jcas.tcas.QuoteAnnotation");
 
  /** @generated */
  final Feature casFeat_Quotee;
  /** @generated */
  final int     casFeatCode_Quotee;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuotee(int addr) {
        if (featOkTst && casFeat_Quotee == null)
      jcas.throwFeatMissing("Quotee", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Quotee);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuotee(int addr, String v) {
        if (featOkTst && casFeat_Quotee == null)
      jcas.throwFeatMissing("Quotee", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_Quotee, v);}
    
  
 
  /** @generated */
  final Feature casFeat_QuoteRelation;
  /** @generated */
  final int     casFeatCode_QuoteRelation;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuoteRelation(int addr) {
        if (featOkTst && casFeat_QuoteRelation == null)
      jcas.throwFeatMissing("QuoteRelation", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_QuoteRelation);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuoteRelation(int addr, String v) {
        if (featOkTst && casFeat_QuoteRelation == null)
      jcas.throwFeatMissing("QuoteRelation", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_QuoteRelation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_QuoteType;
  /** @generated */
  final int     casFeatCode_QuoteType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuoteType(int addr) {
        if (featOkTst && casFeat_QuoteType == null)
      jcas.throwFeatMissing("QuoteType", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_QuoteType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuoteType(int addr, String v) {
        if (featOkTst && casFeat_QuoteType == null)
      jcas.throwFeatMissing("QuoteType", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_QuoteType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_QuoteeReliability;
  /** @generated */
  final int     casFeatCode_QuoteeReliability;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getQuoteeReliability(int addr) {
        if (featOkTst && casFeat_QuoteeReliability == null)
      jcas.throwFeatMissing("QuoteeReliability", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_QuoteeReliability);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuoteeReliability(int addr, int v) {
        if (featOkTst && casFeat_QuoteeReliability == null)
      jcas.throwFeatMissing("QuoteeReliability", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_QuoteeReliability, v);}
    
  
 
  /** @generated */
  final Feature casFeat_RepresentativeQuoteMention;
  /** @generated */
  final int     casFeatCode_RepresentativeQuoteMention;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRepresentativeQuoteMention(int addr) {
        if (featOkTst && casFeat_RepresentativeQuoteMention == null)
      jcas.throwFeatMissing("RepresentativeQuoteMention", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_RepresentativeQuoteMention);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRepresentativeQuoteMention(int addr, String v) {
        if (featOkTst && casFeat_RepresentativeQuoteMention == null)
      jcas.throwFeatMissing("RepresentativeQuoteMention", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_RepresentativeQuoteMention, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public QuoteAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Quotee = jcas.getRequiredFeatureDE(casType, "Quotee", "uima.cas.String", featOkTst);
    casFeatCode_Quotee  = (null == casFeat_Quotee) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Quotee).getCode();

 
    casFeat_QuoteRelation = jcas.getRequiredFeatureDE(casType, "QuoteRelation", "uima.cas.String", featOkTst);
    casFeatCode_QuoteRelation  = (null == casFeat_QuoteRelation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_QuoteRelation).getCode();

 
    casFeat_QuoteType = jcas.getRequiredFeatureDE(casType, "QuoteType", "uima.cas.String", featOkTst);
    casFeatCode_QuoteType  = (null == casFeat_QuoteType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_QuoteType).getCode();

 
    casFeat_QuoteeReliability = jcas.getRequiredFeatureDE(casType, "QuoteeReliability", "uima.cas.Integer", featOkTst);
    casFeatCode_QuoteeReliability  = (null == casFeat_QuoteeReliability) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_QuoteeReliability).getCode();

 
    casFeat_RepresentativeQuoteMention = jcas.getRequiredFeatureDE(casType, "RepresentativeQuoteMention", "uima.cas.String", featOkTst);
    casFeatCode_RepresentativeQuoteMention  = (null == casFeat_RepresentativeQuoteMention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_RepresentativeQuoteMention).getCode();

  }
}



    