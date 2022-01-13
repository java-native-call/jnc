#!/usr/bin/env bash

BLUE='\e[34m'
RED='\e[31m'
RESET='\e[m'

info() {
  printf "[${BLUE}INFO${RESET}] %s\n" "$*"
}

error() {
  printf "[${RED}ERROR${RESET}] ${RED}%s${RESET}\n" "$*" >&2
  return 1
}

to_arch() {
  sed -e 's/^[Ii][3-6]86$/i386/;s/^[Xx]86$/i386/;s/^[Xx]\(86[-_]\)\{0,1\}64$/x86_64/;s/^[Aa][Mm][Dd]64$$/x86_64/'
}

add_native() {
  native=true
  packages=(cmake libtool texinfo)
  maven_extra_config+=(-Pnative)
}

guess_os_group() {
  if command -v apt-get >/dev/null 2>&1; then
    os_group=debian
  elif command -v yum >/dev/null 2>&1; then
    os_group=rhel
  else
    error Package manager not found
  fi
}

debian_pkg_manager() {
  PKG_UPDATE=(sudo apt-get update -y)
  PKG_QUERY_INSTALLED=(dkpg -l)
  # -f fix-broken attempt to correct a system with broken dependencies in place
  PKG_ADD=(sudo apt-get -f -y install)
}

debian_cxx_dep() {
  packages+=(automake gcc g++)
  if "$build_x86_on_x64"; then packages+=(g++-multilib); fi
}

rhel_pkg_manager() {
  # -y assume yes
  PKG_ADD=(sudo yum install -y)
  PKG_QUERY_INSTALLED=(yum list installed)
}

rhel_cxx_dep() {
  packages+=(automake gcc gcc-c++)
  if "$build_x86_on_x64"; then
    packages+=(glibc-devel.i686 libgcc.i686 libstdc++-devel.i686 ncurses-devel.i686)
  fi
}

oraclejdk8() {
  local JDK_ARCH
  case "$ARCH" in
  i386) JDK_ARCH=i586 ;;
  x86_64) JDK_ARCH=x64 ;;
  *) JDK_ARCH="$ARCH" ;;
  esac
  JDK_DOWNLOAD_URL="https://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-${JDK_ARCH}.tar.gz"
  JAVA_HOME=~/jdk1.8.0_131-$JDK_ARCH
  export JAVA_HOME
}

debian_openjdk8() {
  local JDK_ARCH="$ARCH"
  if [ "$ARCH" = x86_64 ]; then JDK_ARCH="amd64"; fi
  JDK_INSTALL_NAME="openjdk-8-jdk:$JDK_ARCH"
  export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-$JDK_ARCH"
}

rhel_openjdk8() {
  local JDK_ARCH="$ARCH"
  if [ "$ARCH" = i386 ]; then JDK_ARCH="i686"; fi
  JDK_INSTALL_NAME="java-1.8.0-openjdk-devel.$JDK_ARCH"
  JAVA_HOME="$(cd /usr/lib/jvm/java-1.8.0-openjdk-1.8*."$ARCH" && pwd)"
  export JAVA_HOME
}

# These environment variables will be loaded:
#   ARCH BUILD_NATIVE JDK
init_env() {
  local OS HOST_ARCH
  OS="$(uname -s)"
  HOST_ARCH="$(uname -m | to_arch)"

  PKG_UPDATE=()
  PKG_ADD=()
  PKG_QUERY_INSTALLED=()
  maven_extra_config=()
  build_x86_on_x64=false

  if [ -z "$ARCH" ]; then
    ARCH="$HOST_ARCH"
  else
    ARCH="$(printf '%s\n' "$ARCH" | to_arch)"
    if [ "$ARCH" = i386 ] && [ "$HOST_ARCH" = x86_64 ]; then
      build_x86_on_x64=true
    fi
  fi

  native=false
  packages=()

  if [ -n "$BUILD_NATIVE" ]; then
    add_native
  fi

  windows=false

  case "$OS" in
  CYGWIN* | MINGW* | MSYS*)
    PKG_ADD=(echo)
    windows=true
    ;;
  Linux)
    guess_os_group
    "${os_group}_pkg_manager"
    if "$native"; then "${os_group}_cxx_dep"; fi
    case "$JDK" in
    oraclejdk8) oraclejdk8 ;;
    openjdk8)
      "${os_group}_openjdk8"
      packages+=("$JDK_INSTALL_NAME")
      ;;
    *)
      # use default jdk
      ;;
    esac
    ;;
  Darwin)
    pkg_query() {
      brew list | grep -q "^$1$"
    }
    PKG_ADD=(brew install)
    PKG_QUERY_INSTALLED=(pkg_query)
    if "$native"; then
      packages+=(automake coreutils gnu-sed make)
    fi
    ;;
  FreeBSD)
    PKG_ADD=(sudo pkg install -fy)
    PKG_QUERY_INSTALLED=(pkg info)
    if "$native"; then
      packages+=(automake gmake)
    fi
    packages+=(openjdk8)
    ;;
  OpenBSD)
    pkg_query() {
      pkg_info | grep -qF "$1"
    }
    PKG_ADD=(sudo pkg_add -I)
    #PKG_QUERY_INSTALLED=(pkg_query)
    if "$native"; then
      local autoconf automake
      autoconf="$(pkg_info -Iq autoconf-- | tail -1)"
      automake="$(pkg_info -Iq automake-- | grep -v '^automake-1\.[0-9]\.' | tail -1)"
      packages+=("$autoconf" "$automake" gmake)
      AUTOCONF_VERSION="$(printf "%s\n" "$autoconf" | sed 's/^autoconf-\([0-9]\{1,\}\.[0-9]\{1,\}\).*$/\1/p;d')"
      AUTOMAKE_VERSION="$(printf "%s\n" "$automake" | sed 's/^automake-\([0-9]\{1,\}\.[0-9]\{1,\}\).*$/\1/p;d')"
      export AUTOCONF_VERSION AUTOMAKE_VERSION
    fi
    packages+=(jdk%1.8)
    export JAVA_HOME="${JAVA_HOME:-/usr/local/jdk-1.8.0}"
    ;;
  *) error Platform not support ;;
  esac
  [ $# -eq 0 ] || "$@"
}

