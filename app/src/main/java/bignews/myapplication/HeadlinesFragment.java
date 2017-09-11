package bignews.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import bignews.myapplication.db.DAO;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bignews.myapplication.db.DAOParam;
import bignews.myapplication.db.Headline;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lazycal on 2017/9/7.
 */

public class HeadlinesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "HeadlinesFragment";
    private static final int LIMIT = 5;
    DAO dao = DAO.getInstance(); //should be a singleton.

    private String mText;
    private int mID;

    @BindView(R.id.listView)
    PullToRefreshListView listView;

    private List<Headline> news = new ArrayList<>();
    OnHeadlineSelectedListener mCallback;
    List<Map<String, Object>> datas = new ArrayList<>();
    private SimpleAdapter adapter;
    private SingleObserver<? super ArrayList<Headline>> subscriber = new SingleObserver<ArrayList<Headline>>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            disposable = d;
        }

        @Override
        public void onSuccess(@NonNull ArrayList<Headline> strings) {
            news.addAll(strings);
            for(int i = 0; i < strings.size(); ++i)
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("image", R.drawable.home);
                map.put("title", strings.get(i).news_Title + "/" + mText);
                datas.add(map);
                Log.v("Err", " " + i);
            }
            adapter.notifyDataSetChanged();
            listView.onRefreshComplete();
            Log.i(TAG, "onSuccess: loadNewsDatasuccess "+mText+" "+news);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.i(TAG, "onError: "+e);
            listView.onRefreshComplete();
        }
    };
    private Disposable disposable;

    public interface OnHeadlineSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onArticleSelected(int position);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Notify the parent activity of selected item
        mCallback = (OnHeadlineSelectedListener)getActivity();
        mCallback.onArticleSelected(Integer.parseInt(news.get((int)id).news_ID));
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        Log.i("Err", "onCreate: "+mText);
        super.onCreate(bundle);
        if(getArguments()!=null){
            mText = getArguments().getString("text");
            mID = getArguments().getInt("id");
        }
        if (news.size() == 0)
            loadNewsData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: "+mText);
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        ButterKnife.bind(this, view); //??? mogai
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: "+mText);
        super.onResume();

        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        // 下拉刷新
        //listView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        //listView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新...");
        //listView.getLoadingLayoutProxy(true, false).setReleaseLabel("释放立即刷新");
        // 下拉加载
        listView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
        listView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        listView.getLoadingLayoutProxy(false, true).setReleaseLabel("释放立即加载");
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
                Log.i("1", "onPullUpToLoad: hhh");
                loadNewsData();
            }
        });

        listView.setOnItemClickListener(this);

        adapter = new SimpleAdapter(getActivity(), datas, R.layout.search_item,
                                    new String[]{"title", "image"}, new int[]{R.id.title, R.id.image});
        listView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: "+mText);
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadNewsData() {//!!!BUG
        final DAOParam param = DAOParam.fromCategory(mID, news.size(), LIMIT);
        //news = dao.getHeadlineList(param);
        /*Single.create(new SingleOnSubscribe<ArrayList<Headline>>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<ArrayList<Headline>> e) throws Exception {
                e.onSuccess(dao.getHeadlineList(param));
            }
        })*/
        dao.getHeadlineList(param)
                .timeout(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
        Log.i(TAG, "loadNewsData: "+news+" "+mText);

    }
    @Override
    public void onPause() {
        super.onPause();
        if (disposable != null) {
            Log.i(TAG, "onPause: " + mText + " Disposing.");
            disposable.dispose();
        }
    }
}
