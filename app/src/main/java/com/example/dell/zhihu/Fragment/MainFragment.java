package com.example.dell.zhihu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.dell.zhihu.Activity.LatestContentActivity;
import com.example.dell.zhihu.Activity.MainActivity;
import com.example.dell.zhihu.Adapter.MainNewsItemAdapter;
import com.example.dell.zhihu.Model.Before;
import com.example.dell.zhihu.Model.Latest;
import com.example.dell.zhihu.Model.StoryBean;
import com.example.dell.zhihu.R;
import com.example.dell.zhihu.Util.Constant;
import com.example.dell.zhihu.Util.HttpUtil;
import com.example.dell.zhihu.View.Kanner;
import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.List;


/**
 * Created by DELL on 2016/6/25.
 */
public class MainFragment extends  BaseFragment {

    private Latest mLatest;
    private Handler mHandler=new Handler();
    private Kanner mKanner;
    private ListView mListView;
    private MainNewsItemAdapter mAdapter;
    private String mDate;
    private Before mBefore;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.main_news_layout,container,false);
        mListView= (ListView) view.findViewById(R.id.news_ListView);
        View header=inflater.inflate(R.layout.kanner,mListView,false);
        mKanner= (Kanner) header.findViewById(R.id.kanner);
     //   mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.main_swipe);
        mKanner.setOnItemClickListener(new Kanner.OnItemClickListener() {
            @Override
            public void click(View view, Latest.TopStoriesBean bean) {
                StoryBean story=new StoryBean();
                story.setId(bean.getId());
                story.setTitle(bean.getTitle());
                Intent intent=new Intent(getActivity(), LatestContentActivity.class);
                intent.putExtra("story",story);
                startActivity(intent);
            }
        });
        mListView.addHeaderView(header);
        mAdapter=new MainNewsItemAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mListView != null && mListView.getChildCount() > 0) {
                    boolean enable = (firstVisibleItem == 0) && (view.getChildAt(firstVisibleItem).getTop() == 0);
                    MainActivity.setSwipeRefreshEnable(enable);
                    if(firstVisibleItem+visibleItemCount==totalItemCount) {
                        loadBefore(Constant.BEFORE + mDate);
                    }
                }

            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StoryBean story= (StoryBean) parent.getAdapter().getItem(position);
                Intent intent=new Intent(getActivity(),LatestContentActivity.class);
                intent.putExtra("story",story);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
        HttpUtil.get(Constant.LATESTNEWS, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Gson gson=new Gson();
                mLatest=gson.fromJson(responseString,Latest.class);
                mDate=mLatest.getDate();
                mKanner.setTopBeans(mLatest.getTop_stories());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<StoryBean> storiesEntities = mLatest.getStories();
                        StoryBean topic = new StoryBean();
                        topic.setType(Constant.TOPIC);
                        topic.setTitle("今日热闻");
                        storiesEntities.add(0, topic);
                        mAdapter.addList(storiesEntities);

                    }
                });

            }
        });

    }
    private void loadBefore(String url){
    if(HttpUtil.isNetworkConnected(getActivity())){
        HttpUtil.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Gson gson=new Gson();
                mBefore=gson.fromJson(responseString, Before.class);
                mDate=mBefore.getDate();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<StoryBean> mBeans=mBefore.getStories();
                        StoryBean storyBean=new StoryBean();
                        storyBean.setType(Constant.TOPIC);
                        storyBean.setTitle(convertDate(mDate));
                        mBeans.add(0,storyBean);
                        mAdapter.addList(mBeans);

                    }
                });
            }
        });
    }
    }
    private String convertDate(String date){
        String result=date.substring(0,4);
        result+="年";
        result+=date.substring(4,6);
        result+="月";
        result+=date.substring(6,8);
        return  result;
    }
}
