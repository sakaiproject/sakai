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

import org.adl.datamodels.DMTypeValidator;
import org.adl.datamodels.DMErrorCodes;

import java.util.Vector;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * <br><br>
 * 
 * <strong>Filename:</strong> LangStringValidator.java<br><br>
 * 
 * <strong>Description:</strong><br><br>
 * 
 * <strong>Design Issues:</strong><br><br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class LangStringValidator extends DMTypeValidator implements Serializable
{
   /**
    * Describes the set of all 2 and 3 character country codes and their 
    * corresponding 3 digit country code.
    */
   private final String mISOCountries = 
   ",AD.020AND,AE.784ARE,AF.004AFG,AG.028ATG,AI.660AIA,AM.051ARM,AN.530ANT" +
   ",AO.024AGO,AQ.010ATA,AR.032ARG,AT.040AUT,AU,036AUS,AZ.031AZE,AL.008ALB" +
   ",AS.016ASM,AW.533ABW,BA.070BIH,BI.108BDI,BB.052BRB,BE.056BEL,BF.854BFA" + 
   ",BG.100BGR,BH.048BHR,BS.044BHS,BJ.204BEN,BM.060BMU,BN.096BRN,BO.068BOL" +
   ",BR.076BRA,BD.050BGD,BT.064BTN,BV.074BVT,BW.072BWA,BY.112BLR,BZ.084BLZ" +
   ",CA.124CAN,CC.166CCK,CD.180COD,CF.140CAF,CG.178COG,CH.756CHE,CI.384CIV" +
   ",CK.184COK,CL.152CHL,CM.120CMR,CN.156CHN,CO.170COL,CR.188CRI,CU.192CUB" +
   ",CV.132CPV,CX.162CXR,CY.196CYP,CZ.203CZE,DE.276DEU,DJ.262DJI,DK.208DNK" +
   ",DM.212DMA,DO.214DOM,DZ.012DZA,EC.218ECU,EE.233EST,EG.818EGY,EH.732ESH" +
   ",ER.232ERI,ES.724ESP,ET.231ETH,FI.246FIN,FJ.242FJI,FK.238FLK,FM.583FSM" +
   ",FO.234FRO,FR.250FRA,FX.249FXX,GA.266GAB,GB.826GBR,GD.308GRD,GE.268GEO" + 
   ",GF.254GUF,GH.288GHA,GI.292GIB,GL.304GRL,GM.270GMB,GN.324GIN,GP.312GLP" +
   ",GQ.226GNQ,GR.300GRC,GS.239SGS,GT.320GTM,GU.316GUM,GW.624GNB,GY.328GUY" +
   ",HK.344HKG,HM.334HMD,HN.340HND,HR.191HRV,HT.332HTI,HU.348HUN,ID.360IDN" +
   ",IE.372IRL,IL.376ISR,IN.356IND,IO.086IOT,IQ.368IRQ,IR.364IRN,IS.352ISL" +
   ",IT.380ITA,JM.388JAM,JO.400JOR,JP.392JPN,KE.404KEN,KG.417KGZ,KH.116KHM" +
   ",KI.296KIR,KM.174COM,KN.659KNA,KP.408PRK,KR.410KOR,KW.414KWT,KY.136CYM" +
   ",KZ.398KAZ,LA.418LAO,LB.422LBN,LC.662LCA,LI.438LIE,LK.144LKA,LR.430LBR" +
   ",LS.426LSO,LT.440LTU,LU.442LUX,LV.428LVA,LY.434LBY,MA.504MAR,MC.492MCO" +
   ",MD.498MDA,MG.450MDG,MH.584MHL,MK.807MKD,ML.466MLI,MM.104MMR,MN.496MNG" +
   ",MO.446MAC,MP.580MNP,MQ.474MTQ,MR.478MRT,MS.500MSR,MT.470MLT,MU.480MUS" +
   ",MV.462MDV,MW.454MWI,MX.484MEX,MY.458MYS,MZ.508MOZ,NA.516NAM,NC.540NCL" +
   ",NE.562NER,NF.574NFK,NG.566NGA,NI.558NIC,NL.528NLD,NO.578NOR,NP.524NPL" +
   ",NR.520NRU,NU.570NIU,NZ.554NZL,OM.512OMN,PA.591PAN,PE.604PER,PF.258PYF" +
   ",PG.598PNG,PH.608PHL,PK.586PAK,PL.616POL,PM.666SPM,PR.630PRI,PS.275PSE" +
   ",PT.620PRT,PW.585PLW,PN.612PCN,PY.600PRY,QA.634QAT,RE.638REU,RO.642ROU" +
   ",RU.643RUS,RW.646RWA,SA.682SAU,SB.090SLB,SC.690SYC,SD.736SDN,SE.752SWE" +
   ",SG.702SGP,SH.654SHN,SI.705SVN,SJ.744SJM,SK.703SVK,SL.694SLE,SM.674SMR" +
   ",SN.686SEN,SO.706SOM,SR.740SUR,ST.678STP,SV.222SLV,SY.760SYR,SZ.748SWZ" +
   ",TC.796TCA,TD.148TCD,TF.260ATF,TG.768TGO,TH.764THA,TJ.762TJK,TL.626TLS" +
   ",TK.772TKL,TM.795TKM,TN.788TUN,TO.776TON,TR.792TUR,TT.780TTO,TV.798TUV" +
   ",TW.158TWN,TZ.834TZA,UA.804UKR,UG.800UGA,UM.581UMI,US.840USA,UY.858URY" +
   ",UZ.860UZB,VA.336VAT,VC.670VCT,VE.862VEN,VG.092VGB,VI.850VIR,VN.704VNM" +
   ",VU.548VUT,WF.876WLF,WS.882WSM,YE.887YEM,YT.175MYT,YU.891YUG,ZA.710ZAF" +
   ",ZM.894ZMB,ZW.716ZWE,";


   /**
    * Describes the set of valid 2 and 3 character language codes.
    */
   private final String mISOLanguages = 
   ",aaaar,ababk,aeave,afafr,akaka,amamh,anarg,arara,asasm,avava,ayaym,azaze" +
   ",babak,bebel,bgbul,bhbih,bibis,bmbam,bnben,bobod,brbre,bsbos,cacat,ceche" +
   ",chcha,cocos,csces,crcre,cuchu,cvchv,cycym,dadan,dedeu,dvdiv,dzdzo,eeewe" +
   ",elell,eneng,eoepo,esspa,etest,eueus,fafas,ffful,fifin,fjfij,fofao,frfra" +
   ",fyfry,gagle,gdgla,glglg,gngrn,guguj,gvglv,hahau,heheb,hihin,hrhrv,hthat" +
   ",hohmo,huhun,hyhye,hzher,iaina,idind,ieile,igibo,iiiii,ikipk,ioido"       +
   ",iuiku,jajpn,jvjav,kakat,kgkon,kikik,kjkua,kkkaz,klkal,isisl,itita,kmkhm" +
   ",knkan,kokor,krkau,kskas,kukur,kvkom,kwcor,kykir,lalat,lbltz,lilim,lglug" +
   ",lnlin,lolao,ltlit,lulub,lvlav,mgmlg,mhmah,mimri,mkmkd,mlmal,mnmon,momol" +
   ",mrmar,msmsa,mtmlt,mymya,nanau,nbnob,ndnde,nenep,ngndo,nlnld,nnnno,nonor" +
   ",nrnbl,nvnav,nynya,ococi,ojoji,omorm,orori,ososs,papan,pipli,plpol,pspus" +
   ",ptpor,quque,rmroh,rnrun,roron,rurus,rwkin,sasan,scsrd,sesme,sdsnd,sgsag" +
   ",sisin,skslk,slslv,smsmo,snsna,sosom,sqsqi,srsrp,ssssw,stsot,susun,svswe" +
   ",swswa,tatam,tetel,tgtgk,ththa,titir,tktuk,tltgl,tntsn,toton,trtur,tstso" +
   ",tttat,twtwi,tytah,uguig,ukukr,ururd,uzuzb,veven,vivie,vovol,wawln,wowol" +
   ",xhxho,yiyid,yoyor,zazha,zhzho,zuzul,";
 
   /**
    * Describes the set of valid 3 character language codes
    */
   private final String mISOExLanguages = 
   "ace,ach,ada,ady,afa,afh,akk,alb,ale,alg,ang,apa,arc,arm,arn,arp,art,arw," +
   "ast,ath,aus,awa,bad,bai,bal,ban,baq,bas,bat,bej,bem,ber,bho,bik,bin,bla," +
   "bnt,bra,btk,bua,bug,bur,byn,cad,cai,car,cau,ceb,cel,chb,chg,chi,chk,chm," +
   "chn,cho,chp,chr,chy,cmc,cop,cpe,cpf,cpp,crh,crp,csb,cus,cze,dak,dar,day," +
   "del,den,dgr,din,doi,dsb,dra,dua,dum,dut,dyu,efi,egy,eka,elx,enm,ewo,fan," +
   "fat,fiu,fon,fre,frm,fro,fur,gaa,gay,gba,gem,geo,ger,gez,gil,gmh,goh,gon," +
   "gor,got,grb,grc,gre,gwi,hai,haw,hil,him,hit,hmn,hsb,hup,iba,ice,ijo,ilo," +
   "inc,ine,inh,ira,iro,jbo,jpr,jrb,kaa,kab,kac,kam,kar,kaw,kbd,kha,khi,kho," +
   "kmb,kok,kos,kpe,krc,kro,kru,kum,kut,lad,lah,lam,lez,lol,loz,lua,lui,lun," +
   "luo,lus,mac,mad,mag,mai,mak,man,mao,map,mas,may,mdf,mdr,men,mga,mic,min," +
   "mis,mkh,mnc,mni,mno,moh,mos,mul,mun,mus,mwr,myn,myv,nah,nai,nap,nds,new," +
   "nia,nic,niu,nog,non,nso,nub,nwc,nym,nyn,nyo,nzi,osa,ota,oto,paa,pag,pal," +
   "pam,pap,pau,peo,per,phi,phn,pon,pra,pro,raj,rap,rar,roa,rom,rum,sad,sah," +
   "sai,sal,sam,sas,sat,scc,sco,scr,sel,sem,sga,sgn,shn,sid,sio,sit,sla,slo," +
   "sma,smi,smj,smn,sms,snk,sog,son,srr,ssa,suk,sus,sux,syr,tai,tem,ter,tet," +
   "tib,tig,tiv,tkl,tlh,tli,tmh,tog,tpi,tsi,tum,tup,tut,tvl,tyv,udm,uga,umb," +
   "und,vai,vot,wak,wal,war,was,wel,wen,xal,yao,yap,ypk,zap,zen,znd,zun,"     +
   "fil,mwl,scn,";

   /**
    * Defines the set of IANA approved language codes.
    */
   private final String mIANALangauges = 
   "i,x,art-lojban,az-arab,az-cyrl,az-latn,cel-gaulish,de-1901,de-1996,"  +
   "de-at-1901,de-at-1996,de-ch-1901,de-ch-1996,de-de-1901,de-de-1996,"   +
   "en-boont,en-ge-oed,en-scouse,i-ami,i-bnn,i-default,i-enochian,i-hak," +
   "i-klingon,i-lux,i-mingo,i-navajo,i-pwn,i-tao,i-tay,i-tsu,no-bok,"     +
   "no-nyn,sgn-be-fr,sgn-be-nl,sgn-br,sgn-ch-de,sgn-co,sgn-de,sgn-dk,"    +
   "sgn-es,sgn-fr,sgn-gb,sgn-gr,sgn-ie,sgn-it,sgn-jp,sgn-mx,"             +
   "sgn-nl,sgn-no,sgn-pt,sgn-se,sgn-us,sgn-za,sl-rozaj,sr-cyrl,"          +
   "sr-latn,uz-cyrl,uz-latn,yi-latn,zh-hans,zh-hant,zh-gan,zh-guoyu,"     +
   "zh-hakka,zh-min,zh-min-nan,zh-wuu,zh-xiang,";


   /**
    * Describes if an empty string is allowed to be set
    */
    private boolean mAllowEmpty = false;

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Constructors
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Default constructor
    */
   public LangStringValidator()
   {
      mType = "language_type";
   }


   /**
    * Constructor used in cases where an empty string is allowed
    * 
    * @param iAllowEmpty  Indicates if the language string can be empty.
    */
   public LangStringValidator(boolean iAllowEmpty)
   {
      mType = "language_type";
      mAllowEmpty = iAllowEmpty;
   }


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Compares two valid data model elements for equality.
    * 
    * @param iFirst  The first value being compared.
    * 
    * @param iSecond The second value being compared.
    * 
    * @param iDelimiters
    *                The common set of delimiters associated with the
    *                values being compared.
    * 
    * @return Returns <code>true</code> if the two values are equal, otherwise
    *         <code>false</code>.
    */
   public boolean compare(String iFirst, String iSecond, Vector iDelimiters)
   {

      // Assume equal
      boolean equal = true;

      boolean done = false;
      int curDash1 = -1;
      int curDash2 = -1;

      // Make sure there is something to validate
      if ( iFirst != null && iSecond != null )
      {
         if ( iFirst.trim().equals("") || iSecond.trim().equals("") )
         {
            // If either value is empty, there is nothing to compare
            done = true;

            if ( !iFirst.trim().equals("") || !iSecond.trim().equals("") )
            {
               // But they both have to be empty to be equal
               equal = false;
            }
            else
            {
               if ( !mAllowEmpty )
               {
                  equal = false;
               }
            }
         }

         // Compare the lang codes
         if ( !done )
         {
            String lang1 = iFirst;
            String lang2 = iSecond;

            // Find the first '-'
            curDash1 = iFirst.indexOf("-");
            curDash2 = iSecond.indexOf("-");

            if ( curDash1 != -1 )
            {
               lang1 = iFirst.substring(0, curDash1);
            }

            if ( curDash2 != -1 )
            {
               lang2 = iSecond.substring(0, curDash2);
            }

            // Case insensitive search
            lang1 = lang1.toLowerCase();
            lang2 = lang2.toLowerCase();


            // Do a quick compare
            if ( lang1.equalsIgnoreCase(lang2) )
            {
               // The lang codes match
               done = true;
            }
            else
            {
               // Check "x-"
               if ( !lang1.equals("x") )
               {   
                  // Determine the first lang code
                  int mod1 = 0;
                  if ( lang1.length() == 2 )
                  {
                     // Prepend the ','
                     lang1 = "," + lang1;
                  }
                  else if ( lang1.length() == 3 )
                  {
                     // Remember the string distance MOD
                     mod1 = 3;
   
                     // Append the ','
                     lang1 = lang1 + ",";
                  }
                  else
                  {
                     // lang code too long
                     equal = false;
                     done = true;
                  }
   
                  // Determine the second lang code
                  int mod2 = 0;
                  if ( lang2.length() == 2 )
                  {
                     // Prepend the ','
                     lang2 = "," + lang2;
                  }
                  else if ( lang2.length() == 3 )
                  {
                     // Remember the string distance MOD
                     mod2 = 3;
   
                     // Append the ','
                     lang2 = lang2 + ",";
                  }
                  else
                  {
                     // lang code too long
                     equal = false;
                     done = true;
                  }
   
                  if ( !done )
                  {
                     // Check for locally defined 3 char codes
                     if ( mod1 == 3 && mod2 == 3 )
                     {
                        // Check locally defined names
                        if ( lang1.charAt(0) == 'q' )
                        {
                           if ( lang1.charAt(1) >= 'a' &&
                                lang1.charAt(1) <= 't' )
                           {
                              if ( lang1.charAt(2) >= 'a' &&
                                   lang1.charAt(2) <= 'z' )
                              {
                                 equal = lang1.equals(lang2);
                                 done = true;
                              }
                           }
                        }
                     }
                  }
   
                  if ( !done )
                  {
                     int idx1 = mISOLanguages.indexOf(lang1) - mod1;
                     int idx2 = mISOLanguages.indexOf(lang2) - mod2;
   
                     if ( idx1 < 0 || idx2 < 0 || idx1 != idx2 )
                     {
                        // Look at extended languages
                        idx1 = mISOExLanguages.indexOf(lang1) - mod1;
                        idx2 = mISOExLanguages.indexOf(lang2) - mod2;
   
                        if ( idx1 < 0 || idx2 < 0 || idx1 != idx2 )
                        {
                           // The lang codes do not match
                           equal = false;
                           done = true;
                        }
                     }
                  }
               }
               else
               {
                  // Look for the next '-'
                  curDash1 = iFirst.indexOf("-", 2);
                  if (curDash1 == -1)
                  {
                     lang1 = iFirst.substring(2);
                  }
                  else
                  {
                    lang1 = iFirst.substring(2, curDash1);
                  }
   
                  curDash2 = iSecond.indexOf("-", 2);
                  if (curDash2 == -1)
                  {
                     lang2 = iSecond.substring(2);
                  }
                  else
                  {
                     lang2 = iSecond.substring(2, curDash2);
                  }
   
                  if ( !lang1.equalsIgnoreCase(lang2) )
                  {
                     // The lang codes do not match
                     equal = false;
                     done = true;
                  }
               }
            }
         }

         // Make sure there is more to look at
         if ( curDash1 == -1 || curDash2 == -1 )
         {
            done = true;

            if ( curDash1 != curDash2 )
            {
               equal = false;
            }
         }

         String country1 = null;
         String country2 = null;

         // Compare the country codes
         if ( !done )
         {
            // Increment past the dash
            curDash1++;
            curDash2++;

            // Drop the lang code from the current string
            iFirst = iFirst.substring(curDash1);
            iSecond = iSecond.substring(curDash2);

            country1 = new String(iFirst);
            country2 = new String(iSecond);

            // Make sure there is a string to check
            if ( country1.trim().length() > 0 &&
                 country2.trim().length() > 0 )
            {
               // Look for the next dash
               curDash1 = country1.indexOf("-");
               curDash2 = country2.indexOf("-");

               if ( curDash1 != -1 )
               {
                  country1 = country1.substring(0, curDash1);
               }

               if ( curDash2 != -1 )
               {
                  country2 = country2.substring(0, curDash2);
               }

               // Determine the first country code
               int mod1 = 0;
               if ( country1.length() == 2 )
               {
                  // Prepend the ','
                  country1 = "," + country1;
               }
               else if ( country1.length() == 0 )
               {
                  done = true;
               }
               else
               {
                  // country code too long
                  equal = false;
                  done = true;
               }

               // Determine the second country code
               int mod2 = 0;
               if ( country2.length() == 2 )
               {
                  // Prepend the ','
                  country2 = "," + country2;
               }
               else if ( country2.length() == 3 )
               {
                  // Check if this is an integer country code
                  try
                  {
                     int test = Integer.parseInt(country2);

                     // Remember the string distance MOD
                     mod2 = 3;

                     // Prepend the '.'
                     country2 = "." + country2;
                  }
                  catch ( NumberFormatException nfe )
                  {
                     // Remember the string distance MOD
                     mod2 = 7;

                     // Append the ','
                     country2 = country2 + ",";
                  }
               }
               else if ( country2.length() == 0 )
               {
                  done = true;
               }
               else
               {
                  // country code too long
                  equal = false;
                  done = true;
               }

               if ( !done )
               {
                  // Case insensitive search
                  country1 = country1.toUpperCase();
                  country2 = country2.toUpperCase();

                  int idx1 = mISOCountries.indexOf(country1) - mod1;
                  int idx2 = mISOCountries.indexOf(country2) - mod2;

                  if ( idx1 < 0 || idx2 < 0 || idx1 != idx2 )
                  {
                     // The country codes do not match
                     equal = false;
                     done = true;
                  }
               }
            }
            else
            {
               if ( country1.trim().length() > 0 ||
                    country2.trim().length() > 0 )
               {
                  // One of the string has a country code defined
                  equal = false;
                  done = true;
               }
            }
         }

         // Compare the remaining portion of the string
         if ( !done )
         {
            // Drop the country code from the current string
            String remain1 = iFirst.substring(curDash1);
            String remain2 = iSecond.substring(curDash2);
/*
            // Look for the next dash
            curDash1 = remain1.indexOf("-");
            curDash2 = remain2.indexOf("-");

            if ( curDash1 != -1 )
            {
               remain1 = remain1.substring(curDash1 + 1);
            }
            else
            {
               remain1 = "";
            }

            if ( curDash2 != -1 )
            {
               remain2 = remain2.substring(curDash2 + 1);
            }
            else
            {
               remain2 = "";
            }
*/

            if ( remain1.length() > 0 || remain2.length() > 0 )
            {
               equal = remain1.equals(remain2);
            }
         }
      }
      else
      {
         // Any null value is not equal to anything
         equal = false;
      }

      return equal;
   }


   /**
    * Truncates the value to meet the DataType's SPM
    * 
    * @param  iValue  The value to be truncated
    * 
    * @return Returns the value truncated at the DataType's SPM
    */
   public String trunc(String iValue)
   {
      String newValue = iValue;
      
      if ( iValue.length() <= 250 )
      {
         // Do nothing value is <= to the SPM of 250
      }
      else
      {
         newValue = iValue.substring(0, 250);
      }
      
      return newValue;
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
      // Assume valid
      int valid = DMErrorCodes.NO_ERROR;
      boolean done = false;
      int curDash = -1;

      // Make sure there is something to validate
      if ( iValue != null )
      {
         if ( iValue.trim().equals("") )
         {
            if ( !mAllowEmpty )
            {
               valid =  DMErrorCodes.TYPE_MISMATCH;
            }

            done = true;
         }
         else
         {
            byte[] stringAsBytes = null;

            try
            {
               stringAsBytes = iValue.getBytes("UTF-16");
            }
            catch ( UnsupportedEncodingException use )
            {
               valid =  DMErrorCodes.TYPE_MISMATCH;
               done = true;
            }

            if ( !done )
            {

               byte check = 0;

               // Starting at the 2nd character because of the BOM
               for ( int i = 2; i < stringAsBytes.length; i++ )
               {
                  check |= stringAsBytes[i];
               }

               // Make sure only the first 7 bits are used
               if ( (check & 0x80) == 0x80 )
               {
                  valid =  DMErrorCodes.TYPE_MISMATCH;
                  done = true;
               }
            }
         }

         // Test the lang code
         if ( !done )
         {
            String lang = iValue;
            String iana = iValue.toLowerCase();

            // Find the first '-'
            curDash = iValue.indexOf("-");

            // Extract the language
            if ( curDash != -1 )
            {
               lang = iValue.substring(0, curDash);

               int secDash = iValue.indexOf("-", curDash + 1);
               if ( secDash != -1 )
               {
                  iana = iValue.substring(0, secDash).toLowerCase(); 
               }
            }

            boolean toTest = true;

            // First check for IANA Strings
            int idx = -1;

            if ( iana.length() > 0)
            {
               iana += ",";
               idx = mIANALangauges.indexOf(iana);
            }

            if ( idx != -1 )
            {
               done = true;

               if ( lang.equalsIgnoreCase("x") )
               {
                  if ( iana.length() > 1 )
                  {
                     done = false;
                  }
               }
               else if ( lang.equalsIgnoreCase("i") )
               {
                  if ( lang.length() != 1 )
                  {
                     valid =  DMErrorCodes.TYPE_MISMATCH;
                  }
               }
               else if ( lang.length() == 1 )
               {
                  valid =  DMErrorCodes.TYPE_MISMATCH;
               }
            }

            if ( !done && !lang.equalsIgnoreCase("x")  )
            {
               if ( lang.length() == 2 )
               {
                  // Prepend a ','
                  lang = "," + lang;
               }
               else if ( lang.length() == 3 )
               {
                  String temp = lang.toLowerCase();

                  // Check locally defined names
                  if ( temp.charAt(0) == 'q' )
                  {
                     if ( temp.charAt(1) >= 'a' && temp.charAt(1) <= 't' )
                     {
                        if ( temp.charAt(2) >= 'a' && temp.charAt(2) <= 'z' )
                        {
                           toTest = false;
                        }
                     }
                  }

                  if ( toTest )
                  {
                     // Append a ','
                     lang = lang + ",";
                  }
               }
               else
               {
                  // lang code too long
                  valid =  DMErrorCodes.TYPE_MISMATCH;

                  toTest = false;
                  done = true;
               }
            }
            else
            {
               // All 'x-' strings are valid
               toTest = false;
               done = true;

               curDash++;

               // Drop the special code from the current string
               lang = iValue.substring(curDash);

               // Make sure there is a string to check
               if ( lang.trim().length() > 0 )
               {
                  // Find the next '-'
                  curDash = lang.indexOf("-");
               }
               else
               {
                  // Can't just have 'x-'
                  valid =  DMErrorCodes.TYPE_MISMATCH;
               }
            }

            if ( toTest )
            {
               // Case insensitive search
               lang = lang.toLowerCase();

               int found = mISOLanguages.indexOf(lang);

               if ( found == -1 )
               {
                  // Check the extended strings
                  found = mISOExLanguages.indexOf(lang);

                  if ( found == -1 )
                  {
                     // Invalid lang string
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     done = true;
                  }
               }
            }

            // Make sure there is something else to validate
            if ( curDash == -1 )
            {
               done = true;
            }
         }

         // Test the country code
         if ( !done )
         {
            // Increment past the dash
            curDash++;

            // Drop the lang code from the current string
            String country = iValue.substring(curDash);

            // Make sure there is a string to check
            if ( country.trim().length() > 0 )
            {
               // Look for the next dash
               curDash = country.indexOf("-");

               if ( curDash != -1 )
               {
                  country = country.substring(0, curDash);
               }

               if ( country.length() == 2 )
               {
                  // Prepend the ','
                  country = "," + country;
               }
               else if ( country.length() == 3 )
               {
                  valid =  DMErrorCodes.TYPE_MISMATCH;
                  done = true;

               }
               else if ( country.length() == 0 )
               {
                  done = true;
               }
               else
               {
                  // country code too long
                  valid =  DMErrorCodes.TYPE_MISMATCH;
                  done = true;
               }

               if ( !done )
               {

                  // Case insensitive search
                  country = country.toUpperCase();

                  int found = mISOCountries.indexOf(country);

                  if ( found == -1 )
                  {
                     // Invalid lang string
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     done = true;
                  }
               }
            }
            else
            {
               // Invalid lang string
               valid = DMErrorCodes.TYPE_MISMATCH;
               done = true;
            }
         }
/*
         // Test lengths of additional subcodes
         if ( !done )
         {
            // Step past two dashes -- if they exist
            int idx = iValue.indexOf("-");

            if ( idx != -1 )
            {
               idx = iValue.indexOf("-", idx + 1);
            }

            if ( idx != -1 )
            {
               // Drop the lang code from the current string
               String remain = iValue.substring(idx + 1);

               while ( remain.length() > 0 && !done )
               {
                  String test = remain;
                  idx = remain.indexOf("-");
   
                  if ( idx != -1 )
                  {
                     test = remain.substring(0, idx);
                     remain = remain.substring(idx + 1, remain.length());
                  }
                  else
                  {
                     // No more subcodes
                     remain = "";
                  }

                  if ( !(test.length() > 0 && test.length() < 9) )
                  {
                     // Invalid lang string
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     done = true;                  
                  }
               }
            }
         }
*/
         // Test the SPM
         if ( !done )
         {
            if ( iValue.length() > 250 )
            {
               // Invalid lang string
               valid = DMErrorCodes.SPM_EXCEEDED;
            }
         }
      }
      else
      {
         // A null value can never be valid
         valid = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return valid;
   }

} // end LangStringValidator
