package com.offlineledger.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PersonDao _personDao;

  private volatile TransactionDao _transactionDao;

  private volatile ReminderLogDao _reminderLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `persons` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `mobileNumber` TEXT NOT NULL, `isBlacklisted` INTEGER NOT NULL, `reminderEnabled` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personId` INTEGER NOT NULL, `amount` REAL NOT NULL, `label` TEXT NOT NULL, `details` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_personId` ON `transactions` (`personId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminder_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personName` TEXT NOT NULL, `mobileNumber` TEXT NOT NULL, `message` TEXT NOT NULL, `sentAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd3ec01a1859f80472e98d2d481643c71')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `persons`");
        db.execSQL("DROP TABLE IF EXISTS `transactions`");
        db.execSQL("DROP TABLE IF EXISTS `reminder_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPersons = new HashMap<String, TableInfo.Column>(6);
        _columnsPersons.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("mobileNumber", new TableInfo.Column("mobileNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("isBlacklisted", new TableInfo.Column("isBlacklisted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("reminderEnabled", new TableInfo.Column("reminderEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPersons = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPersons = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPersons = new TableInfo("persons", _columnsPersons, _foreignKeysPersons, _indicesPersons);
        final TableInfo _existingPersons = TableInfo.read(db, "persons");
        if (!_infoPersons.equals(_existingPersons)) {
          return new RoomOpenHelper.ValidationResult(false, "persons(com.offlineledger.data.model.Person).\n"
                  + " Expected:\n" + _infoPersons + "\n"
                  + " Found:\n" + _existingPersons);
        }
        final HashMap<String, TableInfo.Column> _columnsTransactions = new HashMap<String, TableInfo.Column>(6);
        _columnsTransactions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("personId", new TableInfo.Column("personId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("details", new TableInfo.Column("details", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactions = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTransactions.add(new TableInfo.ForeignKey("persons", "CASCADE", "NO ACTION", Arrays.asList("personId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTransactions = new HashSet<TableInfo.Index>(1);
        _indicesTransactions.add(new TableInfo.Index("index_transactions_personId", false, Arrays.asList("personId"), Arrays.asList("ASC")));
        final TableInfo _infoTransactions = new TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions);
        final TableInfo _existingTransactions = TableInfo.read(db, "transactions");
        if (!_infoTransactions.equals(_existingTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "transactions(com.offlineledger.data.model.Transaction).\n"
                  + " Expected:\n" + _infoTransactions + "\n"
                  + " Found:\n" + _existingTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsReminderLogs = new HashMap<String, TableInfo.Column>(5);
        _columnsReminderLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("personName", new TableInfo.Column("personName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("mobileNumber", new TableInfo.Column("mobileNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("sentAt", new TableInfo.Column("sentAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminderLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReminderLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReminderLogs = new TableInfo("reminder_logs", _columnsReminderLogs, _foreignKeysReminderLogs, _indicesReminderLogs);
        final TableInfo _existingReminderLogs = TableInfo.read(db, "reminder_logs");
        if (!_infoReminderLogs.equals(_existingReminderLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "reminder_logs(com.offlineledger.data.model.ReminderLog).\n"
                  + " Expected:\n" + _infoReminderLogs + "\n"
                  + " Found:\n" + _existingReminderLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d3ec01a1859f80472e98d2d481643c71", "fe7358ca6007841c20a58a8e74064866");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "persons","transactions","reminder_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `persons`");
      _db.execSQL("DELETE FROM `transactions`");
      _db.execSQL("DELETE FROM `reminder_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PersonDao.class, PersonDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderLogDao.class, ReminderLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PersonDao personDao() {
    if (_personDao != null) {
      return _personDao;
    } else {
      synchronized(this) {
        if(_personDao == null) {
          _personDao = new PersonDao_Impl(this);
        }
        return _personDao;
      }
    }
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public ReminderLogDao reminderLogDao() {
    if (_reminderLogDao != null) {
      return _reminderLogDao;
    } else {
      synchronized(this) {
        if(_reminderLogDao == null) {
          _reminderLogDao = new ReminderLogDao_Impl(this);
        }
        return _reminderLogDao;
      }
    }
  }
}
