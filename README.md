# Nexus authentication using Gitlab User token

## Install

* Unzip the bundle (target/nexus-gitlab-token-auth-plugin-0.1.0-SNAPSHOT-bundle.zip) under sonatype-work/nexus/plugin-repository
* Copy & Edit the `sample/gitlab-plugin.xml` to: `sonatype-work/nexus/conf/gitlab-plugin.xml`
* Start Nexus and use the Administration->Server panel to add Gitlab Token Authentication Realm to the list of active realms.

### Configuration

```
 <?xml version="1.0" encoding="UTF-8"?>
 <gitlabConfiguration>
  <gitlabServerUrl>https://git.nexgen.neustar.biz</gitlabServerUrl>
  <defaultRoles>
    <defaultRole>person</defaultRole>
  </defaultRoles>
  <adminRoles>
    <adminRole>nx-admin</adminRole>
  </adminRoles>
 </gitlabConfiguration>
```


## Development

First: mvn generate-sources
- Generates Configuration stubs
Then you can make changes using the configuration classes.

Build:  mvn clean install -DskipTests=true
Generates: target/nexus-gitlab-token-auth-plugin-0.1.0-SNAPSHOT-bundle.zip

Now you can run Integration Tests: mvn test

### Credits

Thanks to the Crowd Plugin:
https://github.com/PatrickRoumanoff/nexus-crowd-plugin
This was heavily influenced and adapted for Gitlab.

