package com.example.conscia.data.rule;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RuleDao_Impl implements RuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RuleEntity> __insertionAdapterOfRuleEntity;

  private final EntityDeletionOrUpdateAdapter<RuleEntity> __deletionAdapterOfRuleEntity;

  private final EntityDeletionOrUpdateAdapter<RuleEntity> __updateAdapterOfRuleEntity;

  public RuleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRuleEntity = new EntityInsertionAdapter<RuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `rules` (`id`,`packageName`,`appName`,`intentionLabel`,`dailyLimitMinutes`,`trackingEnabled`,`warningEnabled`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPackageName());
        statement.bindString(3, entity.getAppName());
        statement.bindString(4, entity.getIntentionLabel());
        statement.bindLong(5, entity.getDailyLimitMinutes());
        final int _tmp = entity.getTrackingEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.getWarningEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfRuleEntity = new EntityDeletionOrUpdateAdapter<RuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `rules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RuleEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRuleEntity = new EntityDeletionOrUpdateAdapter<RuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `rules` SET `id` = ?,`packageName` = ?,`appName` = ?,`intentionLabel` = ?,`dailyLimitMinutes` = ?,`trackingEnabled` = ?,`warningEnabled` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPackageName());
        statement.bindString(3, entity.getAppName());
        statement.bindString(4, entity.getIntentionLabel());
        statement.bindLong(5, entity.getDailyLimitMinutes());
        final int _tmp = entity.getTrackingEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.getWarningEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insertRule(final RuleEntity rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRuleEntity.insert(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRule(final RuleEntity rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRule(final RuleEntity rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RuleEntity>> getAllRules() {
    final String _sql = "SELECT * FROM rules ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"rules"}, new Callable<List<RuleEntity>>() {
      @Override
      @NonNull
      public List<RuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIntentionLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "intentionLabel");
          final int _cursorIndexOfDailyLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMinutes");
          final int _cursorIndexOfTrackingEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "trackingEnabled");
          final int _cursorIndexOfWarningEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "warningEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<RuleEntity> _result = new ArrayList<RuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpIntentionLabel;
            _tmpIntentionLabel = _cursor.getString(_cursorIndexOfIntentionLabel);
            final int _tmpDailyLimitMinutes;
            _tmpDailyLimitMinutes = _cursor.getInt(_cursorIndexOfDailyLimitMinutes);
            final boolean _tmpTrackingEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfTrackingEnabled);
            _tmpTrackingEnabled = _tmp != 0;
            final boolean _tmpWarningEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfWarningEnabled);
            _tmpWarningEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new RuleEntity(_tmpId,_tmpPackageName,_tmpAppName,_tmpIntentionLabel,_tmpDailyLimitMinutes,_tmpTrackingEnabled,_tmpWarningEnabled,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getRuleById(final long id, final Continuation<? super RuleEntity> $completion) {
    final String _sql = "SELECT * FROM rules WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RuleEntity>() {
      @Override
      @Nullable
      public RuleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIntentionLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "intentionLabel");
          final int _cursorIndexOfDailyLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMinutes");
          final int _cursorIndexOfTrackingEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "trackingEnabled");
          final int _cursorIndexOfWarningEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "warningEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RuleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpIntentionLabel;
            _tmpIntentionLabel = _cursor.getString(_cursorIndexOfIntentionLabel);
            final int _tmpDailyLimitMinutes;
            _tmpDailyLimitMinutes = _cursor.getInt(_cursorIndexOfDailyLimitMinutes);
            final boolean _tmpTrackingEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfTrackingEnabled);
            _tmpTrackingEnabled = _tmp != 0;
            final boolean _tmpWarningEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfWarningEnabled);
            _tmpWarningEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RuleEntity(_tmpId,_tmpPackageName,_tmpAppName,_tmpIntentionLabel,_tmpDailyLimitMinutes,_tmpTrackingEnabled,_tmpWarningEnabled,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRuleByPackageName(final String packageName,
      final Continuation<? super RuleEntity> $completion) {
    final String _sql = "SELECT * FROM rules WHERE packageName = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, packageName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RuleEntity>() {
      @Override
      @Nullable
      public RuleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIntentionLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "intentionLabel");
          final int _cursorIndexOfDailyLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyLimitMinutes");
          final int _cursorIndexOfTrackingEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "trackingEnabled");
          final int _cursorIndexOfWarningEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "warningEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final RuleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpIntentionLabel;
            _tmpIntentionLabel = _cursor.getString(_cursorIndexOfIntentionLabel);
            final int _tmpDailyLimitMinutes;
            _tmpDailyLimitMinutes = _cursor.getInt(_cursorIndexOfDailyLimitMinutes);
            final boolean _tmpTrackingEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfTrackingEnabled);
            _tmpTrackingEnabled = _tmp != 0;
            final boolean _tmpWarningEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfWarningEnabled);
            _tmpWarningEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new RuleEntity(_tmpId,_tmpPackageName,_tmpAppName,_tmpIntentionLabel,_tmpDailyLimitMinutes,_tmpTrackingEnabled,_tmpWarningEnabled,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
