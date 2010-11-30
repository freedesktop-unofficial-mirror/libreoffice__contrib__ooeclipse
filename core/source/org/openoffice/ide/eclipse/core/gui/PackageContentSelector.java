/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2010 by Cédric Bosdonnat
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2010 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.core.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.openoffice.ide.eclipse.core.utils.FilesFinder;
import org.openoffice.plugin.core.model.UnoPackage;

/**
 * Common helper GUI part to select elements to add in the UNO package to be exported.
 * 
 * @author Cedric Bosdonnat
 *
 */
@SuppressWarnings("restriction")
public class PackageContentSelector extends Composite {
    
    private ResourceTreeAndListGroup mResourceGroup;
    private IUnoidlProject mProject;
    
    /**
     * Constructor based on SWT composite's one.
     * 
     * @param pParent the parent composite.
     * @param pStyle the SWT style to give to the composite
     */
    public PackageContentSelector( Composite pParent, int pStyle ) {
        super( pParent, pStyle );
        
        setLayout( new GridLayout( 2, false ) );
        setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        mResourceGroup = new ResourceTreeAndListGroup(this, new ArrayList<Object>(),
                getResourceProvider(IResource.FOLDER | IResource.FILE),
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
                getResourceProvider(IResource.FILE), WorkbenchLabelProvider
                        .getDecoratingWorkbenchLabelProvider(), SWT.NONE,
                DialogUtil.inRegularFontMode(this));
    }
    
    /**
     * Set the project to work on.
     * 
     * @param pPrj the project to show.
     */
    public void setProject(IUnoidlProject pPrj) {
        mProject = pPrj;
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( mProject.getName() );
        mResourceGroup.setRoot( prj );
    }
    
    /**
     * Populate the resource view with some default data (mainly the XCU / XCS files).
     */
    public void loadDefaults( ) {
        // Select the XCU / XCS files by default
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( mProject.getName() );
        FilesFinder finder = new FilesFinder( 
            new String[] { IUnoidlProject.XCU_EXTENSION, IUnoidlProject.XCS_EXTENSION } );
        try {
            finder.addExclude( mProject.getDistFolder().getFullPath() );
            prj.accept( finder );
        } catch (CoreException e) {
            PluginLogger.error("Could not visit the project's content.", e);
        }
        
        ArrayList< IFile > files = finder.getResults();
        for (IFile file : files) {
            mResourceGroup.initialCheckListItem( file );
            mResourceGroup.initialCheckTreeItem( file );
        }
    }
    
    /**
     * @return all the selected items 
     */
    public List<?> getSelected() {
        return mResourceGroup.getAllWhiteCheckedItems();
    }
    
    /**
     * Set the given resources to selected.
     * 
     * @param pSelected the items to select.
     */
    public void setSelected( List<IResource> pSelected ) {
        for (IResource res : pSelected) {
            mResourceGroup.initialCheckTreeItem( res );   
        }
    }
    
    /**
     * Convenience method to create and populate the UnoPackage.
     * 
     * @param pProject the project to export
     * @param pDestFile the file to export to
     * @param pResources the files and folder to add to the OXT
     * 
     * @return the populated package model
     * 
     * @throws Exception if anything goes wrong.
     */
    public static UnoPackage createPackage( IUnoidlProject pProject, File pDestFile, 
                    List<?> pResources ) throws Exception {
        UnoPackage pack = null;
        
        File prjFile = SystemHelper.getFile( pProject );
        
        // Export the library
        IFile library = null;
        ILanguageBuilder langBuilder = pProject.getLanguage().getLanguageBuidler();
        library = langBuilder.createLibrary( pProject );

        // Create the package model
        pack = UnoidlProjectHelper.createMinimalUnoPackage( pProject, pDestFile );
        pack.addToClean( SystemHelper.getFile( library ) );
        
        IFile descrFile = pProject.getFile( IUnoidlProject.DESCRIPTION_FILENAME );
        if ( descrFile.exists() ) {
            File resFile = SystemHelper.getFile( descrFile );
            pack.addContent( UnoPackage.getPathRelativeToBase( resFile, prjFile ),
                            resFile );
        }

        // Add the additional content to the package
        for (Object item : pResources) {
            if ( item instanceof IResource ) {
                File resFile = SystemHelper.getFile( (IResource)item );
                pack.addContent( UnoPackage.getPathRelativeToBase( resFile, prjFile ),
                                resFile );
            }
        }
        
        
        return pack;
    }
    
    /**
     * @param pResourceType the type of the resources to return by the provider.
     * 
     * @return a content provider for <code>IResource</code>s that returns 
     * only children of the given resource type.
     */
    private ITreeContentProvider getResourceProvider( final int pResourceType ) {
        return new WorkbenchContentProvider() {
            public Object[] getChildren( Object pObject ) {
                ArrayList<IResource> results = new ArrayList<IResource>();
                
                if (pObject instanceof ArrayList<?>) {
                    ArrayList<?> objs = (ArrayList<?>)pObject;
                    for (Object o : objs) {
                        if ( o instanceof IResource ) {
                            results.add( ( IResource ) o );
                        }
                    }
                } else if (pObject instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) pObject).members();

                        //filter out the desired resource types
                        for (int i = 0; i < members.length; i++) {
                            //And the test bits with the resource types to see if they are what we want
                            if ((members[i].getType() & pResourceType) > 0 && !isHiddenResource( members[i] ) ) {
                                results.add(members[i]);
                            }
                        }
                    } catch (CoreException e) {
                    }
                }
                return results.toArray( );
            }
        };
    }
    
    /**
     * @param pRes the resource to be checked
     * 
     * @return <code>true</code> if the resource is hidden in the lists, <code>false</code>
     *      otherwise.
     */
    private boolean isHiddenResource( IResource pRes ) {
        boolean hidden = false;
        
        // Hide the binaries: they are always included from somewhere else
        IUnoidlProject unoprj = ProjectsManager.getProject( pRes.getProject().getName() );
        hidden |= unoprj.getFolder( unoprj.getBuildPath() ).equals( pRes );
        
        IFolder[] bins = unoprj.getBinFolders();
        for (IFolder bin : bins) {
            hidden |= bin.equals( pRes );
        }
        
        // Hide the hidden files
        hidden |= pRes.getName().startsWith( "." ); //$NON-NLS-1$
        
        // Hide files which are always included in the package
        hidden |= pRes.getName().equals( IUnoidlProject.DESCRIPTION_FILENAME );
        hidden |= pRes.getName().equals( "MANIFEST.MF" ); //$NON-NLS-1$
        hidden |= pRes.getName().equals( "manifest.xml" ); //$NON-NLS-1$
        hidden |= pRes.getName().equals( "types.rdb" ); //$NON-NLS-1$
        
        return hidden;
    }
}
