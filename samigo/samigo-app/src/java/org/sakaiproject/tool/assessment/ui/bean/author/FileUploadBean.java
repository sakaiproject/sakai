/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.struts.upload.FormFile;

/**
 * <p>
 * Title: sakaiproject.org
 * </p>
 *
 * <p>
 * Description: form for uploading media.
 * </p>
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * Used to be org.navigoproject.ui.web.form.edit.FileUploadForm.java
 *
 * @author Rachel Gollub
 * @author Qingru Zhang
 * @author Ed Smiley
 * @version 1.0
 */
public class FileUploadBean
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -6101267878345384445L;
private String source;
  private FormFile newfile;
  private String link;
  private String name;
  private String title;
  private String description;
  private String author;
  private String filename;
  private String type;
  private boolean isEdit;
  private boolean isHtmlInline;
  private Collection mediaTypes;
  private String mapId;
  // displayed as image true or false
  private boolean isHtmlImage;
  // image attributes
  private String imageAlt;
  private String imageVspace;
  private String imageHspace;
  private String imageAlign;
  private String imageBorder;
  // item_ident_ref is the id of an answer (item_result) submitted by user
  private String itemIdentRef;
  private String userName;

  /**
   * Creates a new FileUploadBean object.
   */
  public FileUploadBean()
  {
    resetFields();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getSource()
  {
    return source;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param psource DOCUMENTATION PENDING
   */
  public void setSource(String psource)
  {
    source = psource;
  }

  /**
   * FormFile is a file upload file.
   *
   * @return FormFile file upload file.
   */
  public FormFile getNewfile()
  {
    return newfile;
  }

  /**
   * FormFile is a file upload file.
   *
   * @param pnewfile FormFile file upload file.
   */
  public void setNewfile(FormFile pnewfile)
  {
    newfile = pnewfile;
  }

  /**
   * Link true or false?
   *
   * @return link true or false?
   */
  public String getLink()
  {
    if(link != null)
    {
      return link;
    }
    else
    {
      return "";
    }
  }

  /**
   * Link true or false?
   *
   * @param plink link true or false?
   */
  public void setLink(String plink)
  {
    link = plink;
  }

  /**
   * Media Name.
   *
   * @return name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Media Name.
   *
   * @param pname name.
   */
  public void setName(String pname)
  {
    name = pname;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ptitle DOCUMENTATION PENDING
   */
  public void setTitle(String ptitle)
  {
    title = ptitle;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pdescription DOCUMENTATION PENDING
   */
  public void setDescription(String pdescription)
  {
    description = pdescription;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getAuthor()
  {
    return author;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pauthor DOCUMENTATION PENDING
   */
  public void setAuthor(String pauthor)
  {
    author = pauthor;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getFileName()
  {
    if(filename != null)
    {
      return filename;
    }

    if((newfile != null) && (newfile.getFileName() != null))
    {
      return newfile.getFileName();
    }

    return "";
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pname DOCUMENTATION PENDING
   */
  public void setFileName(String pname)
  {
    filename = pname;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean getIsEdit()
  {
    return isEdit;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pedit DOCUMENTATION PENDING
   */
  public void setIsEdit(boolean pedit)
  {
    isEdit = pedit;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getType()
  {
    return type;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param ptype DOCUMENTATION PENDING
   */
  public void setType(String ptype)
  {
    type = ptype;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getMediaTypes()
  {
    return mediaTypes;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param types DOCUMENTATION PENDING
   */
  public void setMediaTypes(Collection types)
  {
    mediaTypes = types;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getMapId()
  {
    return mapId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pmapId DOCUMENTATION PENDING
   */
  public void setMapId(String pmapId)
  {
    mapId = pmapId;
  }


  /**
   * Is this media item part of HTML code?
   * @param pIsHtmlInline sets it true if it is, else false.
   */
  public void setIsHtmlInline(boolean pIsHtmlInline)
  {
    isHtmlInline = pIsHtmlInline;
  }

  /**
   * Is this media item part of HTML code?
   * @return true if it is, else false.
   */
  public boolean getIsHtmlInline()
  {
    return isHtmlInline;
  }

  /**
   * If this media item part of HTML code, is it an image?
   * This property is only relevant if isHtmlInline.
   * @param pIsHtmlImage sets it true if it is, else false.
   */
  public void setIsHtmlImage(boolean pIsHtmlImage)
  {
    isHtmlImage = pIsHtmlImage;
  }

  /**
   * If this media item part of HTML code, is it an image?
   * This property is only relevant if isHtmlInline.
   * @return true if it is, else false.
   */
  public boolean getIsHtmlImage()
  {
    return isHtmlImage;
  }


  /**
   * If this media item part of an image HTML code
   * the value set is an image ALT attribute.
   * @param pimageAlt image ALT attribute.
   */
  public void setImageAlt(String pimageAlt)
  {
    imageAlt = pimageAlt;
  }

  /**
   * If this media item part of an image HTML code
   * the value is an image attribute.
   * @return the value of the image attribute
   */
  public String getImageAlt()
  {
    return imageAlt;
  }

  /**
   * If this media item part of an image HTML code
   * the value is an image VSPACE attribute.
   * @return the value of the image VSPACE attribute
   */
  public String getImageVspace()
  {
    return imageVspace;
  }

  /**
   * If this media item part of an image HTML code
   * the value is an image VSPACE attribute.
   * @param pIsHtmlInline sets image VSPACE attribute.
   */
  public void setImageVspace(String pimageVspace)
  {
    imageVspace = pimageVspace;
  }

  /**
   * If this media item part of an image HTML code
   * the value of HSPACE image attribute.
   * @return the value of the HSPACE image attribute
   */
  public String getImageHspace()
  {
    return imageHspace;
  }
  /**
   * If this media item part of an image HTML code
   * sets the value of HSPACE image attribute.
   * @param pimageHspace value of HSPACE image attribute.
   */
  public void setImageHspace(String pimageHspace)
  {
    imageHspace = pimageHspace;
  }

  /**
   * If this media item part of an image HTML code
   * the value of ALIGN image attribute.
   * @return the value of the ALIGN image attribute
   */
  public String getImageAlign()
  {
    return imageAlign;
  }
  /**
   * If this media item part of an image HTML code
   * sets the value of ALIGN image attribute.
   * @param pimageHspace value of ALIGN image attribute.
   */
  public void setImageAlign(String pimageAlign)
  {
    imageAlign = pimageAlign;
  }

  /**
   * If this media item part of an image HTML code
   * the value of BORDER image attribute.
   * @return the value of the BORDER image attribute
   */
  public String getImageBorder()
  {
    return imageBorder;
  }
  /**
   * If this media item part of an image HTML code
   * sets the value of BORDER image attribute.
   * @param pimageHspace value of BORDER image attribute.
   */
  public void setImageBorder(String pimageBorder)
  {
    imageBorder = pimageBorder;
  }


  public String getItemIdentRef()
  {
    if(itemIdentRef != null)
    {
      return itemIdentRef;
    }
    else
    {
      return "";
    }
  }

  public void setItemIdentRef(String pitemIdentRef)
  {
    itemIdentRef = pitemIdentRef;
  }


  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String puserName)
  {
    userName = puserName;
  }

  /**
   * Sets defaults, used by constructor.
   */
  public void resetFields()
  {
    if(source == null)
    {
      source = "0";
      newfile = null;
      link = "";
      name = "New media";
      description = "";
      author = "";
      type = "text";
      filename = "";
      mediaTypes = new ArrayList();
      mapId = "";
      imageAlt = "New image";
      imageVspace = "";
      imageHspace = "";
      imageAlign = "";
      imageBorder = "";
      isEdit = true;
      isHtmlInline = true;
      isHtmlImage = true;
      itemIdentRef = "";
      userName = "guest";
    }
  }
}
