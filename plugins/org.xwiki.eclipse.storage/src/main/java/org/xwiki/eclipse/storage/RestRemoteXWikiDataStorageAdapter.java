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
package org.xwiki.eclipse.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.xwiki.eclipse.model.ModelObject;
import org.xwiki.eclipse.model.XWikiEclipseAttachment;
import org.xwiki.eclipse.model.XWikiEclipseClassSummary;
import org.xwiki.eclipse.model.XWikiEclipseComment;
import org.xwiki.eclipse.model.XWikiEclipseObject;
import org.xwiki.eclipse.model.XWikiEclipseObjectProperty;
import org.xwiki.eclipse.model.XWikiEclipseObjectSummary;
import org.xwiki.eclipse.model.XWikiEclipsePage;
import org.xwiki.eclipse.model.XWikiEclipsePageHistorySummary;
import org.xwiki.eclipse.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.model.XWikiEclipseServerInfo;
import org.xwiki.eclipse.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.model.XWikiEclipseTag;
import org.xwiki.eclipse.model.XWikiEclipseWikiSummary;
import org.xwiki.eclipse.rest.Relations;
import org.xwiki.eclipse.rest.RestRemoteXWikiDataStorage;
import org.xwiki.rest.model.jaxb.Attachment;
import org.xwiki.rest.model.jaxb.Attribute;
import org.xwiki.rest.model.jaxb.Comment;
import org.xwiki.rest.model.jaxb.HistorySummary;
import org.xwiki.rest.model.jaxb.Link;
import org.xwiki.rest.model.jaxb.ObjectSummary;
import org.xwiki.rest.model.jaxb.Page;
import org.xwiki.rest.model.jaxb.PageSummary;
import org.xwiki.rest.model.jaxb.Property;
import org.xwiki.rest.model.jaxb.Space;
import org.xwiki.rest.model.jaxb.Syntaxes;
import org.xwiki.rest.model.jaxb.Tag;
import org.xwiki.rest.model.jaxb.Translation;
import org.xwiki.rest.model.jaxb.Wiki;
import org.xwiki.rest.model.jaxb.Xwiki;

/**
 * @version $Id$
 */
public class RestRemoteXWikiDataStorageAdapter implements IRemoteXWikiDataStorage
{
    RestRemoteXWikiDataStorage restRemoteStorage;

    private DataManager dataManager;

    private String endpoint;

    private String username;

    /**
     * @param dataManager
     * @param endpoint
     * @param userName
     * @param password
     */
    public RestRemoteXWikiDataStorageAdapter(DataManager dataManager, String endpoint, String userName, String password)
    {
        this.username = userName;
        this.dataManager = dataManager;
        this.restRemoteStorage = new RestRemoteXWikiDataStorage(endpoint, userName, password);
        this.endpoint = endpoint;
    }

    public List<XWikiEclipseWikiSummary> getWikiSummaries() throws XWikiEclipseStorageException
    {
        try {
            XWikiEclipseServerInfo serverInfo = getServerInfo();

            List<XWikiEclipseWikiSummary> result = new ArrayList<XWikiEclipseWikiSummary>();

            List<Wiki> wikis = restRemoteStorage.getWikis();
            for (Wiki wiki : wikis) {
                XWikiEclipseWikiSummary wikiSummary = new XWikiEclipseWikiSummary(dataManager);
                wikiSummary.setWikiId(wiki.getId());
                wikiSummary.setName(wiki.getName());
                wikiSummary.setVersion(serverInfo.getVersion());
                wikiSummary.setBaseUrl(serverInfo.getBaseUrl());
                wikiSummary.setSyntaxes(serverInfo.getSyntaxes());
                List<Link> links = wiki.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.SPACES)) {
                        wikiSummary.setSpacesUrl(link.getHref());
                        break;
                    }
                }

