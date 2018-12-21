package com.cqupt.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Alan on 2016/5/24.
 */
public class ChapterGroup implements Serializable{

    private Chapter chapter;
    private ArrayList<Chapter> children;

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public ArrayList<Chapter> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Chapter> children) {
        this.children = children;
    }
}
