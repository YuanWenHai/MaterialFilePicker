package com.nbsp.materialfilepicker.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nbsp.materialfilepicker.R;
import com.nbsp.materialfilepicker.filter.CompositeFilter;
import com.nbsp.materialfilepicker.utils.FileUtils;
import com.nbsp.materialfilepicker.widget.EmptyRecyclerView;

import java.io.File;
import java.util.List;

/**
 * Created by Dimorinny on 24.10.15.
 */
public class DirectoryFragment extends Fragment {
    public interface FileClickListener {
        void onFileClicked(File clickedFile);
        void onMultiChoiceFabClicked(List<File> files);
    }

    private static final String ARG_FILE_PATH = "arg_file_path";
    private static final String ARG_FILTER = "arg_filter";

    private View mEmptyView;
    private String mPath;
    private TextView mSelectedCount;
    private View mMultiChoiceWidget;

    private CompositeFilter mFilter;

    private EmptyRecyclerView mDirectoryRecyclerView;
    private DirectoryAdapter mDirectoryAdapter;
    private FileClickListener mFileClickListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFileClickListener = (FileClickListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFileClickListener = null;
    }

    public static DirectoryFragment getInstance(
            String path, CompositeFilter filter) {
        DirectoryFragment instance = new DirectoryFragment();

        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, path);
        args.putSerializable(ARG_FILTER, filter);
        instance.setArguments(args);

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directory, container, false);
        mDirectoryRecyclerView = (EmptyRecyclerView) view.findViewById(R.id.directory_recycler_view);
        mEmptyView = view.findViewById(R.id.directory_empty_view);
        View mFab = (FloatingActionButton) view.findViewById(R.id.directory_fab);
        mMultiChoiceWidget = view.findViewById(R.id.directory_multi_choice_widget);
        mSelectedCount = (TextView) view.findViewById(R.id.directory_selected_count);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFileClickListener != null){
                    mFileClickListener.onMultiChoiceFabClicked(mDirectoryAdapter.getCheckedFiles());
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initArgs();
        initFilesList();
    }
    public boolean onBackPressed(){
        if(mDirectoryAdapter.isCheckMode()){
            mDirectoryAdapter.setCheckMode(false);
            mMultiChoiceWidget.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private void initFilesList() {
        mDirectoryAdapter = new DirectoryAdapter(getActivity(),
                FileUtils.getFileListByDirPath(mPath, mFilter));

        mDirectoryAdapter.setOnItemClickListener(new DirectoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position,boolean isCheckMode) {
                if (mFileClickListener != null && !isCheckMode) {
                    mFileClickListener.onFileClicked(mDirectoryAdapter.getModel(position));
                }else if(isCheckMode){
                    mSelectedCount.setText(String.valueOf(mDirectoryAdapter.getCheckedFiles().size()));
                }
            }

            @Override
            public void onItemLongClick(View view, int position,boolean isCheckMode) {
                mMultiChoiceWidget.setVisibility(isCheckMode ? View.GONE : View.VISIBLE);
                mDirectoryAdapter.setCheckMode(!isCheckMode);
            }
        });

        mDirectoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDirectoryRecyclerView.setAdapter(mDirectoryAdapter);
        mDirectoryRecyclerView.setEmptyView(mEmptyView);
    }

    @SuppressWarnings("unchecked")
    private void initArgs() {
        if (getArguments().getString(ARG_FILE_PATH) != null) {
            mPath = getArguments().getString(ARG_FILE_PATH);
        }

        mFilter = (CompositeFilter) getArguments().getSerializable(ARG_FILTER);
    }
}
