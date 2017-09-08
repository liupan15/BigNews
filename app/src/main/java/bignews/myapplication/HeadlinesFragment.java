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

import bignews.myapplication.db.Dao;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bignews.myapplication.db.DaoParam;
import bignews.myapplication.db.HeadLine;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lazycal on 2017/9/7.
 */

public class HeadlinesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "HeadlinesFragment";
    Dao dao = Dao.getInstance(); //should be a singleton.

    private String mText;

    @BindView(R.id.listView)
    PullToRefreshListView listView;

    private List<HeadLine> news = new ArrayList<>();
    OnHeadlineSelectedListener mCallback;
    private ArrayAdapter<HeadLine> adapter;
    private SingleObserver<? super ArrayList<HeadLine>> subscriber = new SingleObserver<ArrayList<HeadLine>>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            disposable = d;
        }

        @Override
        public void onSuccess(@NonNull ArrayList<HeadLine> strings) {
            news = strings;
            Log.i(TAG, "onSuccess: loadNewsDatasuccess "+mText+" "+news);
            adapter.clear();
            adapter.addAll(news);
            adapter.notifyDataSetChanged();
            listView.onRefreshComplete();
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.i(TAG, "onError: timeout");
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
        mCallback.onArticleSelected(Integer.parseInt(news.get((int)id).newsID));
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        Log.i(TAG, "onCreate: "+mText);
        super.onCreate(bundle);
        if(getArguments()!=null){
            mText = getArguments().getString("text");
        }
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
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
        adapter = new ArrayAdapter<HeadLine>(getActivity(), layout, news);
        listView.setAdapter(adapter);
        loadNewsData();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: "+mText);
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadNewsData() {
        final DaoParam param = DaoParam.fromCategory(Integer.parseInt(mText));
        //news = dao.getNewsList(param);
        Single.create(new SingleOnSubscribe<ArrayList<HeadLine>>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<ArrayList<HeadLine>> e) throws Exception {
                e.onSuccess(dao.getNewsList(param));
            }
        }).timeout(3, TimeUnit.SECONDS)
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