package sun.net.www.protocol.svn;

import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;

import red.svn.SvnUrlConnection;

public class Handler extends URLStreamHandler {

	@Override
	protected SvnUrlConnection openConnection(URL url) throws IOException {
		// try http
		return new SvnUrlConnection(url);
	}

}
