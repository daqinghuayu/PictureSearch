package com.lx.picturesearch.entity;

/**
 * 推荐网站的实体类
 */
public class HomeLink {
    public HomeLink() {
    }

    /**
     * 构造器
     * @param name
     * @param image
     * @param url
     */



    public HomeLink(String name, String image, String url) {
        this.name = name;
        this.image = image;
        this.url = url;
    }

    public String name;// 网站名
    public String image;// 图片
    public String url;// 网站地址


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
