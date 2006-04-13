/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.util.Date;

/**
 * @author ieb
 */
public class CommentNewCommand extends CommentSaveCommand
{
	protected void doUpdate(String name, String realm, Date versionDate,
			String content)
	{
		objectService.updateNewComment(name, realm, new Date(), content);
	}
}
