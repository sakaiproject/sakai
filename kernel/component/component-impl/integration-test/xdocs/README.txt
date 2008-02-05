Testing component configuration is a little tricky if we want to avoid
undue dependencies on the internal logic of existing real-life Sakai
components. To avoid that problem, the component manager integration
test relies on a local testing-only component being built and deployed.
As a result, the test requires two steps:

# First, build and deploy the test component.
mvn  -Dmaven.test.skip=true clean install sakai:deploy

# Then run the test.
mvn clean test
