package com.hiramine.fileselectiondialogtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements FileSelectionDialog.OnFileSelectListener
{
	// 定数
	private static final int MENUID_FILE                              = 0;// ファイルメニューID
	private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1; // 外部ストレージ読み込みパーミッション要求時の識別コード

	// メンバー変数
	private String m_strInitialDir = Environment.getExternalStorageDirectory().getPath();    // 初期フォルダ

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
	}

	// オプションメニュー生成時
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		menu.add( 0, MENUID_FILE, 0, "Select File..." );

		return true;
	}

	// オプションメニュー選択時
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case MENUID_FILE:
				// ダイアログオブジェクト
				FileSelectionDialog dlg = new FileSelectionDialog( this, this );
				dlg.show( new File( m_strInitialDir ) );
				return true;
		}
		return false;
	}

	// ファイルが選択されたときに呼び出される関数
	public void onFileSelect( File file )
	{
		Toast.makeText( this, "File Selected : " + file.getPath(), Toast.LENGTH_SHORT ).show();
		m_strInitialDir = file.getParent();
	}

	// 初回表示時、および、ポーズからの復帰時
	@Override
	protected void onResume()
	{
		super.onResume();

		// 外部ストレージ読み込みパーミッション要求
		requestReadExternalStoragePermission();
	}

	// 外部ストレージ読み込みパーミッション要求
	private void requestReadExternalStoragePermission()
	{
		if( PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) )
		{    // パーミッションは付与されている
			return;
		}
		// パーミッションは付与されていない。
		// パーミッションリクエスト
		ActivityCompat.requestPermissions( this,
										   new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
										   REQUEST_PERMISSION_READ_EXTERNAL_STORAGE );
	}

	// パーミッション要求ダイアログの操作結果
	@Override
	public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults )
	{
		switch( requestCode )
		{
			case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
				if( grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED )
				{
					// 許可されなかった場合
					Toast.makeText( this, "Permission denied.", Toast.LENGTH_SHORT ).show();
					finish();    // アプリ終了宣言
					return;
				}
				break;
			default:
				break;
		}
	}
}

