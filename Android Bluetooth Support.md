# Android 1.0
无法查询到相关文档

# Android 2.0
* 开启/关闭蓝牙
* 设备和服务发现
* 使用 RFCOMM 连接到远程设备并发送/接收数据
* 公布 RFCOMM 服务并监听传入的 RFCOMM 连接

# Android 2.3.3 
添加了对蓝牙非安全套接字连接的平台和 API 支持。
这样，应用就可以与可能未提供身份验证界面的简单设备进行通信。
如需了解详情，请参阅 createInsecureRfcommSocketToServiceRecord(java.util.UUID) 和 
listenUsingInsecureRfcommWithServiceRecord(java.lang.String, java.util.UUID)。

#Android-3.0
蓝牙 A2DP 和耳机 API
Android 现在包含一些 API，供应用验证已连接的蓝牙 A2DP 的状态 耳机配置文件设备。
例如，应用可以识别蓝牙耳机何时 以便聆听音乐，并根据需要通知用户。
应用还可以 针对供应商特定 AT 命令进行广播，并通知用户连接状态 
例如当所连接设备的电池电量不足时。

您可以通过使用 A2DP 或 HEADSET 调用 getProfileProxy() 来初始化相应的 BluetoothProfile。 
配置文件常量和一个用于接收的 BluetoothProfile.ServiceListener 回调。

#Android-4.0
蓝牙健康设备
Android 现在支持蓝牙健康配置文件设备，因此您可以创建使用 通过蓝牙与支持蓝牙功能的健康设备（如心率监测器）进行通信， 血糖仪、体温计和体重秤

与常规耳机和采用 A2DP 配置文件的设备类似，您必须使用 BluetoothProfile.ServiceListener 和 HEALTH 配置文件类型调用 getProfileProxy()，才能与配置文件建立连接 代理对象。

获取 Health Profile 代理（BluetoothHealth 对象），连接配对健康设备并与之通信涉及以下新功能： Bluetooth 类：
BluetoothHealthCallback：您必须扩展此类并实现 回调方法，以接收有关应用注册状态更改的更新信息， 蓝牙通道状态。
BluetoothHealthAppConfiguration：在回调 BluetoothHealthCallback 期间，您会收到此对象的一个实例，
提供有关可用蓝牙健康设备的配置信息，您必须使用该信息 执行各种操作，
例如通过 BluetoothHealth API 启动和终止连接。

如需详细了解如何使用蓝牙健康配置文件，请参阅 BluetoothHealth 的文档。

# Android-4.0.3
新的公共方法 fetchUuidsWithSdp() 和 getUuids() 允许应用确定远程设备支持的功能 (UUID)。
对于 fetchUuidsWithSdp()，系统在远程设备上执行服务发现以获取支持的 UUID，然后在 ACTION_UUID intent 中广播结果。

# Android-4.1
Android Beam 支持蓝牙传输

# Android 4.4 (API 19)
支持两种新的蓝牙配置文件，让应用支持更多类型的低功耗媒体交互。
* Bluetooth HID over GATT (HOGP) 让应用能够以低延迟的方式将应用与低功耗外围设备（如鼠标、操纵杆和键盘）连接。
* 蓝牙 MAP 可让您的应用与附近的设备（例如可免触摸使用的汽车终端或其他移动设备）交换消息。

作为对蓝牙 AVRCP 1.3 的扩展，用户现在可以通过蓝牙设备设置系统的绝对音量。

Google 推出的 Bluedroid 蓝牙堆栈和 Android 4.2 中的 Broadcom 为 HOGP、MAP 和 AVRCP 的平台支持构建而成。

Nexus 设备和其他提供兼容蓝牙功能的 Android 兼容设备可直接提供支持。

蓝牙打印

# Android 5.0 (API 21)
添加了新的 API，可让应用通过蓝牙低功耗 (BLE) 执行并发操作，同时实现扫描（中心模式）和通告（外围设备模式）。

Android 设备现在可以 充当蓝牙 LE 外围设备，应用可以使用此功能让附近的设备能够检测到它们。
例如，你可以构建应用 可让设备用作计步器或健康监测器， 通过另一个蓝牙 LE 设备传输数据。

