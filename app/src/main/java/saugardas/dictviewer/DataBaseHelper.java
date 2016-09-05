package saugardas.dictviewer;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/saugardas.dictviewer/databases/";
    private static String DB_NAME = "dict.sqlite";
    private static String DB_FULL_PATH = DB_PATH + DB_NAME;
    private SQLiteDatabase myDataBase;
    private final Context mContext;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.mContext = context;
    }

    // создает пустую базу данных и перезаписывает её нашей собственной базой
    public void createDataBase() throws IOException {
        //dropDataBase();
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            // вызывая этот метод создаем пустую базу, позже она будет перезаписана
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    // проверяет, существует ли уже эта база, чтобы не копировать каждый раз при запуске приложения
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DB_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e) {
            // база еще не существует
        }
        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null;
    }

    // копирует базу из папки assets заместо созданной локальной БД
    // выполняется путем копирования потока байтов.
    private void copyDataBase() throws IOException {
        // открываем локальную БД как входящий поток
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // открываем пустую базу данных как исходящий поток
        OutputStream myOutput = new FileOutputStream(DB_FULL_PATH);

        // перемещаем байты из входящего файла в исходящий
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0){
            myOutput.write(buffer, 0, length);
        }

        // закрываем потоки
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    // открывает БД
    public void openDataBase() throws SQLException {
        myDataBase = SQLiteDatabase.openDatabase(DB_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void dropDataBase() {
        SQLiteDatabase.deleteDatabase(new File(DB_FULL_PATH));
    }

    // создаём и открываем
    public void loadDataBase() {
        try {
            createDataBase();
        }
        catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        openDataBase();
    }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