dependency() {
  (
    if [ -n "$JDK_DOWNLOAD_URL" ] && ! [ -e "$JAVA_HOME" ]; then
      mkdir -p "$JAVA_HOME"
      wget -O- --no-cookies -c --header "Cookie: oraclelicense=a" "$JDK_DOWNLOAD_URL" | tar -C "$JAVA_HOME" -zxf- --strip=1
    fi
    git --help >/dev/null 2>&1 || packages+=(git)
    if [ "${#packages[@]}" -gt 0 ]; then
      if [ "${#PKG_QUERY_INSTALLED[@]}" -gt 0 ]; then
        local n_packages=()
        local pkg
        for pkg in "${packages[@]}"; do
          if ! "${PKG_QUERY_INSTALLED[@]}" "$pkg" 2>/dev/null; then
            n_packages+=("$pkg")
          fi
        done
        packages=("${n_packages[@]}")
        unset n_packages
      fi
      if [ "${#packages[@]}" -gt 0 ]; then
        echo "Prepare install ${packages[*]}"
        "${PKG_UPDATE[@]}"
        "${PKG_ADD[@]}" "${packages[@]}"
      fi
    fi
  )
  [ $# -eq 0 ] || "$@"
}

usage() {
  echo "usage: env [ENV DEFINITION] $(dirname "$0")/$(basename "$0") actions [...arguments]"
  echo ""
  echo "Possible environment variables:"
  local indent="    "
  echo "${indent}ARCH=x86_64|i386"
  echo "${indent}BUILD_NATIVE=1"
  echo "${indent}JDK=oraclejdk8|openjdk8"
  echo ""
  echo "Possible actions:"
  echo "${indent}dependency: install dependencies"
  echo "${indent}mvnw: alias for \`./mvnw\` with defined environment variables"
  echo "${indent}git_config: config git user name and email, require env: GITHUB_ACTOR and GITHUB_TOKEN"
  echo "${indent}commit_native: commit native binaries"
  echo "${indent}push: push to remote git repository"
  return "$@"
}

mvnw() {
  (
    echo "JAVA_HOME: ${JAVA_HOME-}"
    set -x
    exec /bin/sh mvnw -B "${maven_extra_config[@]}" "$@"
  )
}

git_config() {
  local name email
  name="$(gh api --cache 5s "users/$GITHUB_ACTOR" -q '.name//.login')"
  email="$(gh api --cache 5s "users/$GITHUB_ACTOR" -q '.email//"\(.id)+\(.login)@users.noreply.github.com"')"
  [ -z "$name" ] || git config --global user.name "$name"
  [ -z "$email" ] || git config --global user.email "$email"
  [ $# -eq 0 ] || "$@"
}

push() {
  if ! git push; then
    git pull --rebase --autostash
    git push
  fi
}

commit_native() {
  local msg OS ARCH="${ARCH-}"
  OS="$(uname -s)"
  ARCH="${ARCH:-$(uname -m)}"

  case "$OS" in
  CYGWIN* | MINGW* | MSYS*) msg="Update Windows $ARCH binary" ;;
  Darwin) msg="Update $OS binary" ;;
  *) msg="Update $OS $ARCH binary" ;;
  esac

  cp -pr jnc-runtime/target/dist/* lib/
  if git diff --quiet -- lib; then
    info Binary not changed
  else
    git add lib
    git commit -m "$msg"
    [ $# -eq 0 ] || "$@"
  fi
}

[ $# -eq 0 ] || {
  set -e
  set -o pipefail
  [ "$(type -t "$1" 2>/dev/null)" = function ] || usage 1
  "$@"
}
