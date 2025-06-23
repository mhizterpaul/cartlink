package dev.paul.cartlink.link.dto;

public class LinkTrafficSourceResponse {
    private String source;
    private int clicks;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }
}