import sys
from _typeshed import structseq
from enum import IntEnum
from types import FrameType
from typing import Any, Callable, Iterable, Optional, Union
from typing_extensions import final

NSIG: int

class Signals(IntEnum):
    SIGABRT: int
    SIGEMT: int
    SIGFPE: int
    SIGILL: int
    SIGINFO: int
    SIGINT: int
    SIGSEGV: int
    SIGTERM: int

    if sys.platform == "win32":
        SIGBREAK: int
        CTRL_C_EVENT: int
        CTRL_BREAK_EVENT: int
    else:
        SIGALRM: int
        SIGBUS: int
        SIGCHLD: int
        SIGCONT: int
        SIGHUP: int
        SIGIO: int
        SIGIOT: int
        SIGKILL: int
        SIGPIPE: int
        SIGPROF: int
        SIGQUIT: int
        SIGSTOP: int
        SIGSYS: int
        SIGTRAP: int
        SIGTSTP: int
        SIGTTIN: int
        SIGTTOU: int
        SIGURG: int
        SIGUSR1: int
        SIGUSR2: int
        SIGVTALRM: int
        SIGWINCH: int
        SIGXCPU: int
        SIGXFSZ: int
        if sys.platform != "darwin":
            SIGCLD: int
            SIGPOLL: int
            SIGPWR: int
            SIGRTMAX: int
            SIGRTMIN: int

class Handlers(IntEnum):
    SIG_DFL: int
    SIG_IGN: int

SIG_DFL: Handlers
SIG_IGN: Handlers

_SIGNUM = Union[int, Signals]
_HANDLER = Union[Callable[[int, Optional[FrameType]], Any], int, Handlers, None]

def default_int_handler(signum: int, frame: FrameType | None) -> None: ...

if sys.version_info >= (3, 10):  # arguments changed in 3.10.2
    def getsignal(signalnum: _SIGNUM) -> _HANDLER: ...
    def signal(signalnum: _SIGNUM, handler: _HANDLER) -> _HANDLER: ...

else:
    def getsignal(__signalnum: _SIGNUM) -> _HANDLER: ...
    def signal(__signalnum: _SIGNUM, __handler: _HANDLER) -> _HANDLER: ...

SIGABRT: Signals
SIGEMT: Signals
SIGFPE: Signals
SIGILL: Signals
SIGINFO: Signals
SIGINT: Signals
SIGSEGV: Signals
SIGTERM: Signals

if sys.platform == "win32":
    SIGBREAK: Signals
    CTRL_C_EVENT: Signals
    CTRL_BREAK_EVENT: Signals
else:
    SIGALRM: Signals
    SIGBUS: Signals
    SIGCHLD: Signals
    SIGCONT: Signals
    SIGHUP: Signals
    SIGIO: Signals
    SIGIOT: Signals
    SIGKILL: Signals
    SIGPIPE: Signals
    SIGPROF: Signals
    SIGQUIT: Signals
    SIGSTOP: Signals
    SIGSYS: Signals
    SIGTRAP: Signals
    SIGTSTP: Signals
    SIGTTIN: Signals
    SIGTTOU: Signals
    SIGURG: Signals
    SIGUSR1: Signals
    SIGUSR2: Signals
    SIGVTALRM: Signals
    SIGWINCH: Signals
    SIGXCPU: Signals
    SIGXFSZ: Signals

    class ItimerError(IOError): ...
    ITIMER_PROF: int
    ITIMER_REAL: int
    ITIMER_VIRTUAL: int

    class Sigmasks(IntEnum):
        SIG_BLOCK: int
        SIG_UNBLOCK: int
        SIG_SETMASK: int
    SIG_BLOCK = Sigmasks.SIG_BLOCK
    SIG_UNBLOCK = Sigmasks.SIG_UNBLOCK
    SIG_SETMASK = Sigmasks.SIG_SETMASK
    def alarm(__seconds: int) -> int: ...
    def getitimer(__which: int) -> tuple[float, float]: ...
    def pause() -> None: ...
    def pthread_kill(__thread_id: int, __signalnum: int) -> None: ...
    if sys.version_info >= (3, 10):  # arguments changed in 3.10.2
        def pthread_sigmask(how: int, mask: Iterable[int]) -> set[_SIGNUM]: ...
    else:
        def pthread_sigmask(__how: int, __mask: Iterable[int]) -> set[_SIGNUM]: ...

    def setitimer(__which: int, __seconds: float, __interval: float = ...) -> tuple[float, float]: ...
    def siginterrupt(__signalnum: int, __flag: bool) -> None: ...
    def sigpending() -> Any: ...
    if sys.version_info >= (3, 10):  # argument changed in 3.10.2
        def sigwait(sigset: Iterable[int]) -> _SIGNUM: ...
    else:
        def sigwait(__sigset: Iterable[int]) -> _SIGNUM: ...
    if sys.platform != "darwin":
        SIGCLD: Signals
        SIGPOLL: Signals
        SIGPWR: Signals
        SIGRTMAX: Signals
        SIGRTMIN: Signals
        @final
        class struct_siginfo(structseq[int], tuple[int, int, int, int, int, int, int]):
            @property
            def si_signo(self) -> int: ...
            @property
            def si_code(self) -> int: ...
            @property
            def si_errno(self) -> int: ...
            @property
            def si_pid(self) -> int: ...
            @property
            def si_uid(self) -> int: ...
            @property
            def si_status(self) -> int: ...
            @property
            def si_band(self) -> int: ...

        def sigtimedwait(sigset: Iterable[int], timeout: float) -> struct_siginfo | None: ...
        def sigwaitinfo(sigset: Iterable[int]) -> struct_siginfo: ...

if sys.version_info >= (3, 8):
    def strsignal(__signalnum: _SIGNUM) -> str | None: ...
    def valid_signals() -> set[Signals]: ...
    def raise_signal(__signalnum: _SIGNUM) -> None: ...

if sys.version_info >= (3, 7):
    def set_wakeup_fd(fd: int, *, warn_on_full_buffer: bool = ...) -> int: ...

else:
    def set_wakeup_fd(fd: int) -> int: ...
