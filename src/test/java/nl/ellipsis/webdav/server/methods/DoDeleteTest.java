package nl.ellipsis.webdav.server.methods;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.ellipsis.webdav.HttpHeaders;
import nl.ellipsis.webdav.server.ITransaction;
import nl.ellipsis.webdav.server.IWebDAVStore;
import nl.ellipsis.webdav.server.StoredObject;
import nl.ellipsis.webdav.server.WebDAVConstants;
import nl.ellipsis.webdav.server.locking.LockedObject;
import nl.ellipsis.webdav.server.locking.ResourceLocks;
import nl.ellipsis.webdav.server.methods.AbstractMethod;
import nl.ellipsis.webdav.server.methods.DoDelete;
import nl.ellipsis.webdav.server.testutil.MockTest;
import nl.ellipsis.webdav.server.util.URLUtil;

import org.jmock.Expectations;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class DoDeleteTest extends MockTest {

	static IWebDAVStore mockStore;
	static HttpServletRequest mockReq;
	static HttpServletResponse mockRes;
	static ITransaction mockTransaction;
	static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

	@BeforeClass
	public static void setUp() throws Exception {
		mockStore = _mockery.mock(IWebDAVStore.class);
		mockReq = _mockery.mock(HttpServletRequest.class);
		mockRes = _mockery.mock(HttpServletResponse.class);
		mockTransaction = _mockery.mock(ITransaction.class);
	}

	@Test
	public void testDeleteIfReadOnlyIsTrue() throws Exception {

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(sourceFilePath));
				
				oneOf(mockRes).sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		});

		ResourceLocks resLocks = new ResourceLocks();
		DoDelete doDelete = new DoDelete(mockStore, resLocks, readOnly);
		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFileIfObjectExists() throws Exception {

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(sourceFilePath));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject fileSo = initFileStoredObject(resourceContent);

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));
				will(returnValue(fileSo));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));
			}
		});

		DoDelete doDelete = new DoDelete(mockStore, new ResourceLocks(), !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFileIfObjectNotExists() throws Exception {

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(sourceFilePath));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject fileSo = null;

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));
				will(returnValue(fileSo));

				oneOf(mockRes).sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		});

		DoDelete doDelete = new DoDelete(mockStore, new ResourceLocks(), !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFolderIfObjectExists() throws Exception {

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(sourceCollectionPath));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject folderSo = initFolderStoredObject();

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath));
				will(returnValue(folderSo));

				oneOf(mockStore).getChildrenNames(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath));
				will(returnValue(new String[] { "subFolder", "sourceFile" }));

				StoredObject fileSo = initFileStoredObject(resourceContent);

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));
				will(returnValue(fileSo));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));

				StoredObject subFolderSo = initFolderStoredObject();

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath,"/subFolder"));
				will(returnValue(subFolderSo));

				oneOf(mockStore).getChildrenNames(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath,"/subFolder"));
				will(returnValue(new String[] { "fileInSubFolder" }));

				StoredObject fileInSubFolderSo = initFileStoredObject(resourceContent);

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath,"/subFolder/fileInSubFolder"));
				will(returnValue(fileInSubFolderSo));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath,"/subFolder/fileInSubFolder"));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath,"/subFolder"));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath));
			}
		});

		DoDelete doDelete = new DoDelete(mockStore, new ResourceLocks(), !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFolderIfObjectNotExists() throws Exception {

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(sourceCollectionPath));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject folderSo = null;

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceCollectionPath));
				will(returnValue(folderSo));

				oneOf(mockRes).sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		});

		DoDelete doDelete = new DoDelete(mockStore, new ResourceLocks(), !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFileInFolder() throws Exception {

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(sourceFilePath));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject fileSo = initFileStoredObject(resourceContent);

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));
				will(returnValue(fileSo));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(sourceFilePath));
			}
		});

		DoDelete doDelete = new DoDelete(mockStore, new ResourceLocks(), !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFileInLockedFolderWithWrongLockToken() throws Exception {

		final String lockedFolderPath = "/lockedFolder";
		final String fileInLockedFolderPath = lockedFolderPath.concat("/fileInLockedFolder");

		String owner = new String("owner");
		ResourceLocks resLocks = new ResourceLocks();

		resLocks.lock(mockTransaction, lockedFolderPath, owner, true, -1, TEMP_TIMEOUT, !TEMPORARY);
		LockedObject lo = resLocks.getLockedObjectByPath(mockTransaction, lockedFolderPath);
		final String wrongLockToken = "(<opaquelocktoken:" + lo.getID() + "WRONG>)";

		final PrintWriter pw = new PrintWriter(tmpFolder + "/XMLTestFile");

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(fileInLockedFolderPath));

				oneOf(mockReq).getHeader(HttpHeaders.IF);
				will(returnValue(wrongLockToken));

				oneOf(mockRes).setStatus(HttpStatus.LOCKED.value());

//				oneOf(mockReq).getRequestURI();
//				will(returnValue("http://foo.bar".concat(lockedFolderPath)));

//				oneOf(mockRes).getWriter();
//				will(returnValue(pw));

			}
		});

		DoDelete doDelete = new DoDelete(mockStore, resLocks, !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFileInLockedFolderWithRightLockToken() throws Exception {

		final String path = "/lockedFolder/fileInLockedFolder";
		final String parentPath = "/lockedFolder";
		final String owner = new String("owner");
		ResourceLocks resLocks = new ResourceLocks();

		resLocks.lock(mockTransaction, parentPath, owner, true, -1, TEMP_TIMEOUT, !TEMPORARY);
		LockedObject lo = resLocks.getLockedObjectByPath(mockTransaction, "/lockedFolder");
		final String rightLockToken = "(<opaquelocktoken:" + lo.getID() + ">)";

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue(path));

				oneOf(mockReq).getHeader(HttpHeaders.IF);
				will(returnValue(rightLockToken));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject so = initFileStoredObject(resourceContent);

				oneOf(mockStore).getStoredObject(mockTransaction, URLUtil.getCleanPath(path));
				will(returnValue(so));

				oneOf(mockStore).removeObject(mockTransaction, URLUtil.getCleanPath(path));

			}
		});

		DoDelete doDelete = new DoDelete(mockStore, resLocks, !readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

	@Test
	public void testDeleteFileInFolderIfObjectNotExists() throws Exception {

		boolean readOnly = false;

		_mockery.checking(new Expectations() {
			{
				oneOf(mockReq).getAttribute(WebDAVConstants.HttpRequestParam.INCLUDE_PATH_INFO);
				will(returnValue(null));

				oneOf(mockReq).getPathInfo();
				will(returnValue("/folder/file"));

				oneOf(mockRes).setStatus(HttpServletResponse.SC_NO_CONTENT);

				StoredObject nonExistingSo = null;

				oneOf(mockStore).getStoredObject(mockTransaction, "/folder/file");
				will(returnValue(nonExistingSo));

				oneOf(mockRes).sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		});

		DoDelete doDelete = new DoDelete(mockStore, new ResourceLocks(), readOnly);

		doDelete.execute(mockTransaction, mockReq, mockRes);

		_mockery.assertIsSatisfied();
	}

}
