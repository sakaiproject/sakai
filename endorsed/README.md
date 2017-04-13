Project Purpose:

Basque and Mongolian languages are not fully supported in Java. e.g., DateFormat, NumberFormat, and 
locale name are not.
This project is in order to support them by implementing the locale sensitive service provider which
is offered in the Java Extension Mechanism.


----------------------------------------------------------------------------------------------------
Usage:

It is needed to store sakai-endorsed-i18n-x.x.jar into the proper location.

For Tomcat 6 or 7, the target location is $CATALINA_HOME/endorsed. The following Maven builds command
will store it there.

  mvn clean install -Dmaven.tomcat.home=$CATALINA_HOME


For Tomcat 5.x, the target location is $CATALINA_HOME/common/endorsed. Please manually build and copy
it there.

  mvn clean install
  copy target/sakai-endorsed-i18n-*.jar $CATALINA_HOME/common/endorsed/


Additionally, $JAVA_HOME/jre/lib/ext can be the target instead of the above locations.

  mvn clean install
  copy target/sakai-endorsed-i18n-*.jar $JAVA_HOME/jre/lib/ext/


----------------------------------------------------------------------------------------------------
Remarks:

1.In order to enable the extension of the service provider, please use

  DateFormatSymbols.getInstance(locale);
  DecimalFormatSymbols.getInstance(locale);

  instead of

  new DateFormatSymbols(locale);
  new DecimalFormatSymbols(locale);

  in your code, because the latters do not support the extension. For example, the day of week in 
  Section Info tool will not be displayed in Basque or Mongolian even if the extension was properly 
  stored. It is because 'new DateFormatSymbols(locale);' is used in the tool in Sakai 2.9.1.

2.The first day of week can not be extended by the service provider currently. It wiil be offered in
  Java 8 as java.util.spi.CalendarDataProvider.


----------------------------------------------------------------------------------------------------
Adding more locales:

If you'd like to add another unsupported locale except for Basque and Mongolian, please follow the 
steps below.

1. Add the locale string into 'locales=' within src/resources/SakaiLocaleServiceProvider.config
2. Create the resource bundle file as src/resources/SakaiLocaleServiceProvider_XX.properties
3. Localize the content of the resource bundle.
4. Build and store it into the proper location.

