package oidc.implementation.common;

import com.mendix.systemwideinterfaces.core.IContext;
import oidc.proxies.Role;

import java.text.ParseException;
import java.util.List;

public final class RoleAssignmentUtil {

   public static List<Role> getIdpRolesFromToken(final IContext context, final String encodedToken, final String name) throws ParseException {

        return TokenUtils.getClaimValueAsStringArray(encodedToken, name).stream().map(
                roleName -> {
                    Role role = new Role(context);
                    role.setRoleName(roleName);
                    return role;
                }
        ).toList();
    }

}
