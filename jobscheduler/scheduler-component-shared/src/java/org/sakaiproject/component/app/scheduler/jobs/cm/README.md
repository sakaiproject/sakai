# User Synchronization

### Comma Separate Value (CSV) Import
The user synchronization job reads a CSV file to create and update users.

###### Job Configuration

| Name | Default Value | Notes |
| :---: | :---: | :--- |
| adminUser | admin | The admin user account the job should run under |
| emailNotification | false | Should an email be sent with the output of the job status |
| recipients | who@somewhere.com | If emailNotification is true, what email address should receive notification | 

You can customiz the jobs configuration using the name from the table above as a sakai property, i.e.
`emailNotification@org.sakaiproject.component.app.scheduler.jobs.cm.SynchronizationJob.user=true`

###### Processor Configuration

| Name | Default Value | Notes |
| :---: | :---: | :--- |
| archive | false | |
| columns | 12 | |
| filename | /tmp/users.csv | |
| headerRowPresent | false | |
| userEmailNotification | true | Whether to notify the user via email upon creating a new account |
| generatePassword | false | If a password should be generated when creating new accounts or use the supplied password from the csv |
| updateAllowed | true | If the user already exists, should their information be updated with that contained in the csv |
| updatePassword | false | If the user already exists, should a their password be updated with that contained in the csv |

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.UserProcessor=/home/sakai/sis/users.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :---: | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | 2018F | jsmith |
| 1 | last name | varchar(255) | Smith | |
| 2 | first name | varchar(255) | John | |
| 3 | email | varchar(255) | jsmith@somewhere.org | |
| 4 | password | varchar(255) | xxxxxx | if generate password is true, a random 9 character password is always generated regardless if a value has been supplied in the csv file. |
| 5 | user type | varchar(255) | maintain, registered | |
| 6 | user id | varchar(255) | 0123456789 | This can left blank and it will be auto generated |
| 7 | property 1 | varchar(255) | value 1 | Using the values from the configuration "user.sis.property" configure the value for index 1 |
| 8 | property 2 | varchar(255) | value 2 | Using the values from the configuration "user.sis.property" configure the value for index 2 |
| 9 | property 3 | varchar(255) | value 3 | Using the values from the configuration "user.sis.property" configure the value for index 3 |
| 10 | property 4 | varchar(255) | value 4 | Using the values from the configuration "user.sis.property" configure the value for index 4 |
| 11 | property 5 | varchar(255) | value 5 | Using the values from the configuration "user.sis.property" configure the value for index 5 |

# Course Management Synchronization
This process contains processors to interface with Sakai's Course Management API. The current implementation supports a
CSV file based process though there is no reason that the process couldn't be extended to other file formats or processors.

### Comma Separate Value (CSV) Import
The synchronization job uses CSV files which are read and the data is used to update Sakai's Course Management tables via
the Course Management API. There are 12 files in all we will list each file below along with the files layout.

###### Job Configuration
| Name | Default Value | Notes |
| :---: | :---: | :--- |
| adminUser | admin | The admin user account the job should run under |
| emailNotification | false | When this job runs if an email should be sent with the output of its status |
| recipients | who@somewhere.com | If emailNotification is true this should be set with who the emails should be sent to | 

You can customize the jobs configuration using the name from the table above as a sakai property, i.e.
`emailNotification@org.sakaiproject.component.app.scheduler.jobs.cm.SynchronizationJob.cm=true`

#### AcademicSessionProcessor
An institutional context for CourseOfferings, distinguishing one instance of a CanonicalCourse from another.
In higher educational institutions, it almost always includes a time range. However, self-paced "sessions" also are possible.

###### Processor Configuration

| Name | Default Value |
| :---: | :---: |
| archive | false |
| columns | 5 |
| dateFormat | MM/dd/yyyy |
| filename | /tmp/academic_session.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`dateFormat@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.AcademicSessionProcessor=yyyyMMdd`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :---: | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | 2018F | |
| 1 | title | varchar(255) | Fall 2018 | |
| 2 | description | varchar(255) | Fall 2018 Semester | |
| 3 | start date | dateFormat | 01/18/2018 | |
| 4 | end date | dateFormat | 05/22/2018 | |

