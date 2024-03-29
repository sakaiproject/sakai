# Contributing to Sakai

Contributions to Sakai from all comers are welcome and encouraged.

Please use these guidelines and the information in [README.md](README.md) to assure that your contributions are compatible with our (evolving) workflow and practices.

## Code of conduct

The Sakai project [participates in the Apereo Welcoming Policy](CODE_OF_CONDUCT.md).

## Contributor License Agreement
Before a code submission will be accepted, you will need to submit a Contributor License Agreement (CLA). This is a one-off process that should only take a few moments to complete. See: https://www.apereo.org/licensing/agreements

To check the status of a CLA, please visit: http://licensing.apereo.org/

## JIRA

Bugs and features against Sakai are tracked in our [JIRA instance](https://jira.sakaiproject.org/) and contributions must reflect a JIRA reference number in the messages and git branch names (e.g., SAK-29469), but please don't put these references in the code as over time files become full of them and the same information can be retrieved from git. To file or comment on a bug or feature, you will need a JIRA account.

## Overview of Sakai Development
Please take a look at our [Confluence pages](https://confluence.sakaiproject.org/display/BOOT/Programmer%27s+Cafe) for pointers on getting started with Sakai development.

## Pull Requests

Generally when you make a change you will create a pull request that allows other people to examine the code and comment on the changes you've made. Typically you may get comments about:

 * Code style - Does it match the existing code in the file?
 * Indentation - Are you keeping to the same indentation format (tabs/spaces) and aligning it?
 * Internationalisation - Does your code support running in a different language?
 * Accessibility - Are you supporting accessibility best practices?
 * Technical Approach - Is this a sensible technical approach? are there any obvious performance implications?
 * Minimal Changes - Are you changing only the lines needed to fix this bug add this feature (no bulk reformatting)?
 * Single Issue - Are you fixing one issue?
 * Tests - Are the tests passing and have you added tests where sensible/possible?
 * Commit comments - Have you linked to the issue you're working on? Have you explained why this is the right fix?

## Initial Git/GitHub setup and advice

You need to do some initial work to set your local environment. In the steps below, the following references are used:

   * ***local*** = A copy of Sakai on your workstation (this is where everyone typically does work)
   * ***origin*** = Your personal copy of Sakai on GitHub (you `clone` this repository on your workstation to make the ***local*** copy for everyday work)
   * ***upstream*** = Main Sakai GitHub project (everyone forks this project into their GitHub account to make the ***origin***)

To work on and contribute to Sakai:

* [Set up Git, create a GitHub account, and set your name and e-mail address correctly as Git global variables on your local workstation](https://help.github.com/articles/set-up-git/)

* [Fork](https://help.github.com/articles/fork-a-repo/) the [central Sakai repository](https://github.com/sakaiproject/sakai) to your own GitHub account

* `Clone` your ***origin*** repository onto your local workstation (this example uses HTTPS as the transport mechanism):

  `git clone https://github.com/GITHUBACCOUNTNAME/sakai`
 
* Add a `remote` to receive updates from the ***upstream*** repository:

  `git remote add upstream https://github.com/sakaiproject/sakai`

* Update your ***local*** repository frequently to stay up to date:

  `git checkout master` (switch to ***local*** `master` branch)
  
  `git pull upstream master` (`pull` in any ***upstream*** changes)
  
  `git push origin master` (`push` the changes up to the ***origin***)

## Working on bugs and features

#### Never work in your ***local*** `master` branch.

This branch should always be the same as what is in Sakai's ***upstream*** `master` and if you make commits into your ***local*** `master`, and those commits are not accepted into the Sakai master repository, you will forever be maintaining them yourself, which can get very complicated. To make life easier for yourself, **use a branch for everything**.

### General workflow

To fix a bug or add a feature, the general Git workflow is:

### Single issue per pull request
Generally issues and pull requests will have a one-to-one relationship, see following example:

1. Create a ***local*** branch using JIRA reference for the branch name:

  `git checkout -b SAK-12345`

2. Do some work

3. Add changed or new files:

   `git add -u`

4. Make your ***local*** commit:

    `git commit -m "SAK-12345 Assignments add option x when creating an assignment"`

5. Commit messages should follow the following format

    `<issue key> <component> <brief description>`

    If more commits are added then the subsequent commit message are not important as they will be squashed

6. Share branch back to ***origin***:

    `git push origin SAK-29469`

7. [Create a pull request (PR)](https://help.github.com/articles/creating-a-pull-request/) using GitHub from the branch against the ***upstream*** `master` for review by others

8. When this PR is merged the option to ***Squash and Merge*** should be used

### Multiple issues per pull request
Sometimes it is helpful to fix several issues with a single pull request this might be because the changes are related in some way

1. Create a ***local*** branch using JIRA reference for the branch name:

   `git checkout -b SAK-12345`

2. Do some work

3. Add changed or new files:

   `git add -u`

4. Make your ***local*** commit:

   `git commit -m "SAK-12345 Assignments add option x when creating an assignment"`

5. Commit messages should follow the following format

   `<issue key> <component> <brief description>`

6. Repeat steps 2 and 3

7. Make your ***local*** commit:

   `git commit -m "SAK-23456 Assignments add option y when creating an assignment"`

8. Repeat steps 6 and 7 adding more commits

    What is paramount in this workflow is that the commits must be managed by the author and there should be exactly one commit per issue

9. Share branch back to ***origin***:

   `git push origin SAK-12345`

10. When creating the PR be sure each <issue-key> is mentioned, as this will like the pull request to each issue in the issue tracking system

11. When this PR is merged the option ***Rebase and Merge*** should be used

### Respond to a pull request (PR) by updating proposed changes

You will often receive friendly advice to improve or fix the changes you proposed in a p. To update your changes and maintain the existing PR, you should:
  
* Change to your ***local*** branch:

  `git checkout SAK-29469`

* Make changes and/or improvements in response to PR comments

* Add changed or new files:

  `git add -u`

* Make a new commit (normal workflow in a ***Single issue per pull request***):

  `git commit -m "updated x per requested changes`

* Or update an existing commit (normal workflow in ***Multiple issues per pull request***):

  `git commit --amend -C HEAD`
  or
  `git rebase -i HEAD~<# of commits to go back and edit>`

  rebasing commits is a way to rewrite history and git documentation should be consulted

* Share your new ***local*** changes back to the ***origin*** branch and the original PR (by force is required for amending commits. You should only ever push by force into your own repo):

  `git push -f origin SAK-29469`

* Make a comment on the existing PR to alert reviewers to your changes

### Security Issues

Security issues are typically handled differently than regular bugs to try to reduce visibility. To get a security fix into Sakai do the following.
 
1. Get access to the security list and security JIRA work group https://confluence.sakaiproject.org/display/SECWG/Security+Policy by emailing the contact on that page
2. Submit a JIRA ticket with security issue indicated (it's a dropdown box) detailing the security issue
3. When fixing the issue, either:
   1. Request access to private Github repo https://github.com/sakaiproject/sakai-security and submit a pull request.
      * Make sure you push your branch to the (private) sakai-security repo rather than your (public) origin
      * The pull request should only have the SAK/KNL/etc number, no additional comments about what was fixed 
   2. If you are unable to submit a pull request, add a diff against the JIRA
4. Pull requests and new issues are generally reviewed and merged at the next Security WG or Core Team meeting prior to the next minor release of Sakai. 
5. For high priority issues email the security list directly

## Questions and support

More documentation and notes can be found in the [Git Setup confluence page](https://confluence.sakaiproject.org/display/SAKDEV/Git+Setup).

Questions are always welcome on the [Sakai Developer mailing list](https://groups.google.com/a/apereo.org/d/forum/sakai-dev). To join the list, send an email to sakai-dev+subscribe@apereo.org.
