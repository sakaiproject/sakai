# Sakai Collaboration and Learning Environment (Sakai CLE)

This is the source code for the Sakai CLE.

The master branch is the most current development release, Sakai 24.
The other branches are currently or previously supported releases. See below for more information on the release plan and support schedule.

## Building

[![Build Status](https://travis-ci.org/sakaiproject/sakai.svg?branch=master)](https://travis-ci.org/sakaiproject/sakai)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c68908d6bc044e95b453bae7ddcbad4a)](https://www.codacy.com/app/sakaiproject/sakai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sakaiproject/sakai&amp;utm_campaign=Badge_Grade)

This is the "Mini Quick Start" for more complete steps to get Sakai configured please look at [this guide on the wiki](https://github.com/sakaiproject/sakai/wiki/Quick-Start-from-Source).

To build Sakai you need Java 1.8. Once you have, clone a copy of this repository you can
build it by running (or `./mvnw install` if you don't have Maven installed):
```
mvn install
```

## Running

Sakai runs on Apache Tomcat 9. Download the latest version from http://tomcat.apache.org and extract the archive.
*Note: Sakai does not work with Tomcat installed via a package from apt-get, yum or other package managers.*

You **must** configure Tomcat according to the instructions on this page:
https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17310646930/Sakai+21+Install+Guide+Source

When you are done, deploy Sakai to Tomcat:
```
mvn clean install sakai:deploy -Dmaven.tomcat.home=/path/to/your/tomcat
```

Now start Tomcat:
```
cd /path/to/your/tomcat/bin
./startup.sh && tail -f ../logs/catalina.out
```

Once Sakai has started up (it usually takes around 30 seconds), open your browser and navigate to http://localhost:8080/portal

## Licensing

Sakai is licensed under the [Educational Community License version 2.0](http://opensource.org/licenses/ECL-2.0) 

Sakai is an [Apereo Foundation](http://www.apereo.org) project and follows the Foundation's guidelines and requirements for [Contributor License Agreements](https://www.apereo.org/licensing).

## Contributing

See [our dedicated page](CONTRIBUTING.md) for more information on contributing to Sakai.

## Bugs

For filing bugs against Sakai please use our Jira instance: https://jira.sakaiproject.org/

## Nightly servers 
For testing out the latest builds go to the [nightly server page](http://nightly2.sakaiproject.org)

## Get in touch
If you have any questions, please join the Sakai developer mailing list: To subscribe send an email to sakai-dev+subscribe@apereo.org

To see a full list of Sakai email lists and other communication channels, please check out this Sakai wiki page:
https://confluence.sakaiproject.org/display/PMC/Sakai+email+lists

If you want more immediate response during M-F typical business hours you could try our Slack channels.

https://apereo.slack.com/signup

If you can't find your  "at institution.edu" on the Apereo signup page then send an email requesting access for yourself and your institution either to sakai-qa-planners@apereo.org or sakaicoordinator@apereo.org.

## Community supported versions
These versions are actively supported by the community.

Sakai 25.1 ([release](http://source.sakaiproject.org/release/25.1/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/33322663940/25.1+Features+by+Tool) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/33232420865/Sakai+25+Release+Notes))

Sakai 23.4 ([release](http://source.sakaiproject.org/release/23.4/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/33392099329/23.4+Fixes+by+Tool) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/32324812801/Sakai+23+Release+Notes))

## Previous community versions which are no longer supported
These versions are no longer supported by the community and will only receive security changes.

Sakai 22.5 ([release](http://source.sakaiproject.org/release/22.5/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/32684769282/22.5+Fixes+by+Tool) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/32201506818/Sakai+22+Release+Notes))

Sakai 21.5 ([release](http://source.sakaiproject.org/release/21.5/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/32296828929/21.5+Fixes+by+Tool) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17310646919/Sakai+21+Release+Notes))

Sakai 20.6 ([release](http://source.sakaiproject.org/release/20.6/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/32250068993/20.6+Fixes+by+tool) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17310646339/Sakai+20+Release+Notes))

Sakai 19.6 ([release](http://source.sakaiproject.org/release/19.6/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17310646740/19.6+Fixes+by+tool) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17297801232/Sakai+19+Release+Notes))

Sakai 12.7 ([release](http://source.sakaiproject.org/release/12.7/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17298719628/12.7+Fixes+by+Tool) |  [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17295934770/Sakai+12+Release+Notes))

Sakai 11.4 ([release](http://source.sakaiproject.org/release/11.4/) | [fixes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17294822150/Sakai+11.4+Bug+Fixes+and+other+improvements) | [notes](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17294820185/Sakai+11+Release+Notes))

For full history of supported releases please see our [release information on confluence](https://sakaiproject.atlassian.net/wiki/spaces/DOC/pages/17225318626/Release+Documentation).

## Under Development

- *Sakai 25.2* is the current development release of Sakai 25. It is expected to release Q2 2026.
- *Sakai 23.5* is the current development release of Sakai 23. It is expected to release Q4 2025.
- *Sakai 22.6* is the current development release of Sakai 22. It is expected to release Q4 2025.

## Accessibility
[The Sakai Accessibility Working Group](https://confluence.sakaiproject.org/display/2ACC/Accessibility+Working+Group) is responsible for ensuring that the Sakai framework and its tools are accessible to persons with disabilities. [The Sakai Ra11y plan](https://confluence.sakaiproject.org/display/2ACC/rA11y+Plan) is working towards a VPAT and/or a WCAG2 certification.

CKSource has created a GPL licensed open source version of their [Accessibility Checker](https://cksource.com/ckeditor/services#accessibility-checker) that lets you inspect the accessibility level of content created in CKEditor and immediately solve any accessibility issues that are found. CKEditor is the open source rich text editor used throughout Sakai. While the Accessibility Checker, due to the GPL license, can not be bundled with Sakai, it can be used with Sakai and the A11y group has created [instructions](https://confluence.sakaiproject.org/display/2ACC/CKEditor+Accessibility+Checker) to help you.

## Skinning Sakai
Documentation on how to alter the Sakai skin (look and feel) is here https://github.com/sakaiproject/sakai/tree/master/library

## Translating Sakai

Translation, internationalization and localization of the Sakai project are coordinated by the Sakai Internationalization/localization community. This community maintains a publicly-accessible report that tracks what percentage of Sakai has been translated into various global languages and dialects. If the software is not yet available in your language, you can translate it with support from the broader Sakai Community to assist you. 

From its inception, the Sakai project has been envisioned and designed for global use. Complete or majority-complete translations of Sakai are available in the languages listed below. 

### Supported languages
| Locale | Language|
| ------ | ------ |
| en_US | English (Default) |
| ca_ES | Catalán |
| de_DE | German |
| es_ES | Español |
| eu | Euskera |
| fa_IR | Farsi |
| fr_FR | Français |
| hi_IN | Hindi |
| ja_JP | Japanese |
| mn | Mongolian |
| pt_BR | Portuguese (Brazil) |
| sv_SE | Swedish |
| tr_TR | Turkish |
| zh_CN | Chinese |
| ar | Arabic |
| ro_RO | Romanian |
| bg | Bulgarian |
| sr | Serbian |

### Other languages

Other languages have been declared legacy in Sakai 19 and have been moved to [Sakai Contrib as language packs](https://github.com/sakaicontrib/legacy-language-packs).

## Community (contrib) tools
A number of institutions have written additional tools for Sakai that they use in their local installations, but are not yet in an official release of Sakai. These are being collected at https://github.com/sakaicontrib where you will find information about each one. You might find just the thing you are after!


