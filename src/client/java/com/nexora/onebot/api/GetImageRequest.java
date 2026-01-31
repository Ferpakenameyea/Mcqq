package com.nexora.onebot.api;

public final class GetImageRequest {
    public static String ACTION = "get_image";

    private String file;

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }
}
