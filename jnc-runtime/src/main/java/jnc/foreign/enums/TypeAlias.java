package jnc.foreign.enums;

/**
 * Code should not depends on ordinal of this class, maybe changed in the future.
 */
public enum TypeAlias {
    /* on some platform sizeof(int) = 2 */
    cint,
    /* maybe sizeof(long) = 4 or 8 */
    clong,
    clock_t,
    /* enum on darwin */
    clockid_t,
    dev_t,
    /* use int instead */
    // errno_t,
    ino_t,
    // ino64_t,
    int16_t,
    int32_t,
    int64_t,
    int8_t,
    // intmax_t,
    intptr_t,
    mode_t,
    off_t,
    pid_t,
    ptrdiff_t,
    // rsize_t,
    size_t,
    socklen_t,
    ssize_t,
    time_t,
    uint16_t,
    uint32_t,
    uint64_t,
    uint8_t,
    // uintmax_t,
    uintptr_t,
    useconds_t,
    wchar_t,
    /*
     * linux: typedef int32_t *wctrans_t;
     * aix: typedef wint_t (*wctrans_t)();
     * solaris typedef unsigned int wctrans_t;
     * mingw typedef wchar_t wctrans_t;
     */
    wctrans_t,
    /* pointer type on OpenBSD */
    wctype_t,
    wint_t,
    // for non windows
    blkcnt_t,
    blksize_t,
    fsblkcnt_t,
    fsfilcnt_t,
    gid_t,
    id_t,
    in_addr_t,
    in_port_t,
    key_t,
    /* not an integer type */
    // mbstate_t,
    nlink_t,
    rlim_t,
    sa_family_t,
    /* maybe not an integer type */
    /* https://www.gnu.org/software/libc/manual/html_node/Signal-Sets.html */
    // sigset_t,
    suseconds_t,
    uid_t,
    /* BSD (DragonFly BSD, FreeBSD, OpenBSD, NetBSD, Darwin). ----------- */
    register_t,
    segsz_t,
    // darwin only
    ct_rune_t,
    rune_t,
    sae_associd_t,
    sae_connid_t,
    // swblk_t,
    syscall_arg_t,
    user_addr_t,
    user_long_t,
    user_off_t,
    user_size_t,
    user_ssize_t,
    user_time_t,
    user_ulong_t,
}
