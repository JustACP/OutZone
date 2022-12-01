# OutZone
---
> 一个基于 SpringBoot 的网盘项目

0. 写在前面:

   这个项目是学完一些基本课程之后,自己构思编写的. 因此很多地方在设计之初就存在很多缺陷和问题.
   如果有时间的话可能会对重新优化.

1. 用到的技术:
    - SpringBoot / Spring Security
    - Mybatis-Plus
    - (其他小工具)

2. 预备知识:
    - 文件处理

3. 可优化部分
    - [ ] 用户判断完全交由Spring Security
    - [ ] 将与前端的id交互改为字符串
        - 目前是自己写了一个粗略的id生成策略, 使得在js精度之内
    - [ ] 视频分片处理
    - [ ] 图片预览
    - [ ] 视频和图片缩略图
        - 前端还没想好

