/**
 * Copyright 2000-2013 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.nexus.plugins.gitlab.client.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserStatus;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GitlabUser {
    @JsonProperty
    private final Integer id;
    @JsonProperty
    private final String username;
    @JsonProperty
    private final String email;
    @JsonProperty
    private final String name;

    @JsonProperty
    private final List<Map<String, String>> identities;

    @JsonProperty
    private final String private_token;
    @JsonProperty
    private final String state;
    @JsonProperty
    private final String created_at;
    @JsonProperty
    private final Boolean is_admin;
    @JsonProperty
    private final Boolean can_create_group;
    @JsonProperty
    private final Boolean can_create_team;
    @JsonProperty
    private final Boolean can_create_project;


    public GitlabUser() {
        id = null;
        username = "";
        email = "";
        name = "";
        identities = new ArrayList<>();
        private_token = "";
        state = "";
        created_at = "";
        is_admin = false;
        can_create_group = false;
        can_create_team = false;
        can_create_project = false;
    }

    GitlabUser(String name) {
        id = null;
        username = "";
        email = "";
        this.name = name;
        identities = new ArrayList<>();
        private_token = "";
        state = "";
        created_at = "";
        is_admin = false;
        can_create_group = false;
        can_create_team = false;
        can_create_project = false;
    }

    public String getUsername() {
        return username;
    }

    public User toUser() {
        User user = new DefaultUser();
        String fullName = nullToEmpty(this.name);
        String firstName = fullName;
        String lastName = "";
        int split = fullName.indexOf(',');

        if (split > 0) {
            lastName = fullName.substring(0, split).trim();
            firstName = fullName.substring(split + 1).trim();
        } else {
            split = fullName.indexOf(' ');
            if (split > 0) {
                firstName = fullName.substring(0, split).trim();
                lastName = fullName.substring(split + 1).trim();
            }
        }

        user.setUserId(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailAddress(nullToEmpty(email));
        user.setStatus(isActive() ? UserStatus.active : UserStatus.disabled);
        return user;
    }

    public boolean isActive() {
        return nullToEmpty(this.state).equalsIgnoreCase("active");
    }

    public boolean isAdmin() {
        return is_admin;
    }

    public List<Map<String, String>> getIdentities() {
        if (getProvider("gitlab") == null) {
            identities.add(generateGitlabProvider());
        }

        return identities;
    }

    // return the map if found or else null.
    public Map<String, String> getProvider(String provider) {
        for (Map<String, String> identity : identities) {
            String providerName = identity.get("provider");
            if (providerName != null && providerName.equals(provider)) {
                return identity;
            }
        }
        return null;
    }

    protected Map<String, String> generateGitlabProvider() {
        Map<String, String> gitlab = new HashMap<>();
        gitlab.put("id", id.toString());
        gitlab.put("username", username);
        gitlab.put("email", email);
        gitlab.put("name", name);
        gitlab.put("provider", "gitlab");
        return gitlab;
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(super.toString())
            .append("[")
            .append("id=").append(id)
            .append("username=").append(username)
            .append("email=").append(email)
            .append("name=").append(name)
            .append("private_token=").append("<HIDDEN>")
            .append("state=").append(state)
            .append("created_at=").append(created_at)
            .append("is_admin=").append(is_admin)
            .append("identities=").append(identities)
            .append("can_create_group=").append(can_create_group)
            .append("can_create_team=").append(can_create_team)
            .append("can_create_project=").append(can_create_project)
            .append("]").toString();
    }
}
