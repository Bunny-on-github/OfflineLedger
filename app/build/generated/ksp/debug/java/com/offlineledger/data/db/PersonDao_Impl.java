package com.offlineledger.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.offlineledger.data.model.Person;
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
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PersonDao_Impl implements PersonDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Person> __insertionAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter<Person> __deletionAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter<Person> __updateAdapterOfPerson;

  private final SharedSQLiteStatement __preparedStmtOfSetBlacklisted;

  private final SharedSQLiteStatement __preparedStmtOfSetReminderEnabled;

  public PersonDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerson = new EntityInsertionAdapter<Person>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `persons` (`id`,`name`,`mobileNumber`,`isBlacklisted`,`reminderEnabled`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Person entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getMobileNumber());
        final int _tmp = entity.isBlacklisted() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.getReminderEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Person>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `persons` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Person entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Person>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `persons` SET `id` = ?,`name` = ?,`mobileNumber` = ?,`isBlacklisted` = ?,`reminderEnabled` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Person entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getMobileNumber());
        final int _tmp = entity.isBlacklisted() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.getReminderEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfSetBlacklisted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET isBlacklisted = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetReminderEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET reminderEnabled = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Person person, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPerson.insertAndReturnId(person);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Person person, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPerson.handle(person);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Person person, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPerson.handle(person);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setBlacklisted(final long personId, final boolean blacklisted,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetBlacklisted.acquire();
        int _argIndex = 1;
        final int _tmp = blacklisted ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, personId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetBlacklisted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setReminderEnabled(final long personId, final boolean enabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetReminderEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, personId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetReminderEnabled.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Person>> getAllPersons() {
    final String _sql = "SELECT * FROM persons ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfIsBlacklisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isBlacklisted");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobileNumber;
            _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            final boolean _tmpIsBlacklisted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBlacklisted);
            _tmpIsBlacklisted = _tmp != 0;
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Person(_tmpId,_tmpName,_tmpMobileNumber,_tmpIsBlacklisted,_tmpReminderEnabled,_tmpCreatedAt);
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
  public Object getPersonById(final long id, final Continuation<? super Person> $completion) {
    final String _sql = "SELECT * FROM persons WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Person>() {
      @Override
      @Nullable
      public Person call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfIsBlacklisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isBlacklisted");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Person _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobileNumber;
            _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            final boolean _tmpIsBlacklisted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBlacklisted);
            _tmpIsBlacklisted = _tmp != 0;
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Person(_tmpId,_tmpName,_tmpMobileNumber,_tmpIsBlacklisted,_tmpReminderEnabled,_tmpCreatedAt);
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
  public Object getAllPersonsSync(final Continuation<? super List<Person>> $completion) {
    final String _sql = "SELECT * FROM persons";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfIsBlacklisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isBlacklisted");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobileNumber;
            _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            final boolean _tmpIsBlacklisted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBlacklisted);
            _tmpIsBlacklisted = _tmp != 0;
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Person(_tmpId,_tmpName,_tmpMobileNumber,_tmpIsBlacklisted,_tmpReminderEnabled,_tmpCreatedAt);
            _result.add(_item);
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
  public Object getReminderEnabledPersons(final Continuation<? super List<Person>> $completion) {
    final String _sql = "SELECT * FROM persons WHERE reminderEnabled = 1 AND mobileNumber != ''";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfIsBlacklisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isBlacklisted");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobileNumber;
            _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            final boolean _tmpIsBlacklisted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBlacklisted);
            _tmpIsBlacklisted = _tmp != 0;
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Person(_tmpId,_tmpName,_tmpMobileNumber,_tmpIsBlacklisted,_tmpReminderEnabled,_tmpCreatedAt);
            _result.add(_item);
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
  public Object getPersonByName(final String name, final Continuation<? super Person> $completion) {
    final String _sql = "SELECT * FROM persons WHERE name = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Person>() {
      @Override
      @Nullable
      public Person call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfIsBlacklisted = CursorUtil.getColumnIndexOrThrow(_cursor, "isBlacklisted");
          final int _cursorIndexOfReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderEnabled");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Person _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobileNumber;
            _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            final boolean _tmpIsBlacklisted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBlacklisted);
            _tmpIsBlacklisted = _tmp != 0;
            final boolean _tmpReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfReminderEnabled);
            _tmpReminderEnabled = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Person(_tmpId,_tmpName,_tmpMobileNumber,_tmpIsBlacklisted,_tmpReminderEnabled,_tmpCreatedAt);
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
