package com.ravi.gae;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Blob;

@PersistenceCapable
public class MyImage2 {
	@Id
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String name;

    @Persistent
    Blob image;
    
    @Persistent
    Date date;

    public MyImage2() { }
    
    public MyImage2(String name, Blob image) {
        this.name = name; 
        this.image = image;
        this.date = new Date();
    }

	public Blob getImage() {
		return image;
	}

	public void setImage(Blob image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}
	
	public Date getDate() {
		return date;
	}
}