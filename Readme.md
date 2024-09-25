# Bluetooth Framework
## 这个项目的意义：
屏蔽内部实现细节，让开发者专注于开发依赖蓝牙的应用

致力于实现一个包含大部分通用蓝牙框架

## 感谢
本项目基于 https://github.com/dingjikerbo/Android-BluetoothKit 二次开发

## dev
* 1、在原项目基础上变更包名
* 2、此项目名以后为Bluetooth-Framework
* 3、将Constants中易滥用的部分装换为相应的枚举类
* 4、删除BluetoothLog
* 5、新增BluetoothLogger，支持日志，支持屏蔽某些类及类内tag的日志，支持全局日志等级屏蔽，类内等级屏蔽
* 6、重构Chanel，需实现TCP级别的Chanel
* 7、新增权限管理器BleConnectAuthorizer，支持自定义权限请求，开发者无需关心Bluetooth运行过程中需要使用的权限