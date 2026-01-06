package localloginfilter.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.mendix.core.Core;
import com.mendix.core.action.user.LoginAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.AuthenticationRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.ISession;

import localloginfilter.proxies.constants.Constants;
import system.proxies.User;

public class LocalLoginFilterAction extends LoginAction {
	
	private static ILogNode LOGGER = Core.getLogger("LocalLoginFilter");
	private Map<String, ? extends Object> params;
	
	public LocalLoginFilterAction(IContext context, Map<String, ? extends Object> params) {
		super(context, params);
		this.params = params;
	}
	
	@Override
	public ISession executeAction() throws Exception {
		String microflow = Constants.getFilterMicroflow();
		String username = (String) params.get(USER_NAME_PARAM);
		
		if(microflow != null && !microflow.isEmpty()) {
			ISession newSession = super.executeAction();
			if (newSession != null) {
				IContext sysContext = Core.createSystemContext();
				
				LOGGER.debug("Checking if " + username + " is allowed to login.");
				Map<String, IDataType> inputParameters = Core.getInputParameters(microflow);
				String argument = null;
				for (Entry<String, IDataType> entry : inputParameters.entrySet()) {
					if (entry.getValue().isMendixObject() && entry.getValue().getObjectType().equals(User.getType())) {
						argument = entry.getKey();
					}
				}
				if (argument == null) {
					LOGGER.error("Unable to find User parameter for " + microflow);
					throw new AuthenticationRuntimeException("Unable to execute login filter microflow.");
				}
				
				Object result = Core.microflowCall(microflow)
						.withParam(argument, newSession.getUser(sysContext).getMendixObject())
						.execute(sysContext);
				
				if (result != null && result instanceof Boolean) {
					if ((Boolean)result) {
						return newSession;
					} else {
						Core.logout(newSession);
						LOGGER.warn("User " + username + " is not allowed to login because it filtered.");
						throw new AuthenticationRuntimeException("Unable to login " + username + " because the user is filtered.");
					}
				} else {
					LOGGER.error("Microflow " + microflow + " did not return a boolean: " + result);
					throw new AuthenticationRuntimeException("Incorrect implementation of filter microflow.");
				}
			} else {
				return null;
			}
		} else {
			LOGGER.warn("Blocked login for " + username + " because local logins are not alllowed.");
			throw new AuthenticationRuntimeException("Local logins are not allowed.");
		}
	}
}