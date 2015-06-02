# Contributing to Sakai

Contributions to Sakai from all comers are welcome and encouraged.

Please use these guidelines and the information in [README.md](README.md) to assure that your contributions are compatible with our (evolving) workflow and practices.

## Jira

Bugs and features against Sakai are tracked in our [Jira instance](https://jira.sakaiproject.org/) and contributions must reflect a Jira reference number in the messages and git branch names (e.g., SAK-29469). To file or comment on a bug or feature, you will need a Jira account.

## Initial Git/GitHub setup and advice

You need to do some initial work to set your local environment. In the steps below, the following references are used:

   * ***local*** = A copy of Sakai on your workstation (this is where everyone typically does work)
   * ***origin*** = Your personal copy of Sakai on GitHub (you `clone` this repository on your workstation to make the ***local*** copy for everyday work)
   * ***upstream*** = Main Sakai GitHub project (everyone forks this project into their GitHub account to make the ***origin***)

To work on and contribute to Sakai:

* [Set up Git, create a GitHub account, and set your name and e-mail address correctly as Git global variables on your local workstation](https://help.github.com/articles/set-up-git/)

* [Fork](https://help.github.com/articles/fork-a-repo/) the [central Sakai repository](https://github.com/sakaiproject/sakai) to your own GitHub account

* `Clone` your ***origin*** repository onto your local workstation (this example uses HTTPS as the transport mechanism):

  `$ git clone https://github.com/GITHUBACCOUNTNAME/sakai`
 
* Add a `remote` to receive updates from the ***upstream*** repository:

  `$ git remote add upstream https://github.com/sakaiproject/sakai`

* Update your ***local*** repository frequently to stay up to date:

  `$ git checkout master` (switch to ***local*** `master` branch)
  
  `$ git pull upstream master` (`pull` in any ***upstream*** changes)
  
  `$ git push origin master` (`push` the changes up to the ***origin***)

## Working on bugs and features

#### Never work in your ***local*** `master` branch.

This branch should always be the same as what is in Sakai's ***upstream*** `master` and if you make commits into your ***local*** `master`, it complicates things.

### General workflow

To fix a bug or add a feature, the general Git workflow is:

* Create a ***local*** branch using Jira reference for the branch name:

  `$ git checkout -b SAK-29469`


* Do work

* Add changed or new files:

  `$ git add -u`

* Make your ***local*** commit:

  `$ git commit -m "SAK-29469 Add some documentation about contributing"

* Share branch back to ***origin***:

  `$ git push origin SAK-29469`

* [Create a pull request (PR)](https://help.github.com/articles/creating-a-pull-request/) using GitHub from the branch against the ***upstream*** `master` for review by others

### Respond to a pull request (PR) by updating proposed changes

You will often receive friendly advice to improve or fix the changes you proposed in a p. To update your changes and maintain the existing PR, you should:
  
* Change to your ***local*** branch:

  `$ git checkout SAK-29469`


* Make changes and/or improvements in response to PR comments

* Add changed or new files:

  `$ git add -u`

* Update existing commit:

  `$ git commit --amend -C HEAD`

* Share your new ***local*** changes back to the ***origin*** branch and the original PR (by force):

  `$ git push -f origin SAK-29469`

* Make a comment on the existing PR to alert reviewers to your changes

## Questions and support

More documentation and notes can be found in the [Git Setup confluence page](https://confluence.sakaiproject.org/display/SAKDEV/Git+Setup).

Questions are always welcome on the [Sakai Developer mailing list](https://groups.google.com/a/apereo.org/d/forum/sakai-dev). To join the list, send an email to sakai-dev+subscribe@apereo.org.
