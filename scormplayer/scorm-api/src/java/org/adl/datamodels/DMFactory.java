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
******************************************************************************/

package org.adl.datamodels;

import org.adl.datamodels.ieee.IValidatorFactory;
import org.adl.datamodels.ieee.SCORM_2004_DM;
import org.adl.datamodels.nav.SCORM_2004_NAV_DM;
import org.adl.datamodels.ssp.SSP_DataModel;

/**
 *
 * <strong>Filename:</strong> DMFactory.java<br><br>
 *
 * <strong>Description:</strong><br><br> Factory pattern used to create datamodel
 *                                        objects based on a value passed in 
 *                                        during runtime
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
 *     <li>SCORM 1.2
 *     <li>SCORM 2004
 * </ul>
 *
 * @author ADL Technical Team
 */
public class DMFactory {

	/**
	 * Enumeration of the run-time data model's supported by the SCORM<br>
	 * <br>Unknown
	 * <br><b>-1</b>
	 * <br><br>[DATA MODEL IMPLEMENTATION CONSTANT]
	 */
	public static final int DM_UNKNOWN = -1;

	/**
	 * Enumeration of the run-time data model's supported by the SCORM<br>
	 * <br>SCORM 1.2 Data Model
	 * <br><b>0</b>
	 * <br><br>[DATA MODEL IMPLEMENTATION CONSTANT]
	 */
	// TODO:  Do we need this or not?  if not remove
	//   public final static int DM_SCORM_1_2              =    0;

	/**
	 * Enumeration of the run-time data model's supported by the SCORM<br>
	 * <br>SCORM 2004 Data Model
	 * <br><b>1</b>
	 * <br><br>[DATA MODEL IMPLEMENTATION CONSTANT]
	 */
	public static final int DM_SCORM_2004 = 1;

	/**
	 * Enumeration of the run-time data model's supported by the SCORM<br>
	 * <br>SCORM 2004 Navigation Data Model
	 * <br><b>2</b>
	 * <br><br>[DATA MODEL IMPLEMENTATION CONSTANT]
	 */
	public static final int DM_SCORM_NAV = 2;

	/**
	 * Enumeration of the run-time data model's supported by the SCORM<br>
	 * <br>IMS SSP Data Model
	 * <br><b>3</b>
	 * <br><br>[DATA MODEL IMPLEMENTATION CONSTANT]
	 */
	public static final int DM_SSP = 3;

	/**
	 * Builds the appropriate datamodel based on the type requested.
	 * 
	 * @param iType enumerated type of datamodel element
	 * <br>
	 * <ul>
	 *    <li>SCORM 2004 DM = 1</li>
	 *    <li>SCORM NAV = 2</li>
	 *    <li>IMS SSP = 3</li>
	 * </ul>
	 * 
	 * @return The appropriate datamodel.
	 */
	public static DataModel createDM(int iType, IValidatorFactory validatorFactory) {
		DataModel dm = null;

		switch (iType) {
		case DM_SCORM_2004: {
			dm = new SCORM_2004_DM();
			((SCORM_2004_DM)dm).init(validatorFactory);
			break;
		}
		case DM_SCORM_NAV: {
			dm = new SCORM_2004_NAV_DM();
			break;
		}
		case DM_SSP: {
			dm = new SSP_DataModel();
			break;
		}
		default: {
			// Do nothing -- this is an error
		}
		}
		return dm;
	}

} // end DMFactory
