/*
 * Created on Sep 19, 2005
 */
package org.sakaiproject.lessonbuildertool.util;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ResourceLoader;

import uk.org.ponder.reflect.ReflectiveCache;
import uk.org.ponder.rsf.flow.errors.SilentRedirectException;
import uk.org.ponder.rsf.template.TPIAggregator;
import uk.org.ponder.rsf.template.XMLCompositeViewTemplate;
import uk.org.ponder.rsf.template.XMLViewTemplate;
import uk.org.ponder.rsf.template.XMLViewTemplateParser;
import uk.org.ponder.rsf.templateresolver.BaseAwareTemplateResolverStrategy;
import uk.org.ponder.rsf.templateresolver.ExpectedTRS;
import uk.org.ponder.rsf.templateresolver.ForceContributingTRS;
import uk.org.ponder.rsf.templateresolver.MultipleTemplateResolverStrategy;
import uk.org.ponder.rsf.templateresolver.RootAwareTRS;
import uk.org.ponder.rsf.templateresolver.TemplateExtensionInferrer;
import uk.org.ponder.rsf.templateresolver.TemplateResolver;
import uk.org.ponder.rsf.templateresolver.TemplateResolverStrategy;
import uk.org.ponder.rsf.view.ViewTemplate;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.templateresolver.support.TemplateResolutionContext;
import uk.org.ponder.springutil.CachingInputStreamSource;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * A basic template resolver accepting a TemplateExtensionInferrer and a
 * TemplateResolverStrategy to load a view template.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */
public class BasicTemplateResolver implements TemplateResolver {

  private TemplateExtensionInferrer tei;
  private int cachesecs;
  private TPIAggregator aggregator;
  private List strategies;

  public void setResourceLoader(ResourceLoader resourceLoader) {
    cachingiis = new CachingInputStreamSource(resourceLoader, cachesecs);
  }
  
  /**
   * Set the lag in seconds at which the filesystem will be polled for changes
   * in the view template. If this value is 0 or the resource is not a
   * filesystem resource, it will always be reloaded.
   */
  public void setCacheSeconds(int cachesecs) {
    this.cachesecs = cachesecs;
  }

  public void setTemplateExtensionInferrer(TemplateExtensionInferrer tei) {
    this.tei = tei;
  }

  public void setTemplateResolverStrategies(List strategies) {
    this.strategies = strategies;
  }

  public void setTPIAggregator(TPIAggregator aggregator) {
    this.aggregator = aggregator;
  }

  public void setReflectiveCache(ReflectiveCache reflectiveCache) {
    templates = reflectiveCache.getConcurrentMap(1);
  }
  
  // this is a map of viewID onto template file.
  private Map templates;

  private CachingInputStreamSource cachingiis;

  public ViewTemplate locateTemplate(ViewParameters viewparams) {

    // NB if we really want this optimisation, it must be based on #
    // of RETURNED templates, not the number of strategies!
    XMLCompositeViewTemplate xcvt = strategies.size() == 1 ? null
        : new XMLCompositeViewTemplate();
    int highestpriority = 0;
    StringList tried = new StringList();
    
    for (int i = 0; i < strategies.size(); ++i) {
      TemplateResolverStrategy trs = (TemplateResolverStrategy) strategies
          .get(i);
      int thispri = trs instanceof RootAwareTRS ? ((RootAwareTRS) trs)
          .getRootResolverPriority()
          : 1;
      
      boolean isexpected = trs instanceof ExpectedTRS ? ((ExpectedTRS) trs)
          .isExpected()
          : true;
      boolean ismultiple = trs instanceof MultipleTemplateResolverStrategy ? ((MultipleTemplateResolverStrategy) trs)
          .isMultiple()
          : false;

      StringList bases = trs.resolveTemplatePath(viewparams);

      StringList[] usebases;
      if (ismultiple) {
        usebases = new StringList[bases.size()];
        for (int j = 0; j < usebases.length; ++j) {
          usebases[j] = new StringList(bases.stringAt(j));
        }
      }
      else {
        usebases = new StringList[] { bases };
      }
      
      TemplateResolutionContext trc = 
        new TemplateResolutionContext(viewparams, trs,  isexpected ? tried : null, ismultiple && isexpected);
      
      
      for (int j = 0; j < usebases.length; ++j) {
        trc.bases = usebases[j];
        resolveTemplateStream(trc);
        XMLViewTemplate template = parseTemplate(trc);
        if (template != null) {
          if (xcvt != null) {
            if (trs.isStatic()) {
             xcvt.globalmap.aggregate(template.rootlump.downmap);
            }
            else {
              xcvt.globalmap.aggregate(template.globalmap);
            }
            if (template.mustcollectmap != null) {
              xcvt.mustcollectmap.aggregate(template.mustcollectmap);
            }
            
            if (thispri == highestpriority && thispri != 0) {
              if (xcvt.roottemplate != null && template != xcvt.roottemplate) {
                Logger.log.warn("Duplicate root TemplateResolverStrategy " + trs
				+ " found at priority " + thispri +", using first entry");
              }
            }
            if (thispri > highestpriority) {
              xcvt.roottemplate = template;
              highestpriority = thispri;
            }

          }
          else {
            return template;
          }
        } // end if template returned
      }
    }
    
    if (xcvt != null && xcvt.roottemplate == null) {
      Exception eclass = viewparams.viewID.trim().length() == 0? 
          (Exception)new SilentRedirectException() : new IllegalArgumentException();
      throw UniversalRuntimeException.accumulate(eclass,
          "No template found for view " + viewparams.viewID + ": tried paths (expected) " + 
          tried.toString() + " from all TemplateResolverStrategy which were marked as a root resolver (rootPriority > 0) ");
    }
    return xcvt;
  }

