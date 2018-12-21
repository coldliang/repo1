package com.cqupt.ui.common;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.fragment.AudioViewFragment;
import com.cqupt.fragment.TextViewSupportFragment;
import com.cqupt.fragment.TreeFragment;
import com.cqupt.fragment.VideoViewFragment;
import com.cqupt.model.Chapter;
import com.cqupt.model.ChapterGroup;
import com.cqupt.model.Lesson;
import com.cqupt.model.UserInfo;
import com.cqupt.net.WebService;
import com.cqupt.util.FileUtil;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alan on 2016/5/20.
 */
public class OnlineLearningDetailActivity extends FragmentActivity implements View.OnClickListener
,TreeFragment.OnTreeNodeClickListener{

    private static final String INTENT_KEY_LESSON = "lessonId";
    private static final String FRAGMENT_TAG_VIDEO = "video";
    private static final String FRAGMENT_TAG_AUDIO = "audio";
    private static final String FRAGMENT_TAG_TEXT = "text";

    private FragmentManager mFragmentManager;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TextView mReturnView;

    private Lesson mLesson;
    private ArrayList<ChapterGroup> mChapterListData = new ArrayList<>();
    private Chapter mCurrentChapter;
    private UserInfo mLessonTeacherInfo;

    //------------------------override method---------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_learning_detail);
        mFragmentManager = getSupportFragmentManager();

        initViews();
        loadData();
        setListener();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.returnView : onBackPressed();break;
        }

    }

    @Override
    public void OnTreeNodeClick(Chapter chapter) {

        if(mCurrentChapter != chapter){

            mCurrentChapter = chapter;
            //change the content fragment
            String uri = chapter.getAttachmentUri();

            int type = FileUtil.getFileType(uri);
            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            AudioViewFragment f1 = (AudioViewFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_AUDIO);
            VideoViewFragment f2 = (VideoViewFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_VIDEO);
            TextViewSupportFragment f3 = (TextViewSupportFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_TEXT);

            switch (type){
                case FileUtil.TYPE_AUDIO :

                    if(f1 == null){
                        f1 = AudioViewFragment.getInstance(chapter.getAttachmentUri());
                        transaction.add(R.id.content,f1,FRAGMENT_TAG_AUDIO);
                    }else{
                        f1.setAudioUri(chapter.getAttachmentUri());
                        transaction.show(f1);
                    }

                    if(f2 != null){
                        transaction.hide(f2);
                    }

                    if(f3 != null){
                        transaction.hide(f3);
                    }

                    break;
                case FileUtil.TYPE_VIDEO :
                    if(f2 == null){
                        f2 = VideoViewFragment.getInstance(chapter.getAttachmentUri());
                        transaction.add(R.id.content,f2,FRAGMENT_TAG_VIDEO);
                    }else{
                        f2.setUri(chapter.getAttachmentUri());
                        transaction.show(f2);
                    }

                    if(f1 != null){
                        transaction.hide(f1);
                    }

                    if(f3 != null){
                        transaction.hide(f3);
                    }

                    break;
                default :
                    if(f3 == null){
                        f3 = TextViewSupportFragment.getInstance(chapter.getChapterContent());
                        transaction.add(R.id.content,f3,FRAGMENT_TAG_TEXT);
                    }else{
                        f3.setTextContent(chapter.getChapterContent());
                        transaction.show(f3);
                    }

                    if(f2 != null){
                        transaction.hide(f2);
                    }

                    if(f1 != null){
                        transaction.hide(f1);
                    }

                    break;
            }

            transaction.commit();

        }


    }

    //----------------------self method----------------------------------------

    private void initViews(){

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mReturnView = (TextView) findViewById(R.id.returnView);

    }

    private void setListener(){

        mReturnView.setOnClickListener(this);

    }

    private void loadData(){

        Intent intent = getIntent();
        mLesson = (Lesson) intent.getSerializableExtra(INTENT_KEY_LESSON);

        if(mLesson != null){
            new GetTeacherInfoThread().execute();
            new GetChapterListThread().execute();
        }

    }

    public static Intent getStartActivityIntent(Context context, Lesson lesson){
        Intent intent = new Intent(context,OnlineLearningDetailActivity.class);
        intent.putExtra(INTENT_KEY_LESSON,lesson);
        return intent;
    }

    //---------------------inner class---------------------

    class GetTeacherInfoThread extends AsyncTask<Void,Void,Integer>{

        private WebService mWebService = new WebService();

        @Override
        protected Integer doInBackground(Void... params) {

            if(mLesson != null){
                HashMap<String,String> p = new HashMap<>();
                p.put("userID",mLesson.getCreateUserId());

                SoapObject result = mWebService.CallWebService("getUserInfo",p);

                if(result == null){
                    return null;
                }

                mLessonTeacherInfo = XMLParser.ParseUserInfo(result);

                return WebService.SUCCESS;

            }else{
                return null;
            }

        }

        @Override
        protected void onPostExecute(Integer result) {

            if(result != null){
                TextViewSupportFragment f = (TextViewSupportFragment) mFragmentManager.findFragmentByTag("android:switcher:view_pager:1");

                if(f != null){
                    f.setTextContent(mLessonTeacherInfo.getUserName() + "\n" + mLessonTeacherInfo.getUserDepartment());
                }
            }

        }
    }

    class GetChapterListThread extends AsyncTask<Void,Void,Integer>{

        private WebService mWebService = new WebService();

        @Override
        protected Integer doInBackground(Void... params) {

            HashMap<String,String> p = new HashMap<>();
            p.put("lessonId",String.valueOf(mLesson.getId()));
            SoapObject result = mWebService.CallWebService("getChapterList",p);

            if(result == null){
                return WebService.CONNECTION_TIMEOUT;
            }

            mChapterListData = XMLParser.parseChapterGroup(result);

            return WebService.SUCCESS;

        }

        @Override
        protected void onPostExecute(Integer result) {

            if(result == WebService.CONNECTION_TIMEOUT){
                ShowToastUtil.showConnectionTimeOutToast(OnlineLearningDetailActivity.this);
            }else{

                mViewPager.setAdapter(new FragmentPagerAdapter(mFragmentManager) {

                    @Override
                    public android.support.v4.app.Fragment getItem(int position) {

                        switch (position){
                            case 0 :
                                TreeFragment f = TreeFragment.getInstance(mChapterListData);
                                f.setOnTreeNodeClickListener(OnlineLearningDetailActivity.this);
                                return f;
                            case 1 :
                                return TextViewSupportFragment.getInstance(
                                        mLessonTeacherInfo == null ? "" :
                                                "<p>" + mLessonTeacherInfo.getUserName() + "</p><p>" + mLessonTeacherInfo.getUserDepartment() + "</p>");
                            case 2 : return TextViewSupportFragment.getInstance(mLesson.getIntroduction());
                            case 3 : return TextViewSupportFragment.getInstance(mLesson.getRefBook());
                            default : return null;
                        }

                    }

                    @Override
                    public int getCount() {
                        return 4;
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {

                        switch (position){
                            case 0 : return getResources().getString(R.string.lesson_category);
                            case 1 : return getResources().getString(R.string.teacher_profile);
                            case 2 : return getResources().getString(R.string.lesson_profile);
                            case 3 : return getResources().getString(R.string.lesson_refer_book);
                            default : return "";
                        }

                    }
                });
                mTabLayout.setupWithViewPager(mViewPager);

                FragmentTransaction transaction = mFragmentManager.beginTransaction();

                Chapter chapter = mChapterListData.get(0).getChapter();
                Fragment f;
                String tag;

                String uri = chapter.getAttachmentUri();

                int type = FileUtil.getFileType(uri);

                switch (type){
                    case FileUtil.TYPE_AUDIO :
                        f = AudioViewFragment.getInstance(chapter.getAttachmentUri());
                        tag = FRAGMENT_TAG_AUDIO;
                        break;
                    case FileUtil.TYPE_VIDEO :
                        f = VideoViewFragment.getInstance(chapter.getAttachmentUri());
                        tag = FRAGMENT_TAG_VIDEO;
                        break;
                    default :
                        f = TextViewSupportFragment.getInstance(chapter.getChapterContent());
                        tag = FRAGMENT_TAG_TEXT;
                }

                transaction.add(R.id.content,f,tag);
                transaction.commit();

            }

        }

    }

}
