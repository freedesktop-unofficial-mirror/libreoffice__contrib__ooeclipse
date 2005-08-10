/*************************************************************************
 *
 * $RCSfile: JavamakerBuilder.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:16 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.builders;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.ooo.OOo;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;

public class JavamakerBuilder extends IncrementalProjectBuilder {
	
	/**
	 * Unique Id of the javamaker builder
	 */
	public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID+".javamaker";

	/**
	 * UNOI-IDL project handled. This is a quick access to the project nature 
	 */
	private UnoidlProject unoidlProject;
	
	
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		unoidlProject = (UnoidlProject)getProject().getNature(OOEclipsePlugin.UNO_NATURE_ID);
		
		IdlcBuilder idlcBuilder = new IdlcBuilder(unoidlProject);
		idlcBuilder.build(FULL_BUILD, args, monitor);
		
		RegmergeBuilder regmergeBuilder = new RegmergeBuilder(unoidlProject);
		regmergeBuilder.build(FULL_BUILD, args, monitor);
		
		generateJava(monitor);
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		return null;
	}
	
	private void generateJava(IProgressMonitor monitor) throws CoreException {
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		// Get a handle on the rdb file
		IFile registryFile = getProject().getFile(getProject().getName() + ".rdb");
		
		if (registryFile.exists()){
			
			try {
			
				UnoidlProject project = (UnoidlProject)getProject().getNature(OOEclipsePlugin.UNO_NATURE_ID);
				SDK sdk = project.getSdk();
				OOo ooo = project.getOOo();
				
				if (null != sdk && null != ooo){
					
					IPath ooTypesPath = new Path (ooo.getOOoHome()).append("/program/types.rdb");
					String firstModule = project.getUnoidlLocation().segment(0);
					
					// HELP quotes are placed here to prevent Windows path names with spaces
					String command = "javamaker -T" + firstModule + ".* -nD -Gc -BUCR " + 
											"-O ." + System.getProperty("file.separator") + 
											         project.getCodeLocation().toOSString() + " " +
											registryFile.getProjectRelativePath().toOSString() + " " +
											"-X\"" + ooTypesPath.toOSString() + "\"";
					
					
					Process process = OOEclipsePlugin.runTool(getProject(), command, monitor);
					
					LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(process.getErrorStream()));
					
					// Only for debugging purpose
					if (null != System.getProperties().getProperty("DEBUG")){
					
						String line = lineReader.readLine();
						while (null != line){
							System.out.println(line);
							line = lineReader.readLine();
						}
					}
					
					process.waitFor();
				}
			} catch (InterruptedException e) {
				// interrupted process: the code generation failed
			} catch (IOException e) {
				// Error whilst reading the error stream
			}
		}
	}
}