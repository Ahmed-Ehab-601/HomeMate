package com.homemate.Model;

public class Service {
    int serviceid ;
    String servicename;
    String description;


    public Service() {}

    public Service(int serviceid, String servicename, String description) {
        this.serviceid = serviceid;
        this.servicename = servicename;
        this.description = description;
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