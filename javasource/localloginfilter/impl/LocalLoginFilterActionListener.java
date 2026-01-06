package localloginfilter.impl;

import com.mendix.core.action.user.LoginAction;
import com.mendix.systemwideinterfaces.core.UserActionListener;

public class LocalLoginFilterActionListener extends UserActionListener<LoginAction> {
	
	public LocalLoginFilterActionListener() {
		super(LoginAction.class);
	}
	
	@Override
	public boolean check(LoginAction action) {
		return true;
	}
}
