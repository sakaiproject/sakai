# Content

Content is the project which provides the 'Resources' tool in Sakai.

## Copyright Options within the 'Resources' tool in Sakai

When uploading files to the 'Resources' tool, Sakai can offer or require that users consider the copyright of the files they are uploading.

Out of the box, Sakai provides six default copyright options to be selected for files uploaded to the 'Resources' tool:

1. "Material is in public domain." (public_domain)
2. "I hold copyright." (hold_copyright)
3. "I have obtained permission to use this material." (have_permission)
4. "Copyright status is not yet determined." (not_determined)
5. "Use copyright below." (use_below)
6. "Material is subject to fair use exception." (fair_use)

For more information on configuring this feature, such as defining the default copyright selection, requiring explicit selection of a copyright selection, defining a custom list of
copyright options and setting up documentation around copyright options (default or custom), please see the README.md located at:

```
/content/content-copyright/README.md
```

# Drop Box

The Drop Box tool is also contained within the content project. Drop Box specific documentation can be found [here](./DropBox.md).

## Release Notes

### Sakai 10.0

SAK-25371 - The feature will add "Print File" action option to the Resources tool for those print-eligible-type resource items.
The printing will be done on item-by-item base, not with a batch mode. The feature will save user effort of downloading the resource first and then upload to a  server.

The following is required to enable the "Print File" choice in the Resources tool:

1. Implement org.sakaiproject.content.api.ContentPrintService
2. Configure implementation in your provider's components.xml
	* You'll need to first comment out the default implementation in the kernel (kernel-component/src/main/webapp/WEB-INF/content-components.xml), and then configure your provider
	* This is an example of UM's provider implementation:

```
<beans>
	<bean id="org.sakaiproject.content.api.ContentPrintService"
		class="org.sakaiproject.content.impl.ContentPrintServiceUnivOfMichImpl"
		singleton="true">
	</bean>
</beans>
```

3. Define print_server_url in your sakai.properties
4. An example provider implementation is available at https://source.sakaiproject.org/svn//msub/umich.edu/ctools/ctools-providers/branches/2.9.x-from-2.7.x/contentprint/

### Legacy conversion tooling

The legacy scripts that converted XML content rows to the binary
`BINARY_ENTITY` format were removed in Sakai 25. All supported releases now
assume the modern schema and binary serializers, so no manual conversion is
required when upgrading.
