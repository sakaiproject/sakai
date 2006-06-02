Outline of package usage:
-------------------------

$Id: readme_packages.txt,v 1.1 2005/01/06 18:39:01 esmiley.stanford.edu Exp $

./org/navigoproject: legacy from Navigo project, deprecated

./org/sakai: an authn implementation, not currently used, but NOT deprecated
./org/sakai/osid:
./org/sakai/osid/impl:
./org/sakai/osid/impl/authn:

./org/sakaiproject: all code that is used should be under here

./org/sakaiproject/jsf: modified versions of standard Sakai widgets (currently just wysiwyg)
./org/sakaiproject/jsf/component:
./org/sakaiproject/jsf/renderer:
./org/sakaiproject/jsf/tag:

SAMIGO
./org/sakaiproject/tool/assessment: highest level--  all Samigo-specific code goes here

BUNDLE
./org/sakaiproject/tool/assessment/bundle: resource bundles

BUSINESS OBJECTS
./org/sakaiproject/tool/assessment/business: business objects

./org/sakaiproject/tool/assessment/business/entity: objects that model some "thing"

./org/sakaiproject/tool/assessment/business/entity/asi: xml models

./org/sakaiproject/tool/assessment/business/entity/assessment/model: models for assessment

./org/sakaiproject/tool/assessment/business/entity/constants: classes with globs of constants

./org/sakaiproject/tool/assessment/business/entity/helper: xml model manipulation 
./org/sakaiproject/tool/assessment/business/entity/helper/assessment:
./org/sakaiproject/tool/assessment/business/entity/helper/item:
./org/sakaiproject/tool/assessment/business/entity/helper/questionpool:
./org/sakaiproject/tool/assessment/business/entity/helper/section:
./org/sakaiproject/tool/assessment/business/entity/properties:
./org/sakaiproject/tool/assessment/business/entity/questionpool:

./org/sakaiproject/tool/assessment/business/questionpool:

HIBERNATE

./org/sakaiproject/tool/assessment/data: Hibernate pojos
./org/sakaiproject/tool/assessment/data/dao:
./org/sakaiproject/tool/assessment/data/dao/assessment:
./org/sakaiproject/tool/assessment/data/dao/authz:
./org/sakaiproject/tool/assessment/data/dao/grading:
./org/sakaiproject/tool/assessment/data/dao/questionpool:
./org/sakaiproject/tool/assessment/data/dao/shared:

./org/sakaiproject/tool/assessment/queries: hibernate queries

API
./org/sakaiproject/tool/assessment/data/ifc:  api interfaces
./org/sakaiproject/tool/assessment/data/ifc/assessment:
./org/sakaiproject/tool/assessment/data/ifc/authz:
./org/sakaiproject/tool/assessment/data/ifc/grading:
./org/sakaiproject/tool/assessment/data/ifc/questionpool:
./org/sakaiproject/tool/assessment/data/ifc/shared:

DEV
./org/sakaiproject/tool/assessment/devtools: useful development tools, not needed to build and run samigo

./org/sakaiproject/tool/assessment/facade: osid like facade classes
./org/sakaiproject/tool/assessment/facade/authz:
./org/sakaiproject/tool/assessment/facade/manager:

JSF
./org/sakaiproject/tool/assessment/jsf: Samigo JSF components
./org/sakaiproject/tool/assessment/jsf/convert:
./org/sakaiproject/tool/assessment/jsf/renderer:
./org/sakaiproject/tool/assessment/jsf/renderer/util:
./org/sakaiproject/tool/assessment/jsf/tag:
./org/sakaiproject/tool/assessment/jsf/validator:

OSID
./org/sakaiproject/tool/assessment/osid: osid implementations, shielded from application by facades
./org/sakaiproject/tool/assessment/osid/assessment:
./org/sakaiproject/tool/assessment/osid/assessment/impl:
./org/sakaiproject/tool/assessment/osid/authz:
./org/sakaiproject/tool/assessment/osid/authz/impl:
./org/sakaiproject/tool/assessment/osid/impl:
./org/sakaiproject/tool/assessment/osid/questionpool:
./org/sakaiproject/tool/assessment/osid/questionpool/impl:
./org/sakaiproject/tool/assessment/osid/questionpool_0_6:
./org/sakaiproject/tool/assessment/osid/questionpool_0_6/impl:
./org/sakaiproject/tool/assessment/osid/shared:
./org/sakaiproject/tool/assessment/osid/shared/extension:
./org/sakaiproject/tool/assessment/osid/shared/impl:


SERVICE
./org/sakaiproject/tool/assessment/services: samigo services
./org/sakaiproject/tool/assessment/services/assessment:
./org/sakaiproject/tool/assessment/services/shared:


UI
./org/sakaiproject/tool/assessment/ui: JSF ui code

./org/sakaiproject/tool/assessment/ui/bean: ui beans/managed beans
./org/sakaiproject/tool/assessment/ui/bean/author:
./org/sakaiproject/tool/assessment/ui/bean/cms:
./org/sakaiproject/tool/assessment/ui/bean/delivery:
./org/sakaiproject/tool/assessment/ui/bean/evaluation:
./org/sakaiproject/tool/assessment/ui/bean/misc:
./org/sakaiproject/tool/assessment/ui/bean/questionpool:
./org/sakaiproject/tool/assessment/ui/bean/select:
./org/sakaiproject/tool/assessment/ui/bean/util:

./org/sakaiproject/tool/assessment/ui/listener: action and value changed listeners, note: we sometimes use these like Struts Actions
./org/sakaiproject/tool/assessment/ui/listener/author:
./org/sakaiproject/tool/assessment/ui/listener/delivery:
./org/sakaiproject/tool/assessment/ui/listener/evaluation:
./org/sakaiproject/tool/assessment/ui/listener/evaluation/util:
./org/sakaiproject/tool/assessment/ui/listener/index:
./org/sakaiproject/tool/assessment/ui/listener/misc:
./org/sakaiproject/tool/assessment/ui/listener/questionpool:
./org/sakaiproject/tool/assessment/ui/listener/select:
./org/sakaiproject/tool/assessment/ui/listener/util: utilities to make listener coding easier

./org/sakaiproject/tool/assessment/ui/model: model classes for things like option lists

UTIL
./org/sakaiproject/tool/assessment/util: utility classes

TEST
./test: test classes and data, not needed for samigo build and run
./test.sakaiproject.*: convention: test.{package} has classes to test {package} 
./test/data: test data
