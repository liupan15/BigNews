package bignews.myapplication.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.OnConflictStrategy;

import java.util.List;

import bignews.myapplication.db.Headline;

/**
 * Created by lazycal on 2017/9/9.
 */
@Dao
public interface HeadlineDao {
    @Query("select * FROM Headline")
    List<Headline> getAll();

    @Query("select * from Headline where newsClassTag = :newsClassTag limit :limit offset :offset")
    List<Headline> load(String newsClassTag, int offset, int limit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addHeadline(Headline headline);

    @Delete()
    void deleteHeadline(Headline headline);
}
