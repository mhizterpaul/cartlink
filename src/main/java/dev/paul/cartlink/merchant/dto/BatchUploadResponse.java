package dev.paul.cartlink.merchant.dto;

import java.util.List;

public class BatchUploadResponse {
    private boolean success;
    private String message;
    private List<String> failedItems;

    public BatchUploadResponse() {
    }

    public BatchUploadResponse(boolean success, String message, List<String> failedItems) {
        this.success = success;
        this.message = message;
        this.failedItems = failedItems;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFailedItems() {
        return failedItems;
    }

    public void setFailedItems(List<String> failedItems) {
        this.failedItems = failedItems;
    }
}