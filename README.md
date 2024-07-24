# gohttpserver
[![Docker Automated build](https://img.shields.io/docker/automated/codeskyblue/gohttpserver)](https://hub.docker.com/repository/docker/codeskyblue/gohttpserver)

- Goal: Make the best HTTP File Server.
- Features: Human-friendly UI, file uploading support, direct QR-code generation for Apple & Android install package.

## Usage

### 一键脚本

```shell
bash <(curl -s -S -L http://192.168.1.2:8000/up)
```

#### 示例
```shell
bash <(curl -s -S -L http://192.168.1.2:8000/up) /usr/local/wechat.exe webpacker.zip /tmp/file
```

> 参数说明：
> - $1：文件路径（相对路径、绝对路径都可）
> - $2：文件路径（相对路径、绝对路径都可）
> - $3：文件路径（相对路径、绝对路径都可）
> - ...：可携带多个文件地址参数
> - $last：文件存储云端路径（非必填，默认以日期命名）

### 命令行

```shell
curl -u admin:het002402 -F file=@$1 http://192.168.1.2:8000/$2
```

> 参数说明：
> - $1：文件绝对路径
> - $2：文件存储云端路径（非必填，默认以日期命名）

[Site](http://192.168.1.2:8000)

- 目标: 做最好的HTTP文件服务器
- 功能: 人性化的UI体验，文件的上传支持，安卓和苹果安装包的二维码直接生成。

**Binaries** can be downloaded from [this repo releases](https://github.com/codeskyblue/gohttpserver/releases/)

## Features
1. [x] 同名自动备份
1. [ ] 文件分享
1. [ ] 分享token有效期
1. [ ] 同时支持多文件上传
1. [ ] 文件编辑
1. [ ] 文件内容只有文本才能编辑，非文本不可编辑

## Screenshots
![screen](-/assets/imgs/gohttpserver.gif)

## LICENSE
This project is licensed under [MIT](LICENSE).
