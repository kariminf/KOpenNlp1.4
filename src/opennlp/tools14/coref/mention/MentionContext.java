///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2003 Thomas Morton
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
package opennlp.tools14.coref.mention;

import java.util.List;

import opennlp.tools14.coref.sim.Context;
import opennlp.tools14.coref.sim.GenderEnum;
import opennlp.tools14.coref.sim.NumberEnum;
import opennlp.tools14.util.Span;

/** Data strucure representation of a mention with additional contextual information.  The contextual
 * information is used in performing coreference resolution.
 */
public class MentionContext extends Context {
  /** The index of first token which is not part of a descriptor.  This is 0 if no descriptor is present. */
  private int nonDescriptorStart;
  /** The Parse of the head constituent of this mention. */
  private Parse head;
  /** Sentence-token-based span whose end is the last token of the mention. */
  private Span indexSpan;
  /** Position of the NP in the sentence. */
  private int nounLocation;
  /** Position of the NP in the document. */
  private  int nounNumber; 
  /** Number of noun phrases in the sentence which contains this mention. */
  private int maxNounLocation;
  /** Index of the sentece in the document which contains this mention. */
  private int sentenceNumber;
  /** The token preceeding this mention's maximal noun phrase.*/
  private Parse prevToken;
  /** The token following this mention's maximal noun phrase.*/
  private Parse nextToken;
  /** The token following this mention's basal noun phrase.*/
  private Parse basalNextToken;
  
  
  /** The parse of the mention's head word. */
  private Parse headToken;
  /** The parse of the first word in the mention. */
  private Parse firstToken;
  /** The text of the first word in the mention. */
  private String firstTokenText;
  /** The pos-tag of the first word in the mention. */
  private String firstTokenTag;
  /** The gender assigned to this mention. */
  private GenderEnum gender;
  /** The probability associated with the gender assignment. */
  private double genderProb;
  /** The number assigned to this mention. */
  private NumberEnum number;
  /** The robability associated with the number assignment. */
  private double numberProb;
  

  public MentionContext(Span span, Span headSpan, int entityId, Parse parse, String extentType, String nameType, int mentionIndex, int mentionsInSentence, int mentionIndexInDocument, int sentenceIndex, HeadFinder headFinder) {
    super(span,headSpan,entityId,parse,extentType,nameType,headFinder);
    nounLocation = mentionIndex;
    maxNounLocation = mentionsInSentence;
    nounNumber = mentionIndexInDocument;
    sentenceNumber = sentenceIndex;
    indexSpan = parse.getSpan();
    prevToken = parse.getPreviousToken();
    nextToken = parse.getNextToken();
    head = headFinder.getLastHead(parse);
    List headTokens = head.getTokens();
    tokens = headTokens.toArray(new Parse[headTokens.size()]);
    basalNextToken = head.getNextToken();
    //System.err.println("MentionContext.init: "+ent+" "+ent.getEntityId()+" head="+head);
    nonDescriptorStart = 0;
    initHeads(headFinder.getHeadIndex(head));
    gender = GenderEnum.UNKNOWN;
    this.genderProb = 0d;
    number = NumberEnum.UNKNOWN;
    this.numberProb = 0d;
  }
  /**
   * Constructs context information for the specified mention.
   * @param mention The mention object on which this object is based.
   * @param mentionIndexInSentence The mention's position in the sentence.
   * @param mentionsInSentence The number of mentions in the sentence.
   * @param mentionIndexInDocument The index of this mention with respect to the document.
   * @param sentenceIndex The index of the sentence which contains this mention.
   * @param headFinder An object which provides head information.
   */
  public MentionContext(Mention mention, int mentionIndexInSentence, int mentionsInSentence, int mentionIndexInDocument, int sentenceIndex, HeadFinder headFinder) {
    this(mention.getSpan(),mention.getHeadSpan(),mention.getId(),mention.getParse(),mention.type,mention.nameType, mentionIndexInSentence,mentionsInSentence,mentionIndexInDocument,sentenceIndex,headFinder);
  }
      

  /** 
   * Constructs context information for the specified mention.
   * @param mentionParse Mention parse structure for which context is to be constructed.
   *  @param mentionIndex mention position in sentence.
   *  @param mentionsInSentence Number of mentions in the sentence.
   *  @param mentionsInDocument Number of mentions in the document.
   *  @param sentenceIndex Sentence number for this mention.
   *  @param nameType The named-entity type for this mention.
   *  @param headFinder Object which provides head information.
   **/
  /*
  public MentionContext(Parse mentionParse, int mentionIndex, int mentionsInSentence, int mentionsInDocument, int sentenceIndex, String nameType, HeadFinder headFinder) {
    nounLocation = mentionIndex;
    maxNounLocation = mentionsInDocument;
    sentenceNumber = sentenceIndex;
    parse = mentionParse;
    indexSpan = mentionParse.getSpan();
    prevToken = mentionParse.getPreviousToken();
    nextToken = mentionParse.getNextToken();
    head = headFinder.getLastHead(mentionParse);
    List headTokens = head.getTokens();
    tokens = (Parse[]) headTokens.toArray(new Parse[headTokens.size()]);
    basalNextToken = head.getNextToken();
    //System.err.println("MentionContext.init: "+ent+" "+ent.getEntityId()+" head="+head);
    indexHeadSpan = head.getSpan();
    nonDescriptorStart = 0;
    initHeads(headFinder.getHeadIndex(head));
    this.neType= nameType;
    if (getHeadTokenTag().startsWith("NN") && !getHeadTokenTag().startsWith("NNP")) {
      //if (headTokenTag.startsWith("NNP") && neType != null) {
      this.synsets = getSynsetSet(this);
    }
    else {
      this.synsets=Collections.EMPTY_SET;
    }
    gender = GenderEnum.UNKNOWN;
    this.genderProb = 0d;
    number = NumberEnum.UNKNOWN;
    this.numberProb = 0d;
  }
  */

