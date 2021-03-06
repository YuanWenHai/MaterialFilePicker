package com.nbsp.materialfilepicker;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.nbsp.materialfilepicker.filter.CompositeFilter;
import com.nbsp.materialfilepicker.filter.PatternFilter;
import com.nbsp.materialfilepicker.ui.DirectoryFragment;
import com.nbsp.materialfilepicker.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Dimorinny on 24.10.15.
 */
public class FilePickerActivity extends AppCompatActivity implements DirectoryFragment.FileClickListener {
    public static final String ARG_START_PATH = "arg_start_path";
    public static final String ARG_CURRENT_PATH = "arg_current_path";

    public static final String ARG_FILTER = "arg_filter";
    public static final String ARG_CLOSEABLE = "arg_closeable";
    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_CALLBACK = "arg_callback";
    public static final String ARG_COUNT_LIMITATION = "arg_count_limitation";

    public static final String STATE_START_PATH = "state_start_path";
    private static final String STATE_CURRENT_PATH = "state_current_path";

    public static final String RESULT_FILE_PATH = "result_file_path";
    private static final int HANDLE_CLICK_DELAY = 150;

    private Toolbar mToolbar;

    private String mStartPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String mCurrentPath = mStartPath;
    private CharSequence mTitle;
    private int mCount;

    private Boolean mCloseable;

    private CompositeFilter mFilter;

    private boolean mCallback;
    private DirectoryFragment mCurrentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        initArguments(savedInstanceState);
        initViews();
        initToolbar();
        initBackStackState();
        initFragment();
    }

    @SuppressWarnings("unchecked")
    private void initArguments(Bundle savedInstanceState) {
        if (getIntent().hasExtra(ARG_FILTER)) {
            Serializable filter = getIntent().getSerializableExtra(ARG_FILTER);

            if (filter instanceof Pattern) {
                ArrayList<FileFilter> filters = new ArrayList<>();
                filters.add(new PatternFilter((Pattern) filter, false));
                mFilter = new CompositeFilter(filters);
            } else {
                mFilter = (CompositeFilter) filter;
            }
        }

        if (savedInstanceState != null) {
            mStartPath = savedInstanceState.getString(STATE_START_PATH);
            mCurrentPath = savedInstanceState.getString(STATE_CURRENT_PATH);
            updateTitle();
        } else {
            if (getIntent().hasExtra(ARG_START_PATH)) {
                mStartPath = getIntent().getStringExtra(ARG_START_PATH);
                mCurrentPath = mStartPath;
            }

            if (getIntent().hasExtra(ARG_CURRENT_PATH)) {
                String currentPath = getIntent().getStringExtra(ARG_CURRENT_PATH);

                if (currentPath.startsWith(mStartPath)) {
                    mCurrentPath = currentPath;
                }
            }
        }

        if (getIntent().hasExtra(ARG_TITLE)) {
            mTitle = getIntent().getCharSequenceExtra(ARG_TITLE);
        }
        if(getIntent().hasExtra(ARG_CALLBACK)){
            mCallback = getIntent().getBooleanExtra(ARG_CALLBACK,false);
        }

        if (getIntent().hasExtra(ARG_CLOSEABLE)) {
            mCloseable = getIntent().getBooleanExtra(ARG_CLOSEABLE, true);
        }
        if(getIntent().hasExtra(ARG_COUNT_LIMITATION)){
            mCount = getIntent().getIntExtra(ARG_COUNT_LIMITATION,0);
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        // Show back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Truncate start of path
        try {
            Field f;
            if (TextUtils.isEmpty(mTitle)) {
                f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            } else {
                f = mToolbar.getClass().getDeclaredField("mSubtitleTextView");
            }

            f.setAccessible(true);
            TextView textView = (TextView) f.get(mToolbar);
            textView.setEllipsize(TextUtils.TruncateAt.START);
        } catch (Exception ignored) {
        }

        if (!TextUtils.isEmpty(mTitle)) {
            setTitle(mTitle);
        }
        updateTitle();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void initFragment() {
        DirectoryFragment fragment = DirectoryFragment.getInstance(
                mCurrentPath,mCount, mFilter);
        mCurrentFragment = fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void initBackStackState() {
        String pathToAdd = mCurrentPath;
        ArrayList<String> separatedPaths = new ArrayList<>();

        while (!pathToAdd.equals(mStartPath)) {
            pathToAdd = FileUtils.cutLastSegmentOfPath(pathToAdd);
            separatedPaths.add(pathToAdd);
        }

        Collections.reverse(separatedPaths);

        for (String path : separatedPaths) {
            addFragmentToBackStack(path);
        }
    }

    private void updateTitle() {
        if (getSupportActionBar() != null) {
            String titlePath = mCurrentPath.isEmpty() ? "/" : mCurrentPath;
            if (TextUtils.isEmpty(mTitle)) {
                getSupportActionBar().setTitle(titlePath);
            } else {
                getSupportActionBar().setSubtitle(titlePath);
            }
        }
    }

    private void addFragmentToBackStack(String path) {
        DirectoryFragment fragment = DirectoryFragment.getInstance(
                path,mCount,mFilter);
        mCurrentFragment = fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.container,fragment )
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_close).setVisible(mCloseable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (menuItem.getItemId() == R.id.action_close) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if(!mCurrentFragment.onBackPressed()){
            FragmentManager fm = getFragmentManager();
            if (!mCurrentPath.equals(mStartPath)) {
                fm.popBackStack();
                mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath);
                updateTitle();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CURRENT_PATH, mCurrentPath);
        outState.putString(STATE_START_PATH, mStartPath);
    }

    @Override
    public void onFileClicked(final File clickedFile) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleFileClicked(clickedFile);
            }
        }, HANDLE_CLICK_DELAY);
    }

    @Override
    public void onMultiChoiceFabClicked(List<File> files) {
        setResultAndFinish(files);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mCallback){
            MaterialFilePicker.mCallback = null;
        }
    }

    private void handleFileClicked(final File clickedFile) {
        if (clickedFile.isDirectory()) {
            mCurrentPath = clickedFile.getPath();
            // If the user wanna go to the emulated directory, he will be taken to the
            // corresponding user emulated folder.
            if (mCurrentPath.equals("/storage/emulated"))
                mCurrentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            addFragmentToBackStack(mCurrentPath);
            updateTitle();
        } else {
            setResultAndFinish(clickedFile.getPath());
        }
    }

    private void setResultAndFinish(String filePath) {
        ArrayList<String> list = new ArrayList<>();
        list.add(filePath);
        if(mCallback){
            MaterialFilePicker.mCallback.onPick(list);
            MaterialFilePicker.mCallback = null;
        }else{
            Intent data = new Intent();
            data.putExtra(RESULT_FILE_PATH, list);
            setResult(RESULT_OK, data);
        }
        finish();
    }
    private void setResultAndFinish(List<File> files){
        ArrayList<String> list = new ArrayList<>();
        for(File file : files){
            list.add(file.getPath());
        }
        if(mCallback){
            MaterialFilePicker.mCallback.onPick(list);
            MaterialFilePicker.mCallback = null;
        }else{
            Intent data = new Intent();
            data.putExtra(RESULT_FILE_PATH, list);
            setResult(RESULT_OK, data);
        }
        finish();
    }
}
