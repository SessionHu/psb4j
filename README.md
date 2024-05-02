<h1 align=center>Project Simple Builder for Java</h1>

## 引言 Intro

> 本项目始于2024年1月4日晚, 由于本人 shell 不好, 及 Gradle 性能不佳, 对于 Java 项目构建存在许多不便, 故想到创建此项目.  
  项目名称由来受 `4j` 一缩略词启发, 加之本人能力不佳, 写的东西功能简易, 使用 `simple` 一词. 简称为 `psb4j`, 尽管名字听着怪怪的.  
  此外, 这个项目是我写给我自己用的, 若有人原因舍弃 Maven 和 Gradle 来用我的 psb4j, 那我不胜荣幸.  
  -- Session 于 Jan 4 2023

## 简述 Desc

- Psb4j (全称: Project Simple Builder for Java ; 内部名称: pj-sp-br-4j) 是一个命令行的跨平台高性能 Java 项目构建工具. 本工具较传统的 Maven 和 Gradle 等, 能够以更低的学习成本, 更低的资源占用, 更高的运行效率, 对 Java 项目进行快速或完整编译打包运行等操作.
- 本项目完全使用 Java 语言进行编写, 理论上能够在任何支持 JDK 的设备上运行. 若您对本项目感兴趣, 请在您的项目上使用本工具进行构建, 您还可以向您认识的朋友等进行推荐. 还有最重要的, star 并赞助.

## 使用 Usage

```text
    --version        Print version message
    --help           Print this help message
    --jar            Path of output JAR
    --manifest       Manifest for JAR
    --pwd            Set work dirctory
    --build-dirctory Set output dirctory
    --sourcepath     Where to find .java code
    --remote-lib     Remote library URL
    --extra-packin   Extra files added to JAR
    --clear          Remove files at output dirctory
```

## 开发 Dev

- 本项目为使用 `Java 8` 的**控制台**应用程序, 请确认设备使用的 JDK 版本

## 许可 Licens

- 本项目使用 `MIT License` 进行分发使用
