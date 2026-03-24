package com.offlineledger.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.offlineledger.data.model.ReminderLog;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderLogDao_Impl implements ReminderLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReminderLog> __insertionAdapterOfReminderLog;

  public ReminderLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminderLog = new EntityInsertionAdapter<ReminderLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reminder_logs` (`id`,`personName`,`mobileNumber`,`message`,`sentAt`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPersonName());
        statement.bindString(3, entity.getMobileNumber());
        statement.bindString(4, entity.getMessage());
        statement.bindLong(5, entity.getSentAt());
      }
    };
  }

  @Override
  public Object insert(final ReminderLog log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReminderLog.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReminderLog>> getAllLogs() {
    final String _sql = "SELECT * FROM reminder_logs ORDER BY sentAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reminder_logs"}, new Callable<List<ReminderLog>>() {
      @Override
      @NonNull
      public List<ReminderLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonName = CursorUtil.getColumnIndexOrThrow(_cursor, "personName");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfSentAt = CursorUtil.getColumnIndexOrThrow(_cursor, "sentAt");
          final List<ReminderLog> _result = new ArrayList<ReminderLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPersonName;
            _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            final String _tmpMobileNumber;
            _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final long _tmpSentAt;
            _tmpSentAt = _cursor.getLong(_cursorIndexOfSentAt);
            _item = new ReminderLog(_tmpId,_tmpPersonName,_tmpMobileNumber,_tmpMessage,_tmpSentAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
