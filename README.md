Nexus authentication using Gitlab User token


Thanks to the Crowd Plugin:
https://github.com/PatrickRoumanoff/nexus-crowd-plugin

This was heavily influenced and adapted for Gitlab.

# Install

unzip the bundle under sonatype-work/nexus/plugin-repository

Start Nexus and use the Administration->Server panel to add OSS Crowd Authentication Realm to the list of active realms.


# Development

First: mvn generate-sources
- Generates Configuration stubs
Then you can make changes using the configuration classes.

Build:  mvn clean install
Generates: target/nexus-gitlab-token-auth-plugin-0.1.0-SNAPSHOT-bundle.zip
