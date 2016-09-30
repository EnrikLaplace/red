package red.svn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import red.svn.core.SvnRemote;

public class SvnUrlConnection extends URLConnection {
	
	private SvnRemote svn;

	public SvnUrlConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		URL httpUrl = new URL(url.toString().replace("svn://", "http://"));
		svn = SvnRemote.connect(httpUrl);
		if(httpUrl.getUserInfo() != null){
		    String[] userData = httpUrl.getUserInfo().split(":");
		    svn = svn.auth(userData[0], userData[1]);
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
	    if(!this.connected) {
	        connect();
	    }
		return svn.openStream("");
	}
}
