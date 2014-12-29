package org.sakaiproject.login.api;


public interface LoginAdvisor {

	public boolean checkCredentials(LoginCredentials credentials);

	public String getLoginAdvice(LoginCredentials credentials);
	
	public boolean isAdvisorEnabled();
	
	public void setFailure(LoginCredentials credentials);
	
	public void setSuccess(LoginCredentials credentials);
	
}
