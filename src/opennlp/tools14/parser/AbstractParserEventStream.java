///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2006 Thomas Morton
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.tools14.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import opennlp.maxent25.DataStream;
import opennlp.maxent25.Event;
import opennlp.maxent25.EventStream;
import opennlp.tools14.chunker.ChunkerContextGenerator;
import opennlp.tools14.dictionary.Dictionary;
import opennlp.tools14.parser.chunking.Parser;
import opennlp.tools14.postag.DefaultPOSContextGenerator;
import opennlp.tools14.postag.POSContextGenerator;

/**
 * Abstract class extended by parser event streams which perform tagging and chunking.
 * @author Tom Morton
 *
 */
public abstract class AbstractParserEventStream implements EventStream {

  private ChunkerContextGenerator chunkerContextGenerator;
  private POSContextGenerator tagContextGenerator;
  private Event[] events;
  private int ei;
  private DataStream data;
  protected HeadRules rules;
  protected Set punctSet;
  /** The type of events being generated by this event stream. */
  protected ParserEventTypeEnum etype;
  protected boolean fixPossesives;
  protected Dictionary dict;
  
  public AbstractParserEventStream(DataStream d, HeadRules rules, ParserEventTypeEnum etype, Dictionary dict) {
    this.dict = dict;
    if (etype == ParserEventTypeEnum.CHUNK) {
      this.chunkerContextGenerator = new ChunkContextGenerator();
    }
    else if (etype == ParserEventTypeEnum.TAG) {
      this.tagContextGenerator = new DefaultPOSContextGenerator(null);
    }
    this.rules = rules;
    punctSet = rules.getPunctuationTags();
    this.etype = etype;
    data = d;
    ei = 0;
    init();
    if (d.hasNext()) {
      addNewEvents();
    }
    else {
      events = new Event[0];
    }
  }
  
  protected void init() {
    fixPossesives = false;
  }
  
  public AbstractParserEventStream(DataStream d, HeadRules rules, ParserEventTypeEnum etype) {
    this (d,rules,etype,null);
  }

  public Event nextEvent() {
    while (ei == events.length) {
      addNewEvents();
      ei = 0;
    }
    return events[ei++];
  }

  public boolean hasNext() {
    return ei < events.length || data.hasNext();
  }
  
  public static Parse[] getInitialChunks(Parse p) {
    List chunks = new ArrayList();
    getInitialChunks(p, chunks);
    return (Parse[]) chunks.toArray(new Parse[chunks.size()]);
  }
  
  private static void getInitialChunks(Parse p, List ichunks) {
    if (p.isPosTag()) {
      ichunks.add(p);
    }
    else {
      Parse[] kids = p.getChildren();
      boolean allKidsAreTags = true;
      for (int ci = 0, cl = kids.length; ci < cl; ci++) {
        if (!kids[ci].isPosTag()) {
          allKidsAreTags = false;
          break;
        }
      }
      if (allKidsAreTags) {
        ichunks.add(p);
      }
      else {
        for (int ci = 0, cl = kids.length; ci < cl; ci++) {
          getInitialChunks(kids[ci], ichunks);
        }
      }
    }
  }
  
  private void addNewEvents() {
    String parseStr = (String) data.nextToken();
    //System.err.println("ParserEventStream.addNewEvents: "+parseStr);
    List newEvents = new ArrayList();
    Parse p = Parse.parseParse(parseStr);
    Parse.pruneParse(p);
    if (fixPossesives) {
      Parse.fixPossesives(p);
    }
    p.updateHeads(rules);
    Parse[] chunks = getInitialChunks(p);
    if (etype == ParserEventTypeEnum.TAG) {
      addTagEvents(newEvents, chunks);
    }
    else if (etype == ParserEventTypeEnum.CHUNK) {
      addChunkEvents(newEvents, chunks);
    }
    else {
      addParseEvents(newEvents, Parser.collapsePunctuation(chunks,punctSet));
    }
    this.events = (Event[]) newEvents.toArray(new Event[newEvents.size()]);
  }
  
  /**
   * Produces all events for the specified sentence chunks 
   * and adds them to the specified list.
   * @param newEvents A list of events to be added to.
   * @param chunks Pre-chunked constituents of a sentence.
   */
  protected abstract void addParseEvents(List newEvents, Parse[] chunks);
  
  private void addChunkEvents(List chunkEvents, Parse[] chunks) {
    List toks = new ArrayList();
    List tags = new ArrayList();
    List preds = new ArrayList();
    for (int ci = 0, cl = chunks.length; ci < cl; ci++) {
      Parse c = chunks[ci];
      if (c.isPosTag()) {
        toks.add(c.toString());
        tags.add(c.getType());
        preds.add(Parser.OTHER);
      }
      else {
        boolean start = true;
        String ctype = c.getType();
        Parse[] kids = c.getChildren();
        for (int ti=0,tl=kids.length;ti<tl;ti++) {
          Parse tok = kids[ti];
          toks.add(tok.toString());
          tags.add(tok.getType());
          if (start) {
            preds.add(Parser.START + ctype);
            start = false;
          }
          else {
            preds.add(Parser.CONT + ctype);
          }
        }
      }
    }
    for (int ti = 0, tl = toks.size(); ti < tl; ti++) {
      chunkEvents.add(new Event((String) preds.get(ti), chunkerContextGenerator.getContext(ti, toks.toArray(), (String[]) tags.toArray(new String[tags.size()]), (String[]) preds.toArray(new String[preds.size()]))));
    }
  }

  private void addTagEvents(List tagEvents, Parse[] chunks) {
    List toks = new ArrayList();
    List preds = new ArrayList();
    for (int ci = 0, cl = chunks.length; ci < cl; ci++) {
      Parse c = chunks[ci];
      if (c.isPosTag()) {
        toks.add(c.toString());
        preds.add(c.getType());
      }
      else {
        Parse[] kids = c.getChildren();
        for (int ti=0,tl=kids.length;ti<tl;ti++) {
          Parse tok = kids[ti];
          toks.add(tok.toString());
          preds.add(tok.getType());
        }
      }
    }
    for (int ti = 0, tl = toks.size(); ti < tl; ti++) {
      tagEvents.add(new Event((String) preds.get(ti), tagContextGenerator.getContext(ti, toks.toArray(), (String[]) preds.toArray(new String[preds.size()]), null)));
    }
  }

  /**
   * Returns true if the specified child is the last child of the specified parent.
   * @param child The child parse.
   * @param parent The parent parse.
   * @return true if the specified child is the last child of the specified parent; false otherwise.
   */
  protected boolean lastChild(Parse child, Parse parent) {
    Parse[] kids = AbstractBottomUpParser.collapsePunctuation(parent.getChildren(),punctSet);
    return (kids[kids.length - 1] == child);
  }

}
