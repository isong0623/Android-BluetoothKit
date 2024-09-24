package com.dreaming.bluetooth.framework.channel;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.dreaming.bluetooth.framework.BluetoothContext;
import com.dreaming.bluetooth.framework.channel.packet.impl.DataPacket;
import com.dreaming.bluetooth.framework.channel.packet.wrapper.Packet;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyBulk;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyInterceptor;
import com.dreaming.bluetooth.framework.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public abstract class Channel implements IChannel, ProxyInterceptor {
	private static BluetoothLogger logger = new BluetoothLogger(Channel.class);

	private static final long TIMEOUT = 5000;
	private static final int MSG_WRITE_CALLBACK = 1;
	private static final String TIMER_EXCEPTION = "exception";

	private ChannelState mCurrentState = ChannelState.IDLE;

	private byte[] mBytesToWrite;

	/**
	 * 收到的包
	 */
	private SparseArray<Packet> mPacketRecv;
	private int mCurrentSync;

	/**
	 * 发端要记录总字节数
	 */
	private int mTotalBytes;

	/**
	 * 收端要记录总帧数
	 */
	private int mFrameCount;

	private ChannelCallback mChannelCallback;

	private Handler mWorkerHandler;

	private IChannel mChannel;

	private int mLastSync;

	public Channel() {
		mPacketRecv = new SparseArray<>();

		mChannel = ProxyUtils.getProxy(mChannelImpl, this);

		HandlerThread thread = new HandlerThread(getClass().getSimpleName());
		thread.start();

		mWorkerHandler = new Handler(thread.getLooper(), mCallback);
	}

	@Override
	public final void onRead(final byte[] bytes) {
		mChannel.onRead(bytes);
	}

	@Override
	public final void send(byte[] value, ChannelCallback callback) {
		logger.e(">>> send %s", new String(value));
		mChannel.send(value, callback);
	}

	private final IChannelStateHandler mSyncPacketHandler = new IChannelStateHandler() {

		@Override
		public void handleState(Object... args) {
			assertRuntime(false);

			DataPacket dataPacket = (DataPacket) args[0];

			if (dataPacket.getSeq() != mCurrentSync) {
				logger.w("sync packet not matched, request #%d but resolve #%d !!", dataPacket.getSeq(), mCurrentSync);
				return;
			}

			if (!onDataPacketRecvd(dataPacket)) {
				logger.w("sync packet repeated!!");
				return;
			}

			mLastSync = mCurrentSync;
			mCurrentSync = 0;

			startSyncPacket();
		}
	};

	/**
	 * 收到数据包的回调
	 */
	private final IChannelStateHandler mRecvDataHandler = new IChannelStateHandler() {

		@Override
		public void handleState(Object... args) {
			assertRuntime(false);
			DataPacket dataPacket = (DataPacket) args[0];

			if (!onDataPacketRecvd(dataPacket)) {
				logger.w("dataPacket repeated!!");
				return;
			}

			if (dataPacket.getSeq() == mFrameCount) {
				// 如果最后一帧收到了，说明对端发送完毕了
				startSyncPacket();
			} else {
				startTimer(TIMEOUT, new Timer.TimerCallback("WaitData") {
					@Override
					public void onTimerCallback() {
						startSyncPacket();
					}
				});
			}
		}
	};

	/**
	 * 收到流控包的回调
	 */
	private final IChannelStateHandler mRecvCTRHandler = new IChannelStateHandler() {
		@Override
		public void handleState(Object... args) {
			assertRuntime(false);

			//TODO
		}
	};

	private final IChannelStateHandler mWaitStartACKHandler = new IChannelStateHandler() {
		@Override
		public void handleState(Object... args) {
			assertRuntime(false);
			setCurrentState(ChannelState.WAIT_START_ACK);
			startTimer();
		}
	};

	private final Timer.TimerCallback mTimeoutHandler = new Timer.TimerCallback(getClass().getSimpleName()) {

		@Override
		public void onTimerCallback() {
			assertRuntime(false);
			onSendCallback(ChanelCallbackCode.Timeout);
			resetChannelStatus();
		}
	};

	/**
	 * 收到ACK包的回调
	 */
	private final IChannelStateHandler mRecvACKHandler = new IChannelStateHandler() {
		@Override
		public void handleState(Object... args) {
			assertRuntime(false);

			//TODO
		}
	};

	private final ChannelStateBlock[] STATE_MACHINE = {
		new ChannelStateBlock(ChannelState.READY         , ChannelEvent.SEND_CTR , mWaitStartACKHandler),
		new ChannelStateBlock(ChannelState.WAIT_START_ACK, ChannelEvent.RECV_ACK , mRecvACKHandler     ),
		new ChannelStateBlock(ChannelState.SYNC          , ChannelEvent.RECV_ACK , mRecvACKHandler     ),
		new ChannelStateBlock(ChannelState.IDLE          , ChannelEvent.RECV_CTR , mRecvCTRHandler     ),
		new ChannelStateBlock(ChannelState.READING       , ChannelEvent.RECV_DATA, mRecvDataHandler    ),
		new ChannelStateBlock(ChannelState.SYNC_ACK      , ChannelEvent.RECV_DATA, mSyncPacketHandler  ),
	};

	/**
	 * 这个函数主要是为了记录写出去的所有包
	 * 执行写要放在UI线程
	 */
	private void performWrite(Packet packet, final ChannelCallback callback) {
		assertRuntime(false);

		if (callback == null) {
			throw new NullPointerException("callback can't be null");
		}

		// 此处为防止底层写没回调，故抛异常提示
		if (!isTimerOn()) {
			startExceptionTimer();
		}

		//TODO
		final byte[] bytes = new byte[]{};

		BluetoothContext.post(new Runnable() {

			@Override
			public void run() {
				write(bytes, new WriteCallback(callback));
			}
		});
	}

	private class WriteCallback implements ChannelCallback {

		ChannelCallback callback;

		WriteCallback(ChannelCallback callback) {
			this.callback = callback;
		}

		@Override
		public void onCallback(ChanelCallbackCode code) {
			if (isExceptionTimerOn()) {
				stopTimer();
			}

			mWorkerHandler.obtainMessage(MSG_WRITE_CALLBACK, code.code, 0, callback).sendToTarget();
		}
	}

	private void sendStartFlowPacket() {
		assertRuntime(false);

		//TODO
	}

	private void onSendCallback(final ChanelCallbackCode code) {
		assertRuntime(false);

		logger.v("%s: onSendCallback-> code = %d", getLogTag(), code);

		if (mChannelCallback != null) {
			mChannelCallback.onCallback(code);
		}
	}

	private boolean onDataPacketRecvd(DataPacket packet) {
		assertRuntime(false);

		//TODO
		stopTimer();

		return true;
	}

	/**
	 * 认为对端发送完毕了，可以开始同步了
	 */
	private void startSyncPacket() {
		assertRuntime(false);

		logger.v("%s: startSyncPacket",getLogTag());

		startTimer();
		setCurrentState(ChannelState.SYNC);

		if (!syncLostPacket()) {
			// 所有包都同步完了

			final byte[] bytes = getTotalRecvdBytes();

			//TODO
		} else {
			// 什么都不做
		}
	}

	private void dispatchOnReceive(final byte[] bytes) {
		logger.e(">>> receive: %s", new String(bytes));
		BluetoothContext.post(new RecvCallback(bytes));
	}

	private class RecvCallback implements Runnable {

		private byte[] bytes;

		RecvCallback(byte[] bytes) {
			this.bytes = bytes;
		}

		@Override
		public void run() {
			onRecv(bytes);
		}
	}

	private byte[] getTotalRecvdBytes() {
		assertRuntime(false);

		if (mPacketRecv.size() != mFrameCount) {
			throw new IllegalStateException();
		}

		logger.v("%s: totalBytes = %d", getLogTag(), mTotalBytes);

		ByteBuffer buffer = ByteBuffer.allocate(mTotalBytes);
        //TODO 不合理内存开销太大

		return buffer.array();
	}

	private boolean syncLostPacket() {
		assertRuntime(false);

		logger.v("$s: syncLostPacket", getLogTag());

        //todo

		return false;
	}

	private void resetChannelStatus() {
		assertRuntime(false);

		logger.v("%s: resetChannelStatus", getLogTag());

		stopTimer();
		setCurrentState(ChannelState.IDLE);
		mBytesToWrite = null;
		mFrameCount = 0;
		mChannelCallback = null;
		mPacketRecv.clear();
		mCurrentSync = 0;
		mLastSync = 0;
		mTotalBytes = 0;
	}

	/**
	 * @param index  包的索引，从0开始
	 * @param looped 是否要循环发送下一个包
	 */
	private void sendDataPacket(final int index, final boolean looped) {
		assertRuntime(false);

		if (index >= mFrameCount) {
			logger.v("%s: all packets sended!!", getLogTag());
			setCurrentState(ChannelState.SYNC);
			startTimer(TIMEOUT * 3);
			return;
		}

		logger.v("%s: index = %d, looped = %b", getLogTag(), index + 1, looped);

		int start = index * 18;
		int end = Math.min(mBytesToWrite.length, (index + 1) * 18); // 开区间

        //TODO
//		DataPacket dataPacket = new DataPacket(index + 1, mBytesToWrite, start, end);
//
//		performWrite(dataPacket, new ChannelCallback() {
//			@Override
//			public void onCallback(ChanelCallbackCode code) {
//				assertRuntime(false);
//				if (code != ChanelCallbackCode.Success) {
//					logger.w(">>> packet %d write failed", index);
//				}
//				if (looped) {
//					sendDataPacket(index + 1, looped);
//				}
//			}
//
//		});
	}

	private void setCurrentState(ChannelState state) {
		assertRuntime(false);
		logger.v("%s: state = %s", getLogTag(), state);
		mCurrentState = state;
	}

	private void onPostState(ChannelEvent event, Object... args) {
		assertRuntime(false);

		logger.v("%s: state = %s, event = %s",
				getLogTag(), mCurrentState, event);

		for (ChannelStateBlock block : STATE_MACHINE) {
			if (block.state == mCurrentState && block.event == event) {
				block.handler.handleState(args);
				break;
			}
		}
	}

	private void assertRuntime(boolean sync) {
		Looper target = sync ? Looper.getMainLooper() : mWorkerHandler.getLooper();
		if (Looper.myLooper() != target) {
			throw new RuntimeException();
		}
	}

	private void performOnRead(byte[] bytes) {
		assertRuntime(false);

		Packet packet = Packet.parse(bytes);

		logger.w("%s: %s", getLogTag(), packet);
        //TODO
		switch (packet.getType()) {
            case Send:
				onPostState(ChannelEvent.RECV_ACK, packet);
				break;

            case Data1:
				onPostState(ChannelEvent.RECV_DATA, packet);
				break;

            case Data2:
				onPostState(ChannelEvent.RECV_CTR, packet);
				break;

			default:
				// 非法的包直接忽略
				break;
		}
	}

	private final IChannel mChannelImpl = new IChannel() {

		@Override
		public void write(byte[] bytes, ChannelCallback callback) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void onRead(byte[] bytes) {
			performOnRead(bytes);
		}

		@Override
		public void onRecv(byte[] bytes) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void send(byte[] value, ChannelCallback callback) {
			performSend(value, callback);
		}
	};

	private void performSend(byte[] value, ChannelCallback callback) {
		assertRuntime(false);

		if (mCurrentState != ChannelState.IDLE) {
			callback.onCallback(ChanelCallbackCode.Busy);
			return;
		}

		mCurrentState = ChannelState.READY;
		mChannelCallback = ProxyUtils.getUIProxy(callback);

		mTotalBytes = value.length;
		mFrameCount = getFrameCount(mTotalBytes);

		logger.v("%s: totalBytes = %d, frameCount = %d", getLogTag(), mTotalBytes, mFrameCount);

		mBytesToWrite = Arrays.copyOf(value, value.length + 2);
//		byte[] crc = CRC16.get(value);
//		System.arraycopy(crc, 0, mBytesToWrite, value.length, 2);
        //TODO
		sendStartFlowPacket();
	}

	@Override
	public boolean onIntercept(Object object, Method method, Object[] args) {
		mWorkerHandler.obtainMessage(0, new ProxyBulk(object, method, args)).sendToTarget();
		return true;
	}

	private final Handler.Callback mCallback = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_WRITE_CALLBACK:
					ChannelCallback callback = (ChannelCallback) msg.obj;
					callback.onCallback(ChanelCallbackCode.parse(msg.arg1));
					break;

				default:
					ProxyBulk.safeInvoke(msg.obj);
					break;
			}

			return false;
		}
	};

	private String getLogTag() {
		return BluetoothContext.getCurrentMethodName();
	}

	/**
	 * 末尾追加两个字节的crc，每包发18个字节
	 *
	 * @return 分包数
	 */
	private int getFrameCount(int totalBytes) {
		int total = totalBytes + 2;
		return 1 + (total - 1) / 18;
	}

	private void startTimer() {
		startTimer(TIMEOUT);
	}

	private void startExceptionTimer() {
		startTimer(TIMEOUT, new Timer.TimerCallback(TIMER_EXCEPTION) {
			@Override
			public void onTimerCallback() throws TimeoutException {
				throw new TimeoutException();
			}
		});
	}

	private void startTimer(long duration) {
		startTimer(duration, mTimeoutHandler);
	}

	private void startTimer(long duration, Timer.TimerCallback callback) {
		logger.v("%s: duration = %d", getLogTag(), duration);
		Timer.start(callback, duration);
	}

	private void stopTimer() {
		logger.v("%s: stopTimer",getLogTag());
		Timer.stop();
	}

	private boolean isTimerOn() {
		return Timer.isRunning();
	}

	private boolean isExceptionTimerOn() {
		return TIMER_EXCEPTION.equals(Timer.getName());
	}
}
