package com.ravi.gae;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	
	public static List<ImageUrls> imageUrls;

	static {
		loadImageUrls();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String action = req.getParameter("action");
		String name = req.getParameter("name");
		String interval= req.getParameter("interval");
		int iinterval = interval == null ? 0 : Integer.valueOf(interval);
		String imageName= req.getParameter("imagename");
		String imageUrl= req.getParameter("imageurl");
		String imageInterval= req.getParameter("imageinterval");
		String filter= req.getParameter("filter");
		
		switch (action) {
		
			case "listImages":
				list(resp);
				break;
			case "showImage":
				imageFor(resp, name);
				break;
			case "cron":
				storeimage(resp, iinterval);
				break;
			case "refresh":
				loadImageUrls();
				break;
			case "addImageUrl":
				storeimageurl(resp, imageName, imageUrl, imageInterval);
				break;
			case "zip":
				zip(resp, filter);
				break;
			case "zipFiles":
				zipFiles(resp, filter);
				break;
			default:
				break;
		}

	}

	private void storeimageurl(HttpServletResponse resp, String imageName,
			String imageUrl, String imageInterval) throws IOException {
		try {
			PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
					"transactions-optional").getPersistenceManager();
			ImageUrls imageUrls= new ImageUrls(imageName, imageUrl, imageInterval);
			pm.makePersistent(imageUrls);
			pm.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.getOutputStream().print("success");
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}

	private void imageFor(HttpServletResponse resp, String name)
			throws IOException {

		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage.class);
		query.setFilter("name == nameParam");
		query.declareParameters("String nameParam");
		List<MyImage> images = (List<MyImage>) query.execute(name);
		pm.close();
		
		MyImage myImage = images.iterator().next();
		Blob image = myImage.getImage();

		resp.setContentType("image/jpeg");
		resp.setHeader("Content-Disposition", "attachment;filename="+myImage.getName());
		resp.getOutputStream().write(image.getBytes());
		
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}

	private void list(HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html");

		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage.class);
		query.setOrdering("this.date descending");
		List<MyImage> muImages = (List<MyImage>) query.execute();
		pm.close();

		for (MyImage myImage : muImages) {
			resp.getOutputStream().print(
					"<a href='mainServlet?action=showImage&name="
							+ myImage.getName()+"'>" + myImage.getName()+ "</a>"
							+ "<br>");
		}
		
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}
	
	private void storeimage(HttpServletResponse resp, int interval) throws IOException {

		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Kolkata"));
		calendar.setTime(new Date());

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		
		//String smonth= month<10?"0"+month:""+month;
		String smonth = new SimpleDateFormat("MMM").format(calendar.getTime());
		String sday = day < 10 ? "0" + day : "" + day;
		String shour = hour < 10 ? "0" + hour : "" + hour;
		String sminute = minute < 10 ? "0" + minute : "" + minute;
		String ssecond = second < 10 ? "0" + second : "" + second;
		
		try {
			PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
					"transactions-optional").getPersistenceManager();
			for (ImageUrls imageUrl : MainServlet.imageUrls) {

				if ((interval == 5 && interval >= imageUrl.getIntervalInt())
						|| interval == imageUrl.getIntervalInt()) {

					InputStream inputStream = new URL(imageUrl.getUrl())
							.openStream();
					Blob imageBlob = new Blob(IOUtils.toByteArray(inputStream));

					String imgaeName = sday + smonth + " " + year + " " + shour
							+ " " + sminute + " " + imageUrl.getName() + ".png";

					MyImage myImage = new MyImage(imgaeName, imageBlob);

					pm.makePersistent(myImage);
				}
			}
			pm.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		resp.getOutputStream().print("success");
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}
	
	private void zip(HttpServletResponse resp, String filter) throws IOException {
	    resp.setContentType("application/zip");
	    resp.setHeader("Content-Disposition", "attachment;filename="+filter+".zip");
	    
		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage.class);
		List<MyImage> myImages = (List<MyImage>) query.execute();
		pm.close();
		
		try (ZipOutputStream out = new ZipOutputStream(resp.getOutputStream())) {
			int count=0;
			for (MyImage myImage : myImages) {
				if(myImage.getName().contains(filter)){
					
					String zipFileName=myImage.getName();
					int indexOfExtention= zipFileName.indexOf(".");
					
					if(indexOfExtention==-1){
						zipFileName= zipFileName+ " "+count++;
					} else {
						zipFileName= zipFileName.substring(0, indexOfExtention)+" "+count++ +zipFileName.substring(indexOfExtention, zipFileName.length());
					}
					
					ZipEntry e = new ZipEntry(zipFileName);
					e.setTime(new Date().getTime());
					
			        out.putNextEntry(e);
			        byte[] bytes = myImage.getImage().getBytes();
			        out.write(bytes, 0, bytes.length);
			        out.closeEntry();

				}
			}
			out.finish();
			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	}
	
	private void zipFiles(HttpServletResponse resp, String filter) throws IOException {
	    resp.setContentType("text/html");
	    
		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage.class);
		List<MyImage> myImages = (List<MyImage>) query.execute();
		pm.close();
		
		for (MyImage myImage : myImages) {
			if(myImage.getName().contains(filter)){
				resp.getOutputStream().print(
						"<a href='mainServlet?action=showImage&name="
								+ myImage.getName()+"'>" + myImage.getName() + "</a>"
								+ "<br>");
			}
		}
		
		resp.getOutputStream().print("<br>");
		resp.getOutputStream().print(
				"<a href='mainServlet?action=zip&filter="
						+ filter+"'>Download above files in zip</a>"
						+ "<br>");
		
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}
	
	private static void loadImageUrls() {
		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(ImageUrls.class);
		query.setOrdering("this.interval ascending");
		imageUrls = (List<ImageUrls>) query.execute();
		pm.close();
	}
}
