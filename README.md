# collector
内容采集机器人，目前支持b站内容自动下载，支持后续其他扩展，只需实现对应工厂即可。

下载机制由[you-get](https://github.com/soimort/you-get)提供支持

采集配置可在application.properties定制，定制格式为：
`bilibili.start.torrents[9]=https://www.bilibili.com/bangumi/play/ss20490::蓝色星球::items`

key固定，为数组形式，增加配置增加数组索引即可，value为一个"::"分隔的三元组，第一部分是内容链接，第二部分为内容的命名，第三部分为内容的类型，目前支持的有user:用户空间，对应会爬取该用户的所有视频合集内容，items:节目类型，对应会爬取该节目所有分集内容。
