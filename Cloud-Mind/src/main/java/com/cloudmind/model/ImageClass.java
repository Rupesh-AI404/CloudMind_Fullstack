package com.cloudmind.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "images")
public class ImageClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;

    private String userEmail;
    private String fileName;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;

    // Constructors
    public ImageClass() {}

    public ImageClass(String image, String userEmail, String fileName) {
        this.image = image;
        this.userEmail = userEmail;
        this.fileName = fileName;
        this.uploadDate = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Date getUploadDate() { return uploadDate; }
    public void setUploadDate(Date uploadDate) { this.uploadDate = uploadDate; }
}