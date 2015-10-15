/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package org.sakaiproject.rsf.helper;

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
