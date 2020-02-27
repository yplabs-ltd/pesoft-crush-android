package net.pesofts.crush.model;

import java.io.Serializable;

public class SystemCheck implements Serializable {
	private static final long serialVersionUID = -925699204793472832L;
	private String status;
	private String title;
	private String description;
	private String url;
	private String minVersion;
	private String latestVersion;
	private String extra;
	private Integer noticeid;

	public Integer getNoticeid() {
		return noticeid;
	}

	public void setNoticeid(Integer noticeid) {
		this.noticeid = noticeid;
	}

	public String getMinVersion() {
		return minVersion;
	}

	public void setMinVersion(String minVersion) {
		this.minVersion = minVersion;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public boolean isOK() {
		return "OK".equals(status);
	}

	public boolean hasNewVersion(String currentVersion) {
		return getLatestVersion() != null && currentVersion != null
				&& compareVersion(getLatestVersion(), currentVersion) > 0;
	}

	public boolean isUsableVersion(String currentVersion) {
		return getMinVersion() == null || currentVersion == null
				|| compareVersion(currentVersion, getMinVersion()) >= 0;
	}

	protected static int compareVersion(String v1, String v2) {
		String[] vals1 = v1.replaceAll("[^\\.\\d].*$", "").split("\\.");
		String[] vals2 = v2.replaceAll("[^\\.\\d].*$", "").split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}

		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return diff < 0 ? -1 : diff == 0 ? 0 : 1;
		}
		return vals1.length < vals2.length ? -1 : vals1.length == vals2.length ? 0 : 1;
	}
}
