package com.github.ttdyce.nhviewer.model.room;

import java.io.Serializable;
import java.util.Date;

public class ComicCollectionEntity implements Serializable {
    private static final long serialVersionUID = 3001880502226771220L;

    private String name;

    private int id;

    private Date dateCreated;

    public ComicCollectionEntity(String name, int id, Date dateCreated) {
        this.name = name;
        this.id = id;
        this.dateCreated = dateCreated;
    }

    // Room uses this factory method to create ComicCollectionEntity objects.
    public static ComicCollectionEntity create(String name, int id, Date dateCreated) {
        return new ComicCollectionEntity(name, id, dateCreated);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    // public String toJson() {
    //     Gson gson = new Gson();
    //     return gson.toJson(this);
    // }
}
