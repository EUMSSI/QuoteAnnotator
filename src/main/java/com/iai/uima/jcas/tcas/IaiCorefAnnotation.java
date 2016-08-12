

/* First created by JCasGen Wed Mar 09 12:17:59 CET 2016 */
package com.iai.uima.jcas.tcas;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Aug 12 16:04:19 CEST 2016
 * XML source: D:/susanne/git/QuoteAnnotator/src/main/resources/com/iai/uima/jcas/tcas/QuoteAnnotatorTypeSystem.xml
 * @generated */
public class IaiCorefAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(IaiCorefAnnotation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected IaiCorefAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public IaiCorefAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public IaiCorefAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public IaiCorefAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: CorefMention

  /** getter for CorefMention - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCorefMention() {
    if (IaiCorefAnnotation_Type.featOkTst && ((IaiCorefAnnotation_Type)jcasType).casFeat_CorefMention == null)
      jcasType.jcas.throwFeatMissing("CorefMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((IaiCorefAnnotation_Type)jcasType).casFeatCode_CorefMention);}
    
  /** setter for CorefMention - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCorefMention(String v) {
    if (IaiCorefAnnotation_Type.featOkTst && ((IaiCorefAnnotation_Type)jcasType).casFeat_CorefMention == null)
      jcasType.jcas.throwFeatMissing("CorefMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((IaiCorefAnnotation_Type)jcasType).casFeatCode_CorefMention, v);}    
   
    
  //*--------------*
  //* Feature: CorefChain

  /** getter for CorefChain - gets 
   * @generated
   * @return value of the feature 
   */
  public int getCorefChain() {
    if (IaiCorefAnnotation_Type.featOkTst && ((IaiCorefAnnotation_Type)jcasType).casFeat_CorefChain == null)
      jcasType.jcas.throwFeatMissing("CorefChain", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((IaiCorefAnnotation_Type)jcasType).casFeatCode_CorefChain);}
    
  /** setter for CorefChain - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCorefChain(int v) {
    if (IaiCorefAnnotation_Type.featOkTst && ((IaiCorefAnnotation_Type)jcasType).casFeat_CorefChain == null)
      jcasType.jcas.throwFeatMissing("CorefChain", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    jcasType.ll_cas.ll_setIntValue(addr, ((IaiCorefAnnotation_Type)jcasType).casFeatCode_CorefChain, v);}    
   
    
  //*--------------*
  //* Feature: RepresentativeMention

  /** getter for RepresentativeMention - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRepresentativeMention() {
    if (IaiCorefAnnotation_Type.featOkTst && ((IaiCorefAnnotation_Type)jcasType).casFeat_RepresentativeMention == null)
      jcasType.jcas.throwFeatMissing("RepresentativeMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((IaiCorefAnnotation_Type)jcasType).casFeatCode_RepresentativeMention);}
    
  /** setter for RepresentativeMention - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRepresentativeMention(String v) {
    if (IaiCorefAnnotation_Type.featOkTst && ((IaiCorefAnnotation_Type)jcasType).casFeat_RepresentativeMention == null)
      jcasType.jcas.throwFeatMissing("RepresentativeMention", "com.iai.uima.jcas.tcas.IaiCorefAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((IaiCorefAnnotation_Type)jcasType).casFeatCode_RepresentativeMention, v);}    
  }

    