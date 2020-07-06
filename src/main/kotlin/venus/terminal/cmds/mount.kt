package venus.terminal.cmds

import venus.terminal.Command
import venus.terminal.Command.Companion.fileTabComplete
import venus.terminal.Terminal
import venus.vfs.VFSMountedDriveHandler

var mount = Command(
        name = "mount",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {
            if (args.size < 1) {
                return Command["mount"].help
            }
            var url = args[0]
            var dir = if (args.size > 1) {
                args[1]
            } else {
                "drive"
            }
            if (url == "local") {
                url = "http://localhost:6161"
            }
            if (!url.startsWith("http://")) {
                url = "http://" + url
            }
            try {
                val h = VFSMountedDriveHandler(url)
                val res = t.vfs.mountDrive(dir, h)
                t.vfs.save()
                return res
            } catch (e: Throwable) {
                return "mount: $e"
            }
            return ""
        },
        tab = ::fileTabComplete,
        help = """Allows you to mount external drives to the Venus web file system.
            |Usage: mount device dir
            |device is either a name of a device or url of a hosted drive.
            |dir is the dir you would like to mount to. By default, it will mount it in the current location with the name the drive gives it.
            |You can use dir to name the mount point/nest it in another folder.
        """.trimMargin()
)