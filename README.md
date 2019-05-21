# Java Native Call
[![Build Status](https://travis-ci.org/java-native-call/jnc-build.svg?branch=master)](https://travis-ci.org/java-native-call/jnc-build)
[![Build status](https://ci.appveyor.com/api/projects/status/github/java-native-call/jnc-build?svg=true)](https://ci.appveyor.com/project/zhanhb/jnc-build)

Origin design of this project is for the [online judge system](https://github.com/zjnu-acm/judge), who's kernel requires native support.

Why not [jna](https://github.com/java-native-access/jna) or [jnr](https://github.com/jnr/jnr-ffi)?
- There is no type conversion for primary types in jna.
- Both jna or jnr will leak memory when a project reloaded in a web container such as tomcat.
- `struct` align in jnr might be incorrect, `union` offset, so Method `Struct`.`size`() results in incorrect [jnr-ffi#146](https://github.com/jnr/jnr-ffi/issues/146)
- jnr may cause `VerifyVerror` when asm is enabled, but asm is enabled by default. [jnr-ffi#150](https://github.com/jnr/jnr-ffi/issues/150).
- type long is ambiguous in jnr. [jnr-ffi#151](https://github.com/jnr/jnr-ffi/issues/151).
- Boolean conversion in jnr is last bit, rather than test equals to zero([jnr-ffi#123](https://github.com/jnr/jnr-ffi/issues/123)). I raised a PR [jnr-ffi#153](https://github.com/jnr/jnr-ffi/pull/153/files), but won't be applied for historical reason.

Purpose of this project is to call native got correct results and no memory issue. By to now, this project's performance worse and fewer function support than jna and jnr, but more secure than those.
