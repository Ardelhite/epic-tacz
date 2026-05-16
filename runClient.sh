#!/bin/sh
set -e
cd "$(dirname "$0")"

# Resolve a JDK 17 install. Minecraft 1.20.1 / Forge 47.x requires Java 17.
# Priority: explicit JAVA_HOME (if it points at JDK 17) > /usr/libexec/java_home -v 17
# > Homebrew openjdk@17 > /Library/Java/JavaVirtualMachines/*jdk-17*.
resolve_java_home() {
    # 1. Honor caller-provided JAVA_HOME if it actually is JDK 17.
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        v=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n1)
        case "$v" in
            *\"17.*) echo "$JAVA_HOME"; return 0 ;;
        esac
    fi

    # 2. macOS java_home helper.
    if [ -x /usr/libexec/java_home ]; then
        if jh=$(/usr/libexec/java_home -v 17 2>/dev/null); then
            [ -n "$jh" ] && echo "$jh" && return 0
        fi
    fi

    # 3. Homebrew openjdk@17 (Apple Silicon then Intel prefix).
    for p in \
        /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
        /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home; do
        [ -x "$p/bin/java" ] && echo "$p" && return 0
    done

    # 4. Anything matching jdk-17 under /Library/Java.
    for p in /Library/Java/JavaVirtualMachines/*jdk-17*/Contents/Home; do
        [ -x "$p/bin/java" ] && echo "$p" && return 0
    done

    return 1
}

if jh=$(resolve_java_home); then
    export JAVA_HOME="$jh"
    export PATH="$JAVA_HOME/bin:$PATH"
else
    echo "runClient.sh: could not find a JDK 17 install." >&2
    echo "  install one via: brew install openjdk@17" >&2
    echo "  or set JAVA_HOME to point at a JDK 17 home." >&2
    exit 1
fi

exec ./gradlew runClient "$@"
