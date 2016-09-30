package red.svn.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnCleanup;
import org.tmatesoft.svn.core.wc2.SvnCommit;
import org.tmatesoft.svn.core.wc2.SvnGetInfo;
import org.tmatesoft.svn.core.wc2.SvnGetStatus;
import org.tmatesoft.svn.core.wc2.SvnInfo;
import org.tmatesoft.svn.core.wc2.SvnList;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevert;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnStatus;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.tmatesoft.svn.core.wc2.SvnUpdate;

import blue.lang.Cache;
import blue.lang.Trial;

public class SvnUser {
	
	// ===================================
	
	private static Cache<String, SvnUser> cache = new Cache<String, SvnUser>() {
		
		@Override
		protected SvnUser init(String key) {
			if(key == null){
				return new SvnUser();
			}
			return new SvnUser(key.split(":")[0], key.split(":")[1]);
		}
	};
	
	static SvnUser getUser(String username,String password){
		String key = username.toLowerCase() + ":" + password;
		return cache.get(key);
	}
	
	static SvnUser getDefaultUser(){
		return cache.get(null);
	}
	
	// ===================================
	
	private String username;
	private String password;
	
	// -------------
	
	private ISVNAuthenticationManager svnAuth;
	private SvnOperationFactory svnOp;
	private SVNClientManager client;
	
	// ===================================
	
	@SuppressWarnings("deprecation")
	private SvnUser(String username,String password){
		this();
		this.username = username;
		this.password = password;
		
		// prepare 
		this.svnAuth = SVNWCUtil.createDefaultAuthenticationManager(username, password);
    	this.svnOp.setAuthenticationManager(svnAuth);
	}
	private SvnUser(){
		this.svnOp = new SvnOperationFactory();
    	this.client = SVNClientManager.newInstance(svnOp);
	}
	
	// ===================================

	SvnOperationFactory svnOperationFactory() {
		return svnOp;
	}

	ISVNAuthenticationManager authData() {
		return svnAuth;
	}
	
	// ===================================
	
	public String getPassword() {
		return password;
	}
	public String getUsername() {
		return username;
	}
	
	// ------------------------------------
	
	/**
	 * Client Commit
	 * 
	 * @return
	 */
	SVNCommitClient clCommit(){
		return client.getCommitClient();
	}
	
	/**
	 * Client Status
	 * 
	 * @return
	 */
	SVNStatusClient clStatus(){
		return client.getStatusClient();
	}

	/**
	 * Perform checkout
	 * 
	 * @param svnTarget
	 * @param target
	 * @return
	 */
	long doCheckout(SVNURL svnTarget, File target) {
        synchronized (svnOp) {
    		SvnCheckout op = svnOp.createCheckout();
    		op.setSingleTarget(SvnTarget.fromFile(target));
    		op.setDepth(SVNDepth.INFINITY);
    		op.setSource(SvnTarget.fromURL(svnTarget));
    		try {
                System.out.println("SVN: checkout " + svnTarget);
    			return op.run();
    		} catch (SVNException e) {
    			e.printStackTrace();
    			return -1;
    		}
        }
	}
	
	/**
	 * Revert folder and sub-folders or single file
	 * 
	 * @param target
	 * @return
	 */
	boolean doRevert(File target){
        synchronized (svnOp) {
    		SvnRevert op = svnOp.createRevert();
    		op.setDepth(SVNDepth.INFINITY);
    		op.setPreserveModifiedCopies(false);
    		op.setRevertMissingDirectories(true);
    		op.setRevision(SVNRevision.HEAD);
    		op.setSingleTarget(SvnTarget.fromFile(target));
    		try {
                System.out.println("SVN: revert " + target);
    			op.run();
    			return true;
    		} catch (SVNException e) {
    			e.printStackTrace();
    			return false;
    		}
        }
	}
	
	/**
	 * Perform cleanup on target folder
	 * 
	 * @param target
	 * @return
	 */
	boolean doClean(File target){
        synchronized (svnOp) {
    		File cleanDir = target;
            if(!target.isDirectory()){
                cleanDir = target.getParentFile();
            }
            SvnCleanup clean = svnOp.createCleanup();
            clean.setDepth(SVNDepth.INFINITY);
            clean.setSingleTarget(SvnTarget.fromFile(cleanDir));
            clean.setDeleteWCProperties(true);
    		try {
                System.out.println("SVN: clean " + cleanDir);
    			clean.run();
    			return true;
    		} catch (SVNException e) {
    			e.printStackTrace();
    			return false;
    		}
        }
	}
	
	/**
	 * Perform update on target
	 * 
	 * @param target
	 * @return
	 */
	long doUpdate(File target){
	    synchronized (svnOp) {
	        SvnUpdate update = svnOp.createUpdate();
	        update.setDepth(SVNDepth.INFINITY);
	        update.setRevision(SVNRevision.HEAD);
	        update.setSingleTarget(SvnTarget.fromFile(target));
	        try {
                System.out.println("SVN: update " + target);
	            return update.run()[0];
	        } catch (SVNException e) {
	            e.printStackTrace();
	            return -1;
	        }
        }
	}
	
	/**
	 * Perform commit
	 * 
	 * @param target
	 * @param message
	 * @return
	 */
	long doCommit(File target, String message){
        synchronized (svnOp) {
    		SvnCommit commit = svnOp.createCommit();
            commit.setDepth(SVNDepth.INFINITY);
            commit.setSingleTarget(SvnTarget.fromFile(target));
            commit.setCommitMessage(message);
            try {
                System.out.println("SVN: commit " + target);
    			return commit.run().getNewRevision();
    		} catch (Exception e) {
    			e.printStackTrace();
    			return -1;
    		}
        }
	}
	
