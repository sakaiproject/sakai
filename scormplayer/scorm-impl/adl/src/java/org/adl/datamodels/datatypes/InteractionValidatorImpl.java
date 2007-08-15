/******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
** redistribute this software in source and binary code form, provided that 
** i) this copyright notice and license appear on all copies of the software; 
** and ii) Licensee does not utilize the software in a manner which is 
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL 
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
** DAMAGES.
**
*******************************************************************************/

package org.adl.datamodels.datatypes;

import java.util.HashSet;
import java.util.Vector;

import org.adl.datamodels.DMDelimiter;
import org.adl.datamodels.DMErrorCodes;
import org.adl.logging.DetailedLogMessageCollection;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.util.Messages;

/**
 * <strong>Filename:</strong> InteractionValidator.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Provides support for the SCORM Data Model Interaction data types, as defined 
 * in the SCORM 2004.
 * 
 * @author ADL Technical Team
 */
public class InteractionValidatorImpl extends InteractionValidator 
{
	private static final long serialVersionUID = 1L;


   /**
    * Constructor required for type initialization.
    * 
    * @param iType  Identifies the type of interaction being validated.
    * 
    * @param iElement  Describes the data model element this validator is 
    * associated with.
    */
   public InteractionValidatorImpl(int iType, String iElement) 
   { 
      mInteractionType = iType;

      // Set the element name for the type
      mType = iElement;
      mElement = iElement;
   } 

   /**
    * Constructor required for type initialization.
    * 
    * @param iType Identifies the type of interaction being validated.
    * 
    * @param iAllowEmpty Describes if this validator should allow an empty
    *  string (<code>""</code>).
    * 
    * @param iElement Describes the data model element this validator is 
    *  associated with.
    */
   public InteractionValidatorImpl(int iType, boolean iAllowEmpty, String iElement) 
   { 
      mInteractionType = iType;
      mAllowEmpty = iAllowEmpty;

      // Set the element name for the type
      mType = iElement;
      mElement = iElement;
   } 


