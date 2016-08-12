
/* First created by JCasGen Wed Mar 09 12:17:59 CET 2016 */
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
 * Updated by JCasGen Fri Aug 12 16:04:19 CEST 2016
 * @generated */
public class IaiCorefAnnotation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (IaiCorefAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = IaiCorefAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new IaiCorefAnnotation(addr, IaiCorefAnnotation_Type.this);
  			   IaiCorefAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new IaiCorefAnnotation(addr, IaiCorefAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = IaiCorefAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("com.iai.uima.jcas.tcas.IaiCorefAnnotation");
 
  /** @generated */
  final Feature casFeat_CorefMention;
  /** @generated */
  final int     casFeatCode_CorefMention;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCorefMention(int addr) {
        if (featOkTst && casFeat_CorefMention == null)
      jcas.throwFeatMissing("CorefMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_CorefMention);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCorefMention(int addr, String v) {
        if (featOkTst && casFeat_CorefMention == null)
      jcas.throwFeatMissing("CorefMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_CorefMention, v);}
    
  
 
  /** @generated */
  final Feature casFeat_CorefChain;
  /** @generated */
  final int     casFeatCode_CorefChain;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getCorefChain(int addr) {
        if (featOkTst && casFeat_CorefChain == null)
      jcas.throwFeatMissing("CorefChain", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_CorefChain);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCorefChain(int addr, int v) {
        if (featOkTst && casFeat_CorefChain == null)
      jcas.throwFeatMissing("CorefChain", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_CorefChain, v);}
    
  
 
  /** @generated */
  final Feature casFeat_RepresentativeMention;
  /** @generated */
  final int     casFeatCode_RepresentativeMention;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRepresentativeMention(int addr) {
        if (featOkTst && casFeat_RepresentativeMention == null)
      jcas.throwFeatMissing("RepresentativeMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_RepresentativeMention);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRepresentativeMention(int addr, String v) {
        if (featOkTst && casFeat_RepresentativeMention == null)
      jcas.throwFeatMissing("RepresentativeMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_RepresentativeMention, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public IaiCorefAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_CorefMention = jcas.getRequiredFeatureDE(casType, "CorefMention", "uima.cas.String", featOkTst);
    casFeatCode_CorefMention  = (null == casFeat_CorefMention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_CorefMention).getCode();

 
    casFeat_CorefChain = jcas.getRequiredFeatureDE(casType, "CorefChain", "uima.cas.Integer", featOkTst);
    casFeatCode_CorefChain  = (null == casFeat_CorefChain) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_CorefChain).getCode();

 
    casFeat_RepresentativeMention = jcas.getRequiredFeatureDE(casType, "RepresentativeMention", "uima.cas.String", featOkTst);
    casFeatCode_RepresentativeMention  = (null == casFeat_RepresentativeMention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_RepresentativeMention).getCode();

  }
}



    