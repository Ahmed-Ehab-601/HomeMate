package com.homemate.Model;

public class Service {
    int serviceid ;
    String servicename;
    String description;
    private byte[] imagedata;
    private String imageName;
    private String imageType;


    public Service() {}

    public Service(int serviceid, String servicename, String description,
                   byte[] imagedata, String imageName, String imageType) {
        this.serviceid = serviceid;
        this.servicename = servicename;
        this.description = description;
        this.imagedata = imagedata;
        this.imageName = imageName;
        this.imageType = imageType;
    }



    public int getServiceid() {

        return serviceid;
    }

    public void setServiceid(int serviceid) {

        this.serviceid = serviceid;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {

        this.servicename = servicename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public byte[] getImagedata() {
        return imagedata;
    }

    public void setImagedata(byte[] imagedata) {
        this.imagedata = imagedata;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

}


//CREATE TABLE Service (
//        serviceID INT AUTO_INCREMENT PRIMARY KEY,
//        name VARCHAR(100) NOT NULL UNIQUE,
//description VARCHAR(500),
//imagedata LONGBLOB,
//imageName VARCHAR(200),
//imageType VARCHAR(200),
//INDEX idx_service_name (name)
//);