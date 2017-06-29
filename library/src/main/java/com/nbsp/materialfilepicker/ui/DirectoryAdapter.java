package com.nbsp.materialfilepicker.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.R;
import com.nbsp.materialfilepicker.utils.FileTypeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimorinny on 24.10.15.
 */

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View view, int position,boolean isCheckMode);
        void onItemLongClick(View view,int position,boolean isCheckMode);
    }
    private boolean isCheckMode;
    private ArrayList<Boolean> mCheckedMap = new ArrayList<>();
    private int mCountLimitation;
    private int mCheckedCount;
    private List<File> mFiles;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;




    public DirectoryAdapter(Context context, List<File> files) {
        mContext = context;
        mFiles = files;
        for (File file : files){
            mCheckedMap.add(false);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
    public void setCheckMode(boolean which){
        isCheckMode = which;
        if(!which){
            resetCheckState();
        }
        notifyDataSetChanged();
    }
    public boolean isCheckMode(){
        return isCheckMode;
    }


    public void setCountLimitation(int count){
        mCountLimitation = count;
    }
    public void resetCheckState(){
        mCheckedCount = 0;
        for(int i=0;i<mCheckedMap.size();i++){
            mCheckedMap.set(i,false);
        }
    }

    @Override
    public DirectoryViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);

        return new DirectoryViewHolder(view, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(DirectoryViewHolder holder, int position) {
        File currentFile = mFiles.get(position);

        FileTypeUtils.FileType fileType = FileTypeUtils.getFileType(currentFile);
        holder.mFileImage.setImageResource(fileType.getIcon());
        holder.mFileSubtitle.setText(fileType.getDescription());
        holder.mFileTitle.setText(currentFile.getName());
        if(!currentFile.isDirectory() && isCheckMode){
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }else{
            holder.mCheckBox.setVisibility(View.INVISIBLE);
        }
        if(isCheckMode){
            if(mCheckedMap.get(position)){
                holder.mCheckBox.setChecked(true);
            }else{
                holder.mCheckBox.setChecked(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public File getModel(int index) {
        return mFiles.get(index);
    }
    public List<File> getCheckedFiles(){
        List<File> list = new ArrayList<>();
        for(int i=0;i<mCheckedMap.size();i++){
            if(mCheckedMap.get(i)){
                list.add(mFiles.get(i));
            }
        }
        return list;
    }
    public int getCheckedCount(){
        return mCheckedCount;
    }

    public class DirectoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView mFileImage;
        private TextView mFileTitle;
        private TextView mFileSubtitle;
        private CheckBox mCheckBox;
        public DirectoryViewHolder(View itemView, final OnItemClickListener clickListener) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isCheckMode() && !mFiles.get(getAdapterPosition()).isDirectory()){
                        mCheckBox.setChecked(!mCheckBox.isChecked());
                    }
                    clickListener.onItemClick(v, getAdapterPosition(), isCheckMode());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //exclude directory item's click event
                    if(!mFiles.get(getAdapterPosition()).isDirectory()){
                        clickListener.onItemLongClick(v,getAdapterPosition(),isCheckMode());
                        return true;
                    }
                    return false;
                }
            });

            mFileImage = (ImageView) itemView.findViewById(R.id.item_file_image);
            mFileTitle = (TextView) itemView.findViewById(R.id.item_file_title);
            mFileSubtitle = (TextView) itemView.findViewById(R.id.item_file_subtitle);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.item_file_check_box);
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if( mCountLimitation != 0 && mCheckedCount >= mCountLimitation){
                            buttonView.setChecked(false);
                            Toast.makeText(mContext, mContext.getText(R.string.reached_pick_limitation), Toast.LENGTH_SHORT).show();
                        }else{
                            mCheckedMap.set(getAdapterPosition(),isChecked);
                            mCheckedCount++;
                        }
                    }else{
                        mCheckedMap.set(getAdapterPosition(),isChecked);
                        mCheckedCount--;
                    }
                }
            });
        }
    }

}