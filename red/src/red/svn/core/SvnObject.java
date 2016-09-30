package red.svn.core;

abstract class SvnObject {
	
	private SvnUser user;
	
	// ==================================
	
	protected SvnObject() {
		this(null);
	}
	
	protected SvnObject(SvnUser user) {
		this.user = user;
		if(user == null){
			this.user = SvnUser.getDefaultUser();
		}
	}
	
	// ==================================
	
	SvnUser user() {
		return user;
	}
	
	boolean login(String user, String pass){
		changeUser(SvnUser.getUser(user, pass));
		return true;
	}

	void changeUser(SvnUser user) {
		this.user = user;
	}
	
	SvnUser getUser() {
		return user;
	}
}
