require go_${PV}.inc

inherit cross

DEPENDS += " virtual/${TARGET_PREFIX}gcc"

do_compile() {
  ## Setting `$GOBIN` doesn't do any good, looks like it ends up copying binaries there.
  export GOROOT_FINAL="${SYSROOT}${libdir}/${PN}-${PV}"

  export GOHOSTOS="linux"
  export GOOS="linux"

  setup_go_arch

  export CGO_ENABLED="1"
  ## TODO: consider setting GO_EXTLINK_ENABLED

  export CC="${BUILD_CC}"
  export CC_FOR_TARGET="${TARGET_PREFIX}gcc ${TARGET_CC_ARCH} --sysroot=${STAGING_DIR_TARGET}"
  export CXX_FOR_TARGET="${TARGET_PREFIX}g++ ${TARGET_CC_ARCH} --sysroot=${STAGING_DIR_TARGET}"
  export GO_CCFLAGS="${HOST_CFLAGS}"
  export GO_LDFLAGS="${HOST_LDFLAGS}"

  cd src && bash -x ./make.bash

  ## The result is `go env` giving this:
  # GOARCH="amd64"
  # GOBIN=""
  # GOCHAR="6"
  # GOEXE=""
  # GOHOSTARCH="amd64"
  # GOHOSTOS="linux"
  # GOOS="linux"
  # GOPATH=""
  # GORACE=""
  # GOROOT="/home/build/poky/build/tmp/sysroots/x86_64-linux/usr/lib/cortexa8hf-vfp-neon-poky-linux-gnueabi/go"
  # GOTOOLDIR="/home/build/poky/build/tmp/sysroots/x86_64-linux/usr/lib/cortexa8hf-vfp-neon-poky-linux-gnueabi/go/pkg/tool/linux_amd64"
  ## The above is good, but these are a bit odd... especially the `-m64` flag.
  # CC="arm-poky-linux-gnueabi-gcc"
  # GOGCCFLAGS="-fPIC -m64 -pthread -fmessage-length=0"
  # CXX="arm-poky-linux-gnueabi-g++"
  ## TODO: test on C+Go project.
  # CGO_ENABLED="1"
}

do_install() {
  ## It turns out that `${D}${bindir}` is already populated by compilation script
  ## We need to copy the rest, unfortunatelly pretty much everything [1, 2].
  ##
  ## [1]: http://sources.gentoo.org/cgi-bin/viewvc.cgi/gentoo-x86/dev-lang/go/go-1.3.1.ebuild?view=markup)
  ## [2]: https://code.google.com/p/go/issues/detail?id=2775

  install -d "${D}${libdir}/${PN}-${PV}"
  tar -C "${WORKDIR}/go-${PV}/go/${dir}" -cf - bin include lib pkg src test |
  tar -C "${D}${libdir}/${PN}-${PV}" -xpf -
}

## TODO: implement do_clean() and ensure we actually do rebuild super cleanly
