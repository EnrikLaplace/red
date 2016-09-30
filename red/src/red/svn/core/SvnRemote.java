package red.svn.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc2.SvnInfo;

import blue.lang.Cached;
import blue.util.Utils;
import red.svn.core.SvnUser.SvnLogQuery;

public class SvnRemote extends SvnObject {
	
	// ============================
	
	private URL target;
	
	// ---------------------
	private SVNURL svnTarget;
	
	// ----------------------
	private Cached<SvnInfo> cached_svnInfo = new Cached<SvnInfo>() {
		@Override
		protected SvnInfo update() {
			return user().doGetInfo(svnTarget);
		}
		public boolean isExpired() {
			return getLastEdit().getTime() > lastUpdate();
		};
	};
	private Cached<Date> cached_editTime = new Cached<Date>(10000) {
		@Override
		protected Date update() {
//			System.out.println("SVN: last edit " + svnTarget);
			try {
				return repo().info(".", -1).getDate();
			} catch (SVNException e) {
				e.printStackTrace();
				return null;
			}
		}
	};

	private ISVNAuthenticationManager auth;
	
	// ============================
	
	SvnRemote(URL target){
		this(target, target.getUserInfo()==null?null:
		    SvnUser.getUser(target.getUserInfo().split(":")[0], target.getUserInfo().split(":")[1]));
	}
	
	SvnRemote(URL target, SvnUser user){
		super(user);
		try {
            this.target = new URL(URI.create(target.toString()).normalize().toString());
        } catch (Exception e1) {
            this.target = target;
        }
		
		// others
		try {
			svnTarget = SVNURL.parseURIEncoded(this.target.toString());
		} catch (SVNException e) {
			throw new RuntimeException("Invalid svn target: " + target.toString());
		}
		if(user != null) {
	        changeUser(user);
		}
	}
	
	// -----------------------------------

	/**
	 * Open SvnRemote object
	 * 
	 * @param url
	 * @return
	 */
	public static SvnRemote connect(URL url){
		return new SvnRemote(url);
	}
	
	/**
	 * Open SvnRemote object
	 * 
	 * @param url
	 * @param user
	 * @param pass
	 * @return
	 */
	public static SvnRemote connect(URL url, String user, String pass){
		return new SvnRemote(url).auth(user, pass);
	}
	
	// ====================================

	SVNRepository repo = null;
	protected SVNRepository repo(){
		if(repo != null){
			repo.closeSession();
			return repo;
		}
		try {
			repo = SVNRepositoryFactory.create(svnTarget);
		} catch (SVNException e) {
			throw new RuntimeException(e);
		}
		if(auth != null) {
			repo.setAuthenticationManager(auth);
		}
		return repo;
	}
	
	@Override
	void changeUser(SvnUser user) {
		super.changeUser(user);
		auth = user.authData();
	}
	
	// ====================================
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SvnRemote){
			return ((SvnRemote)obj).target.equals(target);
		}
		return super.equals(obj);
	}
	
	public URL getUrl(){
		return target;
	}
    
    public URL toSvnUrl(){
        return Utils.toUrl(target,"svn", user().getUsername(), user().getPassword());
    }
	
	public String getHost(){
		return target.getHost();
	}
	
	public String getPath(){
		return target.getPath();
	}
	
	public SVNNodeKind getInfoPath(String path){
		try {
			return repo().checkPath(path, -1);
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isDirectory(){
		return isDirectory("");
	}
	
	public boolean isDirectory(String path){
		return getInfoPath(path) == SVNNodeKind.DIR;
	}
	
	public boolean isFile(){
		return isFile("");
	}
	
	public boolean isFile(String path){
		return getInfoPath(path) == SVNNodeKind.FILE;
	}
	
	public String getName(){
		// return last element in path
		String nm = target.getPath();
		if(nm.endsWith("/") || nm.endsWith("\\")){
			nm = nm.substring(0,nm.length()-1);
		}
		if(nm.length() == 0){
			return target.getHost();
		}
		return Utils.splitAfter(nm, "/",true);
	}
	
	public SVNURL getSvnUri(){
		return svnTarget;
	}
	
	public long getRevision(){
		return getInfo().getRevision();
	}
	
	public Date getLastEdit(){
		return cached_editTime.get();
	}
	
	public SvnRemote auth(String user,String pass){
		super.login(user, pass);
		return this;
	}
	
	public SvnRemote resolve(String path){
		return new SvnRemote(resolvePath(path),user());
	}

	public boolean exists() {
		return isDirectory() || isFile();
	}
	
	
	public SvnInfo getInfo(){
		
		return cached_svnInfo.get();
	}
	
	public URL getOrigin(){
	    return user().doGetOrigin(svnTarget);
	}
	
	public SvnLogQuery getHistory(){
	    return user().doGetLog(svnTarget);
	}
	
	public SvnLogQuery searchHistory(){
		return user().doGetLog(svnTarget);
	}
	
	public List<String> list(){
	    return user().doGetList(svnTarget);
	}
	
	public SvnLocal checkout(File target){
		// cannot perform checkout if is a file
		if(!isDirectory("")){
			return null;
		}
		if(target.exists()){
			// check
			if(target.isFile()){
				throw new RuntimeException("Target "+target.getAbsolutePath()+" is not a directory");
			}
		}
		// do checkout
		if(user().doCheckout(svnTarget, target) <= 0){
			return null;
		}
		return new SvnLocal(target, user());
	}
	
	public URL resolvePath(String path){
		try {
			SVNRepository repo = repo();
			return new URL(target.getProtocol() + "://" + target.getHost() + repo.getFullPath(repo.getRepositoryPath(path)));
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		} catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	/**
	 * open path to file
	 * 
	 * @param path
	 * @return
	 */
	public InputStream openStream(String path){
		ByteArrayOutputStream data = new ByteArrayOutputStream();
//		if(!isFile(path)){
//			// invalid target
//			return null;
//		}
		try {
			repo().getFile(path, -1, null, data);
			return new ByteArrayInputStream(data.toByteArray());
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toString() {
		return svnTarget.toString();
	}
	
	
	
	
//	public static void main(String[] args) throws MalformedURLException {
//		SvnRemote svn = new SvnRemote(new URL("http://cvsbo.cedac.local/svn/webcontoc/backend/webcontoc-access-control/trunk/pom.xml")).auth("matteo.rubini", "Pomodoro33");
////		SVNLogEntry[] list = svn.auth("matteo.rubini", "Pomodoro33").searchHistory().setLimit(30).perform();
////		System.out.println(list.length);
//		System.out.println(svn.isDirectory() || svn.isFile());
////		System.out.println(svn.checkout(new File("C://Users//Matteo//Desktop//test//lol")));
////		InputStream data = svn.openStream("");
////		System.out.println(new String(StreamUtils.read(data)));
//	}
}
