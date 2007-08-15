package org.sakaiproject.scorm.client.pages;

/**
 * Bean that is set to every node of tree as user object.
 * This bean has properties that are used to hold values for the cells.
 *  
 * @author Matej Knopp
 */
public class ModelBean
{
	private String property1;
	private String property2;
	private String property3;
	private String property4;
	private String property5;
	private String property6;

	/**
	 * Creates the bean. 
	 * 
	 * @param s
	 *		String that will be suffix of each property. 		
	 */
	public ModelBean(String s)
	{
		property1 = "1:" + s;
		property2 = "2:" + s;
		property3 = "3:" + s;
		property4 = "4:" + s;
		property5 = "5:" + s;
		property6 = "6:" + s;
	}

	/**
	 * Returns the first property.
	 * 
	 * @return
	 * 		First property
	 */
	public String getProperty1()
	{
		return property1;
	}

	/**
	 * Sets the value of first property.
	 * 
	 * @param property1
	 * 		Mew value
	 */
	public void setProperty1(String property1)
	{
		this.property1 = property1;
	}

	/**
	 * Returns the second property.
	 * 
	 * @return
	 * 		Second property
	 */
	public String getProperty2()
	{
		return property2;
	}

	/**
	 * Sets the value of second property
	 * 
	 * @param property2
	 * 			New value
	 */
	public void setProperty2(String property2)
	{
		this.property2 = property2;
	}

	/**
	 * Returns the value of third property.
	 * 
	 * @return
	 * 		Third property
	 */
	public String getProperty3()
	{
		return property3;
	}

	/**
	 * Sets the value of third property
	 * 
	 * @param property3
	 * 		New value
	 */
	public void setProperty3(String property3)
	{
		this.property3 = property3;
	}

	/**
	 * Returns the value of fourth property
	 * 
	 * @return
	 * 		Value of fourth property
	 */
	public String getProperty4()
	{
		return property4;
	}

	/**
	 * Sets the value of fourth property
	 * 
	 * @param property4
	 * 		New value
	 */
	public void setProperty4(String property4)
	{
		this.property4 = property4;
	}

	/**
	 * Returns the value of fifth property
	 * 
	 * @return
	 * 		Value of fifth property	
	 */
	public String getProperty5()
	{
		return property5;
	}

	/**
	 * Sets the value of fifth property
	 * 
	 * @param property5
	 * 		New value
	 */
	public void setProperty5(String property5)
	{
		this.property5 = property5;
	}
	
	/**
	 * Returns the value of sixth property.
	 * 
	 * @return
	 * 		Value of sixth property
	 */
	public String getProperty6()
	{
		return property6;
	}

	/**
	 * Sets the value of sixth property
	 * 
	 * @param property6
	 * 		New value
	 */
	public void setProperty6(String property6)
	{
		this.property6 = property6;
	}
}