新的 android.bluetooth.le API 可让您的应用广播 查看广告、扫描回复以及通过附近的蓝牙建立连接 LE 设备。
要使用新的广告和扫描功能，请声明 BLUETOOTH_ADMIN 权限。
当用户从 Play 商店更新或下载您的应用时， 系统会要求他们向您的应用授予以下权限： “Bluetooth connection information: 允许应用控制蓝牙， 包括向附近的蓝牙设备广播或获取其相关信息。"

开始蓝牙 LE 广播，以便其他设备可以发现 调用 startAdvertising() 并传入 AdvertiseCallback 类。
回调对象 收到通告操作成功或失败的报告。

Android 5.0 引入了 ScanFilter 类，因此 您的应用只能扫描 它感兴趣的特定设备类型。
开始扫描蓝牙 LE 设备，调用 startScan() 并传入过滤条件列表。
在方法调用中，您还必须提供一个 ScanCallback 的实现来报告 发现蓝牙 LE 通告。

# Android 6.0 (API 23)
如需通过蓝牙扫描获取附近外部设备的硬件标识符，请按以下步骤操作： 
您的应用现在必须拥有 ACCESS_FINE_LOCATION 或 ACCESS_COARSE_LOCATION 权限：

BluetoothDevice.ACTION_FOUND
BluetoothLeScanner.startScan()

蓝牙触控笔支持
此版本改进了对用户使用蓝牙触控笔输入的支持。
用户可以配对 并将兼容的蓝牙触控笔与其手机或平板电脑连接。
连接后，将 触摸屏的信息与来自触控笔的压力和按钮信息融合在一起， 
能够提供比单纯的触摸屏更丰富的表达范围。
您的应用可以监听 通过注册设备，按下触控笔按钮并执行辅助操作 
View.OnContextClickListener和 
您的 activity 中有 GestureDetector.OnContextClickListener 对象。

使用 MotionEvent 方法和常量检测触控笔按钮 互动：
如果用户使用带按钮的触控笔触按应用屏幕， getTooltype() 方法返回 TOOL_TYPE_STYLUS。
对于以 Android 6.0（API 级别 23）为目标平台的应用， getButtonState() 方法返回 BUTTON_STYLUS_PRIMARY 触控笔主键。
如果触控笔有辅助按键，系统会返回相同的方法 BUTTON_STYLUS_SECONDARY。
如果用户按 则此方法会同时返回通过 OR 运算得到的两个值 (BUTTON_STYLUS_PRIMARY|BUTTON_STYLUS_SECONDARY)。
对于以较低平台版本为目标平台的应用， getButtonState() 方法返回 BUTTON_SECONDARY（用于按下触控笔主按钮）， BUTTON_TERTIARY（按下触控笔辅助按钮），或两者都使用。

改进的蓝牙低功耗扫描
如果您的应用执行低功耗蓝牙扫描，请使用新的 setCallbackType() 方法指定您希望系统在首次找到回调时通知回调，
或在 与设置的 ScanFilter 匹配的通告包。这个 扫描方法比以前的平台版本中提供的方法更加节能。

# Android 8.0（API 26）
通过添加以下 功能：

支持 AVRCP 1.4 标准，该标准支持歌曲库浏览。
支持蓝牙低功耗 (BLE) 5.0 标准。
将 Sony LDAC 编解码器集成到蓝牙堆叠中。
配套设备配对
Android 8.0（API 级别 26）提供的 API 可让您自定义尝试与配套设备配对时出现的配对请求对话框蓝牙、BLE 和 Wi-Fi。

Android 8.0（API 级别 26）对 ScanRecord.getBytes() 方法会检索以下内容：
getBytes() 方法 接收的字节数。因此，应用不应依赖于 返回的字节数下限或上限。相反，他们应评估 所得数组的长度。
兼容蓝牙 5 的设备返回的数据长度可能会超出 长度上限为约 60 个字节。
如果远程设备不提供扫描响应，则少于 60 个字节 可能也会返回

# Android 10 (API 29)
蓝牙 LE 连接导向型频道 (CoC)
Android 10 可让您的应用使用 BLE CoC 连接来传输较大的数据 并在两个 BLE 设备之间传输音频流。此接口抽象化处理了蓝牙和 连接机制来简化实现。

