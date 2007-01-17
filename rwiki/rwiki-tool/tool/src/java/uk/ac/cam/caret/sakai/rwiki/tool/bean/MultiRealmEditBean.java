/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;

/**
 * @author ieb
 */
public class MultiRealmEditBean
{

	String pageName = null;

	String localSpace = null;

	HttpServletRequest request = null;

	private RequestScopeSuperBean rsac = null;

	private static final String[] permissionNames = { "Create", "Read", "Edit", "Delete", "Admin", "Super Admin" };

	private static final int PERM_CREATE = 0;

	private static final int PERM_ADMIN = 4;

	private static final int PERM_DELETE = 3;

	private static final int PERM_READ = 1;

	private static final int PERM_SUPERADMIN = 5;

	private static final int PERM_UPDATE = 2;

	public String getLocalSpace()
	{
		return localSpace;
	}

	public String getSakaiHTMLHead()
	{

		return (String) request.getAttribute("sakai.html.head");

	}

	public String getBodyOnLoad()
	{
		return "onload=\""+request.getAttribute("sakai.html.body.onload") + "; parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders(); \"";
	}

	public String getPageName()
	{

		return pageName;

	}

	public String getErrors(String wrapperFormat, String itemFormat)
	{

		ErrorBean eb = rsac.getErrorBean();
		List errors = eb.getErrors();
		StringBuffer sb = new StringBuffer();
		if (errors.size() > 0)
		{
			for (Iterator i = errors.iterator(); i.hasNext();)
			{
				sb.append(MessageFormat.format(itemFormat, new Object[] { i.next() }));
			}
			return MessageFormat.format(wrapperFormat, new Object[] { sb.toString() });
		}
		else
		{
			return "";
		}
	}

	public String getPermissionNames(String nameFormat)
	{

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < permissionNames.length; i++)
		{
			sb.append(MessageFormat.format(nameFormat, new Object[] { permissionNames[i] }));
		}
		return sb.toString();

	}

	public String getPermissionsControl(AuthZGroupBean azbg, RoleBean rb, int perm, String permissionEditOnFormat,
			String permissionEditOffFormat)
	{
		String fieldName = null;
		String format = null;
		switch (perm)
		{
			case PERM_CREATE:
				fieldName = "create_" + azbg.getEscapedId() + "_" + rb.getId();
				format = rb.isSecureCreate() ? permissionEditOffFormat : permissionEditOnFormat;
				break;
			case PERM_ADMIN:
				fieldName = "create_" + azbg.getEscapedId() + "_" + rb.getId();
				format = rb.isSecureAdmin() ? permissionEditOffFormat : permissionEditOnFormat;
				break;
			case PERM_DELETE:
				fieldName = "create_" + azbg.getEscapedId() + "_" + rb.getId();
				format = rb.isSecureDelete() ? permissionEditOffFormat : permissionEditOnFormat;
				break;
			case PERM_READ:
				fieldName = "create_" + azbg.getEscapedId() + "_" + rb.getId();
				format = rb.isSecureRead() ? permissionEditOffFormat : permissionEditOnFormat;
				break;
			case PERM_SUPERADMIN:
				fieldName = "create_" + azbg.getEscapedId() + "_" + rb.getId();
				format = rb.isSecureSuperAdmin() ? permissionEditOffFormat : permissionEditOnFormat;
				break;
			case PERM_UPDATE:
				fieldName = "create_" + azbg.getEscapedId() + "_" + rb.getId();
				format = rb.isSecureUpdate() ? permissionEditOffFormat : permissionEditOnFormat;
				break;
			default:
				fieldName = String.valueOf(perm);
				format = "Undefiend field with index {0} ";
				break;

		}
		return MessageFormat.format(format, new Object[] { fieldName });
	}

	/**
	 * @param permissionsRealmWrapperFormat "
	 *        {0} {1} {2} " (O:realmname, 1:permissionNames, 2: group)* "
	 * @param permissionNamesFormat "
	 *        {0} " (O:permissionName)*
	 * @param groupWrapperFormat "
	 *        {0} {1} " (0:role name 1:permissionEditOnFormat|permissionEditOffFormat)*
	 * @param permissionEditOnFormat "
	 *        {0} " 0:formitemName
	 * @param permissionEditOffFormat "
	 *        {0} " 0:formitemName
	 * @return
	 */
	public String getPermissionsGroupRoles(String permissionsRealmWrapperFormat, String permissionNamesFormat,
			String groupWrapperFormat, String permissionEditOnFormat, String permissionEditOffFormat)
	{
		String permissionNamesHtml = getPermissionNames(permissionNamesFormat);
		AuthZGroupCollectionBean realmCollectionBean = rsac.getAuthZGroupCollectionBean();
		List realms = realmCollectionBean.getRealms();
		StringBuffer output = new StringBuffer();
		for (Iterator i = realms.iterator(); i.hasNext();)
		{
			AuthZGroupBean azgb = (AuthZGroupBean) i.next();
			List roles = azgb.getRoles();
			if (roles.size() > 0)
			{
				StringBuffer rolesOutput = new StringBuffer();
				for (Iterator rolesi = roles.iterator(); rolesi.hasNext();)
				{
					RoleBean rb = (RoleBean) rolesi.next();
					StringBuffer permissionsOutput = new StringBuffer();
					for (int j = 0; j < permissionNames.length; j++)
					{
						permissionsOutput
								.append(getPermissionsControl(azgb, rb, j, permissionEditOnFormat, permissionEditOffFormat));
					}
					rolesOutput.append(MessageFormat.format(groupWrapperFormat, new Object[] { rb.getId(),
							permissionsOutput.toString() }));
				}

				output.append(MessageFormat.format(permissionsRealmWrapperFormat, new Object[] { azgb.getRealmId(),
						permissionNamesHtml, rolesOutput.toString() }));
				break;
			}
		}

		return output.toString();

	}

	public String getAvailableRealms(String rolesFormat)
	{
		AuthZGroupCollectionBean realmCollectionBean = rsac.getAuthZGroupCollectionBean();
		List realms = realmCollectionBean.getRealms();
		StringBuffer output = new StringBuffer();
		for (Iterator i = realms.iterator(); i.hasNext();)
		{
			AuthZGroupBean azgb = (AuthZGroupBean) i.next();
			List roles = azgb.getRoles();
			if (roles.size() == 0)
			{
				output.append(MessageFormat.format(rolesFormat, new Object[] { azgb.getRealmId() }));
			} else {
				break;
			}
		}

		return output.toString();

	}

	public String getFooterScript()
	{

		return "<script type=\"text/javascript\" >" + request.getAttribute("footerScript") + "</script>";

	}

	/**
	 * @return Returns the request.
	 */
	public HttpServletRequest getRequest()
	{
		return request;
	}

	/**
	 * @param request
	 *        The request to set.
	 */
	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	/**
	 * @return Returns the rsac.
	 */
	public RequestScopeSuperBean getRsac()
	{
		return rsac;
	}

	/**
	 * @param rsac
	 *        The rsac to set.
	 */
	public void setRequestScopeSuperBean(RequestScopeSuperBean rsac)
	{
		this.rsac = rsac;
	}

	/**
	 * @param localSpace
	 *        The localSpace to set.
	 */
	public void setLocalSpace(String localSpace)
	{
		this.localSpace = localSpace;
	}

	/**
	 * @param pageName
	 *        The pageName to set.
	 */
	public void setPageName(String pageName)
	{
		this.pageName = pageName;
	}
}
