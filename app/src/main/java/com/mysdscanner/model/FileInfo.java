package com.mysdscanner.model;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hiteshgupta on 8/15/16.
 */
public class FileInfo extends BaseObservable {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getExtensionFrequency() {
		return extensionFrequency;
	}

	public void setExtensionFrequency(long extensionFrequency) {
		this.extensionFrequency = extensionFrequency;
	}

	private String name;

	private String extension;

	private long size;

	private long extensionFrequency;

	protected FileInfo(Parcel in) {
		name = in.readString();
		extension = in.readString();
		size = in.readLong();
		extensionFrequency = in.readLong();
	}

	public FileInfo() {
	}
}
