package com.kimcy929.simplefileexplorelib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kimcy929.simplefileexplorelib.adapter.DirectoryAdapter;
import com.kimcy929.simplefileexplorelib.adapter.SegmentAdapter;
import com.kimcy929.simplefileexplorelib.utils.GetPathUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;


public class SimpleDirectoryChooserActivity extends AppCompatActivity
        implements DirectoryAdapter.OnItemClickListener,
                    SegmentAdapter.OnItemClickListener {

    /*
    private AppCompatTextView txtCurrentPath;
    private AppCompatImageView btnBackFolder;
    private AppCompatImageView btnNewFolder;*/
    private AppCompatButton btnConfirm;
    private AppCompatButton btnCancel;

    private RecyclerView recyclerViewPathSegment;

    private DirectoryAdapter directoryAdapter;
    private SegmentAdapter segmentAdapter;

    private String txtCurrentPath;
    private String initDirectory = Environment.getExternalStorageDirectory().getPath();

    private boolean isChooseFile;

    public static final String INIT_DIRECTORY_EXTRA = "INIT_DIRECTORY_EXTRA";
    public static final String RESULT_DIRECTORY_EXTRA = "RESULT_DIRECTORY_EXTRA";
    public static final int RESULT_CODE_DIRECTORY_SELECTED = 8;

    public static final String CHOOSE_FILE_EXTRA = "CHOOSE_FILE_EXTRA";
    public static final String RESULT_FILE_EXTRA = "RESULT_FILE_EXTRA";
    public static final int RESULT_CODE_FILE_SELECTED = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getInit();

        setContentView(R.layout.activity_directory_chooser);

        initView();

        getDirAndSegment(initDirectory);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void getInit() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String directory = intent.getStringExtra(INIT_DIRECTORY_EXTRA);
            if (!TextUtils.isEmpty(directory)) {
                initDirectory = directory;
            }
            isChooseFile = intent.getBooleanExtra(CHOOSE_FILE_EXTRA, false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_root_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == R.id.menu_external_storage) {
            String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath();
            //txtCurrentPath.setText(externalStorageDirectory);
            txtCurrentPath = externalStorageDirectory;
            getDirAndSegment(externalStorageDirectory);

        } else if (menuId == R.id.menu_removable_storage) {
            showDialogRemovableStorage();

        } else if (menuId == R.id.menu_root_directory) {
            getDirAndSegment(File.separator);

        } else if (menuId == R.id.menu_new_folder) {
            showDialogCreateNewFolder();
        }

        return true;
    }

    private void initView() {
        //txtCurrentPath = findViewById(R.id.txtCurrentPath);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*btnBackFolder = findViewById(R.id.btnBackFolder);
        btnBackFolder.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        btnNewFolder = findViewById(R.id.btnNewFolder);*/
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        if (isChooseFile) {
            btnConfirm.setVisibility(View.GONE);
        }

        /*btnBackFolder.setOnClickListener(myOnClickListener);
        btnNewFolder.setOnClickListener(myOnClickListener);*/
        btnConfirm.setOnClickListener(myOnClickListener);
        btnCancel.setOnClickListener(myOnClickListener);

        RecyclerView recyclerViewDir = findViewById(R.id.recyclerViewDir);
        directoryAdapter = new DirectoryAdapter();
        directoryAdapter.setOnItemClickListener(this);
        recyclerViewDir.setAdapter(directoryAdapter);

        recyclerViewPathSegment = findViewById(R.id.recyclerViewPathSegment);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPathSegment.setLayoutManager(layoutManager);
        segmentAdapter = new SegmentAdapter();
        segmentAdapter.setOnItemClickListener(this);
        recyclerViewPathSegment.setAdapter(segmentAdapter);
    }

    private void showDialogRemovableStorage() {
        List<File> listRemovableStorage = GetPathUtils.getListRemovableStorage(getApplicationContext());
        if (listRemovableStorage.size() == 1) {
            getDirAndSegment(listRemovableStorage.get(0).getPath());
            return;
        }

        final IconAdapter iconAdapter = new IconAdapter(listRemovableStorage);
        new AlertDialog.Builder(this)
                .setTitle(R.string.removable_storage)
                .setAdapter(iconAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = iconAdapter.getItem(which);
                        getDirAndSegment(path);
                    }
                }).show();
    }

    private class IconAdapter extends BaseAdapter {

        private List<File> listRemovableStorage;
        private LayoutInflater inflater;

        @SuppressLint("WrongConstant")
        IconAdapter(List<File> listRemovableStorage) {

            this.listRemovableStorage = listRemovableStorage;

            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return listRemovableStorage != null ? listRemovableStorage.size() : 0;
        }

        @Override
        public String getItem(int position) {
            return listRemovableStorage.get(position).getPath();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.dialog_folder_item_layout, parent, false);
            }

            viewHolder.txtPath = convertView.findViewById(R.id.txtPath);
            viewHolder.txtPath.setText(listRemovableStorage.get(position).getName());

            return convertView;
        }

        class ViewHolder {
            TextView txtPath;
        }
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            /*if (id == btnBackFolder.getId()) {
                File file = new File(txtCurrentPath).getParentFile();
                if (file != null && file.isDirectory()) {
                    getDirAndSegment(file.getPath());
                }

            } else if (id == btnNewFolder.getId()) {
                if (new File(txtCurrentPath).canWrite()) {
                    showDialogCreateNewFolder();
                } else {
                    Toast.makeText(SimpleDirectoryChooserActivity.this, R.string.can_not_create_new_folder, Toast.LENGTH_SHORT).show();
                }

            } else*/

            if (id == btnConfirm.getId()) {
                if (!isChooseFile) {
                    if (new File(txtCurrentPath).canWrite()) {
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_DIRECTORY_EXTRA, txtCurrentPath);
                        setResult(RESULT_CODE_DIRECTORY_SELECTED, intent);
                        finish();
                    } else {
                        Toast.makeText(SimpleDirectoryChooserActivity.this, R.string.can_not_write_data, Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (id == btnCancel.getId()) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    };

    private void showDialogCreateNewFolder() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.new_folder_layout, null, false);
        final EditText editText = view.findViewById(R.id.editNewFolder);

        AlertDialog.Builder builder = new AlertDialog.Builder(SimpleDirectoryChooserActivity.this, R.style.MyAlertDialogAppCompatStyle);
        builder.setTitle(R.string.create_new_folder)
                .setView(view)
                .setPositiveButton(R.string.ok_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                            String currentPath = txtCurrentPath;
                            String newPath = currentPath + File.separator + editText.getText().toString().trim();
                            File file = new File(newPath);
                            if (!file.exists()) {
                                if (!file.mkdir()) {
                                    Toast.makeText(getApplicationContext(), "Can't create folder here", Toast.LENGTH_SHORT).show();
                                } else { //Refresh current folder
                                    getDirAndSegment(newPath);
                                }
                            }
                        } else {
                            Timber.d("A folder name is empty!");
                        }
                    }
                })
                .setNegativeButton(R.string.cancel_title, null);
        builder.show();
    }

    private void getDirAndSegment(String dirPath) {

        if (!TextUtils.equals(File.separator, dirPath)) {
            txtCurrentPath = dirPath;
        } else {
            txtCurrentPath = File.separator;
        }

        getAllSegments(dirPath);

        getDirectoryFromFile(dirPath);
    }

    private void getDirectoryFromFile(String dirPath) {
        File file = new File(dirPath);
        if (file.canWrite()) {
            int accentColor = ContextCompat.getColor(this, R.color.accentColor);
            //btnNewFolder.setColorFilter(accentColor);
            btnConfirm.setTextColor(accentColor);
        } else {
            btnConfirm.setTextColor(Color.BLACK);
            //btnNewFolder.setColorFilter(null);
        }

        loadFilesTask = new LoadFilesTask(this);
        loadFilesTask.execute(dirPath);
    }

    private void getAllSegments(String dirPath) {
        String[] pathSegments = dirPath.split(File.separator);
        List<String> segmentList = new ArrayList<>();
        if (pathSegments.length != 0) {
            for (String segment : pathSegments) {
                if (!TextUtils.isEmpty(segment)) {
                    segmentList.add(segment);
                }
            }
        } else {
            segmentList.add(File.separator);
        }
        segmentAdapter.addPathSegments(segmentList);
    }

    private LoadFilesTask loadFilesTask;

    private static class LoadFilesTask extends AsyncTask<String, Void, List<File>> {

        private WeakReference<SimpleDirectoryChooserActivity> activityWeakReference;

        @SuppressWarnings("WeakerAccess")
        public LoadFilesTask(SimpleDirectoryChooserActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected List<File> doInBackground(String... strings) {

            List<File> tempPaths = new ArrayList<>();

            if (activityWeakReference.get() != null) {
                File file = new File(strings[0]);
                File[] files = file.listFiles();
                boolean isChooseFile = activityWeakReference.get().isChooseFile;

                if (files != null) {
                    Arrays.sort(files, activityWeakReference.get().fileComparator);
                    for (File itemFile : files) {
                        if (isCancelled()) {
                            break;
                        }
                        if (isChooseFile) {
                            tempPaths.add(itemFile);
                        } else if (itemFile.isDirectory()) { //(itemFile.canRead()) {//!f.isHidden()
                            tempPaths.add(itemFile);
                        }
                    }
                }
            }

            return tempPaths;
        }

        @Override
        protected void onPostExecute(List<File> files) {
            super.onPostExecute(files);
            if (activityWeakReference.get() != null) {
                activityWeakReference.get().directoryAdapter.addFolders(files);
            }
        }
    }

    Comparator<File> fileComparator = new Comparator<File>() {

        public int compare(File file1, File file2) {

            if (file1.isDirectory()) {
                if (file2.isDirectory()) {
                    return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
                } else {
                    return -1;
                }
            } else {
                if (file2.isDirectory()) {
                    return 1;
                } else {
                    return String.valueOf(file1.getName().toLowerCase()).compareTo(file2.getName().toLowerCase());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*if (segmentAdapter != null) {
            segmentAdapter.cancelDiffTask();
        }*/

        if (loadFilesTask != null && !loadFilesTask.isCancelled()) {
            loadFilesTask.cancel(true);
        }
    }


    //For adapter

    @Override
    public void pathSegmentClick(String path) {
        getDirAndSegment(path);
    }

    @Override
    public void scrollToLast() {
        try {
            recyclerViewPathSegment.smoothScrollToPosition(segmentAdapter.getItemCount());
        } catch (Exception e) {
            Timber.e("Error scroll recyclerview!");
        }
    }

    @Override
    public void itemClick(File file) {
        if (file.isDirectory()) {
            if (file.canRead()) {
                getDirAndSegment(file.getPath());
            }
        } else {
            if (isChooseFile) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_FILE_EXTRA, file.getPath());
                setResult(RESULT_CODE_FILE_SELECTED, intent);
                finish();
            }
        }
    }
}
