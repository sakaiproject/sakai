# Sakai Automated Tests

## Before you begin

To run this project you need a Selenium webdriver.
This will be simplified soon, but for now, get the geckodriver and unzip it somewhere.

If you don't want to run this project, just contribute feature files to it, see `Contributing` below.

## Usage

To run the tests and generate the report, run:
`mvn clean integration-test -Dwebdriver.gecko.driver=/path/to/webdriver`

## Generating stepdef code
If you create a feature file, Cucumber can build the missing stepdef scaffolding you need to implement.
Run `mvn test`

## Contributing

The aim of this project is to consolidate the testing efforts for Sakai QA and bring testers and developers together.

### For Testers
Create a feature file. This is the best thing you can do to help this project advance.
Please keep to the established language convention. 
Also, using a Cucumber plugin helps highlight what steps have automated tests and what ones are new.

Please add the @manual tag to all feature files so that they don't try to run.

### For Developers
If you feel like automating some feature files, check out the existing automated tests and helpers.
Otherwise, adding a feature file is a great place to help.

If you do automate some features or scenarios, add the @automated tag to it.


## Configuration

This section under construction:

-Dselenium.profile=firefox|chrome
-Dwebdriver.gecko.driver=/path/to/gecko/driver
-Dwebdriver.chrome.driver=/path/to/chrome/driver
