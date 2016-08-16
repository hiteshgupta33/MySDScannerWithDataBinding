package com.mysdscanner.model;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Created by hiteshgupta on 8/10/16.
 */

public class ScannerResult extends BaseObservable{

    private int scanError;

	private double averageSize;

	private List<FileInfo> lstFileInfo;

	public List<FileInfo> getLstFileExtensionInfo() {
		return lstFileExtensionInfo;
	}

	public void setLstFileExtensionInfo(List<FileInfo> lstFileExtensionInfo) {
		this.lstFileExtensionInfo = lstFileExtensionInfo;
	}

	private List<FileInfo> lstFileExtensionInfo;

	public List<FileInfo> getLstFileInfo() {
		return lstFileInfo;
	}

	public void setLstFileInfo(List<FileInfo> lstFileInfo) {
		this.lstFileInfo = lstFileInfo;
	}


	public int getScanError() {
		return scanError;
	}

	public void setScanError(int scanError) {
		this.scanError = scanError;
	}

	public double getAverageSize() {
		return averageSize;
	}

	public void setAverageSize(double averageSize) {
		this.averageSize = averageSize;
	}

    public ScannerResult() {
    }
}
