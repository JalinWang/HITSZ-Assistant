package com.stupidtree.hita.online;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;

public class LostAndFound extends BmobObject{
    String title;
    String content;
    HITAUser author;
    String imageUri;
    String contact;
    String type;
    Location location;
    boolean anonymous;
    boolean found;


    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }



    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public HITAUser getAuthor() {
        return author;
    }

    public void setAuthor(HITAUser author) {
        this.author = author;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
