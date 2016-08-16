package com.mysdscanner.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mysdscanner.model.FileInfo;
import com.mysdscanner.model.ScannerResult;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hiteshgupta on 8/13/16.
 */
public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.BindingHolder> {

	private ScannerResult scannerResult;

	private int holderLayout, variableId;
	private List<FileInfo> items = new ArrayList<>();

	public FileRecyclerAdapter(ScannerResult scannerResult) {
		this.scannerResult = scannerResult;
	}

	public FileRecyclerAdapter(int holderLayout, int variableId, List<FileInfo> items) {
		this.holderLayout = holderLayout;
		this.variableId = variableId;
		this.items = items;
	}

	public static class BindingHolder extends RecyclerView.ViewHolder {
		private ViewDataBinding binding;

		public BindingHolder(View rowView) {
			super(rowView);
			binding = DataBindingUtil.bind(rowView);
		}
		public ViewDataBinding getBinding() {
			return binding;
		}
	}

	@Override
	public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(holderLayout, parent, false);

		return  new BindingHolder(v);
	}

	@Override
	public void onBindViewHolder(BindingHolder holder, int position) {
		final FileInfo item = items.get(position);
		holder.getBinding().setVariable(variableId, item);
		holder.getBinding().executePendingBindings();
	}

	@Override
	public int getItemCount() {
		return items.size();
	}
}
