==========================
Sakai Internationalization
==========================

All of the legacy Sakai tools and many of the JSF based tools have been
localized and internationalized. Translations are underway in many
languages, as follows:

1) Chinese/Simplified (China)
   Status: released  (updated up to 2.9)
   Translated by: Fudan University
   Contact: 高珺 (gaojun@fudan.edu.cn)
   Local ID: zh_CN
   
2) Korean
   Status: released (last update Sakai 2.5)
   Translated by: Il-hwan Kim
   Contact: n/a (unmaintained)
   Local ID: ko_KR
   
3) Japanese
   Status: released (updated up to 2.9)
   Translated by: Ja Sakai Community
   Contact: Ja Sakai Community (dev@sakaiproject.jp)
   Local ID: ja_JP
   
4) Dutch
   Status: released
   Translated by: n/a
   Contact: Jim Doherty   jim.doherty@gmail.com 
   Local ID: nl_NL
   
5) Danish
   Status: in progress
   Translated by: n/a
   Contact: Kasper Pagels   kasper@pagels.dk
   Local ID: da_DK
   
6) Hebrew 
   Status: ??
   Translated by: n/a
   Contact: Dov Winer      admin@makash.org.il
   Local ID: iw_IL

7) Brazilian Portuguese 
   Status: released 2.5
   Translated by: Eduardo Hideki Tanaka
   Contact: Alessandro Oliveira          aro1976@gmail.com
            Nilton Lessa        nlessa@moleque.com.br
   Local ID: pt_BR
   
8) Portuguese
   Status: released
   Translated by: Nuno Fernandes (http://elearning.ufp.pt)
   Contact: Nuno Fernandes   nuno@ufp.pt
				Feliz Gouveia    fribeiro@ufp.pt
   Local ID: pt_PT
   
9) Slovakian
   Status: ??
   Translated by: n/a
   Contact: Michal Mosovic   salmon@salmon.sk
   Local ID: sk_SK
    
10) Catalan
    Status: released (updated up to 2.9)
    Translated by: Universitat de Lleida (UdL)
    Contact: Alexandre Ballesté (alexandre.balleste@udl.cat)
    Local ID: ca_ES
    
11) Chinese/Traditional (Taiwan)
    Status: 2.6 release
    Translated by: Ivan Ho
    Contact: Ivan Ho (ivan@huric.org)
    Local ID: zh_TW
    
12) French (Canadian)
    Status: released
    Translated by: n/a
    Contact: Vincent Siveton (Vincent.Siveton@crim.ca)
    Local ID: fr_CA
	 
13) Spanish (Spain)
    Status: released (updated up to 2.9)
    Translated by: SAMOO
    Contact: Miguel Carro (farreri.sakai@gmail.com)
    Local ID: es_ES

14) Spanish (Mexico)
    Status: Relying on translation from es_ES locale
    Translated by: n/a
    Contact: Cynthia Gonzalez (gonzalez.cynthia@itesm.mx)
    Contact: Larisa Enriquez Vazquez (larisa@piaget.dgsca.unam.mx)
    Local ID: es_MX

15) Spanish (Argentina)
    Status: ??
    Translated by: n/a
    Contact: Sebastin Barreiro (sbarreiro@gmail.com)
    Local ID: es_AR
    
16) Spanish (Chile)
    Status: ??
    Translated by: n/a
    Contact: Alejandro Fuentes de la Hoz (afd@csol.org)
    Local ID: es_CL
    
17) Russian
    Status: 2.5 partial release
    Translated by: Alexander Glebovsky (glebovsky@rambler.ru)
    Contact: Anna Korsun (akorsun@smolny.org)
             Philip Fedchin (philip@smolny.org) 
    Local ID: ru_RU

18) German
    Status: ??
    Translated by: n/a
    Contact: Wolf Hilzensauer (wolf.hilzensauer@salzburgresearch.at)
    Local ID: de_AT

19) Swedish
    Status: released
    Translated by: n/a
    Contact: Magnus Tagesson (magnus.tagesson@it.su.se)
    Local ID: sv_SE
                
20) Turkish
    Status: 2.8 initial release
    Translated by: n/a
    Contact: Yasin ÖZARSLAN <ozarslan@gmail.com>
    Local ID: tr_TR
                
21) Mongolian
    Status: inquiry on 3/15/2007
    Translated by: n/a
    Contact:  B. Ariunbold (ariunbold@newcomsystems.mn)
    Local ID: mn_MN
                
22) Arabic
    Status: released
    Translated by: n/a
    Contact: Tom Landry (tom.landry@crim.ca)
    Local ID: ar
                
23) Vietnamese
    Status: release for sakai 2.8
    Contact:  Nguyen Hua Phung [phung@cse.hcmut.edu.vn] from HCMUT
              Billy Quoc Pham [bpham@huric.org] from HOSEI University    
    Local ID: vi_VN
                
24) Khmer 
    Status: inquiry on 8/12/2008 (target release 3.0)
    Translated by: n/a
    Contact: Ratana Lim (ratana@ic.ucsb.edu) 
    Local ID:km_KH 
    
25) French (France)
   Status: released (updated up to 2.9)
   Translated by: French Sakai Community
   Contact: Jean-François Lévêque (jean-francois.leveque@upmc.fr)
   Local ID: fr_FR

26) Basque
    Status: released
    Translated by: Daniel Merino, Pablo San Roman 
    Contact: Daniel Merino (daniel.merino@unavarra.es), Pablo San Roman (pablo.sanroman@unavarra.es)
    Local ID: eu
 
27) English (Australian)
    Status: Underway for 10.0
    Translated by: Steve Swinsburg
    Contact: Steve Swinsburg (steve.swinsburg@gmail.com)
    Local ID: en_AU
    
28) English (British)
    Status: Underway for 10.0
    Translated by: Steve Swinsburg
    Contact: Steve Swinsburg (steve.swinsburg@gmail.com)
    Local ID: en_GB
       

The default language locale must be defined at boot time (though this 
can be over-ridden by user preferences), by setting the tomcat 
JAVA_OPTS property as follows:

-- catalina.sh -----------------------------------------------
## Define default language locale: Japanese / Japan
JAVA_OPTS="$JAVA_OPTS -Duser.language=ja -Duser.region=JP"
--------------------------------------------------------------

-- catalina.bat ----------------------------------------------
rem Define default language locale: Japanese / Japan
set JAVA_OPTS=%JAVA_OPTS% -Duser.language=ja -Duser.region=JP
--------------------------------------------------------------
