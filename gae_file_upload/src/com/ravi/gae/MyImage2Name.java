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
public class MyImage2Name {
	@Id
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String name;

    @Persistent
    Date date;

    public MyImage2Name() { }
    
    public MyImage2Name(String name) {
        this.name = name; 
        this.date = new Date();
    }

	public String getName() {
		return name;
	}
	
	public Date getDate() {
		return date;
	}
}