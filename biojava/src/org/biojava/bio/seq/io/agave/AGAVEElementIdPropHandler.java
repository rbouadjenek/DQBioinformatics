/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojava.bio.seq.io.agave;
import java.util.ListIterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @author Hanning Ni    Doubletwist Inc
 */
public class AGAVEElementIdPropHandler extends StAXPropertyHandler{

   public static final StAXHandlerFactory AGAVE_ELEMENT_ID_PROP_HANDLER_FACTORY
    = new StAXHandlerFactory() {
    public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
      return new AGAVEElementIdPropHandler(staxenv);
    }
   };
   private String id ;
   AGAVEElementIdPropHandler(StAXFeatureHandler staxenv) {
    // execute superclass method to setup environment
    super(staxenv);
    setHandlerCharacteristics("element_id", true);
  }

  public void startElementHandler(
                String nsURI,
                String localName,
                String qName,
                Attributes attrs)
         throws SAXException
  {
      id = attrs.getValue("id") ;
  }

  public void endElementHandler(
                String nsURI,
                String localName,
                String qName,
                StAXContentHandler handler)
                throws SAXException
  {
       int currLevel = staxenv.getLevel();
       if (currLevel >=1) {
           ListIterator li = staxenv.getHandlerStackIterator(currLevel);
           while( li.hasPrevious() )
          {
              Object ob =   li.previous() ;
              if( ob instanceof AGAVEEvidenceCallbackItf )
              {
                  ( (AGAVEEvidenceCallbackItf) ob ).addElementId( id ) ;
                   return ;
              }
          }
       }

  }

}