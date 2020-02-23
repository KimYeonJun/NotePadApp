package com.example.notepad;
import java.util.ArrayList;
import java.util.List;

/**
 * Memo : list_item layout을 보여줄 데이터 정의
 */
public class Memo {
    int seq;
    String titleText;
    String mainText;
    List<String> uriList=new ArrayList<>();
    String thumb;

    public List<String> geturiList() {
        return uriList;
    }

    public void seturiList(List<String> uriList) {
        this.uriList = uriList;
    }

    public Memo(int seq, String titleText, String mainText, List<String> uriList) {
        this.seq = seq;
        this.titleText = titleText;
        this.mainText = mainText;
        this.uriList = uriList;
    }
    public Memo(String titleText, String mainText, String thumb) {
        this.titleText = titleText;
        this.mainText = mainText;
        this.thumb = thumb;
    }
    public Memo(String titleText, String mainText, List<String> uriList) {
        this.titleText = titleText;
        this.mainText = mainText;
        this.uriList = uriList;
    }
    public Memo(String titleText, String mainText) {
        this.titleText = titleText;
        this.mainText = mainText;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getMainText() {
        return mainText;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }
}
