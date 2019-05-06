SonarQube Jazz RTC SCM Plugin
=============================
[![Build Status](https://travis-ci.org/SonarQubeCommunity/sonar-scm-jazzrtc.svg)](https://travis-ci.org/SonarQubeCommunity/sonar-scm-jazzrtc)

## Description
This plugin implements SCM dependent features of SonarQube for [Jazz RTC](https://jazz.net/library/LearnItem.jsp?href=content/docs/rtc1.0-capabilities/scm.html) projects.

## Usage
This provider is a wrapper around 'lscm' command line utility. You need to have 'lscm' in the PATH.

Auto-detection will works if there is a .jazz5 folder in the project root directory. Otherwise you can force the provider using -Dsonar.scm.provider=jazz.

You can also configure some optional properties:

| Key | Description |
| --- | ----------- |
| sonar.jazzrtc.username | Username to be used for Jazz RTC authentication |
| sonar.jazzrtc.password.secured | Encrypted Password to be used for Jazz RTC authentication |
| sonar.jazzrtc.lscm | File Path to the lscm binary |
| sonar.jazzrtc.proxy | Proxy server to be used to connect to the repository |
| sonar.jazzrtc.mapfile | File Path to the json file mapping usernames to email addresses |

## Known Limitations
* Blame is not executed in parallel since it is not supported by lscm annotate.
* 'lscm' annotate returns information from server for the given file in latest revision (whatever is the status of your local workspace).
