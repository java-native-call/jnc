# Java Native Call
[![Build Status](https://travis-ci.org/java-native-call/jnc-build.svg?branch=master)](https://travis-ci.org/java-native-call/jnc-build)
[![Build status](https://ci.appveyor.com/api/projects/status/github/java-native-call/jnc-build?svg=true)](https://ci.appveyor.com/project/zhanhb/jnc-build)

Origin design of this project is for the [online judge system](https://github.com/zjnu-acm/judge), who's kernel requires native support.
Why not jna or jnr?
Jna or jnr will leak memory when a project reloaded in a web container such as tomcat. And something in jnr is incorrect such as `struct` align, `union` offset, and also jnr may cause VerifyVerror when asm is enabled.
Purpose of this project is to call native got correct results and no memory issue. By to now, this project's performance worse and fewer function support than jna and jnr, but more secure than those.