#### CourseSetProcessor
Models "School" and "Department" as well as more ad hoc groupings.

###### Processor Configuration

| Name | Default Value |
| :---: | :---: |
| archive | false |
| columns | 5 |
| filename | /tmp/course_set.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.CourseSetProcessor=/home/sakai/sis/course_set.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :---: | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | BIO, BIOLOGY | This field is used to map the Canonical Course, Course Offering, and Course Offering Members back to the Course Set. |
| 1 | title | varchar(255) | Biology | |
| 2 | description | varchar(255) | Biology Department | |
| 3 | category | varchar(255) | dept, main | |
| 4 | parent course set eid | varchar(100) | see eid field | If this column is left blank, the course set eid will be saved and the parent course set eid will not be used. |

#### CanonicalCourseProcessor

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 3 |
| filename | /tmp/canonical_course.csv |
| headerRowPresent | false | 

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | biology_101 | |
| 1 | title | varchar(255) | Biology 101 | |
| 2 | description | varchar(255) | Introduction to Cellular Biology |
| 3 | course set eid | varchar(100) | BIO, BIOLOGY | Optional, Matches eid column in the course set file. This then ties that course set back to this canonical course and to a specific course offering in the course offering file via the canonical course eid field. |

#### CourseOfferingProcessor

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 8 |
| dateFormat | MM/dd/yyyy |
| filename | /tmp/course_offering.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.CourseOfferingProcessor=/home/sakai/sis/course_offering.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | biology_101_001 | |
| 1 | academic session eid | varchar(100) | 2018F | |
| 2 | title | varchar(255) | Intro to Biology | |
| 3 | description | varchar(255) | Intro to Biology for Fall 2018 | |
| 4 | status | varchar(255) | open-enrollment, full | |
| 5 | start date | dateFormat | 01/18/2018 | |
| 6 | end date | dateFormat | 05/22/2018 | |
| 7 | canonical course eid | varchar(100) | biology_101 | |
| 8 | course set eid | varchar(100) | BIOLOGY | Optional |

#### CourseOfferingMemberProcessor
Associates a course offering with a user. Users in this file are generally Teaching Assistants and Instructors.

- **For Credit**, user must exist in this processor and the enrollment processor. They could also be in the
section member processor if they are a TA in a specific section. 
- **Not For Credit**, user must exist in this processor and the section member processor. If they are not listed in
both files they will not show up in the site.

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 4 |
| filename | /tmp/course_offering_member.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.CourseOfferingMemberProcessor=/home/sakai/sis/course_offering_member.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | biology_101_001 | |
| 1 | user eid | varchar(255) | jsmith | |
| 2 | role | varchar(255) | I=Instructor, GSI=Teaching Assistant | These values must match the roles in the CourseOfferingRoleResolver, see the following bean for the defaults org.sakaiproject.coursemanagement.GroupProviderConfiguration |
| 3 | status | varchar(255) | enrolled | This field is currently not used |

#### SectionCategoryProcessor
A section category. For example lecture, lab, discussion, etc.

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 2 |
| filename | /tmp/section_category.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.SectionCategoryProcessor=/home/sakai/sis/section_category.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | section category | varchar(255) | Lecture, Lab, Discussion | This will be used in the enrollment set and section processors. 
| 1 | description | varchar(255) | Detail about the type of category |

#### EnrollmentSetProcessor
Defines a group of students who are somehow associated with a CourseOffering or a Section for credit. Defines who is
allowed to submit the final grade for this student via the associated record in the instructor file 
(which contains this enrollment set eid).

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 6 |
| filename | /tmp/enrollment_set.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.EnrollmentSetProcessor=/home/sakai/sis/enrollment_set.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(100) | biology_101_001_lec01_es |
| 1 | title | varchar(255) | Intro to Biology Lecture Enrollment Set |
| 2 | description | varchar(255) | Main Lecture for Intro to Biology 101 |
| 3 | category | varchar(255) | Lecture | The category code from the section category processor |
| 4 | course offering eid | varchar(100) | biology_101_001 | The eid from the course offering processor |
| 5 | default enrollment credits | varchar(255) | 3 | The number of credits for this course |