   /**
    * Compares two valid data model elements for equality.
    * 
    * @param iFirst  The first value being compared.
    * 
    * @param iSecond The second value being compared.
    * 
    * @param iDelimiters The common set of delimiters associated with the
    * values being compared.
    * 
    * @return <code>true</code> if the two values are equal, otherwise
    *         <code>false</code>.
    */
   public boolean compare(String iFirst, String iSecond, Vector iDelimiters)
   {
      boolean equal = true;

      // SCORM defined separators
      String comma  = "\\[,\\]";     

      // Lang code validator
      LangStringValidator langValidator = new LangStringValidator();

      // Real Range validator
      RealRangeValidator realValidator = new  RealRangeValidator();

      // URI validator for maximum of 250 elements
      URIValidator uriValidator =
      new URIValidator(250, "short_identifier_type"); 

      int idx = -1;

      // Swith on the mInteractionType member to determine the type
      // being compared.
      switch ( mInteractionType )
      {
         case MULTIPLE_CHOICE :
         {
            String choices1[] = iFirst.split(comma);
            String choices2[] = iSecond.split(comma);

            HashSet set1 = new HashSet();
            HashSet set2 = new HashSet();

//          TODO:  Is this supposed to be used?  If not remove - not being used - only to hold return value which is never used
            boolean added = false;  

            for ( int i = 0; i < choices1.length; i++ )
            {
               added = set1.add(choices1[i]);
            }

            for ( int i = 0; i < choices2.length; i++ )
            {
               added = set2.add(choices2[i]);
            }

            equal = set1.equals(set2);

            break;
         }
         case FILL_IN :
         {
            // Assume default delimiters
            boolean caseMatters = false;
            boolean orderMatters = true;

            // Extract each part of the match_text
            String matchText1[] = iFirst.split(comma);
            String matchText2[] = iSecond.split(comma);

            // If the lengths are not equal, we're done
            if ( matchText1.length == matchText2.length )
            {
               if ( iDelimiters != null )
               {
                  // Loop accross all delimiters looking for order_matters and
                  // case_matters
                  for ( int i = 0; i < iDelimiters.size(); i++ )
                  {
                     DMDelimiter del = (DMDelimiter)iDelimiters.elementAt(i);

                     if ( del.mDescription.mName.equals("order_matters") ) 
                     {
                        // Is this the default?
                        if ( del.mValue != null )
                        {
                           orderMatters = false;
                        }
                     }
                     else if ( del.mDescription.mName.equals("case_matters") ) 
                     {
                        // Is this the default?
                        if ( del.mValue != null )
                        {
                           caseMatters = true;
                        }
                     }
                  }
               }

               String matchString1 = null;
               String matchString2 = null;

               String langString1 = "en"; 
               String langString2 = "en"; 

               // If order matters, just loop over the array
               if ( orderMatters )
               {
                  for ( int i = 0; i < matchText1.length && equal; i++ )
                  {
                     // Look for the 'lang' delimiter in first value
                     if ( matchText1[i].startsWith("{lang=") ) 
                     {
                        // Find the closing '}'
                        idx = matchText1[i].indexOf('}');
                        if ( idx != -1 )
                        {
                           matchString1 = matchText1[i].substring(idx + 1);
                           langString1 = matchText1[i].substring(6, idx);
                        }
                        else
                        {
                           langString1 = "en"; 
                           matchString1 = matchText1[i];
                        }
                     }
                     else
                     {
                        langString1 = "en"; 
                        matchString1 = matchText1[i];
                     }

                     // Look for the 'lang' delimiter in second value
                     if ( matchText2[i].startsWith("{lang=") ) 
                     {
                        // Find the closing '}'
                        idx = matchText2[i].indexOf('}');
                        if ( idx != -1 )
                        {
                           matchString2 = matchText2[i].substring(idx + 1);
                           langString2 = matchText2[i].substring(6, idx);
                        }
                        else
                        {
                           langString2 = "en"; 
                           matchString2 = matchText2[i];
                        }
                     }
                     else
                     {
                        langString2 = "en"; 
                        matchString2 = matchText2[i];
                     }

                     // Make sure the lang codes are equal
                     equal = langValidator.compare(langString1, 
                                                   langString2, null);

                     if ( equal )
                     {
                        // Compare case?
                        if ( caseMatters )
                        {
                           equal = matchString1.equals(matchString2);
                        }
                        else
                        {
                           equal = matchString1.equalsIgnoreCase(matchString2);
                        }
                     }
                     else
                     {
                        equal = false;
                     }
                  }
               }
               else
               {
                  Vector matched = new Vector();

                  // set all matched to 'not matched'
                  for ( int i = 0; i < matchText1.length; i++ )
                  {
                     matched.add(new Boolean(false));
                  }

                  boolean found = false;

                  // Loop accross all strings, looking for matches
                  for ( int i = 0; i < matchText1.length && equal; i++ )
                  {
                     matchString1 = null;
                     matchString2 = null;

                     langString1 = "en"; 
                     langString2 = "en"; 

                     // Look for the 'lang' delimiter in first value
                     if ( matchText1[i].startsWith("{lang=") ) 
                     {
                        // Find the closing '}'
                        idx = matchText1[i].indexOf('}');
                        if ( idx != -1 )
                        {
                           matchString1 = matchText1[i].substring(idx + 1);
                           langString1 = matchText1[i].substring(6, idx);
                        }
                        else
                        {
                           langString1 = "en"; 
                           matchString1 = matchText1[i];
                        }
                     }
                     else
                     {
                        langString1 = "en"; 
                        matchString1 = matchText1[i];
                     }


                     found = false;
                     for ( int j = 0; j < matchText2.length && !found; j++ )
                     {

                        // Look for the 'lang' delimiter in second value
                        if ( matchText2[j].startsWith("{lang=") ) 
                        {
                           // Find the closing '}'
                           idx = matchText2[j].indexOf('}');
                           if ( idx != -1 )
                           {
                              matchString2 = matchText2[j].substring(idx + 1);
                              langString2 = matchText2[j].substring(6, idx);
                           }
                           else
                           {
                              langString2 = "en"; 
                              matchString2 = matchText2[j];
                           }
                        }
                        else
                        {
                           langString2 = "en"; 
                           matchString2 = matchText2[j];
                        }

                        // Check if the lang codes are equal
                        equal = 
                        langValidator.compare(langString1, langString2, null);

                        if ( equal )
                        {
                           // Compare case?
                           if ( caseMatters )
                           {
                              found = matchString1.equals(matchString2);
                           }
                           else
                           {
                              found = 
                              matchString1.equalsIgnoreCase(matchString2);
                           }
                        }

                        if ( found )
                        {
                           boolean used =
                           ((Boolean)matched.elementAt(j)).booleanValue();

                           // Make sure this value was not used before
                           if ( !used )
                           {
                              // Remember that we've matched this string
                              matched.set(j, new Boolean(true));
                           }
                           else
                           {
                              // Keep looking for a match
                              found = false;
                           }
                        }
                     }

                     if ( !found )
                     {
                        // Did not find a match, we're done
                        equal = false;
                     }
                  }
               }
            }
            else
            {
               equal = false;
            }

            break;
         }
         case LONG_FILL_IN :
         {
            // Assume default delimiters
            boolean caseMatters = false;

            if ( iDelimiters != null )
            {
               // Loop accross all delimiters looking for case_matters
               for ( int i = 0; i < iDelimiters.size(); i++ )
               {
                  DMDelimiter del = (DMDelimiter)iDelimiters.elementAt(i);

                  if ( del.mDescription.mName.equals("case_matters") ) 
                  {
                     // Is this the default?
                     if ( del.mValue != null )
                     {
                        caseMatters = true;
                     }
                  }
               }
            }

            // Compare case?
            if ( caseMatters )
            {
               equal = iFirst.equals(iSecond);
            }
            else
            {
               equal = iFirst.equalsIgnoreCase(iSecond);
            }

            break;
         }
         case LIKERT :
         {
            // Check if the URIs are equal
            equal = uriValidator.compare(iFirst, iSecond, null);

            break;
         }
         case MATCHING :
         {
            // Check to see if we can accept two empty strings
            if ( mAllowEmpty && 
                 (iFirst.trim().equals("") || iSecond.trim().equals("") ) )  
            {
               equal = iFirst.trim().equals(iSecond.trim());

               // We're done
               break;
            }

            String pairs1[] = iFirst.split(comma);
            String pairs2[] = iSecond.split(comma);

            // If the lengths are not equal, we're done
            if ( pairs1.length == pairs2.length )
            {
               Vector matched = new Vector();

               // set all matched pairs to 'not matched'
               for ( int i = 0; i < pairs1.length; i++ )
               {
                  matched.add(new Boolean(false));
               }

               boolean found = false;

               // Loop accross all pairs, looking for matches
               for ( int i = 0; i < pairs1.length && equal; i++ )
               {
                  idx = pairs1[i].indexOf("[.]"); 

                  String source1 = pairs1[i].substring(0, idx);
                  String target1 = pairs1[i].substring(idx + 3, 
                                                       pairs1[i].length());

                  found = false;
                  for ( int j = 0; j < pairs2.length && !found; j++ )
                  {
                     idx = pairs2[i].indexOf("[.]"); 

                     String source2 = pairs2[i].substring(0, idx);
                     String target2 = pairs2[i].substring(idx + 3, 
                                                          pairs2[i].length());

                     // Compare the sources
                     found = uriValidator.compare(source1, source2, null);

                     if ( found )
                     {
                        // Compare the targets
                        found = uriValidator.compare(target1, target2, null);
                     }

                     if ( found )
                     {
                        boolean used =
                        ((Boolean)matched.elementAt(j)).booleanValue();

                        // Make sure this pair was not used before
                        if ( !used )
                        {
                           // Remember that we've matched this pair
                           matched.set(j, new Boolean(true));
                        }
                        else
                        {
                           // Keep looking for a match
                           found = false;
                        }
                     }
                  }

                  if ( !found )
                  {
                     // Did not find a match, we're done
                     equal = false;
                  }
               }
            }
            else
            {
               equal = false;
            }


            break;
         }
         case PERFORMANCE : 
         {
            // Check to see if we can accept two empty strings
            if ( mAllowEmpty && 
                 (iFirst.trim().equals("") || iSecond.trim().equals("") ) )  
            {
               equal = iFirst.trim().equals(iSecond.trim());

               // We're done
               break;
            }

            // Extract the array of records
            String records1[] = iFirst.split(comma);
            String records2[] = iSecond.split(comma);

            boolean orderMatters = true;

            // If the lengths are not equal, we're done
            if ( records1.length == records2.length )
            {
               if ( iDelimiters != null )
               {
                  // Loop accross all delimiters looking for order_matters 
                  for ( int i = 0; i < iDelimiters.size(); i++ )
                  {
                     DMDelimiter del = (DMDelimiter)iDelimiters.elementAt(i);

                     if ( del.mDescription.mName.equals("order_matters") ) 
                     {
                        // Is this the default?
                        if ( del.mValue != null )
                        {
                           orderMatters = false;
                        }
                     }
                  }
               }

               // If order matters, loop accross all records
               if ( orderMatters )
               {
                  for ( int i = 0; i < records1.length && equal; i++ )
                  {
                     // Simply compare the records
                     equal = records1[i].equals(records2[i]);
                  }
               }
               else
               {
                  Vector matched = new Vector();

                  // set all matched records to 'not matched'
                  for ( int i = 0; i < records1.length; i++ )
                  {
                     matched.add(new Boolean(false));
                  }

                  boolean found = false;

                  // Loop accross all pairs, looking for matches
                  for ( int i = 0; i < records1.length && equal; i++ )
                  {
                     found = false;
                     for ( int j = 0; j < records2.length && !found; j++ )
                     {
                        // Compare the records
                        found = records1[i].equals(records2[j]);

                        if ( found )
                        {
                           boolean used =
                           ((Boolean)matched.elementAt(j)).booleanValue();

                           // Make sure this record was not used before
                           if ( !used )
                           {
                              // Remember that we've matched this record
                              matched.set(j, new Boolean(true));
                           }
                           else
                           {
                              // Keep looking for a match
                              found = false;
                           }
                        }
                     }

                     if ( !found )
                     {
                        // Did not find a match, we're done
                        equal = false;
                     }
                  }
               }
            }
            else
            {
               equal = false;
            }

            break;
         }
         case SEQUENCING :
         {
            // Extract each part of the sequence
            String seq1[] = iFirst.split(comma);
            String seq2[] = iSecond.split(comma);

            // If the lengths are not equal, we're done
            if ( seq1.length == seq2.length )
            {
               for ( int i = 0; i < seq1.length && equal; i++ )
               {
                  equal = uriValidator.compare(seq1[i], seq2[i], null);
               }
            }
            else
            {
               equal = false;
               break;
            }

            break;
         }
         case NUMERIC :  
         {
            if ( mElement.
                 equals("cmi.interactions.n.correct_responses.n.pattern") ) 
            {
               String minString1 = null;
               String maxString1 = null;

               String minString2 = null;
               String maxString2 = null;

               idx = iFirst.indexOf("[:]"); 

               if ( idx != -1 )
               {
                  minString1 = iFirst.substring(0, idx);
                  maxString1 = iFirst.substring(idx + 3);
               }

               idx = iSecond.indexOf("[:]"); 
               if ( idx != -1 )
               {
                  minString2 = iSecond.substring(0, idx);
                  maxString2 = iSecond.substring(idx + 3);
               }

               if ( minString1.trim().length() > 0 ||
                    minString2.trim().length() > 0 )
               {
                  equal = realValidator.compare(minString1, 
                                                minString2, 
                                                null);
               }

               if ( equal )
               {
                  if ( maxString1.trim().length() > 0 ||
                       maxString2.trim().length() > 0 )
                  {
                     equal = realValidator.compare(maxString1, 
                                                   maxString2, 
                                                   null);
                  }
               }
            }
            else
            {
               equal = realValidator.compare(iFirst, 
                                             iSecond, 
                                             null);
            }

            break;
         }
         default:
         {
            break;
         }
      }

      return equal;
   }


