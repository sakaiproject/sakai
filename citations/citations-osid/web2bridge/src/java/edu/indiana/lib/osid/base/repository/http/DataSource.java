package edu.indiana.lib.osid.base.repository.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataSource {

	public  String  dataSource;
  private boolean dataSourceFound;
	private String  citationString = "";
	private String  volumeToken = "";
	private String  issueToken = "";
	private String  dateToken = "";
	private String  yearToken = "";
	private String  pagesToken = "";
	private String  sourceTitleToken = "";
	private String  regularExp = "";

	private int replaceStartToken = 1;
	private int replaceEndToken = 1;

	private static final String JSTOR = "jstor";
	private static final String JSTOR_REG_EXP = "(.+,)?Vol\\. \\d+, No\\. (\\d+)(/\\d+)?(, .*)? \\((.*)?\\d{4}\\), (pp\\.|p\\.) \\d+(-\\d+)?";
	
	private static final String PsycINFO = "PsycINFO";
	private static final String PsycINFO_REG_EXP = "(.+)?( Vol \\d+\\(\\d+\\),)?( \\(Vol\\. \\d+\\).)?((\\(\\d{4}\\)|\\w{3} \\d{4}))?(.|,)? (pp\\. \\d+-\\d+|\\d+ pp\\.|\\(pp\\. \\d+-\\d+\\).) (.+)?";
	
	private static final String EBSCOERIC = "ERIC (EBSCO)";
	private static final String EBSCOERIC_REG_EXP = ".+, v\\d+ n\\d+ p\\d+(-\\d+)? (.+)? \\d{4} \\(.+\\)";
	
	private static final String Blackwell = "Blackwell Publishing";
	private static final String BLACKWELL_REG_EXP = "Volume \\d+(, Issue \\d+)?(, .*)?, Page \\d+-\\d+, (.*)?\\d{4}";
	
	private static final String PUBMED = "PubMed";
  private static final String PUBMED_REG_EXP = "(.+)?. \\d{4}(.+)?;\\d+\\(\\d+( .+)?\\):\\d+-\\d+.";
	
	private static final String ProjectMuse = "Project Muse";
	private static final String ProjectMuse_REG_EXP = ".+,( Volume)? \\d+, Number \\d+,( .+)? \\d{4}, pp. \\d+-\\d+";

	private static final String ComputerMusicJournal = "Academic Search (EBSCO)";
	private static final String ComputerMusicJournal_REG_EXP = ".+, \\w{3}\\d{4}, Vol\\. \\d+ Issue \\d+(/\\d+)?, p\\d+-\\d+,.+";

	private static final String ScienceDirect = "Science Direct";
	private static final String ScienceDirect_REG_EXP = ".+, Volume \\d+, Issue \\d+,( \\d+)? (.+)? \\d{4}, Pages \\d+-\\d+";

	private static final String CSAIlluminaERIC = "ERIC (CSA)";
	private static final String CSAIlluminaERIC_REG_EXP = ".+; v\\d+ n\\d+ p\\d+(-\\d+)? (.+)? \\d{4}";

	private static final String ISIZoologicalRecord = "Zoological Record";
	private static final String ISIZoologicalRecord_REG_EXP = "(.+)?( \\d+ \\(.+\\) :)? \\d+-\\d+ (: (.+) )?\\d{4}";

	private static final String OvidBooks = "Ovid Books";
	private static final String OvidBooks_REG_EXP = "(.+)? \\(\\d+(.+)?\\)";

	private static final String Factiva = "Factiva";
	private static final String Factiva_REG_EXP = "(.+)?, \\d+ (.+)? \\d{4}, (.+)?";

	private static final String LexisNexisAcademic = "Lexis-Nexis Academic";
	private static final String LexisNexisAcademic_REG_EXP = "(.+, )?(.+)?\\w+ \\d+, \\d{4}( .+)?,(.+)?((.+)?Pg\\.(.+)?(\\w+)?\\d+,)?(.+)?";

	private static final String FirstSearchWorldCat = "WorldCat";
	private static final String FirstSearchWorldCat_REG_EXP ="(.+)?";
				
	
	public DataSource(String dataSourceCode, String citation) 
	{
		this.dataSource = dataSourceCode;
    dataSourceFound = true;
	
		if (this.isJSTOR()) {
			initJstor(citation);

		} else if (this.isPsycINFO()) {
			initPsycINFO(citation);

		} else if (this.isEBSCOERIC()) {
			initEBSCOERIC(citation);

		} else if (this.isBlackwell()) {
			initBlackwell(citation);

		} else if (this.isPubMed()) {
			initPubMed(citation);

		} else if (this.isProjectMuse()) {
			initProjectMuse(citation);

		} else if (this.isComputerMusicJournal()) {
			initComputerMusicJournal(citation);

		} else if (this.isScienceDirect()) {
			initScienceDirect(citation);

		} else if (this.isCSAIlluminaERIC()) {
			initCSAIlluminaERIC(citation);

		} else if (this.isISIZoologicalRecord()) {
			initISIZoologicalRecord(citation);

		} else if (this.isFirstSearchWorldCat()) {
			initFirstSearchWorldCat(citation);

		}else if (this.isOvidBooks()) {
			initOvidBooks(citation);

		} else if (this.isFactiva()) {
			initFactiva(citation);

		} else if (this.isLexisNexisAcademic()) {
			initLexisNexisAcademic(citation);
		
		} else {
		  dataSourceFound = false;
		}
	}

	private void initJstor(String citation) {
		this.setCitationString(citation);
		// (.+,)?Vol\\. \\d+, No\\. (\\d+)(/\\d+)?(, .*)? \\((.*)?\\d{4}\\), (pp\\.|p\\.) \\d+(-\\d+)?
		this.setRegularExp(DataSource.JSTOR_REG_EXP);
		this.setVolumeToken("Vol\\. \\d+");
		this.setIssueToken("No\\. (\\d+)(/\\d+)?");
		this.setDateToken("\\((.*)?\\d{4}\\)");
		this.setYearToken("\\s\\d{4}\\)");
		this.setPagesToken("(pp\\.|p\\.) \\d+(-\\d+)?");
	}

	private void initBlackwell(String citation) {
		this.setCitationString(citation);
		// Volume \\d+(, Issue \\d+)?(, .*)?, Page \\d+-\\d+, (.*)?\\d{4}
		this.setRegularExp(DataSource.BLACKWELL_REG_EXP);
		this.setVolumeToken("Volume \\d+");
		this.setIssueToken("Issue \\d+");
		this.setDateToken("(.*)?\\d{4}");
		this.setPagesToken("Page \\d+-\\d+");

	}

	private void initPsycINFO(String citation) {
		this.setCitationString(citation);
		// (.+)?( Vol \\d+\\(\\d+\\),)?( \\(Vol\\.
		// \\d+\\).)?((\\(\\d{4}\\)|\\w{3} \\d{4}))?(.|,)? (pp\\. \\d+-\\d+|\\d+
		// pp\\.|\\(pp\\. \\d+-\\d+\\).) (.+)?
		this.setRegularExp(DataSource.PsycINFO_REG_EXP);
		this.setVolumeToken("Vol \\d+");
		this.setIssueToken("\\(\\d+\\)");
		this.setDateToken("\\d{4}(\\(\\d{4}\\)|\\w{3} \\d{4})");
		this.setPagesToken("\\d+-\\d+");
	}

	private void initEBSCOERIC(String citation) {
		this.setCitationString(citation);
		// .+, v\\d+ n\\d+ p\\d+(-\\d+)? (.+)? \\d{4} \\(.+\\)
		this.setRegularExp(DataSource.EBSCOERIC_REG_EXP);
		this.setVolumeToken("v\\d+");
		this.setIssueToken("n\\d+");
		this.setDateToken("(.+)? \\d{4}");
		this.setPagesToken("p\\d+(-\\d+)?");

	}

	private void initPubMed(String citation) {
		this.setCitationString(citation);
		// (.+)?. \\d{4}(.+)?;\\d+\\(\\d+( .+)?\\):\\d+-\\d+.
		this.setRegularExp(DataSource.PUBMED_REG_EXP);
		this.setVolumeToken("\\d+\\");
		this.setIssueToken("(\\d+( .+)?\\)");
		this.setDateToken("\\d{4}(.+)?");
		this.setPagesToken("\\d+-\\d+.");
	}

	private void initProjectMuse(String citation) {
		this.setCitationString(citation);
		// .+,( Volume)? \\d+, Number \\d+,( .+)? \\d{4}, pp. \\d+-\\d+
		this.setRegularExp(DataSource.ProjectMuse_REG_EXP);
		this.setVolumeToken("( Volume)? \\d+");
		this.setIssueToken("Number \\d+");
		this.setDateToken("( .+)? \\d{4}");
		this.setPagesToken("pp. \\d+-\\d+");
	}

	private void initComputerMusicJournal(String citation) {
		this.setCitationString(citation);
		// .+, \\w{3}\\d{4}, Vol\\. \\d+ Issue \\d+(/\\d+)?, p\\d+-\\d+,.+
		this.setRegularExp(DataSource.ComputerMusicJournal_REG_EXP);
		this.setVolumeToken("Vol\\. \\d+");
		this.setIssueToken("Issue \\d+(/\\d+)?");
		this.setDateToken("\\w{3}\\d{4}");
		this.setPagesToken("p\\d+-\\d+");
	}

	private void initScienceDirect(String citation) {
		this.setCitationString(citation);
		// .+, Volume \\d+, Issue \\d+,( \\d+)? (.+)? \\d{4}, Pages \\d+-\\d+
		this.setRegularExp(DataSource.ScienceDirect_REG_EXP);
		this.setVolumeToken("Volume \\d+");
		this.setIssueToken("Issue \\d+");
		this.setDateToken("( \\d+)? (.+)? \\d{4}");
		this.setPagesToken("Pages \\d+-\\d+");
	}

	private void initCSAIlluminaERIC(String citation) {
		this.setCitationString(citation);
		// .+; v\\d+ n\\d+ p\\d+(-\\d+)? (.+)? \\d{4}
		this.setRegularExp(DataSource.CSAIlluminaERIC_REG_EXP);
		this.setVolumeToken("v\\d+");
		this.setIssueToken("n\\d+");
		this.setDateToken("(.+)? \\d{4}");
		this.setPagesToken("p\\d+(-\\d+)?");
	}

	private void initISIZoologicalRecord(String citation) {
		this.setCitationString(citation);
		// (.+)?( \\d+ \\(.+\\) :)? \\d+-\\d+ (: (.+) )?\\d{4}
		this.setRegularExp(DataSource.ISIZoologicalRecord_REG_EXP);
		this.setVolumeToken("\\d+");
		this.setIssueToken("\\(.+\\)");
		this.setDateToken("\\d{4}");
		this.setPagesToken("\\d+-\\d+");
	}
	
	private void initFirstSearchWorldCat(String citation) {
		this.setCitationString(citation);
		// (.+)?
		this.setRegularExp(DataSource.FirstSearchWorldCat_REG_EXP);
		this.setSourceTitleToken("(.+)?");
		this.setDateToken("\\d{4}");
		this.setPagesToken("\\d+-\\d+");
	}

	private void initOvidBooks(String citation) {
		this.setCitationString(citation);
		// (.+)? \\(\\d+(.+)?\\)
		this.setRegularExp(DataSource.OvidBooks_REG_EXP);
		this.setVolumeToken("");
		this.setIssueToken("");
		this.setDateToken("\\d{4}");
		this.setPagesToken("\\d+-\\d+");
	}

	private void initFactiva(String citation) {
		this.setCitationString(citation);
		// (.+)?,( \\d+)? (.+)? \\d{4}, (.+)?
		this.setRegularExp(DataSource.Factiva_REG_EXP);
		this.setDateToken("\\d+ (.+)? \\d{4}");
		this.setPagesToken("\\d+-\\d+");
		this.setReplaceStartToken(0);
		this.setReplaceEndToken(0);

	}

	private void initLexisNexisAcademic(String citation) {
		 
		this.setCitationString(citation);
		//(.+, )?(.+)?\\w+ \\d+, \\d{4}( .+)?,(.+)?( Pg\\. (\\w)?\\d+,)?(.+)?
		
		this.setRegularExp(DataSource.LexisNexisAcademic_REG_EXP);
		this.setSourceTitleToken("(.+, )?(.+)?\\w+ \\d+, \\d{4}( .+)?,");
		this.setDateToken("\\w+ \\d+, \\d{4}?");
		this.setReplaceStartToken(0);
		this.setReplaceEndToken(0);
		//this.setPagesToken("Pg\\. (\\w)?\\d+");
		this.setPagesToken("Pg\\.(.+)?(\\w+)?\\d+,");
		
	}

	public boolean findRegExp() {
		boolean found = false;
		Pattern pattern;
		Matcher matcher;

    if (!dataSourceFound)
    {
      return false;
    }
    
		pattern = Pattern.compile(this.getRegularExp());
		matcher = pattern.matcher(this.getCitationString());

		if (matcher.find()) {
			found = true;
		}

		return found;
	}

	private boolean isJSTOR() {
		return this.dataSource.equalsIgnoreCase(JSTOR);
	}

	private boolean isPsycINFO() {
		return this.dataSource.equalsIgnoreCase(PsycINFO);
	}

	private boolean isEBSCOERIC() {

		return this.dataSource.equalsIgnoreCase(EBSCOERIC);
	}

	private boolean isBlackwell() {

		return this.dataSource.equalsIgnoreCase(Blackwell);
	}

	private boolean isPubMed() {

		return this.dataSource.equalsIgnoreCase(PUBMED);
	}

	private boolean isProjectMuse() {

		return this.dataSource.equalsIgnoreCase(ProjectMuse);
	}

	private boolean isComputerMusicJournal() {

		return this.dataSource.equalsIgnoreCase(ComputerMusicJournal);
	}

	private boolean isScienceDirect() {

		return this.dataSource.equalsIgnoreCase(ScienceDirect);
	}

	private boolean isCSAIlluminaERIC() {

		return this.dataSource.equalsIgnoreCase(CSAIlluminaERIC);
	}

	private boolean isISIZoologicalRecord() {

		return this.dataSource.equalsIgnoreCase(ISIZoologicalRecord);
	}
	
	private boolean isFirstSearchWorldCat() {

		return this.dataSource.equalsIgnoreCase(FirstSearchWorldCat);
	}

	private boolean isOvidBooks() {

		return this.dataSource.equalsIgnoreCase(OvidBooks);
	}

	private boolean isFactiva() {

		return this.dataSource.equalsIgnoreCase(Factiva);
	}

	private boolean isLexisNexisAcademic() {

		return this.dataSource.equalsIgnoreCase(LexisNexisAcademic);
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getDateToken() {
		return dateToken;
	}

	public void setDateToken(String dateToken) {
		this.dateToken = dateToken;
	}
	
	
	public String getYearToken() {
		return yearToken;
	}

	public void setYearToken(String yearToken) {
		this.yearToken = yearToken;
	}
	
	public String getIssueToken() {
		return issueToken;
	}

	public void setIssueToken(String issueToken) {
		this.issueToken = issueToken;
	}

	public String getPagesToken() {
		return pagesToken;
	}

	public void setPagesToken(String pagesToken) {
		this.pagesToken = pagesToken;
	}

	public String getSourceTitleToken() {
		return sourceTitleToken;
	}

	public void setSourceTitleToken(String sourceTitleToken) {
		this.sourceTitleToken = sourceTitleToken;
	}

	public String getVolumeToken() {
		return volumeToken;
	}

	public void setVolumeToken(String volumeToken) {
		this.volumeToken = volumeToken;
	}

	public String getCitationString() {
		return citationString;
	}

	public void setCitationString(String citationRegExp) {
		this.citationString = citationRegExp;
	}

	public String getRegularExp() {
		return regularExp;
	}

	public void setRegularExp(String regularExp) {
		this.regularExp = regularExp;
	}

	public int getReplaceEndToken() {
		return replaceEndToken;
	}

	public void setReplaceEndToken(int replaceEndToken) {
		this.replaceEndToken = replaceEndToken;
	}

	public int getReplaceStartToken() {
		return replaceStartToken;
	}

	public void setReplaceStartToken(int replaceStartToken) {
		this.replaceStartToken = replaceStartToken;
	}

}