  private void initHeads(int headIndex) {
    this.headTokenIndex=headIndex;
    this.headToken = (Parse) tokens[getHeadTokenIndex()];
    this.headTokenText = headToken.toString();
    this.headTokenTag=headToken.getSyntacticType();
    this.firstToken = (Parse) tokens[0];
    this.firstTokenTag = firstToken.getSyntacticType();
    this.firstTokenText=firstToken.toString();
  }

  /**
   * Returns the parse of the head token for this mention.
   * @return the parse of the head token for this mention.
   */
  public Parse getHeadTokenParse() {
    return headToken;
  }
  
  public String getHeadText() {
    StringBuffer headText = new StringBuffer();
    for (int hsi = 0; hsi < tokens.length; hsi++) {
      headText.append(" ").append(tokens[hsi].toString());
    }
    return headText.toString().substring(1);
  }
  
  public Parse getHead() {
    return head;
  }
    
  public int getNonDescriptorStart() {
    return this.nonDescriptorStart;
  }
  
  /** 
   * Returns a sentence-based token span for this mention.  If this mention consist
   * of the third, fourth, and fifth token, then this span will be 2..4.   
   * @return a sentence-based token span for this mention.
   */
  public Span getIndexSpan() {
    return indexSpan;
  }
    
  /**
   * Returns the index of the noun phrase for this mention in a sentence.
   * @return the index of the noun phrase for this mention in a sentence.
   */
  public int getNounPhraseSentenceIndex() {
    return nounLocation;
  }
  
  /**
   * Returns the index of the noun phrase for this mention in a document.
   * @return the index of the noun phrase for this mention in a document.
   */
  public int getNounPhraseDocumentIndex() {
    return nounNumber;
  }
  
  /**
   * Returns the index of the last noun phrase in the sentence containing this mention.
   * This is one less than the number of noun phrases in the sentence which contains this mention. 
   * @return the index of the last noun phrase in the sentence containing this mention.
   */
  public int getMaxNounPhraseSentenceIndex() {
    return maxNounLocation;
  }
  
  public Parse getNextTokenBasal() {
    return basalNextToken;
  }
  
  public Parse getPreviousToken() {
    return prevToken;
  }
  
  public Parse getNextToken() {
    return nextToken;
  }
  
  /**
   * Returns the index of the sentence which contains this mention.
   * @return the index of the sentence which contains this mention.
   */ 
  public int getSentenceNumber() {
    return sentenceNumber;
  }

  /** Returns the parse for the first token in this mention.
   * @return The parse for the first token in this mention.
   */
  public Parse getFirstToken() {
    return firstToken;
  }

  /** Returns the text for the first token of the mention.
   * @return The text for the first token of the mention.
   */
  public String getFirstTokenText() {
    return firstTokenText;
  }
  
  /**
   * Returns the pos-tag of the first token of this mention. 
   * @return the pos-tag of the first token of this mention.
   */
  public String getFirstTokenTag() {
    return firstTokenTag;
  }
  
  /**
   * Returns the parses for the tokens which are contained in this mention.
   * @return An array of parses, in order, for each token contained in this mention.
   */
  public Parse[] getTokenParses() {
    return (Parse[]) tokens;
  }

  /**
   * Returns the text of this mention. 
   * @return A space-delimited string of the tokens of this mention.
   */
  public String toText() {
    return parse.toString();
  }
  
  /*
  private static String[] getLemmas(MentionContext xec) {
    //TODO: Try multi-word lemmas first.
    String word = xec.getHeadTokenText();
    return DictionaryFactory.getDictionary().getLemmas(word,"NN");
  }
  
  private static Set getSynsetSet(MentionContext xec) {
    //System.err.println("getting synsets for mention:"+xec.toText());
    Set synsetSet = new HashSet();
    String[] lemmas = getLemmas(xec);
    for (int li = 0; li < lemmas.length; li++) {
      String[] synsets = DictionaryFactory.getDictionary().getParentSenseKeys(lemmas[li],"NN",0);
      for (int si=0,sn=synsets.length;si<sn;si++) {
        synsetSet.add(synsets[si]);
      }
    }
    return (synsetSet);
  }
  */

  /**
   * Assigns the specified gender with the specified probability to this mention.
   * @param gender The gender to be given to this mention.
   * @param probability The probability assosicated with the gender assignment.
   */
  public void setGender(GenderEnum gender, double probability) {
    this.gender = gender;
    this.genderProb = probability;
  }

  /** 
   * Returns the gender of this mention.
   * @return The gender of this mention.
   */
  public GenderEnum getGender() {
    return gender;
  }

  /**
   * Returns the probability associated with the gender assignment.a
   * @return The probability associated with the gender assignment.
   */
  public double getGenderProb() {
    return genderProb;
  }

  /**
   * Assigns the specified number with the specified probability to this mention.
   * @param number The number to be given to this mention.
   * @param probability The probability assosicated with the number assignment.
   */
  public void setNumber(NumberEnum number, double probability) {
    this.number = number;
    this.numberProb = probability;
  }
  
  /** 
   * Returns the number of this mention.
   * @return The number of this mention.
   */
  public NumberEnum getNumber() {
    return number;
  }

  /**
   * Returns the probability associated with the number assignment.
   * @return The probability associated with the number assignment.
   */
  public double getNumberProb() {
    return numberProb;
  }
}
