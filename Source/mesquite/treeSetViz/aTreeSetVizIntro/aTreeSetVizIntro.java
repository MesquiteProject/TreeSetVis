/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner, Fred Clarke, and Denise Edwards.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted under the GNU Lesser General
 * Public License, as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version,
 * provided that this entire notice is included in all copies of any
 * software which are or include a copy or modification of this software
 * and in all copies of the supporting documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 * IN NO CASE WILL THESE PARTIES BE LIABLE FOR ANY SPECIAL, INCIDENTAL,
 * CONSEQUENTIAL, OR OTHER DAMAGES THAT MAY RESULT FROM USE OF THIS SOFTWARE.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mesquite.treeSetViz.aTreeSetVizIntro;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aTreeSetVizIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aTreeSetVizIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Serves as an introduction to the tree set visualization package for Mesquite.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "TreeSetVis Package  Introduction";
   	 }
	/*.................................................................................................................*/
  	 public String getVersion() {
		return "3.01";
   	 }
 	/*.................................................................................................................*/
 	/** Returns version for a package of modules*/
 	public String getPackageVersion(){
 		return "3.01";
 	}
 	/*.................................................................................................................*/
 	/** Returns version for a package of modules as an integer*/
 	public int getPackageVersionInt(){
 		return 301;
 	}
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "TreeSetVis Package";
 	}
	/*.................................................................................................................*/
	/** Returns the build number*/
 	public int getPackageBuildNumber(){
 		return 101;
 	}

	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "Amenta, N., St. John, K., Klingner, J., Maddison, W., Maddison, D., Clarke, F., Edwards, D., Guzman, D., Mahindru, R., Ivanov, P., Prabhum, U., Postarnakevich, N., Heath, T., Hillis, D.,  "
 		+ "2014.  Tree Set Visualization: a package for Mesquite. Version 3.01.";
 	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
	/*.................................................................................................................*/
	/** Returns the URL of document shown when splash screen icon touched. By default, returns path to module's manual*/
	public String getSplashURL(){
		String splashP =getPath()+"index.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		else
			return getManualPath();
	}
 	/*.................................................................................................................*/
 	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
 	public String  getHomePhoneNumber(){ 
 		return "http://mesquiteproject.org/packages/tsv/notices.xml";
 	}
}
