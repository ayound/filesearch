# filesearch
Mac下的一个桌面文件搜索工具，可基于文件名或文件内容秒搜，支持移动存储（移动存储不连接电脑时不会删除索引）

## 做这个工具的原因：

Mac电脑磁盘容量有限，不少资料放到了移动硬盘上，只要拔掉移动硬盘，Mac就会删除相关索引，每次查资料都要插上移动硬盘，忍受缓慢的搜索过程。

所以我想做一个工具，可以秒搜文件，可以根据文件名、路径和文件内容（仅限文档）搜索文件。

索引分为内部存储索引和外部存储索引，当移动硬盘推出时，外部存储索引默认不会删除。这样就可以实现不插移动硬盘搜索文件，当需要拷贝文件时再插移动硬盘。


## 技术说明：

jdk8,使用javafx做UI，使用lucene做索引，使用tika探测文档，支持常见文档( doc, docx, ppt, pptx, xls, xlsx, pdf)的内容索引和全文检索。

## 使用方式：
1.第一次打开自动开始建索引，可以配置需要检索的目录（默认当前用户）、外置硬盘目录、不建索引的目录。

2.在搜索栏里输入关键字，回车即搜索。因为搜索用了分页，所以基本上是秒搜。我的机器上有200多万个文件，基本上可秒搜。
关键字支持多个，用空格隔开
支持文档内容搜索，用t:开头即可以根据文档内容搜索。

3.支持文件快速预览，选中某一个文件，按空格键可快速预览，第一次打开预览界面慢一点 （javafx慢），后面预览也基本上是秒开，如果遇到比较大的文档会慢一点。

4.支持排序，点击搜索结果的表头，可以根据文件名、目录或文件大小排序。

5.支持边建索引边搜索。

## 缺点：
1.因为使用的是java语言，建索引过程没那么快。
2.没有即时监控文件变化，所以新创建的文件不能马上搜出来。

## 安装说明：

1.因为没有mac的签名，请自行解决

2.如果要自行编译，请在mac + jdk 8下使用 mvn clean package命令
