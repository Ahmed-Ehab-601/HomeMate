package com.homemate.reviews.DTO;

public class ReviewImagesDTO {
    private int imgId;
    private String format;
    private byte[] imgFile;
    private String imgName;

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public byte[] getImgFile() {
        return imgFile;
    }

    public void setImgFile(byte[] imgFile) {
        this.imgFile = imgFile;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }
}
