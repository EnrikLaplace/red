package red.svn.core;

import java.io.File;
import java.net.URL;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNStatus;

import blue.lang.Cached;

public class SvnLocal extends SvnObject {
	
	// ============================
	
	private File target;
	private SvnRemote remote;
	
	// ------------------------------
	
	private Cached<SVNStatus> cached_status = new Cached<SVNStatus>(10000) {
		
		@Override
		protected SVNStatus update() {
			try {
				return user().clStatus().doStatus(target, false);
			} catch (SVNException e) {
				e.printStackTrace();
				return null;
			}
		}
	};
	
	// ============================
	
	SvnLocal(File target){
		this(target,null);
	}
	
	SvnLocal(File target, SvnUser user){
		super(user);
		assert target.isDirectory();
		this.target = target;
		// detect remote
		updateRemote();
	}
	
	// ------------------------------

	/**
	 * Open svn folder
	 * 
	 * @param target
	 * @return
	 */
	public static SvnLocal open(String target){
		return open(new File(target));
	}
	
	/**
	 * Open svn folder
	 * 
	 * @param target
	 * @return
	 */
	public static SvnLocal open(File target){
		return new SvnLocal(target);
	}
    
    /**
     * Open svn folder
     * 
     * @param target
     * @return
     */
    public static SvnLocal open(File target, String username, String password){
        try{
            return new SvnLocal(target, SvnUser.getUser(username, password));
        }catch(RuntimeException ex){
            return null;
        }
    }
	
	/**
	 * Open svn folder with specified SVN
	 * 
	 * @param remoteSvn
	 * @param target
	 * @return
	 */
	public static SvnLocal open(URL remoteSvn, File target){
	    if(target.exists() && target.isDirectory()){
	        SvnLocal local = SvnLocal.open(target);
	        if(local.getRemoteUrl().equals(remoteSvn)) {
	            return local;
	        }
	    }
		return new SvnRemote(remoteSvn).checkout(target);
	}
	
	// ------------------------------
	
	@Override
	void changeUser(SvnUser user) {
		// status my change
		cached_status.invalidate();
		super.changeUser(user);
	}

	/**
	 * Detect remote SVN from folder/file
	 * 
	 * @param target
	 * @return
	 */
	private void updateRemote() {
		if(!target.exists()){
			throw new RuntimeException("Invalid folder: " + target);
		}
		// get SVN info
		try {
		    URL remotePath = new URL(getStatus().getRemoteURL().toString());
			remote = new SvnRemote(remotePath,user());
			if(!remote.isDirectory()){
				throw new RuntimeException("Invalid SVN folder: " + target);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid SVN folder: " + target);
		}
	}
	
	// ============================
	
	public SvnLocal auth(String user, String pass){
		login(user, pass);
		return this;
	}
	
	public SVNStatus getStatus(){
		return cached_status.get();
	}
	
	public boolean isLinked(){
		return remote != null;
	}
	
	public URL getRemoteUrl(){
		return remote.getUrl();
	}
	
	public long getRevision(){
		return getStatus().getRevision().getNumber();
	}
	
	public long getRemoteRevision(){
		return remote.getRevision();
	}
	
	public SvnRemote getRepo(){
		return remote;
	}

    public File toFile() {
        return target;
    }
	
	File resolvePath(String path){
		return new File(target, path);
	}
	
	public SvnLocal resolve(String path){
		return new SvnLocal(resolvePath(path), user());
	}
	
	// --------------------------------------
	
	/**
	 * SVN cleanup
	 * 
	 * @return
	 */
	public boolean clean(){
		return user().doClean(target);
	}
	
	/**
	 * SVN update
	 * 
	 * @return
	 */
	public long update(){
		long ret = user().doUpdate(target);
		cached_status.invalidate();
		return ret;
	}
	
	/**
	 * SVN Revert
	 * 
	 * @return
	 */
	public long revert(){
		// clean
		if(!user().doClean(target)){
			return -1;
		}
		// revert
		if(!user().doRevert(target)){
			return -1;
		}
		cached_status.invalidate();
		return getRevision();
	}
	
	/**
	 * SVN Commit
	 * 
	 * @param message
	 * @return
	 */
	public long commit(String message){
		return user().doCommit(target, message);
	}
	
	/**
	 * SVN Commit
	 * 
	 * @param message
	 * @return
	 */
	public long commit(String path, String message){
		return user().doCommit(resolvePath(path), message);
	}
	
	
	
//	public static void main(String[] args) {
//		SvnLocal svn = SvnLocal.open("C:/ibk/proevo/temp/svn/Cariparma Releases Parent Project/cariparma-backend-application");
//		System.out.println(svn.getRevision());
//		System.out.println(svn.clean());
//		System.out.println(svn.revert());
//	}
}
