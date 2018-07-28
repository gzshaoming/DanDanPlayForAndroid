package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.net.CommJsonEntity;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.CommOtherDataObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.net.RetroFactory;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/7/14.
 */


public class DanmuDownloadBean implements Serializable{

    /**
     * count : 0
     * comments : [{"cid":0,"p":"string","m":"string"}]
     */

    private int count;
    private List<CommentsBean> comments;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<CommentsBean> getComments() {
        return comments;
    }

    public void setComments(List<CommentsBean> comments) {
        this.comments = comments;
    }

    public static class CommentsBean {
        /**
         * cid : 0
         * p : string
         * m : string
         */

        private int cid;
        private String p;
        private String m;

        public int getCid() {
            return cid;
        }

        public void setCid(int cid) {
            this.cid = cid;
        }

        public String getP() {
            return p;
        }

        public void setP(String p) {
            this.p = p;
        }

        public String getM() {
            return m;
        }

        public void setM(String m) {
            this.m = m;
        }
    }

    public static void downloadDanmu(long episodeId, CommOtherDataObserver<DanmuDownloadBean> observer, NetworkConsumer consumer){
        String episodeIdText = String.valueOf(episodeId);
        RetroFactory.getInstance().downloadDanmu(episodeIdText)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}