  public void resolveTemplateStream(TemplateResolutionContext trc) {
    String resourcebase = "/";
    if (trc.trs instanceof BaseAwareTemplateResolverStrategy) {
      BaseAwareTemplateResolverStrategy batrs = (BaseAwareTemplateResolverStrategy) trc.trs;
      resourcebase = batrs.getTemplateResourceBase();
    }

    String extension = tei.inferTemplateExtension(trc.viewparams);
    InputStream is = null;
    trc.fullpath = null;
    for (int i = 0; i < trc.bases.size(); ++i) {
      trc.fullpath = resourcebase + trc.bases.stringAt(i) + "." + extension;
      if (trc.tried != null) {
        trc.tried.add(trc.fullpath);
      }
      is = cachingiis.openStream(trc.fullpath);
      if (is != null)
        break;
      if (is == null && trc.logfailure) {
// This is not a real failure for other content types - see RSF-21        
//        Logger.log.warn("Failed to load template from " + fullpath);
      }
    }
    trc.is = is;
  }
  
  public XMLViewTemplate parseTemplate(TemplateResolutionContext trc) {
    if (trc.is == null) return null;
    
    XMLViewTemplate template = null;
    if (trc.is == CachingInputStreamSource.UP_TO_DATE) {
      template = (XMLViewTemplate) templates.get(trc.fullpath);
    }
    if (template == null) {
      List tpis = aggregator.getFilteredTPIs();
      try {
        // possibly the reason is it had a parse error last time, which may have
        // been corrected
        if (trc.is == CachingInputStreamSource.UP_TO_DATE) {
          trc.is = cachingiis.getNonCachingResolver().openStream(trc.fullpath);
        }
        XMLViewTemplateParser parser = new XMLViewTemplateParser();
        parser.setTemplateParseInterceptors(tpis);
        template = (XMLViewTemplate) parser.parse(trc.is);
        // there WILL be one slash in the path.
        int lastslashpos = trc.fullpath.lastIndexOf('/');
        String resourcebaseext = trc.fullpath.substring(1, lastslashpos + 1);
        if (trc.trs instanceof BaseAwareTemplateResolverStrategy) {
          BaseAwareTemplateResolverStrategy batrs = (BaseAwareTemplateResolverStrategy) trc.trs;
          String extresourcebase = batrs.getExternalURLBase();
          template.setExtResourceBase(extresourcebase);
        }
        if (trc.trs instanceof ForceContributingTRS) {
          ForceContributingTRS fctrs = (ForceContributingTRS) trc.trs;
          if (fctrs.getMustContribute()) {
            template.mustcollectmap = template.collectmap;
          }
        }
        template.setRelativeResourceBase(resourcebaseext);
        template.fullpath = trc.fullpath;
        template.isstatictemplate = trc.trs.isStatic();
        templates.put(trc.fullpath, template);
      }
      catch (Exception e) {
        throw UniversalRuntimeException.accumulate(e,
            "Error parsing view template file " + trc.fullpath);
      }
    }
    return template;
  }

}