   /**
    * Validates the provided string against a known format.
    * 
    * @param iValue The value being validated.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int validate(String iValue)
   {
      // Assume the value is valid
      int valid = DMErrorCodes.NO_ERROR;

      if ( iValue == null )
      {
         // A null value can never be valid
         return DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      // Real Range validator
      RealRangeValidator realValidator = new  RealRangeValidator();

      // Lang code validator
      LangStringValidator langValidator = new LangStringValidator();

      // SPM validator for maximum of 250 elements
      SPMRangeValidator shortSPMValidator = new SPMRangeValidator(250);

      // SPM validator for maximum of 4000 elements
      SPMRangeValidator longSPMValidator = new SPMRangeValidator(4000);

      // URI validator for maximum of 250 elements
      URIValidator uriValidator = new URIValidator(250, "short_identifier_type"); 

      // SCORM defined separators
      String comma  = "\\[,\\]";     
      String warn = null;

      int idx = -1;
      int result = DMErrorCodes.NO_ERROR;

      // Swith on the mInteractionType member to determine the type
      // to validate against.
      switch ( mInteractionType )
      {
         case MULTIPLE_CHOICE :
         {
            // Check for an empty set
            if ( iValue.length() == 0 )
            {
               // Value OK
               break;
            }

            // Make sure the last chars of this value are not "[,]"
            String check = iValue.trim();
            idx = check.lastIndexOf("[,]"); 

            if ( idx != -1 && ( idx == (check.length() - 3) ) )
            {
               // Cannot end with a separator
               valid = DMErrorCodes.TYPE_MISMATCH;
               break;
            }

            String choices[] = iValue.split(comma);

            // Check to determine if there are more than 36 choices
            if ( choices.length > 36 )
            {
               // Number of identifiers SPM Exceeded, create warning.

                warn = Messages.getString("InteractionValidator.0", mElement); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(
                  MessageType.WARNING, warn));
            }

            HashSet set = new HashSet();
            boolean added = false;

            // Check to determine if each choice is within the SPM range  
            for ( int i = 0; i < choices.length; i++ )
            {
               // The identifier cannot be empty
               if ( choices[i].trim().equals("") ) 
               {
                  // Cannot have an empty identifier
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  break;
               }

               // Make sure each short_identifier_type is valid
               result = uriValidator.validate(choices[i]);

               if ( result == DMErrorCodes.SPM_EXCEEDED )
               {
                  // SPM of short_identifier_type exceeded, create warning
                  warn = Messages.getString("InteractionValidator.39", mElement, i ); 

                  // Add the SPM Exceeded warning to the message log
                  DetailedLogMessageCollection.getInstance().addMessage(new 
                     LogMessage(MessageType.WARNING, warn));              
               }

               if ( result == DMErrorCodes.NO_ERROR ||
                    result == DMErrorCodes.SPM_EXCEEDED )
               {
                  // Make sure this identifier is not already in the set
                  added = set.add(choices[i]);

                  if ( !added )
                  {
                     // Cannot have duplicate identifiers
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }
               }
               else
               {
                  // Some type mismatch has occured, we are finished checking
                  valid = result;
                  break;
               }
            }

            break;
         }
         case FILL_IN :
         {
            // Extract each part of the match_text
            String matchText[] = iValue.split(comma);

            // Check to determine if there are more than 10 fill-in responses
            if ( matchText.length > 10 )
            {
               // Number of match_text SPM Exceeded, create warning.
               warn = Messages.getString("InteractionValidator.41", mElement); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new 
                  LogMessage(MessageType.WARNING, warn));
            }

            // Validate each match_string
            for ( int i = 0; i < matchText.length; i++ )
            {
               String matchString = null;
               String langString = null;

               // Look for the 'lang' delimiter
               if ( matchText[i].startsWith("{lang=") ) 
               {
                  // Find the closing '}'
                  idx = matchText[i].indexOf('}');
                  if ( idx != -1 )
                  {
                     matchString = matchText[i].substring(idx + 1);
                     langString = matchText[i].substring(6, idx);

                     // Make sure the lang code is valid
                     result = langValidator.validate(langString);

                     if ( result == DMErrorCodes.SPM_EXCEEDED )
                     {
                        // SPM of lang string exceeded, create warning
                        warn = Messages.getString("InteractionValidator.43",mElement, i );  

                        // Add the SPM Exceeded warning to the message log
                        DetailedLogMessageCollection.getInstance().addMessage(new
                           LogMessage(MessageType.WARNING, warn));              
                     }
                     else if ( result != DMErrorCodes.NO_ERROR )
                     {
                        // Invalid lang string, we're done
                        valid = result;
                        break;
                     }
                  }
                  else
                  {
                     matchString = matchText[i];
                  }
               }
               else
               {
                  matchString = matchText[i];
               }

               // Make sure the match_text is valid
               result = shortSPMValidator.validate(matchString);

               if ( result == DMErrorCodes.SPM_EXCEEDED )
               {
                  // SPM of match_string exceeded, create warning
                  warn = Messages.getString("InteractionValidator.45", mElement, i);  

                  // Add the SPM Exceeded warning to the message log
                  DetailedLogMessageCollection.getInstance().addMessage(new 
                     LogMessage(MessageType.WARNING, warn));
               }
               else if ( result != DMErrorCodes.NO_ERROR )
               {
                  // Invalid match_string, we're done
                  valid = result;
                  break;
               }
            }

            break;
         }
         case LONG_FILL_IN :
         {
            // Make sure the match_text is valid
            result = longSPMValidator.validate(iValue);

            if ( result == DMErrorCodes.SPM_EXCEEDED )
            {
               // SPM of match_string exceeded, create warning
               warn = Messages.getString("InteractionValidator.47", mElement ); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new 
                  LogMessage(MessageType.WARNING, warn));
            }
            else if ( result != DMErrorCodes.NO_ERROR )
            {
               // Invalid match_string, we're done
               valid = result;
            }

            break;
         }
         case LIKERT :
         {
            // Make sure the value is a short_identifier
            result = uriValidator.validate(iValue);
            if ( result == DMErrorCodes.SPM_EXCEEDED )
            {
               // SPM of short_identifier exceeded, create warning
               warn = Messages.getString("InteractionValidator.48", mElement); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new
                  LogMessage(MessageType.WARNING, warn));
            }
            else if ( result != DMErrorCodes.NO_ERROR )
            {
               // Invalid match_string, we're done
               valid = result;
            }

            break;
         }
         case MATCHING :
         {
            // Should we allow a total empty value
            if ( !mAllowEmpty )
            {
               if ( iValue.trim().length() == 0 )
               {
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  break;
               }
            }
            else
            {
               if ( iValue.trim().length() == 0 )
               {
                  // valid -- we're done
                  break;
               }
            }

            // Make sure the last chars of this value are not "[,]"
            String check = iValue.trim();
            idx = check.lastIndexOf("[,]"); 

            if ( idx != -1 && ( idx == (check.length() - 3) ) )
            {
               // Cannot end with a separator
               valid = DMErrorCodes.TYPE_MISMATCH;
               break;
            }

            String commas[] = iValue.split(comma);

            // Check to determine if there are more than 36 pairs
            if ( commas.length > 36 )
            {
               // Number of identifiers SPM Exceeded, create warning.
               warn = Messages.getString("InteractionValidator.50", mElement); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new
                  LogMessage(MessageType.WARNING, warn));
            }

            for ( int i = 0; i < commas.length; i++ )
            {
               if ( commas[i].length() == 0 )
               {
                  // Don't allow back to back "[,]"
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  break;
               }
               else
               {
                  // Look at this pair and confirm both short_identifiers
                  // are valid.
                  idx = commas[i].indexOf("[.]"); 

                  if ( idx == -1 )
                  {
                     // Don't have both values
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }

                  // Make sure we only have two values to look at
                  int second = commas[i].indexOf("[.]", idx + 2); 
                  if ( second != -1 )
                  {
                     // Invalid use of the '[.]'
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }

                  String target = commas[i].substring(0, idx);
                  String source = commas[i].substring(idx + 3, 
                                                      commas[i].length());

                  // Make sure neither value are zero length
                  if ( target.length() == 0 || source.length() == 0 )
                  {
                     // URI's cannot be zero length
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }

                  // Test the source                             
                  result = uriValidator.validate(source);

                  if ( result == DMErrorCodes.SPM_EXCEEDED )
                  {
                     // SPM of short_identifier_type exceeded, create warning
                     warn = Messages.getString("InteractionValidator.53", mElement, i );  
                     
                     // Add the SPM Exceeded warning to the message log
                     DetailedLogMessageCollection.getInstance().addMessage(new
                        LogMessage(MessageType.WARNING, warn));              
                  }

                  if ( result != DMErrorCodes.TYPE_MISMATCH )
                  {
                     // Test the target                             
                     result = uriValidator.validate(target);   

                     if ( result == DMErrorCodes.SPM_EXCEEDED )
                     {
                        // SPM of short_identifier_type exceeded, 
                        // create warning
                        warn = Messages.getString("InteractionValidator.55", mElement, i );  

                        // Add the SPM Exceeded warning to the message log
                        DetailedLogMessageCollection.getInstance().addMessage(new
                           LogMessage(MessageType.WARNING, warn));              
                     }
                     else if ( result != DMErrorCodes.NO_ERROR )
                     {
                        valid = result;
                     }
                  }
                  else
                  {
                     valid = DMErrorCodes.TYPE_MISMATCH;
                  }
               } 

               if ( valid == DMErrorCodes.TYPE_MISMATCH )
               {
                  // done
                  break;
               }
            } 

            break;
         }
         case PERFORMANCE : 
         {
            // Make sure the last chars of this value are not "[,]"
            String check = iValue.trim();
            idx = check.lastIndexOf("[,]"); 

            if ( idx != -1 && ( idx == (check.length() - 3) ) )
            {
               // Cannot end with a seperator
               valid = DMErrorCodes.TYPE_MISMATCH;
               break;
            }

            String commaCheck[] = iValue.split(comma);  

            // Check to determine if there are more than 125 records
            if ( commaCheck.length > 125 )
            {
               // Number of identifiers SPM Exceeded, create warning.
               warn = Messages.getString("InteractionValidator.58", mElement); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new
                  LogMessage(MessageType.WARNING, warn));
            }

            for ( int i = 0; i < commaCheck.length; i++ )
            {
               if ( commaCheck[i].length() == 0 )
               {
                  // Don't allow back to back "[,]"
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  break;
               }
               else
               {
                  idx = commaCheck[i].indexOf("[.]"); 

                  if ( idx == -1 )
                  {
                     // There must be a seperator
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }

                  // Make sure we only have two values to look at
                  int second = commaCheck[i].indexOf("[.]", idx + 3); 
                  if ( second != -1 )
                  {
                     // Cannot have two '[.]' separators
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }

                  String sn = commaCheck[i].substring(0, idx);
                  String sa = 
                  commaCheck[i].substring(idx + 3, 
                                          commaCheck[i].length());

                  // Make sure both value are not zero length
                  if ( sn.length() == 0 && sa.length() == 0 )
                  {
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }

                  // Test the step name
                  if ( sn.length() > 0 )
                  {
                     result = uriValidator.validate(sn);

                     if ( result == DMErrorCodes.SPM_EXCEEDED )
                     {
                        // SPM of step name exceeded, create warning
                        warn = Messages.getString("InteractionValidator.61", mElement, i );  

                        // Add the SPM Exceeded warning to the message log
                        DetailedLogMessageCollection.getInstance().addMessage(new
                           LogMessage(MessageType.WARNING, warn));              
                     }
                     else if ( result != DMErrorCodes.NO_ERROR )
                     {
                        valid = result;
                     }
                  }

                  // Test the step answer
                  if ( valid != DMErrorCodes.TYPE_MISMATCH )
                  {
                     // Test the step answer  
                     idx = sa.indexOf("[:]"); 

                     if ( idx != -1 )
                     {
                        // Make sure there is no second separator
                        int sec = sa.indexOf("[:]", idx + 3); 
                        if ( sec != -1 )
                        {
                           // Too many separators
                           valid = DMErrorCodes.TYPE_MISMATCH;
                        }
                        else
                        {
                           // Test both parts of the range
                           String minString = sa.substring(0, idx);
                           String maxString = sa.substring(idx + 3);

                           try
                           {
                              Double min = null;
                              Double max = null;

                              if ( minString.trim().length() > 0 )
                              {
                                 min = new Double(minString);

                              }

                              if ( maxString.trim().length() > 0 )
                              {
                                 max = new Double(maxString);
                              }

                              if ( min != null && max != null )
                              {
                                 if ( min.doubleValue() > max.doubleValue() )
                                 {
                                    // The range minimum cannot exceed max
                                    valid = DMErrorCodes.TYPE_MISMATCH;
                                 }
                              }
                           }
                           catch ( NumberFormatException nfe )
                           {
                              // Some number format exception
                              valid = DMErrorCodes.TYPE_MISMATCH;
                           }
                        }
                     }
                     else
                     {
                        result = shortSPMValidator.validate(sa);

                        if ( result == DMErrorCodes.SPM_EXCEEDED )
                        {
                           // SPM of step name exceeded, create warning
                           warn = Messages.getString("InteractionValidator.65", mElement, i);  

                           // Add the SPM Exceeded warning to the message log
                           DetailedLogMessageCollection.getInstance().addMessage(new
                              LogMessage(MessageType.WARNING, warn));              
                        }
                        else if ( result != DMErrorCodes.NO_ERROR )
                        {
                           valid = result;
                        }
                     }                    
                  }
               } 
            } 
       
            break;
         }
         case SEQUENCING :
         {
            String array[] = iValue.split(comma);

            // Empty string is not allowed
            if ( array.length == 0 )
            {
               valid = DMErrorCodes.TYPE_MISMATCH;
               break;
            }           

            // Ending with an empty string is also not allowed
            if ( iValue.lastIndexOf("[,]") == iValue.length() - 3 ) 
            {
               valid = DMErrorCodes.TYPE_MISMATCH;
               break;
            }

            // Check to determine if there are more 
            // than 36 elements in this array
            if ( array.length > 36 )
            {
               // Number of identifiers SPM Exceeded, create warning.
               warn = Messages.getString("InteractionValidator.68", mElement); 

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(new
                  LogMessage(MessageType.WARNING, warn));
            }

            if ( !(array.length == 1 && array[0].length() == 0) )
            {          
               for ( int i = 0; i < array.length; i++ )
               {
                  result = uriValidator.validate(array[i]);
   
                  if ( result == DMErrorCodes.SPM_EXCEEDED )
                  {
                     // SPM of step name exceeded, create warning
                     warn = Messages.getString("InteractionValidator.69",  mElement, i);  
   
                     // Add the SPM Exceeded warning to the message log
                     DetailedLogMessageCollection.getInstance().addMessage(new
                        LogMessage(MessageType.WARNING, warn));              
                  }
                  else if ( result != DMErrorCodes.NO_ERROR )
                  {
                     valid = result;
                  }
               }
            }
            else
            {
               // Set of empty URIs is not allowed
               valid = DMErrorCodes.TYPE_MISMATCH;
            }

            break;

         }
         case NUMERIC :  
         {
            if ( mElement.
                 equals("cmi.interactions.n.correct_responses.n.pattern") ) 
            {
               idx = iValue.indexOf("[:]"); 

               if ( idx != -1 )
               {
                  // Make sure there is no second separator
                  int sec = iValue.indexOf("[:]", idx + 3); 
                  if ( sec != -1 )
                  {
                     // Too many separators
                     valid = DMErrorCodes.TYPE_MISMATCH;
                  }
                  else
                  {
                     // Test both parts of the range
                     String minString = iValue.substring(0, idx);
                     String maxString = iValue.substring(idx + 3);

                     try
                     {
                        Double min = null;
                        Double max = null;

                        if ( minString.trim().length() > 0 )
                        {
                           min = new Double(minString);

                        }

                        if ( maxString.trim().length() > 0 )
                        {
                           max = new Double(maxString);
                        }

                        if ( min != null && max != null )
                        {
                           if ( min.doubleValue() > max.doubleValue() )
                           {
                              // The range minimum cannot exceed max
                              valid = DMErrorCodes.TYPE_MISMATCH;
                           }
                        }
                     }
                     catch ( NumberFormatException e )
                     {
                        // Some number format exception
                        valid = DMErrorCodes.TYPE_MISMATCH;
                     }
                  }
               }
               else
               {
                  // No separator
                  valid = DMErrorCodes.TYPE_MISMATCH;
               }
            }
            else
            {
               valid = realValidator.validate(iValue);
            }

            break;
         }
         case UNKNOWN_TYPE :
         {
            // Assume this is always an error
            valid = DMErrorCodes.UNKNOWN_EXCEPTION;

            break;
         }
         default: 
         {
            // Assume this is always an error
            valid = DMErrorCodes.UNKNOWN_EXCEPTION;
         }
      }

      return valid;
   }

} // end InteractionValidator
