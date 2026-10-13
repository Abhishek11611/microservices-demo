package com.example.productservice.dtos;

public class ProductMediaRequestDTO {

    private String mediaType;
    private String mediaUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;

    public ProductMediaRequestDTO() {
    }

    public ProductMediaRequestDTO(String mediaType, String mediaUrl, String altText, Integer sortOrder, Boolean isPrimary) {
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }
}
