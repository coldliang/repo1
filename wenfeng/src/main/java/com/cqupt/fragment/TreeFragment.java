package com.cqupt.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.model.Chapter;
import com.cqupt.model.ChapterGroup;

import java.util.ArrayList;

/**
 * Created by Alan on 2016/5/24.
 */
public class TreeFragment extends Fragment {

    private static final String ARGUMENT_DATA = "data";

    private ArrayList<ChapterGroup> mDatas;
    private OnTreeNodeClickListener mOnTreeNodeClickListener;
    private ExpandableListView mListView;
    private MyListViewAdapter mMyListViewAdapter;

    private int mFocusedGroupPosition = -1;//-1 means no focused item
    private int mFocusedChildrenPosition = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mListView == null){
            mDatas = (ArrayList<ChapterGroup>) getArguments().getSerializable(ARGUMENT_DATA);
            mListView = new ExpandableListView(getActivity());

            LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            mListView.setLayoutParams(p);
            mMyListViewAdapter = new MyListViewAdapter();
            mListView.setAdapter(mMyListViewAdapter);

            mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                    mFocusedGroupPosition = groupPosition;
                    mFocusedChildrenPosition = -1;
                    mMyListViewAdapter.notifyDataSetChanged();

                    if(mOnTreeNodeClickListener != null){
                        mOnTreeNodeClickListener.OnTreeNodeClick(mDatas.get(groupPosition).getChapter());
                    }

                    return false;

                }
            });

            mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                    mFocusedGroupPosition = groupPosition;
                    mFocusedChildrenPosition = childPosition;
                    mMyListViewAdapter.notifyDataSetChanged();

                    if(mOnTreeNodeClickListener != null){
                        mOnTreeNodeClickListener.OnTreeNodeClick(mDatas.get(groupPosition).getChildren().get(childPosition));
                    }

                    return false;

                }
            });
        }

        return mListView;

    }

    public static TreeFragment getInstance(ArrayList<ChapterGroup> datas){

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_DATA,datas);
        TreeFragment f = new TreeFragment();
        f.setArguments(bundle);
        return f;

    }

    public void setOnTreeNodeClickListener(OnTreeNodeClickListener listener){
        mOnTreeNodeClickListener = listener;
    }

    public class MyListViewAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {

            return mDatas == null ? 0 : mDatas.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {

            if(mDatas == null || mDatas.get(groupPosition) == null || mDatas.get(groupPosition).getChildren() == null){
                return 0;
            }

            return mDatas.get(groupPosition).getChildren().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mDatas.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mDatas.get(groupPosition).getChildren().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_empty_textview,parent,false);

                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.WRAP_CONTENT);
                convertView.setLayoutParams(lp);
            }

            TextView textView = (TextView) convertView;
            textView.setPadding(80,20,0,20);
            textView.setText(mDatas.get(groupPosition).getChapter().getChapterTitle());

            if(mFocusedChildrenPosition == -1 && mFocusedGroupPosition == groupPosition){
                textView.setBackgroundColor(Color.LTGRAY);
            }else{
                textView.setBackgroundColor(Color.WHITE);
            }

            return convertView;

        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_empty_textview,parent,false);

                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.WRAP_CONTENT);
                convertView.setLayoutParams(lp);
            }

            TextView textView = (TextView) convertView;
            textView.setPadding(80,20,0,20);
            textView.setTextSize(13);
            textView.setTextColor(Color.GRAY);
            textView.setText(mDatas.get(groupPosition).getChildren().get(childPosition).getChapterTitle());

            if(mFocusedChildrenPosition == childPosition && mFocusedGroupPosition == groupPosition){
                textView.setBackgroundColor(Color.LTGRAY);
            }else{
                textView.setBackgroundColor(Color.WHITE);
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public interface OnTreeNodeClickListener {
        void OnTreeNodeClick(Chapter chapter);
    }
}
