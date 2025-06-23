package dev.paul.cartlink.product.dto;

public class GeneratedLinkResponse {
    private Long linkId;
    private String url;

    public GeneratedLinkResponse() {
    }

    public GeneratedLinkResponse(Long linkId, String url) {
        this.linkId = linkId;
        this.url = url;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}