

/* First created by JCasGen Wed Oct 28 14:17:53 CET 2015 */
package com.iai.uima.jcas.tcas;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Mar 23 13:19:32 CET 2016
 * XML source: D:/merlin/GitHub/QuoteAnnotator/src/main/resources/com/iai/uima/jcas/tcas/QuoteAnnotatorTypeSystem.xml
 * @generated */
public class QuoteAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(QuoteAnnotation.class);
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
  protected QuoteAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public QuoteAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public QuoteAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public QuoteAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: Quotee

  /** getter for Quotee - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuotee() {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_Quotee == null)
      jcasType.jcas.throwFeatMissing("Quotee", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_Quotee);}
    
  /** setter for Quotee - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuotee(String v) {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_Quotee == null)
      jcasType.jcas.throwFeatMissing("Quotee", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_Quotee, v);}    
   
    
  //*--------------*
  //* Feature: QuoteRelation

  /** getter for QuoteRelation - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuoteRelation() {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_QuoteRelation == null)
      jcasType.jcas.throwFeatMissing("QuoteRelation", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_QuoteRelation);}
    
  /** setter for QuoteRelation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuoteRelation(String v) {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_QuoteRelation == null)
      jcasType.jcas.throwFeatMissing("QuoteRelation", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_QuoteRelation, v);}    
   
    
  //*--------------*
  //* Feature: QuoteType

  /** getter for QuoteType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuoteType() {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_QuoteType == null)
      jcasType.jcas.throwFeatMissing("QuoteType", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_QuoteType);}
    
  /** setter for QuoteType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuoteType(String v) {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_QuoteType == null)
      jcasType.jcas.throwFeatMissing("QuoteType", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_QuoteType, v);}    
   
    
  //*--------------*
  //* Feature: QuoteeReliability

  /** getter for QuoteeReliability - gets 
   * @generated
   * @return value of the feature 
   */
  public int getQuoteeReliability() {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_QuoteeReliability == null)
      jcasType.jcas.throwFeatMissing("QuoteeReliability", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_QuoteeReliability);}
    
  /** setter for QuoteeReliability - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuoteeReliability(int v) {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_QuoteeReliability == null)
      jcasType.jcas.throwFeatMissing("QuoteeReliability", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    jcasType.ll_cas.ll_setIntValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_QuoteeReliability, v);}    
   
    
  //*--------------*
  //* Feature: RepresentativeQuoteMention

  /** getter for RepresentativeQuoteMention - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRepresentativeQuoteMention() {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_RepresentativeQuoteMention == null)
      jcasType.jcas.throwFeatMissing("RepresentativeQuoteMention", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_RepresentativeQuoteMention);}
    
  /** setter for RepresentativeQuoteMention - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRepresentativeQuoteMention(String v) {
    if (QuoteAnnotation_Type.featOkTst && ((QuoteAnnotation_Type)jcasType).casFeat_RepresentativeQuoteMention == null)
      jcasType.jcas.throwFeatMissing("RepresentativeQuoteMention", "com.iai.uima.jcas.tcas.QuoteAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((QuoteAnnotation_Type)jcasType).casFeatCode_RepresentativeQuoteMention, v);}    
  }

    