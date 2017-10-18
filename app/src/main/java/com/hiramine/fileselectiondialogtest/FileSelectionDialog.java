package com.hiramine.fileselectiondialogtest;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSelectionDialog implements AdapterView.OnItemClickListener
{
	static public class FileInfo implements Comparable<FileInfo>
	{
		private String m_strName;    // 表示名
		private File   m_file;    // ファイルオブジェクト

		// コンストラクタ
		public FileInfo( String strName, File file )
		{
			m_strName = strName;
			m_file = file;
		}

		public String getName()
		{
			return m_strName;
		}

		public File getFile()
		{
			return m_file;
		}

		// 比較
		public int compareTo( FileInfo another )
		{
			// ディレクトリ < ファイル の順
			if( m_file.isDirectory() && !another.getFile().isDirectory() )
			{
				return -1;
			}
			if( !m_file.isDirectory() && another.getFile().isDirectory() )
			{
				return 1;
			}

			// ファイル同士、ディレクトリ同士の場合は、ファイル名（ディレクトリ名）の大文字小文字区別しない辞書順
			return m_file.getName().toLowerCase().compareTo( another.getFile().getName().toLowerCase() );
		}
	}

	static public class FileInfoArrayAdapter extends BaseAdapter
	{
		private Context        m_context;
		private List<FileInfo> m_listFileInfo; // ファイル情報リスト

		// コンストラクタ
		public FileInfoArrayAdapter( Context context, List<FileInfo> list )
		{
			super();
			m_context = context;
			m_listFileInfo = list;
		}

		@Override
		public int getCount()
		{
			return m_listFileInfo.size();
		}

		@Override
		public FileInfo getItem( int position )
		{
			return m_listFileInfo.get( position );
		}

		@Override
		public long getItemId( int position )
		{
			return position;
		}

		static class ViewHolder
		{
			TextView textviewFileName;
			TextView textviewFileSize;
		}

		// 一要素のビューの生成
		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			ViewHolder viewHolder;
			if( null == convertView )
			{
				// レイアウト
				LinearLayout layout = new LinearLayout( m_context );
				layout.setOrientation( LinearLayout.VERTICAL );
				layout.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
				// ファイル名テキスト
				TextView textviewFileName = new TextView( m_context );
				textviewFileName.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 24 );
				layout.addView( textviewFileName, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
				// ファイルサイズテキスト
				TextView textviewFileSize = new TextView( m_context );
				textviewFileSize.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 12 );
				layout.addView( textviewFileSize, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );

				convertView = layout;
				viewHolder = new ViewHolder();
				viewHolder.textviewFileName = textviewFileName;
				viewHolder.textviewFileSize = textviewFileSize;
				convertView.setTag( viewHolder );
			}
			else
			{
				viewHolder = (ViewHolder)convertView.getTag();
			}

			FileInfo fileinfo = m_listFileInfo.get( position );
			if( fileinfo.getFile().isDirectory() )
			{ // ディレクトリの場合は、名前の後ろに「/」を付ける
				viewHolder.textviewFileName.setText( fileinfo.getName() + "/" );
				viewHolder.textviewFileSize.setText( "(directory)" );
			}
			else
			{
				viewHolder.textviewFileName.setText( fileinfo.getName() );
				viewHolder.textviewFileSize.setText( String.valueOf( fileinfo.getFile().length() / 1024 ) + " [KB]" );
			}

			return convertView;
		}
	}

	private Context              m_contextParent;    // 呼び出し元
	private OnFileSelectListener m_listener;    // 結果受取先
	private AlertDialog          m_dialog;    // ダイアログ
	private FileInfoArrayAdapter m_fileinfoarrayadapter; // ファイル情報配列アダプタ

	// コンストラクタ
	public FileSelectionDialog( Context context, OnFileSelectListener listener )
	{
		m_contextParent = context;
		m_listener = listener;
	}

	// ダイアログの作成と表示
	public void show( File fileDirectory )
	{
		// タイトル
		String strTitle = fileDirectory.getAbsolutePath();

		// リストビュー
		ListView listview = new ListView( m_contextParent );
		listview.setScrollingCacheEnabled( false );
		listview.setOnItemClickListener( this );
		// ファイルリスト
		File[]         aFile        = fileDirectory.listFiles();
		List<FileInfo> listFileInfo = new ArrayList<>();
		if( null != aFile )
		{
			for( File fileTemp : aFile )
			{
				listFileInfo.add( new FileInfo( fileTemp.getName(), fileTemp ) );
			}
			Collections.sort( listFileInfo );
		}
		// 親フォルダに戻るパスの追加
		if( null != fileDirectory.getParent() )
		{
			listFileInfo.add( 0, new FileInfo( "..", new File( fileDirectory.getParent() ) ) );
		}
		m_fileinfoarrayadapter = new FileInfoArrayAdapter( m_contextParent, listFileInfo );
		listview.setAdapter( m_fileinfoarrayadapter );

		AlertDialog.Builder builder = new AlertDialog.Builder( m_contextParent );
		builder.setTitle( strTitle );
		builder.setNegativeButton( "Cancel", null );
		builder.setView( listview );
		m_dialog = builder.show();
	}

	// ListView内の項目をクリックしたときの処理
	public void onItemClick( AdapterView<?> parent, View view, int position, long id )
	{
		if( null != m_dialog )
		{
			m_dialog.dismiss();
			m_dialog = null;
		}

		FileInfo fileinfo = m_fileinfoarrayadapter.getItem( position );

		if( fileinfo.getFile().isDirectory() )
		{
			show( fileinfo.getFile() );
		}
		else
		{
			// ファイルが選ばれた：リスナーのハンドラを呼び出す
			m_listener.onFileSelect( fileinfo.getFile() );
		}
	}

	// 選択したファイルの情報を取り出すためのリスナーインターフェース
	public interface OnFileSelectListener
	{
		// ファイルが選択されたときに呼び出される関数
		void onFileSelect( File file );
	}
}