#### SectionProcessor
Models a "cohort" (a stable group which enrolls in multiple courses as a unit) as well as officially delimited course "groups" and "sections".

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 7 |
| filename | /tmp/section.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.SectionProcessor=/home/sakai/sis/section.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | eid | varchar(255) | biology_101_001_lec01 | The section eid is used in the section member and section meeting processors |
| 1 | title | varchar(255) | Intro to Biology 101 Lecture 1 |
| 2 | description | varchar(255) | Detailes about Intro to Biology 101 Lecture 1 |
| 3 | section category | varchar(255) | Lecture | section category from the section category processor |
| 4 | parent section eid | varchar(255) | biology_101_001_lec | This value is used to map sub sections back to the parent section. This value is not necessary and can be left blank if this functionality is not being utilized. |
| 5 | enrollment set eid | varchar(100) | biology_101_001_lec01_es | This is the enrollment set eid from the enrollment set processor, maps the section to the enrollment set |
| 6 | course offering eid | varchar(255) | biology_101_001 | This is the course offering eid from the course offering processor, maps the original course offering |

#### SectionMeetingProcessor
A time and a place for a Section to meet. Meetings are completely controlled by their sections.

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 3 |
| filename | /tmp/section_meeting.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.SectionMeetingProcessor=/home/sakai/sis/section_meeting.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | section eid | varchar(255) | biology_101_001_lec01 | The eid comes from the section processor |
| 1 | location  | varchar(255) | Lecture Hall A |  |
| 2 | notes | varchar(255) | Bring your text books |  |

#### SectionMemberProcessor
A user-role pair associated with a section. This file would normally be used for a Teaching Assistant role.
Instructors and students do not appear in this file.

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 4 |
| filename | /tmp/section_member.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.SectionMemberProcessor=/home/sakai/sis/section_member.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | section eid | varchar(255) | biology_101_001_lec01 | The eid comes from the section processor |
| 1 | user eid | varchar(255) | jsmith |  |
| 2 | role | varchar(255) | GSI | These values must match the roles in the SectionRoleResolver, see the following bean for the defaults org.sakaiproject.coursemanagement.GroupProviderConfiguration |
| 3 | status | varchar(255) | enrolled, wait | |

#### EnrollmentProcessor
The official relationship of a student to something that gets a final grade (or equivalent).  All students for a course are in this file.
This processor is used to manage both the courses as well as the sections in those courses. Once a user is enrolled in a 
section/course they will remain in said section/course until they are removed from this list. Removing users from this
list will Drop them from the section/course.

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 5 |
| filename | /tmp/enrollment.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.EnrollmentProcessor=/home/sakai/sis/enrollment.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | enrollment set eid | varchar(100) | biology_101_001_lec01_es | The eid comes from the enrollment set processor |
| 1 | user eid | varchar(255) | jsmith | |
| 2 | status | varchar(100) | enrolled, wait | This information is shown in the Roster tool's enrollment status display |
| 3 | credits | varchar(255) | 1, 3, Pass/Fail | This information is shown in the Roster tool's enrollment status display |
| 4 | grading scheme | varchar(255) | pnp, standard |  |

#### InstructorProcessor
Instructors must exist in this processor and the course offering member processor.
Official association of Instructors to Enrollment Sets. If multiple Instructors are listed with the same Enrollment Set
they will all show up in each site that was created with said Enrollment Set.

###### Processor Configuration

| Name | Default Value |
| :-----: | :---: |
| archive | false |
| columns | 2 |
| filename | /tmp/instructor.csv |
| headerRowPresent | false | 

You can customize the processor's configuration using the name from the table above as a sakai property, i.e.
`filename@org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis.InstructorProcesso=/home/sakai/sis/instructor.csv`

###### CSV File Layout

| Column | Name | Data Type | Example | Notes |
| :----- | :---: | :---: | :---: | :--- |
| 0 | enrollment set eid | varchar(255) | biology_101_001_lec01_es | The eid comes from the enrollment set processor |
| 1 | instructor eid | varchar(255) | jsmith |

