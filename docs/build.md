How to build this package with native binary?

# Windows
- Install
  [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html),
  [CMake](https://cmake.org/download/),
  [MSYS](https://osdn.net/projects/mingw/releases/),
  [MinGW-w64](https://sourceforge.net/projects/mingw-w64/)

  These package are used when configuring MinGW installer.

  - MSYS
    - Base
      - msys-base-bin
      - msys-bash-bin
      - msys-coreutils-bin
      - msys-grep-bin
      - msys-m4-bin
      - msys-make-bin
      - msys-sed-bin
      - msys-texinfo-bin
    - System
      - msys-autoconf-bin
      - msys-automake-bin
      - msys-binutils-bin??
      - msys-diffutils-bin??
      - msys-dos2unix-bin??
      - msys-findutils-bin??
      - msys-gawk-bin??
      - msys-gzip-bin??
      - msys-libtool-bin
      - msys-tar-bin??
      - msys-xz-bin??

  Install directory should not have a space in it,
  MinGW-w64 installed to `C:\Program Files` or `C:\Program Files (x86)` by default.
  If you have already installed them into such directory, yout can create a junction to them.
  ```cmd
  mklink /J C:\MinGW64 "C:\Program Files\mingw-w64\x86_64-8.1.0-posix-seh-rt_v6-rev0\mingw64"
  mklink /J C:\MinGW32 "C:\Program Files (x86)\mingw-w64\i686-8.1.0-posix-dwarf-rt_v6-rev0\mingw32"
  set MINGW32_HOME=C:\MinGW32
  set MINGW64_HOME=C:\MinGW64
  ```

  Qutation mark followed package names means we are not sure
  if this package is required, you'd better install all of them.

  Install MSYS packages, do `NOT` install MinGW packages.
  On appveyor ci, MSYS autoconf is not installed, We had to use MINGW autoconf instead.

  Install MinGW-w64 x86 version if you want to build binary for Windows x86.

- Configure environment variables as descripted in [appveyor.yml](../appveyor.yml).
- Execute script `mvnw -Pnative package` with MSYS bash, bash in git `git-bash` may not work as expect.

# Linux, Mac OS X:
- Install required packages.
- Execute build script `./mvnw -Pnative package`.

Cross build libs for windows on Linux, Mac OS X:

[nb.xml](../libjnc/nb.yml) shows a how to open native in NetBeans IDE,
it contains scripts how to perform a cross-build.
