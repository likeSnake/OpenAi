package net.test.openai.pojo;

public class VersionInfo {
    private String VersionName;
    private String Url;
    private String version_content;

    public VersionInfo(String versionName, String url, String version_content) {
        VersionName = versionName;
        Url = url;
        this.version_content = version_content;
    }

    public String getVersionName() {
        return VersionName;
    }

    public String getVersion_content() {
        return version_content;
    }

    public void setVersion_content(String version_content) {
        this.version_content = version_content;
    }

    public void setVersionName(String versionName) {
        VersionName = versionName;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }
}
