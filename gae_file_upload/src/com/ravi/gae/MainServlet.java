package com.ravi.gae;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import com.google.appengine.api.datastore.FetchOptions;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	
	public static List<ImageUrls> imageUrls;

	static {
		loadImageUrls();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		System.gc();
		
		if(imageUrls==null){
			loadImageUrls();
		}

		String action = req.getParameter("action");//listImages
		String name = req.getParameter("name");
		String interval= req.getParameter("interval");
		int iinterval = interval == null ? 0 : Integer.valueOf(interval);
		String imageName= req.getParameter("imagename");// URL
		String imageUrl= req.getParameter("imageurl");// URL
		String imageInterval= req.getParameter("imageinterval");//30 URL
		String filter= req.getParameter("filter");//15Dec
		
		String showurl= req.getParameter("showurl");//Y
		String prefix= req.getParameter("prefix");//http://1-dot-newproject7dec2015.appspot.com
		
		switch (action) {
		
			case "listImages":
				list(resp,showurl,prefix);
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
				zipFiles(resp, filter,showurl,prefix);
				break;
			case "zipAll":
				zip(resp, null);
				break;
			default:
				break;
		}

	}

	//need to encode it before passing this way
	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=addImageUrl&imagename=5min&imageurl=http%3A%2F%2Fquotes.esignal.com%2Fesignalprod%2F%2Fesigchartspon%3Fcont%3D%2524NIFTY-NSE%26period%3DV%26varminutes%3D5%26size%3D1345x550%26bartype%3DCANDLE%26bardensity%3DMEDIUM%26STUDY%3DEMA%26STUDY0%3D20%26STUDY1%3D20%26STUDY2%3D20%26showextendednames%3Dtrue&imageinterval=5
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

	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=showImage&name=18Dec%202015%2013%2051%203min%20nifty.png
	private void imageFor(HttpServletResponse resp, String name)
			throws IOException {

		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage2.class);
		query.setFilter("name == nameParam");
		query.declareParameters("String nameParam");
		query.getFetchPlan().setFetchSize(1000);
		List<MyImage2> images = (List<MyImage2>) query.execute(name);
		pm.close();
		
		MyImage2 myImage = images.iterator().next();
		Blob image = myImage.getImage();

		resp.setContentType("image/jpeg");
		//resp.setHeader("Content-Disposition", "attachment;filename="+myImage.getName());
		resp.setHeader("Content-Disposition", "inline;filename="+myImage.getName());
		resp.getOutputStream().write(image.getBytes());
		
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}

	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=listImages
	
	private void list(HttpServletResponse resp, String showurl, String prefix) throws IOException {
		resp.setContentType("text/html");

		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage2Name.class);
		query.getFetchPlan().setFetchSize(1000);
		query.setOrdering("this.date descending");
		List<MyImage2Name> muImages = (List<MyImage2Name>) query.execute();
		pm.close();

		for (MyImage2Name myImage : muImages) {
			if("Y".equalsIgnoreCase(showurl))
				resp.getOutputStream().print(
						"<a href='mainServlet?action=showImage&name="
								+ myImage.getName()+"'>" + myImage.getName()+ "</a>"
								+ "<br>");
			else
				resp.getOutputStream().print(prefix+
						"/mainServlet?action=showImage&name="
								+ myImage.getName()+ "<br>");
		}
		
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}
	
	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=cron&interval=5
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
		
		for (ImageUrls imageUrl : MainServlet.imageUrls) {
			int count=0;
			boolean isProcesssed=false;
			
			while (count < 3 && !isProcesssed) {
				
				PersistenceManager pm = JDOHelper
						.getPersistenceManagerFactory(
								"transactions-optional")
						.getPersistenceManager();
				try {
					count++;
					// if ((interval == 5 && interval >=
					// imageUrl.getIntervalInt())
					// || interval == imageUrl.getIntervalInt()) {
					
					if (interval == imageUrl.getIntervalInt()) {
						InputStream inputStream = new URL(imageUrl.getUrl())
								.openStream();
						Blob imageBlob = new Blob(
								IOUtils.toByteArray(inputStream));

						String imgaeName = sday + smonth + " " + year + " "
								+ shour + " " + sminute + " "
								+ imageUrl.getName() + " "+ count +".png";

						MyImage2 myImage = new MyImage2(imgaeName, imageBlob);
						pm.makePersistent(myImage);

						MyImage2Name myImageName = new MyImage2Name(imgaeName);
						pm.makePersistent(myImageName);
					}
					
					isProcesssed = true;
				} catch (Exception e) {
					e.printStackTrace();
					isProcesssed = false;
				} finally {
					pm.close();
				}
			}//end while
		}// end for
		
		resp.getOutputStream().print("success");
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}
	
	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=zip&filter=18Dec
	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=zip
	//FIXME
	//FAILS due to logic & huge size
	private void zip(HttpServletResponse resp, String filter) throws IOException {
	    resp.setContentType("application/zip");
	    resp.setHeader("Content-Disposition", "attachment;filename="+(filter==null?"fullzip":filter)+".zip");
	    
		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage2Name.class);
		query.getFetchPlan().setFetchSize(1000);
		List<MyImage2Name> myImage2Names = (List<MyImage2Name>) query.execute();
		pm.close();
		
		try (ZipOutputStream out = new ZipOutputStream(resp.getOutputStream())) {
			//int count=0;
			
			for (MyImage2Name myImage2Name : myImage2Names) {
				
				List<String> filters= Arrays.asList(filter.split(" "));

				boolean flag=true;
				for(String str: filters){
					if(!myImage2Name.getName().contains(str)){
						flag=false;
						break;
					}
				}
				
				if(filter==null || flag){
					
					pm = JDOHelper.getPersistenceManagerFactory(
							"transactions-optional").getPersistenceManager();
					
					Query query2 = pm.newQuery(MyImage2.class);
					query.setFilter("name == nameParam");
					query.declareParameters("String nameParam");
					query.getFetchPlan().setFetchSize(1000);
					List<MyImage2> images = (List<MyImage2>) query2.execute(myImage2Name.getName());
					pm.close();
					
					System.gc();
					
					String zipFileName= myImage2Name.getName();
					//int indexOfExtention= zipFileName.indexOf(".");
					
					/*if(indexOfExtention==-1){
						zipFileName= zipFileName+ " "+count++;
					} else {
						zipFileName= zipFileName.substring(0, indexOfExtention)+" "+count++ +zipFileName.substring(indexOfExtention, zipFileName.length());
					}*/
					
					ZipEntry e = new ZipEntry(zipFileName);
					e.setTime(new Date().getTime());
					
			        out.putNextEntry(e);
			        byte[] bytes = images.get(0).getImage().getBytes();
			        out.write(bytes, 0, bytes.length);
			        out.closeEntry();

				}
			}
			out.finish();
			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	    	try{
	    		pm.close();
	    	}catch(Exception e){
	    	}
	    }
	    
	}
	
	// list of files & url at end- action=zip
	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=zipFiles&filter=18Dec
	private void zipFiles(HttpServletResponse resp, String filter,String showurl, String prefix) throws IOException {
	    resp.setContentType("text/html");
	    
		PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
				"transactions-optional").getPersistenceManager();
		Query query = pm.newQuery(MyImage2Name.class);
		query.getFetchPlan().setFetchSize(1000);
		List<MyImage2Name> myImage2Names = (List<MyImage2Name>) query.execute();
		query.setOrdering("this.name descending");
		pm.close();
		
		for (MyImage2Name myImage2Name : myImage2Names) {
			
			List<String> filters= Arrays.asList(filter.split(" "));

			boolean flag=true;
			for(String str: filters){
				if(!myImage2Name.getName().contains(str)){
					flag=false;
					break;
				}
			}
			
			if(filter==null || flag){
				if("Y".equalsIgnoreCase(showurl))
					resp.getOutputStream().print(
							"<a href='mainServlet?action=showImage&name="
									+ myImage2Name.getName()+"'>" + myImage2Name.getName() + "</a>"
									+ "<br>");
				
				else
					resp.getOutputStream().print(prefix+
							"/mainServlet?action=showImage&name="
									+ myImage2Name.getName()+ "<br>");
			}
		}
		
/*		resp.getOutputStream().print("<br>");
		resp.getOutputStream().print(
				"<a href='mainServlet?action=zip&filter="
						+ filter+"'>Download above files in zip</a>"
						+ "<br>");*/
		
		resp.getOutputStream().flush();
		resp.getOutputStream().close();
	}
	
	//http://1-dot-newproject7dec2015.appspot.com/mainServlet?action=refresh
	private static void loadImageUrls() {
		try{
			PersistenceManager pm = JDOHelper.getPersistenceManagerFactory(
					"transactions-optional").getPersistenceManager();
			Query query = pm.newQuery(ImageUrls.class);
			query.getFetchPlan().setFetchSize(1000);
			query.setOrdering("this.interval ascending");
			imageUrls = (List<ImageUrls>) query.execute();
			pm.close();
		} catch(Exception e){
			imageUrls= null;
		}
	}
}
