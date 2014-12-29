Configurable Jobs
=================

I. Overview

The Job Scheduler had been augmented to accommodate providing configuration parameters to jobs during creation and
scheduling. This feature has been added in response to the preponderance of jobs designed to extract job parameters
from values encoded in the job name at creation time. Configurable jobs provide a more formal mechanism for managing
customization of jobs and removes the need to overload job naming with parameter passing concerns.

II. Configuring a Job

There are two opportunities to supply configuration parameters to a job: during job creation, and during job
scheduling. If a job accepts configuration parameters the forms for setting those parameters will be shown to the
user during these two phases of job management.

A. Job Creation

During job creation the user is instantiating a job type which can be triggered by a schedule that will be created in
the Job Scheduling phase below. The user will select from among the available job definitions registered with the
Sakai CLE and will provide a name for the job type. If the job accepts no configuration parameters the job creation
process is complete. However, if the job is configured to accept configuration parameters the user will be shown a
form with fields for setting those parameters. Each field will be labelled with a parameter name. A description of
the field may be provided by the job designer as well. The field will accept a text value. It is up to the job itself
to interpret the text value. For some fields a default value will be provided.

Submitted parameters will be validated before the user is allowed to continue. Some fields may be marked as required.
These fields will appear with a red asterisk. Any required field which is left blank will cause a validation error.
The user must fill in these fields to complete the job creation. Other fields will be validated by custom rules
built by the job designer. Any violation of those custom rules will also prompt a validation error which the user
must resolve before continuing.

B. Job Scheduling

Job Scheduling involves the choice to run a job immediately, or the creation of a scheduled trigger which will run
the job according to a periodic schedule established by the user.

When scheduling a job of one of the types created in the Job Creation phase above the user will be allowed to override
or accept the values submitted as part of that creation process. The user will be presented with the same configuration
form as was displayed in the Job Creation process. This time the form will show the values that were configured
as part of Job Creation. The user may edit those values or leave them as they are, and then will submit the form.
The form fields will be subject to the same validation rules as were applied during Job Creation.

III. Job Development

Configurable jobs are simply implementations of the Quartz Job class which obtain configuration parameters at execution
time from a Map object (specifically a JobDataMap object). The job designer must provide details about the parameters
the job accepts such that the Job Scheduler tool can create the configuration form. These details include: names and
descriptions for the parameters, a ResourceBundle which can be used to obtain localized versions of those names and
descriptions, and a class which can validate the submitted parameter values. These details are provided as part of
a component definition file (components.xml).

A. Defining Configurable Jobs

In the components.xml file a configurable job can be defined by creating a bean of type:

    org.sakaiproject.component.app.scheduler.jobs.SpringConfigurableJobBeanWrapper

This is a subclass of SpringJobBeanWrapper from the same package. As such it inherits the following properties:

   Property Name             |   Description
   ------------------------------------------------------------------------------------------------------------
   beanId                    | The id of the <bean> element which defines the Quartz Job implementation itself
                             |
   jobName                   | The string used to identify this job type in the Job Scheduler tool
                             |
   schedulerManager          | Identifies the SchedulerManager implementat with which this job type should be
                             | registered. To work with the existing Job Scheduler tool this value MUST be:
                             |   org.sakaiproject.api.app.scheduler.SchedulerManager

In addition, the SpringConfigurableJobBeanWrapper adds the following properties:

   Property Name                    |   Description
   ----------------------------------------------------------------------------------------------------------------
   resourceBundleBase               | The fully qualified name for a message bundle which contains the text to use
                                    | for the property names and descriptions, and validation error messages, used
                                    | by this job.
                                    |
   configurableJobProperties        | This should be a Set containing instances of SpringConfigurableJobProperty
                                    | (see below).
                                    |
   configurableJobPropertyValidator | An instance of:
                                    |   org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator
                                    | This instantiates logic to validate submitted configuration values. If no
                                    | value is supplied, a default instance which permits all values will be used.

An example can be seen in the jobscheduler/scheduler-configurable-job-test-component project at the path:

   src/webapp/WEB-INF/components.xml

B. SpringConfigurableJobProperty

Each instance of this class defines a single property which may be configured for a configurable job. That definition
includes four properties:

   Property Name            |   Description
   -------------------------------------------------------------------------------------------------------------
   labelResourceKey         | The id of a resource string providing the name/label of this property for display
                            | by the Job Scheduler tool. That resource string MUST be defined in the
                            | ResourceBundle identified by the resourceBundleBase proeperty of the
                            | SpringConigurableJobBeanWrapper (above).
                            |
   descriptionResourceKey   | The id of a resource string providing the description of this property for display
                            | by the Job Scheduler tool. That resource string MUST be defined in the
                            | ResourceBundle identified by the resourceBundleBase proeperty of the
                            | SpringConigurableJobBeanWrapper (above).
                            |
   defaultValue             | An optional value that will be used by default for this property.
                            |
   required                 | A boolean flag. If true, this property MUST be configured before job creation can
                            | be completed.

C. ConfigurableJobPropertyValidator

An instance of this object can be supplied to the SpringConfigurableJobBeanWrapper. It will implement a single method:

   public void assertValid (String key, String value)

The assertValid(...) method will be called with each submitted property value when a Job configuration form is
submitted. The 'key' parametery must match the labelResourceKey for a property defined as a
SpringConfigurablePropertyBean. The method is responsible for preforming any logic required to assure that the
'value' parameter is valid. If the 'value' submitted is invalid the assertValid(...) method should throw an
Exception of type:

   org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException

When creating a ConfigurableJobPropertyValidationException one should supply a resource key as the message parameter
to the Exception's constructor method. This resource key will be used to look up the localized resources string
to display to the user to indicate the validation failure. This resource key will be looked up in the ResourceBundle
identified by the resourceBundleBase in the SpringConfigurableJobBeanWrapper (see above).

D. AbstractConfigurableJob

When a configured job executes it must be supplied the configuration parameters the user has specified during job
creation or scheduling. These are generally available to the job from the Map of values obtained via a call to
JobExecutionEnvironment.getMergedJobDataMap(). AbstractConfigurableJob provides a convenience method for
obtaining values from that map:

   String getConfiguredProperty(String key);

The 'key' parameter to the getConfiguredProperty(...) method should match the labelResourceKey for the property the
developer wants to obtain.

AbstractConfigurableJob implements the execute(...) method defined by the Quartz Job class in order to wire up the
JobDataMap internally. An instance of AbstractConfigurableJob should implement its execution logic in the abstract
method runJob() instead.

IV. Example

An example implmentation of the above configurable job framework is available under the jobscheduler module and is
implemented in two sub-modules:

   scheduler-configurable-job-test-component
   scheduler-configurable-job-test-component-shared

These modules are not built by default. In order to include these in the build and deploy steps for the Sakai CLE
the Maven command line should include a flag: "scheduler.configurable.test". For example, from the root folder of the
Sakai CLE source, one could execute:

   mvn -Dscheduler.configurable.test clean install sakai:deploy

This will deploy a Job type with the name "Configurable Job Test". Using the Job Scheduler tool, it should be possible
to test creation and scheduling of this job type to see the configuration in action.

In order to test the processing of job parameters by the job instance itself one can enable logging for the job
instance by including the following in the sakai.properties configuration file for your Sakai CLE instance:

   log.config.count=1
   log.config.1 = ALL.org.sakaiproject.scheduler.configurable
   