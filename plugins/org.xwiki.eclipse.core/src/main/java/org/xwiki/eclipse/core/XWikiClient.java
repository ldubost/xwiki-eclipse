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
 */
package org.xwiki.eclipse.core;

import org.xwiki.eclipse.rest.storage.XWikiRESTClient;
import org.xwiki.eclipse.storage.AbstractXWikiClient;
import org.xwiki.eclipse.storage.BackendType;
import org.xwiki.eclipse.storage.XWikiEclipseStorageException;
import org.xwiki.eclipse.storage.utils.StorageUtils;
import org.xwiki.eclipse.xmlrpc.storage.XWikiXmlrpcClient;

/**
 * 
 * @version $Id$
 */
public class  XWikiClient
{
    private AbstractXWikiClient client;
    
    public XWikiClient(String serverUrl) {
        
        BackendType backend;
        try {
            backend = StorageUtils.getBackend(serverUrl);
            switch (backend) {
                case XMLRPC:
                    this.client = new XWikiXmlrpcClient(serverUrl);
                    break;
                case REST:
                    this.client = new XWikiRESTClient(serverUrl);
                    break;
                default:
                    break;
            }
        } catch (XWikiEclipseStorageException e) {
            e.printStackTrace();
        }
    }
    
    public boolean login(String username, String password) throws XWikiEclipseException {
        boolean success = this.client.login(username, password);
        
        if (success) {
            return true;
        } else {
            Exception e = new Exception("login fails");
            throw new XWikiEclipseException(e);
        }
    }
    
    public boolean logout() {
        return this.client.logout();
    }
}
