/*************************************************************************
 *
 * $RCSfile: IProjectHandler.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/12/06 07:49:24 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.model.language;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.preferences.IOOo;

/**
 * This interface provides a set of methods to perform language 
 * specific tasks on uno-idl projects.
 * 
 * @author cedricbosdo
 */
public interface IProjectHandler {

	/**
	 * Adds the language specific things of the uno project. The projet handle
	 * has to be contained in the provided data.
	 */
	public void configureProject(UnoFactoryData data)
		throws Exception;

	/**
	 * Add a language specific language nature. This one has to
	 * configure the language-specific properties of the project and 
	 * set the builders.
	 * 
	 * @param project the project on which to add the nature. 
	 * 		Must not be null, otherwise the nature won't be added
	 */
	public void addProjectNature(IProject project);

	/**
	 * Adds the language specific OpenOffice.org dependencies to the project.
	 * 
	 * @param ooo the OpenOffice.org instance
	 * @param project the project on which to add the dependencies
	 */
	public void addOOoDependencies(IOOo ooo, IProject project);

	/**
	 * Removes the language specific OpenOffice.org dependencies from the project.
	 * 
	 * @param ooo the OpenOffice.org instance
	 * @param project the project from which to remove the dependencies
	 */
	public void removeOOoDependencies(IOOo ooo, IProject project);
	
	/**
	 * Adds the language specific dependencies for the project. This
	 * is mostly about library path settings. This method althought has
	 * to add the build path to the libraries and the implementation as 
	 * the sources path. 
	 * 
	 * @param unoproject the uno project on which to add the dependencies
	 * @param monitor a progress monitor
	 * @throws CoreException if there is any problem during the operation
	 */
	public void addLanguageDependencies(IUnoidlProject unoproject,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Extracts the language option name to give to the 
	 * <code>uno-skeletonmaker</code>: <code>--java5</code> for Java 1.5 
	 * for example. To know the correct language name, please refer to 
	 * the <code>uno-skeletonmaker</code> help.
	 * 
	 * @param data the Uno factory data from where to extract the value
	 * @return the option or <code>null</code> if uno-skeletonmaker utility
	 * 		doesn't support the language.
	 * 
	 * @throws Exception is thrown if anything wrong happens
	 */
	public String getSkeletonMakerLanguage(UnoFactoryData data) throws Exception;
	
	/**
	 * Extracts the Implementation name of the class that will be generated by
	 * the <code>uno-skeletonmaker</code>. It should never return a 
	 * <code>null</code> value.
	 * 
	 * @param data the uno factory data to extract the implementation name from
	 * @return the implementation name
	 * @throws Exception if anything wrong happens.
	 */
	public String getImplementationName(UnoFactoryData data) throws Exception;

	/**
 	 * Computes the implementation file path from the implementation name.
 	 *
 	 * @param implementationName the implementation name returned by the
 	 * 	  project handler.
 	 * @return a source directory relative path pointing to the file that
 	 * 	  will be generated by <code>uno-skeletonmaker</code>.
 	 */
	public IPath getImplementationFile(String implementationName);
	
	/**
	 * @param prj the uno project from which to get the library path
	 * 
	 * @return the library path, ready to be provided to the 
	 * <code>File</code> class constructor.
	 */
	public String getLibraryPath(IUnoidlProject prj);
}