	SvnStatus doGetStatus(final SVNURL url){
		synchronized (svnOp) {
    		SvnGetStatus status = svnOp.createGetStatus();
    		status.setRemote(true);
    		status.setRevision(SVNRevision.HEAD);
    		status.setSingleTarget(SvnTarget.fromURL(url));
            try {
                System.out.println("SVN: status " + url);
    			return status.run();
    		} catch (Exception e) {
    			e.printStackTrace();
    			return null;
    		}
        }
	}
	
	List<String> doGetList(final SVNURL url){
        final List<String> ret = new ArrayList<String>();
		SvnList list = svnOp.createList();
		list.setDepth(SVNDepth.IMMEDIATES);
		list.setRevision(SVNRevision.HEAD);
		list.setSingleTarget(SvnTarget.fromURL(url));
		list.setReceiver(new ISvnObjectReceiver<SVNDirEntry>() {
            public void receive(SvnTarget target, SVNDirEntry object) throws SVNException {
                String name = object.getRelativePath();
                if(name!=null && !name.isEmpty()){
                	ret.add(name);
                }
            }
        });
		try{
			list.run();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}
	
	/**
	 * get remote SVN info
	 * 
	 * @param url
	 * @return
	 */
	SvnInfo doGetInfo(final SVNURL url){
        synchronized (svnOp) {
    	    return new Trial<SvnInfo>(5,5000) {
    
                @Override
                protected SvnInfo doTry() throws Throwable {
                    SvnGetInfo getInfo = svnOp.createGetInfo();
                    getInfo.setRevision(SVNRevision.HEAD);
                    getInfo.setFetchExcluded(false);
                    getInfo.setSingleTarget(SvnTarget.fromURL(url));
                    System.out.println("SVN: info " + url);
                    return getInfo.run();
                }
            }.get();
        }
     }
//        int nTry = 5;
//        while(nTry-->0){
//    		try {
//    	        return getInfo.run();
//    		} catch (SVNException e) {
//    		}
//    		try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                return null;
//            }
//        }
//        return null;
//	}
	
	
	
	// -----------------------------------------
	//				log query
	// -----------------------------------------
	
	public class SvnLogQuery {
		private long limit;
		private SVNURL url;
		private SvnRevisionRange revRange;
		private SvnLogQuery(SVNURL url){
			this.url = url;
			limit = Long.MAX_VALUE;
			revRange = SvnRevisionRange.create(SVNRevision.HEAD, SVNRevision.create(1));
		}
		
		public SvnLogQuery setLimit(long limit){
			this.limit = limit;
			return this;
		}
		
		public SvnLogQuery setRange(SvnRevisionRange range){
			this.revRange = range;
			return this;
		}
		
		public SvnLogQuery setRange(long max, long min){
			return setRange(SvnRevisionRange.create(SVNRevision.create(max), SVNRevision.create(min)));
		}
		
		public SvnLogQuery setRangeMin(long min){
			return setRange(SvnRevisionRange.create(revRange.getStart(), SVNRevision.create(min)));
		}
		
		public SvnLogQuery setRangeMax(long max){
			return setRange(SvnRevisionRange.create(SVNRevision.create(max), revRange.getEnd()));
		}
		
		public SVNLogEntry[] perform(){
	        synchronized (svnOp) {
                return new Trial<SVNLogEntry[]>(5,5000){
                    @Override
                    protected SVNLogEntry[] doTry() throws Throwable {
                        SvnLog log = svnOp.createLog();
                        log.setSingleTarget(SvnTarget.fromURL(url));
                        log.setDiscoverChangedPaths(true);
                        log.addRange(SvnRevisionRange.create(SVNRevision.HEAD, SVNRevision.create(1)));
                        log.setLimit(limit);
                        ArrayList<SVNLogEntry> ret = new ArrayList<SVNLogEntry>();
                        System.out.println("SVN: log " + url);
                        log.run(ret);
                        return ret.toArray(new SVNLogEntry[ret.size()]);
                    }
                    
                }.get();
	        }
		}
		
	}
	
	// -----------------------------------------
	
	SvnLogQuery doGetLog(SVNURL url){
	    return new SvnLogQuery(url);
	}

	URL doGetOrigin(final SVNURL url){
        synchronized (svnOp) {
    	    return new Trial<URL>(5,5000){
    	        @Override
    	        protected URL doTry() throws Throwable {
        	            SvnLog log = svnOp.createLog();
        	            log.setLimit(10);
        	            log.addRange(SvnRevisionRange.create(SVNRevision.create(1), SVNRevision.HEAD));
        	            log.setDiscoverChangedPaths(true);
        	            log.setDepth(SVNDepth.INFINITY);
        	            log.setStopOnCopy(true);
        	            log.setSingleTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url.toString())));
                        System.out.println("SVN: log " + url);
        	            SVNLogEntry result = log.run();
        	            
        	            Map<String, SVNLogEntryPath> hist = result.getChangedPaths();
        	            if(hist.size() == 0){
        	                return null;
        	            }
        	            
        	            String svnPos = hist.keySet().iterator().next();
        	            SVNLogEntryPath origin = result.getChangedPaths().get(svnPos);
        	            if(origin.getCopyPath() == null){
        	                return new URL(url.toString());
        	            }
        	            String ret = url.toString().replace(origin.getPath(), origin.getCopyPath());
        	            return new URL(ret);
        	        }
        	}.get();
        }
	}
}
