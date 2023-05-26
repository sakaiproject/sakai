# SAKAI - Online Meetings tool

This is a tool for Sakai dedicated to the creation, management and use of virtual meeting rooms based on different online video conferencing providers. Instructors can schedule new meetings for their students on a site or group basis. Students can see a list of their scheduled meetings for each site and access them directly by clicking on them. Only members of the organization can access these meetings, unless the organizer allows guest access.

#### Features

- Instructors can schedule new meetings for their students.
- Instructors can control access to meetings by site or group.
- Students can access a list of their scheduled meetings.
- Search for scheduled meetings by name.
- Meetings can be added as events to the site calendar.
- Instructors can notify all meeting participants by e-mail.
- Only members of the organization can access Microsoft Teams meetings.
- Only the organizer can invite external users to access the Microsoft Teams meeting.
- Simple, fast and responsive interface.

## Current supported providers
- Microsoft Teams

## Prerequisites
You need:
- A Sakai 22.x instance or higher.
- For Microsoft Teams integration:
  - A Microsoft Azure Active Directory application.
  - Azure Active Directory users must have the same email in Sakai to be identified as members of the organization.

## Microsoft Teams
### Azure AD configuration
You must create a new application in the  _App Registrations_ section of the Azure Active Directory portal by clicking on the _New Registration_ button.

![App registrations](../microsoft-integration/docs/images/1.png "App registration")

You can enter a name and select the supported account types. The _Single tenant_ option is marked by default.

![Registering new app](../microsoft-integrationdocs/images/2.png "Registering new app")

To grant **Meetings tool** access to your registered Azure application, you will need a **client secret**. To obtain this, you can access the _Certificates & secrets_ section within the configuration page of your registered Azure application.

![Client secret](../microsoft-integrationdocs/images/3.png "Client secret")

Once the app is created, you need to configure the permissions for your registered Azure App in the _API Permissions_ section. To add a new permission you must click _Add a permission_, then select _Microsoft Graph_ and _Application Permissions_.

![Permissions](../microsoft-integrationdocs/images/4.png "Permissions")

The permissions to enable are defined in the following table:

```sh
Directory.Read.All
Directory.ReadWrite.All
Group.Read.All
OnlineMeetings.ReadWrite.All
User.Read.All
-- Chat Messages
Chat.Read.All
ChatMessage.Read.All
-- Create link
Files.ReadWrite.All
Sites.ReadWrite.All
Sites.Manage.All
```

Then you must click on the _Grant admin consent_ button for your Azure directory.

### Application access directives
In order for Sakai to manage meetings on its own, without user authentication by Microsoft, you need to set up an application user and its access policies. You can read the official Microsoft documentation on these steps:

https://docs.microsoft.com/en-us/graph/cloud-communication-online-meeting-application-access-policy

#### Summary
    #Install Microsoft Teams Poweshell Module: https://learn.microsoft.com/en-us/microsoftteams/teams-powershell-install

    #Get version
    $PSVersionTable.PSVersion

    #Install module
    Install-Module -Name PowerShellGet -Force -AllowClobber
    Install-Module -Name MicrosoftTeams -Force -AllowClobber

    #Enable script running
    Set-ExecutionPolicy Unrestricted

    #Connect to Teams
    Import-Module MicrosoftTeams
    $userCredential = Get-Credential
    Connect-MicrosoftTeams -Credential $userCredential

    #Create policy
    New-CsApplicationAccessPolicy -Identity Test-policy -AppIds "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" -Description "OnlineMeetings Access Policy"

    #Grant to specific user (sakai@nkrd.onmicrosoft.com)
    Grant-CsApplicationAccessPolicy -PolicyName Test-policy -Identity "yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy"

    #(Optional)Grant Global
    Grant-CsApplicationAccessPolicy -PolicyName Test-policy -Global

### Access to Protected APIs
To get Microsoft chat messages using application permissions, we need to request access to this protected API: https://learn.microsoft.com/en-us/graph/teams-protected-apis

## Future plans and Roadmap

- Improve documentation.
- Improve Microsoft permissions for institutions using Microsoft accounts.
- Reuse the meeting card component in other tools like Lessons or create a meeting widget.
- Support other webconference providers like Zoom or BBB depending on funding or contributions.

## Contact
If you have any questions please contact the devs at **Entornos de Formacion S.L.** at sakaigers@edf.global