# Android 11 (API 31)
接触史通知
Android 11 在更新平台时考虑了接触史通知系统。
用户现已可在 Android 11 上运行接触史通知应用，且无需开启设备位置信息设置。
接触史通知系统的设计使得使用该系统的应用无法通过蓝牙扫描推断设备所处的位置，因此，此例外情况仅适用于接触史通知系统。

为保护用户的隐私，所有其他应用仍无法执行蓝牙扫描，除非用户已开启设备位置信息设置且已授予相应应用位置权限。
世界各地的公共卫生机构都在开发使用暴露通知系统的应用，以帮助开展接触者追踪工作。
公共卫生当局能够快速通知可能接触过 COVID-19 感染者的人，包括那些可能不直接认识的人。
在 Android 上，您可以在 Google Play 上找到您所在地区的应用（如果有）。
如果所在州已提供暴露通知，Android 用户还将收到来自 Play Store 的通知。

# Android 12 (API 32)
引入了一些新权限，可使应用扫描附近的蓝牙设备，而无需请求位置信息权限。
Android 12 引入了 BLUETOOTH_SCAN、BLUETOOTH_ADVERTISE 和 BLUETOOTH_CONNECT 权限。
这些权限可让以 Android 12 为目标平台的应用更轻松地与蓝牙设备互动，尤其是不需要访问设备位置信息的应用。

注意：配套设备管理器提供了一种更精简的方法来连接到配套设备。系统代表您的应用提供配对界面。
如果您希望更好地控制配对和连接体验，请使用 Android 12 中引入的蓝牙权限。
更新应用的蓝牙权限声明
为了让您的设备做好准备以 Android 12 或更高版本为目标平台，请更新应用的逻辑。请声明一组更现代的蓝牙权限，而不是声明一组旧版蓝牙权限。

# Android 13 (API 33)
蓝牙 LE 音频
低功耗 (LE) 音频是一种无线音频，旨在取代传统蓝牙并支持特定的应用场景和连接拓扑。
通过该技术，用户能够与朋友和家人分享音频内容以及播放音频给他们听，也可以订阅信息、娱乐或无障碍用途的公共广播内容。 
这项新技术可以确保用户接收到高保真度的音频，而不必牺牲电池续航时间，并且还可以在不同使用情形之间无缝切换，这是传统蓝牙技术无法实现的。
从 Android 13 开始， 系统包括对 LE 音频的内置支持，因此开发者会收到 在兼容的设备上免费下载这些功能。

# Android 14 (API 34)
对于第一个请求 MTU 的 GATT 客户端，MTU 设置为 517
Android 蓝牙堆栈更严格地遵循蓝牙核心规范的第 5.2 版，{@link https://www.bluetooth.com/wp-content/uploads/2020/01/Bluetooth_5.2_Feature_Overview.pdf}
当第一个 GATT 客户端使用 BluetoothGatt#requestMtu(int) API 请求 MTU 时，
请求将 BLE ATT MTU 设置为 517 个字节，并忽略该 ACL 连接上的所有后续 MTU 请求。

注意：除非外围设备未正确处理 MTU 协商并接受任何 MTU 大小，即使它不支持 MTU 大小，否则此更改不会产生影响。
在这种情况下，当您的应用从 Android 14 设备发送大量数据时，可能会导致问题。
为了解决这一变化并使您的应用程序更加可靠，请考虑以下选项：

您的外围设备应使用外围设备可以容纳的合理值来响应 Android 设备的 MTU 请求。
最终协商的值将是 Android 请求的值和远程提供的值的最小值（例如，min（517， remoteMtu））
实施此修复可能需要外围设备的固件更新
或者，根据外围设备的已知支持值与收到的 MTU 更改之间的最小值来限制 GATT 特征写入
提醒您应该将标头支持的大小减少 5 个字节
例如：arrayMaxLength = min（SUPPORTED_MTU， GATT_MAX_ATTR_LEN（517）） - 5

# Android 15 (API 35)
Android 15 增加了可改进用户无障碍功能的功能。
改进盲文，在 Android 15 中，TalkBack 可以支持通过 USB 和安全蓝牙使用 HID 标准的盲文显示屏。
此标准与鼠标和键盘使用的标准非常相似，有助于 Android 随着时间的推移而支持更广泛的盲文显示屏。