                result.add(wikiSummary);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XWikiEclipseStorageException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getSpaces()
     */
    @Override
    public List<XWikiEclipseSpaceSummary> getSpaceSummaries(XWikiEclipseWikiSummary wiki)
    {
        List<XWikiEclipseSpaceSummary> result = new ArrayList<XWikiEclipseSpaceSummary>();

        List<Space> spaces = this.restRemoteStorage.getSpaces(wiki.getSpacesUrl());
        if (spaces != null) {
            for (Space space : spaces) {
                XWikiEclipseSpaceSummary summary = new XWikiEclipseSpaceSummary(dataManager);
                summary.setId(space.getId());
                summary.setName(space.getName());
                summary.setUrl(space.getXwikiAbsoluteUrl());
                summary.setWiki(space.getWiki());
                List<Link> links = space.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.PAGES)) {
                        summary.setPagesUrl(link.getHref());
                        break;
                    }
                }

                result.add(summary);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#dispose()
     */
    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getServerInfo()
     */
    @Override
    public XWikiEclipseServerInfo getServerInfo()
    {
        XWikiEclipseServerInfo serverInfo = new XWikiEclipseServerInfo();

        Xwiki xwiki = this.restRemoteStorage.getServerInfo();
        List<Link> links = xwiki.getLinks();
        String syntaxesUrl = null;
        for (Link link : links) {
            if (link.getRel().equals(Relations.SYNTAXES)) {
                syntaxesUrl = link.getHref();
                break;
            }
        }

        Syntaxes syntaxes = this.restRemoteStorage.getSyntaxes(syntaxesUrl);

        List<String> syntaxeList = syntaxes.getSyntaxes();
        serverInfo.setSyntaxes(syntaxeList);

        String versionStr = xwiki.getVersion(); // e.g., 3.1-rc-1
        serverInfo.setVersion(versionStr);
        StringTokenizer tokenizer = new StringTokenizer(versionStr, "-");

        String v = tokenizer.nextToken();

        tokenizer = new StringTokenizer(v, ".");
        serverInfo.setMajorVersion(Integer.parseInt(tokenizer.nextToken()));
        serverInfo.setMinorVersion(Integer.parseInt(tokenizer.nextToken()));

        serverInfo.setBaseUrl(endpoint);

        return serverInfo;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getPageSummaries(org.xwiki.eclipse.model.XWikiEclipseSpaceSummary)
     */
    @Override
    public List<XWikiEclipsePageSummary> getPageSummaries(XWikiEclipseSpaceSummary spaceSummary)
    {
        List<XWikiEclipsePageSummary> result = new ArrayList<XWikiEclipsePageSummary>();

        List<PageSummary> pages = this.restRemoteStorage.getPages(spaceSummary.getPagesUrl());
        for (PageSummary pageSummary : pages) {
            XWikiEclipsePageSummary page = new XWikiEclipsePageSummary(dataManager);
            page.setId(pageSummary.getId());
            page.setName(pageSummary.getName());
            page.setFullName(pageSummary.getFullName());

            page.setParentId(pageSummary.getParentId());
            page.setSpace(pageSummary.getSpace());
            page.setTitle(pageSummary.getTitle());
            page.setUrl(pageSummary.getXwikiAbsoluteUrl());
            page.setWiki(pageSummary.getWiki());
            page.setSyntax(pageSummary.getSyntax());

            List<String> translationLangs = new ArrayList<String>();
            List<Translation> translations = pageSummary.getTranslations().getTranslations();
            if (translations != null && translations.size() > 0) {
                for (Translation translation : translations) {
                    translationLangs.add(translation.getLanguage());
                }
            }
            page.setTranslations(translationLangs);

            List<Link> links = pageSummary.getLinks();
            for (Link link : links) {
                if (link.getRel().equals(Relations.OBJECTS)) {
                    page.setObjectsUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.ATTACHMENTS)) {
                    page.setAttachmentsUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.HISTORY)) {
                    page.setHistoryUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.PAGE)) {
                    page.setPageUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.COMMENTS)) {
                    page.setCommentsUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.TAGS)) {
                    page.setTagsUrl(link.getHref());
                }

                if (link.getRel().equals(Relations.SPACE)) {
                    page.setSpaceUrl(link.getHref());
                }
            }

            result.add(page);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getObjectSummaries(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipseObjectSummary> getObjectSummaries(XWikiEclipsePageSummary pageSummary)
    {
        List<XWikiEclipseObjectSummary> result = new ArrayList<XWikiEclipseObjectSummary>();

        List<ObjectSummary> objects = this.restRemoteStorage.getObjects(pageSummary.getObjectsUrl());

        if (objects != null) {
            for (ObjectSummary objectSummary : objects) {
                XWikiEclipseObjectSummary o = new XWikiEclipseObjectSummary(dataManager);
                o.setClassName(objectSummary.getClassName());
                o.setId(objectSummary.getId());
                o.setPageId(objectSummary.getPageId());
                o.setPageName(objectSummary.getPageName());
                o.setSpace(pageSummary.getSpace());
                o.setWiki(pageSummary.getWiki());

                /* set up pretty name = className + [number] */
                String className = objectSummary.getClassName();
                int number = objectSummary.getNumber();
                String prettyName = className + "[" + number + "]";
                o.setPrettyName(prettyName);

                /* set up the number */
                o.setNumber(number);

                /* set up propertiesUrl */
                List<Link> links = objectSummary.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.PROPERTIES)) {
                        o.setPropertiesUrl(link.getHref());
                    }

                    if (link.getRel().equals(Relations.OBJECT)) {
                        o.setObjectUrl(link.getHref());
                    }
                }

                result.add(o);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getAttachments(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipseAttachment> getAttachments(XWikiEclipsePageSummary pageSummary)
    {
        List<XWikiEclipseAttachment> result = new ArrayList<XWikiEclipseAttachment>();

        List<Attachment> attachments = this.restRemoteStorage.getAttachments(pageSummary.getAttachmentsUrl());
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                XWikiEclipseAttachment a = new XWikiEclipseAttachment(dataManager);
                a.setAbsoluteUrl(attachment.getXwikiAbsoluteUrl());
                a.setAuthor(attachment.getAuthor());
                a.setDate(attachment.getDate());
                a.setId(attachment.getId());
                a.setMimeType(attachment.getMimeType());
                a.setName(attachment.getName());
                a.setPageId(attachment.getPageId());
                a.setPageVersion(attachment.getPageVersion());
                a.setVersion(attachment.getVersion());

                List<Link> links = attachment.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.ATTACHMENT_DATA)) {
                        a.setAttachmentUrl(link.getHref());
                    }

                    if (link.getRel().equals(Relations.PAGE)) {
                        a.setPageUrl(link.getHref());
                    }
                }

                result.add(a);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getPageHistorySummaries(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipsePageHistorySummary> getPageHistorySummaries(XWikiEclipsePageSummary pageSummary)
        throws XWikiEclipseStorageException
    {
        List<XWikiEclipsePageHistorySummary> result = new ArrayList<XWikiEclipsePageHistorySummary>();

        List<HistorySummary> history = this.restRemoteStorage.getPageHistory(pageSummary.getHistoryUrl());
        if (history != null) {
            for (HistorySummary historySummary : history) {
                XWikiEclipsePageHistorySummary h = new XWikiEclipsePageHistorySummary(dataManager);
                h.setPageId(historySummary.getPageId());
                h.setLanguage(historySummary.getLanguage());
                h.setMajorVersion(historySummary.getMajorVersion());
                h.setMinorVersion(historySummary.getMinorVersion());
                h.setModified(historySummary.getModified());
                h.setModifier(historySummary.getModifier());
                h.setName(historySummary.getName());
                h.setSpace(historySummary.getSpace());
                h.setVersion(historySummary.getVersion());
                h.setWiki(historySummary.getWiki());
                List<Link> links = historySummary.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.PAGE)) {
                        h.setPageUrl(link.getHref());
                        break;
                    }
                }

                result.add(h);
            }
        }

        return result;

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getClasses(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public XWikiEclipseClassSummary getPageClassSummary(XWikiEclipsePageSummary pageSummary)
    {
        XWikiEclipsePage page = getPage(pageSummary);

        org.xwiki.rest.model.jaxb.Class classSummary = this.restRemoteStorage.getPageClass(page.getPageClassUrl());

        XWikiEclipseClassSummary result = new XWikiEclipseClassSummary(dataManager);
        result.setId(classSummary.getId());
        result.setName(classSummary.getName());

        List<Link> links = classSummary.getLinks();
        for (Link link : links) {
            if (link.getHref().equals(Relations.OBJECTS)) {
                result.setObjectsUrl(link.getHref());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getTags(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipseTag> getTags(XWikiEclipsePageSummary pageSummary)
    {
        List<XWikiEclipseTag> result = new ArrayList<XWikiEclipseTag>();

        List<Tag> tags = this.restRemoteStorage.getTags(pageSummary.getTagsUrl());

        if (tags != null) {
            for (Tag tag : tags) {
                XWikiEclipseTag t = new XWikiEclipseTag(dataManager);
                t.setName(tag.getName());
                List<Link> links = tag.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.TAG)) {
                        t.setUrl(link.getHref());
                    }
                }

                result.add(t);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getComments(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public List<XWikiEclipseComment> getComments(XWikiEclipsePageSummary pageSummary)
    {
        List<XWikiEclipseComment> result = new ArrayList<XWikiEclipseComment>();
        List<Comment> comments = this.restRemoteStorage.getComments(pageSummary.getCommentsUrl());

        if (comments != null && comments.size() > 0) {
            for (Comment comment : comments) {
                XWikiEclipseComment c = new XWikiEclipseComment(dataManager);
                c.setAuthor(comment.getAuthor());
                c.setDate(comment.getDate());
                c.setHighlight(comment.getHighlight());
                c.setId(comment.getId());
                c.setText(comment.getText());
                c.setPageId(comment.getPageId());
                c.setReplyTo(comment.getReplyTo());

                /* add pageUrl attribute */
                List<Link> links = comment.getLinks();
                for (Link link : links) {
                    if (link.getRel().equals(Relations.PAGE)) {
                        c.setPageUrl(link.getHref());
                    }
                }

                result.add(c);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getObjectProperties(org.xwiki.eclipse.model.XWikiEclipseObjectSummary)
     */
    @Override
    public List<XWikiEclipseObjectProperty> getObjectProperties(XWikiEclipseObjectSummary objectSummary)
    {
        List<XWikiEclipseObjectProperty> result = new ArrayList<XWikiEclipseObjectProperty>();

        List<Property> properties = this.restRemoteStorage.getObjectProperties(objectSummary.getPropertiesUrl());

        for (Property property : properties) {
            XWikiEclipseObjectProperty p = new XWikiEclipseObjectProperty(dataManager);
            p.setName(property.getName());
            p.setType(property.getType());
            p.setValue(property.getValue());
            Map<String, String> attributeMap = new HashMap<String, String>();

            List<Attribute> attributes = property.getAttributes();
            for (Attribute attribute : attributes) {
                attributeMap.put(attribute.getName(), attribute.getValue());
            }

            p.setAttributes(attributeMap);

            result.add(p);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#download(java.lang.String, java.util.List)
     */
    @Override
    public void download(String dir, XWikiEclipseAttachment attachment)
    {
        if (attachment != null) {
            restRemoteStorage.download(dir, attachment.getAbsoluteUrl(), attachment.getName());
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getPage(org.xwiki.eclipse.model.ModelObject)
     */
    @Override
    public XWikiEclipsePage getPage(ModelObject o)
    {
        String pageUrl = null;

        if (o instanceof XWikiEclipseAttachment) {
            XWikiEclipseAttachment attachment = (XWikiEclipseAttachment) o;
            pageUrl = attachment.getPageUrl();
        }

        if (o instanceof XWikiEclipsePageHistorySummary) {
            XWikiEclipsePageHistorySummary pageHistory = (XWikiEclipsePageHistorySummary) o;
            pageUrl = pageHistory.getPageUrl();
        }

        if (o instanceof XWikiEclipsePageSummary) {
            XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) o;
            pageUrl = pageSummary.getPageUrl();
        }

        if (o instanceof XWikiEclipseComment) {
            XWikiEclipseComment comment = (XWikiEclipseComment) o;
            pageUrl = comment.getPageUrl();
        }

        Page page = restRemoteStorage.getPage(pageUrl);
        XWikiEclipsePage result = new XWikiEclipsePage(dataManager);
        result.setFullName(page.getFullName());
        result.setId(page.getId());
        result.setName(page.getName());
        result.setParentId(page.getParentId());
        result.setSpace(page.getSpace());
        result.setSyntax(page.getSyntax());
        result.setTitle(page.getTitle());
        result.setWiki(page.getWiki());

        List<Link> links = page.getLinks();
        for (Link link : links) {
            if (link.getRel().equals(Relations.CLASS)) {
                result.setPageClassUrl(link.getHref());
            }
        }

        for (Link link : links) {
            if (link.getRel().equals(Relations.SPACE)) {
                result.setSpaceUrl(link.getHref());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getObject(org.xwiki.eclipse.model.XWikiEclipseObjectSummary)
     */
    @Override
    public XWikiEclipseObject getObject(XWikiEclipseObjectSummary objectSummary)
    {
        XWikiEclipseObject result = new XWikiEclipseObject(dataManager);

        String objectUrl = objectSummary.getObjectUrl();

        org.xwiki.rest.model.jaxb.Object object = restRemoteStorage.getObject(objectUrl);
        result.setName(object.getId());

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#removeComment(org.xwiki.eclipse.model.XWikiEclipseComment)
     */
    @Override
    public void removeComment(XWikiEclipseComment comment)
    {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#storeComment(org.xwiki.eclipse.model.XWikiEclipseComment)
     */
    @Override
    public XWikiEclipseComment storeComment(XWikiEclipseComment c)
    {
        Comment comment = new Comment();
        comment.setAuthor(c.getAuthor());
        comment.setDate(c.getDate());
        comment.setHighlight(c.getHighlight());
        comment.setPageId(c.getPageId());
        comment.setReplyTo(c.getReplyTo());
        comment.setText(c.getText());

        String commentsUrl = c.getPageUrl() + "/comments";
        Comment stored = restRemoteStorage.storeComment(commentsUrl, comment);

        XWikiEclipseComment result = new XWikiEclipseComment(dataManager);
        result.setAuthor(stored.getAuthor());
        result.setDate(stored.getDate());
        result.setHighlight(stored.getHighlight());
        result.setId(stored.getId());
        result.setText(stored.getText());
        result.setPageId(stored.getPageId());
        result.setReplyTo(stored.getReplyTo());

        /* add pageUrl attribute */
        List<Link> links = stored.getLinks();
        for (Link link : links) {
            if (link.getRel().equals(Relations.PAGE)) {
                result.setPageUrl(link.getHref());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getPageSummary(org.xwiki.eclipse.model.ModelObject)
     */
    @Override
    public XWikiEclipsePageSummary getPageSummary(ModelObject m)
    {
        XWikiEclipsePageSummary result = null;

        if (m instanceof XWikiEclipseObjectSummary) {

        }

        if (m instanceof XWikiEclipseObject) {

        }

        if (m instanceof XWikiEclipseComment) {
            XWikiEclipseComment comment = (XWikiEclipseComment) m;
            XWikiEclipsePage page = getPage(comment);
            XWikiEclipseSpaceSummary space = getSpaceSummary(page);
            List<XWikiEclipsePageSummary> pageSummaries = getPageSummaries(space);
            for (XWikiEclipsePageSummary pageSummary : pageSummaries) {
                if (pageSummary.getId().equals(page.getId())) {
                    result = pageSummary;
                    break;
                }
            }
        }

        if (m instanceof XWikiEclipseAttachment) {
            XWikiEclipseAttachment attachment = (XWikiEclipseAttachment) m;
            XWikiEclipsePage page = getPage(attachment);
            XWikiEclipseSpaceSummary space = getSpaceSummary(page);
            List<XWikiEclipsePageSummary> pageSummaries = getPageSummaries(space);
            for (XWikiEclipsePageSummary pageSummary : pageSummaries) {
                if (pageSummary.getId().equals(page.getId())) {
                    result = pageSummary;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * @param m
     * @return
     */
    public XWikiEclipseSpaceSummary getSpaceSummary(ModelObject m)
    {
        XWikiEclipseSpaceSummary result = null;

        if (m instanceof XWikiEclipsePage) {
            XWikiEclipsePage page = (XWikiEclipsePage) m;
            Space spaceSummary = this.restRemoteStorage.getSpace(page.getSpaceUrl());

            result = new XWikiEclipseSpaceSummary(dataManager);
            result.setId(spaceSummary.getId());
            result.setName(spaceSummary.getName());
            result.setWiki(spaceSummary.getWiki());
            List<Link> links = spaceSummary.getLinks();
            for (Link link : links) {
                if (link.getRel().equals(Relations.PAGES)) {
                    result.setPagesUrl(link.getHref());
                    break;
                }
            }

        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#removeAttachment(org.xwiki.eclipse.model.XWikiEclipseAttachment)
     */
    @Override
    public void removeAttachment(XWikiEclipseAttachment attachment)
    {
        restRemoteStorage.removeAttachment(attachment.getAttachmentUrl());

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#uploadAttachment(org.xwiki.eclipse.model.XWikiEclipsePageSummary,
     *      java.lang.String)
     */
    @Override
    public void uploadAttachment(XWikiEclipsePageSummary pageSummary, String filePath)
    {
        File f = new File(filePath);

        String attachmentUrl = null;
        /* this field may be null as the page may does not contain any attachments before */
        String attachmentsUrl = pageSummary.getAttachmentsUrl();
        if (attachmentsUrl != null) {
            attachmentUrl = attachmentsUrl + "/" + f.getName();
        } else {
            /* construct one */
            attachmentUrl = pageSummary.getPageUrl() + "/" + "attachments" + "/" + f.getName();
        }

        restRemoteStorage.uploadAttachment(attachmentUrl, f.getName(), filePath);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#getSpace(org.xwiki.eclipse.model.XWikiEclipsePageSummary)
     */
    @Override
    public XWikiEclipseSpaceSummary getSpace(XWikiEclipsePageSummary pageSummary)
    {
        Space space = restRemoteStorage.getSpace(pageSummary.getSpaceUrl());
        XWikiEclipseSpaceSummary result = new XWikiEclipseSpaceSummary(dataManager);

        result.setId(space.getId());
        result.setName(space.getName());
        result.setUrl(space.getXwikiAbsoluteUrl());
        result.setWiki(space.getWiki());
        List<Link> links = space.getLinks();
        for (Link link : links) {
            if (link.getRel().equals(Relations.PAGES)) {
                result.setPagesUrl(link.getHref());
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.eclipse.storage.IRemoteXWikiDataStorage#updateAttachment(org.xwiki.eclipse.model.XWikiEclipseAttachment,
     *      java.lang.String)
     */
    @Override
    public void updateAttachment(XWikiEclipseAttachment attachment, String filePath)
    {
        String attachmentUrl = attachment.getAttachmentUrl();

        restRemoteStorage.uploadAttachment(attachmentUrl, attachment.getName(), filePath);

    }
}
