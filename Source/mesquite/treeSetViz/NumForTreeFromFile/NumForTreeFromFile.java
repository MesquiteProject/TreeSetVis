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
package mesquite.treeSetViz.NumForTreeFromFile;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.duties.NumberForTree;

public class NumForTreeFromFile extends NumberForTree implements ActionListener, KeyListener{
	String scoreFileContents = null;
	String pathToScoreFile = null;
	String directoryOfScoreFile = "";

	int titleLine = -1;
	int firstTreeLine = 1;
	int increment = 1;
	int columnToUse = 1;
	Vector scoreVector=null;
	Parser scoreFileParser;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		scoreVector = new Vector();
		if (!MesquiteThread.isScripting()){ //file dialog to choose 
			MesquiteString dir = new MesquiteString();
			MesquiteString f = new MesquiteString();

			String path = MesquiteFile.openFileDialog("File containing scores", dir, f);
			String d = dir.getValue();
			if (!StringUtil.blank(d) && !StringUtil.blank(f.getValue())) {
				if (!d.endsWith("/")) 
					d += "/";
				directoryOfScoreFile = d;
				boolean success;
				if (getProject().getHomeDirectoryName().equalsIgnoreCase(d)){
					pathToScoreFile = f.getValue();
					String fullPathToList = MesquiteFile.composePath(getProject().getHomeDirectoryName(), pathToScoreFile);
					if (!MesquiteFile.fileExists(fullPathToList))
						return sorry(getName() + " couldn't start because score file not found.");
					success = readScoreFile(fullPathToList);
				}
				else {
					pathToScoreFile = path;
					if (!MesquiteFile.fileExists(path))
						return sorry(getName() + " couldn't start because score file not found.");
					success = readScoreFile(pathToScoreFile);
				}
				if (!success)
					return sorry(getName() + " couldn't start because of a problem with the score file.");
			}
			else 
				return sorry(getName() + " couldn't start because no score file was specified.");
		}
		return true;
	}


	/*.................................................................................................................*/
	private boolean readScoreFile(String path){
		scoreFileContents = MesquiteFile.getFileContentsAsString(path);
		if (scoreFileContents==null)
			return false;
		scoreFileParser = new Parser(scoreFileContents);

		if (!MesquiteThread.isScripting())
			if (!queryOptions( ))
				return false;
		scoreFileParser.setPosition(0);
		String line = scoreFileParser.getRawNextDarkLine();
		long count = 1;
		int linesSinceLastProcessed = increment;
		scoreVector.removeAllElements();
		while (!StringUtil.blank(line)) {
			if (count>=firstTreeLine)
				if  (linesSinceLastProcessed>=increment) {
					Parser lineParser = new Parser(line);
					lineParser.setWhitespaceString("\t");
					lineParser.setPunctuationString("");  // DRM February 2014 - needed so that MrBayes files with values of the form "-3.99e+03" can be read
					String token = lineParser.getTokenNumber(columnToUse);
					MesquiteNumber newNumber = new MesquiteNumber();
					newNumber.setValue(token);
					scoreVector.addElement(newNumber);
					linesSinceLastProcessed=1;
				} else
					linesSinceLastProcessed++;
			count++;
			line = scoreFileParser.getRawNextDarkLine();
		}
		return true;
	}


	/*.................................................................................................................*/
	public String findScoreFileElement(int columnToUse, int firstTreeLine, int increment, int addedIncrement) {
		scoreFileParser.setPosition(0);
		String line = scoreFileParser.getRawNextDarkLine();
		long count = 1;
		int linesSinceLastProcessed = increment;
		while (!StringUtil.blank(line)) {
			if (count>=firstTreeLine)
				if  (linesSinceLastProcessed>=addedIncrement) {
					Parser lineParser = new Parser(line);
					lineParser.setWhitespaceString("\t");
					lineParser.setPunctuationString(""); // DRM February 2014 - needed so that MrBayes files with values of the form "-3.99e+03" can be read
					String token = lineParser.getTokenNumber(columnToUse);
					scoreFileParser.setPosition(0);
					if (StringUtil.blank(token))
						token = "NO SCORE!";
					return token;
				} else
					linesSinceLastProcessed++;
			count++;
			line = scoreFileParser.getRawNextDarkLine();
		}
		scoreFileParser.setPosition(0);
		return "";
	}

	IntegerField columnToUseField;
	IntegerField incrementField;
	IntegerField firstTreeLineField;
	JLabel firstEntry, secondEntry;
	
	/*.................................................................................................................*/
	public void checkFields() {
		int tempColumnToUse = columnToUseField.getValue();
		int tempFirstTreeLine = firstTreeLineField.getValue();
		int tempIncrement = incrementField.getValue();
		String token = findScoreFileElement(tempColumnToUse, tempFirstTreeLine, tempIncrement,tempIncrement);
		firstEntry.setText("First entry: " + token);		
		token = findScoreFileElement(tempColumnToUse, tempFirstTreeLine, tempIncrement,tempIncrement*2);
		secondEntry.setText("Second entry: " + token);		
	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		checkFields();
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		checkFields();
	}

	public void keyTyped(KeyEvent e) {
		checkFields();
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Number from Score File Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Score File Options");

		columnToUseField = dialog.addIntegerField("Column to use", columnToUse, 5,0,10000);
		columnToUseField.addKeyListener(this);
		firstTreeLineField = dialog.addIntegerField("First line to process", firstTreeLine, 5,0,10000);
		firstTreeLineField.addKeyListener(this);
		incrementField = dialog.addIntegerField("Sample every nth line:", increment, 5,0,10000);
		incrementField.addKeyListener(this);
		

		if (parser!=null) {
			dialog.addBlankLine();
			dialog.addLabel("Preview of score file (lines may wrap): ");
			int numRows = 10;
			StringBuffer fileStart = new StringBuffer();
			for (int i=0; i<numRows; i++){
				//fileStart.append("["+(i+1)+"] ");
				String line = scoreFileParser.getRawNextLine();
				if (StringUtil.blank(line))
					fileStart.append("\n");
				else
					fileStart.append(line+"\n");
			}

			TextArea textArea = dialog.addTextAreaVerySmallFont(fileStart.toString(), numRows, 40,TextArea.SCROLLBARS_BOTH);
		}
		dialog.addHorizontalLine(2);
		dialog.addLabel("Preview of entries to be used");
		firstEntry = dialog.addLabel("");	
		secondEntry = dialog.addLabel("");	
		firstEntry.setText("First entry: " +findScoreFileElement(columnToUse, firstTreeLine, increment,increment));
		secondEntry.setText("Second entry: " +findScoreFileElement(columnToUse, firstTreeLine, increment,increment*2));
		

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			columnToUse = columnToUseField.getValue();
			firstTreeLine = firstTreeLineField.getValue();
			increment = incrementField.getValue();
		}
		dialog.dispose();
		parser.setPosition(0);
		return (buttonPressed.getValue()==0) ;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("setColumnToUse " + columnToUse);
		temp.addLine("setFirstTreeLine " + firstTreeLine);
		temp.addLine("setIncrement " + increment);

		temp.addLine("setScoreFilePath " + StringUtil.tokenize(MesquiteFile.decomposePath(getProject().getHomeDirectoryName(), pathToScoreFile))); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the path to the file containing tree scores", "[path to file; if relative, should be relative to home file of project]", commandName, "setScoreFilePath")){
			pathToScoreFile = ParseUtil.getFirstToken(arguments, pos);
			boolean success = readScoreFile(MesquiteFile.composePath(getProject().getHomeDirectoryName(), pathToScoreFile));
			if (!success)
				iQuit();
			parametersChanged();
		} 
		else	if (checker.compare(this.getClass(), "Sets the increment for lines to be sampled", "[increment]", commandName, "setIncrement")){
			int line = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(line) && line>0)
				increment = line;
		} 
		else	if (checker.compare(this.getClass(), "Sets the line containing the first score", "[first score line]", commandName, "setFirstTreeLine")){
			int line = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(line) && line>0)
				firstTreeLine = line;
		} 
		else	if (checker.compare(this.getClass(), "Sets the column to use", "[column to use]", commandName, "setColumnToUse")){
			int column = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(column) && column>0)
				columnToUse = column;
		} 


		else
			return  super.doCommand(commandName, arguments, checker);

		return null;
	}

	/* ................................................................................................................. */
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || tree == null)
			return;
		clearResultAndLastResult(result);

		int treeNum = tree.getFileIndex();
		if (MesquiteLong.isCombinable(treeNum)) {
			if (treeNum>=0 && treeNum<scoreVector.size())
				result.setValue((MesquiteNumber)scoreVector.get(treeNum));
		}

		if (resultString != null) {
			resultString.setValue("Number from Score File : " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/* ................................................................................................................. */

	public String getName() {
		return "Number for Tree from Score File [TSV]";
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() { return false; }
	/* ................................................................................................................. */

	public String getExplanation() {
		return "Reads in a tab-delimited text file, and reads a chosen column as the source of scores for trees.";
	}






}
