package com.example.androidclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static TextView textResponse;
	private EditText editTextAddress, editTextPort;
	private Button buttonConnect;
	private String message = "Hi client!";
	private static String kq = "";
	private ClientTask myClientTask;
	private OnListener listener;
	private static boolean flag = true;
	Socket socket = null;

	public interface OnListener {
		void listener(String text);
	}

	public void addListener(OnListener listener) {
		this.listener = listener;
	}

	static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (flag) {
				kq += msg.obj.toString() + "\r\n";
				textResponse.setText(kq);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		editTextAddress = (EditText) findViewById(R.id.address);
		editTextPort = (EditText) findViewById(R.id.port);
		buttonConnect = (Button) findViewById(R.id.connect);
		textResponse = (TextView) findViewById(R.id.response);

		buttonConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myClientTask = new ClientTask(editTextAddress.getText()
						.toString(), Integer.parseInt(editTextPort.getText()
						.toString()));
				myClientTask.execute();
			}
		});

	}

	public class ClientTask extends AsyncTask<String, String, String> implements
			OnListener {

		String dstAddress;
		int dstPort;
		PrintWriter out1;
		

		ClientTask(String addr, int port) {
			dstAddress = addr;
			dstPort = port;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			try {

				socket = new Socket(dstAddress, dstPort);
				out1 = new PrintWriter(socket.getOutputStream(), true);
				//out1.print("Hello server!");
				out1.flush();

				BufferedReader in1 = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				do {
					try {
						if (!in1.ready()) {
							if (message != null) {
								MainActivity.handler.obtainMessage(0, 0, -1,
										"Server: " + message).sendToTarget();
								message = "";
							}
						}
						int num = in1.read();
						message += Character.toString((char) num);
					} catch (Exception classNot) {
					}

				} while (!message.equals("bye"));

				try {
					sendMessage("bye");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					socket.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (socket.isClosed()) {
					flag = false;
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Lỗi nhập!", Toast.LENGTH_LONG).show();
			}

			super.onPostExecute(result);
		}

		@Override
		public void listener(String text) {
			// TODO Auto-generated method stub
			sendMessage(text);
		}

		void sendMessage(String msg) {
			try {
				out1.print(msg);
				out1.flush();
				if (!msg.equals("bye"))
					MainActivity.handler.obtainMessage(0, 0, -1, "Me: " + msg)
							.sendToTarget();
				else
					MainActivity.handler.obtainMessage(0, 0, -1,
							"Ngắt kết nối!").sendToTarget();
			} catch (Exception ioException) {
				ioException.printStackTrace();
			}
		}

	}

	public void send(View v) {
		addListener(myClientTask);
		if (listener != null)
			listener.listener(((EditText) findViewById(R.id.editText1))
					.getText().toString());
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			if (listener != null)
				listener.listener("bye");
			socket.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		try {
			if (listener != null)
				listener.listener("bye");
			socket.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		super.onStop();
	}
	
	public void onClick(View v) {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(intent);
		finish();
	}

}
