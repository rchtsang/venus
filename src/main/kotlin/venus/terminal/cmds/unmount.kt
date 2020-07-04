package venus.terminal.cmds

import venus.terminal.Command
import venus.terminal.Terminal

var umount = Command(
        name = "umount",
        execute = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): String {

            return ""
        },
        tab = fun(args: MutableList<String>, t: Terminal, sudo: Boolean): ArrayList<Any> {
            return arrayListOf("", ArrayList<String>())
        }
)