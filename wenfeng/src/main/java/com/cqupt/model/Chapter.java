package com.cqupt.model;

/**
 * Created by Alan on 2016/5/23.
 */
public class Chapter {

    int chapterID;
    int parentChapterID;
    int lessonID;
    String chapterTitle;
    String chapterContent;
    String attachmentUri;

    public int getChapterID() {
        return chapterID;
    }

    public void setChapterID(int chapterID) {
        this.chapterID = chapterID;
    }

    public int getParentChapterID() {
        return parentChapterID;
    }

    public void setParentChapterID(int parentChapterID) {
        this.parentChapterID = parentChapterID;
    }

    public int getLessonID() {
        return lessonID;
    }

    public void setLessonID(int lessonID) {
        this.lessonID = lessonID;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public String getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    public String getAttachmentUri() {
        return attachmentUri;
    }

    public void setAttachmentUri(String attachmentUri) {
        this.attachmentUri = attachmentUri;
    }
}
