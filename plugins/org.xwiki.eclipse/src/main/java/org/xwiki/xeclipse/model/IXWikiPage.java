/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.xwiki.xeclipse.model;

import java.util.Date;

/**
 * This interface provides access to all page data and related information.
 */
public interface IXWikiPage
{
    public String getId();
    public int getLocks();
    public String getParentId();
    public String getSpace();
    public String getTitle();
    public String getUrl();
    public String getCreator();       
    public String getContentStatus();
    public int getVersion();
    public Date getModified();
    public String getModifier();
    
    public String getContent();
    public void setContent(String content);
    
    /**
     * @return true if the page has been modified locally but not yet synchronized with the remote XWiki instance.
     * @throws XWikiConnectionException 
     */
    public boolean isDirty();
    
    /**
     * @return true if the page has been modified both locally and remotely.
     * @throws XWikiConnectionException 
     */
    public boolean isConflict();
    
    /**
     * Save the page content.
     * 
     * @throws XWikiConnectionException
     */
    public void save() throws XWikiConnectionException;
}