package venus.glue.terminal

open class Command(
    val name: String,
    val execute: (parsedInput: MutableList<String>, t: Terminal, sudo: Boolean) -> String,
    val tab: (parsedInput: MutableList<String>, t: Terminal, sudo: Boolean) -> MutableList<String>
) {
    companion object {
        private val allCommands = arrayListOf<Command>()

        fun getCommands(): ArrayList<String> {
            val cmds = ArrayList<String>()
            for (cmd in this.allCommands) {
                cmds.add(cmd.name)
            }
            return cmds
        }

        operator fun get(name: String) =
                allCommands.firstOrNull { it.name == name }
                        ?: throw CommandNotFoundError(name)
    }

    init {
        allCommands.add(this)
    }

    override fun toString() = name
}