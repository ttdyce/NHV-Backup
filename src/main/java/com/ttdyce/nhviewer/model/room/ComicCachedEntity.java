package com.ttdyce.nhviewer.model.room;

import java.io.Serializable;

public class ComicCachedEntity implements Serializable {
    private static final long serialVersionUID = 3001880502226771220L;
    
    private int id;
    
    private String mid;
    
    private String title;
    
    private String pageTypes;
    
    private int numOfPages;

    public ComicCachedEntity(int id,  String mid,  String title,  String pageTypes, int numOfPages) {
        this.id = id;
        this.mid = mid;
        this.title = title;
        this.pageTypes = pageTypes;
        this.numOfPages = numOfPages;
    }

    // Room uses this factory method to create ComicCollectionEntity objects.
    public static ComicCachedEntity create(int id, String mid, String title, String pageTypes, int numOfPages) {
        return new ComicCachedEntity(id, mid, title, pageTypes, numOfPages);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
    public String getMid() {
        return mid;
    }

    public void setMid( String mid) {
        this.mid = mid;
    }

    
    public String getTitle() {
        return title;
    }

    public void setTitle( String title) {
        this.title = title;
    }

    
    public String getPageTypes() {
        return pageTypes;
    }

    public void setPageTypes( String pageTypes) {
        this.pageTypes = pageTypes;
    }

    public int getNumOfPages() {
        return numOfPages;
    }

    public void setNumOfPages(int numOfPages) {
        this.numOfPages = numOfPages;
    }

    // public String toJson() {
    //     Gson gson = new Gson();
    //     return gson.toJson(this);
    // }
}
