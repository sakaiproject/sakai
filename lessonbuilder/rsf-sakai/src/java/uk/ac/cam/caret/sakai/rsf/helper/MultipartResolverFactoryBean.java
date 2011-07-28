package uk.ac.cam.caret.sakai.rsf.helper;

import org.springframework.web.multipart.MultipartResolver;

import uk.org.ponder.rsf.state.TokenStateHolder;
import uk.org.ponder.util.Logger;

/* We need this resolver to swap in the Blank MultiPart resolver when
 * using Sakai Helpers.  Otherwise the usual resolver strips any file
 * uploads away before the helper can get to them (rendering the attachments
 * helper useless).
 * 
 * This should be beefed up in the future with a smoother scheme.
 */
public class MultipartResolverFactoryBean {
  
  private MultipartResolver commonsMultipartResolver;
  private MultipartResolver blankMultipartResolver;
  private TokenStateHolder tsh;
  
  public void setTokenStateHolder(TokenStateHolder tsh) {
    this.tsh = tsh;
  }
  
  public void setCommonsMultipartResolver(MultipartResolver resolver) {
    commonsMultipartResolver = resolver;
  }
  
  public void setBlankMultipartResolver(MultipartResolver resolver) {
    blankMultipartResolver = resolver;
  }
  
  public MultipartResolver getMultipartResolver() throws Exception {
    String indicator = (String) tsh.getTokenState(HelperHandlerHookBean.IN_HELPER_INDICATOR);
    if (indicator != null && indicator.equals(HelperHandlerHookBean.IN_HELPER_INDICATOR))
      return blankMultipartResolver;
    else
      return commonsMultipartResolver;
  }

}
