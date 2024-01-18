package com.mysite.core.models;
import com.adobe.granite.ui.components.rendercondition.RenderCondition;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class)
public class UserGroupRenderCondition {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupRenderCondition.class);

    @Self
    private SlingHttpServletRequest request;

    @SlingObject
    private ResourceResolver resourceResolver;

    @Inject
    private String groups;

    @PostConstruct
    public void init() {
        List<String> allowedGroups = Arrays.asList(groups.split(","));
        LOGGER.info("allowedGroups ", allowedGroups.toString());
        UserManager userManager = resourceResolver.adaptTo(UserManager.class);
        if (userManager == null) {
            return;
        }
        boolean belongsToGroup = false;
        try {
            Authorizable currentUser = userManager.getAuthorizable(resourceResolver.getUserID());
            Iterator<Group> groupsIt = currentUser.memberOf();
            while (groupsIt.hasNext()) {
                Group group = groupsIt.next();
                String groupId = group.getID();
                if (allowedGroups.stream().anyMatch(g -> g.equals(groupId))) {
                    belongsToGroup = true;
                    break;
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Exception in UserGroupRenderCondition ", e);
        }
        request.setAttribute(RenderCondition.class.getName(),
                new SimpleRenderCondition(belongsToGroup));
    }
}