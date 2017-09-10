package bignews.myapplication.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;

import bignews.myapplication.db.dao.HeadlineDao;

import static org.junit.Assert.*;

/**
 * Created by lazycal on 2017/9/11.
 */
@RunWith(AndroidJUnit4.class)
public class DAOTest {

    private static final String TAG = "DAOTest";
    private DAO dao;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        dao = DAO.init(context, Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build());
    }
    @After
    public void closeDb() throws IOException {
        //dao.mDb.close();
    }
    @Test
    public void getNews() throws Exception {
        News news = dao.getNews(DAOParam.fromNewsId("201609130413080e91293fb5402b80437a65970fcb7d")).blockingGet();
        Log.i(TAG, "getNews: first="+news);
        assertEquals(news.news_Title, "乐视921邀请函曝光 乐Pro 3悬疑即将揭晓");
        news = dao.getNews(DAOParam.fromNewsId("201609130413080e91293fb5402b80437a65970fcb7d")).blockingGet();
        Log.i(TAG, "getNews: second="+news);
        assertEquals(news.news_Title, "乐视921邀请函曝光 乐Pro 3悬疑即将揭晓");
        Preferences preferences = dao.getSettings();
        Log.i(TAG, "getNews: "+preferences);
        preferences.isOffline = true;
        dao.setSettings(preferences);
        news = dao.getNews(DAOParam.fromNewsId("201609130413080e91293fb5402b80437a65970fcb7d")).blockingGet();
        Log.i(TAG, "getNews: third="+news);
        assertEquals(news.news_Title, "乐视921邀请函曝光 乐Pro 3悬疑即将揭晓");
    }

    @Test
    public void getHeadlineList() throws Exception {

    }

    @Test
    public void star() throws Exception {
        News news = dao.getNews(DAOParam.fromNewsId("201609130413080e91293fb5402b80437a65970fcb7d")).blockingGet();
        Log.i(TAG, "getNews: "+news);
        assertEquals(news.news_Title, "乐视921邀请函曝光 乐Pro 3悬疑即将揭晓");
        ArrayList<Headline> headlines = dao.getHeadlineList(DAOParam.fromCategory(DAOParam.FAVORITE, 0, 10)).blockingGet();
        assertEquals(headlines.size(), 0);
        dao.star(news.news_ID).subscribe();
        Log.i(TAG, "star: all headlines="+dao.getHeadlineDao().getAll().blockingGet());
        headlines = dao.getHeadlineList(DAOParam.fromCategory(DAOParam.FAVORITE, 0, 10)).blockingGet();
        assertEquals(headlines.size(), 1);
    }

    @Test
    public void unStar() throws Exception {

    }

    @Test
    public void clearCache() throws Exception {

    }

}