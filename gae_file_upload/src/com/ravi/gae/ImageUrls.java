package com.ravi.gae;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Id;

@PersistenceCapable
public class ImageUrls {
	@Id
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
	private String name;
    
    @Persistent
	private String url;
    
    @Persistent
	private String interval;
    
	public ImageUrls(String name, String url, String interval) {
		super();
		this.name = name;
		this.url = url;
		this.interval = interval;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getInterval() {
		return interval;
	}

	public int getIntervalInt() {
		return Integer.valueOf(interval);
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}
}
