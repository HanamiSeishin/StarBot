<div align="center">

![logo](https://bot.starlwr.com/images/static/logo.jpg)

[![PyPI](https://img.shields.io/pypi/v/starbot-bilibili)](https://pypi.org/project/starbot-bilibili)
[![Python](https://img.shields.io/badge/python-3.10%20|%203.11-blue)](https://www.python.org)
[![License](https://img.shields.io/github/license/Starlwr/StarBot)](https://github.com/Starlwr/StarBot/blob/master/LICENSE)
[![STARS](https://img.shields.io/github/stars/Starlwr/StarBot?color=yellow&label=Stars)](https://github.com/Starlwr/StarBot/stargazers)


**一个极速，多功能的哔哩哔哩推送机器人**
</div>

## 特性

* 极速的直播推送，通过连接直播间实现
* 接近于手机端效果的绘图式动态推送
* 详细的直播数据统计
* 群内数据查询、榜单查询

## 快速开始

项目依赖于 Redis 进行持久化的直播相关数据存储，依赖于 Mirai 和 mirai-http 进行消息推送

### 魔改特性
该版本为魔改版starbot，对比主干版本，他多了些什么：
* 新增了插件重载指令，指令为 重载插件或reload_plugins，可以执行自定义命令包的指令重载，搭配[starbot-mysql-plugin](https://github.com/HanamiSeishin/starbot-mysql-plugin)插件可实现插件热加载
  **当前发现require加载的对象使用了传统import的依赖无法热更新，导致依赖报错，例如函数签名的变化，暂未想到好的解决方案，该功能仍然不完善，慎用
* 对指令进行了at对象的校验，该改动使得bot拒绝响应at其他成员、at全体和引用回复消息
* 要求指令和参数之间必须用空格分隔，搭配第2条可以极大减少指令误触现象，例如绑定，禁用，启用
* 每次动态爬取取用最新的cookie字符串，这个改动主要是适配暂未开源的B站cookie热更新插件

### 安装

```shell
pip install starbot-bilibili
```

### 启动

推送配置的 JSON 文件可使用官网的 [在线制作工具](https://bot.starlwr.com/depoly/json) 生成  
详细文档及 config 中所需的 credential 获取方式请参见 [部署文档](https://bot.starlwr.com/depoly/document)

```python
from starbot.core.bot import StarBot
from starbot.core.datasource import JsonDataSource
from starbot.utils import config

config.set_credential(sessdata="B站账号的sessdata", bili_jct="B站账号的bili_jct", buvid3="B站账号的buvid3")

datasource = JsonDataSource("推送配置.json")
bot = StarBot(datasource)
bot.run()
```

## 鸣谢

* [bilibili-api](https://github.com/MoyuScript/bilibili-api): 哔哩哔哩的 API 调用模块
* [bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect): 哔哩哔哩 API 收集整理
* [mirai](https://github.com/mamoe/mirai): 高效率 QQ 机器人支持库
* [Ariadne](https://github.com/GraiaProject/Ariadne): 一个优雅且完备的 Python QQ 自动化框架，基于 Mirai API HTTP v2
* [Fake-QQ-Chat-Window](https://github.com/Redlnn/Fake-QQ-Chat-Window): 伪 QQ 移动客户端聊天窗口，当前官网的推送配置在线预览基于此项目魔改实现
* [为本项目赞赏支持的小伙伴们](https://bot.starlwr.com/about): 名单较长，详细名单可点击链接在官网查看
* [JetBrains](https://www.jetbrains.com/?from=StarBot): 为开源项目免费提供的优秀 IDE

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg"/>](https://www.jetbrains.com/?from=StarBot)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Starlwr/StarBot&type=Date)](https://star-history.com/#Starlwr/StarBot&Date)
